package com.agenttrainhub.common;

/**
 * 预定义错误码。
 *
 * <p>{@code code} 为业务错误码（对外返回到响应体），{@code httpStatus} 为建议的 HTTP 状态码。
 * 业务模块也可以在 {@link BizException} 中使用更细粒度的自定义错误码，例如 {@code 40901}
 * 表示「非 RUNNING 任务不能停止」。</p>
 */
public enum ErrorCode {

    SUCCESS(0, 200, "ok"),
    PARAM_ERROR(400, 400, "参数错误"),
    UNAUTHORIZED(401, 401, "未登录或登录已过期"),
    FORBIDDEN(403, 403, "无权限访问"),
    NOT_FOUND(404, 404, "资源不存在"),
    CONFLICT(409, 409, "资源状态冲突"),
    INTERNAL_ERROR(500, 500, "系统内部错误");

    private final int code;
    private final int httpStatus;
    private final String message;

    ErrorCode(int code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
