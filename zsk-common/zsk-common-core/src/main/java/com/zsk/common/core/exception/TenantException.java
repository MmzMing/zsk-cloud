package com.zsk.common.core.exception;

import com.zsk.common.core.enums.ResultCode;

import java.io.Serial;

/**
 * 多租户异常
 *
 * @author zsk
 */
public class TenantException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TenantException(String message) {
        super(ResultCode.TENANT_ERROR.getCode(), message);
    }

    public TenantException(String message, Throwable cause) {
        super(ResultCode.TENANT_ERROR.getCode(), message, cause);
    }

    public TenantException(ResultCode resultCode) {
        super(resultCode);
    }

    public static TenantException notExist() {
        return new TenantException(ResultCode.TENANT_NOT_EXIST);
    }

    public static TenantException expired() {
        return new TenantException(ResultCode.TENANT_EXPIRED);
    }

    public static TenantException disabled() {
        return new TenantException(ResultCode.TENANT_DISABLED);
    }

    public static TenantException noPermission() {
        return new TenantException(ResultCode.TENANT_NO_PERMISSION);
    }
}
