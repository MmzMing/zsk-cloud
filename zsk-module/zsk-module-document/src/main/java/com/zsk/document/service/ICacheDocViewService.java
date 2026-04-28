package com.zsk.document.service;

import java.util.Map;

/**
 * 缓存文档浏览服务接口
 * <p>
 * 提供基于Redis的浏览量统计功能，包括增加浏览量、查询浏览数、批量获取浏览数以及定时同步到数据库。
 * 浏览量不支持取消操作。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-25
 */
public interface ICacheDocViewService {

    /**
     * 增加浏览量
     * <p>
     * 用户浏览内容时调用，增加对应目标的浏览计数。
     * </p>
     *
     * @param type     浏览类型（1-笔记 2-视频）
     * @param targetId 目标ID
     * @param userId   用户ID（可为空，表示匿名浏览）
     * @return 是否成功（重复浏览返回false）
     */
    boolean view(Integer type, Long targetId, Long userId);

    /**
     * 获取来源浏览数
     * <p>
     * 获取指定目标内容的总浏览数量。
     * </p>
     *
     * @param type     浏览类型
     * @param targetId 目标ID
     * @return 浏览数量
     */
    Long getViewCount(Integer type, Long targetId);

    /**
     * 根据多个来源，批量获取浏览数
     * <p>
     * 批量查询多个目标内容的浏览数量。
     * </p>
     *
     * @param type      浏览类型
     * @param targetIds 目标ID列表
     * @return 目标ID与浏览数量的映射
     */
    Map<Long, Long> getViewCountBatch(Integer type, Iterable<Long> targetIds);

    /**
     * 同步浏览数据到数据库
     * <p>
     * 将Redis中的浏览量数据同步到数据库持久化。
     * 由定时任务调用。
     * </p>
     */
    void syncViewDataToDb();

    /**
     * 从数据库预热浏览量缓存
     * <p>
     * 将数据库中的浏览量数据加载到Redis缓存，用于服务重启后的缓存预热。
     * 加载浏览量计数到Hash。
     * </p>
     *
     * @param type     浏览类型
     * @param targetId 目标ID
     */
    void warmViewCacheFromDb(Integer type, Long targetId);
}
