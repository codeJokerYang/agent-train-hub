package com.agenttrainhub.dataset.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据集对外视图。
 */
@Data
public class DatasetVO {

    private Long id;
    private String name;
    private String type;
    private Long fileSize;
    private String fileHash;
    private String status;
    private Long ownerId;
    private String ownerName;

    /** 分析结果，作为原始 JSON 内联返回（null 时输出 null）。 */
    @JsonRawValue
    private String profileJson;

    private LocalDateTime createdAt;
}
