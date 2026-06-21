package com.agenttrainhub.job;

import com.agenttrainhub.artifact.ArtifactService;
import com.agenttrainhub.artifact.dto.ArtifactVO;
import com.agenttrainhub.common.BizException;
import com.agenttrainhub.common.PageQuery;
import com.agenttrainhub.common.PageResult;
import com.agenttrainhub.dataset.DatasetService;
import com.agenttrainhub.dataset.entity.Dataset;
import com.agenttrainhub.dataset.mapper.DatasetMapper;
import com.agenttrainhub.job.dto.CreateJobRequest;
import com.agenttrainhub.job.dto.JobStatsVO;
import com.agenttrainhub.job.dto.LogVO;
import com.agenttrainhub.job.dto.MetricVO;
import com.agenttrainhub.job.dto.TrainingJobVO;
import com.agenttrainhub.job.dto.TrainingParams;
import com.agenttrainhub.job.entity.TrainingJob;
import com.agenttrainhub.job.mapper.TrainingJobMapper;
import com.agenttrainhub.security.UserPrincipal;
import com.agenttrainhub.template.ModelTemplateService;
import com.agenttrainhub.template.entity.ModelTemplate;
import com.agenttrainhub.template.mapper.ModelTemplateMapper;
import com.agenttrainhub.user.UserService;
import com.agenttrainhub.user.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 训练任务服务（状态机 + 数据权限编排）。
 *
 * <p>数据权限：ADMIN 与 TEACHER 可访问全部任务，STUDENT 仅自己的，校验集中在 {@link JobAccessGuard}。
 * 状态机规则见 {@link JobStatus}。</p>
 */
@Service
public class TrainingJobService {

    private static final int DEFAULT_EPOCHS = 20;
    private static final int DEFAULT_BATCH_SIZE = 16;
    private static final double DEFAULT_LR = 0.001;
    private static final double DEFAULT_VAL_RATIO = 0.2;

    private final TrainingJobMapper jobMapper;
    private final DatasetService datasetService;
    private final DatasetMapper datasetMapper;
    private final ModelTemplateService templateService;
    private final ModelTemplateMapper templateMapper;
    private final UserService userService;
    private final TrainingExecutor trainingExecutor;
    private final TrainingMetricService metricService;
    private final TrainingLogService logService;
    private final ArtifactService artifactService;
    private final JobAccessGuard jobAccessGuard;
    private final ObjectMapper objectMapper;

    public TrainingJobService(TrainingJobMapper jobMapper,
                              DatasetService datasetService,
                              DatasetMapper datasetMapper,
                              ModelTemplateService templateService,
                              ModelTemplateMapper templateMapper,
                              UserService userService,
                              TrainingExecutor trainingExecutor,
                              TrainingMetricService metricService,
                              TrainingLogService logService,
                              ArtifactService artifactService,
                              JobAccessGuard jobAccessGuard,
                              ObjectMapper objectMapper) {
        this.jobMapper = jobMapper;
        this.datasetService = datasetService;
        this.datasetMapper = datasetMapper;
        this.templateService = templateService;
        this.templateMapper = templateMapper;
        this.userService = userService;
        this.trainingExecutor = trainingExecutor;
        this.metricService = metricService;
        this.logService = logService;
        this.artifactService = artifactService;
        this.jobAccessGuard = jobAccessGuard;
        this.objectMapper = objectMapper;
    }

    /* ----------------------- 创建 / 查询 ----------------------- */

    public TrainingJobVO create(CreateJobRequest request) {
        UserPrincipal me = jobAccessGuard.currentUser();
        // 数据集与模板均需校验：数据集走数据权限，模板需存在且启用
        Dataset dataset = datasetService.requireAccessible(request.getDatasetId());
        ModelTemplate template = templateService.requireUsable(request.getTemplateId());

        TrainingParams p = request.getParams();
        int epochs = (p != null && p.getEpochs() != null) ? p.getEpochs() : DEFAULT_EPOCHS;
        if (epochs < 1) {
            throw BizException.paramError("epochs 不得小于 1");
        }
        int batchSize = (p != null && p.getBatchSize() != null) ? p.getBatchSize() : DEFAULT_BATCH_SIZE;
        double learningRate = (p != null && p.getLearningRate() != null) ? p.getLearningRate() : DEFAULT_LR;
        double validationRatio = (p != null && p.getValidationRatio() != null) ? p.getValidationRatio() : DEFAULT_VAL_RATIO;

        TrainingJob job = new TrainingJob();
        job.setOwnerId(me.id());
        job.setDatasetId(dataset.getId());
        job.setTemplateId(template.getId());
        job.setTaskName(request.getTaskName());
        job.setStatus(JobStatus.PENDING.name());
        job.setParamsJson(buildParamsJson(epochs, batchSize, learningRate, validationRatio));
        job.setProgress(0);
        job.setCurrentEpoch(0);
        job.setTotalEpoch(epochs);
        job.setCreatedAt(LocalDateTime.now());
        jobMapper.insert(job);

        return detailInternal(job.getId());
    }

