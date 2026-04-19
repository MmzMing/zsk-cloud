package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 评论请求参数
 *
 * @author wuhuaming
 */
@Data
@Schema(description = "评论请求参数")
public class CommentRequestVo {

    @Schema(description = "文档ID")
    private String docId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论ID")
    private String parentId;

    @Schema(description = "回复用户ID")
    private String replyToId;
}
