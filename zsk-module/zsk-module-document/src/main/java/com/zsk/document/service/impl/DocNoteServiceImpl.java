package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocNote;
import com.zsk.document.mapper.DocNoteMapper;
import com.zsk.document.service.IDocNoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 笔记Service业务层处理
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Slf4j
@Service
public class DocNoteServiceImpl extends ServiceImpl<DocNoteMapper, DocNote> implements IDocNoteService {

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
