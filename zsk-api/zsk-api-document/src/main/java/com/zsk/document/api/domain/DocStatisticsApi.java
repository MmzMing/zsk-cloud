package com.zsk.document.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文档统计信息 API对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
public class DocStatisticsApi implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 文档总数 */
    private Long noteCount;

    /** 视频总数 */
    private Long videoCount;

    /** 文档总浏览量 */
    private Long noteViewCount;

    /** 视频总浏览量 */
    private Long videoViewCount;

    /** 上周文档总数 */
    private Long lastWeekNoteCount;

    /** 上周视频总数 */
    private Long lastWeekVideoCount;

    /** 上周文档总浏览量 */
    private Long lastWeekNoteViewCount;

    /** 上周视频总浏览量 */
    private Long lastWeekVideoViewCount;
}
