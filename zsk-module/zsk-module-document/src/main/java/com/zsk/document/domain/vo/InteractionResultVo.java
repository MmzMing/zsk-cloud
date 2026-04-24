package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 交互结果响应（点赞/收藏/浏览）
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-25
 */
@Data
@Builder
@Schema(description = "交互结果响应")
public class InteractionResultVo {

    @Schema(description = "是否成功")
    private boolean success;

    @Schema(description = "操作后的状态（是否点赞/收藏/关注）")
    private boolean status;

    @Schema(description = "计数")
    private Long count;

    @Schema(description = "浏览量")
    private Long viewCount;

    @Schema(description = "点赞量")
    private Long likeCount;

    @Schema(description = "收藏量")
    private Long collectCount;

    @Schema(description = "是否已点赞")
    private Boolean hasLiked;

    @Schema(description = "是否已收藏")
    private Boolean hasCollected;

    @Schema(description = "是否已关注")
    private Boolean hasFollowed;
}
