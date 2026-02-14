package com.zsk.common.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.zsk.common.core.constant.SecurityConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * 安全上下文 - 存储当前登录用户信息
 *
 * @author wuhuaming
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
        Object userId = get(SecurityConstants.USER_ID);
        return userId == null ? null : Long.valueOf(userId.toString());
    }

    public static void setUserId(Long userId) {
        set(SecurityConstants.USER_ID, userId);
    }

    public static String getUserName() {
        Object userName = get(SecurityConstants.USER_NAME);
        return userName == null ? null : userName.toString();
    }

    public static void setUserName(String userName) {
        set(SecurityConstants.USER_NAME, userName);
    }

    public static String getNickName() {
        Object nickName = get(SecurityConstants.NICK_NAME);
        return nickName == null ? null : nickName.toString();
    }

    public static void setNickName(String nickName) {
        set(SecurityConstants.NICK_NAME, nickName);
    }

    public static Long getDeptId() {
        Object deptId = get(SecurityConstants.DEPT_ID);
        return deptId == null ? null : Long.valueOf(deptId.toString());
    }

    public static void setDeptId(Long deptId) {
        set(SecurityConstants.DEPT_ID, deptId);
    }

    public static String getToken() {
        Object token = get(SecurityConstants.TOKEN_KEY);
        return token == null ? null : token.toString();
    }

    public static void setToken(String token) {
        set(SecurityConstants.TOKEN_KEY, token);
    }

    public static java.util.Set<String> getRoles() {
        Object roles = get(SecurityConstants.ROLES);
        return roles == null ? new java.util.HashSet<>() : (java.util.Set<String>) roles;
    }

    public static void setRoles(java.util.Set<String> roles) {
        set(SecurityConstants.ROLES, roles);
    }

    public static java.util.Set<String> getPermissions() {
        Object permissions = get(SecurityConstants.PERMISSIONS);
        return permissions == null ? new java.util.HashSet<>() : (java.util.Set<String>) permissions;
    }

    public static void setPermissions(java.util.Set<String> permissions) {
        set(SecurityConstants.PERMISSIONS, permissions);
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
