package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 前台首页视频评论视图对象
 * <p>
 * 用于前台首页视频详情页展示评论信息。
 * 采用B站式二级评论结构：根评论包含回复列表，回复统一挂在根评论下。
 * 点赞数从Redis缓存获取，不依赖数据库字段。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-29
 */
@Data
@Schema(description = "前台首页视频评论")
public class DocHomeVideoCommentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @Schema(description = "评论ID")
    private Long id;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容")
    private String content;

    /**
     * 评论作者信息
     */
    @Schema(description = "评论作者信息")
    private DocHomeNoteAuthorVo author;

    /**
     * 评论创建时间
     */
    @Schema(description = "评论创建时间")
    private String createdAt;

    /**
     * 评论点赞数（从Redis缓存获取）
     */
    @Schema(description = "评论点赞数")
    private Integer likes;

    /**
     * 当前用户是否已点赞该评论（从Redis缓存获取）
     */
    @Schema(description = "当前用户是否已点赞该评论")
    private Boolean isLiked;

    /**
     * 回复列表（二级结构，所有回复统一挂在根评论下）
     */
    @Schema(description = "回复列表")
    private List<DocHomeVideoCommentVo> replies;

    /**
     * 回复目标用户信息（用于显示"A回复B"）
     */
    @Schema(description = "回复目标用户信息")
    private DocHomeNoteAuthorVo replyTo;
}
