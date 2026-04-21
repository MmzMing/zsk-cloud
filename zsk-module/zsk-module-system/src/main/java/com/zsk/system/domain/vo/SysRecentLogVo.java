package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 最近管理日志项 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "最近管理日志项")
public class SysRecentLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    @Schema(description = "日志ID")
    private String id;

    /** 分类（content/user/system） */
    @Schema(description = "分类（content/user/system）")
    private String category;

    /** 操作人 */
    @Schema(description = "操作人")
    private String operator;

    /** 动作名称 */
    @Schema(description = "动作名称")
    private String action;

    /** 详细描述 */
    @Schema(description = "详细描述")
    private String detail;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private String createdAt;

    /** 请求方式 */
    @Schema(description = "请求方式")
    private String requestMethod;

    /** 请求URL */
    @Schema(description = "请求URL")
    private String requestUrl;

    /** 请求参数 */
    @Schema(description = "请求参数")
    private String requestParam;

    /** 响应结果 */
    @Schema(description = "响应结果")
    private String responseResult;

    /** 操作状态（0正常 1异常） */
    @Schema(description = "操作状态（0正常 1异常）")
    private Integer status;

    /** 消耗时间(ms) */
    @Schema(description = "消耗时间(ms)")
    private Long costTime;

    /** 操作IP */
    @Schema(description = "操作IP")
    private String operIp;
}
