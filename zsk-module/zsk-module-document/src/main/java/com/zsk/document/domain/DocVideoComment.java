package com.zsk.document.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 视频详情评论表对象 doc_video_comment
 * 
 * @author wuhuaming
 * @date 2026-02-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("document_video_comment")
@Schema(description = "视频详情评论对象")
public class DocVideoComment extends TenantEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 关联视频文件ID（关联document_files.file_id） */
    @Schema(description = "关联视频文件ID")
    private String videoId;

    /** 评论人ID */
    @Schema(description = "评论人ID")
    private String commentUserId;

    /** 评论内容 */
    @Schema(description = "评论内容")
    private String commentContent;

    /** 父评论ID（用于回复：NULL为根评论，非NULL为回复某条评论） */
    @Schema(description = "父评论ID")
    private Long parentCommentId;

    /** 审核状态（0-待审核 1-审核通过 2-审核驳回） */
    @Schema(description = "审核状态")
    private Integer auditStatus;

    /** 评论状态（1-正常 2-隐藏 3-删除） */
    @Schema(description = "评论状态")
    private Integer status;

    /** 评论点赞数 */
    @Schema(description = "评论点赞数")
    private Long likeCount;

    /** 乐观锁版本号 */
    @Schema(description = "乐观锁版本号")
    private Long version;
}
