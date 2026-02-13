package com.zsk.common.core.enums;

import lombok.Getter;

/**
 * 用户状态枚举
 *
 * @author zsk
 */
@Getter
public enum UserStatus {

    /**
     * 正常
     */
    NORMAL("0", "正常"),

    /**
     * 停用
     */
    DISABLED("1", "停用"),

    /**
     * 锁定
     */
    LOCKED("2", "锁定"),

    /**
     * 过期
     */
    EXPIRED("3", "过期");

    private final String code;
    private final String desc;

    UserStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserStatus getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (UserStatus status : UserStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
