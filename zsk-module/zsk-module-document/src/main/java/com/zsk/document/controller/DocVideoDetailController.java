package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocFiles;
import com.zsk.document.domain.DocVideoDetail;
import com.zsk.document.domain.vo.InteractionResultVo;
import com.zsk.document.service.IDocFilesService;
import com.zsk.document.service.IDocVideoDetailService;
import com.zsk.document.service.IDocVideoInteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 视频详情Controller
 * <p>
 * 提供视频详情的CRUD操作、草稿管理、状态管理以及交互数据查询功能。
 * 交互数据（浏览量、点赞量、收藏量）通过独立接口获取，与基础详情分离。
 * </p>
 *
 * @author wuhuaming
 * @date 2026-02-14
 * @version 2.0
 */
@Tag(name = "视频详情管理")
@RestController
@RequestMapping("/docVideoDetail")
@RequiredArgsConstructor
public class DocVideoDetailController {

    /**
     * 视频详情Service
     */
    private final IDocVideoDetailService docVideoDetailService;

    /**
     * 文件Service
     */
    private final IDocFilesService docFilesService;

    /**
     * 视频交互Service
     */
    private final IDocVideoInteractionService docVideoInteractionService;

    /**
     * 查询视频详情列表
     * <p>
     * 根据查询条件获取视频详情列表，支持按标题、分类等条件筛选。
     * </p>
     *
     * @param docVideoDetail 查询条件（可选：videoTitle、broadCode、narrowCode等）
     * @return 视频详情列表
     */
    @Operation(summary = "查询视频详情列表")
    @GetMapping("/list")
    public R<List<DocVideoDetail>> list(DocVideoDetail docVideoDetail) {
        // 构建查询条件
        LambdaQueryWrapper<DocVideoDetail> lqw = new LambdaQueryWrapper<>(docVideoDetail);
        return R.ok(docVideoDetailService.list(lqw));
    }

    /**
     * 分页查询视频详情列表
     * <p>
     * 分页获取视频详情列表，默认按创建时间倒序排列。
     * </p>
     *
     * @param docVideoDetail 查询条件（可选）
     * @param pageQuery      分页参数（pageNum、pageSize）
     * @return 分页结果
     */
    @Operation(summary = "分页查询视频详情列表")
    @GetMapping("/page")
    public R<PageResult<DocVideoDetail>> page(DocVideoDetail docVideoDetail, PageQuery pageQuery) {
        // 1. 构建分页对象
        Page<DocVideoDetail> page = pageQuery.build();
        // 2. 构建查询条件并设置排序
        LambdaQueryWrapper<DocVideoDetail> lqw = new LambdaQueryWrapper<>(docVideoDetail);
        lqw.orderByDesc(DocVideoDetail::getCreateTime);
        // 3. 执行分页查询
        return R.ok(PageResult.build(docVideoDetailService.page(page, lqw)));
    }

