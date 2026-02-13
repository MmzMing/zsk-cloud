package com.zsk.common.security.utils;

import cn.hutool.crypto.digest.BCrypt;
import com.zsk.common.core.context.SecurityContext;

/**
 * 安全服务工具类
 *
 * @author zsk
 */
public class SecurityUtils {

    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        return SecurityContext.getUserId();
    }

    /**
     * 获取用户名
     */
    public static String getUserName() {
        return SecurityContext.getUserName();
    }

    /**
     * 获取部门ID
     */
    public static Long getDeptId() {
        return SecurityContext.getDeptId();
    }

    /**
     * 获取用户Token
     */
    public static String getToken() {
        return SecurityContext.getToken();
    }

    /**
     * 是否为管理员
     *
     * @param userId 用户ID
     * @return 结果
     */
    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    /**
     * 生成BCryptPasswordEncoder密码
     *
     * @param password 密码
     * @return 加密字符串
     */
    public static String encryptPassword(String password) {
        return BCrypt.hashpw(password);
    }

    /**
     * 判断密码是否相同
     *
     * @param rawPassword     真实密码
     * @param encodedPassword 加密后密码
     * @return 结果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
