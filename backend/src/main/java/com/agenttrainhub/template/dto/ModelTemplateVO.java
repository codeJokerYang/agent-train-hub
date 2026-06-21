package com.agenttrainhub.template.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;

/**
 * 模型模板对外视图。两个 JSON 字段作为原始 JSON 内联返回。
 */
@Data
public class ModelTemplateVO {

    private Long id;
    private String code;
    private String name;
    private String algorithmType;

    @JsonRawValue
    private String defaultParamsJson;

    @JsonRawValue
    private String paramSchemaJson;
}
