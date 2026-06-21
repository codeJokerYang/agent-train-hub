package com.agenttrainhub.job;

/**
 * 训练任务状态机。
 *
 * <pre>
 * [*] --> PENDING
 * PENDING/FAILED/SUCCESS/CANCELLED --> RUNNING   (start / rerun)
 * RUNNING --> SUCCESS | FAILED | CANCELLED
 * </pre>
 *
 * 规则：只有 PENDING / FAILED / SUCCESS / CANCELLED 可以 start/rerun；只有 RUNNING 可以 cancel。
 */
public enum JobStatus {

    PENDING,
    RUNNING,
    SUCCESS,
    FAILED,
    CANCELLED;

    /** 该状态是否可以启动 / 重跑。 */
    public static boolean canStart(String status) {
        return PENDING.name().equals(status)
                || FAILED.name().equals(status)
                || SUCCESS.name().equals(status)
                || CANCELLED.name().equals(status);
    }

    /** 该状态是否可以停止。 */
    public static boolean canCancel(String status) {
        return RUNNING.name().equals(status);
    }
}
