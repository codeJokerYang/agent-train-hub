/**
 * 训练任务模块：创建、启动、停止、重跑、状态机、指标与日志。
 *
 * <p>计划类：TrainingJobController、TrainingJobService、TrainingExecutor。
 * 状态机：PENDING → RUNNING → SUCCESS / FAILED / CANCELLED。第一阶段用线程池模拟训练。</p>
 */
package com.agenttrainhub.job;
