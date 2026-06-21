package com.agenttrainhub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 生成与解析。
 *
 * <p>第一阶段提供完整的签发/校验能力，但登录流程（AuthController/AuthService）将在第二阶段接入。
 * payload 中包含 userId、role、username，对应技术文档第 13 节。</p>
 */
@Component
public class JwtTokenProvider {

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /** 生成 token。 */
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getExpireMinutes() * 60_000L);
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /** 解析 token，返回 claims；失败抛异常。 */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 校验 token 是否有效（签名正确且未过期）。 */
    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /** 从 claims 构造用户上下文。 */
    public UserPrincipal toUserPrincipal(Claims claims) {
        Number userId = claims.get("userId", Number.class);
        String role = claims.get("role", String.class);
        return new UserPrincipal(
                userId == null ? null : userId.longValue(),
                claims.getSubject(),
                role);
    }
}
