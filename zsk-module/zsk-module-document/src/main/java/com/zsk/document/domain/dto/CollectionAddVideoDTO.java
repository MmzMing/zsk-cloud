package com.zsk.document.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 添加视频到合集请求DTO
 * <p>
 * 用于将一批视频添加到指定合集的请求参数封装。
 * 视频ID列表不能为空，且会自动过滤已存在于合集中的视频（防重复）。
 * 新添加的视频默认按顺序排在合集末尾。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-28
 */
@Data
@Schema(description = "添加视频到合集请求")
public class CollectionAddVideoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 视频ID列表
     * <p>
     * 待添加到合集的视频ID集合，至少包含一个有效视频ID。
     * 系统会自动去重：已存在于合集中的视频将被静默忽略。
     * </p>
     */
    @NotEmpty(message = "视频ID列表不能为空")
    @Schema(description = "视频ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> videoIds;

}
