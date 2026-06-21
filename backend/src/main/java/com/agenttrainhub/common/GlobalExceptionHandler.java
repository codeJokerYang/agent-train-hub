package com.agenttrainhub.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

/**
 * 全局异常处理。
 *
 * <p>把业务异常、参数校验异常和未捕获异常统一转换为 {@link Result} 响应体，
 * 并设置合理的 HTTP 状态码。任何异常都不应直接以 500 堆栈返回给前端。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常。 */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBizException(BizException ex) {
        log.warn("业务异常: code={}, message={}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus())
                .body(Result.fail(ex.getCode(), ex.getMessage()));
    }

    /** @Valid 请求体校验失败。 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .collect(Collectors.joining("; "));
        if (message.isEmpty()) {
            message = ErrorCode.PARAM_ERROR.getMessage();
        }
        return ResponseEntity.status(ErrorCode.PARAM_ERROR.getHttpStatus())
                .body(Result.fail(ErrorCode.PARAM_ERROR.getCode(), message));
    }

    /** 方法参数 @Validated 校验失败。 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(ErrorCode.PARAM_ERROR.getHttpStatus())
                .body(Result.fail(ErrorCode.PARAM_ERROR.getCode(), message));
    }

    /** 缺少必填请求参数。 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        String message = "缺少必填参数: " + ex.getParameterName();
        return ResponseEntity.status(ErrorCode.PARAM_ERROR.getHttpStatus())
                .body(Result.fail(ErrorCode.PARAM_ERROR.getCode(), message));
    }

    /** 上传文件超过大小限制。 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result<Void>> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(ErrorCode.PARAM_ERROR.getHttpStatus())
                .body(Result.fail(ErrorCode.PARAM_ERROR.getCode(), "上传文件超过大小限制（默认 500MB）"));
    }

    /** 兜底异常。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception ex) {
        log.error("系统异常", ex);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(Result.fail(ErrorCode.INTERNAL_ERROR));
    }

    private static String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
