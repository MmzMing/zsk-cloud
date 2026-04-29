package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.vo.*;
import com.zsk.document.service.IDocAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统一审核管理 控制器
 *
 * <p>提供文档、视频、评论等多种内容类型的统一审核API，
 * 通过 targetType 参数区分不同内容类型，替代原有的 DocNoteAuditController、
 * DocVideoAuditController、DocNoteReviewController 三个独立控制器。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
@Tag(name = "统一审核管理")
@RestController
@RequestMapping("/docAudit")
@RequiredArgsConstructor
public class DocAuditController {

    /**
     * 统一审核服务
     */
    private final IDocAuditService auditService;

    /**
     * 获取审核队列
     *
     * <p>根据目标类型和审核状态筛选审核队列，支持分页查询。</p>
     *
     * @param targetType 目标类型（1-文档 2-视频 3-文档评论 4-视频评论）
     * @param auditStatus 审核状态（可选，0-待审核 1-通过 2-驳回）
     * @param pageQuery  分页参数
     * @return 审核队列分页结果
     */
    @Operation(summary = "获取审核队列")
    @GetMapping("/queue")
    public R<PageResult<DocAuditQueueVO>> getAuditQueue(
            @RequestParam Integer targetType,
            @RequestParam(required = false) Integer auditStatus,
            PageQuery pageQuery) {
        return R.ok(auditService.getAuditQueue(targetType, auditStatus, pageQuery));
    }

    /**
     * 获取审核详情
     *
     * <p>根据目标类型和目标ID查询最新的审核记录。</p>
     *
     * @param targetType 目标类型（1-文档 2-视频 3-文档评论 4-视频评论）
     * @param targetId   目标ID
     * @return 审核详情
     */
    @Operation(summary = "获取审核详情")
    @GetMapping("/detail")
    public R<DocAuditDetailVO> getAuditDetail(
            @RequestParam Integer targetType,
            @RequestParam Long targetId) {
        return R.ok(auditService.getAuditDetail(targetType, targetId));
    }

    /**
     * 提交审核结果
     *
     * <p>单条内容审核结果提交，审核通过或驳回。</p>
     *
     * @param request 审核提交请求
     * @return 是否成功
     */
    @Operation(summary = "提交审核结果")
    @PostMapping("/submit")
    public R<Void> submitAudit(@RequestBody @Valid DocAuditSubmitRequest request) {
        auditService.submitAudit(request);
        return R.ok();
    }

    /**
     * 批量提交审核结果
     *
     * <p>对同一类型的多个内容批量提交审核结果，部分失败不影响其他项。</p>
     *
     * @param request 批量审核提交请求
     * @return 是否成功
     */
    @Operation(summary = "批量提交审核结果")
    @PostMapping("/submitBatch")
    public R<Void> submitAuditBatch(@RequestBody @Valid DocAuditBatchSubmitRequest request) {
        auditService.submitAuditBatch(request);
        return R.ok();
    }

    /**
     * 获取审核日志
     *
     * <p>查询审核操作日志，支持按目标类型筛选，按审核时间倒序排列。</p>
     *
     * @param targetType 目标类型（可选，null则查询全部类型）
     * @param pageQuery  分页参数
     * @return 审核日志分页结果
     */
    @Operation(summary = "获取审核日志")
    @GetMapping("/logs")
    public R<PageResult<DocAuditLogVO>> getAuditLogs(
            @RequestParam(required = false) Integer targetType,
            PageQuery pageQuery) {
        return R.ok(auditService.getAuditLogs(targetType, pageQuery));
    }

    /**
     * 获取违规原因列表
     *
     * <p>根据目标类型获取对应的违规原因字典数据，
     * 文档类使用 DOCUMENT_VIOLATION_REASON，视频类使用 VIDEO_VIOLATION_REASON。</p>
     *
     * @param targetType 目标类型（1-文档 2-视频 3-文档评论 4-视频评论）
     * @return 违规原因列表
     */
    @Operation(summary = "获取违规原因列表")
    @GetMapping("/violation-reasons")
    public R<List<DocViolationReasonVO>> getViolationReasons(
            @RequestParam Integer targetType) {
        return R.ok(auditService.getViolationReasons(targetType));
    }
}
