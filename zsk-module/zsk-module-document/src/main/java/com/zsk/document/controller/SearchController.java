package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.dto.SearchRequestDto;
import com.zsk.document.domain.vo.SearchResultVo;
import com.zsk.document.service.ISearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 全局搜索控制器
 * <p>
 * 提供全站内容搜索的 HTTP 接口，仅处理请求接收、参数校验和响应封装。
 * 所有业务逻辑（搜索、排序、分页、统计数据聚合）均委托给 {@link ISearchService} 处理。
 * </p>
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-25
 */
@Tag(name = "全局搜索")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    /**
     * 搜索服务
     * <p>
     * 负责全站搜索的核心业务逻辑，包括视频/笔记搜索、统计数据聚合、排序和分页。
     * </p>
     */
    private final ISearchService searchService;

    /**
     * 全站搜索
     * <p>
     * 根据关键字、类型、分类等条件搜索视频和笔记内容。
     * 支持按热门、点赞等方式排序，使用通用分页组件返回结果。
     * </p>
     *
     * @param searchRequest 搜索请求参数（包含关键字、类型、排序、分类等筛选条件）
     * @param pageQuery     分页查询参数（包含页码、每页大小）
     * @return 搜索结果分页列表（包含完整的统计信息和格式化文本）
     */
    @Operation(summary = "全站搜索")
    @GetMapping("/all")
    public R<PageResult<SearchResultVo>> searchAll(
            SearchRequestDto searchRequest,
            PageQuery pageQuery) {
        PageResult<SearchResultVo> result = searchService.searchAll(searchRequest, pageQuery);
        return R.ok(result);
    }
}
