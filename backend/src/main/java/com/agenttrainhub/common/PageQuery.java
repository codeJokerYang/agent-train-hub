package com.agenttrainhub.common;

import lombok.Data;

/**
 * 通用分页查询参数（GET 查询参数自动绑定）。
 */
@Data
public class PageQuery {

    /** 页码，从 1 开始。 */
    private long pageNum = 1;

    /** 每页条数。 */
    private long pageSize = 10;

    /** 关键字（可选，按业务字段模糊匹配）。 */
    private String keyword;
}
