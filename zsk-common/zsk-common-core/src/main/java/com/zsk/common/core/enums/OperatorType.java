package com.zsk.common.core.enums;

import lombok.Getter;

/**
 * 操作者类型枚举
 *
 * @author wuhuaming
 */
@Getter
public enum OperatorType {

    /**
     * 其他
     */
    OTHER(0, "其他"),

    /**
     * 后台用户
     */
    ADMIN(1, "后台用户"),

    /**
     * 移动端用户
     */
    MOBILE(2, "移动端用户"),

    /**
     * 小程序用户
     */
    MP(3, "小程序用户"),

    /**
     * 系统
     */
    SYSTEM(4, "系统");

    private final Integer code;
    private final String desc;

    OperatorType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
