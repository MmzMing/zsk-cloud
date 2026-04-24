package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户行为详情 视图对象
 * <p>
 * 完整返回某次请求的全部参数 / 响应 / 异常信息，用于前端「详情面板」展示。
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-22
 */
@Data
@Schema(description = "用户行为详情")
public class SysBehaviorDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 行为记录ID
     */
    @Schema(description = "行为记录ID")
    private String id;

    /**
     * 操作人员
     */
    @Schema(description = "操作人员")
    private String operName;

    /**
     * 模块标题
     */
    @Schema(description = "模块标题")
    private String title;

    /**
     * 行为类型代码
     */
    @Schema(description = "行为类型代码")
    private Integer businessType;

    /**
     * 行为类型名称
     */
    @Schema(description = "行为类型名称")
    private String actionType;

    /**
     * 控制器方法（全限定）
     */
    @Schema(description = "控制器方法")
    private String method;

    /**
     * 请求方式
     */
    @Schema(description = "请求方式")
    private String requestMethod;

    /**
     * 请求URL
     */
    @Schema(description = "请求URL")
    private String operUrl;

    /**
     * 操作IP
     */
    @Schema(description = "操作IP")
    private String operIp;

    /**
     * 操作地点
     */
    @Schema(description = "操作地点")
    private String operLocation;

    /**
     * 完整请求参数
     */
    @Schema(description = "完整请求参数")
    private String operParam;

    /**
     * 完整响应结果
     */
    @Schema(description = "完整响应结果")
    private String jsonResult;

    /**
     * 操作状态（0正常 1异常）
     */
    @Schema(description = "操作状态（0正常 1异常）")
    private Integer status;

    /**
     * 错误消息
     */
    @Schema(description = "错误消息")
    private String errorMsg;

    /**
     * 行为时间
     */
    @Schema(description = "行为时间")
    private LocalDateTime operTime;

    /**
     * 耗时（毫秒）
     */
    @Schema(description = "耗时（毫秒）")
    private Long costTime;
}
