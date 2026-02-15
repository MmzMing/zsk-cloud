package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocFiles;
import com.zsk.document.domain.DocVideoDetail;
import com.zsk.document.service.IDocFilesService;
import com.zsk.document.service.IDocVideoDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 视频详情Controller
 *
 * @author wuhuaming
 * @date 2026-02-14
 * @version 1.0
 */
@Tag(name = "视频详情管理")
@RestController
@RequestMapping("/video/detail")
@RequiredArgsConstructor
public class DocVideoDetailController {

    private final IDocVideoDetailService docVideoDetailService;
    private final IDocFilesService docFilesService;

    /**
     * 查询视频详情列表
     *
     * @param docVideoDetail 查询条件
     * @return 视频列表
     */
    @Operation(summary = "查询视频详情列表")
    @GetMapping("/list")
    public R<List<DocVideoDetail>> list(DocVideoDetail docVideoDetail) {
        LambdaQueryWrapper<DocVideoDetail> lqw = new LambdaQueryWrapper<>(docVideoDetail);
        return R.ok(docVideoDetailService.list(lqw));
    }

    /**
     * 分页查询视频详情列表
     *
     * @param docVideoDetail 查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Operation(summary = "分页查询视频详情列表")
    @GetMapping("/page")
    public R<PageResult<DocVideoDetail>> page(DocVideoDetail docVideoDetail, PageQuery pageQuery) {
        Page<DocVideoDetail> page = pageQuery.build();
        LambdaQueryWrapper<DocVideoDetail> lqw = new LambdaQueryWrapper<>(docVideoDetail);
        lqw.orderByDesc(DocVideoDetail::getCreateTime);
        return R.ok(PageResult.build(docVideoDetailService.page(page, lqw)));
    }

    /**
     * 获取视频详情详细信息
     *
     * @param id 视频ID
     * @return 视频详情
     */
    @Operation(summary = "获取视频详情详细信息")
    @GetMapping(value = "/{id}")
    public R<DocVideoDetail> getInfo(@PathVariable("id") Long id) {
        DocVideoDetail detail = docVideoDetailService.getById(id);
        if (detail != null && detail.getFileId() != null) {
            // 关联查询文件信息获取coverUrl和videoUrl
            DocFiles file = docFilesService.getByFileId(detail.getFileId());
            if (file != null) {
                // 根据文件类型设置不同的URL
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
     * 判断是否为图片文件
     *
     * @param fileType 文件类型
     * @return 是否图片
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
     * @param fileType 文件类型
     * @return 是否视频
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
     *
     * @param docVideoDetail 视频详情
     * @return 是否成功
     */
    @Operation(summary = "新增视频详情")
    @PostMapping
    public R<Boolean> add(@RequestBody DocVideoDetail docVideoDetail) {
        return R.ok(docVideoDetailService.save(docVideoDetail));
    }

    /**
     * 上传视频文件并保存详情
     *
     * @param file 视频文件
     * @param docVideoDetail 视频详情
     * @return 是否成功
     */
    @Operation(summary = "上传视频文件并保存详情")
    @PostMapping("/upload")
    public R<Boolean> upload(@RequestPart("file") MultipartFile file, DocVideoDetail docVideoDetail) {
        // 1. 上传文件
        DocFiles docFile = docFilesService.uploadFile(file);

        // 2. 设置文件关联ID
        docVideoDetail.setFileId(docFile.getFileId());

        // 3. 保存视频详情
        return R.ok(docVideoDetailService.save(docVideoDetail));
    }

    /**
     * 修改视频详情
     *
     * @param docVideoDetail 视频详情
     * @return 是否成功
     */
    @Operation(summary = "修改视频详情")
    @PutMapping
    public R<Boolean> edit(@RequestBody DocVideoDetail docVideoDetail) {
        return R.ok(docVideoDetailService.updateById(docVideoDetail));
    }

    /**
     * 删除视频详情
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
     *
     * @param pageQuery 分页参数
     * @return 草稿列表
     */
    @Operation(summary = "获取草稿列表")
    @GetMapping("/draft/list")
    public R<PageResult<DocVideoDetail>> draftList(PageQuery pageQuery) {
        Page<DocVideoDetail> page = pageQuery.build();
        LambdaQueryWrapper<DocVideoDetail> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DocVideoDetail::getStatus, 3);
        lqw.orderByDesc(DocVideoDetail::getUpdateTime);
        return R.ok(PageResult.build(docVideoDetailService.page(page, lqw)));
    }

    /**
     * 保存草稿
     *
     * @param docVideoDetail 视频详情
     * @return 草稿ID
     */
    @Operation(summary = "保存草稿")
    @PostMapping("/draft")
    public R<Long> saveDraft(@RequestBody DocVideoDetail docVideoDetail) {
        docVideoDetail.setStatus(3);
        docVideoDetail.setAuditStatus(0);
        docVideoDetailService.saveOrUpdate(docVideoDetail);
        return R.ok(docVideoDetail.getId());
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
        LambdaUpdateWrapper<DocVideoDetail> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocVideoDetail::getId, id);
        updateWrapper.set(DocVideoDetail::getStatus, 1);
        updateWrapper.set(DocVideoDetail::getAuditStatus, 0);
        return docVideoDetailService.update(updateWrapper) ? R.ok() : R.fail();
    }

    // ===== 状态管理接口 =====

    /**
     * 批量更新视频状态
     *
     * @param request 请求参数（ids, status）
     * @return 是否成功
     */
    @Operation(summary = "批量更新视频状态")
    @PutMapping("/status/batch")
    public R<Void> batchUpdateStatus(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) request.get("ids");
        Integer status = (Integer) request.get("status");
        
        LambdaUpdateWrapper<DocVideoDetail> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(DocVideoDetail::getId, ids);
        updateWrapper.set(DocVideoDetail::getStatus, status);
        
        return docVideoDetailService.update(updateWrapper) ? R.ok() : R.fail();
    }

    /**
     * 切换视频置顶状态
     *
     * @param id 视频ID
     * @param pinned 是否置顶（0否 1是）
     * @return 是否成功
     */
    @Operation(summary = "切换视频置顶状态")
    @PutMapping("/{id}/pinned")
    public R<Void> togglePinned(@PathVariable Long id, @RequestParam Integer pinned) {
        LambdaUpdateWrapper<DocVideoDetail> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocVideoDetail::getId, id);
        updateWrapper.set(DocVideoDetail::getIsPinned, pinned);
        
        return docVideoDetailService.update(updateWrapper) ? R.ok() : R.fail();
    }

    /**
     * 切换视频推荐状态
     *
     * @param id 视频ID
     * @param recommended 是否推荐（0否 1是）
     * @return 是否成功
     */
    @Operation(summary = "切换视频推荐状态")
    @PutMapping("/{id}/recommended")
    public R<Void> toggleRecommended(@PathVariable Long id, @RequestParam Integer recommended) {
        LambdaUpdateWrapper<DocVideoDetail> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocVideoDetail::getId, id);
        updateWrapper.set(DocVideoDetail::getIsRecommended, recommended);
        
        return docVideoDetailService.update(updateWrapper) ? R.ok() : R.fail();
    }
}
