package com.zsk.document.service.impl;

import com.zsk.common.core.exception.BusinessException;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocNoteDtl;
import com.zsk.document.domain.dto.CommentRequestDTO;
import com.zsk.document.domain.dto.DocHomeNoteCommentPostDto;
import com.zsk.document.domain.vo.*;
import com.zsk.document.enums.CacheDocCollectTypeEnum;
import com.zsk.document.enums.CacheDocFollowTypeEnum;
import com.zsk.document.enums.CacheDocLikeTypeEnum;
import com.zsk.document.enums.CacheDocViewTypeEnum;
import com.zsk.document.mapper.DocNoteMapper;
import com.zsk.document.service.*;
import com.zsk.system.api.RemoteUserService;
import com.zsk.system.api.domain.SysUserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 前台首页笔记聚合服务实现类
 * <p>
 * 前台首页笔记详情的聚合服务实现，负责从数据库和Redis缓存中拼装数据。
 * 本服务是前台聚合层，仅调用其他已有Service方法进行数据拼装，
 * 不在其他Service中编写方法供本服务调用，确保前后台逻辑隔离。
 * </p>
 * <p>
 * 三大区域化接口：
 * 1. 笔记元信息+详情：从数据库查询笔记基本信息和内容，优先增加浏览量
 * 2. 点赞收藏+作者关注信息：从Redis缓存获取交互数据和作者信息
 * 3. 评论区域：调用评论Service获取二级结构评论列表
 * </p>
 * <p>
 * 所有交互数据（浏览量、点赞数、收藏数、粉丝数）均通过 Redis 缓存服务获取，
 * 不依赖主表字段，确保数据实时性。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocHomeNoteServiceImpl implements IDocHomeNoteService {

    /**
     * 笔记Mapper
     */
    private final DocNoteMapper noteMapper;

    /**
     * 缓存浏览服务
     */
    private final ICacheDocViewService cacheDocViewService;

    /**
     * 缓存点赞服务
     */
    private final ICacheDocLikeService cacheDocLikeService;

    /**
     * 缓存收藏服务
     */
    private final ICacheDocCollectService cacheDocCollectService;

    /**
     * 缓存关注服务
     */
    private final ICacheDocFollowService cacheDocFollowService;

    /**
     * 笔记详情服务（用于获取笔记Markdown内容）
     */
    private final IDocNoteDtlService noteDtlService;

    /**
     * 笔记评论服务（用于获取评论列表和发表评论）
     */
    private final IDocNoteCommentService docNoteCommentService;

    /**
     * 远程用户服务（用于获取用户昵称、头像等公开信息）
     */
    private final RemoteUserService remoteUserService;

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 区域一：笔记元信息+详情 ====================

    /**
     * 获取笔记元信息+详情
     * <p>
     * 查询笔记的基本信息和内容详情，仅返回前台展示所需字段。
     * 获取元信息时优先增加浏览量（Redis），确保浏览量统计准确。
     * 不包含审核状态、版本号、后台标记等敏感字段。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（可为null，未登录时传null）
     * @return 笔记详情VO，如果笔记不存在返回null
     */
    @Override
    public DocHomeNoteDetailVo getNoteDetail(Long noteId, Long userId) {
        log.info("获取笔记元信息+详情, noteId={}, userId={}", noteId, userId);

        // 1. 优先增加浏览量（Redis），无论后续查询是否成功都记录浏览
        cacheDocViewService.view(CacheDocViewTypeEnum.NOTE.getCode(), noteId, userId);
        log.debug("浏览量已增加, noteId={}", noteId);

        // 2. 查询笔记基本信息
        DocNote note = noteMapper.selectById(noteId);
        if (note == null) {
            log.warn("笔记不存在, noteId={}", noteId);
            return null;
        }

        // 3. 查询笔记内容详情
        String content = "";
        DocNoteDtl dtl = noteDtlService.getByNoteId(noteId);
        if (dtl != null && dtl.getContent() != null) {
            content = dtl.getContent();
        }

        // 4. 组装笔记详情VO（仅包含前台展示字段，不含敏感数据）
        DocHomeNoteDetailVo vo = new DocHomeNoteDetailVo();
        vo.setId(note.getId());
        vo.setTitle(note.getNoteName());
        vo.setContent(content);
        vo.setCategory(note.getBroadCode());
        vo.setTags(note.getNoteTags());
        vo.setDescription(note.getDescription());
        vo.setDate(note.getCreateTime() != null ? note.getCreateTime().format(DATE_FORMATTER) : "");

        log.info("获取笔记元信息+详情成功, noteId={}", noteId);
        return vo;
    }

    // ==================== 区域二：点赞收藏+作者关注信息 ====================

    /**
     * 获取笔记交互信息（点赞收藏+作者关注）
     * <p>
     * 独立查询笔记的交互统计数据和作者关注信息。
     * 浏览量、点赞数、收藏数分别从Redis缓存独立查询。
     * 当前用户的点赞、收藏状态从Redis Bitmap查询。
     * 作者信息（昵称、头像）从远程用户服务获取，粉丝数和关注状态从Redis获取。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（可为null，未登录时传null）
     * @return 笔记交互信息VO，如果笔记不存在返回null
     */
    @Override
    public DocHomeNoteInteractionVo getNoteInteraction(Long noteId, Long userId) {
        log.info("获取笔记交互信息, noteId={}, userId={}", noteId, userId);

        // 1. 验证笔记是否存在
        DocNote note = noteMapper.selectById(noteId);
        if (note == null) {
            log.warn("笔记不存在, noteId={}", noteId);
            return null;
        }

        // 2. 从Redis独立查询浏览量
        Long viewCount = cacheDocViewService.getViewCount(CacheDocViewTypeEnum.NOTE.getCode(), noteId);
        log.debug("浏览量查询完成, noteId={}, viewCount={}", noteId, viewCount);

        // 3. 从Redis独立查询点赞数和点赞状态
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE.getCode(), noteId);
        boolean isLiked = userId != null && cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.NOTE.getCode(), noteId, userId);
        log.debug("点赞数据查询完成, noteId={}, likeCount={}, isLiked={}", noteId, likeCount, isLiked);

        // 4. 从Redis独立查询收藏数和收藏状态
        Long favoriteCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.NOTE.getCode(), noteId);
        boolean isFavorited = userId != null && cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.NOTE.getCode(), noteId, userId);
        log.debug("收藏数据查询完成, noteId={}, favoriteCount={}, isFavorited={}", noteId, favoriteCount, isFavorited);

        // 5. 构建作者信息（包含粉丝数和关注状态）
        DocHomeNoteAuthorVo authorVo = buildAuthorVo(note.getUserId(), userId);

        // 6. 组装交互信息VO
        DocHomeNoteInteractionVo vo = new DocHomeNoteInteractionVo();
        vo.setViewCount(viewCount);
        vo.setLikeCount(likeCount);
        vo.setFavoriteCount(favoriteCount);
        vo.setIsLiked(isLiked);
        vo.setIsFavorited(isFavorited);
        vo.setAuthor(authorVo);

        log.info("获取笔记交互信息成功, noteId={}", noteId);
        return vo;
    }

    // ==================== 区域三：评论区域 ====================

    /**
     * 获取笔记评论列表（分页，二级结构）
     * <p>
     * 调用评论Service获取二级结构评论列表，然后转换为前台展示VO。
     * 采用B站式二级评论结构：根评论包含回复列表，回复统一挂在根评论下。
     * </p>
     *
     * @param noteId    笔记ID
     * @param pageQuery 分页查询参数
     * @param sort      排序方式（hot-按点赞数降序，new-按创建时间降序）
     * @param userId    当前用户ID（可为null，未登录时传null）
     * @return 评论分页结果
     */
    @Override
    public PageResult<DocHomeNoteCommentVo> getNoteComments(Long noteId, PageQuery pageQuery, String sort, Long userId) {
        log.info("获取笔记评论列表, noteId={}, pageQuery={}, sort={}, userId={}", noteId, pageQuery, sort, userId);

        // 调用评论Service获取评论列表（已包含二级结构和用户信息）
        PageResult<DocCommentVo> commentPage = docNoteCommentService.getCommentList(noteId, pageQuery, sort, userId);

        // 转换为前台首页评论VO
        List<DocHomeNoteCommentVo> homeCommentVos = commentPage.getList().stream()
                .map(this::convertToHomeCommentVo)
                .collect(Collectors.toList());

        log.info("获取笔记评论列表成功, noteId={}, total={}", noteId, commentPage.getTotal());
        return PageResult.of(homeCommentVos, commentPage.getTotal(), commentPage.getPageNum(), commentPage.getPageSize());
    }

    // ==================== 交互操作：点赞、收藏、关注 ====================

    /**
     * 切换笔记点赞状态
     * <p>
     * 通过Redis缓存服务操作点赞状态，先查询当前状态再执行相反操作。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（不能为空）
     * @return 点赞操作结果
     */
    @Override
    public DocHomeNoteInteractionResultVo toggleNoteLike(Long noteId, Long userId) {
        log.info("切换笔记点赞状态, noteId={}, userId={}", noteId, userId);

        // 1. 查询当前点赞状态
        boolean isLiked = cacheDocLikeService.hasLiked(CacheDocLikeTypeEnum.NOTE.getCode(), noteId, userId);

        // 2. 执行相反操作
        if (isLiked) {
            cacheDocLikeService.unlike(CacheDocLikeTypeEnum.NOTE.getCode(), noteId, userId);
            log.info("取消笔记点赞成功, noteId={}, userId={}", noteId, userId);
        } else {
            cacheDocLikeService.like(CacheDocLikeTypeEnum.NOTE.getCode(), noteId, userId);
            log.info("笔记点赞成功, noteId={}, userId={}", noteId, userId);
        }

        // 3. 获取最新点赞数
        Long likeCount = cacheDocLikeService.getLikeCount(CacheDocLikeTypeEnum.NOTE.getCode(), noteId);

        // 4. 构建并返回结果
        return DocHomeNoteInteractionResultVo.builder()
                .success(true)
                .status(!isLiked)
                .count(likeCount)
                .build();
    }

    /**
     * 切换笔记收藏状态
     * <p>
     * 通过Redis缓存服务操作收藏状态，先查询当前状态再执行相反操作。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（不能为空）
     * @return 收藏操作结果
     */
    @Override
    public DocHomeNoteInteractionResultVo toggleNoteFavorite(Long noteId, Long userId) {
        log.info("切换笔记收藏状态, noteId={}, userId={}", noteId, userId);

        // 1. 查询当前收藏状态
        boolean isFavorited = cacheDocCollectService.hasCollected(CacheDocCollectTypeEnum.NOTE.getCode(), noteId, userId);

        // 2. 执行相反操作
        if (isFavorited) {
            cacheDocCollectService.uncollect(CacheDocCollectTypeEnum.NOTE.getCode(), noteId, userId);
            log.info("取消笔记收藏成功, noteId={}, userId={}", noteId, userId);
        } else {
            cacheDocCollectService.collect(CacheDocCollectTypeEnum.NOTE.getCode(), noteId, userId);
            log.info("笔记收藏成功, noteId={}, userId={}", noteId, userId);
        }

        // 3. 获取最新收藏数
        Long collectCount = cacheDocCollectService.getCollectCount(CacheDocCollectTypeEnum.NOTE.getCode(), noteId);

        // 4. 构建并返回结果
        return DocHomeNoteInteractionResultVo.builder()
                .success(true)
                .status(!isFavorited)
                .count(collectCount)
                .build();
    }

    /**
     * 切换关注作者状态
     * <p>
     * 通过Redis缓存服务操作关注状态，先查询当前状态再执行相反操作。
     * </p>
     *
     * @param authorId 作者ID
     * @param userId   当前用户ID（不能为空）
     * @return 关注操作结果
     */
    @Override
    public DocHomeNoteInteractionResultVo toggleFollowAuthor(Long authorId, Long userId) {
        log.info("切换关注作者状态, authorId={}, userId={}", authorId, userId);

        // 1. 查询当前关注状态
        boolean isFollowing = cacheDocFollowService.hasFollowed(CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), authorId, userId);

        // 2. 执行相反操作
        if (isFollowing) {
            cacheDocFollowService.unfollow(CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), authorId, userId);
            log.info("取消关注作者成功, authorId={}, userId={}", authorId, userId);
        } else {
            cacheDocFollowService.follow(CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), authorId, userId);
            log.info("关注作者成功, authorId={}, userId={}", authorId, userId);
        }

        // 3. 获取最新粉丝数
        Long followCount = cacheDocFollowService.getFollowCount(CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), authorId);

        // 4. 构建并返回结果
        return DocHomeNoteInteractionResultVo.builder()
                .success(true)
                .status(!isFollowing)
                .count(followCount)
                .build();
    }

    // ==================== 评论操作：发表/回复、点赞 ====================

    /**
     * 发表/回复笔记评论
     * <p>
     * 发表根评论和回复评论使用同一个接口。
     * 转换为评论Service的DTO后调用已有方法处理业务逻辑。
     * </p>
     *
     * @param dto    评论请求DTO
     * @param userId 当前用户ID（不能为空）
     * @return 评论VO
     */
    @Override
    public DocHomeNoteCommentVo postComment(DocHomeNoteCommentPostDto dto, Long userId) {
        log.info("发表/回复笔记评论, noteId={}, userId={}", dto.getNoteId(), userId);

        // 1. 参数校验
        if (dto.getNoteId() == null) {
            throw new BusinessException("笔记ID不能为空");
        }
        if (dto.getContent() == null || dto.getContent().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }

        // 2. 转换为评论Service的DTO
        CommentRequestDTO commentRequest = new CommentRequestDTO();
        commentRequest.setDocId(dto.getNoteId());
        commentRequest.setContent(dto.getContent());
        commentRequest.setParentId(dto.getParentId());
        commentRequest.setReplyToId(dto.getReplyToId());

        // 3. 调用评论Service处理业务逻辑
        DocCommentVo commentVo = docNoteCommentService.postCommentWithValidation(commentRequest, userId);

        // 4. 转换为前台首页评论VO
        DocHomeNoteCommentVo homeCommentVo = convertToHomeCommentVo(commentVo);

        log.info("发表/回复笔记评论成功, commentId={}", homeCommentVo.getId());
        return homeCommentVo;
    }

    /**
     * 切换评论点赞状态
     * <p>
     * 通过评论Service操作评论点赞，然后转换为前台结果VO。
     * </p>
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID（不能为空）
     * @return 点赞操作结果
     */
    @Override
    public DocHomeNoteInteractionResultVo toggleCommentLike(Long commentId, Long userId) {
        log.info("切换评论点赞状态, commentId={}, userId={}", commentId, userId);

        // 调用评论Service处理评论点赞
        InteractionResultVo result = docNoteCommentService.toggleCommentLike(commentId, userId);

        // 转换为前台首页交互结果VO
        return DocHomeNoteInteractionResultVo.builder()
                .success(result.isSuccess())
                .status(result.isStatus())
                .count(result.getCount())
                .build();
    }

    // ==================== 私有方法：数据组装 ====================

    /**
     * 构建作者信息VO
     * <p>
     * 从远程用户服务获取作者昵称和头像，从Redis获取粉丝数和关注状态。
     * 如果远程服务调用失败，使用降级信息。
     * </p>
     *
     * @param authorId 作者用户ID
     * @param userId   当前用户ID（可为null）
     * @return 作者信息VO
     */
    private DocHomeNoteAuthorVo buildAuthorVo(Long authorId, Long userId) {
        log.debug("构建作者信息VO, authorId={}, userId={}", authorId, userId);

        DocHomeNoteAuthorVo authorVo = new DocHomeNoteAuthorVo();
        authorVo.setId(authorId);

        // 1. 从远程用户服务获取作者昵称和头像
        SysUserApi authorUser = fetchUserById(authorId);
        if (authorUser != null) {
            authorVo.setName(authorUser.getNickName() != null ? authorUser.getNickName() : authorUser.getUserName());
            authorVo.setAvatar(authorUser.getAvatar());
        } else {
            authorVo.setName("用户" + authorId);
            authorVo.setAvatar("");
        }

        // 2. 从Redis获取作者粉丝数
        Long fansCount = cacheDocFollowService.getFollowCount(CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), authorId);
        authorVo.setFans(fansCount.intValue());

        // 3. 从Redis查询当前用户是否关注作者
        if (userId != null) {
            boolean isFollowing = cacheDocFollowService.hasFollowed(CacheDocFollowTypeEnum.NOTE_AUTHOR.getCode(), authorId, userId);
            authorVo.setIsFollowing(isFollowing);
        } else {
            authorVo.setIsFollowing(false);
        }

        return authorVo;
    }

    /**
     * 转换评论VO为前台首页评论VO
     * <p>
     * 将评论Service返回的DocCommentVo转换为前台首页的DocHomeNoteCommentVo，
     * 同时递归转换回复列表和作者信息。
     * </p>
     *
     * @param commentVo 评论VO
     * @return 前台首页评论VO
     */
    private DocHomeNoteCommentVo convertToHomeCommentVo(DocCommentVo commentVo) {
        if (commentVo == null) {
            return null;
        }

        DocHomeNoteCommentVo vo = new DocHomeNoteCommentVo();
        vo.setId(commentVo.getId());
        vo.setContent(commentVo.getContent());
        vo.setCreatedAt(commentVo.getCreatedAt());
        vo.setLikes(commentVo.getLikes());
        vo.setIsLiked(commentVo.getIsLiked());

        // 转换作者信息
        vo.setAuthor(convertToHomeAuthorVo(commentVo.getAuthor()));

        // 转换回复目标用户信息
        vo.setReplyTo(convertToHomeAuthorVo(commentVo.getReplyTo()));

        // 递归转换回复列表
        if (commentVo.getReplies() != null && !commentVo.getReplies().isEmpty()) {
            List<DocHomeNoteCommentVo> replyVos = commentVo.getReplies().stream()
                    .map(this::convertToHomeCommentVo)
                    .collect(Collectors.toList());
            vo.setReplies(replyVos);
        } else {
            vo.setReplies(new ArrayList<>());
        }

        return vo;
    }

    /**
     * 转换用户VO为前台首页作者VO
     * <p>
     * 将DocUserVo转换为DocHomeNoteAuthorVo，保持字段映射一致。
     * </p>
     *
     * @param userVo 用户VO
     * @return 前台首页作者VO
     */
    private DocHomeNoteAuthorVo convertToHomeAuthorVo(DocUserVo userVo) {
        if (userVo == null) {
            return null;
        }

        DocHomeNoteAuthorVo authorVo = new DocHomeNoteAuthorVo();
        authorVo.setId(userVo.getId());
        authorVo.setName(userVo.getName());
        authorVo.setAvatar(userVo.getAvatar());
        authorVo.setFans(userVo.getFans());
        authorVo.setIsFollowing(userVo.getIsFollowing());
        return authorVo;
    }

    /**
     * 根据用户ID获取用户信息
     * <p>
     * 通过远程用户服务获取用户公开信息（昵称、头像）。
     * 调用失败时返回null，由调用方使用降级信息。
     * </p>
     *
     * @param userId 用户ID
     * @return 用户信息，获取失败返回null
     */
    private SysUserApi fetchUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            var result = remoteUserService.getUserById(userId);
            if (result != null && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.error("获取用户信息失败, userId={}", userId, e);
        }
        return null;
    }
}
