package com.agenttrainhub.job.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 训练指标视图（前端按 metricName 分组绘制曲线）。
 */
@Data
public class MetricVO {

    private Integer epoch;
    private Integer step;
    private String metricName;
    private Double metricValue;
    private LocalDateTime createdAt;
}
