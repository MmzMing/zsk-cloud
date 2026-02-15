package com.zsk.document.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 用户交互关系表对象 doc_user_interaction
 * 统一管理点赞、收藏、关注关系
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_user_interaction")
@Schema(description = "用户交互关系对象")
public class DocUserInteraction extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Schema(description = "用户ID")
    private Long userId;

    /** 目标类型（1-文档 2-视频 3-用户 4-评论） */
    @Schema(description = "目标类型（1-文档 2-视频 3-用户 4-评论）")
    private Integer targetType;

    /** 目标ID */
    @Schema(description = "目标ID")
    private Long targetId;

    /** 交互类型（1-点赞 2-收藏 3-关注） */
    @Schema(description = "交互类型（1-点赞 2-收藏 3-关注）")
    private Integer interactionType;

    /** 状态（0-取消 1-有效） */
    @Schema(description = "状态（0-取消 1-有效）")
    private Integer status;
}
