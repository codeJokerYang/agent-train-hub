package com.agenttrainhub.job;

import com.agenttrainhub.artifact.ArtifactService;
import com.agenttrainhub.job.entity.TrainingJob;
import com.agenttrainhub.job.mapper.TrainingJobMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * 模拟训练执行器。
 *
 * <p>不依赖 GPU / Python：在线程池里按 epoch 循环，逐轮写入日志、指标、进度；
 * 支持通过内存 cancel flag 停止。每轮 loss 缓慢下降、accuracy 缓慢上升，均带少量随机波动。
 * 训练成功后生成模型产物与训练报告。</p>
 */
@Component
public class TrainingExecutor {

    private static final Logger log = LoggerFactory.getLogger(TrainingExecutor.class);

    private final Executor executor;
    private final TrainingJobMapper jobMapper;
    private final TrainingLogService logService;
    private final TrainingMetricService metricService;
    private final ArtifactService artifactService;
    private final long epochIntervalMs;

    /** 内存停止标记。若已接入 Redis 也可改为分布式标记。 */
    private final ConcurrentHashMap<Long, Boolean> cancelFlags = new ConcurrentHashMap<>();

    public TrainingExecutor(@Qualifier("trainingTaskExecutor") Executor executor,
                            TrainingJobMapper jobMapper,
                            TrainingLogService logService,
                            TrainingMetricService metricService,
                            ArtifactService artifactService,
                            @Value("${agenttrainhub.training.epoch-interval-ms:1000}") long epochIntervalMs) {
        this.executor = executor;
        this.jobMapper = jobMapper;
        this.logService = logService;
        this.metricService = metricService;
        this.artifactService = artifactService;
        this.epochIntervalMs = epochIntervalMs;
    }

    /** 提交一个任务异步执行（调用方已把任务置为 RUNNING）。 */
    public void submit(Long jobId) {
        cancelFlags.remove(jobId);
        executor.execute(() -> run(jobId));
    }

    /** 请求停止：置 cancel flag，执行循环会在下一轮检测到并落 CANCELLED。 */
    public void requestCancel(Long jobId) {
        cancelFlags.put(jobId, Boolean.TRUE);
    }

    private void run(Long jobId) {
        try {
            TrainingJob job = jobMapper.selectById(jobId);
            if (job == null || !JobStatus.RUNNING.name().equals(job.getStatus())) {
                return;
            }
            int totalEpoch = (job.getTotalEpoch() != null && job.getTotalEpoch() > 0)
                    ? job.getTotalEpoch() : 20;
            logService.add(jobId, "INFO", "training started: totalEpoch=" + totalEpoch);

            Random random = new Random();
            double finalLoss = 1.0;
            double finalAccuracy = 0.5;

            for (int epoch = 1; epoch <= totalEpoch; epoch++) {
                if (isCancelled(jobId)) {
                    markCancelled(job, epoch);
                    return;
                }
                try {
                    Thread.sleep(epochIntervalMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    markCancelled(job, epoch);
                    return;
                }
                if (isCancelled(jobId)) {
                    markCancelled(job, epoch);
                    return;
                }

                double loss = Math.max(0.02, Math.exp(-0.15 * epoch) + (random.nextDouble() * 0.06 - 0.03));
                double accuracy = clamp(1.0 - loss + (random.nextDouble() * 0.04 - 0.02), 0.0, 0.99);
                int progress = (int) Math.round(epoch * 100.0 / totalEpoch);
                finalLoss = loss;
                finalAccuracy = accuracy;

                if (!updateProgress(jobId, epoch, progress)) {
                    return;
                }
                metricService.add(jobId, epoch, "loss", round(loss));
                metricService.add(jobId, epoch, "accuracy", round(accuracy));
                logService.add(jobId, "INFO",
                        String.format("epoch %d/%d - loss=%.4f acc=%.4f", epoch, totalEpoch, loss, accuracy));
            }

            artifactService.generateForSuccess(job, finalLoss, finalAccuracy);
            int completed = jobMapper.update(null, new LambdaUpdateWrapper<TrainingJob>()
                    .eq(TrainingJob::getId, jobId)
                    .eq(TrainingJob::getStatus, JobStatus.RUNNING.name())
                    .set(TrainingJob::getStatus, JobStatus.SUCCESS.name())
                    .set(TrainingJob::getProgress, 100)
                    .set(TrainingJob::getCurrentEpoch, totalEpoch)
                    .set(TrainingJob::getFinishedAt, LocalDateTime.now()));
            if (completed != 1) {
                artifactService.deleteByJob(jobId);
                return;
            }
            logService.add(jobId, "INFO", "training finished: SUCCESS");
        } catch (Exception ex) {
            log.error("training job {} failed", jobId, ex);
            failJob(jobId, ex);
        } finally {
            cancelFlags.remove(jobId);
        }
    }

    private boolean isCancelled(Long jobId) {
        return Boolean.TRUE.equals(cancelFlags.get(jobId));
    }

    private void markCancelled(TrainingJob job, int epoch) {
        int updated = jobMapper.update(null, new LambdaUpdateWrapper<TrainingJob>()
                .eq(TrainingJob::getId, job.getId())
                .eq(TrainingJob::getStatus, JobStatus.RUNNING.name())
                .set(TrainingJob::getStatus, JobStatus.CANCELLED.name())
                .set(TrainingJob::getFinishedAt, LocalDateTime.now()));
        if (updated == 1) {
            logService.add(job.getId(), "WARN", "training cancelled at epoch " + epoch);
        }
    }

    private void failJob(Long jobId, Exception ex) {
        int updated = jobMapper.update(null, new LambdaUpdateWrapper<TrainingJob>()
                .eq(TrainingJob::getId, jobId)
                .eq(TrainingJob::getStatus, JobStatus.RUNNING.name())
                .set(TrainingJob::getStatus, JobStatus.FAILED.name())
                .set(TrainingJob::getFinishedAt, LocalDateTime.now())
                .set(TrainingJob::getErrorMessage, truncate(ex.getMessage())));
        if (updated == 1) {
            logService.add(jobId, "ERROR", "training failed: " + ex.getMessage());
        }
    }

    private boolean updateProgress(Long jobId, int epoch, int progress) {
        return jobMapper.update(null, new LambdaUpdateWrapper<TrainingJob>()
                .eq(TrainingJob::getId, jobId)
                .eq(TrainingJob::getStatus, JobStatus.RUNNING.name())
                .set(TrainingJob::getCurrentEpoch, epoch)
                .set(TrainingJob::getProgress, progress)) == 1;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private static String truncate(String message) {
        if (message == null) {
            return "unknown error";
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
