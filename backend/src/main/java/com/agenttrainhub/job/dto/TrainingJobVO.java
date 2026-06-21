package com.agenttrainhub.job.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 训练任务对外视图。
 */
@Data
public class TrainingJobVO {

    private Long id;
    private String taskName;
    private String status;
    private Integer progress;
    private Integer currentEpoch;
    private Integer totalEpoch;

    private Long datasetId;
    private String datasetName;
    private Long templateId;
    private String templateName;

    @JsonRawValue
    private String paramsJson;

    private Long ownerId;
    private String ownerName;
    private String errorMessage;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
}
