package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 笔记首页详情视图对象
 * <p>
 * 用于前台笔记详情页展示，包含笔记基本信息、作者信息、统计信息等。
 * 统计信息通过独立的 {@link DocNoteHomeDetailStatsInfoVo} 封装，
 * 作者信息通过独立的 {@link DocNoteHomeDetailAuthorVo} 封装。
 * </p>
 *
 * @author wuhuaming
 * @date 2026-04-25
 * @version 2.0
 */
@Data
@Schema(description = "笔记首页详情")
public class DocNoteHomeDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 笔记ID */
    @Schema(description = "笔记ID")
    private String id;

    /** 标题 */
    @Schema(description = "标题")
    private String title;

    /** 内容 */
    @Schema(description = "内容")
    private String content;

    /** 分类 */
    @Schema(description = "分类")
    private String category;

    /** 日期 */
    @Schema(description = "日期")
    private String date;

    /** 封面图地址 */
    @Schema(description = "封面图地址")
    private String coverUrl;

    /** 作者信息 */
    @Schema(description = "作者信息")
    private DocNoteHomeDetailAuthorVo author;

    /** 统计信息 */
    @Schema(description = "统计信息")
    private DocNoteHomeDetailStatsInfoVo stats;
}
