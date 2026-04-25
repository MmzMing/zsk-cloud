package com.zsk.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocVideo;
import com.zsk.document.domain.vo.DocVideoListVo;

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

    /**
     * 查询视频列表（带文件URL）
     *
     * @param docVideo 查询条件
     * @return 视频列表（包含文件信息）
     */
    List<DocVideoListVo> listWithFileUrl(DocVideo docVideo);

    /**
     * 分页查询视频列表（带文件URL）
     *
     * @param docVideo  查询条件
     * @param pageQuery 分页参数
     * @return 分页结果（包含文件信息）
     */
    PageResult<DocVideoListVo> pageWithFileUrl(DocVideo docVideo, PageQuery pageQuery);

    /**
     * 获取视频详细信息（带文件URL）
     *
     * @param id 视频ID
     * @return 视频详情（包含文件信息）
     */
    DocVideoListVo getByIdWithFileUrl(Long id);

    /**
     * 获取草稿列表（带文件URL）
     *
     * @param pageQuery 分页参数
     * @return 草稿列表（包含文件信息）
     */
    PageResult<DocVideoListVo> draftListWithFileUrl(PageQuery pageQuery);
}
