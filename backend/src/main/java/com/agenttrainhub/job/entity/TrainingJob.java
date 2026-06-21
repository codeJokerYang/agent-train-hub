package com.agenttrainhub.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 训练任务实体，对应 training_jobs 表。
 */
@Data
@TableName("training_jobs")
public class TrainingJob {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ownerId;

    private Long datasetId;

    private Long templateId;

    private String taskName;

    /** PENDING / RUNNING / SUCCESS / FAILED / CANCELLED */
    private String status;

    private String paramsJson;

    private Integer progress;

    private Integer currentEpoch;

    private Integer totalEpoch;

    private String errorMessage;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private LocalDateTime createdAt;
}
