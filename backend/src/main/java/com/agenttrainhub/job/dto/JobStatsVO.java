package com.agenttrainhub.job.dto;

import lombok.Data;

/**
 * 仪表盘任务统计（按当前用户的数据权限范围统计）。
 */
@Data
public class JobStatsVO {

    private long total;
    private long running;
    private long success;
    private long failed;
    private long pending;
    private long cancelled;
}
