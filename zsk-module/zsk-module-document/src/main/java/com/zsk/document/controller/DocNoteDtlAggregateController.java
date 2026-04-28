package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.log.annotation.Log;
import com.zsk.common.log.enums.BusinessType;
import com.zsk.document.domain.dto.DocNoteFullDTO;
import com.zsk.document.domain.vo.DocNoteFullVO;
import com.zsk.document.service.IDocNoteDtlAggregateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 笔记聚合Controller
 * <p>
 * 提供笔记元信息和正文的聚合操作，在事务边界内保证 document_note 和 document_note_dtl 两张表的数据一致性。
 * 遵循 Controller 层约束：仅处理 HTTP 请求、参数校验、结果封装，不编写复杂业务逻辑。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-26
 */
@Tag(name = "笔记聚合管理")
@RestController
@RequestMapping("/docNoteDtlAggregate")
@RequiredArgsConstructor
public class DocNoteDtlAggregateController {

    /**
     * 笔记聚合Service
     */
    private final IDocNoteDtlAggregateService docNoteDtlAggregateService;

    /**
     * 创建笔记（元信息 + 正文）
     * <p>
     * 在一个事务中同时创建笔记元信息和正文内容。
     * docNote.id 由雪花算法自动生成，创建后回填到实体中。
     * </p>
     *
     * @param dto 笔记全量DTO（包含元信息对象和正文内容）
     * @return 是否成功
     */
    @Log(title = "笔记聚合管理", businessType = BusinessType.INSERT)
    @Operation(summary = "创建笔记（元信息 + 正文）")
    @PostMapping("/full")
    public R<Boolean> createFull(@RequestBody @Valid DocNoteFullDTO dto) {
        return R.ok(docNoteDtlAggregateService.createNoteFull(dto));
    }

    /**
     * 获取笔记全量信息
     * <p>
     * 同时返回元信息（含作者、封面、统计）和正文内容，供阅读页一次请求拿到全量数据。
     * </p>
     *
     * @param id 笔记ID
     * @return 笔记全量视图对象（元信息 + 正文）
     */
    @Log(title = "笔记聚合管理", businessType = BusinessType.QUERY)
    @Operation(summary = "获取笔记全量信息")
    @GetMapping("/{id}/full")
    public R<DocNoteFullVO> getFull(@PathVariable("id") Long id) {
        DocNoteFullVO fullVO = docNoteDtlAggregateService.getNoteFull(id);
        if (fullVO == null) {
            return R.fail("笔记不存在");
        }
        return R.ok(fullVO);
    }

    /**
     * 全量更新笔记（元信息 + 正文）
     * <p>
     * 在一个事务中同时更新笔记元信息和正文内容。
     * dto.docNote.id 必填，用于定位要更新的笔记记录。
     * </p>
     *
     * @param dto 笔记全量DTO（包含元信息对象和正文内容）
     * @return 是否成功
     */
    @Log(title = "笔记聚合管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "全量更新笔记（元信息 + 正文）")
    @PutMapping("/{id}/full")
    public R<Boolean> updateFull(@PathVariable("id") Long id,
                                 @RequestBody @Valid DocNoteFullDTO dto) {
        dto.getDocNote().setId(id);
        return R.ok(docNoteDtlAggregateService.updateNoteFull(dto));
    }

    /**
     * 级联删除笔记（元信息 + 正文）
     * <p>
     * 同时逻辑删除 document_note 和 document_note_dtl 两张表的记录。
     * 两个操作在同一事务中执行，保证数据一致性。
     * </p>
     *
     * @param id 笔记ID
     * @return 是否成功
     */
    @Log(title = "笔记聚合管理", businessType = BusinessType.DELETE)
    @Operation(summary = "级联删除笔记（元信息 + 正文）")
    @DeleteMapping("/{id}/full")
    public R<Boolean> removeFull(@PathVariable("id") Long id) {
        return R.ok(docNoteDtlAggregateService.removeNoteFull(id));
    }
}
