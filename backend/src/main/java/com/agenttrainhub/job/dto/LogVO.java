package com.agenttrainhub.job.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 训练日志视图。
 */
@Data
public class LogVO {

    private Long id;
    private String level;
    private String message;
    private LocalDateTime createdAt;
}
