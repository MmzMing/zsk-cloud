package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 前台首页视频合集视图对象
 * <p>
 * 用于前台首页视频合集区域展示合集信息及其视频列表。
 * 仅包含公开展示的字段，私密合集不对外展示。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-29
 */
@Data
@Schema(description = "前台首页视频合集")
public class DocHomeVideoCollectionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 合集ID
     */
    @Schema(description = "合集ID")
    private Long id;

    /**
     * 合集名称
     */
    @Schema(description = "合集名称")
    private String collectionName;

    /**
     * 合集描述
     */
    @Schema(description = "合集描述")
    private String description;

    /**
     * 封面图URL
     */
    @Schema(description = "封面图URL")
    private String coverUrl;

    /**
     * 视频数量
     */
    @Schema(description = "视频数量")
    private Integer videoCount;

    /**
     * 合集中的视频列表
     */
    @Schema(description = "合集中的视频列表")
    private List<DocHomeVideoCollectionItemVo> videos;
}
