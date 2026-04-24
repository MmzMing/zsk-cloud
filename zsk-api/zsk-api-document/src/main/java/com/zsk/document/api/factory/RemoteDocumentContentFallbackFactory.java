package com.zsk.document.api.factory;

import com.zsk.common.core.domain.R;
import com.zsk.document.api.RemoteDocumentContentService;
import com.zsk.document.api.domain.DocCommentApi;
import com.zsk.document.api.domain.DocNoteDetailApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 文档内容服务降级处理
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Component
public class RemoteDocumentContentFallbackFactory implements FallbackFactory<RemoteDocumentContentService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteDocumentContentFallbackFactory.class);

    @Override
    public RemoteDocumentContentService create(Throwable throwable) {
        log.error("文档内容服务调用失败:{}", throwable.getMessage());
        return new RemoteDocumentContentService() {
            @Override
            public R<DocNoteDetailApi> getDetail(Long id, String source) {
                return R.fail("获取文档详情失败:" + throwable.getMessage());
            }

            @Override
            public R<Map<String, Object>> toggleLike(Long id, String source) {
                return R.fail("切换点赞状态失败:" + throwable.getMessage());
            }

            @Override
            public R<Map<String, Object>> toggleFavorite(Long id, String source) {
                return R.fail("切换收藏状态失败:" + throwable.getMessage());
            }

            @Override
            public R<Map<String, Object>> getComments(Long id, Integer page, Integer pageSize, String sort, String source) {
                return R.fail("获取评论列表失败:" + throwable.getMessage());
            }

            @Override
            public R<DocCommentApi> postComment(Map<String, Object> params, String source) {
                return R.fail("发表评论失败:" + throwable.getMessage());
            }

            @Override
            public R<Map<String, Object>> toggleCommentLike(Long commentId, String source) {
                return R.fail("切换评论点赞状态失败:" + throwable.getMessage());
            }
        };
    }
}