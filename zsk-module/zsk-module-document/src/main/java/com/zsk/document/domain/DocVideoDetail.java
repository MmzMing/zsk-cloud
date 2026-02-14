package com.zsk.document.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 视频详情表对象 doc_video_detail
 * 
 * @author wuhuaming
 * @date 2026-02-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("document_video_detail")
@Schema(description = "视频详情对象")
public class DocVideoDetail extends TenantEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 文件ID（关联document_files.file_id） */
    @Schema(description = "文件ID")
    private String fileId;

    /** 所属用户ID */
    @Schema(description = "所属用户ID")
    private Long userId;

    /** 大类（如：技术、生活、职场） */
    @Schema(description = "大类")
    private String broadCode;

    /** 小类（如：技术-Java、生活-美食） */
    @Schema(description = "小类")
    private String narrowCode;

    /** 标签（多个用英文逗号分隔） */
    @Schema(description = "标签")
    private String tags;

    /** 视频描述/文本内容 */
    @Schema(description = "视频描述")
    private String fileContent;

    /** 元数据（JSON格式，如分辨率、时长、编码等） */
    @Schema(description = "元数据")
    private String metaData;

    /** 浏览量 */
    @Schema(description = "浏览量")
    private Long viewCount;

    /** 点赞量 */
    @Schema(description = "点赞量")
    private Long likeCount;

    /** 评论量 */
    @Schema(description = "评论量")
    private Long commentCount;

    /** 收藏量 */
    @Schema(description = "收藏量")
    private Long collectCount;

    /** 审核状态（0-待审核 1-审核通过 2-审核驳回） */
    @Schema(description = "审核状态")
    private Integer auditStatus;

    /** 审核意见 */
    @Schema(description = "审核意见")
    private String auditMind;

    /** 状态（1-正常 2-下架 3-草稿） */
    @Schema(description = "状态")
    private Integer status;
}
