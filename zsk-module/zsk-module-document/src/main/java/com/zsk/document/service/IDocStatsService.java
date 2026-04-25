package com.zsk.document.service;

import com.zsk.document.domain.vo.AllStatsVo;
import com.zsk.document.domain.vo.UserStatsVo;

/**
 * 统计信息Service接口
 * <p>
 * 提供文档系统的统计信息查询服务，包括：
 * 1. 用户统计信息：点赞数、关注数、收藏数、评论数
 * 2. 内容统计信息：文章总数、视频总数、评论总数及周增长数据
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
public interface IDocStatsService {

    /**
     * 获取用户统计信息
     * <p>
     * 查询指定用户的交互统计数据，包括：
     * - 点赞总数（笔记点赞 + 视频点赞）
     * - 关注总数（用户关注 + 笔记作者关注 + 视频作者关注）
     * - 收藏总数（笔记收藏 + 视频收藏）
     * - 评论总数（用户发表的评论数）
     * </p>
     * <p>
     * 交互数据（点赞、关注、收藏）优先从 Redis 缓存获取，保证数据的实时性和高性能。
     * 评论数据从数据库查询。
     * </p>
     *
     * @param userId 用户ID，不能为空
     * @return 用户统计信息VO，包含各项统计数据
     */
    UserStatsVo getUserStats(Long userId);

    /**
     * 获取内容统计信息
     * <p>
     * 查询文档系统的全局内容统计数据，包括：
     * - 文档总数（未删除的笔记数量）
     * - 视频总数（未删除的视频数量）
     * - 评论总数（笔记评论 + 视频评论）
     * - 上周新增文档数
     * - 上周新增视频数
     * - 上周新增评论数
     * </p>
     * <p>
     * 统计数据均从数据库查询，统计范围为未删除的内容。
     * 上周时间范围定义为：当前时间往前推7天，从周一开始到周日结束。
     * </p>
     *
     * @return 内容统计信息VO，包含各项统计数据
     */
    AllStatsVo getContentStats();
}