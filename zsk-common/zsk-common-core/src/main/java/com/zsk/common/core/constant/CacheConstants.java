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
    public static final String CACHE_ONLINE_TOKEN = CACHE_PREFIX + "online:token:";

    /**
     * 登录失败次数缓存键前缀
     */
    public static final String CACHE_LOGIN_FAIL = CACHE_PREFIX + "login:fail:";

    /**
     * 登录IP黑名单缓存键前缀
     */
    public static final String CACHE_LOGIN_BLACKLIST = CACHE_PREFIX + "login:blacklist:";

    /**
     * 验证码缓存键前缀
     */
    public static final String CACHE_CAPTCHA_CODE = CACHE_PREFIX + "captcha:";

    /**
     * 字典数据缓存键前缀
     */
    public static final String CACHE_DICT = CACHE_PREFIX + "dict:";

    /**
     * 参数配置缓存键前缀
     */
    public static final String CACHE_CONFIG = CACHE_PREFIX + "config:";

    /**
     * 用户信息缓存键前缀
     */
    public static final String CACHE_USER_INFO = CACHE_PREFIX + "user:info:";

    /**
     * 用户权限缓存键前缀
     */
    public static final String CACHE_USER_PERMISSION = CACHE_PREFIX + "user:permission:";

    /**
     * 用户角色缓存键前缀
     */
    public static final String CACHE_USER_ROLE = CACHE_PREFIX + "user:role:";

    /**
     * 路由缓存键前缀
     */
    public static final String CACHE_ROUTE = CACHE_PREFIX + "route:";

    /**
     * Token黑名单缓存键前缀
     */
    public static final String CACHE_TOKEN_BLACKLIST = CACHE_PREFIX + "token:blacklist:";

    /**
     * 分布式锁前缀
     */
    public static final String CACHE_LOCK = CACHE_PREFIX + "lock:";

    /**
     * 限流前缀
     */
    public static final String CACHE_RATE_LIMIT = CACHE_PREFIX + "rate_limit:";

    /**
     * 幂等性前缀
     */
    public static final String CACHE_IDEMPOTENCY = CACHE_PREFIX + "idempotency:";

    /**
     * 租户信息缓存键前缀
     */
    public static final String CACHE_TENANT_INFO = CACHE_PREFIX + "tenant:info:";

    /**
     * 租户数据源缓存键前缀
     */
    public static final String CACHE_TENANT_DATASOURCE = CACHE_PREFIX + "tenant:datasource:";

    /**
     * 系统信息缓存键前缀
     */
    public static final String CACHE_SYS_INFO = CACHE_PREFIX + "sys:info:";

    /**
     * 登录令牌缓存键前缀
     */
    public static final String CACHE_LOGIN_TOKEN = CACHE_PREFIX + "login:token:";

    /**
     * 第三方登录状态缓存键前缀
     */
    public static final String CACHE_THIRD_PARTY_STATE = CACHE_PREFIX + "auth:third:state:";

    /**
     * 邮箱验证码缓存键前缀
     */
    public static final String CACHE_EMAIL_CODE = CACHE_PREFIX + "email_code:";

    /**
     * 网关黑名单缓存键前缀
     */
    public static final String CACHE_GATEWAY_BLACKLIST = CACHE_PREFIX + "gateway:blacklist:";

    /**
     * 点赞计数缓存键前缀（用于存储点赞数量）
     */
    public static final String CACHE_LIKE_COUNT = CACHE_PREFIX + "like:count:";

    /**
     * 点赞用户记录缓存键前缀（用于记录用户是否已点赞）
     */
    public static final String CACHE_LIKE_USER = CACHE_PREFIX + "like:user:";

    /**
     * 收藏计数缓存键前缀（用于存储收藏数量）
     */
    public static final String CACHE_COLLECT_COUNT = CACHE_PREFIX + "collect:count:";

    /**
     * 收藏用户记录缓存键前缀（用于记录用户是否已收藏）
     */
    public static final String CACHE_COLLECT_USER = CACHE_PREFIX + "collect:user:";

    /**
     * 关注计数缓存键前缀（用于存储关注数量）
     */
    public static final String CACHE_FOLLOW_COUNT = CACHE_PREFIX + "follow:count:";

    /**
     * 关注用户记录缓存键前缀（用于记录用户是否已关注）
     */
    public static final String CACHE_FOLLOW_USER = CACHE_PREFIX + "follow:user:";

    private CacheConstants() {
        throw new AssertionError("常量类禁止实例化");
    }
}
