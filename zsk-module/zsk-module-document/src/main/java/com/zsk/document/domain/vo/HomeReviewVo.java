package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 首页评论项 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "首页评论项")
public class HomeReviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 评价ID */
    @Schema(description = "评价ID")
    private String id;

    /** 评价人姓名 */
    @Schema(description = "评价人姓名")
    private String name;

    /** 评价人角色/职位 */
    @Schema(description = "评价人角色/职位")
    private String role;

    /** 评价来源 */
    @Schema(description = "评价来源")
    private String source;

    /** 评价日期 */
    @Schema(description = "评价日期")
    private String date;

    /** 评价内容 */
    @Schema(description = "评价内容")
    private String content;

    /** 情感倾向 */
    @Schema(description = "情感倾向")
    private String tone;
}
