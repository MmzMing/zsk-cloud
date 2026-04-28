package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.document.domain.dto.DocHomeVideoCommentPostDto;
import com.zsk.document.domain.vo.*;
import com.zsk.document.service.IDocHomeVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台首页视频详情控制器
 * <p>
 * 提供前台视频详情页的四大区域化接口和交互操作接口：
 * </p>
 * <p>
 * 区域一：视频元信息+详情 —— 获取视频基本信息和内容，优先增加浏览量
 * </p>
 * <p>
 * 区域二：点赞收藏+作者关注信息 —— 获取交互统计数据和作者关注状态
 * </p>
 * <p>
 * 区域三：评论区域 —— 分页获取二级结构评论列表，发表/回复评论使用同一接口
 * </p>
 * <p>
 * 区域四：视频合集 —— 获取视频所属的公开合集列表（仅查询）
 * </p>
 * <p>
 * 本控制器是前台聚合接口，所有业务逻辑通过 {@link IDocHomeVideoService} 聚合服务处理，
 * 不在其他Service中编写方法供本控制器调用，确保前后台逻辑隔离。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-29
 */
@Slf4j
@Tag(name = "前台首页视频详情")
@RestController
@RequestMapping("/docHomeVideo")
@RequiredArgsConstructor
public class DocHomeVideoController {

    /**
     * 前台首页视频聚合服务
     */
    private final IDocHomeVideoService docHomeVideoService;

    // ==================== 区域一：视频元信息+详情 ====================

    /**
     * 获取视频元信息+详情
     * <p>
     * 查询视频的基本信息和内容详情，仅返回前台展示所需字段。
     * 获取元信息时优先增加浏览量（Redis）。
     * 不包含审核状态、版本号等后台管理字段。
     * </p>
     *
     * @param id 视频ID
     * @return 视频详情
     */
    @Operation(summary = "获取视频元信息+详情")
    @GetMapping("/detail/{id}")
    public R<DocHomeVideoDetailVo> getDetail(@PathVariable("id") Long id) {
        log.info("获取视频元信息+详情请求, id={}", id);

        Long userId = getCurrentUserId();
        DocHomeVideoDetailVo vo = docHomeVideoService.getVideoDetail(id, userId);
        if (vo == null) {
            return R.fail("视频不存在");
        }

        log.info("获取视频元信息+详情成功, id={}", id);
        return R.ok(vo);
    }

    // ==================== 区域二：点赞收藏+作者关注信息 ====================

    /**
     * 获取视频交互信息（点赞收藏+作者关注）
     * <p>
     * 独立查询视频的交互统计数据和作者关注信息。
     * 浏览量、点赞数、收藏数从Redis缓存获取。
     * 当前用户的点赞、收藏、关注状态从Redis Bitmap获取。
     * </p>
     *
     * @param id 视频ID
     * @return 视频交互信息
     */
    @Operation(summary = "获取视频交互信息（点赞收藏+作者关注）")
    @GetMapping("/interaction/{id}")
    public R<DocHomeVideoInteractionVo> getInteraction(@PathVariable("id") Long id) {
        log.info("获取视频交互信息请求, id={}", id);

        Long userId = getCurrentUserId();
        DocHomeVideoInteractionVo vo = docHomeVideoService.getVideoInteraction(id, userId);
        if (vo == null) {
            return R.fail("视频不存在");
        }

        log.info("获取视频交互信息成功, id={}", id);
        return R.ok(vo);
    }

    /**
     * 切换视频点赞状态
     * <p>
     * 用户点赞或取消点赞视频，通过Redis缓存服务操作。
     * </p>
     *
     * @param id 视频ID
     * @return 点赞操作结果
     */
    @Operation(summary = "切换视频点赞状态")
    @PostMapping("/like/{id}")
    public R<DocHomeVideoInteractionResultVo> toggleLike(@PathVariable("id") Long id) {
        log.info("切换视频点赞状态请求, id={}", id);

        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        DocHomeVideoInteractionResultVo result = docHomeVideoService.toggleVideoLike(id, userId);

        log.info("切换视频点赞状态成功, id={}, status={}", id, result.isStatus());
        return R.ok(result);
    }

    /**
     * 切换视频收藏状态
     * <p>
     * 用户收藏或取消收藏视频，通过Redis缓存服务操作。
     * </p>
     *
     * @param id 视频ID
     * @return 收藏操作结果
     */
    @Operation(summary = "切换视频收藏状态")
    @PostMapping("/favorite/{id}")
    public R<DocHomeVideoInteractionResultVo> toggleFavorite(@PathVariable("id") Long id) {
        log.info("切换视频收藏状态请求, id={}", id);

        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        DocHomeVideoInteractionResultVo result = docHomeVideoService.toggleVideoFavorite(id, userId);

        log.info("切换视频收藏状态成功, id={}, status={}", id, result.isStatus());
        return R.ok(result);
    }

