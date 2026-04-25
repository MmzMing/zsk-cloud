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
     * 验证码验证通过凭证缓存键前缀
     */
    public static final String CACHE_CAPTCHA_VERIFIED = CACHE_PREFIX + "captcha:verified:";

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
     * 登录用户角色缓存键前缀（key: uuid）
     */
    public static final String CACHE_LOGIN_ROLES = CACHE_PREFIX + "login:roles:";

    /**
     * 登录用户权限缓存键前缀（key: uuid）
     */
    public static final String CACHE_LOGIN_PERMISSIONS = CACHE_PREFIX + "login:permissions:";

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
     * 点赞 Bitmap 缓存键前缀（记录用户是否点赞，SETBIT/GETBIT 操作）
     */
    public static final String CACHE_LIKE_BIT = CACHE_PREFIX + "like:bit:";

    /**
     * 收藏 Bitmap 缓存键前缀（记录用户是否收藏，SETBIT/GETBIT 操作）
     */
    public static final String CACHE_COLLECT_BIT = CACHE_PREFIX + "collect:bit:";

    /**
     * 关注 Bitmap 缓存键前缀（记录用户关注关系，SETBIT/GETBIT 操作）
     */
    public static final String CACHE_FOLLOW_BIT = CACHE_PREFIX + "follow:bit:";

    /**
     * 互动统计 Hash 缓存键前缀（存储点赞/收藏/关注/浏览计数，HINCRBY 操作）
     */
    public static final String CACHE_STAT = CACHE_PREFIX + "stat:";

    /**
     * 浏览去重锁缓存键前缀（防止短时间重复计数，SETNX 操作）
     */
    public static final String CACHE_VIEW_LOCK = CACHE_PREFIX + "view:lock:";

    /**
     * 密码重置验证令牌缓存键前缀
     */
    public static final String CACHE_PASSWORD_RESET = CACHE_PREFIX + "password:reset:";

    /**
     * 魔法链接缓存键前缀
     */
    public static final String CACHE_MAGIC_LINK = CACHE_PREFIX + "magic:link:";

    /**
     * 魔法链接发送频率限制缓存键前缀
     */
    public static final String CACHE_MAGIC_LINK_RATE_LIMIT = CACHE_PREFIX + "magic:link:rate_limit:";

    /**
     * 视频分类缓存键
     */
    public static final String CACHE_VIDEO_CATEGORY = CACHE_PREFIX + "video:category:list";

    /**
     * 视频标签缓存键
     */
    public static final String CACHE_VIDEO_TAG = CACHE_PREFIX + "video:tag:list";

    private CacheConstants() {
        throw new AssertionError("常量类禁止实例化");
    }
}
