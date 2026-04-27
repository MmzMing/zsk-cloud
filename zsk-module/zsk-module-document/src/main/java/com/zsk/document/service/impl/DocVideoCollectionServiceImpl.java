package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.core.exception.BusinessException;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.document.domain.DocFiles;
import com.zsk.document.domain.DocVideo;
import com.zsk.document.domain.DocVideoCollection;
import com.zsk.document.domain.DocVideoCollectionItem;
import com.zsk.document.domain.dto.CollectionVideoSortDTO;
import com.zsk.document.domain.vo.DocFileInfoVo;
import com.zsk.document.domain.vo.DocVideoCollectionDtlVo;
import com.zsk.document.domain.vo.DocVideoCollectionVo;
import com.zsk.document.domain.vo.DocVideoFileVo;
import com.zsk.document.domain.vo.DocVideoListVo;
import com.zsk.document.mapper.DocFilesMapper;
import com.zsk.document.mapper.DocVideoCollectionItemMapper;
import com.zsk.document.mapper.DocVideoCollectionMapper;
import com.zsk.document.mapper.DocVideoMapper;
import com.zsk.document.service.IDocVideoCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 视频合集Service业务层处理
 * <p>
 * 提供视频合集的查询、创建、修改、删除、视频加入/移除/排序等核心业务逻辑。
 * 所有操作均基于当前登录用户进行权限校验，确保用户只能操作自己的合集。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocVideoCollectionServiceImpl extends ServiceImpl<DocVideoCollectionMapper, DocVideoCollection> implements IDocVideoCollectionService {

    /**
     * 合集视频关联Mapper
     */
    private final DocVideoCollectionItemMapper collectionItemMapper;

    /**
     * 视频Mapper
     */
    private final DocVideoMapper docVideoMapper;

    /**
     * 文件Mapper
     */
    private final DocFilesMapper docFilesMapper;

    /**
     * 查询当前用户的合集列表
     * <p>
     * 根据当前登录用户ID查询其拥有的所有视频合集（未删除）。
     * 支持按状态（公开/私密）筛选，默认按排序值降序、创建时间降序排列。
     * 返回结果包含封面文件URL信息。
     * </p>
     *
     * @param docVideoCollection 查询条件（可选：status状态筛选）
     * @return 合集视图对象列表
     */
    @Override
    public List<DocVideoCollectionVo> listByUser(DocVideoCollection docVideoCollection) {
        Long userId = SecurityUtils.getUserId();
        log.info("查询用户视频合集列表, userId={}", userId);

        LambdaQueryWrapper<DocVideoCollection> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DocVideoCollection::getUserId, userId);
        lqw.eq(DocVideoCollection::getDeleted, 0);
        if (docVideoCollection != null && docVideoCollection.getStatus() != null) {
            lqw.eq(DocVideoCollection::getStatus, docVideoCollection.getStatus());
        }
        lqw.orderByDesc(DocVideoCollection::getSortOrder).orderByDesc(DocVideoCollection::getCreateTime);

        List<DocVideoCollection> list = this.list(lqw);
        return convertToVoList(list);
    }

    /**
     * 分页查询当前用户的合集列表
     * <p>
     * 根据当前登录用户ID分页查询其拥有的视频合集（未删除）。
     * 支持按状态（公开/私密）筛选，默认按排序值降序、创建时间降序排列。
     * 返回结果包含封面文件URL信息。
     * </p>
     *
     * @param docVideoCollection 查询条件（可选：status状态筛选）
     * @param pageQuery          分页参数（pageNum、pageSize）
     * @return 分页结果（包含封面文件信息）
     */
    @Override
    public PageResult<DocVideoCollectionVo> pageByUser(DocVideoCollection docVideoCollection, PageQuery pageQuery) {
        Long userId = SecurityUtils.getUserId();
        log.info("分页查询用户视频合集列表, userId={}, pageNum={}, pageSize={}", userId, pageQuery.getPageNum(), pageQuery.getPageSize());

        Page<DocVideoCollection> page = pageQuery.build();
        LambdaQueryWrapper<DocVideoCollection> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DocVideoCollection::getUserId, userId);
        lqw.eq(DocVideoCollection::getDeleted, 0);
        if (docVideoCollection != null && docVideoCollection.getStatus() != null) {
            lqw.eq(DocVideoCollection::getStatus, docVideoCollection.getStatus());
        }
        lqw.orderByDesc(DocVideoCollection::getSortOrder).orderByDesc(DocVideoCollection::getCreateTime);

        Page<DocVideoCollection> resultPage = this.page(page, lqw);
        List<DocVideoCollectionVo> voList = convertToVoList(resultPage.getRecords());
        return PageResult.of(voList, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    /**
     * 获取合集详情（包含视频列表）
     * <p>
     * 根据合集ID获取详情信息，同时查询合集中的所有视频列表。
     * 视频列表按合集中的排序顺序排列，仅返回状态正常且审核通过的视频。
     * 每个视频包含视频文件和缩略图文件信息。
     * </p>
     *
     * @param id 合集ID
     * @return 合集详情视图对象（包含视频列表）
     */
    @Override
    public DocVideoCollectionDtlVo getCollectionDetail(Long id) {
        log.info("获取视频合集详情, id={}", id);
        if (id == null) {
            return null;
        }

        DocVideoCollection collection = this.getById(id);
        if (collection == null || collection.getDeleted() == 1) {
            log.warn("视频合集不存在或已删除, id={}", id);
            return null;
        }

        DocVideoCollectionDtlVo dtlVo = new DocVideoCollectionDtlVo();
        dtlVo.setId(collection.getId());
        dtlVo.setCollectionName(collection.getCollectionName());
        dtlVo.setDescription(collection.getDescription());
        dtlVo.setVideoCount(collection.getVideoCount());
        dtlVo.setSortOrder(collection.getSortOrder());
        dtlVo.setStatus(collection.getStatus());
        dtlVo.setCreateTime(collection.getCreateTime());
        dtlVo.setUpdateTime(collection.getUpdateTime());

        // 查询封面文件信息
        if (collection.getCoverFileId() != null) {
            DocFiles coverFile = docFilesMapper.selectById(collection.getCoverFileId());
            if (coverFile != null) {
                dtlVo.setCover(DocFileInfoVo.builder()
                        .fileId(coverFile.getId())
                        .fileUrl(coverFile.getUrl())
                        .build());
            }
        }

        // 查询合集中的视频列表
        List<Long> videoIds = collectionItemMapper.selectVideoIdsByCollectionId(id);
        if (!videoIds.isEmpty()) {
            List<DocVideo> videoList = docVideoMapper.selectList(
                    new LambdaQueryWrapper<DocVideo>()
                            .in(DocVideo::getId, videoIds)
                            .eq(DocVideo::getDeleted, 0)
                            .eq(DocVideo::getStatus, 1)
                            .eq(DocVideo::getAuditStatus, 1)
            );

            // 按合集排序顺序排列（数据库查询结果无序，需根据videoIds顺序重排）
            Map<Long, DocVideo> videoMap = videoList.stream()
                    .collect(Collectors.toMap(DocVideo::getId, v -> v));
            List<DocVideo> sortedVideos = new ArrayList<>();
            for (Long videoId : videoIds) {
                DocVideo video = videoMap.get(videoId);
                if (video != null) {
                    sortedVideos.add(video);
                }
            }

            dtlVo.setVideoList(convertToVideoVoList(sortedVideos));
        } else {
            dtlVo.setVideoList(List.of());
        }

        return dtlVo;
    }

    /**
     * 创建合集
     * <p>
     * 为当前登录用户创建一个新的视频合集。
     * 自动设置用户ID、初始化视频数量为0、默认排序值为0、默认状态为公开（1）。
     * </p>
     *
     * @param docVideoCollection 合集信息（collectionName必填）
     * @return 新创建合集的ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCollection(DocVideoCollection docVideoCollection) {
        Long userId = SecurityUtils.getUserId();
        String userName = SecurityUtils.getUserName();
        log.info("创建视频合集, userId={}, collectionName={}", userId, docVideoCollection.getCollectionName());

        docVideoCollection.setUserId(userId);
        docVideoCollection.setVideoCount(0);
        if (docVideoCollection.getSortOrder() == null) {
            docVideoCollection.setSortOrder(0);
        }
        if (docVideoCollection.getStatus() == null) {
            docVideoCollection.setStatus(1);
        }
        docVideoCollection.setCreateName(userName);
        docVideoCollection.setUpdateName(userName);

        this.save(docVideoCollection);
        log.info("创建视频合集成功, id={}", docVideoCollection.getId());
        return docVideoCollection.getId();
    }

    /**
     * 修改合集信息
     * <p>
     * 修改合集的基本信息（名称、描述、封面、排序、状态等）。
     * 操作前校验当前用户是否为合集所有者，禁止修改用户ID和视频数量。
     * </p>
     *
     * @param docVideoCollection 合集信息（id必填）
     * @return 是否修改成功
     */
    @Override
    public boolean updateCollection(DocVideoCollection docVideoCollection) {
        log.info("修改视频合集, id={}", docVideoCollection.getId());
        if (docVideoCollection.getId() == null) {
            log.warn("修改视频合集失败, ID为空");
            return false;
        }

        // 校验权限：确保当前用户是合集所有者
        checkOwnership(docVideoCollection.getId());

        // 禁止通过此接口修改用户ID和视频数量
        docVideoCollection.setUserId(null);
        docVideoCollection.setVideoCount(null);
        docVideoCollection.setUpdateName(SecurityUtils.getUserName());

        boolean result = this.updateById(docVideoCollection);
        log.info("修改视频合集完成, id={}, result={}", docVideoCollection.getId(), result);
        return result;
    }

    /**
     * 删除合集（软删除，同时删除关联项）
     * <p>
     * 批量删除合集及其关联的视频项，采用软删除策略（设置deleted=1）。
     * 操作前校验当前用户是否为每个合集的所有者。
     * </p>
     *
     * @param ids 合集ID列表
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeCollectionByIds(List<Long> ids) {
        log.info("删除视频合集, ids={}", ids);
        if (ids == null || ids.isEmpty()) {
            log.warn("删除视频合集失败, ID列表为空");
            return false;
        }

        // 校验每个合集的所有权
        for (Long id : ids) {
            checkOwnership(id);
        }

        // 软删除合集主表
        LambdaUpdateWrapper<DocVideoCollection> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(DocVideoCollection::getId, ids);
        updateWrapper.set(DocVideoCollection::getDeleted, 1);
        updateWrapper.set(DocVideoCollection::getUpdateName, SecurityUtils.getUserName());
        boolean result = this.update(updateWrapper);

        // 软删除关联表记录
        LambdaUpdateWrapper<DocVideoCollectionItem> itemWrapper = new LambdaUpdateWrapper<>();
        itemWrapper.in(DocVideoCollectionItem::getCollectionId, ids);
        itemWrapper.set(DocVideoCollectionItem::getDeleted, 1);
        itemWrapper.set(DocVideoCollectionItem::getUpdateName, SecurityUtils.getUserName());
        collectionItemMapper.update(itemWrapper);

        log.info("删除视频合集完成, 影响{}条记录", ids.size());
        return result;
    }

    /**
     * 批量添加视频到合集
     * <p>
     * 将多个视频批量添加到指定合集中。
     * 自动过滤已存在于合集中的视频（通过唯一索引防重复）。
     * 新添加的视频默认按顺序排在末尾（sortOrder递增）。
     * 操作完成后自动更新合集的视频数量。
     * </p>
     *
     * @param collectionId 合集ID
     * @param videoIds     视频ID列表
     * @return 是否添加成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addVideosToCollection(Long collectionId, List<Long> videoIds) {
        log.info("批量添加视频到合集, collectionId={}, videoIds={}", collectionId, videoIds);
        if (collectionId == null || videoIds == null || videoIds.isEmpty()) {
            log.warn("批量添加视频到合集失败, 参数为空");
            return false;
        }

        // 校验合集所有权
        checkOwnership(collectionId);

        // 查询已存在于该合集中的视频ID（避免重复添加）
        List<Long> existingVideoIds = collectionItemMapper.selectVideoIdsByCollectionId(collectionId);

        // 获取当前最大排序号，新视频排在末尾
        int maxSort = existingVideoIds.size();

        String userName = SecurityUtils.getUserName();
        List<DocVideoCollectionItem> items = new ArrayList<>();
        for (Long videoId : videoIds) {
            if (!existingVideoIds.contains(videoId)) {
                DocVideoCollectionItem item = new DocVideoCollectionItem();
                item.setCollectionId(collectionId);
                item.setVideoId(videoId);
                item.setSortOrder(maxSort++);
                item.setCreateName(userName);
                item.setUpdateName(userName);
                items.add(item);
            }
        }

        if (!items.isEmpty()) {
            // 逐条插入（MyBatis-Plus批量插入需配置，此处采用逐条方式确保兼容）
            for (DocVideoCollectionItem item : items) {
                collectionItemMapper.insert(item);
            }

            // 更新合集视频数量
            updateVideoCount(collectionId);
        }

        log.info("批量添加视频到合集完成, collectionId={}, 新增{}条", collectionId, items.size());
        return true;
    }

    /**
     * 批量从合集移除视频
     * <p>
     * 将多个视频从指定合集中移除，采用软删除策略。
     * 操作前校验合集所有权，操作完成后自动更新合集的视频数量。
     * </p>
     *
     * @param collectionId 合集ID
     * @param videoIds     视频ID列表
     * @return 是否移除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeVideosFromCollection(Long collectionId, List<Long> videoIds) {
        log.info("批量从合集移除视频, collectionId={}, videoIds={}", collectionId, videoIds);
        if (collectionId == null || videoIds == null || videoIds.isEmpty()) {
            log.warn("批量从合集移除视频失败, 参数为空");
            return false;
        }

        // 校验合集所有权
        checkOwnership(collectionId);

        // 软删除关联记录
        LambdaUpdateWrapper<DocVideoCollectionItem> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(DocVideoCollectionItem::getCollectionId, collectionId);
        wrapper.in(DocVideoCollectionItem::getVideoId, videoIds);
        wrapper.set(DocVideoCollectionItem::getDeleted, 1);
        wrapper.set(DocVideoCollectionItem::getUpdateName, SecurityUtils.getUserName());
        collectionItemMapper.update(wrapper);

        // 更新合集视频数量
        updateVideoCount(collectionId);

        log.info("批量从合集移除视频完成, collectionId={}", collectionId);
        return true;
    }

    /**
     * 调整合集中视频排序
     * <p>
     * 根据传入的视频ID列表顺序，重新设置合集中视频的排序值。
     * 列表中的第一个视频sortOrder=0，第二个=1，以此类推。
     * 操作前校验合集所有权。
     * </p>
     *
     * @param collectionId 合集ID
     * @param sortDTO      排序参数（videoIds按期望顺序排列）
     * @return 是否排序成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sortCollectionVideos(Long collectionId, CollectionVideoSortDTO sortDTO) {
        log.info("调整合集视频排序, collectionId={}, videoIds={}", collectionId, sortDTO.getVideoIds());
        if (collectionId == null || sortDTO == null || sortDTO.getVideoIds() == null || sortDTO.getVideoIds().isEmpty()) {
            log.warn("调整合集视频排序失败, 参数为空");
            return false;
        }

        // 校验合集所有权
        checkOwnership(collectionId);

        String userName = SecurityUtils.getUserName();
        List<Long> videoIds = sortDTO.getVideoIds();
        for (int i = 0; i < videoIds.size(); i++) {
            LambdaUpdateWrapper<DocVideoCollectionItem> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(DocVideoCollectionItem::getCollectionId, collectionId);
            wrapper.eq(DocVideoCollectionItem::getVideoId, videoIds.get(i));
            wrapper.set(DocVideoCollectionItem::getSortOrder, i);
            wrapper.set(DocVideoCollectionItem::getUpdateName, userName);
            collectionItemMapper.update(wrapper);
        }

        log.info("调整合集视频排序完成, collectionId={}", collectionId);
        return true;
    }

    /**
     * 校验合集所有权
     * <p>
     * 检查当前登录用户是否为指定合集的所有者。
     * 如果合集不存在、已删除，或当前用户非所有者，则抛出BusinessException。
     * </p>
     *
     * @param collectionId 合集ID
     * @throws BusinessException 合集不存在/已删除，或无权操作
     */
    private void checkOwnership(Long collectionId) {
        DocVideoCollection collection = this.getById(collectionId);
        if (collection == null || collection.getDeleted() == 1) {
            throw new BusinessException("合集不存在或已删除");
        }
        Long currentUserId = SecurityUtils.getUserId();
        if (!collection.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权操作该合集");
        }
    }

    /**
     * 更新合集视频数量
     * <p>
     * 统计指定合集中未删除的视频数量，并更新到合集主表的videoCount字段。
     * 同时更新updateName为当前操作用户。
     * </p>
     *
     * @param collectionId 合集ID
     */
    private void updateVideoCount(Long collectionId) {
        Long count = collectionItemMapper.countByCollectionId(collectionId);
        LambdaUpdateWrapper<DocVideoCollection> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(DocVideoCollection::getId, collectionId);
        wrapper.set(DocVideoCollection::getVideoCount, count.intValue());
        wrapper.set(DocVideoCollection::getUpdateName, SecurityUtils.getUserName());
        this.update(wrapper);
    }

    /**
     * 将合集实体列表转换为视图对象列表
     * <p>
     * 核心转换逻辑：
     * 1. 提取所有封面文件ID
     * 2. 批量查询文件表获取封面URL映射
     * 3. 将实体对象转换为视图对象，并填充封面文件信息
     * </p>
     *
     * @param list 合集实体列表
     * @return 合集视图对象列表（包含封面文件信息）
     */
    private List<DocVideoCollectionVo> convertToVoList(List<DocVideoCollection> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }

        // 提取所有非空的封面文件ID，用于批量查询
        List<Long> coverFileIds = list.stream()
                .map(DocVideoCollection::getCoverFileId)
                .filter(fid -> fid != null)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询封面文件，构建文件ID到文件对象的映射
        Map<Long, DocFiles> fileMap = new HashMap<>();
        if (!coverFileIds.isEmpty()) {
            List<DocFiles> files = docFilesMapper.selectList(
                    new LambdaQueryWrapper<DocFiles>().in(DocFiles::getId, coverFileIds)
            );
            for (DocFiles file : files) {
                fileMap.put(file.getId(), file);
            }
        }

        // 将实体列表转换为视图对象列表
        return list.stream().map(collection -> {
            DocVideoCollectionVo vo = new DocVideoCollectionVo();
            vo.setId(collection.getId());
            vo.setCollectionName(collection.getCollectionName());
            vo.setDescription(collection.getDescription());
            vo.setVideoCount(collection.getVideoCount());
            vo.setSortOrder(collection.getSortOrder());
            vo.setStatus(collection.getStatus());
            vo.setCreateTime(collection.getCreateTime());
            vo.setUpdateTime(collection.getUpdateTime());

            // 设置封面文件信息
            if (collection.getCoverFileId() != null) {
                DocFiles coverFile = fileMap.get(collection.getCoverFileId());
                if (coverFile != null) {
                    vo.setCover(DocFileInfoVo.builder()
                            .fileId(coverFile.getId())
                            .fileUrl(coverFile.getUrl())
                            .build());
                }
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 将视频实体列表转换为视频视图对象列表
     * <p>
     * 核心转换逻辑：
     * 1. 提取所有视频文件ID和封面文件ID
     * 2. 批量查询文件表获取文件URL映射
     * 3. 将实体对象转换为视图对象，并填充视频文件和缩略图信息
     * </p>
     *
     * @param videoList 视频实体列表
     * @return 视频视图对象列表（包含文件信息）
     */
    private List<DocVideoListVo> convertToVideoVoList(List<DocVideo> videoList) {
        if (videoList == null || videoList.isEmpty()) {
            return List.of();
        }

        // 提取所有非空的视频文件ID
        List<Long> videoFileIds = videoList.stream()
                .map(DocVideo::getFileId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        // 提取所有非空的封面文件ID
        List<Long> coverFileIds = videoList.stream()
                .map(DocVideo::getCoverFileId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        // 合并所有文件ID进行批量查询
        List<Long> allFileIds = new ArrayList<>();
        allFileIds.addAll(videoFileIds);
        allFileIds.addAll(coverFileIds);

        // 构建文件ID到文件对象的映射
        Map<Long, DocFiles> fileIdMap = new HashMap<>();
        if (!allFileIds.isEmpty()) {
            List<DocFiles> files = docFilesMapper.selectList(
                    new LambdaQueryWrapper<DocFiles>().in(DocFiles::getId, allFileIds)
            );
            for (DocFiles file : files) {
                fileIdMap.put(file.getId(), file);
            }
        }

        // 将实体列表转换为视图对象列表
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

            // 构建视频文件信息和缩略图信息
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
            vo.setVideoFile(videoFileVo);

            return vo;
        }).collect(Collectors.toList());
    }

}
