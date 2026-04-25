package com.zsk.document.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 视频评论请求参数DTO
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-25
 */
@Data
@Schema(description = "视频评论请求参数")
public class VideoCommentRequestDTO {

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
     * 父评论ID
     */
    @Schema(description = "父评论ID")
    private Long parentId;

    /**
     * 回复用户ID
     */
    @Schema(description = "回复用户ID")
    private Long replyToId;
}