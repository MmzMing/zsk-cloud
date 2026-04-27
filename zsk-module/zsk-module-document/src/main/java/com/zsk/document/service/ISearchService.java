package com.zsk.document.service;

import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.dto.SearchRequestDto;
import com.zsk.document.domain.vo.SearchResultVo;

/**
 * 全局搜索服务接口
 * <p>
 * 提供全站内容搜索功能，支持视频、笔记等多种类型资源的统一搜索。
 * 搜索结果中的统计数据（浏览量、点赞数、收藏数、评论数）均通过 Redis 缓存服务获取，
 * 不再依赖主表中的统计字段。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-27
 */
public interface ISearchService {

    /**
     * 全站搜索
     * <p>
     * 根据关键字、类型、分类等条件搜索视频和笔记内容。
     * 支持按热门、点赞等方式排序，使用通用分页组件返回结果。
     * 搜索流程：
     * 1. 根据类型筛选搜索范围（全部/视频/笔记）
     * 2. 分别查询视频和笔记数据
     * 3. 从 Redis 缓存获取统计数据（浏览量、点赞数、收藏数）
     * 4. 从数据库获取评论数
     * 5. 按指定方式排序
     * 6. 执行内存分页返回结果
     * </p>
     *
     * @param searchRequest 搜索请求参数（包含关键字、类型、排序、分类等）
     * @param pageQuery     分页查询参数（包含页码、每页大小）
     * @return 搜索结果分页列表（包含完整的统计信息和格式化文本）
     */
    PageResult<SearchResultVo> searchAll(SearchRequestDto searchRequest, PageQuery pageQuery);
}
