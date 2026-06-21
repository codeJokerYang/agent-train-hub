package com.agenttrainhub.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查 / 探活接口。
 *
 * <p>第一阶段用于验证骨架可启动、统一响应可用：{@code GET /api/ping}。</p>
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @Value("${spring.application.name:agent-train-hub}")
    private String appName;

    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("app", appName);
        data.put("status", "UP");
        data.put("phase", "phase-1-skeleton");
        data.put("time", LocalDateTime.now().toString());
        return Result.ok(data);
    }
}
