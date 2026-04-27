package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 评论项 视图对象
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
@Schema(description = "评论项")
public class DocCommentVo implements Serializable {

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
     * 回复列表
     */
    @Schema(description = "回复列表")
    private List<DocCommentVo> replies;

    /**
     * 回复对象
     */
    @Schema(description = "回复对象")
    private DocUserVo replyTo;

    /**
     * 评论状态（1-正常 2-隐藏 3-删除）
     */
    @Schema(description = "评论状态（1-正常 2-隐藏 3-删除）")
    private Integer status;
}
