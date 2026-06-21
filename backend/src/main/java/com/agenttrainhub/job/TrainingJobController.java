package com.agenttrainhub.job;

import com.agenttrainhub.artifact.dto.ArtifactVO;
import com.agenttrainhub.common.PageQuery;
import com.agenttrainhub.common.PageResult;
import com.agenttrainhub.common.Result;
import com.agenttrainhub.job.dto.CreateJobRequest;
import com.agenttrainhub.job.dto.JobStatsVO;
import com.agenttrainhub.job.dto.LogVO;
import com.agenttrainhub.job.dto.MetricVO;
import com.agenttrainhub.job.dto.TrainingJobVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 训练任务接口。所有接口需登录；数据权限在 Service 层按 ownerId 校验。
 */
@RestController
@RequestMapping("/api/training-jobs")
public class TrainingJobController {

    private final TrainingJobService jobService;

    public TrainingJobController(TrainingJobService jobService) {
        this.jobService = jobService;
    }

    /** 创建任务（状态 PENDING）。 */
    @PostMapping
    public Result<TrainingJobVO> create(@Valid @RequestBody CreateJobRequest request) {
        return Result.ok(jobService.create(request));
    }

    /** 分页列表。 */
    @GetMapping
    public Result<PageResult<TrainingJobVO>> page(PageQuery query) {
        return Result.ok(jobService.page(query));
    }

    /** 仪表盘统计。 */
    @GetMapping("/stats")
    public Result<JobStatsVO> stats() {
        return Result.ok(jobService.stats());
    }

    /** 任务详情。 */
    @GetMapping("/{id}")
    public Result<TrainingJobVO> detail(@PathVariable Long id) {
        return Result.ok(jobService.detail(id));
    }

    /** 启动任务。 */
    @PostMapping("/{id}/start")
    public Result<TrainingJobVO> start(@PathVariable Long id) {
        return Result.ok(jobService.start(id));
    }

    /** 停止任务。 */
    @PostMapping("/{id}/cancel")
    public Result<TrainingJobVO> cancel(@PathVariable Long id) {
        return Result.ok(jobService.cancel(id));
    }

    /** 重跑任务。 */
    @PostMapping("/{id}/rerun")
    public Result<TrainingJobVO> rerun(@PathVariable Long id) {
        return Result.ok(jobService.rerun(id));
    }

    /** 指标列表。 */
    @GetMapping("/{id}/metrics")
    public Result<List<MetricVO>> metrics(@PathVariable Long id) {
        return Result.ok(jobService.metrics(id));
    }

    /** 日志分页。 */
    @GetMapping("/{id}/logs")
    public Result<PageResult<LogVO>> logs(@PathVariable Long id, PageQuery query) {
        return Result.ok(jobService.logs(id, query));
    }

    /** 产物列表。 */
    @GetMapping("/{id}/artifacts")
    public Result<List<ArtifactVO>> artifacts(@PathVariable Long id) {
        return Result.ok(jobService.artifacts(id));
    }
}
