package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 前台首页笔记交互信息视图对象
 * <p>
 * 用于前台首页笔记详情页展示点赞、收藏、关注等交互数据。
 * 所有统计数据（浏览量、点赞数、收藏数）均从 Redis 缓存获取。
 * 包含作者信息及当前用户的交互状态。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-29
 */
@Data
@Schema(description = "前台首页笔记交互信息")
public class DocHomeNoteInteractionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 浏览量（从Redis缓存获取）
     */
    @Schema(description = "浏览量")
    private Long viewCount;

    /**
     * 点赞数（从Redis缓存获取）
     */
    @Schema(description = "点赞数")
    private Long likeCount;

    /**
     * 收藏数（从Redis缓存获取）
     */
    @Schema(description = "收藏数")
    private Long favoriteCount;

    /**
     * 当前用户是否已点赞（从Redis缓存获取）
     */
    @Schema(description = "当前用户是否已点赞")
    private Boolean isLiked;

    /**
     * 当前用户是否已收藏（从Redis缓存获取）
     */
    @Schema(description = "当前用户是否已收藏")
    private Boolean isFavorited;

    /**
     * 作者信息（包含粉丝数和关注状态）
     */
    @Schema(description = "作者信息")
    private DocHomeNoteAuthorVo author;
}
