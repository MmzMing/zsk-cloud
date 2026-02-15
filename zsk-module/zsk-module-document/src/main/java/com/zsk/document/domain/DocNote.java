package com.zsk.document.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zsk.common.datasource.domain.entity.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 笔记信息表对象 doc_note
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("document_note")
@Schema(description = "笔记信息对象")
public class DocNote extends TenantEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Schema(description = "用户ID")
    private Long userId;

    /** 笔记名称 */
    @Schema(description = "笔记名称")
    private String noteName;

    /** 笔记标签 */
    @Schema(description = "笔记标签")
    private String noteTags;

    /** 文档内容 */
    @Schema(description = "文档内容")
    private String content;

    /** 笔记简介/描述 */
    @Schema(description = "笔记简介/描述")
    private String description;

    /** 大类 */
    @Schema(description = "大类")
    private String broadCode;

    /** 小类 */
    @Schema(description = "小类")
    private String narrowCode;

    /** 笔记等级 */
    @Schema(description = "笔记等级")
    private Integer noteGrade;

    /** 笔记模式 */
    @Schema(description = "笔记模式")
    private Integer noteMode;

    /** 适合人群 */
    @Schema(description = "适合人群")
    private String suitableUsers;

    /** 审核状态 */
    @Schema(description = "审核状态")
    private Integer auditStatus;

    /** 笔记状态 */
    @Schema(description = "笔记状态")
    private Integer status;

    /** 笔记发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "笔记发布时间")
    private LocalDateTime publishTime;

    /** 笔记浏览量 */
    @Schema(description = "笔记浏览量")
    private Long viewCount;

    /** 笔记点赞量 */
    @Schema(description = "笔记点赞量")
    private Long likeCount;

    /** 评论量 */
    @Schema(description = "评论量")
    private Long commentCount;

    /** 收藏量 */
    @Schema(description = "收藏量")
    private Long collectCount;

    /** 封面图 */
    @Schema(description = "封面图")
    private String cover;

    /** 是否置顶（0否 1是） */
    @Schema(description = "是否置顶（0否 1是）")
    private Integer isPinned;

    /** 是否推荐（0否 1是） */
    @Schema(description = "是否推荐（0否 1是）")
    private Integer isRecommended;

    /** SEO标题 */
    @Schema(description = "SEO标题")
    private String seoTitle;

    /** SEO描述 */
    @Schema(description = "SEO描述")
    private String seoDescription;

    /** SEO关键词 */
    @Schema(description = "SEO关键词")
    private String seoKeywords;

    /** 乐观锁版本号 */
    @Schema(description = "乐观锁版本号")
    private Long version;
}
