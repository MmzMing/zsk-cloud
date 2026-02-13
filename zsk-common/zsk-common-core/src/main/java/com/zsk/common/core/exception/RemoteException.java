package com.zsk.common.core.exception;

import com.zsk.common.core.enums.ResultCode;

import java.io.Serial;

/**
 * 远程调用异常
 *
 * @author zsk
 */
public class RemoteException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 服务名
     */
    private String serviceName;

    public RemoteException(String message) {
        super(ResultCode.REMOTE_ERROR.getCode(), message);
    }

    public RemoteException(String message, Throwable cause) {
        super(ResultCode.REMOTE_ERROR.getCode(), message, cause);
    }

    public RemoteException(String serviceName, String message) {
        super(ResultCode.REMOTE_ERROR.getCode(), "[" + serviceName + "] " + message);
        this.serviceName = serviceName;
    }

    public RemoteException(String serviceName, String message, Throwable cause) {
        super(ResultCode.REMOTE_ERROR.getCode(), "[" + serviceName + "] " + message, cause);
        this.serviceName = serviceName;
    }

    public static RemoteException of(String serviceName, String message) {
        return new RemoteException(serviceName, message);
    }

    public static RemoteException timeout(String serviceName) {
        return new RemoteException(serviceName, "服务调用超时");
    }

    public static RemoteException fallback(String serviceName) {
        return new RemoteException(serviceName, "服务降级");
    }

    public String getServiceName() {
        return serviceName;
    }
}
