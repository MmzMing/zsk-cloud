package com.zsk.common.core.enums;

import lombok.Getter;

/**
 * 数据状态枚举
 *
 * @author zsk
 */
@Getter
public enum DataStatus {

    /**
     * 正常
     */
    NORMAL(0, "正常"),

    /**
     * 停用
     */
    DISABLED(1, "停用"),

    /**
     * 删除
     */
    DELETED(2, "已删除");

    private final Integer code;
    private final String desc;

    DataStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DataStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DataStatus status : DataStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
