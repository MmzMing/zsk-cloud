package com.zsk.document.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文档统计信息 API对象
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
public class DocStatisticsApi implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文档总数
     */
    private Long noteCount;

    /**
     * 视频总数
     */
    private Long videoCount;

    /**
     * 上周文档总数
     */
    private Long lastWeekNoteCount;

    /**
     * 上周视频总数
     */
    private Long lastWeekVideoCount;

    /**
     * 评论总数
     */
    private Long commentCount;

    /**
     * 上周评论总数
     */
    private Long lastWeekCommentCount;
}
