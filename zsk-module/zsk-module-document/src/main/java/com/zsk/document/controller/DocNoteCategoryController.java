package com.zsk.document.controller;

import com.zsk.common.core.constant.DictTypeConstants;
import com.zsk.common.core.domain.R;
import com.zsk.system.api.RemoteDictService;
import com.zsk.system.api.domain.SysDictDataApi;
import com.zsk.document.domain.vo.DocCategoryVO;
import com.zsk.document.domain.vo.DocTagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文档分类标签Controller
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "文档分类标签")
@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
public class DocNoteCategoryController {

    private final RemoteDictService remoteDictService;

    /**
     * 获取文档分类列表
     *
     * @return 分类列表
     */
    @Operation(summary = "获取文档分类列表")
    @GetMapping("/category/list")
    public R<List<DocCategoryVO>> getCategoryList() {
        try {
            var result = remoteDictService.getDictDataByType(DictTypeConstants.DOCUMENT_CATEGORY);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return R.ok(buildCategoryTree(result.getData()));
            }
        } catch (Exception e) {
            // 忽略异常，返回空列表
        }
        return R.ok(new ArrayList<>());
    }

    /**
     * 获取文档标签列表
     *
     * @return 标签列表
     */
    @Operation(summary = "获取文档标签列表")
    @GetMapping("/tag/list")
    public R<List<DocTagVO>> getTagList() {
        try {
            var result = remoteDictService.getDictDataByType(DictTypeConstants.DOCUMENT_TAG);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                List<DocTagVO> tags = result.getData().stream()
                        .map(dict -> {
                            DocTagVO vo = new DocTagVO();
                            vo.setLabel(dict.getDictLabel());
                            vo.setValue(dict.getDictValue());
                            return vo;
                        })
                        .toList();
                return R.ok(tags);
            }
        } catch (Exception e) {
            // 忽略异常，返回空列表
        }
        return R.ok(new ArrayList<>());
    }

    /**
     * 构建分类树形结构
     *
     * @param dictDataList 字典数据列表
     * @return 分类树
     */
    private List<DocCategoryVO> buildCategoryTree(List<SysDictDataApi> dictDataList) {
        Map<String, List<SysDictDataApi>> parentMap = dictDataList.stream()
                .collect(Collectors.groupingBy(d -> d.getParentValue() == null ? "" : d.getParentValue()));

        List<SysDictDataApi> topCategories = parentMap.getOrDefault("", new ArrayList<>());

        return topCategories.stream()
                .map(dict -> {
                    DocCategoryVO vo = new DocCategoryVO();
                    vo.setId(dict.getDictValue());
                    vo.setName(dict.getDictLabel());
                    List<SysDictDataApi> children = parentMap.getOrDefault(dict.getDictValue(), new ArrayList<>());
                    vo.setChildren(children.stream()
                            .map(child -> {
                                DocCategoryVO childVo = new DocCategoryVO();
                                childVo.setId(child.getDictValue());
                                childVo.setName(child.getDictLabel());
                                childVo.setChildren(new ArrayList<>());
                                return childVo;
                            })
                            .toList());
                    return vo;
                })
                .toList();
    }
}
