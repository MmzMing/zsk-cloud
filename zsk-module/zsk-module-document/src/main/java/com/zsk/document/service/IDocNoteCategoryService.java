package com.zsk.document.service;

import com.zsk.document.domain.vo.DocCategoryVO;
import com.zsk.document.domain.vo.DocTagVO;

import java.util.List;

/**
 * 文档分类标签Service接口
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
public interface IDocNoteCategoryService {

    /**
     * 获取文档分类列表
     *
     * @return 分类列表
     */
    List<DocCategoryVO> getCategoryList();

    /**
     * 获取文档标签列表
     *
     * @return 标签列表
     */
    List<DocTagVO> getTagList();
}
