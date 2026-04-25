package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "视频列表视图对象")
public class DocVideoListVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "视频ID")
    private Long id;

    @Schema(description = "视频文件信息")
    private DocVideoFileVo videoFile;

    @Schema(description = "所属用户ID")
    private Long userId;

    @Schema(description = "视频标题")
    private String videoTitle;

    @Schema(description = "大类")
    private String broadCode;

    @Schema(description = "小类")
    private String narrowCode;

    @Schema(description = "标签（多个用英文逗号分隔）")
    private String tags;

    @Schema(description = "视频描述/文本内容")
    private String fileContent;

    @Schema(description = "元数据（JSON格式，如分辨率、时长、编码等）")
    private String metaData;

    @Schema(description = "审核状态（0-待审核 1-审核通过 2-审核驳回）")
    private Integer auditStatus;

    @Schema(description = "审核意见")
    private String auditMind;

    @Schema(description = "审核记录ID")
    private Long auditId;

    @Schema(description = "状态（1-正常 2-下架 3-草稿）")
    private Integer status;

    @Schema(description = "是否置顶（0否 1是）")
    private Integer isPinned;

    @Schema(description = "是否推荐（0否 1是）")
    private Integer isRecommended;

    @Schema(description = "删除标记")
    private Integer deleted;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}