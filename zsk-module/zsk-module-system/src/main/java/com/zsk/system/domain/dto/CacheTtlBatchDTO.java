package com.zsk.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 缓存TTL批量刷新 请求数据传输对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "缓存TTL批量刷新请求")
public class CacheTtlBatchDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 缓存键名与过期时间的映射 */
    @Schema(description = "缓存键名与过期时间的映射", required = true)
    @NotEmpty(message = "缓存键名与过期时间映射不能为空")
    private Map<String, Long> cacheKeyTtlMap;
}