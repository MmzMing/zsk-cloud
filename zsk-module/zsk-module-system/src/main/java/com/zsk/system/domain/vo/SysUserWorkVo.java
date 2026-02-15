package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户作品项 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "用户作品项")
public class SysUserWorkVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 作品ID */
    @Schema(description = "作品ID")
    private String id;

    /** 作品类型（video/article/document） */
    @Schema(description = "作品类型（video/article/document）")
    private String type;

    /** 标题 */
    @Schema(description = "标题")
    private String title;

    /** 封面图URL */
    @Schema(description = "封面图URL")
    private String coverUrl;

    /** 浏览量 */
    @Schema(description = "浏览量")
    private Long views;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private String createdAt;
}
