package com.zsk.common.core.enums;

import lombok.Getter;

/**
 * 性别枚举
 *
 * @author zsk
 */
@Getter
public enum Gender {

    /**
     * 未知
     */
    UNKNOWN("0", "未知"),

    /**
     * 男
     */
    MALE("1", "男"),

    /**
     * 女
     */
    FEMALE("2", "女");

    private final String code;
    private final String desc;

    Gender(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static Gender getByCode(String code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (Gender gender : Gender.values()) {
            if (gender.getCode().equals(code)) {
                return gender;
            }
        }
        return UNKNOWN;
    }
}
