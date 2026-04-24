package com.zsk.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.document.domain.DocVideo;

import java.util.List;

/**
 * 视频Service接口
 *
 * @author wuhuaming
 * @date 2026-02-14
 */
public interface IDocVideoService extends IService<DocVideo> {

    /**
     * 发布草稿
     *
     * @param id 草稿ID
     * @return 是否成功
     */
    boolean publishDraft(Long id);

    /**
     * 批量更新视频状态
     *
     * @param ids    视频ID列表
     * @param status 目标状态
     * @return 是否成功
     */
    boolean batchUpdateStatus(List<Long> ids, Integer status);

    /**
     * 切换视频置顶状态
     *
     * @param id     视频ID
     * @param pinned 置顶状态（0-否 1-是）
     * @return 是否成功
     */
    boolean togglePinned(Long id, Integer pinned);

    /**
     * 切换视频推荐状态
     *
     * @param id          视频ID
     * @param recommended 推荐状态（0-否 1-是）
     * @return 是否成功
     */
    boolean toggleRecommended(Long id, Integer recommended);
}
