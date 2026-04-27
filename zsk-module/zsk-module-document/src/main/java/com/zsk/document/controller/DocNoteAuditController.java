package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocNoteAudit;
import com.zsk.document.domain.vo.*;
import com.zsk.document.service.IDocNoteAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文档审核 控制器
 *
 * <p>提供文档审核队列查询、审核详情获取、审核结果提交（单条/批量）、审核日志查询及违规原因获取等RESTful API。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Tag(name = "文档审核")
@RestController
@RequestMapping("/docNoteAudit")
@RequiredArgsConstructor
public class DocNoteAuditController {

    private final IDocNoteAuditService auditService;

    /**
     * 获取审核队列
     *
     * @param auditStatus 审核状态（可选）
     * @param pageQuery   分页参数
     * @return 审核队列分页结果
     */
    @Operation(summary = "获取审核队列")
    @GetMapping("/queue")
    public R<PageResult<NoteAuditQueueVO>> getAuditQueue(
            @RequestParam(required = false) Integer auditStatus,
            PageQuery pageQuery) {
        return R.ok(auditService.getAuditQueue(auditStatus, pageQuery));
    }

    /**
     * 获取审核详情
     *
     * @param noteId 文档ID
     * @return 审核详情
     */
    @Operation(summary = "获取审核详情")
    @GetMapping("/detail/{noteId}")
    public R<DocNoteAudit> getAuditDetail(@PathVariable Long noteId) {
        return R.ok(auditService.getAuditDetail(noteId));
    }

    /**
     * 提交审核结果
     *
     * @param request 审核提交请求
     * @return 是否成功
     */
    @Operation(summary = "提交审核结果")
    @PostMapping("/submit")
    public R<Void> submitAudit(@RequestBody @Valid AuditSubmitRequest request) {
        return auditService.submitAudit(request) ? R.ok() : R.fail();
    }

    /**
     * 批量提交审核结果
     *
     * @param request 批量审核提交请求
     * @return 是否成功
     */
    @Operation(summary = "批量提交审核结果")
    @PostMapping("/submitBatch")
    public R<Void> submitAuditBatch(@RequestBody @Valid AuditBatchSubmitRequest request) {
        return auditService.submitAuditBatch(request) ? R.ok() : R.fail();
    }

    /**
     * 获取审核日志
     *
     * @param pageQuery 分页参数
     * @return 审核日志分页结果
     */
    @Operation(summary = "获取审核日志")
    @GetMapping("/logs")
    public R<PageResult<NoteAuditLogVO>> getAuditLogs(PageQuery pageQuery) {
        return R.ok(auditService.getAuditLogs(pageQuery));
    }

    /**
     * 获取违规原因列表
     *
     * @return 违规原因列表
     */
    @Operation(summary = "获取违规原因列表")
    @GetMapping("/violation-reasons")
    public R<List<ViolationReasonVO>> getViolationReasons() {
        return R.ok(auditService.getViolationReasons());
    }
}
