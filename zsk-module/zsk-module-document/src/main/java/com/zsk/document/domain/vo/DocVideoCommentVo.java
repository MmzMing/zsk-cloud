package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 视频评论 视图对象
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
@Schema(description = "视频评论")
public class DocVideoCommentVo implements Serializable {

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
     * 作者信息
     */
    @Schema(description = "作者信息")
    private DocUserVo author;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createdAt;

    /**
     * 点赞数
     */
    @Schema(description = "点赞数")
    private Integer likes;

    /**
     * 是否已点赞
     */
    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    /**
     * 子评论列表
     */
    @Schema(description = "子评论列表")
    private List<DocVideoCommentVo> replies;

    /**
     * 回复对象信息
     */
    @Schema(description = "回复对象信息")
    private DocUserVo replyTo;
}