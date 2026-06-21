package com.agenttrainhub.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置项，绑定 application.yml 中 {@code agenttrainhub.jwt.*}。
 */
@Component
@ConfigurationProperties(prefix = "agenttrainhub.jwt")
public class JwtProperties {

    /** 签名密钥，HS256 要求至少 32 字节。生产环境请通过环境变量覆盖。 */
    private String secret = "agent-train-hub-default-dev-secret-please-change-0123456789";

    /** 过期时间（分钟）。 */
    private long expireMinutes = 120;

    /** 请求头名称。 */
    private String header = "Authorization";

    /** Token 前缀。 */
    private String tokenPrefix = "Bearer ";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpireMinutes() {
        return expireMinutes;
    }

    public void setExpireMinutes(long expireMinutes) {
        this.expireMinutes = expireMinutes;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }
}
