package com.zsk.document.service.impl;

import com.zsk.document.domain.vo.InteractionResultVo;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.enums.CacheDocViewTypeEnum;
import com.zsk.document.service.ICacheDocCollectService;
import com.zsk.document.service.ICacheDocLikeService;
import com.zsk.document.service.ICacheDocViewService;
import com.zsk.document.service.IDocVideoInteractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 视频交互查询服务实现类
 * <p>
 * 独立查询视频的交互数据，包括浏览量、点赞量、收藏量以及当前用户的交互状态。
 * 所有数据来源于Redis缓存服务，如缓存未命中则从数据库加载。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocVideoInteractionServiceImpl implements IDocVideoInteractionService {

    /**
     * 缓存浏览服务
     */
    private final ICacheDocViewService cacheDocViewService;

    /**
     * 缓存点赞服务
     */
    private final ICacheDocLikeService cacheDocLikeService;

    /**
     * 缓存收藏服务
     */
    private final ICacheDocCollectService cacheDocCollectService;

    /**
     * 获取视频交互数据
     * <p>
     * 查询指定视频的交互数据，包括：
     * - 浏览量：总浏览次数
     * - 点赞量：总点赞次数
     * - 收藏量：总收藏次数
     * - 用户交互状态：当前用户是否已点赞/收藏
     * </p>
     *
     * @param videoId 视频ID
     * @param userId  当前用户ID（可选，为空时不返回用户交互状态）
     * @return 交互数据封装对象
     */
    @Override
    public InteractionResultVo getVideoInteraction(Long videoId, Long userId) {
        // 1. 查询各项统计数据（从Redis缓存）
        Long viewCount = cacheDocViewService.getViewCount(CacheDocViewTypeEnum.VIDEO.getCode(), videoId);
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.VIDEO.getCode(), videoId);
        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.VIDEO.getCode(), videoId);

        // 2. 如果用户已登录，查询用户的交互状态
        Boolean hasLiked = null;
        Boolean hasCollected = null;
        if (userId != null) {
            hasLiked = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.VIDEO.getCode(), videoId, userId);
            hasCollected = cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.VIDEO.getCode(), videoId, userId);
        }

        // 3. 构建并返回交互数据
        return InteractionResultVo.builder()
            .success(true)
            .viewCount(viewCount)
            .likeCount(likeCount)
            .collectCount(collectCount)
            .hasLiked(hasLiked)
            .hasCollected(hasCollected)
            .build();
    }

    /**
     * 增加视频浏览量
     * <p>
     * 用户浏览视频时调用，增加对应视频的浏览计数。
     * 浏览量先写入Redis缓存，后由定时任务同步到数据库。
     * </p>
     *
     * @param videoId 视频ID
     * @param userId  用户ID（可选，用于防止同一用户短时间内重复计数）
     */
    @Override
    public void incrementViewCount(Long videoId, Long userId) {
        // 调用缓存浏览服务增加浏览量
        cacheDocViewService.view(CacheDocViewTypeEnum.VIDEO.getCode(), videoId, userId);
    }
}
