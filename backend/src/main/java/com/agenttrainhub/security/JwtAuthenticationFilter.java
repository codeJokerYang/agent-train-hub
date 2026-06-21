package com.agenttrainhub.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器。
 *
 * <p>从请求头解析 token，校验通过后把用户上下文写入 SecurityContext。
 * 第一阶段 SecurityConfig 放行所有请求，因此即便没有携带 token 也不会被拦截；
 * 第二阶段接入真实登录后，本过滤器即为鉴权入口。任何解析异常都被吞掉并继续放行，
 * 避免过滤器抛出未捕获异常。</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final JwtProperties properties;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, JwtProperties properties) {
        this.tokenProvider = tokenProvider;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (token != null && tokenProvider.validate(token)) {
                Claims claims = tokenProvider.parseClaims(token);
                UserPrincipal principal = tokenProvider.toUserPrincipal(claims);
                if (principal.role() != null) {
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + principal.role()));
                    var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            // token 非法/过期：保持未认证状态，交由后续鉴权逻辑处理
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(properties.getHeader());
        String prefix = properties.getTokenPrefix();
        if (StringUtils.hasText(bearer) && bearer.startsWith(prefix)) {
            return bearer.substring(prefix.length()).trim();
        }
        return null;
    }
}
