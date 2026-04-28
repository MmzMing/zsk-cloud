package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.document.domain.dto.DocHomeNoteCommentPostDto;
import com.zsk.document.domain.vo.DocHomeNoteCommentVo;
import com.zsk.document.domain.vo.DocHomeNoteDetailVo;
import com.zsk.document.domain.vo.DocHomeNoteInteractionResultVo;
import com.zsk.document.domain.vo.DocHomeNoteInteractionVo;
import com.zsk.document.service.IDocHomeNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 前台首页笔记详情控制器
 * <p>
 * 提供前台笔记详情页的三大区域化接口和交互操作接口：
 * </p>
 * <p>
 * 区域一：笔记元信息+详情 —— 获取笔记基本信息和内容，优先增加浏览量
 * </p>
 * <p>
 * 区域二：点赞收藏+作者关注信息 —— 获取交互统计数据和作者关注状态
 * </p>
 * <p>
 * 区域三：评论区域 —— 分页获取二级结构评论列表，发表/回复评论使用同一接口
 * </p>
 * <p>
 * 本控制器是前台聚合接口，所有业务逻辑通过 {@link IDocHomeNoteService} 聚合服务处理，
 * 不在其他Service中编写方法供本控制器调用，确保前后台逻辑隔离。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-29
 */
@Slf4j
@Tag(name = "前台首页笔记详情")
@RestController
@RequestMapping("/docHomeNote")
@RequiredArgsConstructor
public class DocHomeNoteController {

    /**
     * 前台首页笔记聚合服务
     */
    private final IDocHomeNoteService docHomeNoteService;

    // ==================== 区域一：笔记元信息+详情 ====================

    /**
     * 获取笔记元信息+详情
     * <p>
     * 查询笔记的基本信息和内容详情，仅返回前台展示所需字段。
     * 获取元信息时优先增加浏览量（Redis）。
     * 不包含审核状态、版本号等后台管理字段。
     * </p>
     *
     * @param id 笔记ID
     * @return 笔记详情
     */
    @Operation(summary = "获取笔记元信息+详情")
    @GetMapping("/detail/{id}")
    public R<DocHomeNoteDetailVo> getDetail(@PathVariable("id") Long id) {
        log.info("获取笔记元信息+详情请求, id={}", id);

        Long userId = getCurrentUserId();
        DocHomeNoteDetailVo vo = docHomeNoteService.getNoteDetail(id, userId);
        if (vo == null) {
            return R.fail("笔记不存在");
        }

        log.info("获取笔记元信息+详情成功, id={}", id);
        return R.ok(vo);
    }

    // ==================== 区域二：点赞收藏+作者关注信息 ====================

    /**
     * 获取笔记交互信息（点赞收藏+作者关注）
     * <p>
     * 独立查询笔记的交互统计数据和作者关注信息。
     * 浏览量、点赞数、收藏数从Redis缓存获取。
     * 当前用户的点赞、收藏、关注状态从Redis Bitmap获取。
     * </p>
     *
     * @param id 笔记ID
     * @return 笔记交互信息
     */
    @Operation(summary = "获取笔记交互信息（点赞收藏+作者关注）")
    @GetMapping("/interaction/{id}")
    public R<DocHomeNoteInteractionVo> getInteraction(@PathVariable("id") Long id) {
        log.info("获取笔记交互信息请求, id={}", id);

        Long userId = getCurrentUserId();
        DocHomeNoteInteractionVo vo = docHomeNoteService.getNoteInteraction(id, userId);
        if (vo == null) {
            return R.fail("笔记不存在");
        }

        log.info("获取笔记交互信息成功, id={}", id);
        return R.ok(vo);
    }

    /**
     * 切换笔记点赞状态
     * <p>
     * 用户点赞或取消点赞笔记，通过Redis缓存服务操作。
     * </p>
     *
     * @param id 笔记ID
     * @return 点赞操作结果
     */
    @Operation(summary = "切换笔记点赞状态")
    @PostMapping("/like/{id}")
    public R<DocHomeNoteInteractionResultVo> toggleLike(@PathVariable("id") Long id) {
        log.info("切换笔记点赞状态请求, id={}", id);

        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        DocHomeNoteInteractionResultVo result = docHomeNoteService.toggleNoteLike(id, userId);

        log.info("切换笔记点赞状态成功, id={}, status={}", id, result.isStatus());
        return R.ok(result);
    }

