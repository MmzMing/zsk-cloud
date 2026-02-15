package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsk.common.core.domain.R;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocVideoDetail;
import com.zsk.document.domain.vo.SearchResultVo;
import com.zsk.document.service.IDocNoteService;
import com.zsk.document.service.IDocVideoDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "搜索")
@RestController
@RequestMapping("/content/search")
@RequiredArgsConstructor
public class SearchController {

    private final IDocVideoDetailService videoService;
    private final IDocNoteService noteService;

    /**
     * 全站搜索
     *
     * @param keyword 关键字
     * @param type 类型（all/video/document/tool/user）
     * @param sort 排序（hot/latest/like/usage/relevance/fans/active）
     * @param duration 时长筛选
     * @param timeRange 时间范围
     * @param category 分类筛选
     * @param page 页码
     * @param pageSize 每页数量
     * @return 搜索结果
     */
    @Operation(summary = "全站搜索")
    @GetMapping("/all")
    public R<Map<String, Object>> searchAll(
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "type", required = false, defaultValue = "all") String type,
        @RequestParam(value = "sort", required = false) String sort,
        @RequestParam(value = "duration", required = false) String duration,
        @RequestParam(value = "timeRange", required = false) String timeRange,
        @RequestParam(value = "category", required = false) String category,
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {

        List<SearchResultVo> allResults = new ArrayList<>();

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
        int total = allResults.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<SearchResultVo> pageResults = fromIndex < total 
            ? allResults.subList(fromIndex, toIndex) 
            : new ArrayList<>();

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResults);
        result.put("total", total);
        return R.ok(result);
    }

    /**
     * 搜索视频
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