    /**
     * 切换关注作者状态
     * <p>
     * 用户关注或取消关注视频作者，通过Redis缓存服务操作。
     * </p>
     *
     * @param authorId 作者ID
     * @return 关注操作结果
     */
    @Operation(summary = "切换关注作者状态")
    @PostMapping("/follow/{authorId}")
    public R<DocHomeVideoInteractionResultVo> toggleFollow(@PathVariable("authorId") Long authorId) {
        log.info("切换关注作者状态请求, authorId={}", authorId);

        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        if (authorId.equals(userId)) {
            return R.fail("不能关注自己");
        }

        DocHomeVideoInteractionResultVo result = docHomeVideoService.toggleFollowAuthor(authorId, userId);

        log.info("切换关注作者状态成功, authorId={}, status={}", authorId, result.isStatus());
        return R.ok(result);
    }

    // ==================== 区域三：评论区域 ====================

    /**
     * 获取视频评论列表（分页，二级结构）
     * <p>
     * 查询视频的根评论列表，每个根评论包含其下的所有回复。
     * 采用B站式二级评论结构：所有回复统一挂在根评论下，不存在层级嵌套。
     * 支持热门/最新排序。
     * </p>
     *
     * @param videoId   视频ID
     * @param pageQuery 分页查询参数
     * @param sort      排序方式（hot-按点赞数降序，new-按创建时间降序）
     * @return 评论分页列表
     */
    @Operation(summary = "获取视频评论列表（分页，二级结构）")
    @GetMapping("/comments/{videoId}")
    public R<PageResult<DocHomeVideoCommentVo>> getComments(
            @PathVariable("videoId") Long videoId,
            PageQuery pageQuery,
            @RequestParam(value = "sort", required = false) String sort) {
        log.info("获取视频评论列表请求, videoId={}, pageQuery={}, sort={}", videoId, pageQuery, sort);

        Long userId = getCurrentUserId();
        PageResult<DocHomeVideoCommentVo> pageResult = docHomeVideoService.getVideoComments(videoId, pageQuery, sort, userId);

        log.info("获取视频评论列表成功, videoId={}, total={}", videoId, pageResult.getTotal());
        return R.ok(pageResult);
    }

    /**
     * 发表/回复视频评论
     * <p>
     * 发表根评论和回复评论使用同一个接口。
     * 采用B站式评论结构：parentId记录根评论ID，replyToId记录被回复用户ID。
     * </p>
     *
     * @param dto 评论请求参数
     * @return 评论结果
     */
    @Operation(summary = "发表/回复视频评论")
    @PostMapping("/comment")
    public R<DocHomeVideoCommentVo> postComment(@RequestBody DocHomeVideoCommentPostDto dto) {
        log.info("发表/回复视频评论请求, videoId={}", dto.getVideoId());

        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        DocHomeVideoCommentVo commentVo = docHomeVideoService.postComment(dto, userId);

        log.info("发表/回复视频评论成功, commentId={}", commentVo.getId());
        return R.ok(commentVo);
    }

    /**
     * 切换评论点赞状态
     * <p>
     * 用户点赞或取消点赞视频评论，通过Redis缓存服务操作。
     * </p>
     *
     * @param commentId 评论ID
     * @return 点赞操作结果
     */
    @Operation(summary = "切换评论点赞状态")
    @PostMapping("/comment/like/{commentId}")
    public R<DocHomeVideoInteractionResultVo> toggleCommentLike(@PathVariable("commentId") Long commentId) {
        log.info("切换评论点赞状态请求, commentId={}", commentId);

        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        DocHomeVideoInteractionResultVo result = docHomeVideoService.toggleCommentLike(commentId, userId);

        log.info("切换评论点赞状态成功, commentId={}, status={}", commentId, result.isStatus());
        return R.ok(result);
    }

    // ==================== 区域四：视频合集 ====================

    /**
     * 获取视频所属的公开合集列表
     * <p>
     * 查询包含该视频的所有公开合集，每个合集包含其视频列表。
     * 仅返回公开状态的合集，私密合集不对外展示。
     * </p>
     *
     * @param videoId 视频ID
     * @return 公开合集列表
     */
    @Operation(summary = "获取视频所属公开合集列表")
    @GetMapping("/collections/{videoId}")
    public R<List<DocHomeVideoCollectionVo>> getCollections(@PathVariable("videoId") Long videoId) {
        log.info("获取视频所属公开合集列表请求, videoId={}", videoId);

        List<DocHomeVideoCollectionVo> collections = docHomeVideoService.getVideoCollections(videoId);

        log.info("获取视频所属公开合集列表成功, videoId={}, count={}", videoId, collections.size());
        return R.ok(collections);
    }

    // ==================== 私有方法 ====================

    /**
     * 获取当前用户ID
     * <p>
     * 尝试从安全工具类获取当前登录用户ID，如果获取失败则返回null。
     * 前台接口允许未登录访问（如浏览视频详情），因此获取失败不抛异常。
     * </p>
     *
     * @return 当前用户ID，未登录或获取失败返回null
     */
    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            log.warn("获取当前用户ID失败", e);
            return null;
        }
    }
}
