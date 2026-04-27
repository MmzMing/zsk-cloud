package com.zsk.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocVideoCollection;
import com.zsk.document.domain.dto.CollectionVideoSortDTO;
import com.zsk.document.domain.vo.DocVideoCollectionDtlVo;
import com.zsk.document.domain.vo.DocVideoCollectionVo;

import java.util.List;

/**
 * 视频合集Service接口
 * <p>
 * 定义视频合集模块的核心业务操作，包括合集的增删改查、视频加入/移除/排序等功能。
 * 所有操作均基于当前登录用户进行权限隔离，确保数据安全性。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-28
 */
public interface IDocVideoCollectionService extends IService<DocVideoCollection> {

    /**
     * 查询当前用户的合集列表
     * <p>
     * 根据当前登录用户ID查询其拥有的所有未删除合集。
     * 支持按状态（公开/私密）筛选，默认按排序值降序、创建时间降序排列。
     * </p>
     *
     * @param docVideoCollection 查询条件（可选：status状态筛选）
     * @return 合集视图对象列表（包含封面文件信息）
     */
    List<DocVideoCollectionVo> listByUser(DocVideoCollection docVideoCollection);

    /**
     * 分页查询当前用户的合集列表
     * <p>
     * 根据当前登录用户ID分页查询其拥有的所有未删除合集。
     * 支持按状态（公开/私密）筛选，默认按排序值降序、创建时间降序排列。
     * </p>
     *
     * @param docVideoCollection 查询条件（可选：status状态筛选）
     * @param pageQuery          分页参数（pageNum、pageSize）
     * @return 分页结果（包含封面文件信息）
     */
    PageResult<DocVideoCollectionVo> pageByUser(DocVideoCollection docVideoCollection, PageQuery pageQuery);

    /**
     * 获取合集详情（包含视频列表）
     * <p>
     * 根据合集ID获取详情信息，同时查询合集中的所有视频列表。
     * 视频列表按合集中的排序顺序排列，仅返回状态正常且审核通过的视频。
     * </p>
     *
     * @param id 合集ID
     * @return 合集详情视图对象（包含视频列表），合集不存在时返回 null
     */
    DocVideoCollectionDtlVo getCollectionDetail(Long id);

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
    Long createCollection(DocVideoCollection docVideoCollection);

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
    boolean updateCollection(DocVideoCollection docVideoCollection);

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
    boolean removeCollectionByIds(List<Long> ids);

    /**
     * 批量添加视频到合集
     * <p>
     * 将多个视频批量添加到指定合集中，自动过滤已存在的视频（防重复）。
     * 新添加的视频默认按顺序排在末尾，操作完成后自动更新合集视频数量。
     * </p>
     *
     * @param collectionId 合集ID
     * @param videoIds     视频ID列表
     * @return 是否添加成功
     */
    boolean addVideosToCollection(Long collectionId, List<Long> videoIds);

    /**
     * 批量从合集移除视频
     * <p>
     * 将多个视频从指定合集中移除，采用软删除策略。
     * 操作前校验合集所有权，操作完成后自动更新合集视频数量。
     * </p>
     *
     * @param collectionId 合集ID
     * @param videoIds     视频ID列表
     * @return 是否移除成功
     */
    boolean removeVideosFromCollection(Long collectionId, List<Long> videoIds);

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
    boolean sortCollectionVideos(Long collectionId, CollectionVideoSortDTO sortDTO);

}
