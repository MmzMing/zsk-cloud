package com.zsk.document.api;

import com.zsk.common.core.constant.CommonConstants;
import com.zsk.common.core.domain.R;
import com.zsk.document.api.domain.DocStatisticsApi;
import com.zsk.document.api.domain.DocUserStatsApi;
import com.zsk.document.api.factory.RemoteDocAllContentFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 用户统计信息服务远程调用接口
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@FeignClient(contextId = "remoteDocAllContentService", value = "zsk-module-document", fallbackFactory = RemoteDocAllContentFallbackFactory.class)
public interface RemoteDocAllContentService {

    /**
     * 获取用户统计信息（点赞、关注、收藏总数）
     *
     * @param source 请求来源
     * @return 用户统计信息
     */
    @GetMapping("/docAllContent/user/stats")
    R<DocUserStatsApi> getUserStats(@RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);

    /**
     * 获取内容统计信息（文档总数、视频总数、评论总数）
     *
     * @param source 请求来源
     * @return 内容统计信息
     */
    @GetMapping("/docAllContent/content/stats")
    R<DocStatisticsApi> getContentStats(@RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);
}
