package com.zsk.document.api.factory;

import com.zsk.common.core.domain.R;
import com.zsk.document.api.RemoteDocAllContentService;
import com.zsk.document.api.domain.DocStatisticsApi;
import com.zsk.document.api.domain.DocUserStatsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 用户统计信息服务降级处理
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Component
public class RemoteDocAllContentFallbackFactory implements FallbackFactory<RemoteDocAllContentService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteDocAllContentFallbackFactory.class);

    @Override
    public RemoteDocAllContentService create(Throwable throwable) {
        log.error("用户统计信息服务调用失败:{}", throwable.getMessage());
        return new RemoteDocAllContentService() {
            @Override
            public R<DocUserStatsApi> getUserStats(String source) {
                return R.fail("获取用户统计信息失败:" + throwable.getMessage());
            }

            @Override
            public R<DocStatisticsApi> getContentStats(String source) {
                return R.fail("获取内容统计信息失败:" + throwable.getMessage());
            }
        };
    }
}
