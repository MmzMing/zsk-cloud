package com.zsk.document.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 评论请求参数DTO
 *
 * @author wuhuaming
 */
@Data
@Schema(description = "评论请求参数")
public class CommentRequestDTO {

    @Schema(description = "文档ID")
    private Long docId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论ID")
    private Long parentId;

    @Schema(description = "回复用户ID")
    private Long replyToId;
}