package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocFiles;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocNoteDtl;
import com.zsk.document.domain.DocVideo;
import com.zsk.document.domain.dto.SearchRequestDto;
import com.zsk.document.domain.vo.DocHomeSearchResultVo;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.enums.CacheDocViewTypeEnum;
import com.zsk.document.enums.SearchTypeEnum;
import com.zsk.document.mapper.DocFilesMapper;
import com.zsk.document.mapper.DocNoteDtlMapper;
import com.zsk.document.mapper.DocNoteMapper;
import com.zsk.document.mapper.DocVideoMapper;
import com.zsk.document.service.ICacheDocLikeService;
import com.zsk.document.service.ICacheDocViewService;
import com.zsk.document.service.ISearchService;
import com.zsk.system.api.RemoteUserService;
import com.zsk.system.api.domain.SysUserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
 * @version 2.0
 * @date 2026-04-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements ISearchService {

    /**
     * 视频 Mapper（数据库查询）
     */
    private final DocVideoMapper videoMapper;

    /**
     * 笔记 Mapper（数据库查询）
     */
    private final DocNoteMapper noteMapper;

    /**
     * 笔记详情 Mapper（数据库查询）
     */
    private final DocNoteDtlMapper noteDtlMapper;

    /**
     * 文件 Mapper（数据库查询）
     */
    private final DocFilesMapper docFilesMapper;

    /**
     * 缓存浏览服务
     */
    private final ICacheDocViewService cacheDocViewService;

    /**
     * 缓存点赞服务
     */
    private final ICacheDocLikeService cacheDocLikeService;

    /**
     * 远程用户服务
     */
    private final RemoteUserService remoteUserService;

    /**
     * 视频状态：正常
     */
    private static final Integer VIDEO_STATUS_NORMAL = 1;

    /**
     * 视频审核状态：通过
     */
    private static final Integer VIDEO_AUDIT_STATUS_PASS = 1;

    /**
     * 笔记状态：正常
     */
    private static final Integer NOTE_STATUS_NORMAL = 1;

    /**
     * 笔记审核状态：通过
     */
    private static final Integer NOTE_AUDIT_STATUS_PASS = 1;

    /**
     * 删除标记：正常
     */
    private static final Integer DELETED_NORMAL = 0;

    /**
     * 全站搜索
     * <p>
     * 根据关键字、类型、分类等条件搜索视频和笔记内容。
     * 支持按热门、点赞等方式排序，使用通用分页组件返回结果。
     * 搜索流程：
     * 1. 解析搜索参数（关键字、类型、排序、分类）
     * 2. 根据类型筛选搜索范围（全部/视频/笔记）
     * 3. 分别查询视频和笔记数据（仅返回审核通过且状态正常的数据）
     * 4. 从 Redis 缓存获取统计数据，从数据库获取评论数
     * 5. 构建前台搜索结果 VO（仅包含展示所需字段）
     * 6. 按指定方式排序
     * 7. 执行内存分页并返回结果
     * </p>
     *
     * @param searchRequest 搜索请求参数（包含关键字、类型、排序、分类等）
     * @param pageQuery     分页查询参数（包含页码、每页大小）
     * @return 搜索结果分页列表（包含完整的统计信息和格式化文本）
     */
    @Override
    public PageResult<DocHomeSearchResultVo> searchAll(SearchRequestDto searchRequest, PageQuery pageQuery) {
        log.info("执行全站搜索, keyword={}, type={}, sort={}, category={}",
                searchRequest.getKeyword(), searchRequest.getType(),
                searchRequest.getSort(), searchRequest.getCategory());

        // 1. 获取搜索参数
        String keyword = searchRequest.getKeyword();
        String type = searchRequest.getType();
        String sort = searchRequest.getSort();
        String category = searchRequest.getCategory();

        // 2. 初始化结果列表
        List<DocHomeSearchResultVo> allResults = new ArrayList<>();

        // 3. 搜索视频
        if (SearchTypeEnum.matchVideo(type)) {
            log.debug("开始搜索视频内容");
            List<DocHomeSearchResultVo> videoResults = searchVideos(keyword, category);
            allResults.addAll(videoResults);
            log.debug("视频搜索完成, 共{}条", videoResults.size());
        }

        // 4. 搜索笔记
        if (SearchTypeEnum.matchDocument(type)) {
            log.debug("开始搜索笔记内容");
            List<DocHomeSearchResultVo> docResults = searchDocuments(keyword, category);
            allResults.addAll(docResults);
            log.debug("笔记搜索完成, 共{}条", docResults.size());
        }

        // 5. 排序结果
        sortResults(allResults, sort);

        // 6. 内存分页
        long total = allResults.size();
        int fromIndex = (int) ((pageQuery.getPageNum() - 1) * pageQuery.getPageSize());
        int toIndex = (int) Math.min(fromIndex + pageQuery.getPageSize(), total);
        List<DocHomeSearchResultVo> pageResults = fromIndex < total
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
     * 根据关键字和分类筛选视频内容，仅查询审核通过且状态正常的视频。
     * 搜索流程：
     * 1. 构建 MyBatis-Plus 查询条件（未删除、状态正常、审核通过、关键字模糊查询、分类）
     * 2. 执行数据库查询获取视频列表
     * 3. 批量查询封面文件信息
     * 4. 批量查询缓存统计数据（浏览量、点赞数、收藏数）
     * 5. 批量查询评论数
     * 6. 批量查询作者信息
     * 7. 构建前台搜索结果 VO
     * </p>
     *
     * @param keyword  搜索关键字（可为空）
     * @param category 分类筛选（可为空）
     * @return 视频搜索结果列表（仅包含前台展示字段）
     */
    private List<DocHomeSearchResultVo> searchVideos(String keyword, String category) {
        // 1. 构建查询条件：查询未删除、状态正常、审核通过的视频
        LambdaQueryWrapper<DocVideo> wrapper = new LambdaQueryWrapper<DocVideo>()
                .eq(DocVideo::getDeleted, DELETED_NORMAL)
                .eq(DocVideo::getStatus, VIDEO_STATUS_NORMAL)
                .eq(DocVideo::getAuditStatus, VIDEO_AUDIT_STATUS_PASS);

        // 2. 添加关键字模糊查询条件（搜索标题和内容）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(DocVideo::getVideoTitle, keyword)
                    .or()
                    .like(DocVideo::getFileContent, keyword)
            );
        }

        // 3. 添加分类筛选条件
        if (StringUtils.hasText(category)) {
            wrapper.eq(DocVideo::getBroadCode, category);
        }

        // 4. 执行数据库查询
        List<DocVideo> videos = videoMapper.selectList(wrapper);
        if (videos == null || videos.isEmpty()) {
            return new ArrayList<>();
        }

        // 5. 提取视频ID列表，用于批量查询
        List<Long> videoIds = videos.stream()
                .map(DocVideo::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        // 6. 批量查询封面文件信息
        Map<Long, DocFiles> fileMap = buildVideoCoverFileMap(videos);

        // 7. 批量查询统计数据（从 Redis 缓存）
        Map<Long, Long> viewCountMap = cacheDocViewService.getViewCountBatch(
                CacheDocViewTypeEnum.VIDEO.getCode(), videoIds);
        Map<Long, Long> likeCountMap = cacheDocLikeService.getLikeCountBatch(
                CacheDocLikeTypeEnum.VIDEO.getCode(), videoIds);

        // 8. 批量查询作者信息
        Map<Long, SysUserApi> userMap = buildUserMap(videos.stream()
                .map(DocVideo::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList()));

        // 10. 构建搜索结果VO列表
        List<DocHomeSearchResultVo> results = new ArrayList<>();
        for (DocVideo video : videos) {
            Long videoId = video.getId();
            Long userId = video.getUserId();

            Long viewCount = viewCountMap.getOrDefault(videoId, 0L);
            Long likeCount = likeCountMap.getOrDefault(videoId, 0L);

            DocHomeSearchResultVo vo = new DocHomeSearchResultVo();
            vo.setId(String.valueOf(videoId));
            vo.setType("video");
            vo.setTitle(video.getVideoTitle());
            vo.setDescription(video.getFileContent() != null ? video.getFileContent() : "");
            vo.setCategory(video.getBroadCode());

            // 填充封面URL
            DocFiles coverFile = fileMap.get(video.getCoverFileId());
            if (coverFile != null) {
                vo.setThumbnail(coverFile.getUrl());
            }

            // 填充作者信息
            SysUserApi user = userMap.get(userId);
            if (user != null) {
                vo.setAuthorId(String.valueOf(userId));
                vo.setAuthor(user.getNickName() != null ? user.getNickName() : user.getUserName());
            }

            // 解析标签
            if (video.getTags() != null && !video.getTags().isEmpty()) {
                vo.setTags(Arrays.asList(video.getTags().split(",")));
            } else {
                vo.setTags(new ArrayList<>());
            }

            // 填充统计数据
            vo.setPlayCount(viewCount);
            vo.setLikeCount(likeCount);

            results.add(vo);
        }

        return results;
    }

    /**
     * 搜索笔记
     * <p>
     * 根据关键字和分类筛选笔记内容，仅查询审核通过且状态正常的笔记。
     * 搜索流程：
     * 1. 构建 MyBatis-Plus 查询条件（未删除、状态正常、审核通过、关键字模糊查询、分类）
     * 2. 执行数据库查询获取笔记列表
     * 3. 批量查询封面文件信息
     * 4. 批量查询笔记详情内容
     * 5. 批量查询缓存统计数据（浏览量、点赞数、收藏数）
     * 6. 批量查询评论数
     * 7. 批量查询作者信息
     * 8. 构建前台搜索结果 VO
     * </p>
     *
     * @param keyword  搜索关键字（可为空）
     * @param category 分类筛选（可为空）
     * @return 笔记搜索结果列表（仅包含前台展示字段）
     */
    private List<DocHomeSearchResultVo> searchDocuments(String keyword, String category) {
        // 1. 构建查询条件：查询未删除、状态正常、审核通过的笔记
        LambdaQueryWrapper<DocNote> wrapper = new LambdaQueryWrapper<DocNote>()
                .eq(DocNote::getDeleted, DELETED_NORMAL)
                .eq(DocNote::getStatus, NOTE_STATUS_NORMAL)
                .eq(DocNote::getAuditStatus, NOTE_AUDIT_STATUS_PASS);

        // 2. 添加关键字模糊查询条件（搜索标题和描述）
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

        // 4. 执行数据库查询
        List<DocNote> notes = noteMapper.selectList(wrapper);
        if (notes == null || notes.isEmpty()) {
            return new ArrayList<>();
        }

        // 5. 提取笔记ID列表，用于批量查询
        List<Long> noteIds = notes.stream()
                .map(DocNote::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        // 6. 批量查询封面文件信息
        Map<Long, DocFiles> fileMap = buildNoteCoverFileMap(notes);

        // 7. 批量查询笔记详情内容
        Map<Long, String> noteContentMap = buildNoteContentMap(noteIds);

        // 8. 批量查询统计数据（从 Redis 缓存）
        Map<Long, Long> viewCountMap = cacheDocViewService.getViewCountBatch(
                CacheDocViewTypeEnum.NOTE.getCode(), noteIds);
        Map<Long, Long> likeCountMap = cacheDocLikeService.getLikeCountBatch(
                CacheDocLikeTypeEnum.NOTE.getCode(), noteIds);

        // 9. 批量查询作者信息
        Map<Long, SysUserApi> userMap = buildUserMap(notes.stream()
                .map(DocNote::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList()));

        // 11. 构建搜索结果VO列表
        List<DocHomeSearchResultVo> results = new ArrayList<>();
        for (DocNote note : notes) {
            Long noteId = note.getId();
            Long userId = note.getUserId();

            Long viewCount = viewCountMap.getOrDefault(noteId, 0L);
            Long likeCount = likeCountMap.getOrDefault(noteId, 0L);

            DocHomeSearchResultVo vo = new DocHomeSearchResultVo();
            vo.setId(String.valueOf(noteId));
            vo.setType("document");
            vo.setTitle(note.getNoteName());

            // 优先使用描述，若无则使用详情内容
            String description = note.getDescription();
            if (!StringUtils.hasText(description)) {
                description = noteContentMap.getOrDefault(noteId, "");
            }
            vo.setDescription(description != null ? description : "");

            vo.setCategory(note.getBroadCode());

            // 填充封面URL
            DocFiles coverFile = fileMap.get(note.getCoverFileId());
            if (coverFile != null) {
                vo.setThumbnail(coverFile.getUrl());
            }

            // 填充作者信息
            SysUserApi user = userMap.get(userId);
            if (user != null) {
                vo.setAuthorId(String.valueOf(userId));
                vo.setAuthor(user.getNickName() != null ? user.getNickName() : user.getUserName());
            }

            // 解析标签
            if (note.getNoteTags() != null && !note.getNoteTags().isEmpty()) {
                vo.setTags(Arrays.asList(note.getNoteTags().split(",")));
            } else {
                vo.setTags(new ArrayList<>());
            }

            // 填充统计数据
            vo.setReadCount(viewCount);
            vo.setLikeCount(likeCount);

            results.add(vo);
        }

        return results;
    }

    /**
     * 构建视频封面文件映射
     * <p>
     * 批量查询视频封面文件信息，构建封面文件ID到文件对象的映射。
     * </p>
     *
     * @param videos 视频实体列表
     * @return 封面文件ID到文件对象的映射
     */
    private Map<Long, DocFiles> buildVideoCoverFileMap(List<DocVideo> videos) {
        List<Long> coverFileIds = videos.stream()
                .map(DocVideo::getCoverFileId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        return buildFileMap(coverFileIds);
    }

    /**
     * 构建笔记封面文件映射
     * <p>
     * 批量查询笔记封面文件信息，构建封面文件ID到文件对象的映射。
     * </p>
     *
     * @param notes 笔记实体列表
     * @return 封面文件ID到文件对象的映射
     */
    private Map<Long, DocFiles> buildNoteCoverFileMap(List<DocNote> notes) {
        List<Long> coverFileIds = notes.stream()
                .map(DocNote::getCoverFileId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        return buildFileMap(coverFileIds);
    }

    /**
     * 构建文件映射
     * <p>
     * 根据文件ID列表批量查询文件信息，构建文件ID到文件对象的映射。
     * </p>
     *
     * @param fileIds 文件ID列表
     * @return 文件ID到文件对象的映射
     */
    private Map<Long, DocFiles> buildFileMap(List<Long> fileIds) {
        Map<Long, DocFiles> fileMap = new HashMap<>();
        if (fileIds == null || fileIds.isEmpty()) {
            return fileMap;
        }

        List<DocFiles> files = docFilesMapper.selectList(
                new LambdaQueryWrapper<DocFiles>().in(DocFiles::getId, fileIds)
        );
        if (files != null) {
            for (DocFiles file : files) {
                fileMap.put(file.getId(), file);
            }
        }
        return fileMap;
    }

    /**
     * 构建笔记内容映射
     * <p>
     * 批量查询笔记详情内容，构建笔记ID到内容的映射。
     * </p>
     *
     * @param noteIds 笔记ID列表
     * @return 笔记ID到内容的映射
     */
    private Map<Long, String> buildNoteContentMap(List<Long> noteIds) {
        Map<Long, String> contentMap = new HashMap<>();
        if (noteIds == null || noteIds.isEmpty()) {
            return contentMap;
        }

        for (Long noteId : noteIds) {
            DocNoteDtl dtl = noteDtlMapper.selectByNoteId(noteId);
            if (dtl != null && dtl.getContent() != null) {
                contentMap.put(noteId, dtl.getContent());
            }
        }
        return contentMap;
    }

    /**
     * 构建用户信息映射
     * <p>
     * 批量查询用户信息，构建用户ID到用户对象的映射。
     * </p>
     *
     * @param userIds 用户ID列表
     * @return 用户ID到用户对象的映射
     */
    private Map<Long, SysUserApi> buildUserMap(List<Long> userIds) {
        Map<Long, SysUserApi> userMap = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return userMap;
        }

        R<List<SysUserApi>> userResult = remoteUserService.listByIds(userIds);
        if (userResult != null && userResult.getData() != null) {
            for (SysUserApi user : userResult.getData()) {
                userMap.put(user.getId(), user);
            }
        }
        return userMap;
    }

    /**
     * 排序结果
     * <p>
     * 根据排序方式对搜索结果进行排序：
     * - hot：按浏览量降序排序（视频用 playCount，笔记用 readCount）
     * - latest：按创建时间降序排序
     * - 默认/其他：不排序，保持原始顺序
     * </p>
     *
     * @param results 搜索结果列表
     * @param sort    排序方式（hot/latest）
     */
    private void sortResults(List<DocHomeSearchResultVo> results, String sort) {
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
            case "latest":
                // 按ID降序排序（ID为雪花算法生成，趋势递增）
                results.sort((a, b) -> {
                    try {
                        long aId = Long.parseLong(a.getId());
                        long bId = Long.parseLong(b.getId());
                        return Long.compare(bId, aId);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                });
                break;
            default:
                break;
        }
    }
}
