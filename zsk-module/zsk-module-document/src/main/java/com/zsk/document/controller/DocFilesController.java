package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocFiles;
import com.zsk.document.domain.vo.MultipartCompleteRequest;
import com.zsk.document.domain.vo.MultipartInitRequest;
import com.zsk.document.service.IDocFilesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文件Controller
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Tag(name = "文件管理")
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class DocFilesController {

    private final IDocFilesService docFilesService;

    /**
     * 分页查询文件列表
     */
    @Operation(summary = "分页查询文件列表")
    @GetMapping("/page")
    public R<PageResult<DocFiles>> page(DocFiles docFiles, PageQuery pageQuery) {
        Page<DocFiles> page = pageQuery.build();
        LambdaQueryWrapper<DocFiles> lqw = new LambdaQueryWrapper<>(docFiles);
        return R.ok(PageResult.build(docFilesService.page(page, lqw)));
    }

    /**
     * 上传文件
     */
    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public R<DocFiles> upload(@RequestPart("file") MultipartFile file) {
        return R.ok(docFilesService.uploadFile(file));
    }

    /**
     * 删除文件
     */
    @Operation(summary = "删除文件")
    @DeleteMapping("/{ids}")
    public R<Boolean> remove(@PathVariable String ids) {
        // 这里的ids可能是逗号分隔的ID
        // 实际逻辑应该还包括删除OSS上的文件，这里暂只删除数据库记录
        // 建议完善Service层的remove逻辑
        return R.ok(docFilesService.removeByIds(java.util.Arrays.asList(ids.split(","))));
    }

    /**
     * 初始化分片上传
     * 该方法是先在数据库中保存文件记录，并返回一个分片上传ID（uploadId），用于后续分片上传和完成操作
     * 如果网络断开，请确保在完成分片上传之前，已上传的分片文件都保存在数据库中。
     *
     * @param request 分片上传初始化请求对象，包含文件名、文件类型、MD5等信息
     * @return 响应结果，成功时返回分片上传ID（uploadId），用于后续分片上传和完成操作
     */
    @Operation(summary = "初始化分片上传")
    @PostMapping("/multipart/init")
    public R<String> initiateMultipartUpload(@RequestBody MultipartInitRequest request) {
        return R.ok(docFilesService.initiateMultipartUpload(request));
    }

    /**
     * 上传分片
     */
    @Operation(summary = "上传分片")
    @PostMapping("/multipart/upload")
    public R<String> uploadPart(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("partNumber") Integer partNumber,
            @RequestPart("file") MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            String etag = docFilesService.uploadPart(uploadId, partNumber, is, file.getSize());
            return R.ok(etag);
        }
    }

    /**
     * 完成分片上传
     */
    @Operation(summary = "完成分片上传")
    @PostMapping("/multipart/complete")
    public R<Void> completeMultipartUpload(@RequestBody MultipartCompleteRequest request) {
        docFilesService.completeMultipartUpload(request);
        return R.ok();
    }
}
