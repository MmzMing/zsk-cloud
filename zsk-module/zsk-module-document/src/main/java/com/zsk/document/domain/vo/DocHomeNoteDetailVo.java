package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 前台首页笔记详情视图对象
 * <p>
 * 用于前台首页笔记详情页展示笔记元信息和内容。
 * 仅包含公开展示的字段，不包含审核状态、版本号等后台管理字段。
 * 获取元信息时优先增加浏览量（Redis）。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-29
 */
@Data
@Schema(description = "前台首页笔记详情")
public class DocHomeNoteDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 笔记ID
     */
    @Schema(description = "笔记ID")
    private Long id;

    /**
     * 笔记标题
     */
    @Schema(description = "笔记标题")
    private String title;

    /**
     * 笔记内容（Markdown格式，从笔记详情表获取）
     */
    @Schema(description = "笔记内容")
    private String content;

    /**
     * 大类分类
     */
    @Schema(description = "大类分类")
    private String category;

    /**
     * 笔记标签
     */
    @Schema(description = "笔记标签")
    private String tags;

    /**
     * 笔记简介
     */
    @Schema(description = "笔记简介")
    private String description;

    /**
     * 封面图地址
     */
    @Schema(description = "封面图地址")
    private String coverUrl;

    /**
     * 发布日期
     */
    @Schema(description = "发布日期")
    private String date;
}
