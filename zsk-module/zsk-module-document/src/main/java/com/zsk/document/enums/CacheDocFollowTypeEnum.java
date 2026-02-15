package com.zsk.document.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 缓存文档关注类型枚举
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Getter
@AllArgsConstructor
public enum CacheDocFollowTypeEnum {

    /**
     * 关注用户
     */
    USER(1, "user", "关注用户"),
    /**
     * 关注笔记作者
     */
    NOTE_AUTHOR(2, "note_author", "关注笔记作者"),
    /**
     * 关注视频作者
     */
    VIDEO_AUTHOR(3, "video_author", "关注视频作者");

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
    public static CacheDocFollowTypeEnum getByCode(Integer code) {
        for (CacheDocFollowTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
