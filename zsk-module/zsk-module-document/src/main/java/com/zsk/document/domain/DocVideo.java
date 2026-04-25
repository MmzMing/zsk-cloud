package com.zsk.document.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 视频表对象 document_video
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("document_video")
@Schema(description = "视频对象")
public class DocVideo extends TenantEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件ID（关联document_files.id）
     */
    @Schema(description = "文件ID")
    private Long fileId;

    /**
     * 所属用户ID
     */
    @Schema(description = "所属用户ID")
    private Long userId;

    /**
     * 视频标题
     */
    @Schema(description = "视频标题")
    private String videoTitle;

    /**
     * 大类（如：技术、生活、职场）
     */
    @Schema(description = "大类")
    private String broadCode;

    /**
     * 小类（如：技术-Java、生活-美食）
     */
    @Schema(description = "小类")
    private String narrowCode;

    /**
     * 标签（多个用英文逗号分隔）
     */
    @Schema(description = "标签")
    private String tags;

    /**
     * 视频描述/文本内容
     */
    @Schema(description = "视频描述")
    private String fileContent;

    /**
     * 封面图片文件ID（关联document_files.id）
     */
    @Schema(description = "封面图片文件ID")
    private Long coverFileId;

    /**
     * 元数据（JSON格式，如分辨率、时长、编码等）
     */
    @Schema(description = "元数据")
    private String metaData;

    /**
     * 审核状态（0-待审核 1-审核通过 2-审核驳回）
     */
    @Schema(description = "审核状态（0-待审核 1-审核通过 2-审核驳回）")
    private Integer auditStatus;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String auditMind;

    /**
     * 审核记录ID
     */
    @Schema(description = "审核记录ID")
    private Long auditId;

    /**
     * 状态（1-正常 2-下架 3-草稿）
     */
    @Schema(description = "状态（1-正常 2-下架 3-草稿）")
    private Integer status;

    /**
     * 是否置顶（0否 1是）
     */
    @Schema(description = "是否置顶（0否 1是）")
    private Integer isPinned;

    /**
     * 是否推荐（0否 1是）
     */
    @Schema(description = "是否推荐（0否 1是）")
    private Integer isRecommended;


}
