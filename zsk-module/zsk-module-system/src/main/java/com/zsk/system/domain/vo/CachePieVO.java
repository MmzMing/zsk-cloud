package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 缓存饼图数据 视图对象
 * <p>
 * 用于展示缓存名汇总统计，如 {name: 'login', value: 100}
 *
 * @author wuhuaming
 * @date 2026-04-22
 * @version 1.0
 */
@Data
@Schema(description = "缓存饼图数据")
public class CachePieVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 缓存名称 */
    @Schema(description = "缓存名称")
    private String name;

    /** 数量/值 */
    @Schema(description = "数量/值")
    private Long value;
}