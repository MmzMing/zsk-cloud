package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.log.annotation.Log;
import com.zsk.common.log.enums.BusinessType;
import com.zsk.document.domain.DocNote;
import com.zsk.document.service.IDocNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 笔记信息Controller
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "笔记管理")
@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
public class DocNoteController {

    private final IDocNoteService docNoteService;

    /**
     * 查询笔记列表
     *
     * @param docNote 查询条件
     * @return 笔记列表
     */
    @Log(title = "笔记管理", businessType = BusinessType.QUERY)
    @Operation(summary = "查询笔记列表")
    @GetMapping("/list")
    public R<List<DocNote>> list(DocNote docNote) {
        LambdaQueryWrapper<DocNote> lqw = new LambdaQueryWrapper<>(docNote);
        return R.ok(docNoteService.list(lqw));
    }

    /**
     * 分页查询笔记列表
     *
     * @param docNote 查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Log(title = "笔记管理", businessType = BusinessType.QUERY)
    @Operation(summary = "分页查询笔记列表")
    @GetMapping("/page")
    public R<PageResult<DocNote>> page(DocNote docNote, PageQuery pageQuery) {
        Page<DocNote> page = pageQuery.build();
        LambdaQueryWrapper<DocNote> lqw = new LambdaQueryWrapper<>(docNote);
        return R.ok(PageResult.build(docNoteService.page(page, lqw)));
    }

    /**
     * 获取笔记详细信息
     *
     * @param id 笔记ID
     * @return 笔记详情
     */
    @Log(title = "笔记管理", businessType = BusinessType.QUERY)
    @Operation(summary = "获取笔记详细信息")
    @GetMapping(value = "/{id}")
    public R<DocNote> getInfo(@PathVariable("id") Long id) {
        return R.ok(docNoteService.getById(id));
    }

    /**
     * 新增笔记
     *
     * @param docNote 笔记信息
     * @return 是否成功
     */
    @Log(title = "笔记管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增笔记")
    @PostMapping
    public R<Boolean> add(@RequestBody DocNote docNote) {
        return R.ok(docNoteService.save(docNote));
    }

    /**
     * 修改笔记
     *
     * @param docNote 笔记信息
     * @return 是否成功
     */
    @Log(title = "笔记管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改笔记")
    @PutMapping
    public R<Boolean> edit(@RequestBody DocNote docNote) {
        return R.ok(docNoteService.updateById(docNote));
    }

    /**
     * 删除笔记
     *
     * @param ids 笔记ID列表
     * @return 是否成功
     */
    @Log(title = "笔记管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除笔记")
    @DeleteMapping("/{ids}")
    public R<Boolean> remove(@PathVariable List<Long> ids) {
        return R.ok(docNoteService.removeBatchByIds(ids));
    }

    /**
     * 获取草稿列表
     *
     * @param pageQuery 分页参数
     * @return 草稿列表
     */
    @Log(title = "笔记管理", businessType = BusinessType.QUERY)
    @Operation(summary = "获取草稿列表")
    @GetMapping("/draft/list")
    public R<PageResult<DocNote>> draftList(PageQuery pageQuery) {
        Page<DocNote> page = pageQuery.build();
        LambdaQueryWrapper<DocNote> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DocNote::getStatus, 3);
        return R.ok(PageResult.build(docNoteService.page(page, lqw)));
    }

    /**
     * 批量更新状态
     *
     * @param body 请求体（包含ids和status）
     * @return 是否成功
     */
    @Log(title = "笔记管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "批量更新状态")
    @PutMapping("/status/batch")
    public R<Void> batchUpdateStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) body.get("ids");
        String status = (String) body.get("status");

        LambdaUpdateWrapper<DocNote> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(DocNote::getId, ids);

        if ("published".equals(status)) {
            updateWrapper.set(DocNote::getStatus, 1);
            updateWrapper.set(DocNote::getAuditStatus, 1);
        } else if ("offline".equals(status)) {
            updateWrapper.set(DocNote::getStatus, 2);
        }

        return docNoteService.update(updateWrapper) ? R.ok() : R.fail();
    }

    /**
     * 批量迁移分类
     *
     * @param body 请求体（包含ids和category）
     * @return 是否成功
     */
    @Log(title = "笔记管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "批量迁移分类")
    @PutMapping("/category/batch")
    public R<Void> batchMoveCategory(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) body.get("ids");
        String category = (String) body.get("category");

        LambdaUpdateWrapper<DocNote> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(DocNote::getId, ids);
        updateWrapper.set(DocNote::getBroadCode, category);

        return docNoteService.update(updateWrapper) ? R.ok() : R.fail();
    }

    /**
     * 切换置顶状态
     *
     * @param id 笔记ID
     * @return 是否成功
     */
    @Log(title = "笔记管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "切换置顶状态")
    @PutMapping("/{id}/pinned")
    public R<Void> togglePinned(@PathVariable Long id) {
        DocNote note = docNoteService.getById(id);
        if (note == null) {
            return R.fail("笔记不存在");
        }

        LambdaUpdateWrapper<DocNote> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocNote::getId, id);
        updateWrapper.set(DocNote::getIsPinned, note.getIsPinned() == 1 ? 0 : 1);

        return docNoteService.update(updateWrapper) ? R.ok() : R.fail();
    }

    /**
     * 切换推荐状态
     *
     * @param id 笔记ID
     * @return 是否成功
     */
    @Log(title = "笔记管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "切换推荐状态")
    @PutMapping("/{id}/recommended")
    public R<Void> toggleRecommended(@PathVariable Long id) {
        DocNote note = docNoteService.getById(id);
        if (note == null) {
            return R.fail("笔记不存在");
        }

        LambdaUpdateWrapper<DocNote> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocNote::getId, id);
        updateWrapper.set(DocNote::getIsRecommended, note.getIsRecommended() == 1 ? 0 : 1);

        return docNoteService.update(updateWrapper) ? R.ok() : R.fail();
    }
}
