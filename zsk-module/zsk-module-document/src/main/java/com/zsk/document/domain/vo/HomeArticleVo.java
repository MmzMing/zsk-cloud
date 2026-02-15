package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 首页文章项 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "首页文章项")
public class HomeArticleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 文章ID */
    @Schema(description = "文章ID")
    private String id;

    /** 文章分类 */
    @Schema(description = "文章分类")
    private String category;

    /** 文章标题 */
    @Schema(description = "文章标题")
    private String title;

    /** 发布日期 */
    @Schema(description = "发布日期")
    private String date;

    /** 文章摘要 */
    @Schema(description = "文章摘要")
    private String summary;

    /** 浏览量 */
    @Schema(description = "浏览量")
    private String views;

    /** 作者 */
    @Schema(description = "作者")
    private String author;

    /** 封面图URL */
    @Schema(description = "封面图URL")
    private String cover;
}
