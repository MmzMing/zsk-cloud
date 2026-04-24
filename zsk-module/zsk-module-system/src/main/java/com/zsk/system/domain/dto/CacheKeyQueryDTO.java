package com.zsk.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 缓存键查询 请求数据传输对象
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
@Schema(description = "缓存键查询请求")
public class CacheKeyQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 缓存名称
     */
    @Schema(description = "缓存名称")
    private String cacheName;

    /**
     * 关键字（模糊匹配）
     */
    @Schema(description = "关键字")
    private String keyword;
}