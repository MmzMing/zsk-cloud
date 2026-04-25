package com.zsk.document.service;

import java.util.Map;

/**
 * 缓存文档点赞服务接口
 * <p>
 * 提供基于Redis的点赞功能，包括点赞/取消点赞、查询点赞数、判断用户是否点赞、批量获取点赞数以及定时同步到数据库。
 * </p>
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-25
 */
public interface ICacheDocLikeService {

    /**
     * 点赞
     * <p>
     * 用户对目标内容进行点赞操作。
     * </p>
     *
     * @param type     点赞类型（1-笔记 2-笔记评论 3-视频 4-视频评论）
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否点赞成功（重复点赞返回false）
     */
    boolean like(Integer type, Long targetId, Long userId);

    /**
     * 取消点赞
     * <p>
     * 用户对目标内容取消点赞操作。
     * </p>
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否取消成功（未点赞返回false）
     */
    boolean unlike(Integer type, Long targetId, Long userId);

    /**
     * 获取用户点赞数
     * <p>
     * 获取指定用户总共点赞的数量。
     * </p>
     *
     * @param userId 用户ID
     * @return 用户点赞数量
     */
    Long getUserLikeCount(Long userId);

    /**
     * 获取来源点赞数
     * <p>
     * 获取指定目标内容的总点赞数量。
     * </p>
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @return 来源点赞数量
     */
    Long getLikeCount(Integer type, Long targetId);

    /**
     * 查询用户是否点赞
     * <p>
     * 判断指定用户是否对目标内容已点赞。
     * </p>
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否已点赞
     */
    boolean hasLiked(Integer type, Long targetId, Long userId);

    /**
     * 根据多个来源，批量获取点赞数
     * <p>
     * 批量查询多个目标内容的点赞数量。
     * </p>
     *
     * @param type      点赞类型
     * @param targetIds 目标ID列表
     * @return 目标ID与点赞数量的映射
     */
    Map<Long, Long> getLikeCountBatch(Integer type, Iterable<Long> targetIds);

    /**
 * 同步点赞数据到数据库
 * <p>
 * 将Redis中的点赞数据同步到数据库持久化。
 * 由定时任务调用。
 * </p>
 */
void syncLikeDataToDb();

/**
 * 从数据库预热点赞缓存
 * <p>
 * 将数据库中的点赞数据加载到Redis缓存，用于服务重启后的缓存预热。
 * 加载用户点赞状态到Bitmap，点赞计数到Hash。
 * </p>
 *
 * @param type     点赞类型
 * @param targetId 目标ID
 */
void warmLikeCacheFromDb(Integer type, Long targetId);
}
