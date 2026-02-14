package com.zsk.common.core.enums;

import cn.hutool.core.util.DesensitizedUtil;

import java.util.function.Function;

/**
 * 脱敏策略
 *
 * @author wuhuaming
 */
public enum SensitiveStrategy {
    /**
     * 姓名，第2位开始脱敏
     */
    CHINESE_NAME(s -> DesensitizedUtil.chineseName(s)),

    /**
     * 身份证号，保留前6位和后4位
     */
    ID_CARD(s -> DesensitizedUtil.idCardNum(s, 6, 4)),

    /**
     * 手机号，保留前3位和后4位
     */
    PHONE(s -> DesensitizedUtil.mobilePhone(s)),

    /**
     * 电子邮箱，隐藏@前面的部分
     */
    EMAIL(s -> DesensitizedUtil.email(s)),

    /**
     * 密码，全部掩盖
     */
    PASSWORD(s -> "******");

    private final Function<String, String> desensitizer;

    SensitiveStrategy(Function<String, String> desensitizer) {
        this.desensitizer = desensitizer;
    }

    public Function<String, String> desensitizer() {
        return desensitizer;
    }
}
