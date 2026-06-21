package com.agenttrainhub.common;

/**
 * 统一业务异常。
 *
 * <p>携带业务错误码与对应的 HTTP 状态码，由 {@link GlobalExceptionHandler} 统一转换为
 * {@link Result} 响应体。业务代码应抛出本异常而不是直接返回错误结构。</p>
 */
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 业务错误码（写入响应体 code 字段）。 */
    private final int code;
    /** 建议的 HTTP 状态码。 */
    private final int httpStatus;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }

    /**
     * 自定义业务错误码。
     *
     * @param code       业务错误码，例如 40901
     * @param httpStatus 建议的 HTTP 状态码，例如 409
     * @param message    错误提示
     */
    public BizException(int code, int httpStatus, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    /* ----------------------- 常用静态工厂 ----------------------- */

    public static BizException paramError(String message) {
        return new BizException(ErrorCode.PARAM_ERROR, message);
    }

    public static BizException unauthorized(String message) {
        return new BizException(ErrorCode.UNAUTHORIZED, message);
    }

    public static BizException forbidden(String message) {
        return new BizException(ErrorCode.FORBIDDEN, message);
    }

    public static BizException notFound(String message) {
        return new BizException(ErrorCode.NOT_FOUND, message);
    }

    public static BizException conflict(String message) {
        return new BizException(ErrorCode.CONFLICT, message);
    }
}
