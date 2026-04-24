package com.zsk.document.api.domain;

import lombok.Builder;
import lombok.Data;

/**
 * 用户统计信息 API 对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Builder
public class DocUserStatsApi {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 点赞总数
     */
    private Long likeCount;

    /**
     * 粉丝总数
     */
    private Long fanCount;

    /**
     * 收藏总数
     */
    private Long collectCount;

    /**
     * 评论总数
     */
    private Long commentCount;
}
