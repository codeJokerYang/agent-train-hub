package com.agenttrainhub.security;

/**
 * 当前登录用户上下文，保存在 Spring Security 的 Authentication 中。
 *
 * @param id       用户 ID
 * @param username 登录名
 * @param role     角色：ADMIN / TEACHER / STUDENT
 */
public record UserPrincipal(Long id, String username, String role) {

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isTeacher() {
        return "TEACHER".equals(role);
    }

    public boolean isStudent() {
        return "STUDENT".equals(role);
    }

    /**
     * 数据权限：ADMIN 与 TEACHER 可访问全部数据，STUDENT 仅能访问自己的。
     * 数据集与训练任务统一采用该规则。
     */
    public boolean canAccessAllData() {
        return !isStudent();
    }
}
