package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 视频首页详情视图对象
 * <p>
 * 用于前台视频详情页展示，包含视频基本信息、作者信息、统计信息等。
 * 统计信息通过独立的 {@link DocVideoHomeDetailStatsInfoVo} 封装，
 * 作者信息通过独立的 {@link DocVideoHomeDetailAuthorVo} 封装。
 * </p>
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-25
 */
@Data
@Schema(description = "视频首页详情")
public class DocVideoHomeDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 视频ID
     */
    @Schema(description = "视频ID")
    private String id;

    /**
     * 视频标题
     */
    @Schema(description = "视频标题")
    private String title;

    /**
     * 视频描述
     */
    @Schema(description = "视频描述")
    private String description;

    /**
     * 视频播放地址
     */
    @Schema(description = "视频播放地址")
    private String videoUrl;

    /**
     * 封面图URL
     */
    @Schema(description = "封面图URL")
    private String coverUrl;

    /**
     * 作者信息
     */
    @Schema(description = "作者信息")
    private DocVideoHomeDetailAuthorVo author;

    /**
     * 统计信息
     */
    @Schema(description = "统计信息")
    private DocVideoHomeDetailStatsInfoVo stats;

    /**
     * 标签列表
     */
    @Schema(description = "标签列表")
    private List<String> tags;
}
