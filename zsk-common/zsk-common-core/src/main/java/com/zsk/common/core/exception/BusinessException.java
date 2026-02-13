package com.zsk.common.core.exception;

import com.zsk.common.core.enums.ResultCode;

import java.io.Serial;

/**
 * 业务异常
 *
 * @author zsk
 */
public class BusinessException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(ResultCode.BIZ_ERROR.getCode(), message);
    }

    public BusinessException(String message, Throwable cause) {
        super(ResultCode.BIZ_ERROR.getCode(), message, cause);
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode);
    }

    public BusinessException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }

    public BusinessException(ResultCode resultCode, Object data) {
        super(resultCode, data);
    }
}
