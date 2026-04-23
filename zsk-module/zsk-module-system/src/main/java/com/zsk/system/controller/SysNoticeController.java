package com.zsk.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.system.domain.SysNotice;
import com.zsk.system.service.ISysNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知公告 控制器
 *
 * @author wuhuaming
 */
@Tag(name = "通知公告")
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class SysNoticeController {

    private final ISysNoticeService noticeService;

    /**
     * 查询通知公告列表
     */
    @Operation(summary = "查询通知公告列表")
    @GetMapping("/list")
    public R<List<SysNotice>> list(SysNotice notice) {
        return R.ok(noticeService.list());
    }

    /**
     * 分页查询通知公告列表
     */
    @Operation(summary = "分页查询通知公告列表")
    @GetMapping("/page")
    public R<PageResult<SysNotice>> page(PageQuery pageQuery, SysNotice notice) {
        IPage<SysNotice> page = noticeService.page(pageQuery, notice);
        return R.ok(PageResult.build(page));
    }

    /**
     * 获取控制台最新公告（限制5条）
     */
    @Operation(summary = "获取控制台最新公告")
    @GetMapping("/console")
    public R<List<SysNotice>> listConsoleNotices() {
        return R.ok(noticeService.listLatest());
    }

    /**
     * 获取通知公告详细信息
     */
    @Operation(summary = "获取通知公告详细信息")
    @GetMapping("/{id}")
    public R<SysNotice> getInfo(@PathVariable Long id) {
        return R.ok(noticeService.getById(id));
    }

    /**
     * 新增通知公告
     */
    @Operation(summary = "新增通知公告")
    @PostMapping
    public R<Void> add(@RequestBody SysNotice notice) {
        return noticeService.save(notice) ? R.ok() : R.fail();
    }

    /**
     * 修改通知公告
     */
    @Operation(summary = "修改通知公告")
    @PutMapping
    public R<Void> edit(@RequestBody SysNotice notice) {
        return noticeService.updateById(notice) ? R.ok() : R.fail();
    }

    /**
     * 删除通知公告
     */
    @Operation(summary = "删除通知公告")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        return noticeService.removeByIds(ids) ? R.ok() : R.fail();
    }
}
