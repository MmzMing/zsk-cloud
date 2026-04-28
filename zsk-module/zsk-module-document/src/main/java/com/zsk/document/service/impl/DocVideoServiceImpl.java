package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocFiles;
import com.zsk.document.domain.DocVideo;
import com.zsk.document.domain.vo.*;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.enums.CacheDocViewTypeEnum;
import com.zsk.document.mapper.DocFilesMapper;
import com.zsk.document.mapper.DocVideoMapper;
import com.zsk.document.service.ICacheDocCollectService;
import com.zsk.document.service.ICacheDocLikeService;
import com.zsk.document.service.ICacheDocViewService;
import com.zsk.document.service.IDocVideoService;
import com.zsk.system.api.RemoteUserService;
import com.zsk.system.api.domain.SysUserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 视频Service业务层处理
 *
 * @author wuhuaming
 * @date 2026-02-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocVideoServiceImpl extends ServiceImpl<DocVideoMapper, DocVideo> implements IDocVideoService {

    private final DocFilesMapper docFilesMapper;
    private final RemoteUserService remoteUserService;
    private final ICacheDocViewService cacheDocViewService;
    private final ICacheDocLikeService cacheDocLikeService;
    private final ICacheDocCollectService cacheDocCollectService;


    /**
     * 查询视频列表（带文件URL、用户信息和统计信息）
     * <p>
     * 根据查询条件获取视频列表，并关联查询视频文件、缩略图文件信息、作者信息和统计数据。
     * 采用批量查询策略优化性能，避免N+1查询问题。
     * </p>
     *
     * @param docVideo 查询条件对象（支持videoTitle、broadCode、narrowCode等字段过滤）
     * @return 视频列表视图对象（包含文件信息、作者信息、统计信息）
     */
    @Override
    public List<DocVideoListVo> listWithFileUrl(DocVideo docVideo) {
        log.info("查询视频列表（带文件URL、用户信息和统计信息）");
        List<DocVideo> videoList = this.list(new LambdaQueryWrapper<>(docVideo));
        List<DocVideoListVo> voList = convertToVoList(videoList);
        fillAdditionalInfo(voList);
        return voList;
    }

    /**
     * 分页查询视频列表（带文件URL、用户信息和统计信息）
     * <p>
     * 根据查询条件和分页参数获取视频分页数据，并关联查询视频文件、缩略图文件信息、作者信息和统计数据。
     * 采用批量查询策略优化性能。
     * </p>
     *
     * @param docVideo  查询条件对象
     * @param pageQuery 分页参数（包含pageNum、pageSize）
     * @return 分页结果（包含文件信息、作者信息、统计信息）
     */
    @Override
    public PageResult<DocVideoListVo> pageWithFileUrl(DocVideo docVideo, PageQuery pageQuery) {
        log.info("分页查询视频列表（带文件URL、用户信息和统计信息）, pageNum={}, pageSize={}", pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<DocVideo> page = pageQuery.build();
        LambdaQueryWrapper<DocVideo> lqw = new LambdaQueryWrapper<>(docVideo);
        lqw.orderByDesc(DocVideo::getCreateTime);
        Page<DocVideo> resultPage = this.page(page, lqw);
        List<DocVideoListVo> voList = convertToVoList(resultPage.getRecords());
        fillAdditionalInfo(voList);
        return PageResult.of(voList, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    /**
     * 发布草稿
     * <p>
     * 将草稿状态变更为正常（1），并设置审核状态为待审核（0）。
     * </p>
     *
     * @param id 草稿ID
     * @return 是否成功
     */
    @Override
    public boolean publishDraft(Long id) {
        log.info("发布视频草稿, id={}", id);
        if (id == null) {
            log.warn("发布视频草稿失败, ID为空");
            return false;
        }
        LambdaUpdateWrapper<DocVideo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocVideo::getId, id);
        updateWrapper.set(DocVideo::getStatus, 1);
        updateWrapper.set(DocVideo::getAuditStatus, 0);
        boolean result = this.update(updateWrapper);
        log.info("发布视频草稿完成, id={}, result={}", id, result);
        return result;
    }

    /**
     * 批量更新视频状态
     * <p>
     * 批量修改视频的状态字段。
     * </p>
     *
     * @param ids    视频ID列表
     * @param status 目标状态
     * @return 是否成功
     */
    @Override
    public boolean batchUpdateStatus(List<Long> ids, Integer status) {
        log.info("批量更新视频状态, ids={}, status={}", ids, status);
        if (ids == null || ids.isEmpty()) {
            log.warn("批量更新视频状态失败, ID列表为空");
            return false;
        }
        LambdaUpdateWrapper<DocVideo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(DocVideo::getId, ids);
        updateWrapper.set(DocVideo::getStatus, status);
        boolean result = this.update(updateWrapper);
        log.info("批量更新视频状态完成, 影响{}条记录", ids.size());
        return result;
    }

    /**
     * 切换视频置顶状态
     * <p>
     * 设置视频是否置顶。
     * </p>
     *
     * @param id     视频ID
     * @param pinned 置顶状态（0-否 1-是）
     * @return 是否成功
     */
    @Override
    public boolean togglePinned(Long id, Integer pinned) {
        log.info("切换视频置顶状态, id={}, pinned={}", id, pinned);
        if (id == null || pinned == null) {
            log.warn("切换视频置顶状态失败, 参数为空");
            return false;
        }
        LambdaUpdateWrapper<DocVideo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocVideo::getId, id);
        updateWrapper.set(DocVideo::getIsPinned, pinned);
        boolean result = this.update(updateWrapper);
        log.info("切换视频置顶状态完成, id={}, result={}", id, result);
        return result;
    }

    /**
     * 切换视频推荐状态
     * <p>
     * 设置视频是否推荐。
     * </p>
     *
     * @param id          视频ID
     * @param recommended 推荐状态（0-否 1-是）
     * @return 是否成功
     */
    @Override
    public boolean toggleRecommended(Long id, Integer recommended) {
        log.info("切换视频推荐状态, id={}, recommended={}", id, recommended);
        if (id == null || recommended == null) {
            log.warn("切换视频推荐状态失败, 参数为空");
            return false;
        }
        LambdaUpdateWrapper<DocVideo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocVideo::getId, id);
        updateWrapper.set(DocVideo::getIsRecommended, recommended);
        boolean result = this.update(updateWrapper);
        log.info("切换视频推荐状态完成, id={}, result={}", id, result);
        return result;
    }

    /**
     * 获取草稿列表（带文件URL）
     * <p>
     * 获取状态为草稿（status=3）的视频列表，并关联查询视频文件和缩略图文件信息。
     * 默认按更新时间倒序排列。
     * </p>
     *
     * @param pageQuery 分页参数
     * @return 草稿列表（包含文件信息）
     */
    @Override
    public PageResult<DocVideoListVo> draftListWithFileUrl(PageQuery pageQuery) {
        log.info("获取视频草稿列表（带文件URL）, pageNum={}, pageSize={}", pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<DocVideo> page = pageQuery.build();
        LambdaQueryWrapper<DocVideo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DocVideo::getStatus, 3);
        lqw.orderByDesc(DocVideo::getUpdateTime);
        Page<DocVideo> resultPage = this.page(page, lqw);
        List<DocVideoListVo> voList = convertToVoList(resultPage.getRecords());
        fillAdditionalInfo(voList);
        return PageResult.of(voList, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    /**
     * 查询视频列表（带文件URL、用户信息和统计信息、缩略图）
     * <p>
     * 根据查询条件获取视频列表，并关联查询视频文件、缩略图文件信息、作者信息和统计数据。
     * 采用批量查询策略优化性能，避免N+1查询问题。
     * 返回的VO包含顶层thumbnail字段，便于前端直接获取缩略图URL。
     * </p>
     *
     * @param docVideo 查询条件对象（支持videoTitle、broadCode、narrowCode等字段过滤）
     * @return 视频列表视图对象（包含文件信息、作者信息、统计信息、缩略图）
     */
    @Override
    public List<DocVideoListWithThumbnailVo> listWithFileUrlAndThumbnail(DocVideo docVideo) {
        log.info("查询视频列表（带文件URL、缩略图）");
        List<DocVideo> videoList = this.list(new LambdaQueryWrapper<>(docVideo));
        List<DocVideoListWithThumbnailVo> voList = convertToVoListWithThumbnail(videoList);
        fillAdditionalInfoWithThumbnail(voList);
        return voList;
    }

    /**
     * 分页查询视频列表（带文件URL、用户信息和统计信息、缩略图）
     * <p>
     * 根据查询条件和分页参数获取视频分页数据，并关联查询视频文件、缩略图文件信息、作者信息和统计数据。
     * 采用批量查询策略优化性能。
     * 返回的VO包含顶层thumbnail字段，便于前端直接获取缩略图URL。
     * </p>
     *
     * @param docVideo  查询条件对象
     * @param pageQuery 分页参数（包含pageNum、pageSize）
     * @return 分页结果（包含文件信息、作者信息、统计信息、缩略图）
     */
    @Override
    public PageResult<DocVideoListWithThumbnailVo> pageWithFileUrlAndThumbnail(DocVideo docVideo, PageQuery pageQuery) {
        log.info("分页查询视频列表（带文件URL、缩略图）, pageNum={}, pageSize={}", pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<DocVideo> page = pageQuery.build();
        LambdaQueryWrapper<DocVideo> lqw = new LambdaQueryWrapper<>(docVideo);
        lqw.orderByDesc(DocVideo::getCreateTime);
        Page<DocVideo> resultPage = this.page(page, lqw);
        List<DocVideoListWithThumbnailVo> voList = convertToVoListWithThumbnail(resultPage.getRecords());
        fillAdditionalInfoWithThumbnail(voList);
        return PageResult.of(voList, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    /**
     * 获取视频详情（带文件URL）
     * <p>
     * 根据视频ID获取详情，并关联查询视频文件和缩略图文件信息。
     * 一对一绑定关系：一个视频对应一个视频文件和一个缩略图文件。
     * </p>
     *
     * @param id 视频ID
     * @return 视频详情（包含文件信息、分集信息）
     */
    @Override
    public DocVideoDetailVo getDetailWithFileUrl(Long id) {
        log.info("获取视频详情（带文件URL）, id={}", id);
        if (id == null) {
            log.warn("获取视频详情失败, ID为空");
            return null;
        }

        DocVideo video = this.getById(id);
        if (video == null) {
            log.warn("获取视频详情失败, 视频不存在, id={}", id);
            return null;
        }

        return convertToDetailVo(video);
    }

    /**
     * 将视频实体列表转换为列表视图对象列表（带文件URL）
     * <p>
     * 核心转换逻辑：
     * 1. 提取所有视频的文件ID（视频文件ID和封面文件ID）
     * 2. 批量查询文件表获取文件URL映射
     * 3. 将实体对象转换为视图对象，并填充文件信息
     * </p>
     *
     * @param videoList 视频实体列表
     * @return 视频列表视图对象列表（包含文件信息）
     */
    private List<DocVideoListVo> convertToVoList(List<DocVideo> videoList) {
        if (videoList == null || videoList.isEmpty()) {
            return List.of();
        }

        Map<Long, DocFiles> fileIdMap = buildFileIdMap(videoList);

        return videoList.stream().map(video -> {
            DocVideoListVo vo = new DocVideoListVo();
            vo.setId(video.getId());
            vo.setUserId(video.getUserId());
            vo.setVideoTitle(video.getVideoTitle());
            vo.setBroadCode(video.getBroadCode());
            vo.setNarrowCode(video.getNarrowCode());
            vo.setTags(video.getTags());
            vo.setFileContent(video.getFileContent());
            vo.setMetaData(video.getMetaData());
            vo.setAuditStatus(video.getAuditStatus());
            vo.setAuditMind(video.getAuditMind());
            vo.setAuditId(video.getAuditId());
            vo.setStatus(video.getStatus());
            vo.setIsPinned(video.getIsPinned());
            vo.setIsRecommended(video.getIsRecommended());
            vo.setDeleted(video.getDeleted());
            vo.setCreateTime(video.getCreateTime());
            vo.setUpdateTime(video.getUpdateTime());
            vo.setVideoFile(buildVideoFileVo(video, fileIdMap));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 将视频实体转换为详情视图对象（带文件URL）
     *
     * @param video 视频实体
     * @return 视频详情视图对象（包含文件信息、分集信息）
     */
    private DocVideoDetailVo convertToDetailVo(DocVideo video) {
        Map<Long, DocFiles> fileIdMap = buildFileIdMap(List.of(video));

        DocVideoDetailVo vo = new DocVideoDetailVo();
        vo.setId(video.getId());
        vo.setUserId(video.getUserId());
        vo.setVideoTitle(video.getVideoTitle());
        vo.setBroadCode(video.getBroadCode());
        vo.setNarrowCode(video.getNarrowCode());
        vo.setTags(video.getTags());
        vo.setFileContent(video.getFileContent());
        vo.setMetaData(video.getMetaData());
        vo.setAuditStatus(video.getAuditStatus());
        vo.setAuditMind(video.getAuditMind());
        vo.setAuditId(video.getAuditId());
        vo.setStatus(video.getStatus());
        vo.setIsPinned(video.getIsPinned());
        vo.setIsRecommended(video.getIsRecommended());
        vo.setDeleted(video.getDeleted());
        vo.setCreateTime(video.getCreateTime());
        vo.setUpdateTime(video.getUpdateTime());
        vo.setVideoFile(buildVideoFileVo(video, fileIdMap));
        return vo;
    }

    /**
     * 构建文件ID到文件对象的映射（批量查询优化）
     *
     * @param videoList 视频实体列表
     * @return 文件ID映射
     */
    private Map<Long, DocFiles> buildFileIdMap(List<DocVideo> videoList) {
        List<Long> videoFileIds = videoList.stream()
                .map(DocVideo::getFileId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        List<Long> coverFileIds = videoList.stream()
                .map(DocVideo::getCoverFileId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        List<Long> allFileIds = new java.util.ArrayList<>();
        allFileIds.addAll(videoFileIds);
        allFileIds.addAll(coverFileIds);

        Map<Long, DocFiles> fileIdMap = new HashMap<>();
        if (!allFileIds.isEmpty()) {
            List<DocFiles> files = docFilesMapper.selectList(
                    new LambdaQueryWrapper<DocFiles>().in(DocFiles::getId, allFileIds)
            );
            for (DocFiles file : files) {
                fileIdMap.put(file.getId(), file);
            }
        }
        return fileIdMap;
    }

    /**
     * 构建视频文件信息视图对象
     *
     * @param video     视频实体
     * @param fileIdMap 文件ID映射
     * @return 视频文件信息视图对象
     */
    private DocVideoFileVo buildVideoFileVo(DocVideo video, Map<Long, DocFiles> fileIdMap) {
        DocVideoFileVo videoFileVo = DocVideoFileVo.builder().build();

        if (video.getFileId() != null) {
            DocFiles videoFile = fileIdMap.get(video.getFileId());
            videoFileVo.setVideo(DocFileInfoVo.builder()
                    .fileId(video.getFileId())
                    .fileUrl(videoFile != null ? videoFile.getUrl() : null)
                    .build());
        }

        if (video.getCoverFileId() != null) {
            DocFiles coverFile = fileIdMap.get(video.getCoverFileId());
            videoFileVo.setThumbnail(DocFileInfoVo.builder()
                    .fileId(video.getCoverFileId())
                    .fileUrl(coverFile != null ? coverFile.getUrl() : null)
                    .build());
        }

        return videoFileVo;
    }

    /**
     * 填充视频列表的附加信息（用户信息和统计信息）
     * <p>
     * 批量查询用户信息和统计数据并填充到视频列表中，避免N+1查询问题。
     * </p>
     *
     * @param videoList 视频列表视图对象
     */
    private void fillAdditionalInfo(List<DocVideoListVo> videoList) {
        fillUserInfo(videoList);
        fillStatsInfo(videoList);
    }

    /**
     * 填充带缩略图的视频列表附加信息（用户信息、统计信息和缩略图）
     * <p>
     * 批量查询用户信息、统计数据并提取缩略图信息填充到视频列表中，避免N+1查询问题。
     * </p>
     *
     * @param videoList 带缩略图的视频列表视图对象
     */
    private void fillAdditionalInfoWithThumbnail(List<DocVideoListWithThumbnailVo> videoList) {
        fillUserInfo(videoList);
        fillStatsInfo(videoList);
        fillThumbnail(videoList);
    }

    /**
     * 填充视频列表的用户信息
     * <p>
     * 批量查询用户信息并填充到视频列表中，避免N+1查询问题。
     * 使用通配符类型参数支持 DocVideoListVo 及其子类（如 DocVideoListWithThumbnailVo）。
     * </p>
     *
     * @param videoList 视频列表视图对象（或其子类列表）
     */
    private void fillUserInfo(List<? extends DocVideoListVo> videoList) {
        log.info("开始填充视频用户信息");

        if (videoList == null || videoList.isEmpty()) {
            log.info("视频列表为空，跳过用户信息填充");
            return;
        }

        List<Long> userIds = videoList.stream()
                .map(DocVideoListVo::getUserId)
                .filter(userId -> userId != null)
                .distinct()
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            log.info("无用户ID需要查询，跳过用户信息填充");
            return;
        }

        log.info("批量查询用户信息，用户ID数量: {}", userIds.size());

        R<List<SysUserApi>> userResult = remoteUserService.listByIds(userIds);
        if (userResult == null || userResult.getData() == null) {
            log.warn("远程用户服务查询失败，跳过用户信息填充");
            return;
        }

        Map<Long, SysUserApi> userMap = userResult.getData().stream()
                .collect(Collectors.toMap(SysUserApi::getId, user -> user));

        log.info("用户信息查询成功，查询到{}个用户", userMap.size());

        for (DocVideoListVo video : videoList) {
            if (video.getUserId() != null && userMap.containsKey(video.getUserId())) {
                SysUserApi user = userMap.get(video.getUserId());
                DocUserVo userVo = new DocUserVo();
                userVo.setId(user.getId());
                userVo.setName(user.getNickName() != null ? user.getNickName() : user.getUserName());
                userVo.setAvatar(user.getAvatar());
                video.setUser(userVo);
            }
        }

        log.info("视频用户信息填充完成");
    }

    /**
     * 填充视频列表的统计信息
     * <p>
     * 批量查询缓存中的统计数据（浏览量、点赞数、收藏数）并填充到视频列表中。
     * 使用通配符类型参数支持 DocVideoListVo 及其子类（如 DocVideoListWithThumbnailVo）。
     * </p>
     *
     * @param videoList 视频列表视图对象（或其子类列表）
     */
    private void fillStatsInfo(List<? extends DocVideoListVo> videoList) {
        log.info("开始填充视频统计信息");

        if (videoList == null || videoList.isEmpty()) {
            log.info("视频列表为空，跳过统计信息填充");
            return;
        }

        List<Long> videoIds = videoList.stream()
                .map(DocVideoListVo::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        if (videoIds.isEmpty()) {
            log.info("无视频ID需要查询，跳过统计信息填充");
            return;
        }

        Integer viewType = CacheDocViewTypeEnum.VIDEO.getCode();
        Integer likeType = CacheDocLikeTypeEnum.VIDEO.getCode();
        Integer collectType = CacheDocCollectTypeEnum.VIDEO.getCode();

        Map<Long, Long> viewCountMap = cacheDocViewService.getViewCountBatch(viewType, videoIds);
        Map<Long, Long> likeCountMap = cacheDocLikeService.getLikeCountBatch(likeType, videoIds);
        Map<Long, Long> collectCountMap = cacheDocCollectService.getCollectCountBatch(collectType, videoIds);

        for (DocVideoListVo video : videoList) {
            Long videoId = video.getId();
            DocStatsInfoVo stats = new DocStatsInfoVo();
            stats.setViews(viewCountMap.getOrDefault(videoId, 0L).intValue());
            stats.setLikes(likeCountMap.getOrDefault(videoId, 0L).intValue());
            stats.setFavorites(collectCountMap.getOrDefault(videoId, 0L).intValue());
            video.setStatsInfo(stats);
        }

        log.info("视频统计信息填充完成");
    }

    /**
     * 将视频实体列表转换为带缩略图的列表视图对象列表（带文件URL）
     * <p>
     * 核心转换逻辑：
     * 1. 提取所有视频的文件ID（视频文件ID和封面文件ID）
     * 2. 批量查询文件表获取文件URL映射
     * 3. 将实体对象转换为带缩略图的视图对象，并填充文件信息
     * </p>
     *
     * @param videoList 视频实体列表
     * @return 带缩略图的视频列表视图对象列表（包含文件信息）
     */
    private List<DocVideoListWithThumbnailVo> convertToVoListWithThumbnail(List<DocVideo> videoList) {
        if (videoList == null || videoList.isEmpty()) {
            return List.of();
        }

        Map<Long, DocFiles> fileIdMap = buildFileIdMap(videoList);

        return videoList.stream().map(video -> {
            DocVideoListWithThumbnailVo vo = new DocVideoListWithThumbnailVo();
            vo.setId(video.getId());
            vo.setUserId(video.getUserId());
            vo.setVideoTitle(video.getVideoTitle());
            vo.setBroadCode(video.getBroadCode());
            vo.setNarrowCode(video.getNarrowCode());
            vo.setTags(video.getTags());
            vo.setFileContent(video.getFileContent());
            vo.setMetaData(video.getMetaData());
            vo.setAuditStatus(video.getAuditStatus());
            vo.setAuditMind(video.getAuditMind());
            vo.setAuditId(video.getAuditId());
            vo.setStatus(video.getStatus());
            vo.setIsPinned(video.getIsPinned());
            vo.setIsRecommended(video.getIsRecommended());
            vo.setDeleted(video.getDeleted());
            vo.setCreateTime(video.getCreateTime());
            vo.setUpdateTime(video.getUpdateTime());
            vo.setVideoFile(buildVideoFileVo(video, fileIdMap));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 填充视频列表的缩略图信息
     * <p>
     * 从 videoFile.thumbnail 中提取缩略图信息填充到 thumbnail 字段。
     * </p>
     *
     * @param videoList 带缩略图的视频列表视图对象
     */
    private void fillThumbnail(List<DocVideoListWithThumbnailVo> videoList) {
        if (videoList == null || videoList.isEmpty()) {
            return;
        }

        for (DocVideoListWithThumbnailVo video : videoList) {
            if (video.getVideoFile() != null && video.getVideoFile().getThumbnail() != null) {
                video.setThumbnail(video.getVideoFile().getThumbnail());
            }
        }
    }
}
