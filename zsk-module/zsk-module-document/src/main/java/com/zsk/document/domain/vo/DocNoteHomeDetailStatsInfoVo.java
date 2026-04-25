package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 笔记首页详情统计信息视图对象
 * <p>
 * 包含笔记的点赞、收藏、浏览等交互统计数据，以及当前用户的交互状态。
 * 数据来源于 Redis 缓存服务，通过 {@link com.zsk.document.service.ICacheDocLikeService}、
 * {@link com.zsk.document.service.ICacheDocCollectService}、
 * {@link com.zsk.document.service.ICacheDocViewService} 获取。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-25
 */
@Data
@Schema(description = "笔记首页详情统计信息")
public class DocNoteHomeDetailStatsInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 浏览量
     */
    @Schema(description = "浏览量")
    private Integer views;

    /**
     * 点赞数
     */
    @Schema(description = "点赞数")
    private Integer likes;

    /**
     * 收藏数
     */
    @Schema(description = "收藏数")
    private Integer favorites;

    /**
     * 评论数
     */
    @Schema(description = "评论数")
    private Integer comments;

    /**
     * 是否已点赞
     */
    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    /**
     * 是否已收藏
     */
    @Schema(description = "是否已收藏")
    private Boolean isFavorited;
}
