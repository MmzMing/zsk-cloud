package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 简历模块 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "简历模块")
public class ResumeModuleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 模块ID */
    @Schema(description = "模块ID")
    private String id;

    /** 模块类型（basic/content） */
    @Schema(description = "模块类型（basic/content）")
    private String type;

    /** 模块标题 */
    @Schema(description = "模块标题")
    private String title;

    /** 图标 */
    @Schema(description = "图标")
    private String icon;

    /** 是否可删除 */
    @Schema(description = "是否可删除")
    private Boolean isDeletable;

    /** 是否可见 */
    @Schema(description = "是否可见")
    private Boolean isVisible;

    /** 基础信息数据 */
    @Schema(description = "基础信息数据")
    private BasicInfoVo data;

    /** 富文本内容 */
    @Schema(description = "富文本内容")
    private String content;
}
