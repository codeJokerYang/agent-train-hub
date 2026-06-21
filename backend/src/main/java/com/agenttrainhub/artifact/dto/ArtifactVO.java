package com.agenttrainhub.artifact.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模型产物视图。
 */
@Data
public class ArtifactVO {

    private Long id;
    private String artifactType;
    private String fileName;
    private Long fileSize;
    private LocalDateTime createdAt;
}
