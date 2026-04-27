package com.zsk.document.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 合集视频排序请求DTO
 * <p>
 * 用于调整合集中视频播放顺序的请求参数封装。
 * 传入的视频ID列表顺序即为期望的排序结果：
     * 第一个视频 sort_order = 0，第二个 = 1，以此类推。
 * 列表必须包含合集中所有视频的ID（或至少包含需要排序的视频）。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-28
 */
@Data
@Schema(description = "合集视频排序请求")
public class CollectionVideoSortDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 视频ID排序列表
     * <p>
     * 按期望顺序排列的视频ID列表，用于重新设定合集中视频的播放顺序。
     * 列表中的顺序即为最终排序结果：索引0的视频排在第一位，索引1排在第二位，以此类推。
     * 必须至少包含一个视频ID。
     * </p>
     */
    @NotEmpty(message = "视频ID排序列表不能为空")
    @Schema(description = "视频ID排序列表（按期望顺序排列）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> videoIds;

}
