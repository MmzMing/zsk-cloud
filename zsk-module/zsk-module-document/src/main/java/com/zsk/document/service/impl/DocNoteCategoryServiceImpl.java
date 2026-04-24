package com.zsk.document.service.impl;

import com.zsk.common.core.constant.DictTypeConstants;
import com.zsk.common.core.domain.R;
import com.zsk.document.domain.vo.DocCategoryVO;
import com.zsk.document.domain.vo.DocTagVO;
import com.zsk.document.service.IDocNoteCategoryService;
import com.zsk.system.api.RemoteDictService;
import com.zsk.system.api.domain.SysDictDataApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文档分类标签Service业务层处理
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocNoteCategoryServiceImpl implements IDocNoteCategoryService {

    private final RemoteDictService remoteDictService;

    /**
     * 获取文档分类列表
     *
     * @return 分类列表
     */
    @Override
    public List<DocCategoryVO> getCategoryList() {
        log.info("获取文档分类列表");
        try {
            R<List<SysDictDataApi>> result = remoteDictService.getDictDataByType(DictTypeConstants.DOCUMENT_CATEGORY);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                List<DocCategoryVO> list = buildCategoryTree(result.getData());
                log.info("获取文档分类列表完成, 共{}个分类", list.size());
                return list;
            }
            log.warn("获取文档分类列表失败, 响应异常");
        } catch (Exception e) {
            log.error("获取文档分类列表失败: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * 获取文档标签列表
     *
     * @return 标签列表
     */
    @Override
    public List<DocTagVO> getTagList() {
        log.info("获取文档标签列表");
        try {
            R<List<SysDictDataApi>> result = remoteDictService.getDictDataByType(DictTypeConstants.DOCUMENT_TAG);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                List<DocTagVO> list = result.getData().stream()
                        .map(dict -> {
                            DocTagVO vo = new DocTagVO();
                            vo.setLabel(dict.getDictLabel());
                            vo.setValue(dict.getDictValue());
                            return vo;
                        })
                        .toList();
                log.info("获取文档标签列表完成, 共{}个标签", list.size());
                return list;
            }
            log.warn("获取文档标签列表失败, 响应异常");
        } catch (Exception e) {
            log.error("获取文档标签列表失败: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
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
