package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.log.annotation.Log;
import com.zsk.common.log.enums.BusinessType;
import com.zsk.document.domain.DocNoteDtl;
import com.zsk.document.domain.dto.DocNoteDtlDTO;
import com.zsk.document.service.IDocNoteDtlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 笔记详情Controller
 * <p>
 * 提供笔记详情的 CRUD 操作和 Markdown 解析功能。
 * 遵循 Controller 层约束：仅处理 HTTP 请求、参数校验、结果封装，不编写复杂业务逻辑。
 * 所有方法返回统一 Result 对象，使用构造器注入 Service。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-25
 */
@Tag(name = "笔记详情管理")
@RestController
@RequestMapping("/docNoteDtl")
@RequiredArgsConstructor
public class DocNoteDtlController {

    /**
     * 笔记详情Service
     * <p>
     * 通过构造器注入（Constructor Injection），符合依赖注入规范。
     * 用于调用 Service 层的业务逻辑方法。
     * </p>
     */
    private final IDocNoteDtlService docNoteDtlService;

    /**
     * 根据笔记ID获取笔记详情
     * <p>
     * 通过笔记ID（noteId）查询对应的笔记内容详情。
     * 由于 note_id 有唯一索引，查询结果最多只有一条记录。
     * 若笔记不存在，返回失败响应。
     * </p>
     *
     * @param noteId 笔记ID（路径参数）
     * @return 笔记详情对象
     */
    @Log(title = "笔记详情管理", businessType = BusinessType.QUERY)
    @Operation(summary = "根据笔记ID获取笔记详情")
    @Parameter(name = "noteId", description = "笔记ID", required = true)
    @GetMapping("/{noteId}")
    public R<DocNoteDtl> getByNoteId(@PathVariable("noteId") Long noteId) {
        // 调用 Service 层根据笔记ID查询详情
        DocNoteDtl dtl = docNoteDtlService.getByNoteId(noteId);
        if (dtl == null) {
            return R.fail("笔记详情不存在");
        }
        return R.ok(dtl);
    }

    /**
     * 新增笔记详情
     * <p>
     * 接收前端传入的笔记内容数据，保存到数据库。
     * 使用 @Valid 注解触发参数校验（noteId 不能为空，content 不能为空）。
     * 若该笔记ID已存在详情记录，会更新原有记录（幂等性设计）。
     * </p>
     *
     * @param dto 笔记详情DTO（包含 noteId 和 content）
     * @return 是否成功
     */
    @Log(title = "笔记详情管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增笔记详情")
    @PostMapping
    public R<Boolean> add(@RequestBody @Valid DocNoteDtlDTO dto) {
        // 调用 Service 层保存或更新笔记详情
        return R.ok(docNoteDtlService.saveOrUpdateByNoteId(dto));
    }

    /**
     * 删除笔记详情
     * <p>
     * 根据笔记ID删除对应的笔记内容详情。
     * 使用逻辑删除方式，保留数据便于恢复和审计。
     * 支持批量删除，传入多个ID用逗号分隔。
     * </p>
     *
     * @param noteIds 笔记ID列表（路径参数，多个ID用逗号分隔）
     * @return 是否成功
     */
    @Log(title = "笔记详情管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除笔记详情")
    @DeleteMapping("/{noteIds}")
    public R<Boolean> remove(@PathVariable List<Long> noteIds) {
        // 调用 Service 层批量删除笔记详情
        return R.ok(docNoteDtlService.removeByNoteIds(noteIds));
    }

    /**
     * 上传MD文件并保存到数据库
     * <p>
     * 接收前端上传的Markdown文件，解析文件内容并保存到数据库中。
     * 支持的文件格式：.md、.markdown
     * 文件大小限制：根据Spring配置决定（默认10MB）
     * </p>
     *
     * @param noteId 笔记ID（关联document_note.id）
     * @param file   上传的MD文件
     * @return 是否成功
     */
    @Log(title = "笔记详情管理", businessType = BusinessType.INSERT)
    @Operation(summary = "上传MD文件")
    @Parameter(name = "noteId", description = "笔记ID", required = true)
    @Parameter(name = "file", description = "MD文件（.md或.markdown格式）", required = true)
    @PostMapping("/upload")
    @Validated
    public R<Boolean> uploadMdFile(
            @RequestParam("noteId") @NotNull(message = "笔记ID不能为空") Long noteId,
            @RequestParam("file") MultipartFile file) {
        // 调用 Service 层上传并保存MD文件
        return R.ok(docNoteDtlService.uploadMdFile(noteId, file));
    }
}
