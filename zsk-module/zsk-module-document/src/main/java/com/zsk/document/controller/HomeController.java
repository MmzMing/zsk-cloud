package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsk.common.core.domain.R;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocVideoDetail;
import com.zsk.document.domain.vo.HomeArticleVo;
import com.zsk.document.domain.vo.HomeReviewVo;
import com.zsk.document.domain.vo.HomeSlideVo;
import com.zsk.document.domain.vo.HomeVideoVo;
import com.zsk.document.service.IDocNoteService;
import com.zsk.document.service.IDocVideoDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "首页")
@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final IDocVideoDetailService videoService;
    private final IDocNoteService noteService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取首页视频列表
     *
     * @return 视频列表
     */
    @Operation(summary = "获取首页视频列表")
    @GetMapping("/videos")
    public R<List<HomeVideoVo>> getVideos() {
        List<DocVideoDetail> videos = videoService.list(
            new LambdaQueryWrapper<DocVideoDetail>()
                .eq(DocVideoDetail::getDeleted, 0)
                .eq(DocVideoDetail::getStatus, 1)
                .orderByDesc(DocVideoDetail::getViewCount)
                .last("LIMIT 10")
        );

        List<HomeVideoVo> result = new ArrayList<>();
        for (DocVideoDetail video : videos) {
            HomeVideoVo vo = new HomeVideoVo();
            vo.setId(String.valueOf(video.getId()));
            vo.setCategory(video.getBroadCode() != null ? video.getBroadCode() : "默认");
            vo.setDuration("00:00");
            vo.setTitle(video.getVideoTitle());
            vo.setDescription(video.getFileContent());
            vo.setViews(String.valueOf(video.getViewCount() != null ? video.getViewCount() : 0));
            vo.setLikes(video.getLikeCount() != null ? video.getLikeCount().intValue() : 0);
            vo.setComments(video.getCommentCount() != null ? video.getCommentCount().intValue() : 0);
            vo.setDate(video.getCreateTime() != null ? video.getCreateTime().format(DATE_FORMATTER) : "");
            vo.setCover(video.getCoverUrl());
            vo.setSources(video.getVideoUrl());
            result.add(vo);
        }

        return R.ok(result);
    }

    /**
     * 获取首页文章列表
     *
     * @return 文章列表
     */
    @Operation(summary = "获取首页文章列表")
    @GetMapping("/articles")
    public R<List<HomeArticleVo>> getArticles() {
        List<DocNote> notes = noteService.list(
            new LambdaQueryWrapper<DocNote>()
                .eq(DocNote::getDeleted, 0)
                .eq(DocNote::getStatus, 1)
                .orderByDesc(DocNote::getViewCount)
                .last("LIMIT 10")
        );

        List<HomeArticleVo> result = new ArrayList<>();
        for (DocNote note : notes) {
            HomeArticleVo vo = new HomeArticleVo();
            vo.setId(String.valueOf(note.getId()));
            vo.setCategory(note.getBroadCode() != null ? note.getBroadCode() : "默认");
            vo.setTitle(note.getNoteName());
            vo.setDate(note.getCreateTime() != null ? note.getCreateTime().format(DATE_FORMATTER) : "");
            vo.setSummary("");
            vo.setViews(String.valueOf(note.getViewCount() != null ? note.getViewCount() : 0));
            vo.setAuthor("作者" + note.getUserId());
            vo.setCover(note.getCover());
            result.add(vo);
        }

        return R.ok(result);
    }

    /**
     * 获取首页评论列表
     *
     * @return 评论列表
     */
    @Operation(summary = "获取首页评论列表")
    @GetMapping("/reviews")
    public R<List<HomeReviewVo>> getReviews() {
        /** 暂时返回空列表，后续可从配置或数据库获取 */
        List<HomeReviewVo> result = new ArrayList<>();
        return R.ok(result);
    }

    /**
     * 获取首页幻灯片列表
     *
     * @return 幻灯片列表
     */
    @Operation(summary = "获取首页幻灯片列表")
    @GetMapping("/slides")
    public R<List<HomeSlideVo>> getSlides() {
        /** 暂时返回空列表，后续可从配置或数据库获取 */
        List<HomeSlideVo> result = new ArrayList<>();
        return R.ok(result);
    }
}
