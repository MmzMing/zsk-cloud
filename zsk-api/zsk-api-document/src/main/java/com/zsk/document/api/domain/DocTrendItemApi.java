package com.zsk.document.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 趋势数据项 API对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocTrendItemApi implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 日期/周期标签 */
    private String date;

    /** 访问量 */
    private Long value;
}
