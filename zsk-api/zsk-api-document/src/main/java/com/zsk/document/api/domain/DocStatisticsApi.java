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
    private Long docCount;

    /**
     * 视频总数
     */
    private Long videoCount;

    /**
     * 评论总数
     */
    private Long commentCount;

    /**
     * 上周新增文档数
     */
    private Long lastWeekDocCount;

    /**
     * 上周新增视频数
     */
    private Long lastWeekVideoCount;

    /**
     * 上周新增评论数
     */
    private Long lastWeekCommentCount;
}
