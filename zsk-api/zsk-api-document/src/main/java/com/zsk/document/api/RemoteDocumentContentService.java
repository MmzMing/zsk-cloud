package com.zsk.document.api;

import com.zsk.common.core.constant.CommonConstants;
import com.zsk.common.core.domain.R;
import com.zsk.document.api.domain.DocCommentApi;
import com.zsk.document.api.domain.DocNoteDetailApi;
import com.zsk.document.api.factory.RemoteDocumentContentFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 文档内容服务远程调用接口
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@FeignClient(contextId = "remoteDocumentContentService", value = "zsk-module-document", fallbackFactory = RemoteDocumentContentFallbackFactory.class)
public interface RemoteDocumentContentService {

    /**
     * 获取文档详情
     *
     * @param id 文档ID
     * @param source 请求来源
     * @return 文档详情
     */
    @GetMapping("/content/doc/detail/{id}")
    R<DocNoteDetailApi> getDetail(
        @PathVariable("id") Long id,
        @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);

    /**
     * 切换文档点赞状态
     *
     * @param id 文档ID
     * @param source 请求来源
     * @return 点赞结果
     */
    @PostMapping("/content/doc/like/{id}")
    R<Map<String, Object>> toggleLike(
        @PathVariable("id") Long id,
        @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);

    /**
     * 切换文档收藏状态
     *
     * @param id 文档ID
     * @param source 请求来源
     * @return 收藏结果
     */
    @PostMapping("/content/doc/favorite/{id}")
    R<Map<String, Object>> toggleFavorite(
        @PathVariable("id") Long id,
        @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);

    /**
     * 获取文档评论列表
     *
     * @param id 文档ID
     * @param page 页码
     * @param pageSize 每页数量
     * @param sort 排序方式
     * @param source 请求来源
     * @return 评论列表
     */
    @GetMapping("/content/doc/comments/{id}")
    R<Map<String, Object>> getComments(
        @PathVariable("id") Long id,
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(value = "sort", required = false) String sort,
        @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);

    /**
     * 发表文档评论
     *
     * @param params 评论参数
     * @param source 请求来源
     * @return 评论结果
     */
    @PostMapping("/content/doc/comment")
    R<DocCommentApi> postComment(
        @RequestBody Map<String, Object> params,
        @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);

    /**
     * 切换评论点赞状态
     *
     * @param commentId 评论ID
     * @param source 请求来源
     * @return 点赞结果
     */
    @PostMapping("/content/doc/comment/like/{commentId}")
    R<Map<String, Object>> toggleCommentLike(
        @PathVariable("commentId") Long commentId,
        @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);
}