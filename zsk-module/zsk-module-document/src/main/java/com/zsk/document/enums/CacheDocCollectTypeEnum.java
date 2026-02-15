package com.zsk.document.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 缓存文档收藏类型枚举
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Getter
@AllArgsConstructor
public enum CacheDocCollectTypeEnum {

    /**
     * 笔记收藏
     */
    NOTE(1, "note", "笔记收藏"),
    /**
     * 视频收藏
     */
    VIDEO(2, "video", "视频收藏");

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
    public static CacheDocCollectTypeEnum getByCode(Integer code) {
        for (CacheDocCollectTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
