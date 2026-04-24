package com.zsk.document.service;

import com.zsk.document.domain.vo.InteractionResultVo;

/**
 * 笔记交互查询服务接口
 * <p>
 * 独立查询笔记的交互数据（浏览量、点赞量、收藏量等）
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-25
 */
public interface IDocNoteInteractionService {

    /**
     * 获取笔记交互数据
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（可为空）
     * @return 交互数据
     */
    InteractionResultVo getNoteInteraction(Long noteId, Long userId);

    /**
     * 增加笔记浏览量
     *
     * @param noteId 笔记ID
     * @param userId 用户ID（可为空）
     */
    void incrementViewCount(Long noteId, Long userId);
}
