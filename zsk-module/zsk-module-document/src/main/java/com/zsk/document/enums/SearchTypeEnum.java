package com.zsk.document.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 搜索类型枚举
 * <p>
 * 定义全局搜索支持的资源类型，用于筛选搜索范围。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-28
 */
@Getter
@AllArgsConstructor
public enum SearchTypeEnum {

    /**
     * 全部类型
     */
    ALL("all", "全部"),

    /**
     * 视频
     */
    VIDEO("video", "视频"),

    /**
     * 笔记/文档
     */
    DOCUMENT("document", "笔记");

    /**
     * 类型编码
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String desc;

    /**
     * 根据编码获取枚举
     *
     * @param code 类型编码
     * @return 枚举对象，未找到返回 null
     */
    public static SearchTypeEnum getByCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (SearchTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

    /**
     * 判断是否为全部类型
     *
     * @param code 类型编码
     * @return true-是全部类型
     */
    public static boolean isAll(String code) {
        return ALL.getCode().equals(code);
    }

    /**
     * 判断是否匹配视频类型
     *
     * @param code 类型编码
     * @return true-匹配视频类型（all 或 video）
     */
    public static boolean matchVideo(String code) {
        return ALL.getCode().equals(code) || VIDEO.getCode().equals(code);
    }

    /**
     * 判断是否匹配笔记类型
     *
     * @param code 类型编码
     * @return true-匹配笔记类型（all 或 document）
     */
    public static boolean matchDocument(String code) {
        return ALL.getCode().equals(code) || DOCUMENT.getCode().equals(code);
    }
}
