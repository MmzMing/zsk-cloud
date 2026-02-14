package com.zsk.common.core.handler;

import com.zsk.common.core.domain.R;
import com.zsk.common.core.enums.ResultCode;
import com.zsk.common.core.exception.BaseException;
import com.zsk.common.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ResponseStatusException;

/**
 * WebFlux 环境全局异常处理器 (适用于网关)
 *
 * @author wuhuaming
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class GlobalWebFluxExceptionHandler {

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(MethodNotAllowedException.class)
    public R<?> handleMethodNotAllowedException(MethodNotAllowedException e, ServerHttpRequest request) {
        String requestUri = request.getURI().getPath();
        log.error("请求地址'{}', 不支持'{}'请求", requestUri, e.getHttpMethod());
        return R.fail(ResultCode.METHOD_NOT_ALLOWED);
    }

    /**
     * HTTP 响应状态异常处理
     */
    @ExceptionHandler(ResponseStatusException.class)
    public R<?> handleResponseStatusException(ResponseStatusException e, ServerHttpRequest request) {
        String requestUri = request.getURI().getPath();
        if (requestUri.endsWith("favicon.ico")) {
            return R.fail(e.getStatusCode().value(), "favicon not found");
        }
        log.warn("请求地址'{}', 状态异常: {}, 原因: {}", requestUri, e.getStatusCode(), e.getReason());
        return R.fail(e.getStatusCode().value(), e.getReason() != null ? e.getReason() : "请求资源不存在或状态错误");
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public R<?> handleBusinessException(BusinessException e, ServerHttpRequest request) {
        log.error("业务异常: {}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 基础异常
     */
    @ExceptionHandler(BaseException.class)
    public R<?> handleBaseException(BaseException e, ServerHttpRequest request) {
        log.error("基础异常: {}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public R<?> handleRuntimeException(RuntimeException e, ServerHttpRequest request) {
        String requestUri = request.getURI().getPath();
        log.error("请求地址'{}', 发生未知运行时异常.", requestUri, e);
        return R.fail(ResultCode.SYSTEM_ERROR);
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e, ServerHttpRequest request) {
        String requestUri = request.getURI().getPath();
        log.error("请求地址'{}', 发生系统异常.", requestUri, e);
        return R.fail(ResultCode.SYSTEM_ERROR);
    }
}
