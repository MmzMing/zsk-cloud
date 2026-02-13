package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
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
 * @author zsk
 */
@Tag(name = "通知公告")
@RestController
@RequestMapping("/system/notice")
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
