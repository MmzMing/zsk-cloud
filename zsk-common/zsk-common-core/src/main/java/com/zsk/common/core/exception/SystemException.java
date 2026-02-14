package com.zsk.common.core.exception;

import com.zsk.common.core.enums.ResultCode;

import java.io.Serial;

/**
 * 系统异常
 *
 * @author wuhuaming
 */
public class SystemException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SystemException(String message) {
        super(ResultCode.SYSTEM_ERROR.getCode(), message);
    }

    public SystemException(String message, Throwable cause) {
        super(ResultCode.SYSTEM_ERROR.getCode(), message, cause);
    }

    public SystemException(ResultCode resultCode) {
        super(resultCode);
    }

    public SystemException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }
}
