package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 笔记首页详情作者信息视图对象
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-25
 */
@Data
@Schema(description = "笔记首页详情作者信息")
public class DocNoteHomeDetailAuthorVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 作者ID
     */
    @Schema(description = "作者ID")
    private Long id;

    /**
     * 作者名称
     */
    @Schema(description = "作者名称")
    private String name;

    /**
     * 作者头像URL
     */
    @Schema(description = "作者头像URL")
    private String avatar;

    /**
     * 粉丝数
     */
    @Schema(description = "粉丝数")
    private String fans;

    /**
     * 是否已关注
     */
    @Schema(description = "是否已关注")
    private Boolean isFollowing;
}
