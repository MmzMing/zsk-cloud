package com.zsk.document.service;

import java.util.Map;

/**
 * 缓存文档关注服务接口
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
public interface ICacheDocFollowService {

    /**
     * 关注
     *
     * @param type     关注类型
     * @param targetId 目标ID（被关注者ID）
     * @param userId   用户ID（关注者ID）
     * @return 是否关注成功
     */
    boolean follow(Integer type, Long targetId, Long userId);

    /**
     * 取消关注
     *
     * @param type     关注类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否取消成功
     */
    boolean unfollow(Integer type, Long targetId, Long userId);

    /**
     * 获取关注数量（粉丝数）
     *
     * @param type     关注类型
     * @param targetId 目标ID（被关注者ID）
     * @return 关注数量
     */
    Long getFollowCount(Integer type, Long targetId);

    /**
     * 判断用户是否已关注
     *
     * @param type     关注类型
     * @param targetId 目标ID
     * @param userId   用户ID
     * @return 是否已关注
     */
    boolean hasFollowed(Integer type, Long targetId, Long userId);

    /**
     * 批量获取关注数量
     *
     * @param type      关注类型
     * @param targetIds 目标ID列表
     * @return 目标ID与关注数量的映射
     */
    Map<Long, Long> getFollowCountBatch(Integer type, Iterable<Long> targetIds);

    /**
     * 同步关注数据到数据库
     */
    void syncFollowDataToDb();
}
