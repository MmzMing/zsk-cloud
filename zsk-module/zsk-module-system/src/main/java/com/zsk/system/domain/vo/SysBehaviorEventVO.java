package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户行为列表项 视图对象
 * <p>
 * 对应前端「行为列表」展示字段：用户/行为内容/参数/响应/类型/时间/IP/地点。
 * 列表场景仅返回精简信息，详情通过 {@link SysBehaviorDetailVO} 获取。
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-22
 */
@Data
@Schema(description = "用户行为列表项")
public class SysBehaviorEventVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 行为记录ID（MongoDB _id）
     */
    @Schema(description = "行为记录ID")
    private String id;

    /**
     * 操作人员（用户名）
     */
    @Schema(description = "操作人员")
    private String operName;

    /**
     * 模块标题（行为内容）
     */
    @Schema(description = "模块标题（行为内容）")
    private String title;

    /**
     * 行为类型代码（businessType）
     */
    @Schema(description = "行为类型代码")
    private Integer businessType;

    /**
     * 行为类型名称
     */
    @Schema(description = "行为类型名称")
    private String actionType;

    /**
     * 请求URL
     */
    @Schema(description = "请求URL")
    private String operUrl;

    /**
     * 请求方式（GET/POST...）
     */
    @Schema(description = "请求方式")
    private String requestMethod;

    /**
     * 请求参数（截断）
     */
    @Schema(description = "请求参数（最多200字符）")
    private String operParam;

    /**
     * 响应结果（截断）
     */
    @Schema(description = "响应结果（最多200字符）")
    private String jsonResult;

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
     * 操作状态（0正常 1异常）
     */
    @Schema(description = "操作状态（0正常 1异常）")
    private Integer status;

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
