package com.agenttrainhub.job;

import com.agenttrainhub.artifact.ArtifactService;
import com.agenttrainhub.job.entity.TrainingJob;
import com.agenttrainhub.job.mapper.TrainingJobMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrainingExecutorTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), "test"), TrainingJob.class);
    }

    @Test
    void stopsWithoutWritingMetricsWhenRunningStateWasLost() {
        TrainingJobMapper mapper = mock(TrainingJobMapper.class);
        TrainingLogService logs = mock(TrainingLogService.class);
        TrainingMetricService metrics = mock(TrainingMetricService.class);
        ArtifactService artifacts = mock(ArtifactService.class);
        TrainingJob job = new TrainingJob();
        job.setId(9L);
        job.setStatus(JobStatus.RUNNING.name());
        job.setTotalEpoch(1);
        when(mapper.selectById(9L)).thenReturn(job);
        when(mapper.update(
                org.mockito.ArgumentMatchers.<TrainingJob>isNull(),
                org.mockito.ArgumentMatchers.<Wrapper<TrainingJob>>any())).thenReturn(0);
        Executor direct = Runnable::run;
        TrainingExecutor executor = new TrainingExecutor(direct, mapper, logs, metrics, artifacts, 0);

        executor.submit(9L);

        verify(metrics, never()).add(anyLong(), anyInt(), anyString(), anyDouble());
        verify(artifacts, never()).generateForSuccess(any(TrainingJob.class), anyDouble(), anyDouble());
    }
}
