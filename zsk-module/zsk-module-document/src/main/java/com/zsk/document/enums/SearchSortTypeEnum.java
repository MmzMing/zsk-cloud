package com.zsk.document.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 搜索排序类型枚举
 * <p>
 * 定义全局搜索支持的排序方式，用于控制搜索结果的排序规则。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-28
 */
@Getter
@AllArgsConstructor
public enum SearchSortTypeEnum {

    /**
     * 默认排序（按创建时间降序）
     */
    DEFAULT("default", "默认"),

    /**
     * 热门排序（按浏览量降序）
     */
    HOT("hot", "热门"),

    /**
     * 最新排序（按创建时间降序）
     */
    LATEST("latest", "最新"),

    /**
     * 点赞排序（按点赞数降序）
     */
    LIKE("like", "点赞");

    /**
     * 排序编码
     */
    private final String code;

    /**
     * 排序描述
     */
    private final String desc;

    /**
     * 根据编码获取枚举
     *
     * @param code 排序编码
     * @return 枚举对象，未找到返回 null
     */
    public static SearchSortTypeEnum getByCode(String code) {
        if (code == null || code.isEmpty()) {
            return DEFAULT;
        }
        for (SearchSortTypeEnum sortEnum : values()) {
            if (sortEnum.getCode().equals(code)) {
                return sortEnum;
            }
        }
        return DEFAULT;
    }

    /**
     * 判断是否为热门排序
     *
     * @param code 排序编码
     * @return true-是热门排序
     */
    public static boolean isHot(String code) {
        return HOT.getCode().equals(code);
    }

    /**
     * 判断是否为点赞排序
     *
     * @param code 排序编码
     * @return true-是点赞排序
     */
    public static boolean isLike(String code) {
        return LIKE.getCode().equals(code);
    }

    /**
     * 判断是否按最新排序
     *
     * @param code 排序编码
     * @return true-是最新排序
     */
    public static boolean isLatest(String code) {
        return LATEST.getCode().equals(code);
    }
}
