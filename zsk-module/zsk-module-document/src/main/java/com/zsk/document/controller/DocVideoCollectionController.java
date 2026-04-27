package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocVideoCollection;
import com.zsk.document.domain.dto.CollectionAddVideoDTO;
import com.zsk.document.domain.dto.CollectionVideoSortDTO;
import com.zsk.document.domain.vo.DocVideoCollectionDtlVo;
import com.zsk.document.domain.vo.DocVideoCollectionVo;
import com.zsk.document.service.IDocVideoCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 视频合集Controller
 * <p>
 * 提供视频合集的RESTful API接口，包括：
 * - 合集的查询（列表、分页、详情）
 * - 合集的管理（创建、修改、删除）
 * - 合集内视频的管理（添加、移除、排序）
 * </p>
 * <p>
 * 所有接口均通过 SecurityUtils 获取当前登录用户，确保用户只能操作自己的合集。
 * 返回统一响应对象 {@link R<T>}，异常由全局异常处理器统一处理。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-28
 */
@Tag(name = "视频合集管理")
@RestController
@RequestMapping("/docVideoCollection")
@RequiredArgsConstructor
public class DocVideoCollectionController {

    /**
     * 视频合集Service
     */
    private final IDocVideoCollectionService docVideoCollectionService;

    /**
     * 查询当前用户的合集列表
     * <p>
     * 获取当前登录用户创建的所有视频合集（未删除）。
     * 支持按状态筛选（公开/私密），默认按排序值降序、创建时间降序排列。
     * 返回结果包含封面文件URL信息。
     * </p>
     *
     * @param docVideoCollection 查询条件（可选：status状态筛选）
     * @return 合集列表
     */
    @Operation(summary = "查询我的合集列表")
    @GetMapping("/list")
    public R<List<DocVideoCollectionVo>> list(DocVideoCollection docVideoCollection) {
        return R.ok(docVideoCollectionService.listByUser(docVideoCollection));
    }

    /**
     * 分页查询当前用户的合集列表
     * <p>
     * 分页获取当前登录用户创建的视频合集（未删除）。
     * 支持按状态筛选（公开/私密），默认按排序值降序、创建时间降序排列。
     * 返回结果包含封面文件URL信息。
     * </p>
     *
     * @param docVideoCollection 查询条件（可选：status状态筛选）
     * @param pageQuery          分页参数（pageNum、pageSize）
     * @return 分页结果
     */
    @Operation(summary = "分页查询我的合集列表")
    @GetMapping("/page")
    public R<PageResult<DocVideoCollectionVo>> page(DocVideoCollection docVideoCollection, PageQuery pageQuery) {
        return R.ok(docVideoCollectionService.pageByUser(docVideoCollection, pageQuery));
    }

    /**
     * 获取合集详情（包含视频列表）
     * <p>
     * 根据合集ID获取详情信息，同时返回合集中的所有视频列表。
     * 视频列表按用户在合集中设定的排序顺序排列，仅包含状态正常且审核通过的视频。
     * 每个视频包含视频文件和缩略图文件信息。
     * </p>
     *
     * @param id 合集ID
     * @return 合集详情（包含视频列表）
     */
    @Operation(summary = "获取合集详情")
    @GetMapping("/{id}")
    public R<DocVideoCollectionDtlVo> getInfo(@PathVariable("id") Long id) {
        return R.ok(docVideoCollectionService.getCollectionDetail(id));
    }

    /**
     * 创建合集
     * <p>
     * 为当前登录用户创建一个新的视频合集。
     * 必填字段：collectionName（合集名称）。
     * 可选字段：description（描述）、coverFileId（封面）、sortOrder（排序值）、status（状态）。
     * 自动设置用户ID、初始化视频数量为0。
     * </p>
     *
     * @param docVideoCollection 合集信息
     * @return 新创建合集的ID
     */
    @Operation(summary = "创建合集")
    @PostMapping
    public R<Long> add(@RequestBody DocVideoCollection docVideoCollection) {
        return R.ok(docVideoCollectionService.createCollection(docVideoCollection));
    }

    /**
     * 修改合集信息
     * <p>
     * 修改指定合集的基本信息（名称、描述、封面、排序、状态等）。
     * 只能修改自己创建的合集，禁止修改用户ID和视频数量。
     * </p>
     *
     * @param docVideoCollection 合集信息（id必填）
     * @return 是否成功
     */
    @Operation(summary = "修改合集")
    @PutMapping
    public R<Boolean> edit(@RequestBody DocVideoCollection docVideoCollection) {
        return R.ok(docVideoCollectionService.updateCollection(docVideoCollection));
    }

    /**
     * 删除合集（支持批量）
     * <p>
     * 批量删除合集及其关联的视频项，采用软删除策略。
     * 只能删除自己创建的合集。
     * </p>
     *
     * @param ids 合集ID列表（支持多个，如：1,2,3）
     * @return 是否成功
     */
    @Operation(summary = "删除合集")
    @DeleteMapping("/{ids}")
    public R<Boolean> remove(@PathVariable List<Long> ids) {
        return R.ok(docVideoCollectionService.removeCollectionByIds(ids));
    }

    /**
     * 批量添加视频到合集
     * <p>
     * 将多个视频添加到指定合集中，自动过滤已存在的视频（防重复）。
     * 新添加的视频默认排在合集末尾。
     * 只能操作自己创建的合集。
     * </p>
     *
     * @param collectionId 合集ID
     * @param dto          视频ID列表请求体
     * @return 是否成功
     */
    @Operation(summary = "批量添加视频到合集")
    @PostMapping("/{id}/videos")
    public R<Boolean> addVideos(@PathVariable("id") Long collectionId,
                                @Valid @RequestBody CollectionAddVideoDTO dto) {
        return R.ok(docVideoCollectionService.addVideosToCollection(collectionId, dto.getVideoIds()));
    }

    /**
     * 批量从合集移除视频
     * <p>
     * 将多个视频从指定合集中移除，采用软删除策略。
     * 只能操作自己创建的合集。
     * </p>
     *
     * @param collectionId 合集ID
     * @param dto          视频ID列表请求体
     * @return 是否成功
     */
    @Operation(summary = "批量从合集移除视频")
    @DeleteMapping("/{id}/videos")
    public R<Boolean> removeVideos(@PathVariable("id") Long collectionId,
                                   @Valid @RequestBody CollectionAddVideoDTO dto) {
        return R.ok(docVideoCollectionService.removeVideosFromCollection(collectionId, dto.getVideoIds()));
    }

    /**
     * 调整合集中视频排序
     * <p>
     * 根据传入的视频ID列表顺序，重新设置合集中视频的播放顺序。
     * 列表中的顺序即为最终排序结果：索引0的视频排在第一位，索引1排在第二位，以此类推。
     * 只能操作自己创建的合集。
     * </p>
     *
     * @param collectionId 合集ID
     * @param sortDTO      视频ID排序列表请求体
     * @return 是否成功
     */
    @Operation(summary = "调整合集视频排序")
    @PutMapping("/{id}/videos/sort")
    public R<Boolean> sortVideos(@PathVariable("id") Long collectionId,
                                 @Valid @RequestBody CollectionVideoSortDTO sortDTO) {
        return R.ok(docVideoCollectionService.sortCollectionVideos(collectionId, sortDTO));
    }

}
