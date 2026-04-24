package com.zsk.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 缓存预热 请求数据传输对象
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
@Schema(description = "缓存预热请求")
public class CacheWarmupDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 需要预热的缓存名称列表
     */
    @Schema(description = "缓存名称列表")
    private List<String> cacheNames;
}