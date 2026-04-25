package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "笔记列表视图对象")
public class DocNoteListVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "笔记ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "笔记名称")
    private String noteName;

    @Schema(description = "笔记标签")
    private String noteTags;

    @Schema(description = "文档内容")
    private String content;

    @Schema(description = "笔记简介/描述")
    private String description;

    @Schema(description = "封面文件信息")
    private DocFileInfoVo coverFile;

    @Schema(description = "大类")
    private String broadCode;

    @Schema(description = "小类")
    private String narrowCode;

    @Schema(description = "笔记等级")
    private Integer noteGrade;

    @Schema(description = "笔记模式")
    private Integer noteMode;

    @Schema(description = "适合人群")
    private String suitableUsers;

    @Schema(description = "审核状态")
    private Integer auditStatus;

    @Schema(description = "笔记状态")
    private Integer status;

    @Schema(description = "笔记发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "是否置顶（0否 1是）")
    private Integer isPinned;

    @Schema(description = "是否推荐（0否 1是）")
    private Integer isRecommended;

    @Schema(description = "SEO标题")
    private String seoTitle;

    @Schema(description = "SEO描述")
    private String seoDescription;

    @Schema(description = "SEO关键词")
    private String seoKeywords;

    @Schema(description = "删除标记")
    private Integer deleted;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}