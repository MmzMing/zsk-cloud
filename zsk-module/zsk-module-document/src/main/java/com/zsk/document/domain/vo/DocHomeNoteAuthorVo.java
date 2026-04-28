package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 前台首页笔记作者信息视图对象
 * <p>
 * 用于前台首页笔记详情页展示作者信息，仅包含公开展示的字段。
 * 粉丝数和关注状态通过 Redis 缓存服务获取。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-29
 */
@Data
@Schema(description = "前台首页笔记作者信息")
public class DocHomeNoteAuthorVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 作者用户ID
     */
    @Schema(description = "作者用户ID")
    private Long id;

    /**
     * 作者昵称
     */
    @Schema(description = "作者昵称")
    private String name;

    /**
     * 作者头像URL
     */
    @Schema(description = "作者头像URL")
    private String avatar;

    /**
     * 作者粉丝数（从Redis缓存获取）
     */
    @Schema(description = "作者粉丝数")
    private Integer fans;

    /**
     * 当前用户是否已关注该作者（从Redis缓存获取）
     */
    @Schema(description = "当前用户是否已关注该作者")
    private Boolean isFollowing;
}
