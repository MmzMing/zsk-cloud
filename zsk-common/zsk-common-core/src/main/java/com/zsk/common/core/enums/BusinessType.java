package com.zsk.common.core.enums;

import lombok.Getter;

/**
 * 业务操作类型枚举
 *
 * @author zsk
 */
@Getter
public enum BusinessType {

    /**
     * 其他
     */
    OTHER(0, "其他"),

    /**
     * 新增
     */
    INSERT(1, "新增"),

    /**
     * 修改
     */
    UPDATE(2, "修改"),

    /**
     * 删除
     */
    DELETE(3, "删除"),

    /**
     * 授权
     */
    GRANT(4, "授权"),

    /**
     * 导出
     */
    EXPORT(5, "导出"),

    /**
     * 导入
     */
    IMPORT(6, "导入"),

    /**
     * 强退
     */
    FORCE(7, "强退"),

    /**
     * 清空数据
     */
    CLEAN(8, "清空数据"),

    /**
     * 查询
     */
    QUERY(9, "查询"),

    /**
     * 登录
     */
    LOGIN(10, "登录"),

    /**
     * 登出
     */
    LOGOUT(11, "登出");

    private final Integer code;
    private final String desc;

    BusinessType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static BusinessType getByCode(Integer code) {
        if (code == null) {
            return OTHER;
        }
        for (BusinessType type : BusinessType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return OTHER;
    }
}
