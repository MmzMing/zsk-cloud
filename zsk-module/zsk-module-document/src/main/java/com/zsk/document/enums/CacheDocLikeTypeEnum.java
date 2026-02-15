package com.zsk.document.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 缓存文档点赞类型枚举
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Getter
@AllArgsConstructor
public enum CacheDocLikeTypeEnum {

    /**
     * 笔记点赞
     */
    NOTE(1, "note", "笔记点赞"),
    /**
     * 笔记评论点赞
     */
    NOTE_COMMENT(2, "note_comment", "笔记评论点赞"),
    /**
     * 视频点赞
     */
    VIDEO(3, "video", "视频点赞"),
    /**
     * 视频评论点赞
     */
    VIDEO_COMMENT(4, "video_comment", "视频评论点赞");

    /**
     * 类型编码
     */
    private final Integer code;
    /**
     * 类型标识
     */
    private final String type;
    /**
     * 类型描述
     */
    private final String desc;

    /**
     * 根据编码获取枚举
     *
     * @param code 类型编码
     * @return 枚举对象
     */
    public static CacheDocLikeTypeEnum getByCode(Integer code) {
        for (CacheDocLikeTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
