package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.core.exception.BusinessException;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocNoteComment;
import com.zsk.document.domain.dto.CommentRequestDTO;
import com.zsk.document.domain.vo.DocCommentVo;
import com.zsk.document.domain.vo.DocUserVo;
import com.zsk.document.domain.vo.InteractionResultVo;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.mapper.DocNoteCommentMapper;
import com.zsk.document.service.ICacheDocLikeService;
import com.zsk.document.service.IDocNoteCommentService;
import com.zsk.system.api.RemoteUserService;
import com.zsk.system.api.domain.SysUserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 笔记评论Service业务层处理
 * <p>
 * 采用B站式评论结构：所有回复统一挂在根评论下，不存在层级嵌套。
 * parentCommentId 统一记录根评论ID（NULL表示根评论），
 * replyUserId 记录被回复的用户ID（用于显示"A回复B"）。
 * 评论点赞数从Redis获取，不再存储在数据库表中。
 * </p>
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocNoteCommentServiceImpl extends ServiceImpl<DocNoteCommentMapper, DocNoteComment> implements IDocNoteCommentService {

    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 缓存点赞服务
     */
    private final ICacheDocLikeService cacheDocLikeService;

    /**
     * 远程用户服务
     */
    private final RemoteUserService remoteUserService;

    /**
     * 获取笔记评论列表（支持热门/最新排序）
     * <p>
     * 查询笔记的根评论列表，每个根评论包含其下的所有回复。
     * 采用B站式评论结构：所有回复统一挂在根评论下，不存在层级嵌套。
     * 热门排序时按Redis中的点赞数排序，最新排序时按创建时间降序。
     * </p>
     *
     * @param noteId    笔记ID
     * @param pageQuery 分页查询参数
     * @param sort      排序方式（hot-按点赞数降序，其他-按创建时间降序）
     * @param userId    当前用户ID（可为null，未登录时传null）
     * @return 评论分页结果，包含根评论及其回复列表
     */
    @Override
    public PageResult<DocCommentVo> getCommentList(Long noteId, PageQuery pageQuery, String sort, Long userId) {
        log.info("获取笔记评论列表, noteId={}, pageQuery={}, sort={}, userId={}", noteId, pageQuery, sort, userId);

        // 1. 构建分页对象
        Page<DocNoteComment> page = pageQuery.build();

        // 2. 构建查询条件：查询未删除的根评论（parentCommentId为null）
        LambdaQueryWrapper<DocNoteComment> wrapper = Wrappers.<DocNoteComment>lambdaQuery()
                .eq(DocNoteComment::getDeleted, 0)
                .eq(DocNoteComment::getNoteId, noteId)
                .isNull(DocNoteComment::getParentCommentId);

        // 3. 根据排序参数设置排序方式
        if ("hot".equals(sort)) {
            // 热门排序：按点赞数降序（由于点赞数在Redis，先按创建时间排序，后续再处理）
            wrapper.orderByDesc(DocNoteComment::getCreateTime);
        } else {
            // 默认排序：按创建时间降序（最新优先）
            wrapper.orderByDesc(DocNoteComment::getCreateTime);
        }

        // 4. 执行分页查询
        Page<DocNoteComment> resultPage = this.page(page, wrapper);
        List<DocNoteComment> rootComments = resultPage.getRecords();

        // 5. 如果按热门排序，需要从Redis获取点赞数并重新排序
        if ("hot".equals(sort) && !rootComments.isEmpty()) {
            rootComments.sort((a, b) -> {
                Long likeA = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), a.getId());
                Long likeB = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), b.getId());
                return likeB.compareTo(likeA);
            });
        }

        // 6. 构建评论VO列表（包含回复信息）
        List<DocCommentVo> voList = buildCommentVoListWithReplies(rootComments, userId);

        // 7. 构建并返回分页结果
        PageResult<DocCommentVo> pageResult = PageResult.of(voList, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());

        log.info("获取笔记评论列表成功, noteId={}, total={}", noteId, resultPage.getTotal());
        return pageResult;
    }

    /**
     * 发表笔记评论
     * <p>
     * 支持发表根评论和回复评论。
     * 回复评论时，parentCommentId统一记录根评论ID，replyUserId记录被回复的用户ID。
     * 如果回复的是根评论，则parentCommentId和replyUserId都为null。
     * 如果回复的是某条回复，则parentCommentId记录根评论ID，replyUserId记录被回复评论的用户ID。
     * </p>
     *
     * @param noteId     笔记ID
     * @param content    评论内容
     * @param parentId   父评论ID（根评论时传null，回复时传根评论ID）
     * @param replyToId  回复用户ID（直接回复根评论时传null，回复某条评论时传该评论的用户ID）
     * @param userId     当前用户ID
     * @return 评论VO，包含评论详情和作者信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocCommentVo postComment(Long noteId, String content, Long parentId, Long replyToId, Long userId) {
        log.info("发表笔记评论, noteId={}, content={}, parentId={}, replyToId={}, userId={}", noteId, content, parentId, replyToId, userId);

        // 1. 构建评论实体
        DocNoteComment comment = new DocNoteComment();
        comment.setNoteId(noteId);
        comment.setCommentUserId(userId);
        comment.setCommentContent(content);

        // 2. 处理回复逻辑
        if (parentId != null) {
            // 查询父评论，确认是根评论还是回复
            DocNoteComment parentComment = this.getById(parentId);
            if (parentComment != null) {
                if (parentComment.getParentCommentId() == null) {
                    // 父评论是根评论，直接回复根评论
                    comment.setParentCommentId(parentId);
                    comment.setReplyUserId(replyToId);
                } else {
                    // 父评论是回复，统一挂到根评论下
                    comment.setParentCommentId(parentComment.getParentCommentId());
                    comment.setReplyUserId(replyToId != null ? replyToId : parentComment.getCommentUserId());
                }
            }
        }

        // 3. 保存评论到数据库
        this.save(comment);
        log.info("评论发表成功, commentId={}", comment.getId());

        // 4. 构建并返回评论VO（单条评论查询用户信息）
        Map<Long, SysUserApi> userMap = fetchUserMap(Collections.singletonList(comment.getCommentUserId()));
        if (comment.getReplyUserId() != null) {
            Map<Long, SysUserApi> replyUserMap = fetchUserMap(Collections.singletonList(comment.getReplyUserId()));
            userMap.putAll(replyUserMap);
        }
        return buildCommentVo(comment, userId, userMap);
    }

    /**
     * 切换评论点赞状态
     * <p>
     * 用户点赞或取消点赞笔记评论。点赞数从Redis获取，不再同步到数据库表。
     * 使用事务保证操作的原子性。
     * </p>
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID
     * @return 点赞操作结果，包含操作是否成功、当前状态和最新点赞数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InteractionResultVo toggleCommentLike(Long commentId, Long userId) {
        log.info("切换评论点赞状态, commentId={}, userId={}", commentId, userId);

        // 1. 查询当前点赞状态
        boolean isLiked = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), commentId, userId);

        // 2. 执行相反操作
        boolean result;
        if (isLiked) {
            result = cacheDocLikeService.unlike(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), commentId, userId);
            log.info("取消评论点赞成功, commentId={}, userId={}", commentId, userId);
        } else {
            result = cacheDocLikeService.like(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), commentId, userId);
            log.info("评论点赞成功, commentId={}, userId={}", commentId, userId);
        }

        // 3. 获取最新的点赞数量（从Redis）
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), commentId);

        // 4. 构建并返回结果
        return InteractionResultVo.builder()
                .success(result)
                .status(result && !isLiked)
                .count(likeCount)
                .build();
    }

    /**
     * 构建评论VO
     * <p>
     * 将评论实体转换为评论VO，包含作者信息、点赞数（从Redis获取）、点赞状态、回复对象信息等。
     * </p>
     *
     * @param comment       评论实体
     * @param currentUserId 当前登录用户ID（可为null）
     * @param userMap       用户信息缓存Map（key: userId, value: SysUserApi）
     * @return 评论VO
     */
    private DocCommentVo buildCommentVo(DocNoteComment comment, Long currentUserId, Map<Long, SysUserApi> userMap) {
        log.debug("构建评论VO, commentId={}, currentUserId={}", comment.getId(), currentUserId);

        // 1. 创建评论VO对象
        DocCommentVo vo = new DocCommentVo();
        vo.setId(comment.getId());
        vo.setContent(comment.getCommentContent());
        vo.setCreatedAt(comment.getCreateTime() != null ? comment.getCreateTime().format(DATE_FORMATTER) : "");

        // 2. 从Redis获取评论点赞数
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), comment.getId());
        vo.setLikes(likeCount != null ? likeCount.intValue() : 0);

        // 3. 查询当前用户是否点赞该评论
        if (currentUserId != null) {
            vo.setIsLiked(cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.NOTE_COMMENT.getCode(), comment.getId(), currentUserId));
        } else {
            vo.setIsLiked(false);
        }

        // 4. 构建评论作者信息（从远程用户服务获取）
        SysUserApi authorUser = userMap.get(comment.getCommentUserId());
        DocUserVo author = new DocUserVo();
        author.setId(comment.getCommentUserId());
        if (authorUser != null) {
            author.setName(authorUser.getNickName() != null ? authorUser.getNickName() : authorUser.getUserName());
            author.setAvatar(authorUser.getAvatar());
        } else {
            author.setName("用户" + comment.getCommentUserId());
            author.setAvatar("");
        }
        vo.setAuthor(author);

        // 5. 设置回复对象信息（如果有replyUserId）
        if (comment.getReplyUserId() != null) {
            SysUserApi replyUser = userMap.get(comment.getReplyUserId());
            DocUserVo replyTo = new DocUserVo();
            replyTo.setId(comment.getReplyUserId());
            if (replyUser != null) {
                replyTo.setName(replyUser.getNickName() != null ? replyUser.getNickName() : replyUser.getUserName());
                replyTo.setAvatar(replyUser.getAvatar());
            } else {
                replyTo.setName("用户" + comment.getReplyUserId());
                replyTo.setAvatar("");
            }
            vo.setReplyTo(replyTo);
        }

        // 6. 设置评论状态
        vo.setStatus(comment.getStatus());

        return vo;
    }

    /**
     * 批量获取用户信息
     * <p>
     * 通过远程用户服务批量查询用户信息，避免循环调用RPC。
     * </p>
     *
     * @param userIds 用户ID列表
     * @return 用户信息Map（key: userId, value: SysUserApi）
     */
    private Map<Long, SysUserApi> fetchUserMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }

        // 去重并过滤null
        List<Long> distinctIds = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (distinctIds.isEmpty()) {
            return new HashMap<>();
        }

        try {
            var result = remoteUserService.listByIds(distinctIds);
            if (result != null && result.getData() != null) {
                return result.getData().stream()
                        .collect(Collectors.toMap(SysUserApi::getId, user -> user, (a, b) -> a));
            }
        } catch (Exception e) {
            log.error("批量获取用户信息失败, userIds={}", distinctIds, e);
        }

        return new HashMap<>();
    }

    /**
     * 批量构建评论VO
     * <p>
     * 将评论实体列表转换为评论VO列表，用于批量查询场景。
     * 会批量查询所有相关用户信息，避免循环RPC调用。
     * </p>
     *
     * @param comments      评论实体列表
     * @param currentUserId 当前登录用户ID（可为null）
     * @return 评论VO列表
     */
    @Override
    public List<DocCommentVo> buildCommentVoList(List<DocNoteComment> comments, Long currentUserId) {
        log.debug("批量构建评论VO, commentCount={}, currentUserId={}", comments != null ? comments.size() : 0, currentUserId);

        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 收集所有需要查询的用户ID（评论作者 + 回复对象）
        List<Long> allUserIds = new ArrayList<>();
        for (DocNoteComment comment : comments) {
            if (comment.getCommentUserId() != null) {
                allUserIds.add(comment.getCommentUserId());
            }
            if (comment.getReplyUserId() != null) {
                allUserIds.add(comment.getReplyUserId());
            }
        }

        // 2. 批量获取用户信息
        Map<Long, SysUserApi> userMap = fetchUserMap(allUserIds);

        // 3. 构建VO列表
        return comments.stream()
                .map(comment -> buildCommentVo(comment, currentUserId, userMap))
                .collect(Collectors.toList());
    }

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocCommentVo postCommentWithValidation(CommentRequestDTO commentRequest, Long userId) {
        log.info("前台发表评论, docId={}, userId={}", commentRequest.getDocId(), userId);

        // 1. 参数校验
        if (commentRequest.getDocId() == null) {
            throw new BusinessException("笔记ID不能为空");
        }
        if (commentRequest.getContent() == null || commentRequest.getContent().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }

        // 2. 提取参数
        Long docId = commentRequest.getDocId();
        String content = commentRequest.getContent();
        Long parentId = commentRequest.getParentId();
        Long replyToId = commentRequest.getReplyToId();

        // 3. 调用核心发表逻辑
        DocCommentVo commentVo = postComment(docId, content, parentId, replyToId, userId);

        log.info("前台发表评论成功, commentId={}", commentVo.getId());
        return commentVo;
    }

    /**
     * 构建包含回复的评论VO列表
     * <p>
     * 将根评论列表转换为评论VO列表，每个根评论包含其下的所有回复。
     * 采用B站式评论结构：所有回复统一挂在根评论下，不存在层级嵌套。
     * </p>
     *
     * @param rootComments 根评论列表
     * @param userId       当前用户ID（可为null）
     * @return 评论VO列表，每个VO包含其回复列表
     */
    private List<DocCommentVo> buildCommentVoListWithReplies(List<DocNoteComment> rootComments, Long userId) {
        log.debug("构建包含回复的评论VO列表, rootCommentCount={}, userId={}", rootComments != null ? rootComments.size() : 0, userId);

        // 1. 处理空列表情况
        if (rootComments == null || rootComments.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 提取所有根评论ID
        List<Long> rootCommentIds = rootComments.stream()
                .map(DocNoteComment::getId)
                .collect(Collectors.toList());

        // 3. 批量查询所有回复评论（统一挂在根评论下）
        List<DocNoteComment> allReplies = this.list(
                Wrappers.<DocNoteComment>lambdaQuery()
                        .in(DocNoteComment::getParentCommentId, rootCommentIds)
                        .eq(DocNoteComment::getDeleted, 0)
                        .orderByAsc(DocNoteComment::getCreateTime)
        );

        // 4. 将回复按根评论ID分组
        Map<Long, List<DocNoteComment>> replyMap = new HashMap<>();
        for (DocNoteComment reply : allReplies) {
            replyMap.computeIfAbsent(reply.getParentCommentId(), k -> new ArrayList<>()).add(reply);
        }

        // 5. 收集所有需要查询的用户ID（根评论作者 + 回复作者 + 回复对象）
        List<Long> allUserIds = new ArrayList<>();
        for (DocNoteComment rootComment : rootComments) {
            if (rootComment.getCommentUserId() != null) {
                allUserIds.add(rootComment.getCommentUserId());
            }
            if (rootComment.getReplyUserId() != null) {
                allUserIds.add(rootComment.getReplyUserId());
            }
        }
        for (DocNoteComment reply : allReplies) {
            if (reply.getCommentUserId() != null) {
                allUserIds.add(reply.getCommentUserId());
            }
            if (reply.getReplyUserId() != null) {
                allUserIds.add(reply.getReplyUserId());
            }
        }

        // 6. 批量获取所有用户信息
        Map<Long, SysUserApi> userMap = fetchUserMap(allUserIds);

        // 7. 构建根评论VO列表，并为每个根评论设置回复列表
        List<DocCommentVo> result = new ArrayList<>();
        for (DocNoteComment rootComment : rootComments) {
            DocCommentVo rootVo = buildCommentVo(rootComment, userId, userMap);

            // 构建该根评论下的回复VO列表
            List<DocNoteComment> replies = replyMap.getOrDefault(rootComment.getId(), new ArrayList<>());
            List<DocCommentVo> replyVos = replies.stream()
                    .map(reply -> buildCommentVo(reply, userId, userMap))
                    .collect(Collectors.toList());
            rootVo.setReplies(replyVos);

            result.add(rootVo);
        }

        return result;
    }
}
