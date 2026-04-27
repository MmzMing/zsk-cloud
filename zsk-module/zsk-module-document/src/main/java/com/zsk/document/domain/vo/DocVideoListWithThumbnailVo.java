package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视频列表视图对象（带缩略图）
 *
 * <p>继承自 {@link DocVideoListVo}，额外提供顶层的缩略图字段，
 * 用于需要直接访问缩略图信息的场景（如首页列表、卡片展示等）。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "视频列表视图对象（带缩略图）")
public class DocVideoListWithThumbnailVo extends DocVideoListVo {

    private static final long serialVersionUID = 1L;

    /**
     * 缩略图文件信息
     *
     * <p>从 videoFile.thumbnail 中提取，便于前端直接获取缩略图URL。</p>
     */
    @Schema(description = "缩略图文件信息")
    private DocFileInfoVo thumbnail;
}
