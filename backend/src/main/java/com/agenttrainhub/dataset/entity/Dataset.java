package com.agenttrainhub.dataset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据集实体，对应 datasets 表。
 */
@Data
@TableName("datasets")
public class Dataset {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ownerId;

    private String name;

    /** IMAGE / TABULAR / TEXT / ZIP / OTHER */
    private String type;

    private String storagePath;

    private Long fileSize;

    private String fileHash;

    /** Python/内置分析结果（JSON 字符串） */
    private String profileJson;

    /** READY / ANALYZING / ANALYZE_FAILED */
    private String status;

    private LocalDateTime createdAt;
}