    public PageResult<TrainingJobVO> page(PageQuery query) {
        UserPrincipal me = jobAccessGuard.currentUser();
        long pageNum = Math.max(1, query.getPageNum());
        long pageSize = Math.min(100, Math.max(1, query.getPageSize()));

        LambdaQueryWrapper<TrainingJob> wrapper = scopeWrapper(me);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(TrainingJob::getTaskName, query.getKeyword());
        }
        wrapper.orderByDesc(TrainingJob::getId);

        Page<TrainingJob> result = jobMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(),
                enrich(result.getRecords()));
    }

    public TrainingJobVO detail(Long id) {
        TrainingJob job = jobAccessGuard.requireAccessible(id);
        return enrich(List.of(job)).get(0);
    }

    public List<MetricVO> metrics(Long id) {
        jobAccessGuard.requireAccessible(id);
        return metricService.listByJob(id);
    }

    public PageResult<LogVO> logs(Long id, PageQuery query) {
        jobAccessGuard.requireAccessible(id);
        return logService.page(id, query.getPageNum(), query.getPageSize());
    }

    public List<ArtifactVO> artifacts(Long id) {
        jobAccessGuard.requireAccessible(id);
        return artifactService.listByJob(id);
    }

    public JobStatsVO stats() {
        UserPrincipal me = jobAccessGuard.currentUser();
        JobStatsVO vo = new JobStatsVO();
        vo.setTotal(jobMapper.selectCount(scopeWrapper(me)));
        vo.setRunning(countByStatus(me, JobStatus.RUNNING));
        vo.setSuccess(countByStatus(me, JobStatus.SUCCESS));
        vo.setFailed(countByStatus(me, JobStatus.FAILED));
        vo.setPending(countByStatus(me, JobStatus.PENDING));
        vo.setCancelled(countByStatus(me, JobStatus.CANCELLED));
        return vo;
    }

    /* ----------------------- 状态机操作 ----------------------- */

    public TrainingJobVO start(Long id) {
        TrainingJob job = jobAccessGuard.requireAccessible(id);
        assertStartable(job);
        resetAndSubmit(job);
        return detailInternal(id);
    }

    public TrainingJobVO rerun(Long id) {
        TrainingJob job = jobAccessGuard.requireAccessible(id);
        assertStartable(job);
        // 清空历史指标 / 日志 / 产物，重新开始
        metricService.deleteByJob(id);
        logService.deleteByJob(id);
        artifactService.deleteByJob(id);
        logService.add(id, "INFO", "rerun: previous metrics/logs/artifacts cleared");
        resetAndSubmit(job);
        return detailInternal(id);
    }

    public TrainingJobVO cancel(Long id) {
        TrainingJob job = jobAccessGuard.requireAccessible(id);
        if (!JobStatus.canCancel(job.getStatus())) {
            throw BizException.conflict("当前任务状态为 " + job.getStatus() + "，只能停止运行中的任务");
        }
        trainingExecutor.requestCancel(id);
        return detailInternal(id);
    }

    /* ----------------------- 内部辅助 ----------------------- */

    private void assertStartable(TrainingJob job) {
        if (!JobStatus.canStart(job.getStatus())) {
            throw BizException.conflict("当前任务状态为 " + job.getStatus() + "，不能启动或重跑");
        }
    }

    private void resetAndSubmit(TrainingJob job) {
        int total = (job.getTotalEpoch() != null && job.getTotalEpoch() > 0)
                ? job.getTotalEpoch() : DEFAULT_EPOCHS;
        jobMapper.update(null, new LambdaUpdateWrapper<TrainingJob>()
                .eq(TrainingJob::getId, job.getId())
                .set(TrainingJob::getStatus, JobStatus.RUNNING.name())
                .set(TrainingJob::getStartedAt, LocalDateTime.now())
                .set(TrainingJob::getFinishedAt, null)
                .set(TrainingJob::getProgress, 0)
                .set(TrainingJob::getCurrentEpoch, 0)
                .set(TrainingJob::getErrorMessage, null)
                .set(TrainingJob::getTotalEpoch, total));
        trainingExecutor.submit(job.getId());
    }

    private TrainingJobVO detailInternal(Long id) {
        return enrich(List.of(jobMapper.selectById(id))).get(0);
    }

    private LambdaQueryWrapper<TrainingJob> scopeWrapper(UserPrincipal me) {
        LambdaQueryWrapper<TrainingJob> wrapper = new LambdaQueryWrapper<>();
        if (!me.canAccessAllData()) {
            wrapper.eq(TrainingJob::getOwnerId, me.id());
        }
        return wrapper;
    }

    private long countByStatus(UserPrincipal me, JobStatus status) {
        return jobMapper.selectCount(scopeWrapper(me).eq(TrainingJob::getStatus, status.name()));
    }

    private String buildParamsJson(int epochs, int batchSize, double learningRate, double validationRatio) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("epochs", epochs);
        params.put("batchSize", batchSize);
        params.put("learningRate", learningRate);
        params.put("validationRatio", validationRatio);
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException ex) {
            throw new BizException(com.agenttrainhub.common.ErrorCode.INTERNAL_ERROR, "参数序列化失败");
        }
    }

    private List<TrainingJobVO> enrich(List<TrainingJob> jobs) {
        if (jobs.isEmpty()) {
            return List.of();
        }
        Set<Long> datasetIds = jobs.stream().map(TrainingJob::getDatasetId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> templateIds = jobs.stream().map(TrainingJob::getTemplateId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> ownerIds = jobs.stream().map(TrainingJob::getOwnerId)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> datasetNames = datasetNames(datasetIds);
        Map<Long, String> templateNames = templateNames(templateIds);
        Map<Long, User> owners = userService.mapByIds(ownerIds);

        return jobs.stream().map(job -> toVO(job, datasetNames, templateNames, owners)).toList();
    }

    private Map<Long, String> datasetNames(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return datasetMapper.selectList(new LambdaQueryWrapper<Dataset>()
                        .select(Dataset::getId, Dataset::getName)
                        .in(Dataset::getId, ids))
                .stream().collect(Collectors.toMap(Dataset::getId, Dataset::getName));
    }

    private Map<Long, String> templateNames(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return templateMapper.selectList(new LambdaQueryWrapper<ModelTemplate>()
                        .select(ModelTemplate::getId, ModelTemplate::getName)
                        .in(ModelTemplate::getId, ids))
                .stream().collect(Collectors.toMap(ModelTemplate::getId, ModelTemplate::getName));
    }

    private TrainingJobVO toVO(TrainingJob job, Map<Long, String> datasetNames,
                               Map<Long, String> templateNames, Map<Long, User> owners) {
        TrainingJobVO vo = new TrainingJobVO();
        vo.setId(job.getId());
        vo.setTaskName(job.getTaskName());
        vo.setStatus(job.getStatus());
        vo.setProgress(job.getProgress());
        vo.setCurrentEpoch(job.getCurrentEpoch());
        vo.setTotalEpoch(job.getTotalEpoch());
        vo.setDatasetId(job.getDatasetId());
        vo.setDatasetName(job.getDatasetId() == null ? null : datasetNames.get(job.getDatasetId()));
        vo.setTemplateId(job.getTemplateId());
        vo.setTemplateName(job.getTemplateId() == null ? null : templateNames.get(job.getTemplateId()));
        vo.setParamsJson(job.getParamsJson());
        vo.setOwnerId(job.getOwnerId());
        User owner = owners.get(job.getOwnerId());
        vo.setOwnerName(owner == null ? null
                : (StringUtils.hasText(owner.getRealName()) ? owner.getRealName() : owner.getUsername()));
        vo.setErrorMessage(job.getErrorMessage());
        vo.setStartedAt(job.getStartedAt());
        vo.setFinishedAt(job.getFinishedAt());
        vo.setCreatedAt(job.getCreatedAt());
        return vo;
    }
}
