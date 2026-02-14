package com.zsk.common.core.exception;

import com.zsk.common.core.enums.ResultCode;

import java.io.Serial;

/**
 * 认证异常
 *
 * @author wuhuaming
 */
public class AuthException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AuthException(String message) {
        super(ResultCode.AUTH_ERROR.getCode(), message);
    }

    public AuthException(String message, Throwable cause) {
        super(ResultCode.AUTH_ERROR.getCode(), message, cause);
    }

    public AuthException(ResultCode resultCode) {
        super(resultCode);
    }

    public static AuthException tokenExpired() {
        return new AuthException(ResultCode.TOKEN_EXPIRED);
    }

    public static AuthException tokenInvalid() {
        return new AuthException(ResultCode.TOKEN_INVALID);
    }

    public static AuthException captchaError() {
        return new AuthException(ResultCode.CAPTCHA_ERROR);
    }

    public static AuthException loginFail() {
        return new AuthException(ResultCode.LOGIN_FAIL);
    }
}
