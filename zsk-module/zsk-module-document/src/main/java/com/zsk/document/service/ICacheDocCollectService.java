package com.zsk.document.service;

import java.util.Map;

/**
 * 缓存文档收藏服务接口
 * <p>
 * 提供基于Redis的收藏功能，包括收藏/取消收藏、查询收藏数、判断用户是否收藏、批量获取收藏数以及定时同步到数据库。
 * </p>
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-25
 */
public interface ICacheDocCollectService {

    /**
     * 收藏
     * <p>
     * 用户对目标内容进行收藏操作。
     * </p>
     *
     * @param type     收藏类型（1-笔记 2-视频）
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否收藏成功（重复收藏返回false）
     */
    boolean collect(Integer type, Long targetId, Long userId);

    /**
     * 取消收藏
     * <p>
     * 用户对目标内容取消收藏操作。
     * </p>
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否取消成功（未收藏返回false）
     */
    boolean uncollect(Integer type, Long targetId, Long userId);

    /**
     * 获取用户收藏数
     * <p>
     * 获取指定用户总共收藏的数量。
     * </p>
     *
     * @param userId 用户ID
     * @return 用户收藏数量
     */
    Long getUserCollectCount(Long userId);

    /**
     * 获取来源收藏数
     * <p>
     * 获取指定目标内容的总收藏数量。
     * </p>
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     * @return 来源收藏数量
     */
    Long getCollectCount(Integer type, Long targetId);

    /**
     * 查询用户是否收藏
     * <p>
     * 判断指定用户是否对目标内容已收藏。
     * </p>
     *
     * @param type     收藏类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否已收藏
     */
    boolean hasCollected(Integer type, Long targetId, Long userId);

    /**
     * 根据多个来源，批量获取收藏数
     * <p>
     * 批量查询多个目标内容的收藏数量。
     * </p>
     *
     * @param type      收藏类型
     * @param targetIds 目标ID列表
     * @return 目标ID与收藏数量的映射
     */
    Map<Long, Long> getCollectCountBatch(Integer type, Iterable<Long> targetIds);

    /**
 * 同步收藏数据到数据库
 * <p>
 * 将Redis中的收藏数据同步到数据库持久化。
 * 由定时任务调用。
 * </p>
 */
void syncCollectDataToDb();

/**
 * 从数据库预热收藏缓存
 * <p>
 * 将数据库中的收藏数据加载到Redis缓存，用于服务重启后的缓存预热。
 * 加载用户收藏状态到Bitmap，收藏计数到Hash。
 * </p>
 *
 * @param type     收藏类型
 * @param targetId 目标ID
 */
void warmCollectCacheFromDb(Integer type, Long targetId);
}
