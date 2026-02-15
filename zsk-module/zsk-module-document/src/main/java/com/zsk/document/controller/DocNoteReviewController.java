package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.log.annotation.Log;
import com.zsk.common.log.enums.BusinessType;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.vo.DocNoteReviewVO;
import com.zsk.document.service.IDocNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档审核Controller
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "文档审核")
@RestController
@RequestMapping("/note/review")
@RequiredArgsConstructor
public class DocNoteReviewController {

    private final IDocNoteService docNoteService;

    /**
     * 获取审核队列
     *
     * @param pageQuery 分页参数
     * @return 审核队列
     */
    @Log(title = "文档审核", businessType = BusinessType.QUERY)
    @Operation(summary = "获取审核队列")
    @GetMapping("/queue")
    public R<PageResult<DocNoteReviewVO>> getReviewQueue(PageQuery pageQuery) {
        Page<DocNote> page = pageQuery.build();
        LambdaQueryWrapper<DocNote> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DocNote::getAuditStatus, 0);
        lqw.eq(DocNote::getStatus, 1);
        lqw.orderByDesc(DocNote::getCreateTime);

        Page<DocNote> result = docNoteService.page(page, lqw);
        List<DocNoteReviewVO> voList = result.getRecords().stream()
                .map(this::convertToReviewVO)
                .toList();

        return R.ok(new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /**
     * 提交审核结果
     *
     * @param body 请求体
     * @return 是否成功
     */
    @Log(title = "文档审核", businessType = BusinessType.UPDATE)
    @Operation(summary = "提交审核结果")
    @PostMapping("/submit")
    public R<Void> submitReview(@RequestBody DocNoteReviewVO body) {
        DocNote note = new DocNote();
        note.setId(Long.valueOf(body.getId()));
        note.setAuditStatus("approved".equals(body.getResult()) ? 1 : 2);
        note.setUpdateTime(LocalDateTime.now());

        return docNoteService.updateById(note) ? R.ok() : R.fail();
    }

    /**
     * 转换为审核VO
     */
    private DocNoteReviewVO convertToReviewVO(DocNote note) {
        DocNoteReviewVO vo = new DocNoteReviewVO();
        vo.setId(String.valueOf(note.getId()));
        vo.setTitle(note.getNoteName());
        vo.setCategory(note.getBroadCode());
        vo.setStatus(note.getAuditStatus() == 0 ? "pending" : "approved");
        vo.setCreatedAt(note.getCreateTime() != null ? note.getCreateTime().toString() : "");
        return vo;
    }
}
