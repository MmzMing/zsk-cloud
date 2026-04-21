package com.zsk.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 缓存键批量操作 请求数据传输对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "缓存键批量操作请求")
public class CacheKeyBatchDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 缓存键名列表 */
    @Schema(description = "缓存键名列表", required = true)
    @NotEmpty(message = "缓存键名列表不能为空")
    private List<String> keys;
}