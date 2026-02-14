package com.zsk.common.core.enums;

import lombok.Getter;

/**
 * 登录类型枚举
 *
 * @author wuhuaming
 */
@Getter
public enum LoginType {

    /**
     * 密码登录
     */
    PASSWORD(1, "密码登录"),

    /**
     * 短信登录
     */
    SMS(2, "短信登录"),

    /**
     * 邮箱登录
     */
    EMAIL(3, "邮箱登录"),

    /**
     * 扫码登录
     */
    QRCODE(4, "扫码登录"),

    /**
     * 第三方登录
     */
    THIRD_PARTY(5, "第三方登录"),

    /**
     * 单点登录
     */
    SSO(6, "单点登录");

    private final Integer code;
    private final String desc;

    LoginType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static LoginType getByCode(Integer code) {
        if (code == null) {
            return PASSWORD;
        }
        for (LoginType type : LoginType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return PASSWORD;
    }
}
