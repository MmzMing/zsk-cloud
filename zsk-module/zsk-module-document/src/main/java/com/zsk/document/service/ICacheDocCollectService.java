package com.zsk.document.service;

import java.util.Map;

/**
 * 缓存文档收藏服务接口
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
public interface ICacheDocCollectService {

    /**
     * 收藏
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否收藏成功
     */
    boolean collect(Integer type, Long targetId, Long userId);

    /**
     * 取消收藏
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否取消成功
     */
    boolean uncollect(Integer type, Long targetId, Long userId);

    /**
     * 获取收藏数量
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     * @return 收藏数量
     */
    Long getCollectCount(Integer type, Long targetId);

    /**
     * 判断用户是否已收藏
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否已收藏
     */
    boolean hasCollected(Integer type, Long targetId, Long userId);

    /**
     * 批量获取收藏数量
     *
     * @param type      收藏类型
     * @param targetIds 目标ID列表
     * @return 目标ID与收藏数量的映射
     */
    Map<Long, Long> getCollectCountBatch(Integer type, Iterable<Long> targetIds);

    /**
     * 同步收藏数据到数据库
     */
    void syncCollectDataToDb();
}
