package com.zsk.common.core.exception;

import com.zsk.common.core.enums.ResultCode;

import java.io.Serial;

/**
 * 参数校验异常
 *
 * @author zsk
 */
public class ValidationException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(ResultCode.PARAM_ERROR.getCode(), message);
    }

    public ValidationException(String message, Throwable cause) {
        super(ResultCode.PARAM_ERROR.getCode(), message, cause);
    }

    public ValidationException(ResultCode resultCode) {
        super(resultCode);
    }

    public static ValidationException paramError(String message) {
        return new ValidationException(message);
    }

    public static ValidationException paramMissing(String paramName) {
        return new ValidationException("缺少必要参数: " + paramName);
    }

    public static ValidationException paramTypeError(String paramName) {
        return new ValidationException("参数类型错误: " + paramName);
    }
}
