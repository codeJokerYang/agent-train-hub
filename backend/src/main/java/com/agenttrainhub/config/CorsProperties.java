package com.agenttrainhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * CORS 白名单配置。
 *
 * <p>默认仅允许本地 Vite 开发服务器。生产环境通过
 * {@code CORS_ALLOWED_ORIGINS} 提供逗号分隔的 HTTPS 源列表。</p>
 */
@ConfigurationProperties(prefix = "agenttrainhub.cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:5173"));

    public List<String> getAllowedOrigins() {
        return List.copyOf(allowedOrigins);
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins == null ? new ArrayList<>() : new ArrayList<>(allowedOrigins);
    }
}
