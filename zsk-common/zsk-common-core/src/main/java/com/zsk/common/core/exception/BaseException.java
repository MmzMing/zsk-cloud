package com.zsk.common.core.exception;

import com.zsk.common.core.enums.ResultCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 基础异常类
 *
 * @author wuhuaming
 */
@Getter
public class BaseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 异常码
     */
    private Integer code;

    /**
     * 异常消息
     */
    private String message;

    /**
     * 异常数据
     */
    private Object data;

    public BaseException(String message) {
        super(message);
        this.code = ResultCode.SYSTEM_ERROR.getCode();
        this.message = message;
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.SYSTEM_ERROR.getCode();
        this.message = message;
    }

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BaseException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public BaseException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public BaseException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public BaseException(ResultCode resultCode, Object data) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
    }

    public BaseException code(Integer code) {
        this.code = code;
        return this;
    }

    public BaseException data(Object data) {
        this.data = data;
        return this;
    }
}
