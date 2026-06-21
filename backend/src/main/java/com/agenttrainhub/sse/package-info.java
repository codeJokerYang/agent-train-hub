/**
 * SSE 模块：训练进度与 Agent 输出的实时推送。
 *
 * <p>计划类：SseEmitterRegistry、TrainingEventPublisher。
 * 事件结构见技术文档第 8 节（ack / job_status / log / metric / artifact / error / done）。</p>
 */
package com.agenttrainhub.sse;
