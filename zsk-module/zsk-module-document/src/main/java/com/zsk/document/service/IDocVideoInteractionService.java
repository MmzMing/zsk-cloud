package com.zsk.document.service;

import com.zsk.document.domain.vo.InteractionResultVo;

/**
 * 视频交互查询服务接口
 * <p>
 * 独立查询视频的交互数据（浏览量、点赞量、收藏量等）
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-25
 */
public interface IDocVideoInteractionService {

    /**
     * 获取视频交互数据
     *
     * @param videoId 视频ID
     * @param userId  当前用户ID（可为空）
     * @return 交互数据
     */
    InteractionResultVo getVideoInteraction(Long videoId, Long userId);

    /**
     * 增加视频浏览量
     *
     * @param videoId 视频ID
     * @param userId  用户ID（可为空）
     */
    void incrementViewCount(Long videoId, Long userId);
}
