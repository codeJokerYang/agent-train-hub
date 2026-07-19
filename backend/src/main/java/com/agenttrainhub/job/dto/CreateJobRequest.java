package com.agenttrainhub.job.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建训练任务请求。
 */
@Data
public class CreateJobRequest {

    @NotBlank(message = "任务名称不能为空")
    @Size(max = 128, message = "任务名称不能超过 128 个字符")
    private String taskName;

    @NotNull(message = "请选择数据集")
    private Long datasetId;

    @NotNull(message = "请选择模型模板")
    private Long templateId;

    @Valid
    private TrainingParams params;
}
