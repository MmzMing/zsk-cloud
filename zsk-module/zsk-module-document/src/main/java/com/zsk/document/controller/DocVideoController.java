package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocFiles;
import com.zsk.document.domain.DocVideo;
import com.zsk.document.domain.vo.InteractionResultVo;
import com.zsk.document.service.IDocFilesService;
import com.zsk.document.service.IDocVideoInteractionService;
import com.zsk.document.service.IDocVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 视频Controller
 * <p>
 * 提供视频的CRUD操作、草稿管理、状态管理以及交互数据查询功能。
 * 交互数据（浏览量、点赞量、收藏量）通过独立接口获取，与基础详情分离。
 * </p>
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-02-14
 */
@Tag(name = "视频管理")
@RestController
@RequestMapping("/docVideo")
@RequiredArgsConstructor
public class DocVideoController {

    /**
     * 视频Service
     */
    private final IDocVideoService docVideoService;

    /**
     * 文件Service
     */
    private final IDocFilesService docFilesService;

    /**
     * 视频交互Service
     */
    private final IDocVideoInteractionService docVideoInteractionService;

    /**
     * 查询视频列表
     * <p>
     * 根据查询条件获取视频列表，支持按标题、分类等条件筛选。
     * </p>
     *
     * @param docVideo 查询条件（可选：videoTitle、broadCode、narrowCode等）
     * @return 视频列表
     */
    @Operation(summary = "查询视频列表")
    @GetMapping("/list")
    public R<List<DocVideo>> list(DocVideo docVideo) {
        LambdaQueryWrapper<DocVideo> lqw = new LambdaQueryWrapper<>(docVideo);
        return R.ok(docVideoService.list(lqw));
    }

    /**
     * 分页查询视频列表
     * <p>
     * 分页获取视频列表，默认按创建时间倒序排列。
     * </p>
     *
     * @param docVideo  查询条件（可选）
     * @param pageQuery 分页参数（pageNum、pageSize）
     * @return 分页结果
     */
    @Operation(summary = "分页查询视频列表")
    @GetMapping("/page")
    public R<PageResult<DocVideo>> page(DocVideo docVideo, PageQuery pageQuery) {
        Page<DocVideo> page = pageQuery.build();
        LambdaQueryWrapper<DocVideo> lqw = new LambdaQueryWrapper<>(docVideo);
        lqw.orderByDesc(DocVideo::getCreateTime);
        return R.ok(PageResult.build(docVideoService.page(page, lqw)));
    }

    /**
     * 获取视频详细信息
     * <p>
     * 根据视频ID获取详情，同时关联查询文件信息获取封面图URL和视频播放地址。
     * 交互数据（浏览量、点赞量等）需通过 {@link #getInteraction(Long, Long)} 接口单独获取。
     * </p>
     *
     * @param id 视频ID
     * @return 视频详情（包含封面图URL和视频播放地址）
     */
    @Operation(summary = "获取视频详细信息")
    @GetMapping(value = "/{id}")
    public R<DocVideo> getInfo(@PathVariable("id") Long id) {
        DocVideo detail = docVideoService.getById(id);
        if (detail != null && detail.getFileId() != null) {
            DocFiles file = docFilesService.getByFileId(detail.getFileId());
            if (file != null) {
                String fileType = file.getFileType();
                if (isImageFile(fileType)) {
                    detail.setCoverUrl(file.getUrl());
                } else if (isVideoFile(fileType)) {
                    detail.setVideoUrl(file.getUrl());
                }
            }
        }
        return R.ok(detail);
    }

    /**
     * 获取视频交互数据
     * <p>
     * 独立查询视频的交互数据，包括浏览量、点赞量、收藏量以及当前用户的交互状态。
     * 数据来源于Redis缓存，如缓存未命中则从数据库加载。
     * </p>
     *
     * @param id     视频ID
     * @param userId 当前用户ID（可选，用于判断用户是否已点赞/收藏）
     * @return 交互数据（浏览量、点赞量、收藏量、用户交互状态）
     */
    @Operation(summary = "获取视频交互数据")
    @GetMapping("/{id}/interaction")
    public R<InteractionResultVo> getInteraction(@PathVariable("id") Long id,
                                                 @RequestParam(required = false) Long userId) {
        return R.ok(docVideoInteractionService.getVideoInteraction(id, userId));
    }

    /**
     * 增加视频浏览量
     * <p>
     * 用户浏览视频时调用，增加对应视频的浏览计数。
     * 浏览量先写入Redis，后由定时任务同步到数据库。
     * </p>
     *
     * @param id     视频ID
     * @param userId 用户ID（可选，用于防止同一用户短时间内重复计数）
     * @return 操作结果
     */
    @Operation(summary = "增加视频浏览量")
    @PostMapping("/{id}/view")
    public R<Void> incrementView(@PathVariable("id") Long id,
                                 @RequestParam(required = false) Long userId) {
        docVideoInteractionService.incrementViewCount(id, userId);
        return R.ok();
    }

