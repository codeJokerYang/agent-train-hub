package com.agenttrainhub.template.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模型模板实体，对应 model_templates 表。
 */
@Data
@TableName("model_templates")
public class ModelTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** YOLO_DEMO / IMAGE_CLASSIFY_DEMO / TEXT_CLASSIFY_DEMO */
    private String code;

    private String name;

    /** CLASSIFICATION / OBJECT_DETECTION / TEXT_CLASSIFICATION */
    private String algorithmType;

    private String defaultParamsJson;

    private String paramSchemaJson;

    /** 1 启用，0 停用 */
    private Integer enabled;

    private LocalDateTime createdAt;
}
