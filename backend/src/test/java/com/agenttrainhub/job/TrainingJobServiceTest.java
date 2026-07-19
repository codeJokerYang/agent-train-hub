package com.agenttrainhub.job;

import com.agenttrainhub.artifact.ArtifactService;
import com.agenttrainhub.common.BizException;
import com.agenttrainhub.dataset.DatasetService;
import com.agenttrainhub.dataset.mapper.DatasetMapper;
import com.agenttrainhub.job.entity.TrainingJob;
import com.agenttrainhub.job.mapper.TrainingJobMapper;
import com.agenttrainhub.template.ModelTemplateService;
import com.agenttrainhub.template.mapper.ModelTemplateMapper;
import com.agenttrainhub.user.UserService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskRejectedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingJobServiceTest {

    @Mock TrainingJobMapper jobMapper;
    @Mock DatasetService datasetService;
    @Mock DatasetMapper datasetMapper;
    @Mock ModelTemplateService templateService;
    @Mock ModelTemplateMapper templateMapper;
    @Mock UserService userService;
    @Mock TrainingExecutor trainingExecutor;
    @Mock TrainingMetricService metricService;
    @Mock TrainingLogService logService;
    @Mock ArtifactService artifactService;
    @Mock JobAccessGuard jobAccessGuard;

    private TrainingJobService service;
    private TrainingJob pending;

    @BeforeAll
    static void initializeMybatisMetadata() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), "test"), TrainingJob.class);
    }

    @BeforeEach
    void setUp() {
        service = new TrainingJobService(
                jobMapper, datasetService, datasetMapper, templateService, templateMapper,
                userService, trainingExecutor, metricService, logService, artifactService,
                jobAccessGuard, new ObjectMapper());
        pending = new TrainingJob();
        pending.setId(7L);
        pending.setStatus(JobStatus.PENDING.name());
        pending.setTotalEpoch(20);
        when(jobAccessGuard.requireAccessible(7L)).thenReturn(pending);
    }

    @Test
    void startRejectsWhenAnotherRequestWonTheStateTransition() {
        when(jobMapper.update(
                org.mockito.ArgumentMatchers.<TrainingJob>isNull(),
                org.mockito.ArgumentMatchers.<Wrapper<TrainingJob>>any())).thenReturn(0);

        BizException error = assertThrows(BizException.class, () -> service.start(7L));

        assertEquals(409, error.getHttpStatus());
        verify(trainingExecutor, never()).submit(7L);
    }

    @Test
    void startMarksJobFailedWhenExecutorRejectsSubmission() {
        when(jobMapper.update(
                org.mockito.ArgumentMatchers.<TrainingJob>isNull(),
                org.mockito.ArgumentMatchers.<Wrapper<TrainingJob>>any())).thenReturn(1);
        doThrow(new TaskRejectedException("queue full")).when(trainingExecutor).submit(7L);

        BizException error = assertThrows(BizException.class, () -> service.start(7L));

        assertEquals(500, error.getHttpStatus());
        verify(jobMapper, times(2)).update(
                org.mockito.ArgumentMatchers.<TrainingJob>isNull(),
                org.mockito.ArgumentMatchers.<Wrapper<TrainingJob>>any());
    }
}
