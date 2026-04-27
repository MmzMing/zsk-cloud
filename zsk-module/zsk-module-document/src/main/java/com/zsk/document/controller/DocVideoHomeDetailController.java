package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.document.domain.vo.*;
import com.zsk.document.enums.CacheDocViewTypeEnum;
import com.zsk.document.service.ICacheDocViewService;
import com.zsk.document.service.IDocVideoHomeDetailService;
import com.zsk.document.service.IDocVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 前台视频首页详情控制器
 * <p>
 * 提供视频详情查询、交互操作（点赞、收藏、关注）等功能。
 * 评论相关功能已解耦到 {@link DocVideoCommentController} 中，本控制器不再处理评论业务。
 * 所有业务逻辑已下沉到 Service 层，本层仅负责参数校验和结果封装。
 * </p>
 *
 * @author wuhuaming
 * @version 3.0
 * @date 2026-04-27
 */
@Tag(name = "前台视频首页详情")
@RestController
@RequestMapping("/docVideoHomeDetail")
@RequiredArgsConstructor
public class DocVideoHomeDetailController {

    /**
     * 视频服务
     */
    private final IDocVideoService videoService;

    /**
     * 视频首页详情服务
     */
    private final IDocVideoHomeDetailService videoHomeDetailService;

    /**
     * 缓存浏览服务
     */
    private final ICacheDocViewService cacheDocViewService;

    /**
     * 获取视频详情
     * <p>
     * 根据视频ID查询视频详情，并增加浏览量。
     * 如果用户已登录，会查询用户的点赞、收藏状态以及是否关注作者。
     * </p>
     *
     * @param id 视频ID
     * @return 视频详情
     */
    @Operation(summary = "获取视频详情")
    @GetMapping("/detail/{id}")
    public R<DocVideoHomeDetailVo> getDetail(@PathVariable("id") Long id) {
        // 查询视频基础信息
        DocVideoDetailVo video = videoService.getDetailWithFileUrl(id);
        if (video == null) {
            return R.fail("视频不存在");
        }

        // 获取当前用户ID并增加浏览量
        Long userId = getCurrentUserId();
        cacheDocViewService.view(CacheDocViewTypeEnum.VIDEO.getCode(), id, userId);

        // 构建视频详情VO
        DocVideoHomeDetailVo vo = new DocVideoHomeDetailVo();
        vo.setId(video.getId());
        vo.setTitle(video.getVideoTitle());
        vo.setDescription(video.getFileContent());

        // 设置视频和封面URL
        if (video.getVideoFile() != null) {
            if (video.getVideoFile().getVideo() != null) {
                vo.setVideoUrl(video.getVideoFile().getVideo().getFileUrl());
            }
            if (video.getVideoFile().getThumbnail() != null) {
                vo.setCoverUrl(video.getVideoFile().getThumbnail().getFileUrl());
            }
        }

        // 获取作者和统计信息
        DocVideoHomeDetailVo detail = videoHomeDetailService.getVideoDetail(id, userId);
        vo.setAuthor(detail.getAuthor());
        vo.setStats(detail.getStats());
        vo.setTags(detail.getTags());

        return R.ok(vo);
    }

    /**
     * 获取视频交互详情
     * <p>
     * 独立查询视频的交互统计数据，包括浏览量、点赞数、收藏数以及当前用户的交互状态。
     * </p>
     *
     * @param id 视频ID
     * @return 视频交互详情
     */
    @Operation(summary = "获取视频交互详情")
    @GetMapping("/interaction/{id}")
    public R<DocStatsInfoVo> getInteraction(@PathVariable("id") Long id) {
        // 验证视频是否存在
        if (videoService.getById(id) == null) {
            return R.fail("视频不存在");
        }

        // 获取当前用户ID并查询交互详情
        Long userId = getCurrentUserId();
        DocStatsInfoVo stats = videoHomeDetailService.getVideoInteraction(id, userId);
        return R.ok(stats);
    }

    /**
     * 切换视频点赞状态
     * <p>
     * 用户点赞或取消点赞视频。
     * </p>
     *
     * @param id 视频ID
     * @return 点赞操作结果
     */
    @Operation(summary = "切换视频点赞状态")
    @PostMapping("/like/{id}")
    public R<InteractionResultVo> toggleLike(@PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        InteractionResultVo result = videoHomeDetailService.toggleVideoLike(id, userId);
        return R.ok(result);
    }

    /**
     * 切换视频收藏状态
     * <p>
     * 用户收藏或取消收藏视频。
     * </p>
     *
     * @param id 视频ID
     * @return 收藏操作结果
     */
    @Operation(summary = "切换视频收藏状态")
    @PostMapping("/favorite/{id}")
    public R<InteractionResultVo> toggleFavorite(@PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        InteractionResultVo result = videoHomeDetailService.toggleVideoFavorite(id, userId);
        return R.ok(result);
    }

    /**
     * 切换关注作者状态
     * <p>
     * 用户关注或取消关注视频作者。
     * </p>
     *
     * @param authorId 作者ID
     * @return 关注操作结果
     */
    @Operation(summary = "切换关注作者状态")
    @PostMapping("/follow/{authorId}")
    public R<InteractionResultVo> toggleFollow(@PathVariable("authorId") Long authorId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        if (authorId.equals(userId)) {
            return R.fail("不能关注自己");
        }

        InteractionResultVo result = videoHomeDetailService.toggleFollowAuthor(authorId, userId);
        return R.ok(result);
    }

    /**
     * 获取当前用户ID
     * <p>
     * 尝试从安全工具类获取当前登录用户ID，如果获取失败则返回null。
     * </p>
     *
     * @return 当前用户ID，未登录或获取失败返回null
     */
    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
