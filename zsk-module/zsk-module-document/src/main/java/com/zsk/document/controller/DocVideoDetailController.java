package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

/**
 * 视频详情Controller
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
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
     */
    @Operation(summary = "查询视频详情列表")
    @GetMapping("/list")
    public R<List<DocVideoDetail>> list(DocVideoDetail docVideoDetail) {
        LambdaQueryWrapper<DocVideoDetail> lqw = new LambdaQueryWrapper<>(docVideoDetail);
        return R.ok(docVideoDetailService.list(lqw));
    }

    /**
     * 分页查询视频详情列表
     */
    @Operation(summary = "分页查询视频详情列表")
    @GetMapping("/page")
    public R<PageResult<DocVideoDetail>> page(DocVideoDetail docVideoDetail, PageQuery pageQuery) {
        Page<DocVideoDetail> page = pageQuery.build();
        LambdaQueryWrapper<DocVideoDetail> lqw = new LambdaQueryWrapper<>(docVideoDetail);
        return R.ok(PageResult.build(docVideoDetailService.page(page, lqw)));
    }

    /**
     * 获取视频详情详细信息
     */
    @Operation(summary = "获取视频详情详细信息")
    @GetMapping(value = "/{id}")
    public R<DocVideoDetail> getInfo(@PathVariable("id") Long id) {
        return R.ok(docVideoDetailService.getById(id));
    }

    /**
     * 新增视频详情
     */
    @Operation(summary = "新增视频详情")
    @PostMapping
    public R<Boolean> add(@RequestBody DocVideoDetail docVideoDetail) {
        return R.ok(docVideoDetailService.save(docVideoDetail));
    }

    /**
     * 上传视频文件并保存详情
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
     */
    @Operation(summary = "修改视频详情")
    @PutMapping
    public R<Boolean> edit(@RequestBody DocVideoDetail docVideoDetail) {
        return R.ok(docVideoDetailService.updateById(docVideoDetail));
    }

    /**
     * 删除视频详情
     */
    @Operation(summary = "删除视频详情")
    @DeleteMapping("/{ids}")
    public R<Boolean> remove(@PathVariable List<Long> ids) {
        return R.ok(docVideoDetailService.removeBatchByIds(ids));
    }
}
