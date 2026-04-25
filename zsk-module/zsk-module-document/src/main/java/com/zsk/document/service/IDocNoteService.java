package com.zsk.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.vo.DocNoteListVo;
import com.zsk.document.domain.vo.DocStatsInfoVo;

import java.util.List;
import java.util.Map;

/**
 * 笔记Service接口
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
public interface IDocNoteService extends IService<DocNote> {

    /**
     * 获取草稿列表
     *
     * @param pageQuery 分页参数
     * @return 草稿列表
     */
    PageResult<DocNote> draftList(PageQuery pageQuery);

    /**
     * 批量更新状态
     *
     * @param ids    笔记ID列表
     * @param status 目标状态
     * @return 是否成功
     */
    boolean batchUpdateStatus(List<Long> ids, String status);

    /**
     * 批量迁移分类
     *
     * @param ids      笔记ID列表
     * @param category 目标分类
     * @return 是否成功
     */
    boolean batchMoveCategory(List<Long> ids, String category);

    /**
     * 切换置顶状态
     *
     * @param id 笔记ID
     * @return 是否成功
     */
    boolean togglePinned(Long id);

    /**
     * 切换推荐状态
     *
     * @param id 笔记ID
     * @return 是否成功
     */
    boolean toggleRecommended(Long id);

    /**
     * 查询笔记列表（带文件URL、作者信息和统计信息）
     * <p>
     * 根据查询条件获取笔记列表，并关联查询封面文件信息、作者信息和统计数据。
     * 采用批量查询策略优化性能，避免N+1查询问题。
     * </p>
     *
     * @param docNote 查询条件
     * @return 笔记列表（包含封面文件信息、作者信息和统计信息）
     */
    List<DocNoteListVo> listWithFileUrl(DocNote docNote);

    /**
     * 分页查询笔记列表（带文件URL、作者信息和统计信息）
     * <p>
     * 根据查询条件和分页参数获取笔记分页数据，并关联查询封面文件信息、作者信息和统计数据。
     * 采用批量查询策略优化性能。
     * </p>
     *
     * @param docNote   查询条件
     * @param pageQuery 分页参数
     * @return 分页结果（包含封面文件信息、作者信息和统计信息）
     */
    PageResult<DocNoteListVo> pageWithFileUrl(DocNote docNote, PageQuery pageQuery);

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
    DocNoteListVo getNoteDetail(Long noteId);

    /**
     * 填充笔记列表的作者信息
     * <p>
     * 批量查询用户信息并填充到笔记列表中，避免N+1查询问题。
     * 作者信息包括用户ID、名称（优先使用昵称，若无则使用用户名）和头像URL。
     * </p>
     *
     * @param noteList 笔记列表视图对象
     */
    void fillAuthorInfo(List<DocNoteListVo> noteList);

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
    DocStatsInfoVo getNoteStats(Long noteId);

    /**
     * 批量查询笔记统计信息
     * <p>
     * 批量查询多个笔记的点赞数、收藏数和浏览量，采用批量查询策略优化性能。
     * 数据来源于 Redis 缓存服务。
     * </p>
     *
     * @param noteIds 笔记ID列表
     * @return 笔记ID到统计信息的映射
     */
    Map<Long, DocStatsInfoVo> batchGetNoteStats(List<Long> noteIds);
}
