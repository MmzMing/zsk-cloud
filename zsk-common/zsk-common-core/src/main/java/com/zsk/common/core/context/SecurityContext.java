package com.zsk.common.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.Map;

/**
 * 安全上下文 - 存储当前登录用户信息
 *
 * @author zsk
 */
public class SecurityContext {

    private static final TransmittableThreadLocal<Map<String, Object>> CONTEXT = new TransmittableThreadLocal<>();

    public static void set(String key, Object value) {
        Map<String, Object> map = CONTEXT.get();
        if (map == null) {
            map = new HashMap<>();
            CONTEXT.set(map);
        }
        map.put(key, value);
    }

    public static Object get(String key) {
        Map<String, Object> map = CONTEXT.get();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public static Long getUserId() {
        Object userId = get("userId");
        return userId == null ? null : Long.valueOf(userId.toString());
    }

    public static void setUserId(Long userId) {
        set("userId", userId);
    }

    public static String getUserName() {
        Object userName = get("userName");
        return userName == null ? null : userName.toString();
    }

    public static void setUserName(String userName) {
        set("userName", userName);
    }

    public static Long getDeptId() {
        Object deptId = get("deptId");
        return deptId == null ? null : Long.valueOf(deptId.toString());
    }

    public static void setDeptId(Long deptId) {
        set("deptId", deptId);
    }

    public static String getToken() {
        Object token = get("token");
        return token == null ? null : token.toString();
    }

    public static void setToken(String token) {
        set("token", token);
    }

    public static java.util.Set<String> getRoles() {
        Object roles = get("roles");
        return roles == null ? new java.util.HashSet<>() : (java.util.Set<String>) roles;
    }

    public static void setRoles(java.util.Set<String> roles) {
        set("roles", roles);
    }

    public static java.util.Set<String> getPermissions() {
        Object permissions = get("permissions");
        return permissions == null ? new java.util.HashSet<>() : (java.util.Set<String>) permissions;
    }

    public static void setPermissions(java.util.Set<String> permissions) {
        set("permissions", permissions);
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
