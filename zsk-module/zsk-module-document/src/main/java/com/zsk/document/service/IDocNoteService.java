package com.zsk.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocNote;

import java.util.List;

/**
 * 笔记Service接口
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
public interface IDocNoteService extends IService<DocNote> {

    /**
     * 获取草稿列表
     *
     * @param pageQuery 分页参数
     * @return 草稿列表
     */
    PageResult<DocNote> draftList(PageQuery pageQuery);

    /**
     * 批量更新状态
     *
     * @param ids    笔记ID列表
     * @param status 目标状态
     * @return 是否成功
     */
    boolean batchUpdateStatus(List<Long> ids, String status);

    /**
     * 批量迁移分类
     *
     * @param ids      笔记ID列表
     * @param category 目标分类
     * @return 是否成功
     */
    boolean batchMoveCategory(List<Long> ids, String category);

    /**
     * 切换置顶状态
     *
     * @param id 笔记ID
     * @return 是否成功
     */
    boolean togglePinned(Long id);

    /**
     * 切换推荐状态
     *
     * @param id 笔记ID
     * @return 是否成功
     */
    boolean toggleRecommended(Long id);
}
