package com.zsk.common.core.enums;

import lombok.Getter;

/**
 * 是/否枚举
 *
 * @author wuhuaming
 */
@Getter
public enum YesNo {

    /**
     * 是
     */
    YES("1", "是"),

    /**
     * 否
     */
    NO("0", "否");

    private final String code;
    private final String desc;

    YesNo(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isYes(String code) {
        return YES.getCode().equals(code);
    }

    public static boolean isNo(String code) {
        return NO.getCode().equals(code);
    }
}
