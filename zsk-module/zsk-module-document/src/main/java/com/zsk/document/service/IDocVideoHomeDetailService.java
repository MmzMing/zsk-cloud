package com.zsk.document.service;

import com.zsk.document.domain.vo.DocStatsInfoVo;
import com.zsk.document.domain.vo.DocVideoHomeDetailVo;
import com.zsk.document.domain.vo.InteractionResultVo;

/**
 * 视频首页详情服务接口
 * <p>
 * 提供视频详情查询、交互操作（点赞、收藏、关注）等功能。
 * 评论相关功能已解耦到 {@link IDocVideoCommentService} 中，本接口不再定义评论相关方法。
 * 所有交互数据（浏览量、点赞数、收藏数）均通过 Redis 缓存服务获取。
 * </p>
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-27
 */
public interface IDocVideoHomeDetailService {

    /**
     * 获取视频详情
     *
     * @param id     视频ID
     * @param userId 当前用户ID（可为null）
     * @return 视频详情
     */
    DocVideoHomeDetailVo getVideoDetail(Long id, Long userId);

    /**
     * 获取视频交互详情
     *
     * @param id     视频ID
     * @param userId 当前用户ID（可为null）
     * @return 视频交互详情
     */
    DocStatsInfoVo getVideoInteraction(Long id, Long userId);

    /**
     * 切换视频点赞状态
     *
     * @param id     视频ID
     * @param userId 当前用户ID
     * @return 点赞操作结果
     */
    InteractionResultVo toggleVideoLike(Long id, Long userId);

    /**
     * 切换视频收藏状态
     *
     * @param id     视频ID
     * @param userId 当前用户ID
     * @return 收藏操作结果
     */
    InteractionResultVo toggleVideoFavorite(Long id, Long userId);

    /**
     * 切换关注作者状态
     *
     * @param authorId 作者ID
     * @param userId   当前用户ID
     * @return 关注操作结果
     */
    InteractionResultVo toggleFollowAuthor(Long authorId, Long userId);
}