    /**
     * 获取视频详情详细信息
     * <p>
     * 根据视频ID获取详情，同时关联查询文件信息获取封面图URL和视频播放地址。
     * 交互数据（浏览量、点赞量等）需通过 {@link #getInteraction(Long, Long)} 接口单独获取。
     * </p>
     *
     * @param id 视频ID
     * @return 视频详情（包含封面图URL和视频播放地址）
     */
    @Operation(summary = "获取视频详情详细信息")
    @GetMapping(value = "/{id}")
    public R<DocVideoDetail> getInfo(@PathVariable("id") Long id) {
        // 1. 查询视频基础详情
        DocVideoDetail detail = docVideoDetailService.getById(id);
        // 2. 如果存在关联文件，查询文件信息补充URL
        if (detail != null && detail.getFileId() != null) {
            DocFiles file = docFilesService.getByFileId(detail.getFileId());
            if (file != null) {
                String fileType = file.getFileType();
                // 根据文件类型设置不同的URL
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
        // 查询视频交互数据
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
        // 增加视频浏览量
        docVideoInteractionService.incrementViewCount(id, userId);
        return R.ok();
    }

    /**
     * 判断是否为图片文件
     * <p>
     * 根据文件类型判断是否为图片，用于区分封面图和视频文件。
     * </p>
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
     * <p>
     * 根据文件类型判断是否为视频，用于设置视频播放地址。
     * </p>
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
     * 新增视频详情
     * <p>
     * 创建新的视频详情记录。
     * </p>
     *
     * @param docVideoDetail 视频详情信息
     * @return 是否成功
     */
    @Operation(summary = "新增视频详情")
    @PostMapping
    public R<Boolean> add(@RequestBody DocVideoDetail docVideoDetail) {
        return R.ok(docVideoDetailService.save(docVideoDetail));
    }

    /**
     * 上传视频文件并保存详情
     * <p>
     * 上传视频文件到存储服务，同时保存视频详情信息。
     * 文件上传成功后，会自动设置fileId关联。
     * </p>
     *
     * @param file           视频文件
     * @param docVideoDetail 视频详情信息
     * @return 是否成功
     */
    @Operation(summary = "上传视频文件并保存详情")
    @PostMapping("/upload")
    public R<Boolean> upload(@RequestPart("file") MultipartFile file, DocVideoDetail docVideoDetail) {
        // 1. 上传文件到存储服务
        DocFiles docFile = docFilesService.uploadFile(file);
        // 2. 设置文件关联ID
        docVideoDetail.setFileId(docFile.getFileId());
        // 3. 保存视频详情
        return R.ok(docVideoDetailService.save(docVideoDetail));
    }

    /**
     * 修改视频详情
     * <p>
     * 更新视频详情信息。
     * </p>
     *
     * @param docVideoDetail 视频详情信息
     * @return 是否成功
     */
    @Operation(summary = "修改视频详情")
    @PutMapping
    public R<Boolean> edit(@RequestBody DocVideoDetail docVideoDetail) {
        return R.ok(docVideoDetailService.updateById(docVideoDetail));
    }

    /**
     * 删除视频详情
     * <p>
     * 批量删除视频详情记录。
     * </p>
     *
     * @param ids 视频ID列表
     * @return 是否成功
     */
    @Operation(summary = "删除视频详情")
    @DeleteMapping("/{ids}")
    public R<Boolean> remove(@PathVariable List<Long> ids) {
        return R.ok(docVideoDetailService.removeBatchByIds(ids));
    }

    // ===== 草稿管理接口 =====

    /**
     * 获取草稿列表
     * <p>
     * 分页获取状态为草稿的视频列表，按更新时间倒序排列。
     * </p>
     *
     * @param pageQuery 分页参数
     * @return 草稿列表
     */
    @Operation(summary = "获取草稿列表")
    @GetMapping("/draft/list")
    public R<PageResult<DocVideoDetail>> draftList(PageQuery pageQuery) {
        // 1. 构建分页对象
        Page<DocVideoDetail> page = pageQuery.build();
        // 2. 构建查询条件：状态=草稿，按更新时间倒序
        LambdaQueryWrapper<DocVideoDetail> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DocVideoDetail::getStatus, 3);
        lqw.orderByDesc(DocVideoDetail::getUpdateTime);
        // 3. 执行分页查询
        return R.ok(PageResult.build(docVideoDetailService.page(page, lqw)));
    }

    /**
     * 保存草稿
     * <p>
     * 保存视频草稿，状态设置为草稿（3），审核状态设置为待审核（0）。
     * 如果已存在ID则更新，否则新增。
     * </p>
     *
     * @param docVideoDetail 视频详情信息
     * @return 草稿ID
     */
    @Operation(summary = "保存草稿")
    @PostMapping("/draft")
    public R<Long> saveDraft(@RequestBody DocVideoDetail docVideoDetail) {
        // 设置状态为草稿
        docVideoDetail.setStatus(3);
        // 设置审核状态为待审核
        docVideoDetail.setAuditStatus(0);
        // 保存或更新草稿
        docVideoDetailService.saveOrUpdate(docVideoDetail);
        return R.ok(docVideoDetail.getId());
    }

    /**
     * 发布草稿
     * <p>
     * 将草稿状态变更为正常（1），并设置审核状态为待审核（0）。
     * </p>
     *
     * @param id 草稿ID
     * @return 是否成功
     */
    @Operation(summary = "发布草稿")
    @PutMapping("/draft/publish/{id}")
    public R<Void> publishDraft(@PathVariable Long id) {
        // 构建更新条件
        LambdaUpdateWrapper<DocVideoDetail> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocVideoDetail::getId, id);
        // 设置状态为正常
        updateWrapper.set(DocVideoDetail::getStatus, 1);
        // 设置审核状态为待审核
        updateWrapper.set(DocVideoDetail::getAuditStatus, 0);
        return docVideoDetailService.update(updateWrapper) ? R.ok() : R.fail();
    }

    // ===== 状态管理接口 =====

    /**
     * 批量更新视频状态
     * <p>
     * 批量修改视频的状态字段。
     * </p>
     *
     * @param request 请求参数（ids: 视频ID列表, status: 目标状态）
     * @return 是否成功
     */
    @Operation(summary = "批量更新视频状态")
    @PutMapping("/status/batch")
    public R<Void> batchUpdateStatus(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        // 提取参数
        List<Long> ids = (List<Long>) request.get("ids");
        Integer status = (Integer) request.get("status");

        // 构建批量更新条件
        LambdaUpdateWrapper<DocVideoDetail> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(DocVideoDetail::getId, ids);
        updateWrapper.set(DocVideoDetail::getStatus, status);

        return docVideoDetailService.update(updateWrapper) ? R.ok() : R.fail();
    }

    /**
     * 切换视频置顶状态
     * <p>
     * 设置视频是否置顶。
     * </p>
     *
     * @param id     视频ID
     * @param pinned 置顶状态（0-否 1-是）
     * @return 是否成功
     */
    @Operation(summary = "切换视频置顶状态")
    @PutMapping("/{id}/pinned")
    public R<Void> togglePinned(@PathVariable Long id, @RequestParam Integer pinned) {
        // 构建更新条件
        LambdaUpdateWrapper<DocVideoDetail> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocVideoDetail::getId, id);
        updateWrapper.set(DocVideoDetail::getIsPinned, pinned);

        return docVideoDetailService.update(updateWrapper) ? R.ok() : R.fail();
    }

    /**
     * 切换视频推荐状态
     * <p>
     * 设置视频是否推荐。
     * </p>
     *
     * @param id          视频ID
     * @param recommended 推荐状态（0-否 1-是）
     * @return 是否成功
     */
    @Operation(summary = "切换视频推荐状态")
    @PutMapping("/{id}/recommended")
    public R<Void> toggleRecommended(@PathVariable Long id, @RequestParam Integer recommended) {
        // 构建更新条件
        LambdaUpdateWrapper<DocVideoDetail> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocVideoDetail::getId, id);
        updateWrapper.set(DocVideoDetail::getIsRecommended, recommended);

        return docVideoDetailService.update(updateWrapper) ? R.ok() : R.fail();
    }
}
