package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * 视频合集详情视图对象（包含视频列表）
 * <p>
 * 继承自 {@link DocVideoCollectionVo}，在基础合集信息之上扩展了视频列表字段。
 * 用于合集详情页展示，包含合集中所有视频的完整信息（含视频文件和缩略图URL）。
 * 视频列表按用户在合集中设定的排序顺序排列。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "视频合集详情视图对象")
public class DocVideoCollectionDtlVo extends DocVideoCollectionVo {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 合集中的视频列表
     * <p>
     * 包含该合集中所有视频的详细信息，每个视频包含：
     * - 基础信息（标题、分类、标签等）
     * - 视频文件信息（播放URL）
     * - 缩略图信息（封面URL）
     * 列表按 sort_order 升序排列，即用户在合集中设定的播放顺序。
     * </p>
     */
    @Schema(description = "合集中的视频列表")
    private List<DocVideoListVo> videoList;

}
