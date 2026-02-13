package com.zsk.common.core.exception;

import com.zsk.common.core.enums.ResultCode;

import java.io.Serial;

/**
 * 数据不存在异常
 *
 * @author zsk
 */
public class DataNotFoundException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DataNotFoundException(String message) {
        super(ResultCode.DATA_NOT_EXIST.getCode(), message);
    }

    public DataNotFoundException(String dataType, Object dataId) {
        super(ResultCode.DATA_NOT_EXIST.getCode(), dataType + "不存在: " + dataId);
    }

    public static DataNotFoundException of(String message) {
        return new DataNotFoundException(message);
    }

    public static DataNotFoundException of(String dataType, Object dataId) {
        return new DataNotFoundException(dataType, dataId);
    }
}
