package com.zsk.document.domain.vo;

import lombok.Data;

/**
 * 文档标签VO
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
public class DocTagVO {
    /** 标签显示文本 */
    private String label;
    /** 标签值 */
    private String value;
}
