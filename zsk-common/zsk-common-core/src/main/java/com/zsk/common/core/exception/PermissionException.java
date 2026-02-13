package com.zsk.common.core.exception;

import com.zsk.common.core.enums.ResultCode;

import java.io.Serial;

/**
 * 权限异常
 *
 * @author zsk
 */
public class PermissionException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PermissionException(String message) {
        super(ResultCode.PERMISSION_DENIED.getCode(), message);
    }

    public PermissionException(String message, Throwable cause) {
        super(ResultCode.PERMISSION_DENIED.getCode(), message, cause);
    }

    public PermissionException(ResultCode resultCode) {
        super(resultCode);
    }

    public static PermissionException denied() {
        return new PermissionException(ResultCode.PERMISSION_DENIED);
    }

    public static PermissionException accessDenied() {
        return new PermissionException(ResultCode.ACCESS_DENIED);
    }

    public static PermissionException dataScopeDenied() {
        return new PermissionException("没有该数据的操作权限");
    }
}
