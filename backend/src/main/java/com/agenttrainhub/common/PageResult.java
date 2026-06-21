package com.agenttrainhub.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 统一分页结果。
 *
 * @param <T> 列表元素类型
 */
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页码，从 1 开始。 */
    private long pageNum;
    /** 每页条数。 */
    private long pageSize;
    /** 总记录数。 */
    private long total;
    /** 当前页数据。 */
    private List<T> records;

    public PageResult() {
        this.records = Collections.emptyList();
    }

    public PageResult(long pageNum, long pageSize, long total, List<T> records) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.records = records == null ? Collections.emptyList() : records;
    }

    public static <T> PageResult<T> of(long pageNum, long pageSize, long total, List<T> records) {
        return new PageResult<>(pageNum, pageSize, total, records);
    }

    /** 空分页。 */
    public static <T> PageResult<T> empty(long pageNum, long pageSize) {
        return new PageResult<>(pageNum, pageSize, 0L, Collections.emptyList());
    }

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
