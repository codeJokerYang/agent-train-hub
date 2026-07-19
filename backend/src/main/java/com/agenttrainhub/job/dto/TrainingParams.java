package com.agenttrainhub.job.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 训练参数。字段可空，服务端会补默认值（epochs=20、batchSize=16、learningRate=0.001、validationRatio=0.2）。
 */
@Data
public class TrainingParams {

    @Min(value = 1, message = "epochs 不得小于 1")
    private Integer epochs;

    @Min(value = 1, message = "batchSize 不得小于 1")
    private Integer batchSize;

    @DecimalMin(value = "0.0", inclusive = false, message = "learningRate 必须大于 0")
    private Double learningRate;

    @DecimalMin(value = "0.0", message = "validationRatio 不得小于 0")
    @DecimalMax(value = "1.0", inclusive = false, message = "validationRatio 必须小于 1")
    private Double validationRatio;
}
