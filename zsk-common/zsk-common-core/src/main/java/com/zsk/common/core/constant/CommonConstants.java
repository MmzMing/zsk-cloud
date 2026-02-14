package com.zsk.common.core.constant;

/**
 * 通用常量
 *
 * @author wuhuaming
 */
public class CommonConstants {

    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    public static final String GBK = "GBK";

    /**
     * 请求头 - Token
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * 请求头 - Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 请求头 - 租户ID
     */
    public static final String TENANT_ID_HEADER = "X-Tenant-Id";

    /**
     * 请求头 - 追踪ID
     */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * 请求头 - 用户ID
     */
    public static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 请求头 - 用户名
     */
    public static final String USER_NAME_HEADER = "X-User-Name";

    /**
     * 请求头 - 请求来源
     */
    public static final String REQUEST_SOURCE_HEADER = "X-Request-Source";

    /**
     * 请求来源 - 网关
     */
    public static final String REQUEST_SOURCE_GATEWAY = "gateway";

    /**
     * 请求来源 - 内部服务
     */
    public static final String REQUEST_SOURCE_INNER = "inner";

    /**
     * 默认页码
     */
    public static final Long DEFAULT_PAGE_NUM = 1L;

    /**
     * 默认每页大小
     */
    public static final Long DEFAULT_PAGE_SIZE = 10L;

    /**
     * 最大每页大小
     */
    public static final Long MAX_PAGE_SIZE = 500L;

    /**
     * 升序
     */
    public static final String ASC = "asc";

    /**
     * 降序
     */
    public static final String DESC = "desc";

    /**
     * 成功标记
     */
    public static final Integer SUCCESS = 200;

    /**
     * 失败标记
     */
    public static final Integer FAIL = 500;

    /**
     * 登录成功
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 注销
     */
    public static final String LOGOUT = "Logout";

    /**
     * 注册
     */
    public static final String REGISTER = "Register";

    /**
     * 删除标记 - 正常
     */
    public static final Integer DEL_FLAG_NORMAL = 0;

    /**
     * 删除标记 - 已删除
     */
    public static final Integer DEL_FLAG_DELETED = 2;

    /**
     * 是
     */
    public static final String YES = "1";

    /**
     * 否
     */
    public static final String NO = "0";

    /**
     * 用户状态 - 正常
     */
    public static final String USER_STATUS_NORMAL = "0";

    /**
     * 用户状态 - 停用
     */
    public static final String USER_STATUS_DISABLED = "1";

    /**
     * 用户状态 - 锁定
     */
    public static final String USER_STATUS_LOCKED = "2";

    /**
     * 验证码有效期（分钟）
     */
    public static final Integer CAPTCHA_EXPIRATION = 5;

    /**
     * Token有效期（小时）
     */
    public static final Integer TOKEN_EXPIRATION = 24;

    /**
     * 刷新Token有效期（小时）
     */
    public static final Integer REFRESH_TOKEN_EXPIRATION = 168;

    /**
     * 限流前缀
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 幂等性前缀
     */
    public static final String IDEMPOTENCY_KEY = "idempotency:";

    /**
     * 防重提交前缀
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 分布式锁前缀
     */
    public static final String LOCK_KEY_PREFIX = "lock:";

    /**
     * 本地缓存前缀
     */
    public static final String LOCAL_CACHE_KEY = "local_cache:";

    private CommonConstants() {
        throw new AssertionError("常量类禁止实例化");
    }
}
