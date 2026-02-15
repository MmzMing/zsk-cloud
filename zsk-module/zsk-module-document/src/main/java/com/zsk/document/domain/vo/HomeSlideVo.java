package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 首页幻灯片 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "首页幻灯片")
public class HomeSlideVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 幻灯片ID */
    @Schema(description = "幻灯片ID")
    private String id;

    /** 幻灯片标签 */
    @Schema(description = "幻灯片标签")
    private String tag;

    /** 幻灯片标题 */
    @Schema(description = "幻灯片标题")
    private String title;

    /** 幻灯片描述 */
    @Schema(description = "幻灯片描述")
    private String description;

    /** 核心特性列表 */
    @Schema(description = "核心特性列表")
    private List<FeatureCardVo> features;

    /** 完整特性列表 */
    @Schema(description = "完整特性列表")
    private List<FeatureCardVo> featureList;

    /** 预览组件类型 */
    @Schema(description = "预览组件类型")
    private String previewType;

    /**
     * 特性卡片
     */
    @Data
    @Schema(description = "特性卡片")
    public static class FeatureCardVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 特性标题 */
        @Schema(description = "特性标题")
        private String title;

        /** 特性描述 */
        @Schema(description = "特性描述")
        private String description;

        /** 特性标签 */
        @Schema(description = "特性标签")
        private String tag;
    }
}
