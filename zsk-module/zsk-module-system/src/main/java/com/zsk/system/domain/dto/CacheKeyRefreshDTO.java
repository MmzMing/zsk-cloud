package com.zsk.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 缓存键刷新 请求数据传输对象
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
@Schema(description = "缓存键刷新请求")
public class CacheKeyRefreshDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 缓存键名
     */
    @Schema(description = "缓存键名", required = true)
    @NotBlank(message = "缓存键名不能为空")
    private String key;
}