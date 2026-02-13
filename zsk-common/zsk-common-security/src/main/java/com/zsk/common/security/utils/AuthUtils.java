package com.zsk.common.security.utils;

import com.zsk.common.core.context.SecurityContext;
import com.zsk.common.core.exception.AuthException;
import com.zsk.common.core.exception.PermissionException;
import com.zsk.common.core.utils.StringUtils;
import com.zsk.common.security.enums.Logical;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * 权限验证工具类
 *
 * @author zsk
 */
public class AuthUtils {

    /**
     * 验证用户是否登录
     */
    public static void checkLogin() {
        if (SecurityContext.getUserId() == null) {
            throw new AuthException("未登录或登录已过期");
        }
    }

    /**
     * 验证用户是否具有某权限
     *
     * @param permission 权限码
     */
    public static void checkPermission(String permission) {
        if (!hasPermission(permission)) {
            throw PermissionException.denied();
        }
    }

    /**
     * 验证用户是否具有以下任意一个权限
     *
     * @param permissions 权限列表
     */
    public static void checkPermissionAny(String... permissions) {
        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return;
            }
        }
        if (permissions.length > 0) {
            throw PermissionException.denied();
        }
    }

    /**
     * 验证用户是否具有以下所有权限
     *
     * @param permissions 权限列表
     */
    public static void checkPermissionAnd(String... permissions) {
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                throw PermissionException.denied();
            }
        }
    }

    /**
     * 判断是否包含权限
     *
     * @param permission 权限码
     * @return 结果
     */
    public static boolean hasPermission(String permission) {
        if (StringUtils.isEmpty(permission)) {
            return true;
        }
        // 管理员拥有所有权限
        if (SecurityUtils.isAdmin(SecurityUtils.getUserId())) {
            return true;
        }
        Set<String> permissions = SecurityContext.getPermissions();
        return !CollectionUtils.isEmpty(permissions) && permissions.contains(permission);
    }

    /**
     * 验证用户是否具有某角色
     *
     * @param role 角色标识
     */
    public static void checkRole(String role) {
        if (!hasRole(role)) {
            throw PermissionException.denied();
        }
    }

    /**
     * 验证用户是否具有以下任意一个角色
     *
     * @param roles 角色列表
     */
    public static void checkRoleAny(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return;
            }
        }
        if (roles.length > 0) {
            throw PermissionException.denied();
        }
    }

    /**
     * 验证用户是否具有以下所有角色
     *
     * @param roles 角色列表
     */
    public static void checkRoleAnd(String... roles) {
        for (String role : roles) {
            if (!hasRole(role)) {
                throw PermissionException.denied();
            }
        }
    }

    /**
     * 判断是否包含角色
     *
     * @param role 角色标识
     * @return 结果
     */
    public static boolean hasRole(String role) {
        if (StringUtils.isEmpty(role)) {
            return true;
        }
        // 管理员拥有所有角色
        if (SecurityUtils.isAdmin(SecurityUtils.getUserId())) {
            return true;
        }
        Set<String> roles = SecurityContext.getRoles();
        return !CollectionUtils.isEmpty(roles) && roles.contains(role);
    }

    /**
     * 根据逻辑关系验证权限
     */
    public static void checkPermission(String[] permissions, Logical logical) {
        if (logical == Logical.AND) {
            checkPermissionAnd(permissions);
        } else {
            checkPermissionAny(permissions);
        }
    }

    /**
     * 根据逻辑关系验证角色
     */
    public static void checkRole(String[] roles, Logical logical) {
        if (logical == Logical.AND) {
            checkRoleAnd(roles);
        } else {
            checkRoleAny(roles);
        }
    }
}
