package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.document.domain.DocVideo;
import com.zsk.document.mapper.DocVideoMapper;
import com.zsk.document.service.IDocVideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 视频Service业务层处理
 *
 * @author wuhuaming
 * @date 2026-02-14
 */
@Slf4j
@Service
public class DocVideoServiceImpl extends ServiceImpl<DocVideoMapper, DocVideo> implements IDocVideoService {

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
}
