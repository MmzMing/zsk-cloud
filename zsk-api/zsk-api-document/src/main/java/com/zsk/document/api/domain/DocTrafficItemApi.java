package com.zsk.document.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 流量统计项 API对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocTrafficItemApi implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 类型（文档/视频） */
    private String type;

    /** 日期/周期标签 */
    private String date;

    /** 数量 */
    private Long value;
}
