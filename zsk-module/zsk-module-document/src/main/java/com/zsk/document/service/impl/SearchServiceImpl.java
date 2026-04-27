package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocVideo;
import com.zsk.document.domain.dto.SearchRequestDto;
import com.zsk.document.domain.vo.DocVideoFileVo;
import com.zsk.document.domain.vo.DocVideoListVo;
import com.zsk.document.domain.vo.SearchResultVo;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.enums.CacheDocViewTypeEnum;
import com.zsk.document.service.ICacheDocCollectService;
import com.zsk.document.service.ICacheDocLikeService;
import com.zsk.document.service.ICacheDocViewService;
import com.zsk.document.service.IDocNoteCommentService;
import com.zsk.document.service.IDocNoteDtlService;
import com.zsk.document.service.IDocNoteService;
import com.zsk.document.service.IDocVideoCommentService;
import com.zsk.document.service.IDocVideoService;
import com.zsk.document.service.ISearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 全局搜索服务实现类
 * <p>
 * 提供全站内容搜索的业务逻辑实现，支持视频、笔记等多种类型资源的统一搜索。
 * 核心业务包括：
 * 1. 多类型资源搜索（视频、笔记）
 * 2. 统计数据聚合（从 Redis 缓存获取浏览量、点赞数、收藏数）
 * 3. 评论数查询（从数据库获取）
 * 4. 结果排序与分页
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements ISearchService {

    /**
     * 视频服务
     */
    private final IDocVideoService videoService;

    /**
     * 笔记服务
     */
    private final IDocNoteService noteService;

    /**
     * 视频评论服务
     */
    private final IDocVideoCommentService videoCommentService;

    /**
     * 笔记评论服务
     */
    private final IDocNoteCommentService noteCommentService;

    /**
     * 笔记详情服务（用于获取笔记内容）
     */
    private final IDocNoteDtlService noteDtlService;

    /**
     * 缓存浏览服务
     */
    private final ICacheDocViewService cacheDocViewService;

    /**
     * 缓存点赞服务
     */
    private final ICacheDocLikeService cacheDocLikeService;

    /**
     * 缓存收藏服务
     */
    private final ICacheDocCollectService cacheDocCollectService;

    /**
     * 全站搜索
     * <p>
     * 根据关键字、类型、分类等条件搜索视频和笔记内容。
     * 支持按热门、点赞等方式排序，使用通用分页组件返回结果。
     * 搜索流程：
     * 1. 解析搜索参数（关键字、类型、排序、分类）
     * 2. 根据类型筛选搜索范围（全部/视频/笔记）
     * 3. 分别查询视频和笔记数据
     * 4. 从 Redis 缓存获取统计数据，从数据库获取评论数
     * 5. 构建搜索结果 VO
     * 6. 按指定方式排序
     * 7. 执行内存分页并返回结果
     * </p>
     *
     * @param searchRequest 搜索请求参数（包含关键字、类型、排序、分类等）
     * @param pageQuery     分页查询参数（包含页码、每页大小）
     * @return 搜索结果分页列表（包含完整的统计信息和格式化文本）
     */
    @Override
    public PageResult<SearchResultVo> searchAll(SearchRequestDto searchRequest, PageQuery pageQuery) {
        log.info("执行全站搜索, keyword={}, type={}, sort={}, category={}",
                searchRequest.getKeyword(), searchRequest.getType(),
                searchRequest.getSort(), searchRequest.getCategory());

        // 1. 获取搜索参数
        String keyword = searchRequest.getKeyword();
        String type = searchRequest.getType();
        String sort = searchRequest.getSort();
        String category = searchRequest.getCategory();

        // 2. 初始化结果列表
        List<SearchResultVo> allResults = new ArrayList<>();

        // 3. 搜索视频
        if ("all".equals(type) || "video".equals(type)) {
            log.debug("开始搜索视频内容");
            List<SearchResultVo> videoResults = searchVideos(keyword, category);
            allResults.addAll(videoResults);
            log.debug("视频搜索完成, 共{}条", videoResults.size());
        }

        // 4. 搜索笔记
        if ("all".equals(type) || "document".equals(type)) {
            log.debug("开始搜索笔记内容");
            List<SearchResultVo> docResults = searchDocuments(keyword, category);
            allResults.addAll(docResults);
            log.debug("笔记搜索完成, 共{}条", docResults.size());
        }

        // 5. 排序结果
        sortResults(allResults, sort);

        // 6. 内存分页
        long total = allResults.size();
        int fromIndex = (int) ((pageQuery.getPageNum() - 1) * pageQuery.getPageSize());
        int toIndex = (int) Math.min(fromIndex + pageQuery.getPageSize(), total);
        List<SearchResultVo> pageResults = fromIndex < total
                ? allResults.subList(fromIndex, toIndex)
                : new ArrayList<>();

        log.info("全站搜索完成, 总结果{}条, 当前页{}条", total, pageResults.size());

        // 7. 构建分页结果
        return PageResult.of(
                pageResults,
                total,
                pageQuery.getPageNum(),
                pageQuery.getPageSize()
        );
    }

    /**
     * 搜索视频
     * <p>
     * 根据关键字和分类筛选视频内容，从 Redis 缓存获取统计数据。
     * 搜索流程：
     * 1. 构建查询条件（状态、关键字、分类）
     * 2. 执行查询获取视频列表（带文件URL）
     * 3. 过滤关键字匹配（因为 listWithFileUrl 不支持模糊查询）
     * 4. 构建搜索结果 VO（包含 Redis 缓存统计数据）
     * </p>
     *
     * @param keyword  搜索关键字（可为空）
     * @param category 分类筛选（可为空）
     * @return 视频搜索结果列表（包含完整的统计信息）
     */
    private List<SearchResultVo> searchVideos(String keyword, String category) {
        // 1. 构建查询条件对象
        DocVideo query = new DocVideo();
        query.setDeleted(0);
        query.setStatus(1);
        if (StringUtils.hasText(keyword)) {
            query.setVideoTitle(keyword);
            query.setFileContent(keyword);
        }
        if (StringUtils.hasText(category)) {
            query.setBroadCode(category);
        }

        // 2. 执行查询（带文件URL）
        List<DocVideoListVo> videos = videoService.listWithFileUrl(query);

        // 3. 构建搜索结果VO列表
        List<SearchResultVo> results = new ArrayList<>();
        for (DocVideoListVo video : videos) {
            // 过滤关键字匹配（因为listWithFileUrl不支持模糊查询）
            if (StringUtils.hasText(keyword)) {
                boolean matches = (video.getVideoTitle() != null && video.getVideoTitle().contains(keyword))
                        || (video.getFileContent() != null && video.getFileContent().contains(keyword));
                if (!matches) {
                    continue;
                }
            }
            SearchResultVo vo = buildVideoSearchResult(video);
            results.add(vo);
        }

        return results;
    }

    /**
     * 搜索笔记
     * <p>
     * 根据关键字和分类筛选笔记内容，从 Redis 缓存获取统计数据。
     * 搜索流程：
     * 1. 构建 MyBatis-Plus 查询条件（状态、关键字模糊查询、分类）
     * 2. 执行查询获取笔记列表
     * 3. 构建搜索结果 VO（包含笔记详情内容和 Redis 缓存统计数据）
     * </p>
     *
     * @param keyword  搜索关键字（可为空）
     * @param category 分类筛选（可为空）
     * @return 笔记搜索结果列表（包含完整的统计信息）
     */
    private List<SearchResultVo> searchDocuments(String keyword, String category) {
        // 1. 构建查询条件：查询未删除且状态正常的笔记
        LambdaQueryWrapper<DocNote> wrapper = new LambdaQueryWrapper<DocNote>()
                .eq(DocNote::getDeleted, 0)
                .eq(DocNote::getStatus, 1);

        // 2. 添加关键字模糊查询条件（只搜索标题和描述）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(DocNote::getNoteName, keyword)
                    .or()
                    .like(DocNote::getDescription, keyword)
            );
        }

        // 3. 添加分类筛选条件
        if (StringUtils.hasText(category)) {
            wrapper.eq(DocNote::getBroadCode, category);
        }

        // 4. 执行查询
        List<DocNote> notes = noteService.list(wrapper);

        // 5. 构建搜索结果VO列表
        List<SearchResultVo> results = new ArrayList<>();
        for (DocNote note : notes) {
            SearchResultVo vo = buildNoteSearchResult(note);
            results.add(vo);
        }

        return results;
    }

    /**
     * 构建视频搜索结果VO
     * <p>
     * 将视频列表VO转换为搜索结果VO，从 Redis 缓存获取浏览量、点赞数、收藏数，从数据库获取评论数。
     * 数据填充流程：
     * 1. 填充基础信息（ID、类型、标题、描述、分类、作者）
     * 2. 填充封面URL（从文件信息中获取）
     * 3. 解析标签（逗号分隔转列表）
     * 4. 从 Redis 获取浏览量、点赞数、收藏数
     * 5. 从数据库获取评论数
     * 6. 构建统计信息文本
     * </p>
     *
     * @param video 视频列表VO（包含文件信息）
     * @return 搜索结果VO（包含完整的统计信息）
     */
    private SearchResultVo buildVideoSearchResult(DocVideoListVo video) {
        SearchResultVo vo = new SearchResultVo();
        vo.setId(String.valueOf(video.getId()));
        vo.setType("video");
        vo.setTitle(video.getVideoTitle());
        vo.setDescription(video.getFileContent() != null ? video.getFileContent() : "");
        vo.setCategory(video.getBroadCode());

        // 获取封面URL（从文件信息中获取）
        DocVideoFileVo videoFile = video.getVideoFile();
        if (videoFile != null && videoFile.getThumbnail() != null) {
            vo.setThumbnail(videoFile.getThumbnail().getFileUrl());
        }

        vo.setAuthorId(String.valueOf(video.getUserId()));
        vo.setAuthor("作者" + video.getUserId());

        // 解析标签
        if (video.getTags() != null && !video.getTags().isEmpty()) {
            vo.setTags(Arrays.asList(video.getTags().split(",")));
        } else {
            vo.setTags(new ArrayList<>());
        }

        // 从 Redis 缓存获取浏览量
        Long viewCount = cacheDocViewService.getViewCount(CacheDocViewTypeEnum.VIDEO.getCode(), video.getId());
        vo.setPlayCount(viewCount);

        // 从 Redis 缓存获取点赞数
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.VIDEO.getCode(), video.getId());

        // 从 Redis 缓存获取收藏数
        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.VIDEO.getCode(), video.getId());
        vo.setFavoriteCount(collectCount);

        // 从数据库获取评论数
        Long commentCount = videoCommentService.count(
                new LambdaQueryWrapper<com.zsk.document.domain.DocVideoComment>()
                        .eq(com.zsk.document.domain.DocVideoComment::getDeleted, 0)
                        .eq(com.zsk.document.domain.DocVideoComment::getVideoId, String.valueOf(video.getId()))
        );
        vo.setCommentCount(commentCount);

        // 构建统计信息文本
        vo.setStats(buildStatsText(viewCount, likeCount, collectCount, commentCount));

        return vo;
    }

    /**
     * 构建笔记搜索结果VO
     * <p>
     * 将笔记实体转换为搜索结果VO，从 Redis 缓存获取浏览量、点赞数、收藏数，从数据库获取评论数。
     * 数据填充流程：
     * 1. 填充基础信息（ID、类型、标题、分类、作者）
     * 2. 从笔记详情表获取内容作为描述
     * 3. 从 Redis 获取浏览量、点赞数、收藏数
     * 4. 从数据库获取评论数
     * 5. 构建统计信息文本
     * </p>
     *
     * @param note 笔记实体
     * @return 搜索结果VO（包含完整的统计信息）
     */
    private SearchResultVo buildNoteSearchResult(DocNote note) {
        SearchResultVo vo = new SearchResultVo();
        vo.setId(String.valueOf(note.getId()));
        vo.setType("document");
        vo.setTitle(note.getNoteName());

        // 从笔记详情表获取内容
        String content = "";
        if (note.getId() != null) {
            com.zsk.document.domain.DocNoteDtl dtl = noteDtlService.getByNoteId(note.getId());
            if (dtl != null && dtl.getContent() != null) {
                content = dtl.getContent();
            }
        }
        vo.setDescription(content);

        vo.setCategory(note.getBroadCode());
        vo.setAuthorId(String.valueOf(note.getUserId()));
        vo.setAuthor("作者" + note.getUserId());
        vo.setTags(new ArrayList<>());

        // 从 Redis 缓存获取浏览量
        Long viewCount = cacheDocViewService.getViewCount(CacheDocViewTypeEnum.NOTE.getCode(), note.getId());
        vo.setReadCount(viewCount);

        // 从 Redis 缓存获取点赞数
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE.getCode(), note.getId());

        // 从 Redis 缓存获取收藏数
        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.NOTE.getCode(), note.getId());
        vo.setFavoriteCount(collectCount);

        // 从数据库获取评论数
        Long commentCount = noteCommentService.count(
                new LambdaQueryWrapper<com.zsk.document.domain.DocNoteComment>()
                        .eq(com.zsk.document.domain.DocNoteComment::getDeleted, 0)
                        .eq(com.zsk.document.domain.DocNoteComment::getNoteId, note.getId())
        );
        vo.setCommentCount(commentCount);

        // 构建统计信息文本
        vo.setStats(buildStatsText(viewCount, likeCount, collectCount, commentCount));

        return vo;
    }

    /**
     * 构建统计信息文本
     * <p>
     * 将浏览量、点赞数、收藏数、评论数格式化为展示文本。
     * 格式示例："1.2w次浏览 · 3.5k点赞 · 500收藏 · 100评论"
     * </p>
     *
     * @param viewCount    浏览量
     * @param likeCount    点赞数
     * @param collectCount 收藏数
     * @param commentCount 评论数
     * @return 统计信息文本（已格式化的可读字符串）
     */
    private String buildStatsText(Long viewCount, Long likeCount, Long collectCount, Long commentCount) {
        StringBuilder stats = new StringBuilder();
        stats.append(formatCount(viewCount)).append("次浏览 · ");
        stats.append(formatCount(likeCount)).append("点赞 · ");
        stats.append(formatCount(collectCount)).append("收藏 · ");
        stats.append(formatCount(commentCount)).append("评论");
        return stats.toString();
    }

    /**
     * 格式化数量
     * <p>
     * 将大数字格式化为带单位的字符串，便于展示：
     * - 大于等于 10000：显示为 "x.xw" 格式（如 12500 → "1.2w"）
     * - 大于等于 1000：显示为 "x.xk" 格式（如 3500 → "3.5k"）
     * - 小于 1000：显示原数字
     * - null 或负数：显示 "0"
     * </p>
     *
     * @param count 数量
     * @return 格式化后的字符串（如 "1.2w"、"3.5k"、"100"）
     */
    private String formatCount(Long count) {
        if (count == null || count < 0) {
            return "0";
        }
        if (count >= 10000) {
            return String.format("%.1fw", count / 10000.0);
        }
        if (count >= 1000) {
            return String.format("%.1fk", count / 1000.0);
        }
        return String.valueOf(count);
    }

    /**
     * 排序结果
     * <p>
     * 根据排序方式对搜索结果进行排序：
     * - hot：按浏览量降序排序（视频用 playCount，笔记用 readCount）
     * - like：按点赞数降序排序（使用 favoriteCount 字段）
     * - 默认/其他：不排序，保持原始顺序
     * </p>
     *
     * @param results 搜索结果列表
     * @param sort    排序方式（hot/like）
     */
    private void sortResults(List<SearchResultVo> results, String sort) {
        if (sort == null || sort.isEmpty()) {
            return;
        }

        switch (sort) {
            case "hot":
                // 按浏览量降序排序（视频用playCount，笔记用readCount）
                results.sort((a, b) -> {
                    long aCount = (a.getPlayCount() != null ? a.getPlayCount() : 0)
                            + (a.getReadCount() != null ? a.getReadCount() : 0);
                    long bCount = (b.getPlayCount() != null ? b.getPlayCount() : 0)
                            + (b.getReadCount() != null ? b.getReadCount() : 0);
                    return Long.compare(bCount, aCount);
                });
                break;
            case "like":
                // 按点赞数降序排序
                results.sort((a, b) -> {
                    long aCount = a.getFavoriteCount() != null ? a.getFavoriteCount() : 0;
                    long bCount = b.getFavoriteCount() != null ? b.getFavoriteCount() : 0;
                    return Long.compare(bCount, aCount);
                });
                break;
            default:
                break;
        }
    }
}
