package com.zsk.document.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 文档详情 API对象
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
public class DocNoteDetailApi implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String content;
    private String category;
    private String date;
    private String coverUrl;
    private AuthorInfo author;
    private StatsInfo stats;
    private List<RecommendDoc> recommendations;

    @Data
    public static class AuthorInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private String id;
        private String name;
        private String avatar;
        private String fans;
        private Boolean isFollowing;
    }

    @Data
    public static class StatsInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private String views;
        private Integer likes;
        private Integer favorites;
        private Boolean isLiked;
        private Boolean isFavorited;
    }

    @Data
    public static class RecommendDoc implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private String id;
        private String title;
        private String views;
    }
}