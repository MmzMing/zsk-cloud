package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.log.annotation.Log;
import com.zsk.common.log.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档上传任务Controller
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "文档上传任务")
@RestController
@RequestMapping("/note/upload")
@RequiredArgsConstructor
public class DocNoteUploadController {

    /**
     * 获取上传任务列表
     *
     * @return 任务列表
     */
    @Log(title = "文档上传任务", businessType = BusinessType.QUERY)
    @Operation(summary = "获取上传任务列表")
    @GetMapping("/task/list")
    public R<List<Object>> getTaskList() {
        // 暂时返回空列表，后续可实现上传任务管理
        return R.ok(new ArrayList<>());
    }

    /**
     * 删除上传任务
     *
     * @param ids 任务ID列表
     * @return 是否成功
     */
    @Log(title = "文档上传任务", businessType = BusinessType.DELETE)
    @Operation(summary = "删除上传任务")
    @DeleteMapping("/task/{ids}")
    public R<Void> removeTask(@PathVariable List<Long> ids) {
        // 暂时返回成功，后续可实现上传任务管理
        return R.ok();
    }

    /**
     * 重试上传任务
     *
     * @param id 任务ID
     * @return 是否成功
     */
    @Log(title = "文档上传任务", businessType = BusinessType.UPDATE)
    @Operation(summary = "重试上传任务")
    @PostMapping("/task/{id}/retry")
    public R<Void> retryTask(@PathVariable Long id) {
        // 暂时返回成功，后续可实现上传任务管理
        return R.ok();
    }
}
