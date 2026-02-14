package com.zsk.common.core.enums;

import lombok.Getter;

/**
 * 响应状态码枚举
 *
 * @author wuhuaming
 */
@Getter
public enum ResultCode {

    // ==================== 成功 ====================
    SUCCESS(200, "操作成功"),

    // ==================== 客户端错误 4xx ====================
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),
    UNSUPPORTED_MEDIA_TYPE(415, "不支持的媒体类型"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    // ==================== 业务错误 100xx ====================
    PARAM_ERROR(10001, "参数校验失败"),
    PARAM_TYPE_ERROR(10002, "参数类型错误"),
    PARAM_MISSING(10003, "缺少必要参数"),
    PARAM_FORMAT_ERROR(10004, "参数格式错误"),

    // ==================== 业务处理错误 101xx ====================
    BIZ_ERROR(10100, "业务处理失败"),
    DATA_NOT_EXIST(10101, "数据不存在"),
    DATA_ALREADY_EXIST(10102, "数据已存在"),
    DATA_EXPIRED(10103, "数据已过期"),
    DATA_DISABLED(10104, "数据已被禁用"),
    DATA_DELETED(10105, "数据已被删除"),
    DATA_LOCKED(10106, "数据已被锁定"),

    // ==================== 用户相关错误 102xx ====================
    USER_ERROR(10200, "用户操作失败"),
    USER_NOT_EXIST(10201, "用户不存在"),
    USER_DISABLED(10202, "用户已被禁用"),
    USER_LOCKED(10203, "用户已被锁定"),
    USER_PASSWORD_ERROR(10204, "用户名或密码错误"),
    USER_OLD_PASSWORD_ERROR(10205, "旧密码错误"),
    USER_TWO_PASSWORD_NOT_MATCH(10206, "两次输入的密码不一致"),
    USER_ACCOUNT_EXPIRED(10207, "账号已过期"),
    USER_CREDENTIALS_EXPIRED(10208, "凭证已过期"),

    // ==================== 认证授权错误 103xx ====================
    AUTH_ERROR(10300, "认证失败"),
    TOKEN_EXPIRED(10301, "Token已过期"),
    TOKEN_INVALID(10302, "Token无效"),
    TOKEN_BLACKLIST(10303, "Token已被列入黑名单"),
    CAPTCHA_ERROR(10304, "验证码错误"),
    CAPTCHA_EXPIRED(10305, "验证码已过期"),
    LOGIN_TYPE_NOT_SUPPORT(10306, "不支持的登录方式"),
    LOGIN_FAIL(10307, "登录失败"),
    LOGOUT_FAIL(10308, "登出失败"),
    PERMISSION_DENIED(10309, "权限不足"),
    ACCESS_DENIED(10310, "访问被拒绝"),

    // ==================== 文件相关错误 104xx ====================
    FILE_ERROR(10400, "文件操作失败"),
    FILE_NOT_EXIST(10401, "文件不存在"),
    FILE_UPLOAD_FAIL(10402, "文件上传失败"),
    FILE_DOWNLOAD_FAIL(10403, "文件下载失败"),
    FILE_TYPE_NOT_SUPPORT(10404, "不支持的文件类型"),
    FILE_SIZE_EXCEED(10405, "文件大小超出限制"),
    FILE_NAME_TOO_LONG(10406, "文件名过长"),
    FILE_CONTENT_EMPTY(10407, "文件内容为空"),

    // ==================== 系统错误 5xx ====================
    SYSTEM_ERROR(500, "系统内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    GATEWAY_ERROR(504, "网关错误"),

    // ==================== 远程调用错误 600xx ====================
    REMOTE_ERROR(60000, "远程调用失败"),
    REMOTE_TIMEOUT(60001, "远程调用超时"),
    REMOTE_FALLBACK(60002, "远程服务降级"),
    REMOTE_LIMIT(60003, "远程服务限流"),

    // ==================== 限流熔断错误 700xx ====================
    RATE_LIMIT_ERROR(70000, "请求过于频繁，请稍后再试"),
    CIRCUIT_BREAKER_ERROR(70001, "服务熔断，请稍后再试"),
    DEGRADE_ERROR(70002, "服务降级"),

    // ==================== 分布式事务错误 800xx ====================
    SEATA_ERROR(80000, "分布式事务失败"),

    // ==================== 幂等性错误 900xx ====================
    IDEMPOTENCY_ERROR(90000, "重复请求"),

    // ==================== 多租户错误 1000xx ====================
    TENANT_ERROR(100000, "租户操作失败"),
    TENANT_NOT_EXIST(100001, "租户不存在"),
    TENANT_EXPIRED(100002, "租户已过期"),
    TENANT_DISABLED(100003, "租户已被禁用"),
    TENANT_NO_PERMISSION(100004, "没有该租户的操作权限");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态信息
     */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据code获取枚举
     */
    public static ResultCode getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ResultCode resultCode : ResultCode.values()) {
            if (resultCode.getCode().equals(code)) {
                return resultCode;
            }
        }
        return null;
    }
}
