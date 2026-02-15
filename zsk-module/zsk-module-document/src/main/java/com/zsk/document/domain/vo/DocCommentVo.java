package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论项 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "评论项")
public class DocCommentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 评论ID */
    @Schema(description = "评论ID")
    private String id;

    /** 评论内容 */
    @Schema(description = "评论内容")
    private String content;

    /** 作者信息 */
    @Schema(description = "作者信息")
    private AuthorInfo author;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private String createdAt;

    /** 点赞数 */
    @Schema(description = "点赞数")
    private Integer likes;

    /** 是否已点赞 */
    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    /** 回复列表 */
    @Schema(description = "回复列表")
    private List<DocCommentVo> replies;

    /** 回复对象 */
    @Schema(description = "回复对象")
    private ReplyToInfo replyTo;

    /**
     * 作者信息
     */
    @Data
    @Schema(description = "作者信息")
    public static class AuthorInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 作者ID */
        @Schema(description = "作者ID")
        private String id;

        /** 名称 */
        @Schema(description = "名称")
        private String name;

        /** 头像 */
        @Schema(description = "头像")
        private String avatar;
    }

    /**
     * 回复对象信息
     */
    @Data
    @Schema(description = "回复对象信息")
    public static class ReplyToInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 用户ID */
        @Schema(description = "用户ID")
        private String id;

        /** 用户名称 */
        @Schema(description = "用户名称")
        private String name;
    }
}
