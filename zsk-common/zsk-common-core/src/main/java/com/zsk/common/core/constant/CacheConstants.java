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
     * 字典类型标签集合缓存键（Set，存储所有已缓存的 dictType）
     */
    public static final String CACHE_DICT_TAGS = CACHE_DICT + "tags";

    /**
     * 字典数据缓存键模板（Value/DictCacheItem，拼接 dictType 值）
     * 用法：CACHE_DICT_DATA_KEY_PREFIX + dictType
     */
    public static final String CACHE_DICT_DATA_KEY_PREFIX = CACHE_DICT + "data:";

    /**
     * 字典缓存全局版本号键（String/Long，记录所有字典数据的全局版本）
     * 任何字典类型或字典数据的增删改都会递增此版本号
     * 前端可通过比较版本号决定是否需要重新拉取字典数据
     * <p>
     * 按类型版本号已内嵌到 DictCacheItem.version 字段中，
     * 与字典数据存储在同一个 Redis Value 里，无需单独维护。
     */
    public static final String CACHE_DICT_VERSION = CACHE_DICT + "version";

    /**
     * 字典缓存预热分布式锁键
     */
    public static final String CACHE_DICT_WARMUP_LOCK = CACHE_PREFIX + "lock:dict:warmup";

    /**
     * 字典缓存过期时间（小时）
     */
    public static final long CACHE_DICT_EXPIRE_HOURS = 24;

    /**
     * 字典缓存预热锁等待时间（分钟）
     */
    public static final long CACHE_DICT_WARMUP_LOCK_WAIT_MINUTES = 3;

    /**
     * 字典缓存预热锁持有时间（分钟）
     */
    public static final long CACHE_DICT_WARMUP_LOCK_LEASE_MINUTES = 5;

    /**
     * 字典缓存预热分片大小（每个虚拟线程处理的字典类型数量）
     */
    public static final int CACHE_DICT_WARMUP_SHARD_SIZE = 5;

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
     * 点赞用户 Set 缓存键前缀（记录用户点赞的目标集合）
     * 完整 Key: like:user:{userId}:{typeCode}  Value: Set<targetId>
     */
    public static final String CACHE_LIKE_USER = CACHE_PREFIX + "like:user:";

    /**
     * 收藏用户 Set 缓存键前缀（记录用户收藏的目标集合）
     * 完整 Key: collect:user:{userId}:{typeCode}  Value: Set<targetId>
     */
    public static final String CACHE_COLLECT_USER = CACHE_PREFIX + "collect:user:";

    /**
     * 关注用户 Set 缓存键前缀（记录用户关注的目标集合）
     * 完整 Key: follow:user:{userId}:{typeCode}  Value: Set<targetId>
     */
    public static final String CACHE_FOLLOW_USER = CACHE_PREFIX + "follow:user:";

    /**
     * 点赞待同步队列（Hash，field=userId:typeCode:targetId，value=1/0）
     * RENAME 原子切换后批量写库，避免 KEYS 扫描阻塞 Redis
     */
    public static final String CACHE_LIKE_PENDING = CACHE_PREFIX + "like:pending";

    /**
     * 收藏待同步队列（Hash，field=userId:typeCode:targetId，value=1/0）
     */
    public static final String CACHE_COLLECT_PENDING = CACHE_PREFIX + "collect:pending";

    /**
     * 关注待同步队列（Hash，field=userId:typeCode:targetId，value=1/0）
     */
    public static final String CACHE_FOLLOW_PENDING = CACHE_PREFIX + "follow:pending";

    /**
     * 互动统计 Hash 缓存键前缀（存储点赞/收藏/关注/浏览计数，HINCRBY 操作）
     */
    public static final String CACHE_STAT = CACHE_PREFIX + "stat:";

    /**
     * 用户维度 Set TTL（秒），7天
     */
    public static final long CACHE_INTERACTION_TTL_SECONDS = 7 * 24 * 3600L;

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

    private CacheConstants() {
        throw new AssertionError("常量类禁止实例化");
    }
}
