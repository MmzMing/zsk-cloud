package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.document.domain.vo.DocCategoryVO;
import com.zsk.document.domain.vo.DocTagVO;
import com.zsk.document.service.IDocNoteCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文档分类标签Controller
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Tag(name = "文档分类标签")
@RestController
@RequestMapping("/docNoteCategory")
@RequiredArgsConstructor
public class DocNoteCategoryController {

    private final IDocNoteCategoryService docNoteCategoryService;

    /**
     * 获取文档分类列表
     *
     * @return 分类列表
     */
    @Operation(summary = "获取文档分类列表")
    @GetMapping("/category/list")
    public R<List<DocCategoryVO>> getCategoryList() {
        return R.ok(docNoteCategoryService.getCategoryList());
    }

    /**
     * 获取文档标签列表
     *
     * @return 标签列表
     */
    @Operation(summary = "获取文档标签列表")
    @GetMapping("/tag/list")
    public R<List<DocTagVO>> getTagList() {
        return R.ok(docNoteCategoryService.getTagList());
    }
}
