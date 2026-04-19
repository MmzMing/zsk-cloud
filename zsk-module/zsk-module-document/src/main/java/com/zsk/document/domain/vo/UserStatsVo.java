package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 用户统计信息
 *
 * @author wuhuaming
 */
@Data
@Builder
@Schema(description = "用户统计信息")
public class UserStatsVo {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "点赞总数")
    private Long likeCount;

    @Schema(description = "粉丝总数")
    private Long fanCount;

    @Schema(description = "收藏总数")
    private Long collectCount;
}
