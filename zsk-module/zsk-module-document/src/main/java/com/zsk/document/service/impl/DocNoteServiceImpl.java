package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocFiles;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.vo.DocFileInfoVo;
import com.zsk.document.domain.vo.DocNoteListVo;
import com.zsk.document.mapper.DocFilesMapper;
import com.zsk.document.mapper.DocNoteMapper;
import com.zsk.document.service.IDocNoteService;
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


    /**
     * 查询笔记列表（带文件URL）
     * <p>
     * 根据查询条件获取笔记列表，并关联查询封面文件信息，返回包含文件URL的视图对象列表。
     * 采用批量查询策略优化性能，避免N+1查询问题。
     * </p>
     *
     * @param docNote 查询条件对象（支持noteName、broadCode、narrowCode等字段过滤）
     * @return 笔记列表视图对象（包含封面文件信息）
     */
    @Override
    public List<DocNoteListVo> listWithFileUrl(DocNote docNote) {
        log.info("查询笔记列表（带文件URL）");
        List<DocNote> noteList = this.list(new LambdaQueryWrapper<>(docNote));
        return convertToVoList(noteList);
    }

    /**
     * 分页查询笔记列表（带文件URL）
     * <p>
     * 根据查询条件和分页参数获取笔记分页数据，并关联查询封面文件信息。
     * 采用批量查询策略优化性能。
     * </p>
     *
     * @param docNote   查询条件对象
     * @param pageQuery 分页参数（包含pageNum、pageSize）
     * @return 分页结果（包含封面文件信息）
     */
    @Override
    public PageResult<DocNoteListVo> pageWithFileUrl(DocNote docNote, PageQuery pageQuery) {
        log.info("分页查询笔记列表（带文件URL）, pageNum={}, pageSize={}", pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<DocNote> page = pageQuery.build();
        Page<DocNote> resultPage = this.page(page, new LambdaQueryWrapper<>(docNote));
        List<DocNoteListVo> voList = convertToVoList(resultPage.getRecords());
        return PageResult.of(voList, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
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
}
