package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 趋势数据项 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "趋势数据项")
public class SysDashboardTrendVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 日期/周期标签 */
    @Schema(description = "日期/周期标签")
    private String date;

    /** 访问量 */
    @Schema(description = "访问量")
    private Long value;
}
