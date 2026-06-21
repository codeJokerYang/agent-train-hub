package com.agenttrainhub.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 训练日志实体，对应 training_logs 表。
 */
@Data
@TableName("training_logs")
public class TrainingLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long jobId;

    /** INFO / WARN / ERROR */
    private String level;

    private String message;

    private LocalDateTime createdAt;
}