    /**
     * 切换笔记收藏状态
     * <p>
     * 用户收藏或取消收藏笔记，通过Redis缓存服务操作。
     * </p>
     *
     * @param id 笔记ID
     * @return 收藏操作结果
     */
    @Operation(summary = "切换笔记收藏状态")
    @PostMapping("/favorite/{id}")
    public R<DocHomeNoteInteractionResultVo> toggleFavorite(@PathVariable("id") Long id) {
        log.info("切换笔记收藏状态请求, id={}", id);

        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        DocHomeNoteInteractionResultVo result = docHomeNoteService.toggleNoteFavorite(id, userId);

        log.info("切换笔记收藏状态成功, id={}, status={}", id, result.isStatus());
        return R.ok(result);
    }

    /**
     * 切换关注作者状态
     * <p>
     * 用户关注或取消关注笔记作者，通过Redis缓存服务操作。
     * </p>
     *
     * @param authorId 作者ID
     * @return 关注操作结果
     */
    @Operation(summary = "切换关注作者状态")
    @PostMapping("/follow/{authorId}")
    public R<DocHomeNoteInteractionResultVo> toggleFollow(@PathVariable("authorId") Long authorId) {
        log.info("切换关注作者状态请求, authorId={}", authorId);

        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        if (authorId.equals(userId)) {
            return R.fail("不能关注自己");
        }

        DocHomeNoteInteractionResultVo result = docHomeNoteService.toggleFollowAuthor(authorId, userId);

        log.info("切换关注作者状态成功, authorId={}, status={}", authorId, result.isStatus());
        return R.ok(result);
    }

    // ==================== 区域三：评论区域 ====================

    /**
     * 获取笔记评论列表（分页，二级结构）
     * <p>
     * 查询笔记的根评论列表，每个根评论包含其下的所有回复。
     * 采用B站式二级评论结构：所有回复统一挂在根评论下，不存在层级嵌套。
     * 支持热门/最新排序。
     * </p>
     *
     * @param noteId    笔记ID
     * @param pageQuery 分页查询参数
     * @param sort      排序方式（hot-按点赞数降序，new-按创建时间降序）
     * @return 评论分页列表
     */
    @Operation(summary = "获取笔记评论列表（分页，二级结构）")
    @GetMapping("/comments/{noteId}")
    public R<PageResult<DocHomeNoteCommentVo>> getComments(
            @PathVariable("noteId") Long noteId,
            PageQuery pageQuery,
            @RequestParam(value = "sort", required = false) String sort) {
        log.info("获取笔记评论列表请求, noteId={}, pageQuery={}, sort={}", noteId, pageQuery, sort);

        Long userId = getCurrentUserId();
        PageResult<DocHomeNoteCommentVo> pageResult = docHomeNoteService.getNoteComments(noteId, pageQuery, sort, userId);

        log.info("获取笔记评论列表成功, noteId={}, total={}", noteId, pageResult.getTotal());
        return R.ok(pageResult);
    }

    /**
     * 发表/回复笔记评论
     * <p>
     * 发表根评论和回复评论使用同一个接口。
     * 采用B站式评论结构：parentId记录根评论ID，replyToId记录被回复用户ID。
     * </p>
     *
     * @param dto 评论请求参数
     * @return 评论结果
     */
    @Operation(summary = "发表/回复笔记评论")
    @PostMapping("/comment")
    public R<DocHomeNoteCommentVo> postComment(@RequestBody DocHomeNoteCommentPostDto dto) {
        log.info("发表/回复笔记评论请求, noteId={}", dto.getNoteId());

        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        DocHomeNoteCommentVo commentVo = docHomeNoteService.postComment(dto, userId);

        log.info("发表/回复笔记评论成功, commentId={}", commentVo.getId());
        return R.ok(commentVo);
    }

    /**
     * 切换评论点赞状态
     * <p>
     * 用户点赞或取消点赞笔记评论，通过Redis缓存服务操作。
     * </p>
     *
     * @param commentId 评论ID
     * @return 点赞操作结果
     */
    @Operation(summary = "切换评论点赞状态")
    @PostMapping("/comment/like/{commentId}")
    public R<DocHomeNoteInteractionResultVo> toggleCommentLike(@PathVariable("commentId") Long commentId) {
        log.info("切换评论点赞状态请求, commentId={}", commentId);

        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        DocHomeNoteInteractionResultVo result = docHomeNoteService.toggleCommentLike(commentId, userId);

        log.info("切换评论点赞状态成功, commentId={}, status={}", commentId, result.isStatus());
        return R.ok(result);
    }

    // ==================== 私有方法 ====================

    /**
     * 获取当前用户ID
     * <p>
     * 尝试从安全工具类获取当前登录用户ID，如果获取失败则返回null。
     * 前台接口允许未登录访问（如浏览笔记详情），因此获取失败不抛异常。
     * </p>
     *
     * @return 当前用户ID，未登录或获取失败返回null
     */
    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            log.warn("获取当前用户ID失败", e);
            return null;
        }
    }
}
