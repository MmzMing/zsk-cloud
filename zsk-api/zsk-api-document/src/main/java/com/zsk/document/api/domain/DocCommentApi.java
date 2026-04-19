package com.zsk.document.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 文档评论 API对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
public class DocCommentApi implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private String content;
    private AuthorInfo author;
    private String createdAt;
    private Integer likes;
    private Boolean isLiked;
    private List<DocCommentApi> replies;
    private ReplyToInfo replyTo;

    @Data
    public static class AuthorInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private String id;
        private String name;
        private String avatar;
    }

    @Data
    public static class ReplyToInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private String id;
        private String name;
    }
}