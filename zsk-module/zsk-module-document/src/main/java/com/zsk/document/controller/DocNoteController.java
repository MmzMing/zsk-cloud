package com.zsk.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
 * @version 2.0
 * @date 2026-02-15
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
     *
     * @param docNote 查询条件（可选：noteName、broadCode、narrowCode等）
     * @return 笔记列表
     */
    @Log(title = "笔记管理", businessType = BusinessType.QUERY)
    @Operation(summary = "查询笔记列表")
    @GetMapping("/list")
    public R<List<DocNote>> list(DocNote docNote) {
        return R.ok(docNoteService.list(new LambdaQueryWrapper<>(docNote)));
    }

    /**
     * 分页查询笔记列表
     *
     * @param docNote   查询条件（可选）
     * @param pageQuery 分页参数（pageNum、pageSize）
     * @return 分页结果
     */
    @Log(title = "笔记管理", businessType = BusinessType.QUERY)
    @Operation(summary = "分页查询笔记列表")
    @GetMapping("/page")
    public R<PageResult<DocNote>> page(DocNote docNote, PageQuery pageQuery) {
        return R.ok(PageResult.build(docNoteService.page(pageQuery.build(), new LambdaQueryWrapper<>(docNote))));
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
     * 获取笔记交互数据
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
        return R.ok(docNoteInteractionService.getNoteInteraction(id, userId));
    }

    /**
     * 增加笔记浏览量
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
        docNoteInteractionService.incrementViewCount(id, userId);
        return R.ok();
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
        return R.ok(docNoteService.draftList(pageQuery));
    }

    /**
     * 批量更新状态
     *
     * @param body 请求体（ids: 笔记ID列表, status: 目标状态）
     * @return 是否成功
     */
    @Log(title = "笔记管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "批量更新状态")
    @PutMapping("/status/batch")
    public R<Void> batchUpdateStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) body.get("ids");
        String status = (String) body.get("status");
        return docNoteService.batchUpdateStatus(ids, status) ? R.ok() : R.fail();
    }

    /**
     * 批量迁移分类
     *
     * @param body 请求体（ids: 笔记ID列表, category: 目标分类）
     * @return 是否成功
     */
    @Log(title = "笔记管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "批量迁移分类")
    @PutMapping("/category/batch")
    public R<Void> batchMoveCategory(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) body.get("ids");
        String category = (String) body.get("category");
        return docNoteService.batchMoveCategory(ids, category) ? R.ok() : R.fail();
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
        return docNoteService.togglePinned(id) ? R.ok() : R.fail("笔记不存在");
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
        return docNoteService.toggleRecommended(id) ? R.ok() : R.fail("笔记不存在");
    }
}
