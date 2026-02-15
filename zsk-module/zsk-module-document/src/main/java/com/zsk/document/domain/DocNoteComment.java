package com.zsk.document.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 笔记评论表对象 doc_note_comment
 *
 * @author wuhuaming
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("document_note_comment")
@Schema(description = "笔记评论对象")
public class DocNoteComment extends TenantEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联笔记ID
     */
    @Schema(description = "关联笔记ID")
    private Long noteId;

    /**
     * 评论人ID
     */
    @Schema(description = "评论人ID")
    private String commentUserId;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容")
    private String commentContent;

    /**
     * 父评论ID
     */
    @Schema(description = "父评论ID")
    private Long parentCommentId;

    /**
     * 审核状态
     */
    @Schema(description = "审核状态")
    private Integer auditStatus;

    /**
     * 评论状态
     */
    @Schema(description = "评论状态")
    private Integer status;

    /**
     * 评论点赞数
     */
    @Schema(description = "评论点赞数")
    private Long likeCount;

    /**
     * 乐观锁版本号
     */
    @Schema(description = "乐观锁版本号")
    private Long version;
}
