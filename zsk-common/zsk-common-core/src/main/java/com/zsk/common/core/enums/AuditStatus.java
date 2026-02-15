package com.zsk.common.core.enums;

import lombok.Getter;

/**
 * 审核状态枚举
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Getter
public enum AuditStatus {

    /** 待审核 */
    PENDING(0, "pending", "待审核"),

    /** 审核通过 */
    APPROVED(1, "approved", "审核通过"),

    /** 审核驳回 */
    REJECTED(2, "rejected", "审核驳回");

    private final Integer code;
    private final String value;
    private final String desc;

    AuditStatus(Integer code, String value, String desc) {
        this.code = code;
        this.value = value;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 状态码
     * @return 枚举
     */
    public static AuditStatus getByCode(Integer code) {
        if (code == null) {
            return PENDING;
        }
        for (AuditStatus status : AuditStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PENDING;
    }

    /**
     * 根据value获取枚举
     *
     * @param value 状态值
     * @return 枚举
     */
    public static AuditStatus getByValue(String value) {
        if (value == null) {
            return PENDING;
        }
        for (AuditStatus status : AuditStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return PENDING;
    }

    /**
     * code转value
     *
     * @param code 状态码
     * @return 状态值
     */
    public static String codeToValue(Integer code) {
        return getByCode(code).getValue();
    }

    /**
     * value转code
     *
     * @param value 状态值
     * @return 状态码
     */
    public static Integer valueToCode(String value) {
        return getByValue(value).getCode();
    }
}
