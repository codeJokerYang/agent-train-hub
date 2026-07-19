package com.agenttrainhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URI;
import java.util.List;

/**
 * CORS 配置。
 *
 * <p>供 Spring Security 的 {@code http.cors(...)} 使用。只接受显式的 HTTP(S) 源，
 * 避免「任意来源 + 凭据」组合扩大 JWT 与下载接口的攻击面。</p>
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class WebCorsConfig {

    private final CorsProperties properties;

    public WebCorsConfig(CorsProperties properties) {
        this.properties = properties;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> allowedOrigins = validateOrigins(properties.getAllowedOrigins());
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private static List<String> validateOrigins(List<String> origins) {
        if (origins == null || origins.isEmpty()) {
            throw new IllegalStateException("agenttrainhub.cors.allowed-origins 不能为空");
        }
        return origins.stream().map(String::trim).map(origin -> {
            URI uri;
            try {
                uri = URI.create(origin);
            } catch (IllegalArgumentException ex) {
                throw new IllegalStateException("非法 CORS 源: " + origin, ex);
            }
            if ((!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())))
                    || uri.getHost() == null || uri.getUserInfo() != null || uri.getPath() == null
                    || !uri.getPath().isEmpty() || uri.getQuery() != null || uri.getFragment() != null) {
                throw new IllegalStateException("CORS 源必须是完整的 HTTP(S) origin: " + origin);
            }
            return origin;
        }).distinct().toList();
    }
}
