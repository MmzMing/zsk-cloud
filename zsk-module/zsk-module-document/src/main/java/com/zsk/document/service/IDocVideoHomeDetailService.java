package com.zsk.document.service;

import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.vo.DocVideoCommentVo;
import com.zsk.document.domain.vo.DocVideoHomeDetailStatsInfoVo;
import com.zsk.document.domain.vo.DocVideoHomeDetailVo;
import com.zsk.document.domain.vo.InteractionResultVo;

/**
 * 视频首页详情服务接口
 * <p>
 * 提供视频详情查询、交互操作（点赞、收藏、关注）、评论管理等功能。
 * 所有交互数据（浏览量、点赞数、收藏数）均通过 Redis 缓存服务获取。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-25
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
    DocVideoHomeDetailStatsInfoVo getVideoInteraction(Long id, Long userId);

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

    /**
     * 获取视频评论列表
     *
     * @param id        视频ID
     * @param pageQuery 分页查询参数
     * @param sort      排序方式（hot/new）
     * @param userId    当前用户ID（可为null）
     * @return 评论分页列表
     */
    PageResult<DocVideoCommentVo> getVideoComments(Long id, PageQuery pageQuery, String sort, Long userId);

    /**
     * 发表视频评论
     *
     * @param videoId  视频ID
     * @param content  评论内容
     * @param parentId 父评论ID（可为null）
     * @param userId   当前用户ID
     * @return 评论结果
     */
    DocVideoCommentVo postComment(Long videoId, String content, Long parentId, Long userId);

    /**
     * 切换评论点赞状态
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID
     * @return 点赞操作结果
     */
    InteractionResultVo toggleCommentLike(Long commentId, Long userId);
}