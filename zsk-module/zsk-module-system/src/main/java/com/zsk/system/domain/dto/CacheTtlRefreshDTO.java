package com.zsk.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 缓存TTL刷新 请求数据传输对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "缓存TTL刷新请求")
public class CacheTtlRefreshDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 缓存键名 */
    @Schema(description = "缓存键名", required = true)
    @NotBlank(message = "缓存键名不能为空")
    private String cacheKey;

    /** 过期时间（秒） */
    @Schema(description = "过期时间（秒）", required = true)
    @NotNull(message = "过期时间不能为空")
    private Long ttl;
}