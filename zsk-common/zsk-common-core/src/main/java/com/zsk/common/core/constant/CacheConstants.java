package com.zsk.common.core.constant;

/**
 * 缓存常量
 *
 * @author wuhuaming
 */
public class CacheConstants {

    /**
     * 缓存前缀
     */
    public static final String CACHE_PREFIX = "zsk:";

    /**
     * 在线用户缓存键前缀
     */
    public static final String ONLINE_TOKEN_KEY = CACHE_PREFIX + "online:token:";

    /**
     * 登录失败次数缓存键前缀
     */
    public static final String LOGIN_FAIL_KEY = CACHE_PREFIX + "login:fail:";

    /**
     * 登录IP黑名单缓存键前缀
     */
    public static final String LOGIN_BLACKLIST_KEY = CACHE_PREFIX + "login:blacklist:";

    /**
     * 验证码缓存键前缀
     */
    public static final String CAPTCHA_CODE_KEY = CACHE_PREFIX + "captcha:";

    /**
     * 字典数据缓存键前缀
     */
    public static final String DICT_KEY = CACHE_PREFIX + "dict:";

    /**
     * 参数配置缓存键前缀
     */
    public static final String CONFIG_KEY = CACHE_PREFIX + "config:";

    /**
     * 用户信息缓存键前缀
     */
    public static final String USER_INFO_KEY = CACHE_PREFIX + "user:info:";

    /**
     * 用户权限缓存键前缀
     */
    public static final String USER_PERMISSION_KEY = CACHE_PREFIX + "user:permission:";

    /**
     * 用户角色缓存键前缀
     */
    public static final String USER_ROLE_KEY = CACHE_PREFIX + "user:role:";

    /**
     * 路由缓存键前缀
     */
    public static final String ROUTE_KEY = CACHE_PREFIX + "route:";

    /**
     * Token黑名单缓存键前缀
     */
    public static final String TOKEN_BLACKLIST_KEY = CACHE_PREFIX + "token:blacklist:";

    /**
     * 分布式锁前缀
     */
    public static final String LOCK_KEY = CACHE_PREFIX + "lock:";

    /**
     * 限流前缀
     */
    public static final String RATE_LIMIT_KEY = CACHE_PREFIX + "rate_limit:";

    /**
     * 幂等性前缀
     */
    public static final String IDEMPOTENCY_KEY = CACHE_PREFIX + "idempotency:";

    /**
     * 租户信息缓存键前缀
     */
    public static final String TENANT_INFO_KEY = CACHE_PREFIX + "tenant:info:";

    /**
     * 租户数据源缓存键前缀
     */
    public static final String TENANT_DATASOURCE_KEY = CACHE_PREFIX + "tenant:datasource:";

    /**
     * 系统信息缓存键前缀
     */
    public static final String SYS_INFO_KEY = CACHE_PREFIX + "sys:info:";

    /**
     * 登录令牌缓存键前缀
     */
    public static final String LOGIN_TOKEN_KEY = CACHE_PREFIX + "login:token:";


    /**
     * 第三方登录状态缓存键前缀
     */
    public static final String THIRD_PARTY_STATE_KEY = CACHE_PREFIX + "auth:third:state:";

    /**
     * 邮箱验证码缓存键前缀
     */
    public static final String EMAIL_CODE_KEY = CACHE_PREFIX + "email_code:";

    /**
     * 网关黑名单缓存键前缀
     */
    public static final String GATEWAY_BLACKLIST_KEY = CACHE_PREFIX + "gateway:blacklist:";

    private CacheConstants() {
        throw new AssertionError("常量类禁止实例化");
    }
}
