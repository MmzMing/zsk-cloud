package com.zsk.common.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 租户上下文 - 多租户SaaS模式支持
 *
 * @author wuhuaming
 */
public class TenantContext {

    private static final TransmittableThreadLocal<Long> TENANT_ID = new TransmittableThreadLocal<>();

    /**
     * 获取当前租户ID
     */
    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 设置当前租户ID
     */
    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    /**
     * 清除租户ID
     */
    public static void clear() {
        TENANT_ID.remove();
    }

    /**
     * 是否为多租户模式
     */
    public static boolean isTenantMode() {
        return TENANT_ID.get() != null;
    }
}
