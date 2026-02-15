package com.zsk.document.service;

import com.zsk.document.domain.vo.VideoCategoryVO;
import com.zsk.document.domain.vo.VideoTagVO;

import java.util.List;

/**
 * 视频分类标签缓存Service接口
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public interface IVideoCategoryCacheService {

    /**
     * 从缓存获取视频分类列表
     *
     * @return 分类列表
     */
    List<VideoCategoryVO> getCategoryListFromCache();

    /**
     * 从缓存获取视频标签列表
     *
     * @return 标签列表
     */
    List<VideoTagVO> getTagListFromCache();

    /**
     * 刷新分类缓存
     */
    void refreshCategoryCache();

    /**
     * 刷新标签缓存
     */
    void refreshTagCache();
}
