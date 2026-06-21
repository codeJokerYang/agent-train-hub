package com.agenttrainhub.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * 当前登录用户读取工具。
 *
 * <p>Service 层做数据权限校验时使用，避免只靠前端隐藏按钮（技术文档第 13 节）。</p>
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** 获取当前登录用户上下文。 */
    public static Optional<UserPrincipal> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    /** 获取当前登录用户 ID，未登录返回 null。 */
    public static Long currentUserId() {
        return currentUser().map(UserPrincipal::id).orElse(null);
    }

    /** 当前用户是否为某角色。 */
    public static boolean hasRole(String role) {
        return currentUser().map(u -> role.equals(u.role())).orElse(false);
    }
}
