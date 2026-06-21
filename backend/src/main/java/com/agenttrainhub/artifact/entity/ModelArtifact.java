package com.agenttrainhub.artifact.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模型产物实体，对应 model_artifacts 表。
 */
@Data
@TableName("model_artifacts")
public class ModelArtifact {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long jobId;

    /** MODEL / REPORT / LOG */
    private String artifactType;

    private String fileName;

    private String storagePath;

    private Long fileSize;

    private LocalDateTime createdAt;
}
