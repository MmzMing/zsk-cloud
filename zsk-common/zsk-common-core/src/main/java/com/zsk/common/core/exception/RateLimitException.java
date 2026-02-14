package com.zsk.common.core.exception;

import com.zsk.common.core.enums.ResultCode;

import java.io.Serial;

/**
 * 限流异常
 *
 * @author wuhuaming
 */
public class RateLimitException extends BaseException {

    public static final Integer CODE = ResultCode.RATE_LIMIT_ERROR.getCode();

    @Serial
    private static final long serialVersionUID = 1L;

    public RateLimitException(String message) {
        super(CODE, message);
    }

    public RateLimitException() {
        super(ResultCode.RATE_LIMIT_ERROR);
    }

    public static RateLimitException of() {
        return new RateLimitException();
    }

    public static RateLimitException of(String message) {
        return new RateLimitException(message);
    }
}
