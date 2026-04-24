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
import com.zsk.document.domain.vo.InteractionResultVo;
import com.zsk.document.service.IDocNoteInteractionService;
import com.zsk.document.service.IDocNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 笔记信息Controller
 * <p>
 * 提供笔记的CRUD操作、草稿管理、状态管理以及交互数据查询功能。
 * 交互数据（浏览量、点赞量、收藏量）通过独立接口获取，与基础详情分离。
 * </p>
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 2.0
 */
@Tag(name = "笔记管理")
@RestController
@RequestMapping("/docNote")
@RequiredArgsConstructor
public class DocNoteController {

    /**
     * 笔记Service
     */
    private final IDocNoteService docNoteService;

    /**
     * 笔记交互Service
     */
    private final IDocNoteInteractionService docNoteInteractionService;

    /**
     * 查询笔记列表
     * <p>
     * 根据查询条件获取笔记列表，支持按名称、分类等条件筛选。
     * </p>
     *
     * @param docNote 查询条件（可选：noteName、broadCode、narrowCode等）
     * @return 笔记列表
     */
    @Log(title = "笔记管理", businessType = BusinessType.QUERY)
    @Operation(summary = "查询笔记列表")
    @GetMapping("/list")
    public R<List<DocNote>> list(DocNote docNote) {
        // 构建查询条件
        LambdaQueryWrapper<DocNote> lqw = new LambdaQueryWrapper<>(docNote);
        return R.ok(docNoteService.list(lqw));
    }

    /**
     * 分页查询笔记列表
     * <p>
     * 分页获取笔记列表。
     * </p>
     *
     * @param docNote   查询条件（可选）
     * @param pageQuery 分页参数（pageNum、pageSize）
     * @return 分页结果
     */
    @Log(title = "笔记管理", businessType = BusinessType.QUERY)
    @Operation(summary = "分页查询笔记列表")
    @GetMapping("/page")
    public R<PageResult<DocNote>> page(DocNote docNote, PageQuery pageQuery) {
        // 1. 构建分页对象
        Page<DocNote> page = pageQuery.build();
        // 2. 构建查询条件
        LambdaQueryWrapper<DocNote> lqw = new LambdaQueryWrapper<>(docNote);
        // 3. 执行分页查询
        return R.ok(PageResult.build(docNoteService.page(page, lqw)));
    }

    /**
     * 获取笔记详细信息
     * <p>
     * 根据笔记ID获取详情。
     * 交互数据（浏览量、点赞量等）需通过 {@link #getInteraction(Long, Long)} 接口单独获取。
     * </p>
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
     * 获取笔记交互数据
     * <p>
     * 独立查询笔记的交互数据，包括浏览量、点赞量、收藏量以及当前用户的交互状态。
     * 数据来源于Redis缓存，如缓存未命中则从数据库加载。
     * </p>
     *
     * @param id     笔记ID
     * @param userId 当前用户ID（可选，用于判断用户是否已点赞/收藏）
     * @return 交互数据（浏览量、点赞量、收藏量、用户交互状态）
     */
    @Log(title = "笔记管理", businessType = BusinessType.QUERY)
    @Operation(summary = "获取笔记交互数据")
    @GetMapping("/{id}/interaction")
    public R<InteractionResultVo> getInteraction(@PathVariable("id") Long id,
                                                  @RequestParam(required = false) Long userId) {
        // 查询笔记交互数据
        return R.ok(docNoteInteractionService.getNoteInteraction(id, userId));
    }

