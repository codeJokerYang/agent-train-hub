package com.agenttrainhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AgentTrainHub 启动类。
 *
 * <p>Agent+ 算法训练与实验管理平台后端入口。第一阶段仅搭建骨架，
 * 业务模块（dataset / job / agent 等）将在后续阶段填充。</p>
 */
@EnableAsync
@SpringBootApplication
public class AgentTrainHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentTrainHubApplication.class, args);
    }
}
