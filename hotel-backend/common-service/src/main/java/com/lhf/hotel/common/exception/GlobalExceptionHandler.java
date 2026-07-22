package com.lhf.hotel.common.exception;

import com.lhf.hotel.common.result.Result;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import feign.FeignException;

import java.net.ConnectException;

/**
 * 全局异常拦截 —— 各业务模块引入 common-service 后自动生效
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常 —— 根据 code 映射 HTTP 状态码 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException e) {
        log.warn("业务异常 — code={} msg={}", e.getCode(), e.getMessage());
        HttpStatus status = switch (e.getCode()) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return new ResponseEntity<>(Result.fail(e.getCode(), e.getMessage()), status);
    }

    /** 参数校验失败（@Valid） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        log.warn("参数校验失败 — {}", msg);
        return Result.fail(400, msg);
    }

    /** 单字段校验失败 */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        log.warn("参数校验失败 — {}", e.getMessage());
        return Result.fail(400, e.getMessage());
    }

    /** 静态资源 404（favicon.ico 等），不打印 error 日志 */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoResource(org.springframework.web.servlet.resource.NoResourceFoundException e) {
        return Result.fail(404, "资源不存在");
    }

    /** 文件大小超限 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("上传文件过大 — {}", e.getMessage());
        return Result.fail(400, "上传文件大小超过限制");
    }

    /** 文件上传通用异常 */
    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMultipart(MultipartException e) {
        log.warn("文件上传异常 — {}", e.getMessage());
        return Result.fail(400, "文件上传失败");
    }

    /** Feign 调用失败（下游服务返回错误） */
    @ExceptionHandler(FeignException.class)
    public Result<Void> handleFeign(FeignException e) {
        log.warn("Feign 调用失败 — status={} uri={} msg={}", e.status(), e.request().url(), e.getMessage());
        return Result.fail(502, "服务暂时不可用，请稍后重试");
    }

    /** 下游服务连接失败（网关路由 / Feign 调用） */
    @ExceptionHandler(ConnectException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result<Void> handleConnect(ConnectException e) {
        log.warn("下游服务不可用 — {}", e.getMessage());
        return Result.fail(503, "服务暂时不可用，请稍后重试");
    }

    /** 兜底 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleUnknown(Exception e) {
        log.error("未预期异常", e);
        return Result.fail(500, "服务器内部错误");
    }
}