    /**
     * 判断是否为图片文件
     *
     * @param fileType 文件类型（如jpg、png、mp4等）
     * @return true-是图片文件，false-不是图片文件
     */
    private boolean isImageFile(String fileType) {
        if (fileType == null) return false;
        String type = fileType.toLowerCase();
        return type.equals("jpg") || type.equals("jpeg") || type.equals("png")
                || type.equals("gif") || type.equals("bmp") || type.equals("webp");
    }

    /**
     * 判断是否为视频文件
     *
     * @param fileType 文件类型（如mp4、avi、mov等）
     * @return true-是视频文件，false-不是视频文件
     */
    private boolean isVideoFile(String fileType) {
        if (fileType == null) return false;
        String type = fileType.toLowerCase();
        return type.equals("mp4") || type.equals("avi") || type.equals("mov")
                || type.equals("wmv") || type.equals("flv") || type.equals("mkv")
                || type.equals("webm") || type.equals("m3u8");
    }

    /**
     * 新增视频
     *
     * @param docVideo 视频信息
     * @return 是否成功
     */
    @Operation(summary = "新增视频")
    @PostMapping
    public R<Boolean> add(@RequestBody DocVideo docVideo) {
        return R.ok(docVideoService.save(docVideo));
    }

    /**
     * 上传视频文件并保存
     *
     * @param file     视频文件
     * @param docVideo 视频信息
     * @return 是否成功
     */
    @Operation(summary = "上传视频文件并保存")
    @PostMapping("/upload")
    public R<Boolean> upload(@RequestPart("file") MultipartFile file, DocVideo docVideo) {
        DocFiles docFile = docFilesService.uploadFile(file);
        docVideo.setFileId(docFile.getFileId());
        return R.ok(docVideoService.save(docVideo));
    }

    /**
     * 修改视频
     *
     * @param docVideo 视频信息
     * @return 是否成功
     */
    @Operation(summary = "修改视频")
    @PutMapping
    public R<Boolean> edit(@RequestBody DocVideo docVideo) {
        return R.ok(docVideoService.updateById(docVideo));
    }

    /**
     * 删除视频
     *
     * @param ids 视频ID列表
     * @return 是否成功
     */
    @Operation(summary = "删除视频")
    @DeleteMapping("/{ids}")
    public R<Boolean> remove(@PathVariable List<Long> ids) {
        return R.ok(docVideoService.removeBatchByIds(ids));
    }

    // ===== 草稿管理接口 =====

    /**
     * 获取草稿列表
     *
     * @param pageQuery 分页参数
     * @return 草稿列表
     */
    @Operation(summary = "获取草稿列表")
    @GetMapping("/draft/list")
    public R<PageResult<DocVideo>> draftList(PageQuery pageQuery) {
        Page<DocVideo> page = pageQuery.build();
        LambdaQueryWrapper<DocVideo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DocVideo::getStatus, 3);
        lqw.orderByDesc(DocVideo::getUpdateTime);
        return R.ok(PageResult.build(docVideoService.page(page, lqw)));
    }

    /**
     * 保存草稿
     *
     * @param docVideo 视频信息
     * @return 草稿ID
     */
    @Operation(summary = "保存草稿")
    @PostMapping("/draft")
    public R<Long> saveDraft(@RequestBody DocVideo docVideo) {
        docVideo.setStatus(3);
        docVideo.setAuditStatus(0);
        docVideoService.saveOrUpdate(docVideo);
        return R.ok(docVideo.getId());
    }

    /**
     * 发布草稿
     *
     * @param id 草稿ID
     * @return 是否成功
     */
    @Operation(summary = "发布草稿")
    @PutMapping("/draft/publish/{id}")
    public R<Void> publishDraft(@PathVariable Long id) {
        return docVideoService.publishDraft(id) ? R.ok() : R.fail();
    }

    // ===== 状态管理接口 =====

    /**
     * 批量更新视频状态
     *
     * @param request 请求参数（ids: 视频ID列表, status: 目标状态）
     * @return 是否成功
     */
    @Operation(summary = "批量更新视频状态")
    @PutMapping("/status/batch")
    public R<Void> batchUpdateStatus(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) request.get("ids");
        Integer status = (Integer) request.get("status");
        return docVideoService.batchUpdateStatus(ids, status) ? R.ok() : R.fail();
    }

    /**
     * 切换视频置顶状态
     *
     * @param id     视频ID
     * @param pinned 置顶状态（0-否 1-是）
     * @return 是否成功
     */
    @Operation(summary = "切换视频置顶状态")
    @PutMapping("/{id}/pinned")
    public R<Void> togglePinned(@PathVariable Long id, @RequestParam Integer pinned) {
        return docVideoService.togglePinned(id, pinned) ? R.ok() : R.fail();
    }

    /**
     * 切换视频推荐状态
     *
     * @param id          视频ID
     * @param recommended 推荐状态（0-否 1-是）
     * @return 是否成功
     */
    @Operation(summary = "切换视频推荐状态")
    @PutMapping("/{id}/recommended")
    public R<Void> toggleRecommended(@PathVariable Long id, @RequestParam Integer recommended) {
        return docVideoService.toggleRecommended(id, recommended) ? R.ok() : R.fail();
    }
}
