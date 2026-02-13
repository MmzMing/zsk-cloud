package com.zsk.common.core.enums;

import lombok.Getter;

/**
 * 数据权限范围枚举
 *
 * @author zsk
 */
@Getter
public enum DataScope {

    /**
     * 全部数据权限
     */
    ALL(1, "全部数据权限"),

    /**
     * 本部门数据权限
     */
    DEPT_ONLY(2, "本部门数据权限"),

    /**
     * 本部门及以下数据权限
     */
    DEPT_AND_CHILD(3, "本部门及以下数据权限"),

    /**
     * 仅本人数据权限
     */
    SELF_ONLY(4, "仅本人数据权限"),

    /**
     * 自定义数据权限
     */
    CUSTOM(5, "自定义数据权限");

    private final Integer code;
    private final String desc;

    DataScope(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DataScope getByCode(Integer code) {
        if (code == null) {
            return SELF_ONLY;
        }
        for (DataScope scope : DataScope.values()) {
            if (scope.getCode().equals(code)) {
                return scope;
            }
        }
        return SELF_ONLY;
    }
}
