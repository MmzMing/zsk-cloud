package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocFiles;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.vo.DocFileInfoVo;
import com.zsk.document.domain.vo.DocNoteListVo;
import com.zsk.document.domain.vo.DocStatsInfoVo;
import com.zsk.document.domain.vo.DocUserVo;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.enums.CacheDocViewTypeEnum;
import com.zsk.document.mapper.DocFilesMapper;
import com.zsk.document.mapper.DocNoteMapper;
import com.zsk.document.service.ICacheDocCollectService;
import com.zsk.document.service.ICacheDocLikeService;
import com.zsk.document.service.ICacheDocViewService;
import com.zsk.document.service.IDocNoteService;
import com.zsk.system.api.RemoteUserService;
import com.zsk.system.api.domain.SysUserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 笔记Service业务层处理
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocNoteServiceImpl extends ServiceImpl<DocNoteMapper, DocNote> implements IDocNoteService {

    private final DocFilesMapper docFilesMapper;
    private final RemoteUserService remoteUserService;
    private final ICacheDocViewService cacheDocViewService;
    private final ICacheDocLikeService cacheDocLikeService;
    private final ICacheDocCollectService cacheDocCollectService;


    /**
     * 查询笔记列表（带文件URL、作者信息和统计信息）
     * <p>
     * 根据查询条件获取笔记列表，并关联查询封面文件信息、作者信息和统计数据。
     * 采用批量查询策略优化性能，避免N+1查询问题。
     * </p>
     *
     * @param docNote 查询条件对象（支持noteName、broadCode、narrowCode等字段过滤）
     * @return 笔记列表视图对象（包含封面文件信息、作者信息和统计信息）
     */
    @Override
    public List<DocNoteListVo> listWithFileUrl(DocNote docNote) {
        log.info("查询笔记列表（带文件URL、作者信息和统计信息）");
        List<DocNote> noteList = this.list(new LambdaQueryWrapper<>(docNote));
        List<DocNoteListVo> voList = convertToVoList(noteList);
        fillAdditionalInfo(voList);
        return voList;
    }

    /**
     * 分页查询笔记列表（带文件URL、作者信息和统计信息）
     * <p>
     * 根据查询条件和分页参数获取笔记分页数据，并关联查询封面文件信息、作者信息和统计数据。
     * 采用批量查询策略优化性能。
     * </p>
     *
     * @param docNote   查询条件对象
     * @param pageQuery 分页参数（包含pageNum、pageSize）
     * @return 分页结果（包含封面文件信息、作者信息和统计信息）
     */
    @Override
    public PageResult<DocNoteListVo> pageWithFileUrl(DocNote docNote, PageQuery pageQuery) {
        log.info("分页查询笔记列表（带文件URL、作者信息和统计信息）, pageNum={}, pageSize={}", pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<DocNote> page = pageQuery.build();
        Page<DocNote> resultPage = this.page(page, new LambdaQueryWrapper<>(docNote));
        List<DocNoteListVo> voList = convertToVoList(resultPage.getRecords());
        fillAdditionalInfo(voList);
        return PageResult.of(voList, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    /**
     * 填充笔记列表的附加信息（作者信息和统计信息）
     * <p>
     * 批量查询用户信息和统计数据并填充到笔记列表中，避免N+1查询问题。
     * </p>
     *
     * @param noteList 笔记列表视图对象
     */
    private void fillAdditionalInfo(List<DocNoteListVo> noteList) {
        fillAuthorInfo(noteList);
        fillStatsInfo(noteList);
    }

    /**
     * 填充笔记列表的统计信息
     * <p>
     * 批量查询缓存中的统计数据（浏览量、点赞数、收藏数）并填充到笔记列表中。
     * </p>
     *
     * @param noteList 笔记列表视图对象
     */
    private void fillStatsInfo(List<DocNoteListVo> noteList) {
        log.info("开始填充笔记统计信息");

        if (noteList == null || noteList.isEmpty()) {
            log.info("笔记列表为空，跳过统计信息填充");
            return;
        }

        List<Long> noteIds = noteList.stream()
                .map(DocNoteListVo::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        if (noteIds.isEmpty()) {
            log.info("无笔记ID需要查询，跳过统计信息填充");
            return;
        }

        Map<Long, DocStatsInfoVo> statsMap = batchGetNoteStats(noteIds);

        for (DocNoteListVo note : noteList) {
            if (note.getId() != null && statsMap.containsKey(note.getId())) {
                note.setStats(statsMap.get(note.getId()));
            }
        }

        log.info("笔记统计信息填充完成");
    }

    /**
     * 根据笔记ID查询笔记详情（带文件URL、作者信息和统计信息）
     * <p>
     * 根据笔记ID获取笔记详细信息，并关联查询封面文件信息、作者信息和统计数据。
     * 数据来源于数据库和Redis缓存服务。
     * </p>
     *
     * @param noteId 笔记ID
     * @return 笔记详情视图对象（包含封面文件信息、作者信息和统计信息）
     */
    @Override
    public DocNoteListVo getNoteDetail(Long noteId) {
        log.info("查询笔记详情, noteId={}", noteId);

        // 查询笔记实体
        DocNote note = this.getById(noteId);
        if (note == null) {
            log.warn("查询笔记详情失败, 笔记不存在, noteId={}", noteId);
            return null;
        }

        // 转换为视图对象
        DocNoteListVo vo = convertToVo(note);

        // 填充作者信息
        if (vo.getUserId() != null) {
            fillAuthorInfo(List.of(vo));
        }

        // 填充统计信息
        DocStatsInfoVo stats = getNoteStats(noteId);
        vo.setStats(stats);

        log.info("查询笔记详情成功, noteId={}", noteId);
        return vo;
    }

    /**
     * 将单个笔记实体转换为视图对象（带文件URL）
     *
     * @param note 笔记实体
     * @return 笔记视图对象（包含封面文件信息）
     */
    private DocNoteListVo convertToVo(DocNote note) {
        DocNoteListVo vo = new DocNoteListVo();
        BeanUtils.copyProperties(note, vo);

        // 填充封面文件信息
        if (note.getCoverFileId() != null) {
            DocFiles file = docFilesMapper.selectById(note.getCoverFileId());
            vo.setCoverFile(DocFileInfoVo.builder()
                    .fileId(note.getCoverFileId())
                    .fileUrl(file != null ? file.getUrl() : null)
                    .build());
        }

        return vo;
    }

    /**
     * 将笔记实体列表转换为视图对象列表（带文件URL）
     * <p>
     * 核心转换逻辑：
     * 1. 提取所有笔记的封面文件ID
     * 2. 批量查询文件表获取文件URL映射
     * 3. 将实体对象转换为视图对象，并填充封面文件信息
     * </p>
     *
     * @param noteList 笔记实体列表
     * @return 笔记视图对象列表（包含封面文件信息）
     */
    private List<DocNoteListVo> convertToVoList(List<DocNote> noteList) {
        // 空列表直接返回空结果
        if (noteList == null || noteList.isEmpty()) {
            return List.of();
        }

        // 提取所有非空的封面文件ID（关联主键id），用于批量查询
        List<Long> coverFileIds = noteList.stream()
                .map(DocNote::getCoverFileId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        // 构建封面文件ID到文件对象的映射（笔记与封面文件为一对一绑定关系，通过主键id关联）
        Map<Long, DocFiles> fileMap = new HashMap<>();
        if (!coverFileIds.isEmpty()) {
            List<DocFiles> files = docFilesMapper.selectList(
                    new LambdaQueryWrapper<DocFiles>().in(DocFiles::getId, coverFileIds)
            );
            // 一对一绑定：每个文件ID对应唯一的文件记录
            for (DocFiles file : files) {
                fileMap.put(file.getId(), file);
            }
        }

        // 将实体转换为视图对象
        return noteList.stream().map(note -> {
            DocNoteListVo vo = new DocNoteListVo();
            // 复制基本属性
            BeanUtils.copyProperties(note, vo);

            // 填充封面文件信息（一对一绑定：一个笔记对应一个封面文件）
            if (note.getCoverFileId() != null) {
                DocFiles file = fileMap.get(note.getCoverFileId());
                vo.setCoverFile(DocFileInfoVo.builder()
                        .fileId(note.getCoverFileId())
                        .fileUrl(file != null ? file.getUrl() : null)
                        .build());
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取草稿列表
     *
     * @param pageQuery 分页参数
     * @return 草稿列表
     */
    @Override
    public PageResult<DocNote> draftList(PageQuery pageQuery) {
        log.info("获取草稿列表, pageNum={}, pageSize={}", pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<DocNote> page = pageQuery.build();
        LambdaQueryWrapper<DocNote> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DocNote::getStatus, 3);
        PageResult<DocNote> result = PageResult.build(this.page(page, lqw));
        log.info("获取草稿列表完成, 共{}条", result.getTotal());
        return result;
    }

    /**
     * 批量更新状态
     *
     * @param ids    笔记ID列表
     * @param status 目标状态
     * @return 是否成功
     */
    @Override
    public boolean batchUpdateStatus(List<Long> ids, String status) {
        log.info("批量更新笔记状态, ids={}, status={}", ids, status);
        if (ids == null || ids.isEmpty()) {
            log.warn("批量更新笔记状态失败, ID列表为空");
            return false;
        }
        LambdaUpdateWrapper<DocNote> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(DocNote::getId, ids);

        if ("published".equals(status)) {
            updateWrapper.set(DocNote::getStatus, 1);
            updateWrapper.set(DocNote::getAuditStatus, 1);
        } else if ("offline".equals(status)) {
            updateWrapper.set(DocNote::getStatus, 2);
        } else {
            log.warn("批量更新笔记状态失败, 不支持的状态: {}", status);
            return false;
        }

        boolean result = this.update(updateWrapper);
        log.info("批量更新笔记状态完成, 影响{}条记录", ids.size());
        return result;
    }

    /**
     * 批量迁移分类
     *
     * @param ids      笔记ID列表
     * @param category 目标分类
     * @return 是否成功
     */
    @Override
    public boolean batchMoveCategory(List<Long> ids, String category) {
        log.info("批量迁移笔记分类, ids={}, category={}", ids, category);
        if (ids == null || ids.isEmpty()) {
            log.warn("批量迁移笔记分类失败, ID列表为空");
            return false;
        }
        LambdaUpdateWrapper<DocNote> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(DocNote::getId, ids);
        updateWrapper.set(DocNote::getBroadCode, category);
        boolean result = this.update(updateWrapper);
        log.info("批量迁移笔记分类完成, 影响{}条记录", ids.size());
        return result;
    }

    /**
     * 切换置顶状态
     *
     * @param id 笔记ID
     * @return 是否成功
     */
    @Override
    public boolean togglePinned(Long id) {
        log.info("切换笔记置顶状态, id={}", id);
        DocNote note = this.getById(id);
        if (note == null) {
            log.warn("切换笔记置顶状态失败, 笔记不存在, id={}", id);
            return false;
        }

        LambdaUpdateWrapper<DocNote> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocNote::getId, id);
        int newPinned = note.getIsPinned() == 1 ? 0 : 1;
        updateWrapper.set(DocNote::getIsPinned, newPinned);

        boolean result = this.update(updateWrapper);
        log.info("切换笔记置顶状态完成, id={}, 新状态={}", id, newPinned);
        return result;
    }

    /**
     * 切换推荐状态
     *
     * @param id 笔记ID
     * @return 是否成功
     */
    @Override
    public boolean toggleRecommended(Long id) {
        log.info("切换笔记推荐状态, id={}", id);
        DocNote note = this.getById(id);
        if (note == null) {
            log.warn("切换笔记推荐状态失败, 笔记不存在, id={}", id);
            return false;
        }

        LambdaUpdateWrapper<DocNote> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocNote::getId, id);
        int newRecommended = note.getIsRecommended() == 1 ? 0 : 1;
        updateWrapper.set(DocNote::getIsRecommended, newRecommended);

        boolean result = this.update(updateWrapper);
        log.info("切换笔记推荐状态完成, id={}, 新状态={}", id, newRecommended);
        return result;
    }

    /**
     * 填充笔记列表的作者信息
     * <p>
     * 批量查询用户信息并填充到笔记列表中，避免N+1查询问题。
     * 处理流程：
     * 1. 收集笔记列表中所有非空的用户ID
     * 2. 调用远程用户服务批量查询用户信息
     * 3. 构建用户ID到用户信息的映射
     * 4. 遍历笔记列表，根据用户ID填充作者信息
     * </p>
     *
     * @param noteList 笔记列表视图对象
     */
    @Override
    public void fillAuthorInfo(List<DocNoteListVo> noteList) {
        log.info("开始填充笔记作者信息");

        // 空列表直接返回
        if (noteList == null || noteList.isEmpty()) {
            log.info("笔记列表为空，跳过作者信息填充");
            return;
        }

        // 收集所有非空的用户ID，用于批量查询
        List<Long> userIds = noteList.stream()
                .map(DocNoteListVo::getUserId)
                .filter(userId -> userId != null)
                .distinct()
                .collect(Collectors.toList());

        // 无用户ID时直接返回
        if (userIds.isEmpty()) {
            log.info("无用户ID需要查询，跳过作者信息填充");
            return;
        }

        log.info("批量查询用户信息，用户ID数量: {}", userIds.size());

        // 调用远程用户服务批量查询用户信息
        R<List<SysUserApi>> userResult = remoteUserService.listByIds(userIds);
        if (userResult == null || userResult.getData() == null) {
            log.warn("远程用户服务查询失败，跳过作者信息填充");
            return;
        }

        // 构建用户ID到用户信息的映射，便于快速查找
        Map<Long, SysUserApi> userMap = userResult.getData().stream()
                .collect(Collectors.toMap(SysUserApi::getId, user -> user));

        log.info("用户信息查询成功，查询到{}个用户", userMap.size());

        // 遍历笔记列表，填充作者信息
        for (DocNoteListVo note : noteList) {
            if (note.getUserId() != null && userMap.containsKey(note.getUserId())) {
                SysUserApi user = userMap.get(note.getUserId());
                DocUserVo author = new DocUserVo();
                author.setId(user.getId());
                // 优先使用昵称，若无则使用用户名
                author.setName(user.getNickName() != null ? user.getNickName() : user.getUserName());
                author.setAvatar(user.getAvatar());
                note.setAuthor(author);
            }
        }

        log.info("笔记作者信息填充完成");
    }

    /**
     * 根据单个笔记ID查询统计信息
     * <p>
     * 查询指定笔记的点赞数、收藏数和浏览量。
     * 数据来源于 Redis 缓存服务，确保数据的实时性。
     * </p>
     *
     * @param noteId 笔记ID
     * @return 笔记统计信息VO（包含浏览量、点赞数、收藏数）
     */
    @Override
    public DocStatsInfoVo getNoteStats(Long noteId) {
        log.info("查询笔记统计信息, noteId={}", noteId);

        DocStatsInfoVo stats = new DocStatsInfoVo();

        // 查询浏览量
        Long viewCount = cacheDocViewService.getViewCount(CacheDocViewTypeEnum.NOTE.getCode(), noteId);
        stats.setViews(viewCount != null ? viewCount.intValue() : 0);

        // 查询点赞数
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE.getCode(), noteId);
        stats.setLikes(likeCount != null ? likeCount.intValue() : 0);

        // 查询收藏数
        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.NOTE.getCode(), noteId);
        stats.setFavorites(collectCount != null ? collectCount.intValue() : 0);

        log.info("查询笔记统计信息成功, noteId={}, views={}, likes={}, favorites={}",
                noteId, stats.getViews(), stats.getLikes(), stats.getFavorites());

        return stats;
    }

    /**
     * 批量查询笔记统计信息
     * <p>
     * 批量查询多个笔记的点赞数、收藏数和浏览量。
     * 数据来源于 Redis 缓存服务。
     * </p>
     *
     * @param noteIds 笔记ID列表
     * @return 笔记ID到统计信息的映射
     */
    @Override
    public Map<Long, DocStatsInfoVo> batchGetNoteStats(List<Long> noteIds) {
        log.info("批量查询笔记统计信息, noteIds数量={}", noteIds != null ? noteIds.size() : 0);

        Map<Long, DocStatsInfoVo> statsMap = new HashMap<>();

        // 空列表直接返回空结果
        if (noteIds == null || noteIds.isEmpty()) {
            return statsMap;
        }

        // 遍历查询每个笔记的统计信息
        for (Long noteId : noteIds) {
            DocStatsInfoVo stats = new DocStatsInfoVo();

            // 查询浏览量
            Long viewCount = cacheDocViewService.getViewCount(CacheDocViewTypeEnum.NOTE.getCode(), noteId);
            stats.setViews(viewCount != null ? viewCount.intValue() : 0);

            // 查询点赞数
            Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE.getCode(), noteId);
            stats.setLikes(likeCount != null ? likeCount.intValue() : 0);

            // 查询收藏数
            Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.NOTE.getCode(), noteId);
            stats.setFavorites(collectCount != null ? collectCount.intValue() : 0);

            statsMap.put(noteId, stats);
        }

        log.info("批量查询笔记统计信息完成, 成功查询{}条笔记", statsMap.size());

        return statsMap;
    }

    /**
     * 根据笔记ID查询笔记元信息（仅包含基础信息和封面文件URL）
     * <p>
     * 获取笔记的轻量级元信息，仅包含基础字段和封面文件地址。
     * 不包含作者信息和交互统计数据，适用于分享、SEO等场景。
     * </p>
     *
     * @param noteId 笔记ID
     * @return 笔记元信息视图对象（包含封面文件信息，不含作者和统计信息）
     */
    @Override
    public DocNoteListVo getNoteMeta(Long noteId) {
        log.info("查询笔记元信息, noteId={}", noteId);

        // 查询笔记实体
        DocNote note = this.getById(noteId);
        if (note == null) {
            log.warn("查询笔记元信息失败, 笔记不存在, noteId={}", noteId);
            return null;
        }

        // 转换为视图对象（仅包含基础信息和封面文件URL）
        DocNoteListVo vo = convertToVo(note);

        log.info("查询笔记元信息成功, noteId={}", noteId);
        return vo;
    }
}
