package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocFiles;
import com.zsk.document.domain.DocVideo;
import com.zsk.document.domain.vo.DocFileInfoVo;
import com.zsk.document.domain.vo.DocVideoFileVo;
import com.zsk.document.domain.vo.DocVideoListVo;
import com.zsk.document.mapper.DocFilesMapper;
import com.zsk.document.mapper.DocVideoMapper;
import com.zsk.document.service.IDocVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
     * 查询视频列表（带文件URL）
     * <p>
     * 根据查询条件获取视频列表，并关联查询视频文件和缩略图文件信息，返回包含文件URL的视图对象列表。
     * 采用批量查询策略优化性能，避免N+1查询问题。
     * </p>
     *
     * @param docVideo 查询条件对象（支持videoTitle、broadCode、narrowCode等字段过滤）
     * @return 视频列表视图对象（包含文件信息）
     */
    @Override
    public List<DocVideoListVo> listWithFileUrl(DocVideo docVideo) {
        log.info("查询视频列表（带文件URL）");
        List<DocVideo> videoList = this.list(new LambdaQueryWrapper<>(docVideo));
        return convertToVoList(videoList);
    }

    /**
     * 分页查询视频列表（带文件URL）
     * <p>
     * 根据查询条件和分页参数获取视频分页数据，并关联查询视频文件和缩略图文件信息。
     * 采用批量查询策略优化性能。
     * </p>
     *
     * @param docVideo  查询条件对象
     * @param pageQuery 分页参数（包含pageNum、pageSize）
     * @return 分页结果（包含文件信息）
     */
    @Override
    public PageResult<DocVideoListVo> pageWithFileUrl(DocVideo docVideo, PageQuery pageQuery) {
        log.info("分页查询视频列表（带文件URL）, pageNum={}, pageSize={}", pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<DocVideo> page = pageQuery.build();
        LambdaQueryWrapper<DocVideo> lqw = new LambdaQueryWrapper<>(docVideo);
        lqw.orderByDesc(DocVideo::getCreateTime);
        Page<DocVideo> resultPage = this.page(page, lqw);
        List<DocVideoListVo> voList = convertToVoList(resultPage.getRecords());
        return PageResult.of(voList, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
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
        return PageResult.of(voList, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    /**
     * 获取视频详细信息（带文件URL）
     * <p>
     * 根据视频ID获取详情，并关联查询视频文件和缩略图文件信息。
     * 一对一绑定关系：一个视频对应一个视频文件和一个缩略图文件。
     * </p>
     *
     * @param id 视频ID
     * @return 视频详情（包含文件信息）
     */
    @Override
    public DocVideoListVo getByIdWithFileUrl(Long id) {
        log.info("获取视频详细信息（带文件URL）, id={}", id);
        if (id == null) {
            log.warn("获取视频详细信息失败, ID为空");
            return null;
        }

        DocVideo video = this.getById(id);
        if (video == null) {
            log.warn("获取视频详细信息失败, 视频不存在, id={}", id);
            return null;
        }

        // 将单个视频实体转换为视图对象
        List<DocVideoListVo> voList = convertToVoList(List.of(video));
        return voList.isEmpty() ? null : voList.get(0);
    }

    /**
     * 将视频实体列表转换为视图对象列表（带文件URL）
     * <p>
     * 核心转换逻辑：
     * 1. 提取所有视频的文件ID（视频文件ID和封面文件ID）
     * 2. 批量查询文件表获取文件URL映射
     * 3. 将实体对象转换为视图对象，并填充文件信息
     * </p>
     * <p>
     * 一对一绑定关系说明：
     * - 一个视频对应一个视频文件（通过fileId关联）
     * - 一个视频对应一个缩略图文件（通过coverFileId关联）
     * </p>
     *
     * @param videoList 视频实体列表
     * @return 视频视图对象列表（包含文件信息）
     */
    private List<DocVideoListVo> convertToVoList(List<DocVideo> videoList) {
        // 空列表直接返回空结果
        if (videoList == null || videoList.isEmpty()) {
            return List.of();
        }

        // 提取所有非空的文件ID（视频文件ID，关联主键id），用于批量查询
        List<Long> videoFileIds = videoList.stream()
                .map(DocVideo::getFileId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        // 提取所有非空的封面文件ID（关联主键id），用于批量查询
        List<Long> coverFileIds = videoList.stream()
                .map(DocVideo::getCoverFileId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        // 合并所有文件ID进行批量查询（都关联主键id）
        List<Long> allFileIds = new java.util.ArrayList<>();
        allFileIds.addAll(videoFileIds);
        allFileIds.addAll(coverFileIds);

        // 构建文件ID到文件对象的映射（一对一绑定）
        Map<Long, DocFiles> fileIdMap = new HashMap<>();
        if (!allFileIds.isEmpty()) {
            List<DocFiles> files = docFilesMapper.selectList(
                    new LambdaQueryWrapper<DocFiles>().in(DocFiles::getId, allFileIds)
            );
            for (DocFiles file : files) {
                fileIdMap.put(file.getId(), file);
            }
        }

        // 将实体转换为视图对象
        return videoList.stream().map(video -> {
            DocVideoListVo vo = new DocVideoListVo();
            // 复制基本属性
            BeanUtils.copyProperties(video, vo);

            // 构建视频文件信息（一对一绑定：一个视频对应一个视频文件）
            DocVideoFileVo videoFileVo = DocVideoFileVo.builder()
                    .build();

            // 设置视频文件信息（通过主键id关联）
            if (video.getFileId() != null) {
                DocFiles videoFile = fileIdMap.get(video.getFileId());
                videoFileVo.setVideo(DocFileInfoVo.builder()
                        .fileId(video.getFileId())
                        .fileUrl(videoFile != null ? videoFile.getUrl() : null)
                        .build());
            }

            // 设置缩略图文件信息（一对一绑定：通过主键id关联）
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
