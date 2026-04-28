package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 前台首页视频交互操作结果视图对象
 * <p>
 * 用于点赞、收藏、关注等交互切换操作的返回结果。
 * 包含操作是否成功、操作后的状态和最新计数。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-29
 */
@Data
@Builder
@Schema(description = "前台首页视频交互操作结果")
public class DocHomeVideoInteractionResultVo {

    /**
     * 操作是否成功
     */
    @Schema(description = "操作是否成功")
    private boolean success;

    /**
     * 操作后的状态（true-已点赞/已收藏/已关注，false-已取消）
     */
    @Schema(description = "操作后的状态")
    private boolean status;

    /**
     * 最新计数（点赞数/收藏数/粉丝数）
     */
    @Schema(description = "最新计数")
    private Long count;
}
