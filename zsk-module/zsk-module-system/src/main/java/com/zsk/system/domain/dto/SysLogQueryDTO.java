package com.zsk.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理日志查询 请求数据传输对象
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
@Schema(description = "管理日志查询请求")
public class SysLogQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分类（content/user/system）
     */
    @Schema(description = "分类（content/user/system）")
    private String category;

    /**
     * 操作人
     */
    @Schema(description = "操作人")
    private String operator;

    /**
     * 请求URL
     */
    @Schema(description = "请求URL")
    private String requestUrl;

    /**
     * 请求方式
     */
    @Schema(description = "请求方式")
    private String requestMethod;

    /**
     * 操作状态（0正常 1异常）
     */
    @Schema(description = "操作状态（0正常 1异常）")
    private Integer status;

    /**
     * 模块标题
     */
    @Schema(description = "模块标题")
    private String title;

    /**
     * 业务类型（0其它 1新增 2修改 3删除 4授权 5导出 6导入）
     */
    @Schema(description = "业务类型（0其它 1新增 2修改 3删除 4授权 5导出 6导入）")
    private Integer businessType;

    /**
     * 操作开始时间
     */
    @Schema(description = "操作开始时间（yyyy-MM-dd HH:mm:ss）")
    private String beginTime;

    /**
     * 操作结束时间
     */
    @Schema(description = "操作结束时间（yyyy-MM-dd HH:mm:ss）")
    private String endTime;
}
