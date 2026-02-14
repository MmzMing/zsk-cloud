package com.zsk.common.core.exception;

import com.zsk.common.core.enums.ResultCode;

import java.io.Serial;

/**
 * 幂等性异常
 *
 * @author wuhuaming
 */
public class IdempotencyException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public IdempotencyException(String message) {
        super(ResultCode.IDEMPOTENCY_ERROR.getCode(), message);
    }

    public IdempotencyException() {
        super(ResultCode.IDEMPOTENCY_ERROR);
    }

    public static IdempotencyException of() {
        return new IdempotencyException("重复请求，请稍后再试");
    }

    public static IdempotencyException of(String message) {
        return new IdempotencyException(message);
    }
}
