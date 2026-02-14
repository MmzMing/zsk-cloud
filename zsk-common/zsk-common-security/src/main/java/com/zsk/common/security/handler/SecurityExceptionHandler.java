package com.zsk.common.security.handler;

import com.zsk.common.core.domain.R;
import com.zsk.common.core.exception.AuthException;
import com.zsk.common.core.exception.PermissionException;
import com.zsk.common.core.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

/**
 * 安全模块异常处理器
 *
 * @author wuhuaming
 */
@Slf4j
@RestControllerAdvice
public class SecurityExceptionHandler {

    /**
     * 权限异常
     */
    @ExceptionHandler(PermissionException.class)
    public R<?> handlePermissionException(PermissionException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}', 权限校验失败'{}'", requestUri, e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 认证异常
     */
    @ExceptionHandler(AuthException.class)
    public R<?> handleAuthException(AuthException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}', 认证失败'{}', 无法访问系统资源", requestUri, e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(BindException.class)
    public R<?> handleBindException(BindException e) {
        log.error(e.getMessage());
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return R.fail(message);
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage());
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return R.fail(message);
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(ValidationException.class)
    public R<?> handleValidationException(ValidationException e) {
        log.error(e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }
}
