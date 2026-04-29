package com.zsk.document.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocFiles;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocVideo;
import com.zsk.document.domain.vo.DocHomeUserStatsVo;
import com.zsk.document.domain.vo.DocHomeUserWorksVo;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.enums.CacheDocViewTypeEnum;
import com.zsk.document.mapper.DocFilesMapper;
import com.zsk.document.mapper.DocNoteMapper;
import com.zsk.document.mapper.DocVideoMapper;
import com.zsk.document.service.ICacheDocCollectService;
import com.zsk.document.service.ICacheDocLikeService;
import com.zsk.document.service.ICacheDocViewService;
import com.zsk.document.service.IDocHomeUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 前台用户作品主页聚合服务实现类
 * <p>
 * 提供用户作品主页的两大核心功能：
 * <ol>
 *     <li><b>作品列表查询</b> — 根据用户ID分页查询其已发布的笔记和视频，支持按类型筛选</li>
 *     <li><b>作品统计查询</b> — 汇总用户所有作品的总点赞数、总浏览数、总收藏数</li>
 * </ol>
 * <p>
 * <b>设计原则：</b>
 * <ul>
 *     <li>不复用其他业务Service（如 IDocNoteService、IDocVideoService），直接通过 Mapper 查询数据，确保前后台逻辑隔离</li>
 *     <li>允许使用 Cache 服务（ICacheDocLikeService、ICacheDocViewService、ICacheDocCollectService）获取交互数据</li>
 *     <li>交互数据（点赞数、浏览数、收藏数）均从 Redis 缓存获取，缓存未命中时自动回源数据库</li>
 *     <li>仅查询已审核通过（auditStatus=1）且正常状态（status=1）且未删除（deleted=0）的作品</li>
 *     <li>VO/DTO 独立定义，不复用后台管理的 VO/DTO</li>
 * </ul>
 * <p>
 * <b>数据流：</b>
 * <pre>
 *   Controller → DocHomeUserServiceImpl → Mapper（查询作品基础数据）
 *                                    → CacheDocLikeService（Redis → DB 回源获取点赞数）
 *                                    → CacheDocViewService（Redis → DB 回源获取浏览数）
 *                                    → CacheDocCollectService（Redis → DB 回源获取收藏数）
 *                                    → DocFilesMapper（查询封面图URL）
 * </pre>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocHomeUserServiceImpl implements IDocHomeUserService {

    /**
     * 笔记 Mapper — 直接查询笔记基础数据，不通过 IDocNoteService
     */
    private final DocNoteMapper noteMapper;

    /**
     * 视频 Mapper — 直接查询视频基础数据，不通过 IDocVideoService
     */
    private final DocVideoMapper videoMapper;

    /**
     * 文件 Mapper — 根据 coverFileId 查询封面图访问地址
     */
    private final DocFilesMapper filesMapper;

    /**
     * 缓存点赞服务 — 从 Redis 获取点赞数（Redis 未命中自动回源 DB）
     */
    private final ICacheDocLikeService cacheDocLikeService;

    /**
     * 缓存浏览服务 — 从 Redis 获取浏览量（Redis 未命中自动回源 DB）
     */
    private final ICacheDocViewService cacheDocViewService;

    /**
     * 缓存收藏服务 — 从 Redis 获取收藏数（Redis 未命中自动回源 DB）
     */
    private final ICacheDocCollectService cacheDocCollectService;

    /**
     * 日期格式化器 — 将 LocalDateTime 格式化为 "yyyy-MM-dd HH:mm:ss" 字符串
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 作品类型常量：笔记
     */
    private static final String TYPE_NOTE = "note";

    /**
     * 作品类型常量：视频
     */
    private static final String TYPE_VIDEO = "video";

    /**
     * 获取用户作品列表（分页）
     * <p>
     * 根据类型参数分三种查询策略：
     * <ul>
     *     <li><b>type=note</b> — 仅查询笔记，使用数据库分页（性能最优）</li>
     *     <li><b>type=video</b> — 仅查询视频，使用数据库分页（性能最优）</li>
     *     <li><b>type为空或其他</b> — 查询笔记+视频，合并后按创建时间降序排序，内存分页</li>
     * </ul>
     * <p>
     * 每条作品包含基础信息（标题、描述、封面等）和交互数据（点赞数、浏览数、收藏数）。
     * 交互数据通过 Cache 服务的批量接口获取，避免 N+1 查询问题。
     *
     * @param userId    目标用户ID（不能为空，为空时返回空分页）
     * @param type      作品类型筛选（note-仅笔记 video-仅视频 null-全部）
     * @param pageQuery 分页查询参数
     * @return 分页作品列表
     */
    @Override
    public PageResult<DocHomeUserWorksVo> getUserWorks(Long userId, String type, PageQuery pageQuery) {
        if (userId == null) {
            return PageResult.empty();
        }

        if (TYPE_NOTE.equalsIgnoreCase(type)) {
            return queryNoteWorks(userId, pageQuery);
        } else if (TYPE_VIDEO.equalsIgnoreCase(type)) {
            return queryVideoWorks(userId, pageQuery);
        } else {
            return queryAllWorks(userId, pageQuery);
        }
    }

    /**
     * 获取用户作品统计
     * <p>
     * 汇总用户所有已发布作品的交互数据，包括：
     * <ul>
     *     <li>总获赞数 — 所有笔记点赞数 + 所有视频点赞数</li>
     *     <li>总浏览数 — 所有笔记浏览数 + 所有视频浏览数</li>
     *     <li>总收藏数 — 所有笔记收藏数 + 所有视频收藏数</li>
     *     <li>笔记数量 — 已发布且审核通过的笔记总数</li>
     *     <li>视频数量 — 已发布且审核通过的视频总数</li>
     * </ul>
     * <p>
     * <b>注意：</b>统计接口需要遍历用户所有作品逐个查询交互数据，
     * 如果用户作品量非常大，此接口可能较慢。后续可考虑增加汇总缓存优化。
     *
     * @param userId 目标用户ID（不能为空，为空时返回零值统计）
     * @return 用户作品统计 VO
     */
    @Override
    public DocHomeUserStatsVo getUserStats(Long userId) {
        if (userId == null) {
            return buildEmptyStats();
        }

        DocHomeUserStatsVo stats = new DocHomeUserStatsVo();

        // 查询笔记和视频数量
        Long noteCount = noteMapper.selectCount(buildNoteQueryWrapper(userId));
        Long videoCount = videoMapper.selectCount(buildVideoQueryWrapper(userId));
        stats.setNoteCount(noteCount != null ? noteCount : 0L);
        stats.setVideoCount(videoCount != null ? videoCount : 0L);

        // 查询所有笔记和视频，用于逐个汇总交互数据
        List<DocNote> notes = noteMapper.selectList(buildNoteQueryWrapper(userId));
        List<DocVideo> videos = videoMapper.selectList(buildVideoQueryWrapper(userId));

        long totalLikeCount = 0L;
        long totalViewCount = 0L;
        long totalFavoriteCount = 0L;

        // 汇总所有笔记的交互数据（点赞数、浏览数、收藏数）
        for (DocNote note : notes) {
            totalLikeCount += cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE.getCode(), note.getId());
            totalViewCount += cacheDocViewService.getViewCount(CacheDocViewTypeEnum.NOTE.getCode(), note.getId());
            totalFavoriteCount += cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.NOTE.getCode(), note.getId());
        }

        // 汇总所有视频的交互数据（点赞数、浏览数、收藏数）
        for (DocVideo video : videos) {
            totalLikeCount += cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.VIDEO.getCode(), video.getId());
            totalViewCount += cacheDocViewService.getViewCount(CacheDocViewTypeEnum.VIDEO.getCode(), video.getId());
            totalFavoriteCount += cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.VIDEO.getCode(), video.getId());
        }

        stats.setTotalLikeCount(totalLikeCount);
        stats.setTotalViewCount(totalViewCount);
        stats.setTotalFavoriteCount(totalFavoriteCount);

        return stats;
    }

    /**
     * 查询用户笔记作品（数据库分页）
     * <p>
     * 使用 MyBatis-Plus 的 selectPage 进行数据库级分页查询，
     * 查询后通过 Cache 服务的批量接口一次性填充所有笔记的交互数据。
     *
     * @param userId    用户ID
     * @param pageQuery 分页参数
     * @return 分页笔记作品列表
     */
    private PageResult<DocHomeUserWorksVo> queryNoteWorks(Long userId, PageQuery pageQuery) {
        Page<DocNote> page = noteMapper.selectPage(pageQuery.build(), buildNoteQueryWrapper(userId));
        List<DocHomeUserWorksVo> worksList = page.getRecords().stream()
                .map(note -> convertNoteToWorksVo(note))
                .collect(Collectors.toList());
        // 批量填充交互数据（点赞数、浏览数、收藏数）
        fillInteractionCounts(worksList, TYPE_NOTE);
        return PageResult.of(worksList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 查询用户视频作品（数据库分页）
     * <p>
     * 使用 MyBatis-Plus 的 selectPage 进行数据库级分页查询，
     * 查询后通过 Cache 服务的批量接口一次性填充所有视频的交互数据。
     *
     * @param userId    用户ID
     * @param pageQuery 分页参数
     * @return 分页视频作品列表
     */
    private PageResult<DocHomeUserWorksVo> queryVideoWorks(Long userId, PageQuery pageQuery) {
        Page<DocVideo> page = videoMapper.selectPage(pageQuery.build(), buildVideoQueryWrapper(userId));
        List<DocHomeUserWorksVo> worksList = page.getRecords().stream()
                .map(video -> convertVideoToWorksVo(video))
                .collect(Collectors.toList());
        // 批量填充交互数据（点赞数、浏览数、收藏数）
        fillInteractionCounts(worksList, TYPE_VIDEO);
        return PageResult.of(worksList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 查询用户全部作品（笔记+视频混合，内存分页）
     * <p>
     * 由于笔记和视频分布在两张表中，无法直接使用数据库 UNION 分页，
     * 因此采用「全量查询 + 内存排序 + 内存分页」策略：
     * <ol>
     *     <li>分别查询所有笔记和所有视频</li>
     *     <li>转换为统一的 DocHomeUserWorksVo</li>
     *     <li>按创建时间降序排序</li>
     *     <li>批量填充交互数据</li>
     *     <li>内存分页截取</li>
     * </ol>
     * <p>
     * <b>性能说明：</b>此方法会查询用户所有作品到内存，适用于作品量不大的场景。
     * 如果用户作品量可能非常大，建议前端优先使用 type=note 或 type=video 单独查询。
     *
     * @param userId    用户ID
     * @param pageQuery 分页参数
     * @return 分页混合作品列表
     */
    private PageResult<DocHomeUserWorksVo> queryAllWorks(Long userId, PageQuery pageQuery) {
        // 查询所有笔记和视频
        List<DocNote> notes = noteMapper.selectList(buildNoteQueryWrapper(userId));
        List<DocVideo> videos = videoMapper.selectList(buildVideoQueryWrapper(userId));

        // 转换为统一的 VO 列表
        List<DocHomeUserWorksVo> allWorks = new ArrayList<>();
        notes.forEach(note -> allWorks.add(convertNoteToWorksVo(note)));
        videos.forEach(video -> allWorks.add(convertVideoToWorksVo(video)));

        // 按创建时间降序排序（null 值排到最后）
        allWorks.sort((a, b) -> {
            if (a.getCreateTime() == null && b.getCreateTime() == null) return 0;
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });

        // 批量填充交互数据（混合类型，需要按 type 分别批量查询）
        fillInteractionCounts(allWorks);

        // 内存分页
        long total = allWorks.size();
        long pageNum = pageQuery.getPageNum();
        long pageSize = pageQuery.getPageSize();
        long fromIndex = (pageNum - 1) * pageSize;
        long toIndex = Math.min(fromIndex + pageSize, total);

        if (fromIndex >= total) {
            return PageResult.of(Collections.emptyList(), total, pageNum, pageSize);
        }

        List<DocHomeUserWorksVo> pageData = allWorks.subList((int) fromIndex, (int) toIndex);
        return PageResult.of(pageData, total, pageNum, pageSize);
    }

    /**
     * 批量填充交互数据（单一类型）
     * <p>
     * 当作品列表只包含笔记或只包含视频时使用此方法。
     * 通过 Cache 服务的批量接口（getLikeCountBatch、getViewCountBatch、getCollectCountBatch）
     * 一次性获取所有作品的交互数据，避免逐个查询导致的 N+1 问题。
     * <p>
     * <b>批量查询流程：</b>
     * <ol>
     *     <li>收集所有作品ID</li>
     *     <li>调用 Cache 服务的 Batch 接口，返回 Map&lt;targetId, count&gt;</li>
     *     <li>遍历作品列表，从 Map 中取出对应的计数值填充</li>
     * </ol>
     *
     * @param worksList  作品 VO 列表
     * @param singleType 作品类型（TYPE_NOTE 或 TYPE_VIDEO）
     */
    private void fillInteractionCounts(List<DocHomeUserWorksVo> worksList, String singleType) {
        if (worksList == null || worksList.isEmpty()) {
            return;
        }

        if (TYPE_NOTE.equals(singleType)) {
            // 收集笔记ID，批量查询交互数据
            List<Long> noteIds = worksList.stream().map(DocHomeUserWorksVo::getId).collect(Collectors.toList());
            Map<Long, Long> likeMap = cacheDocLikeService.getLikeCountBatch(CacheDocLikeTypeEnum.NOTE.getCode(), noteIds);
            Map<Long, Long> viewMap = cacheDocViewService.getViewCountBatch(CacheDocViewTypeEnum.NOTE.getCode(), noteIds);
            Map<Long, Long> favoriteMap = cacheDocCollectService.getCollectCountBatch(CacheDocCollectTypeEnum.NOTE.getCode(), noteIds);
            for (DocHomeUserWorksVo vo : worksList) {
                vo.setLikeCount(likeMap.getOrDefault(vo.getId(), 0L));
                vo.setViewCount(viewMap.getOrDefault(vo.getId(), 0L));
                vo.setFavoriteCount(favoriteMap.getOrDefault(vo.getId(), 0L));
            }
        } else if (TYPE_VIDEO.equals(singleType)) {
            // 收集视频ID，批量查询交互数据
            List<Long> videoIds = worksList.stream().map(DocHomeUserWorksVo::getId).collect(Collectors.toList());
            Map<Long, Long> likeMap = cacheDocLikeService.getLikeCountBatch(CacheDocLikeTypeEnum.VIDEO.getCode(), videoIds);
            Map<Long, Long> viewMap = cacheDocViewService.getViewCountBatch(CacheDocViewTypeEnum.VIDEO.getCode(), videoIds);
            Map<Long, Long> favoriteMap = cacheDocCollectService.getCollectCountBatch(CacheDocCollectTypeEnum.VIDEO.getCode(), videoIds);
            for (DocHomeUserWorksVo vo : worksList) {
                vo.setLikeCount(likeMap.getOrDefault(vo.getId(), 0L));
                vo.setViewCount(viewMap.getOrDefault(vo.getId(), 0L));
                vo.setFavoriteCount(favoriteMap.getOrDefault(vo.getId(), 0L));
            }
        }
    }

    /**
     * 批量填充交互数据（混合类型：笔记+视频）
     * <p>
     * 当作品列表同时包含笔记和视频时使用此方法。
     * 按 type 字段将作品分为两组，分别调用对应类型的 Cache 批量接口查询交互数据。
     * <p>
     * <b>分组查询流程：</b>
     * <ol>
     *     <li>按 type 字段将作品分为笔记组和视频组</li>
     *     <li>分别收集笔记ID和视频ID</li>
     *     <li>分别调用笔记类型和视频类型的 Cache 批量接口</li>
     *     <li>遍历作品列表，根据 type 字段从对应的 Map 中取值填充</li>
     * </ol>
     *
     * @param worksList 作品 VO 列表（包含笔记和视频）
     */
    private void fillInteractionCounts(List<DocHomeUserWorksVo> worksList) {
        if (worksList == null || worksList.isEmpty()) {
            return;
        }

        // 按 type 分组收集 ID
        List<Long> noteIds = worksList.stream()
                .filter(w -> TYPE_NOTE.equals(w.getType()))
                .map(DocHomeUserWorksVo::getId)
                .collect(Collectors.toList());
        List<Long> videoIds = worksList.stream()
                .filter(w -> TYPE_VIDEO.equals(w.getType()))
                .map(DocHomeUserWorksVo::getId)
                .collect(Collectors.toList());

        // 批量查询笔记交互数据
        Map<Long, Long> noteLikeMap = noteIds.isEmpty() ? Collections.emptyMap()
                : cacheDocLikeService.getLikeCountBatch(CacheDocLikeTypeEnum.NOTE.getCode(), noteIds);
        Map<Long, Long> noteViewMap = noteIds.isEmpty() ? Collections.emptyMap()
                : cacheDocViewService.getViewCountBatch(CacheDocViewTypeEnum.NOTE.getCode(), noteIds);
        Map<Long, Long> noteFavoriteMap = noteIds.isEmpty() ? Collections.emptyMap()
                : cacheDocCollectService.getCollectCountBatch(CacheDocCollectTypeEnum.NOTE.getCode(), noteIds);

        // 批量查询视频交互数据
        Map<Long, Long> videoLikeMap = videoIds.isEmpty() ? Collections.emptyMap()
                : cacheDocLikeService.getLikeCountBatch(CacheDocLikeTypeEnum.VIDEO.getCode(), videoIds);
        Map<Long, Long> videoViewMap = videoIds.isEmpty() ? Collections.emptyMap()
                : cacheDocViewService.getViewCountBatch(CacheDocViewTypeEnum.VIDEO.getCode(), videoIds);
        Map<Long, Long> videoFavoriteMap = videoIds.isEmpty() ? Collections.emptyMap()
                : cacheDocCollectService.getCollectCountBatch(CacheDocCollectTypeEnum.VIDEO.getCode(), videoIds);

        // 根据 type 字段从对应的 Map 中取值填充
        for (DocHomeUserWorksVo vo : worksList) {
            if (TYPE_NOTE.equals(vo.getType())) {
                vo.setLikeCount(noteLikeMap.getOrDefault(vo.getId(), 0L));
                vo.setViewCount(noteViewMap.getOrDefault(vo.getId(), 0L));
                vo.setFavoriteCount(noteFavoriteMap.getOrDefault(vo.getId(), 0L));
            } else if (TYPE_VIDEO.equals(vo.getType())) {
                vo.setLikeCount(videoLikeMap.getOrDefault(vo.getId(), 0L));
                vo.setViewCount(videoViewMap.getOrDefault(vo.getId(), 0L));
                vo.setFavoriteCount(videoFavoriteMap.getOrDefault(vo.getId(), 0L));
            }
        }
    }

    /**
     * 将笔记实体转换为作品 VO
     * <p>
     * 字段映射：
     * <ul>
     *     <li>noteName → title</li>
     *     <li>description → description</li>
     *     <li>broadCode → category</li>
     *     <li>noteTags → tags</li>
     *     <li>coverFileId → coverUrl（通过 DocFilesMapper 查询文件访问地址）</li>
     *     <li>createTime → createTime（格式化为 yyyy-MM-dd HH:mm:ss）</li>
     * </ul>
     *
     * @param note 笔记实体
     * @return 作品 VO
     */
    private DocHomeUserWorksVo convertNoteToWorksVo(DocNote note) {
        DocHomeUserWorksVo vo = new DocHomeUserWorksVo();
        vo.setId(note.getId());
        vo.setTitle(note.getNoteName());
        vo.setType(TYPE_NOTE);
        vo.setDescription(note.getDescription());
        vo.setCategory(note.getBroadCode());
        vo.setTags(note.getNoteTags());
        vo.setCoverUrl(resolveCoverUrl(note.getCoverFileId()));
        if (note.getCreateTime() != null) {
            vo.setCreateTime(note.getCreateTime().format(FORMATTER));
        }
        return vo;
    }

    /**
     * 将视频实体转换为作品 VO
     * <p>
     * 字段映射：
     * <ul>
     *     <li>videoTitle → title</li>
     *     <li>fileContent → description</li>
     *     <li>broadCode → category</li>
     *     <li>tags → tags</li>
     *     <li>coverFileId → coverUrl（通过 DocFilesMapper 查询文件访问地址）</li>
     *     <li>createTime → createTime（格式化为 yyyy-MM-dd HH:mm:ss）</li>
     * </ul>
     *
     * @param video 视频实体
     * @return 作品 VO
     */
    private DocHomeUserWorksVo convertVideoToWorksVo(DocVideo video) {
        DocHomeUserWorksVo vo = new DocHomeUserWorksVo();
        vo.setId(video.getId());
        vo.setTitle(video.getVideoTitle());
        vo.setType(TYPE_VIDEO);
        vo.setDescription(video.getFileContent());
        vo.setCategory(video.getBroadCode());
        vo.setTags(video.getTags());
        vo.setCoverUrl(resolveCoverUrl(video.getCoverFileId()));
        if (video.getCreateTime() != null) {
            vo.setCreateTime(video.getCreateTime().format(FORMATTER));
        }
        return vo;
    }

    /**
     * 根据文件ID解析封面图访问地址
     * <p>
     * 通过 DocFilesMapper 查询 document_files 表获取文件的 url 字段。
     * 如果文件不存在或 coverFileId 为空，返回 null。
     *
     * @param coverFileId 封面图文件ID（关联 document_files.id）
     * @return 封面图访问地址，不存在时返回 null
     */
    private String resolveCoverUrl(Long coverFileId) {
        if (coverFileId == null) {
            return null;
        }
        DocFiles files = filesMapper.selectById(coverFileId);
        return files != null ? files.getUrl() : null;
    }

    /**
     * 构建笔记查询条件
     * <p>
     * 查询条件：
     * <ul>
     *     <li>userId = 指定用户ID</li>
     *     <li>status = 1（正常状态，排除下架和草稿）</li>
     *     <li>auditStatus = 1（审核通过，排除待审核和驳回）</li>
     *     <li>deleted = 0（未删除）</li>
     * </ul>
     * 排序：按创建时间降序
     *
     * @param userId 用户ID
     * @return MyBatis-Plus Lambda 查询条件
     */
    private LambdaQueryWrapper<DocNote> buildNoteQueryWrapper(Long userId) {
        return new LambdaQueryWrapper<DocNote>()
                .eq(DocNote::getUserId, userId)
                .eq(DocNote::getStatus, 1)
                .eq(DocNote::getAuditStatus, 1)
                .eq(DocNote::getDeleted, 0)
                .orderByDesc(DocNote::getCreateTime);
    }

    /**
     * 构建视频查询条件
     * <p>
     * 查询条件：
     * <ul>
     *     <li>userId = 指定用户ID</li>
     *     <li>status = 1（正常状态，排除下架和草稿）</li>
     *     <li>auditStatus = 1（审核通过，排除待审核和驳回）</li>
     *     <li>deleted = 0（未删除）</li>
     * </ul>
     * 排序：按创建时间降序
     *
     * @param userId 用户ID
     * @return MyBatis-Plus Lambda 查询条件
     */
    private LambdaQueryWrapper<DocVideo> buildVideoQueryWrapper(Long userId) {
        return new LambdaQueryWrapper<DocVideo>()
                .eq(DocVideo::getUserId, userId)
                .eq(DocVideo::getStatus, 1)
                .eq(DocVideo::getAuditStatus, 1)
                .eq(DocVideo::getDeleted, 0)
                .orderByDesc(DocVideo::getCreateTime);
    }

    /**
     * 构建空统计对象
     * <p>
     * 当 userId 为空时返回零值统计，避免返回 null 导致 NPE。
     *
     * @return 所有字段为 0 的统计 VO
     */
    private DocHomeUserStatsVo buildEmptyStats() {
        DocHomeUserStatsVo stats = new DocHomeUserStatsVo();
        stats.setTotalLikeCount(0L);
        stats.setTotalViewCount(0L);
        stats.setTotalFavoriteCount(0L);
        stats.setNoteCount(0L);
        stats.setVideoCount(0L);
        return stats;
    }
}
