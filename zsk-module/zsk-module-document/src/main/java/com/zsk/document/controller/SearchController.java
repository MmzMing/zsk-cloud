package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocVideoDetail;
import com.zsk.document.domain.dto.SearchRequestDto;
import com.zsk.document.domain.vo.SearchResultVo;
import com.zsk.document.service.IDocNoteService;
import com.zsk.document.service.IDocVideoDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 搜索 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "全局搜索")
@RestController
@RequestMapping("/content/search")
@RequiredArgsConstructor
public class SearchController {

    private final IDocVideoDetailService videoService;
    private final IDocNoteService noteService;

    /**
     * 全站搜索
     *
     * @param searchRequest 搜索请求参数
     * @param pageQuery 分页查询参数
     * @return 搜索结果
     */
    @Operation(summary = "全站搜索")
    @GetMapping("/all")
    public R<PageResult<SearchResultVo>> searchAll(
        SearchRequestDto searchRequest,
        PageQuery pageQuery) {

        List<SearchResultVo> allResults = new ArrayList<>();
        String keyword = searchRequest.getKeyword();
        String type = searchRequest.getType();
        String sort = searchRequest.getSort();
        String category = searchRequest.getCategory();

        /** 搜索视频 */
        if ("all".equals(type) || "video".equals(type)) {
            List<SearchResultVo> videoResults = searchVideos(keyword, category, sort);
            allResults.addAll(videoResults);
        }

        /** 搜索文档 */
        if ("all".equals(type) || "document".equals(type)) {
            List<SearchResultVo> docResults = searchDocuments(keyword, category, sort);
            allResults.addAll(docResults);
        }

        /** 排序 */
        sortResults(allResults, sort);

        /** 分页 */
        long total = allResults.size();
        int fromIndex = (int) ((pageQuery.getPageNum() - 1) * pageQuery.getPageSize());
        int toIndex = (int) Math.min(fromIndex + pageQuery.getPageSize(), total);
        List<SearchResultVo> pageResults = fromIndex < total 
            ? allResults.subList(fromIndex, toIndex) 
            : new ArrayList<>();

        /** 构建分页结果 */
        PageResult<SearchResultVo> pageResult = PageResult.of(
            pageResults, 
            total, 
            pageQuery.getPageNum(), 
            pageQuery.getPageSize()
        );
        
        return R.ok(pageResult);
    }

    /**
     * 搜索视频
     *
     * @param keyword 搜索关键字
     * @param category 分类筛选
     * @param sort 排序方式
     * @return 视频搜索结果列表
     */
    private List<SearchResultVo> searchVideos(String keyword, String category, String sort) {
        LambdaQueryWrapper<DocVideoDetail> wrapper = new LambdaQueryWrapper<DocVideoDetail>()
            .eq(DocVideoDetail::getDeleted, 0)
            .eq(DocVideoDetail::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(DocVideoDetail::getVideoTitle, keyword)
                .or()
                .like(DocVideoDetail::getFileContent, keyword)
            );
        }

        if (StringUtils.hasText(category)) {
            wrapper.eq(DocVideoDetail::getBroadCode, category);
        }

        List<DocVideoDetail> videos = videoService.list(wrapper);

        List<SearchResultVo> results = new ArrayList<>();
        for (DocVideoDetail video : videos) {
            SearchResultVo vo = new SearchResultVo();
            vo.setId(String.valueOf(video.getId()));
            vo.setType("video");
            vo.setTitle(video.getVideoTitle());
            vo.setDescription(video.getFileContent() != null ? video.getFileContent() : "");
            vo.setCategory(video.getBroadCode());
            vo.setThumbnail(video.getCoverUrl());
            vo.setPlayCount(video.getViewCount());
            vo.setCommentCount(video.getCommentCount());
            vo.setFavoriteCount(video.getCollectCount());
            vo.setAuthorId(String.valueOf(video.getUserId()));
            vo.setAuthor("作者" + video.getUserId());
            if (video.getTags() != null && !video.getTags().isEmpty()) {
                vo.setTags(Arrays.asList(video.getTags().split(",")));
            } else {
                vo.setTags(new ArrayList<>());
            }
            results.add(vo);
        }

        return results;
    }

    /**
     * 搜索文档
     *
     * @param keyword 搜索关键字
     * @param category 分类筛选
     * @param sort 排序方式
     * @return 文档搜索结果列表
     */
    private List<SearchResultVo> searchDocuments(String keyword, String category, String sort) {
        LambdaQueryWrapper<DocNote> wrapper = new LambdaQueryWrapper<DocNote>()
            .eq(DocNote::getDeleted, 0)
            .eq(DocNote::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(DocNote::getNoteName, keyword)
                .or()
                .like(DocNote::getContent, keyword)
            );
        }

        if (StringUtils.hasText(category)) {
            wrapper.eq(DocNote::getBroadCode, category);
        }

        List<DocNote> notes = noteService.list(wrapper);

        List<SearchResultVo> results = new ArrayList<>();
        for (DocNote note : notes) {
            SearchResultVo vo = new SearchResultVo();
            vo.setId(String.valueOf(note.getId()));
            vo.setType("document");
            vo.setTitle(note.getNoteName());
            vo.setDescription(note.getContent() != null ? note.getContent() : "");
            vo.setCategory(note.getBroadCode());
            vo.setThumbnail(note.getCover());
            vo.setReadCount(note.getViewCount());
            vo.setCommentCount(note.getCommentCount());
            vo.setFavoriteCount(0L);
            vo.setAuthorId(String.valueOf(note.getUserId()));
            vo.setAuthor("作者" + note.getUserId());
            vo.setTags(new ArrayList<>());
            results.add(vo);
        }

        return results;
    }

    /**
     * 排序结果
     *
     * @param results 搜索结果列表
     * @param sort 排序方式
     */
    private void sortResults(List<SearchResultVo> results, String sort) {
        if (sort == null || sort.isEmpty()) {
            return;
        }

        switch (sort) {
            case "hot":
                results.sort((a, b) -> {
                    long aCount = (a.getPlayCount() != null ? a.getPlayCount() : 0) 
                        + (a.getReadCount() != null ? a.getReadCount() : 0);
                    long bCount = (b.getPlayCount() != null ? b.getPlayCount() : 0) 
                        + (b.getReadCount() != null ? b.getReadCount() : 0);
                    return Long.compare(bCount, aCount);
                });
                break;
            case "like":
                results.sort((a, b) -> {
                    long aCount = a.getFavoriteCount() != null ? a.getFavoriteCount() : 0;
                    long bCount = b.getFavoriteCount() != null ? b.getFavoriteCount() : 0;
                    return Long.compare(bCount, aCount);
                });
                break;
            default:
                break;
        }
    }
}
