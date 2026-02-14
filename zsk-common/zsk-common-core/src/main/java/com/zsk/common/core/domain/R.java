package com.zsk.common.core.domain;

import com.zsk.common.core.enums.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一响应结果
 *
 * @author wuhuaming
 */
@Data
@NoArgsConstructor
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 请求追踪ID
     */
    private String traceId;

    public R(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> R<T> ok() {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> R<T> ok(String message, T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 成功响应（仅消息）
     */
    public static <T> R<T> okMsg(String message) {
        return new R<>(ResultCode.SUCCESS.getCode(), message, null);
    }

    /**
     * 失败响应
     */
    public static <T> R<T> fail() {
        return new R<>(ResultCode.SYSTEM_ERROR.getCode(), ResultCode.SYSTEM_ERROR.getMessage(), null);
    }

    /**
     * 失败响应（自定义消息）
     */
    public static <T> R<T> fail(String message) {
        return new R<>(ResultCode.SYSTEM_ERROR.getCode(), message, null);
    }

    /**
     * 失败响应（自定义状态码和消息）
     */
    public static <T> R<T> fail(Integer code, String message) {
        return new R<>(code, message, null);
    }

    /**
     * 失败响应（使用ResultCode）
     */
    public static <T> R<T> fail(ResultCode resultCode) {
        return new R<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 失败响应（使用ResultCode + 自定义消息）
     */
    public static <T> R<T> fail(ResultCode resultCode, String message) {
        return new R<>(resultCode.getCode(), message, null);
    }

    /**
     * 参数错误
     */
    public static <T> R<T> paramError(String message) {
        return new R<>(ResultCode.PARAM_ERROR.getCode(), message, null);
    }

    /**
     * 业务错误
     */
    public static <T> R<T> bizError(String message) {
        return new R<>(ResultCode.BIZ_ERROR.getCode(), message, null);
    }

    /**
     * 未登录
     */
    public static <T> R<T> unauthorized(String message) {
        return new R<>(ResultCode.UNAUTHORIZED.getCode(), message, null);
    }

    /**
     * 无权限
     */
    public static <T> R<T> forbidden(String message) {
        return new R<>(ResultCode.FORBIDDEN.getCode(), message, null);
    }

    /**
     * 设置追踪ID
     */
    public R<T> traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
}
