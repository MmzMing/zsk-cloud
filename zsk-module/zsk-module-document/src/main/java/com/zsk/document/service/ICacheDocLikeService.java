package com.zsk.document.service;

import java.util.Map;

/**
 * 缓存文档点赞服务接口
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
public interface ICacheDocLikeService {

    /**
     * 点赞
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否点赞成功
     */
    boolean like(Integer type, Long targetId, Long userId);

    /**
     * 取消点赞
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否取消成功
     */
    boolean unlike(Integer type, Long targetId, Long userId);

    /**
     * 获取点赞数量
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @return 点赞数量
     */
    Long getLikeCount(Integer type, Long targetId);

    /**
     * 判断用户是否已点赞
     *
     * @param type     点赞类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否已点赞
     */
    boolean hasLiked(Integer type, Long targetId, Long userId);

    /**
     * 批量获取点赞数量
     *
     * @param type      点赞类型
     * @param targetIds 目标ID列表
     * @return 目标ID与点赞数量的映射
     */
    Map<Long, Long> getLikeCountBatch(Integer type, Iterable<Long> targetIds);

    /**
     * 同步点赞数据到数据库
     * 由定时任务调用
     */
    void syncLikeDataToDb();
}
