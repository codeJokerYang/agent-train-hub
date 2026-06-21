package com.agenttrainhub.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 训练指标实体，对应 training_metrics 表。
 */
@Data
@TableName("training_metrics")
public class TrainingMetric {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long jobId;

    private Integer epoch;

    private Integer step;

    /** loss / accuracy / precision / recall */
    private String metricName;

    private Double metricValue;

    private LocalDateTime createdAt;
}
