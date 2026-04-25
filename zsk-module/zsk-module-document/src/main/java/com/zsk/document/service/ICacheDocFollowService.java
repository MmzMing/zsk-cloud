package com.zsk.document.service;

import java.util.Map;

/**
 * 缓存文档关注服务接口
 * <p>
 * 提供基于Redis的关注功能，包括关注/取消关注、查询粉丝数、判断用户是否关注、批量获取粉丝数以及定时同步到数据库。
 * </p>
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-25
 */
public interface ICacheDocFollowService {

    /**
     * 关注
     * <p>
     * 用户关注目标用户。
     * </p>
     *
     * @param type     关注类型（1-用户）
     * @param targetId 目标用户ID（被关注者ID）
     * @param userId   用户ID（关注者ID）
     * @return 是否关注成功（重复关注返回false）
     */
    boolean follow(Integer type, Long targetId, Long userId);

    /**
     * 取消关注
     * <p>
     * 用户取消关注目标用户。
     * </p>
     *
     * @param type     关注类型
     * @param targetId 目标用户ID
     * @param userId   用户ID
     * @return 是否取消成功（未关注返回false）
     */
    boolean unfollow(Integer type, Long targetId, Long userId);

    /**
     * 获取用户关注数
     * <p>
     * 获取指定用户总共关注了多少人。
     * </p>
     *
     * @param userId 用户ID
     * @return 用户关注数量
     */
    Long getUserFollowCount(Long userId);

    /**
     * 获取来源粉丝数
     * <p>
     * 获取指定目标用户的粉丝数量。
     * </p>
     *
     * @param type     关注类型
     * @param targetId 目标用户ID（被关注者ID）
     * @return 粉丝数量
     */
    Long getFollowCount(Integer type, Long targetId);

    /**
     * 查询用户是否关注
     * <p>
     * 判断指定用户是否已关注目标用户。
     * </p>
     *
     * @param type     关注类型
     * @param targetId 目标用户ID
     * @param userId   用户ID
     * @return 是否已关注
     */
    boolean hasFollowed(Integer type, Long targetId, Long userId);

    /**
     * 根据多个来源，批量获取粉丝数
     * <p>
     * 批量查询多个目标用户的粉丝数量。
     * </p>
     *
     * @param type      关注类型
     * @param targetIds 目标用户ID列表
     * @return 目标ID与粉丝数量的映射
     */
    Map<Long, Long> getFollowCountBatch(Integer type, Iterable<Long> targetIds);

    /**
 * 同步关注数据到数据库
 * <p>
 * 将Redis中的关注数据同步到数据库持久化。
 * 由定时任务调用。
 * </p>
 */
void syncFollowDataToDb();

/**
 * 从数据库预热关注缓存
 * <p>
 * 将数据库中的关注数据加载到Redis缓存，用于服务重启后的缓存预热。
 * 加载用户关注状态到Bitmap，粉丝计数到Hash。
 * </p>
 *
 * @param type     关注类型
 * @param targetId 目标ID（被关注者ID）
 */
void warmFollowCacheFromDb(Integer type, Long targetId);
}
