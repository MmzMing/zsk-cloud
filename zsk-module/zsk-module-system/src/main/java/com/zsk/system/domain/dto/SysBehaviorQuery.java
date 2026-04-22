package com.zsk.system.domain.dto;

import com.zsk.common.datasource.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 用户行为查询条件
 *
 * @author wuhuaming
 * @date 2026-04-22
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户行为查询条件")
public class SysBehaviorQuery extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户名（操作人，模糊查询） */
    @Schema(description = "用户名（模糊查询）")
    private String userName;

    /**
     * 行为类型 - 业务类型代码
     * 0其它 1新增 2修改 3删除 4授权 5导出 6导入 7强退 8生成代码 9清空数据 10查询
     */
    @Schema(description = "行为类型（businessType 代码）")
    private Integer businessType;

    /** 模块标题（模糊） */
    @Schema(description = "模块标题（模糊查询）")
    private String title;

    /** 操作 IP（模糊） */
    @Schema(description = "操作IP（模糊查询）")
    private String operIp;

    /** 操作状态：0正常 1异常 */
    @Schema(description = "操作状态（0正常 1异常）")
    private Integer status;

    /** 行为开始时间 */
    @Schema(description = "行为开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    /** 行为结束时间 */
    @Schema(description = "行为结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
