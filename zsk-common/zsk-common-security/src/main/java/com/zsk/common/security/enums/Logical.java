package com.zsk.common.security.enums;

/**
 * 权限验证逻辑
 *
 * @author wuhuaming
 */
public enum Logical {
    /**
     * 必须具有所有权限
     */
    AND,

    /**
     * 只要具有其中一个权限即可
     */
    OR
}
