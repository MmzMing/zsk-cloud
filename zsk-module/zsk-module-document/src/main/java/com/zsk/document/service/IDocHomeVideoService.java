package com.zsk.document.service;

import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.dto.DocHomeVideoCommentPostDto;
import com.zsk.document.domain.vo.*;

import java.util.List;

/**
 * 前台首页视频聚合服务接口
 * <p>
 * 前台首页视频详情的聚合服务，负责组装视频元信息、交互数据、评论数据、合集数据。
 * 本服务是前台聚合层，仅调用其他已有Service方法进行数据拼装，
 * 不在其他Service中编写方法供本服务调用，确保前后台逻辑隔离。
 * </p>
 * <p>
 * 四大区域化接口：
 * 1. 视频元信息+详情（{@link #getVideoDetail}）
 * 2. 点赞收藏+作者关注信息（{@link #getVideoInteraction}）
 * 3. 评论区域（{@link #getVideoComments}）
 * 4. 视频合集（{@link #getVideoCollections}）
 * </p>
 * <p>
 * 所有交互数据（浏览量、点赞数、收藏数、粉丝数）均通过 Redis 缓存服务获取。
 * 获取元信息时优先增加浏览量。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-29
 */
public interface IDocHomeVideoService {

    /**
     * 获取视频元信息+详情
     * <p>
     * 查询视频的基本信息和内容详情，仅返回前台展示所需字段。
     * 获取元信息时优先增加浏览量（Redis）。
     * 不包含审核状态、版本号等后台管理字段。
     * </p>
     *
     * @param videoId 视频ID
     * @param userId  当前用户ID（可为null，未登录时传null）
     * @return 视频详情VO，如果视频不存在返回null
     */
    DocHomeVideoDetailVo getVideoDetail(Long videoId, Long userId);

    /**
     * 获取视频交互信息（点赞收藏+作者关注）
     * <p>
     * 独立查询视频的交互统计数据和作者关注信息。
     * 浏览量、点赞数、收藏数从Redis缓存获取。
     * 当前用户的点赞、收藏、关注状态从Redis Bitmap获取。
     * 作者粉丝数从Redis缓存获取。
     * </p>
     *
     * @param videoId 视频ID
     * @param userId  当前用户ID（可为null，未登录时传null）
     * @return 视频交互信息VO，如果视频不存在返回null
     */
    DocHomeVideoInteractionVo getVideoInteraction(Long videoId, Long userId);

    /**
     * 获取视频评论列表（分页，二级结构）
     * <p>
     * 查询视频的根评论列表，每个根评论包含其下的所有回复。
     * 采用B站式二级评论结构：所有回复统一挂在根评论下，不存在层级嵌套。
     * 支持热门/最新排序，评论点赞数从Redis缓存获取。
     * </p>
     *
     * @param videoId   视频ID
     * @param pageQuery 分页查询参数
     * @param sort      排序方式（hot-按点赞数降序，new-按创建时间降序）
     * @param userId    当前用户ID（可为null，未登录时传null）
     * @return 评论分页结果
     */
    PageResult<DocHomeVideoCommentVo> getVideoComments(Long videoId, PageQuery pageQuery, String sort, Long userId);

    /**
     * 获取视频所属的公开合集列表
     * <p>
     * 查询包含该视频的所有公开合集，每个合集包含其视频列表。
     * 仅返回公开状态的合集，私密合集不对外展示。
     * 合集数据从合集Service获取，视频列表仅包含前台展示字段。
     * </p>
     *
     * @param videoId 视频ID
     * @return 公开合集列表
     */
    List<DocHomeVideoCollectionVo> getVideoCollections(Long videoId);

    /**
     * 切换视频点赞状态
     * <p>
     * 用户点赞或取消点赞视频，通过Redis缓存服务操作。
     * </p>
     *
     * @param videoId 视频ID
     * @param userId  当前用户ID（不能为空）
     * @return 点赞操作结果
     */
    DocHomeVideoInteractionResultVo toggleVideoLike(Long videoId, Long userId);

    /**
     * 切换视频收藏状态
     * <p>
     * 用户收藏或取消收藏视频，通过Redis缓存服务操作。
     * </p>
     *
     * @param videoId 视频ID
     * @param userId  当前用户ID（不能为空）
     * @return 收藏操作结果
     */
    DocHomeVideoInteractionResultVo toggleVideoFavorite(Long videoId, Long userId);

    /**
     * 切换关注作者状态
     * <p>
     * 用户关注或取消关注视频作者，通过Redis缓存服务操作。
     * </p>
     *
     * @param authorId 作者ID
     * @param userId   当前用户ID（不能为空）
     * @return 关注操作结果
     */
    DocHomeVideoInteractionResultVo toggleFollowAuthor(Long authorId, Long userId);

    /**
     * 发表/回复视频评论
     * <p>
     * 发表根评论和回复评论使用同一个接口。
     * 采用B站式评论结构：parentId记录根评论ID，replyToId记录被回复用户ID。
     * </p>
     *
     * @param dto    评论请求DTO
     * @param userId 当前用户ID（不能为空）
     * @return 评论VO
     */
    DocHomeVideoCommentVo postComment(DocHomeVideoCommentPostDto dto, Long userId);

    /**
     * 切换评论点赞状态
     * <p>
     * 用户点赞或取消点赞视频评论，通过Redis缓存服务操作。
     * </p>
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID（不能为空）
     * @return 点赞操作结果
     */
    DocHomeVideoInteractionResultVo toggleCommentLike(Long commentId, Long userId);
}
