package com.zsk.document.domain.vo;

import lombok.Data;

/**
 * 文档审核VO
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
public class DocNoteReviewVO {
    /** 文档ID */
    private String id;
    /** 文档标题 */
    private String title;
    /** 分类 */
    private String category;
    /** 状态 */
    private String status;
    /** 审核结果 */
    private String result;
    /** 创建时间 */
    private String createdAt;
}
