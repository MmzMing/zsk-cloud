package com.zsk.document.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 文档分类VO
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
public class DocCategoryVO {
    /** 分类ID */
    private String id;
    /** 分类名称 */
    private String name;
    /** 子分类 */
    private List<DocCategoryVO> children;
}
