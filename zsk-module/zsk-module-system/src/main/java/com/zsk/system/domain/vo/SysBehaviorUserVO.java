package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 行为审计 - 用户聚合视图
 * <p>
 * 从 sys_oper_log 聚合得到的有行为记录的用户，用于前端用户筛选下拉。
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-22
 */
@Data
@Schema(description = "行为审计用户")
public class SysBehaviorUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 操作人员（账号名）
     */
    @Schema(description = "操作人员（账号名）")
    private String operName;

    /**
     * 累计行为次数
     */
    @Schema(description = "累计行为次数")
    private Long operCount;

    /**
     * 最近一次行为时间
     */
    @Schema(description = "最近一次行为时间")
    private LocalDateTime lastOperTime;

    /**
     * 最近一次行为IP
     */
    @Schema(description = "最近一次行为IP")
    private String lastOperIp;

    /**
     * 风险等级（low/medium/high）
     */
    @Schema(description = "风险等级（low/medium/high）")
    private String riskLevel;
}