    /**
     * 增加笔记浏览量
     * <p>
     * 用户浏览笔记时调用，增加对应笔记的浏览计数。
     * 浏览量先写入Redis，后由定时任务同步到数据库。
     * </p>
     *
     * @param id     笔记ID
     * @param userId 用户ID（可选，用于防止同一用户短时间内重复计数）
     * @return 操作结果
     */
    @Log(title = "笔记管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "增加笔记浏览量")
    @PostMapping("/{id}/view")
    public R<Void> incrementView(@PathVariable("id") Long id,
                                  @RequestParam(required = false) Long userId) {
        // 增加笔记浏览量
        docNoteInteractionService.incrementViewCount(id, userId);
        return R.ok();
    }

    /**
     * 新增笔记
     * <p>
     * 创建新的笔记记录。
     * </p>
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
     * <p>
     * 更新笔记信息。
     * </p>
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
     * <p>
     * 批量删除笔记记录。
     * </p>
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
     * <p>
     * 分页获取状态为草稿的笔记列表。
     * </p>
     *
     * @param pageQuery 分页参数
     * @return 草稿列表
     */
    @Log(title = "笔记管理", businessType = BusinessType.QUERY)
    @Operation(summary = "获取草稿列表")
    @GetMapping("/draft/list")
    public R<PageResult<DocNote>> draftList(PageQuery pageQuery) {
        // 1. 构建分页对象
        Page<DocNote> page = pageQuery.build();
        // 2. 构建查询条件：状态=草稿
        LambdaQueryWrapper<DocNote> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DocNote::getStatus, 3);
        // 3. 执行分页查询
        return R.ok(PageResult.build(docNoteService.page(page, lqw)));
    }

    /**
     * 批量更新状态
     * <p>
     * 批量修改笔记的状态和审核状态。
     * </p>
     *
     * @param body 请求体（ids: 笔记ID列表, status: 目标状态）
     * @return 是否成功
     */
    @Log(title = "笔记管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "批量更新状态")
    @PutMapping("/status/batch")
    public R<Void> batchUpdateStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        // 提取参数
        List<Long> ids = (List<Long>) body.get("ids");
        String status = (String) body.get("status");

        // 构建批量更新条件
        LambdaUpdateWrapper<DocNote> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(DocNote::getId, ids);

        // 根据状态值设置对应的状态和审核状态
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
     * <p>
     * 批量修改笔记的大类分类。
     * </p>
     *
     * @param body 请求体（ids: 笔记ID列表, category: 目标分类）
     * @return 是否成功
     */
    @Log(title = "笔记管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "批量迁移分类")
    @PutMapping("/category/batch")
    public R<Void> batchMoveCategory(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        // 提取参数
        List<Long> ids = (List<Long>) body.get("ids");
        String category = (String) body.get("category");

        // 构建批量更新条件
        LambdaUpdateWrapper<DocNote> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(DocNote::getId, ids);
        updateWrapper.set(DocNote::getBroadCode, category);

        return docNoteService.update(updateWrapper) ? R.ok() : R.fail();
    }

    /**
     * 切换置顶状态
     * <p>
     * 切换笔记的置顶状态（置顶/取消置顶）。
     * </p>
     *
     * @param id 笔记ID
     * @return 是否成功
     */
    @Log(title = "笔记管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "切换置顶状态")
    @PutMapping("/{id}/pinned")
    public R<Void> togglePinned(@PathVariable Long id) {
        // 1. 查询当前笔记信息
        DocNote note = docNoteService.getById(id);
        if (note == null) {
            return R.fail("笔记不存在");
        }

        // 2. 构建更新条件，切换置顶状态
        LambdaUpdateWrapper<DocNote> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocNote::getId, id);
        updateWrapper.set(DocNote::getIsPinned, note.getIsPinned() == 1 ? 0 : 1);

        return docNoteService.update(updateWrapper) ? R.ok() : R.fail();
    }

    /**
     * 切换推荐状态
     * <p>
     * 切换笔记的推荐状态（推荐/取消推荐）。
     * </p>
     *
     * @param id 笔记ID
     * @return 是否成功
     */
    @Log(title = "笔记管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "切换推荐状态")
    @PutMapping("/{id}/recommended")
    public R<Void> toggleRecommended(@PathVariable Long id) {
        // 1. 查询当前笔记信息
        DocNote note = docNoteService.getById(id);
        if (note == null) {
            return R.fail("笔记不存在");
        }

        // 2. 构建更新条件，切换推荐状态
        LambdaUpdateWrapper<DocNote> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocNote::getId, id);
        updateWrapper.set(DocNote::getIsRecommended, note.getIsRecommended() == 1 ? 0 : 1);

        return docNoteService.update(updateWrapper) ? R.ok() : R.fail();
    }
}
