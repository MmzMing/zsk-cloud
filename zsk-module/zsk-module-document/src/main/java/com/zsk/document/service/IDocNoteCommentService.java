package com.zsk.document.service;

import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocNoteComment;
import com.zsk.document.domain.dto.CommentRequestDTO;
import com.zsk.document.domain.vo.DocCommentVo;
import com.zsk.document.domain.vo.InteractionResultVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 笔记评论Service接口
 * <p>
 * 提供笔记评论的增删改查、分页查询、评论列表获取、评论点赞等功能。
 * 采用B站式评论结构：所有回复统一挂在根评论下，不存在层级嵌套。
 * 评论点赞数从Redis获取，不再存储在数据库表中。
 * </p>
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-27
 */
public interface IDocNoteCommentService extends IService<DocNoteComment> {

    /**
     * 获取笔记评论列表（支持热门/最新排序）
     * <p>
     * 查询笔记的根评论列表，每个根评论包含其下的所有回复。
     * 回复统一挂在根评论下，不存在层级嵌套。
     * </p>
     *
     * @param noteId    笔记ID
     * @param pageQuery 分页查询参数
     * @param sort      排序方式（hot-按点赞数降序，其他-按创建时间降序）
     * @param userId    当前用户ID（可为null，未登录时传null）
     * @return 评论分页结果，包含根评论及其回复列表
     */
    PageResult<DocCommentVo> getCommentList(Long noteId, PageQuery pageQuery, String sort, Long userId);

    /**
     * 发表笔记评论
     * <p>
     * 支持发表根评论和回复评论。
     * 回复评论时，parentCommentId统一记录根评论ID，replyUserId记录被回复的用户ID。
     * </p>
     *
     * @param noteId     笔记ID
     * @param content    评论内容
     * @param parentId   父评论ID（根评论时传null，回复时传根评论ID）
     * @param replyToId  回复用户ID（直接回复根评论时传null，回复某条评论时传该评论的用户ID）
     * @param userId     当前用户ID
     * @return 评论VO，包含评论详情和作者信息
     */
    DocCommentVo postComment(Long noteId, String content, Long parentId, Long replyToId, Long userId);

    /**
     * 切换评论点赞状态
     * <p>
     * 用户点赞或取消点赞笔记评论。点赞数从Redis获取，不再同步到数据库。
     * </p>
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID
     * @return 点赞操作结果，包含操作是否成功、当前状态和最新点赞数
     */
    InteractionResultVo toggleCommentLike(Long commentId, Long userId);

    /**
     * 前台发表评论（带参数校验）
     * <p>
     * 封装前台发表评论的完整业务流程，包含参数校验、业务处理和结果构建。
     * Controller层直接调用此方法，无需处理业务逻辑。
     * </p>
     *
     * @param commentRequest 评论请求DTO
     * @param userId         当前用户ID
     * @return 评论VO
     */
    DocCommentVo postCommentWithValidation(CommentRequestDTO commentRequest, Long userId);

    /**
     * 批量构建评论VO
     * <p>
     * 将评论实体列表转换为评论VO列表，用于批量查询场景。
     * 会通过远程用户服务批量获取真实的用户信息（昵称、头像）。
     * </p>
     *
     * @param comments      评论实体列表
     * @param currentUserId 当前登录用户ID（可为null）
     * @return 评论VO列表
     */
    List<DocCommentVo> buildCommentVoList(List<DocNoteComment> comments, Long currentUserId);
}
