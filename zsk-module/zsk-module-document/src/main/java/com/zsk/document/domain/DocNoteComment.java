package com.zsk.document.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 笔记评论表对象 doc_note_comment
 * <p>
 * 采用B站式评论结构：所有回复统一挂在根评论下，不存在层级嵌套。
 * parentCommentId 统一记录根评论ID（NULL表示根评论），
 * replyUserId 记录被回复的用户ID（用于显示"A回复B"）。
 * 评论点赞数从Redis获取，不再存储在表中。
 * </p>
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-27
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
    private Long commentUserId;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容")
    private String commentContent;

    /**
     * 父评论ID（统一记录根评论ID，NULL表示根评论）
     */
    @Schema(description = "父评论ID（统一记录根评论ID，NULL表示根评论）")
    private Long parentCommentId;

    /**
     * 回复人ID（记录被回复的用户ID，用于显示A回复B）
     */
    @Schema(description = "回复人ID（记录被回复的用户ID，用于显示A回复B）")
    private Long replyUserId;

    /**
     * 审核状态（0-待审核 1-审核通过 2-审核驳回）
     */
    @Schema(description = "审核状态")
    private Integer auditStatus;

    /**
     * 评论状态（1-正常 2-隐藏 3-删除）
     */
    @Schema(description = "评论状态")
    private Integer status;

    /**
     * 乐观锁版本号
     */
    @Schema(description = "乐观锁版本号")
    private Long version;
}
