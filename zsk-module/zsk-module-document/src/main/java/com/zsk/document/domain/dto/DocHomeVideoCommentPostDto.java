package com.zsk.document.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 前台首页视频评论发表/回复请求DTO
 * <p>
 * 发表根评论和回复评论使用同一个接口。
 * 发表根评论时 parentId 和 replyToId 为空；
 * 回复评论时 parentId 传根评论ID，replyToId 传被回复用户ID。
 * 采用B站式评论结构：所有回复统一挂在根评论下，不存在层级嵌套。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-29
 */
@Data
@Schema(description = "前台首页视频评论发表/回复请求")
public class DocHomeVideoCommentPostDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 视频ID
     */
    @Schema(description = "视频ID")
    private Long videoId;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容")
    private String content;

    /**
     * 父评论ID（根评论时为空，回复时传根评论ID）
     */
    @Schema(description = "父评论ID")
    private Long parentId;

    /**
     * 回复目标用户ID（直接回复根评论时为空，回复某条评论时传该评论的用户ID）
     */
    @Schema(description = "回复目标用户ID")
    private Long replyToId;
}
