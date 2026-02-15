package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.document.domain.vo.VideoCategoryVO;
import com.zsk.document.domain.vo.VideoTagVO;
import com.zsk.document.service.IVideoCategoryCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 视频分类标签 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "视频分类标签")
@RestController
@RequestMapping("/video/category")
@RequiredArgsConstructor
public class DocVideoCategoryController {

    private final IVideoCategoryCacheService cacheService;

    /**
     * 获取视频分类列表（从Redis缓存获取）
     *
     * @return 分类列表
     */
    @Operation(summary = "获取视频分类列表")
    @GetMapping("/list")
    public R<List<VideoCategoryVO>> getCategoryList() {
        return R.ok(cacheService.getCategoryListFromCache());
    }

    /**
     * 获取视频标签列表（从Redis缓存获取）
     *
     * @return 标签列表
     */
    @Operation(summary = "获取视频标签列表")
    @GetMapping("/tag/list")
    public R<List<VideoTagVO>> getTagList() {
        return R.ok(cacheService.getTagListFromCache());
    }
}
