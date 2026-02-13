package com.zsk.common.core.constant;

/**
 * 安全常量
 *
 * @author zsk
 */
public class SecurityConstants {

    /**
     * 登录用户标识
     */
    public static final String LOGIN_USER_KEY = "login_user";

    /**
     * Token标识
     */
    public static final String TOKEN_KEY = "token";

    /**
     * 用户ID字段
     */
    public static final String USER_ID = "user_id";

    /**
     * 用户名字段
     */
    public static final String USER_NAME = "user_name";

    /**
     * 用户Key字段
     */
    public static final String USER_KEY = "user_key";

    /**
     * 部门ID字段
     */
    public static final String DEPT_ID = "dept_id";

    /**
     * 数据权限字段
     */
    public static final String DATA_SCOPE = "data_scope";

    /**
     * 角色标识字段
     */
    public static final String ROLES = "roles";

    /**
     * 权限标识字段
     */
    public static final String PERMISSIONS = "permissions";

    /**
     * JWT标识
     */
    public static final String JWT_KEY = "jwt";

    /**
     * 匿名用户标识
     */
    public static final String ANONYMOUS_USER = "anonymousUser";

    /**
     * 超级管理员角色标识
     */
    public static final String SUPER_ADMIN = "admin";

    /**
     * 超级管理员ID
     */
    public static final Long SUPER_ADMIN_ID = 1L;

    /**
     * 密码加密盐值长度
     */
    public static final int PASSWORD_SALT_LENGTH = 16;

    /**
     * 密码最小长度
     */
    public static final int PASSWORD_MIN_LENGTH = 6;

    /**
     * 密码最大长度
     */
    public static final int PASSWORD_MAX_LENGTH = 20;

    /**
     * 登录失败最大次数
     */
    public static final int LOGIN_FAIL_MAX_TIMES = 5;

    /**
     * 登录失败锁定时间（分钟）
     */
    public static final int LOGIN_FAIL_LOCK_MINUTES = 30;

    /**
     * 用户ID请求头
     */
    public static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 用户名称请求头
     */
    public static final String USER_NAME_HEADER = "X-User-Name";

    /**
     * 用户Key请求头
     */
    public static final String USER_KEY_HEADER = "X-User-Key";

    /**
     * 授权请求头
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Token有效期（分钟）
     */
    public static final long TOKEN_EXPIRE = 720;

    /**
     * 刷新Token有效期（天）
     */
    public static final long REFRESH_TOKEN_EXPIRE = 30;

    private SecurityConstants() {
        throw new AssertionError("常量类禁止实例化");
    }
}
