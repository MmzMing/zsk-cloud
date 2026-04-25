package com.zsk.document.service;

import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.vo.*;

/**
 * 笔记首页详情服务接口
 * <p>
 * 提供笔记详情查询、交互操作（点赞、收藏、关注）、评论管理等功能。
 * 所有交互数据（浏览量、点赞数、收藏数）均通过 Redis 缓存服务获取，不再依赖主表字段。
 * </p>
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-25
 */
public interface IDocNoteHomeDetailService {

    /**
     * 获取笔记详情
     * <p>
     * 根据笔记ID查询笔记详情，并增加浏览量。
     * 如果用户已登录，会查询用户的点赞、收藏状态以及是否关注作者。
     * 所有统计数据（浏览量、点赞数、收藏数、评论数）均从 Redis 缓存获取。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（可为null，未登录时传null）
     * @return 笔记详情VO，如果笔记不存在返回null
     */
    DocNoteHomeDetailVo getNoteDetail(Long noteId, Long userId);

    /**
     * 获取笔记交互详情
     * <p>
     * 独立查询笔记的交互统计数据，包括浏览量、点赞数、收藏数、评论数以及当前用户的交互状态。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（可为null，未登录时传null）
     * @return 笔记交互详情VO，如果笔记不存在返回null
     */
    DocStatsInfoVo getNoteInteraction(Long noteId, Long userId);

    /**
     * 切换笔记点赞状态
     * <p>
     * 用户点赞或取消点赞笔记。先查询当前点赞状态，然后执行相反操作。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（不能为空）
     * @return 点赞操作结果
     */
    InteractionResultVo toggleNoteLike(Long noteId, Long userId);

    /**
     * 切换笔记收藏状态
     * <p>
     * 用户收藏或取消收藏笔记。先查询当前收藏状态，然后执行相反操作。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（不能为空）
     * @return 收藏操作结果
     */
    InteractionResultVo toggleNoteFavorite(Long noteId, Long userId);

    /**
     * 切换关注作者状态
     * <p>
     * 用户关注或取消关注笔记作者。
     * </p>
     *
     * @param authorId 作者ID
     * @param userId   当前用户ID（不能为空）
     * @return 关注操作结果
     */
    InteractionResultVo toggleFollowAuthor(Long authorId, Long userId);

    /**
     * 获取笔记评论列表
     * <p>
     * 查询笔记的评论列表，支持热门排序和最新排序。
     * </p>
     *
     * @param noteId    笔记ID
     * @param pageQuery 分页查询参数
     * @param sort      排序方式（hot/new）
     * @param userId    当前用户ID（可为null，未登录时传null）
     * @return 评论分页列表
     */
    PageResult<DocCommentVo> getNoteComments(Long noteId, PageQuery pageQuery, String sort, Long userId);

    /**
     * 发表笔记评论
     * <p>
     * 用户发表笔记评论，支持回复其他评论。评论成功后返回构建好的评论VO。
     * </p>
     *
     * @param noteId   笔记ID
     * @param content  评论内容
     * @param parentId 父评论ID（可为null，顶级评论时传null）
     * @param userId   当前用户ID（不能为空）
     * @return 评论VO
     */
    DocCommentVo postComment(Long noteId, String content, Long parentId, Long userId);

    /**
     * 切换评论点赞状态
     * <p>
     * 用户点赞或取消点赞笔记评论。
     * </p>
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID（不能为空）
     * @return 点赞操作结果
     */
    InteractionResultVo toggleCommentLike(Long commentId, Long userId);

    /**
     * 构建笔记首页详情VO
     * <p>
     * 将笔记实体转换为笔记首页详情VO，包含基本信息。
     * </p>
     *
     * @param noteId 笔记ID
     * @return 笔记首页详情VO，如果笔记不存在返回null
     */
    DocNoteHomeDetailVo buildNoteHomeDetailVo(Long noteId);

    /**
     * 构建笔记统计信息VO
     * <p>
     * 从 Redis 缓存服务获取笔记的浏览量、点赞数、收藏数、评论数，
     * 以及当前用户的点赞、收藏状态。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（可为null）
     * @return 笔记统计信息VO
     */
    DocStatsInfoVo buildNoteStatsInfo(Long noteId, Long userId);

    /**
     * 构建笔记作者信息VO
     * <p>
     * 根据笔记实体构建作者信息，并查询当前用户是否关注该作者。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（可为null）
     * @return 笔记作者信息VO，如果笔记不存在返回null
     */
    DocUserVo buildNoteAuthorInfo(Long noteId, Long userId);

    /**
     * 构建评论VO
     * <p>
     * 将评论实体转换为评论VO，包含作者信息、点赞数、点赞状态等。
     * </p>
     *
     * @param commentId     评论ID
     * @param currentUserId 当前登录用户ID（可为null）
     * @return 评论VO，如果评论不存在返回null
     */
    DocCommentVo buildCommentVo(Long commentId, Long currentUserId);
}