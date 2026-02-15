package com.zsk.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsk.common.core.domain.R;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.document.api.RemoteDocumentService;
import com.zsk.document.api.domain.DocStatisticsApi;
import com.zsk.system.domain.SysUser;
import com.zsk.system.domain.vo.SysUserProfileVo;
import com.zsk.system.domain.vo.SysUserWorkVo;
import com.zsk.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前台用户 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "前台用户")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SysUserFrontController {

    private final ISysUserService userService;
    private final RemoteDocumentService remoteDocumentService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取用户资料详情
     *
     * @param id 用户ID
     * @return 用户资料
     */
    @Operation(summary = "获取用户资料详情")
    @GetMapping("/profile/{id}")
    public R<SysUserProfileVo> getProfile(@PathVariable("id") Long id) {
        SysUser user = userService.selectUserById(id);
        if (user == null) {
            return R.fail("用户不存在");
        }

        SysUserProfileVo vo = buildUserProfileVo(user);

        /** 获取当前用户ID */
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null && !currentUserId.equals(id)) {
            /** 检查是否关注（需要交互服务支持） */
            vo.setIsFollowing(false);
        } else {
            vo.setIsFollowing(false);
        }

        return R.ok(vo);
    }

    /**
     * 更新用户资料
     *
     * @param data 用户资料
     * @return 更新后的用户资料
     */
    @Operation(summary = "更新用户资料")
    @PostMapping("/profile/update")
    public R<SysUserProfileVo> updateProfile(@RequestBody Map<String, Object> data) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        SysUser user = userService.selectUserById(userId);
        if (user == null) {
            return R.fail("用户不存在");
        }

        /** 更新字段 */
        if (data.containsKey("name")) {
            user.setNickName((String) data.get("name"));
        }
        if (data.containsKey("avatar")) {
            user.setAvatar((String) data.get("avatar"));
        }
        if (data.containsKey("bio")) {
            user.setRemark((String) data.get("bio"));
        }

        userService.updateUser(user);

        return R.ok(buildUserProfileVo(user));
    }

    /**
     * 切换用户关注状态
     *
     * @param id 用户ID
     * @return 关注状态
     */
    @Operation(summary = "切换用户关注状态")
    @PostMapping("/follow/{id}")
    public R<Map<String, Object>> toggleFollow(@PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        if (userId.equals(id)) {
            return R.fail("不能关注自己");
        }

        /** 关注状态（需要交互服务支持，暂时返回固定值） */
        Map<String, Object> result = new HashMap<>();
        result.put("isFollowing", true);
        return R.ok(result);
    }

    /**
     * 获取用户作品列表
     *
     * @param id 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @param type 作品类型
     * @return 作品列表
     */
    @Operation(summary = "获取用户作品列表")
    @GetMapping("/works/{id}")
    public R<Map<String, Object>> getWorks(
        @PathVariable("id") Long id,
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(value = "type", required = false) String type) {

        List<SysUserWorkVo> works = new ArrayList<>();

        /** 暂时返回空列表，后续需要调用document服务获取用户作品 */
        Map<String, Object> result = new HashMap<>();
        result.put("list", works);
        result.put("total", 0);
        return R.ok(result);
    }

    /**
     * 获取用户收藏列表
     *
     * @param id 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 收藏列表
     */
    @Operation(summary = "获取用户收藏列表")
    @GetMapping("/favorites/{id}")
    public R<Map<String, Object>> getFavorites(
        @PathVariable("id") Long id,
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {

        List<SysUserWorkVo> favorites = new ArrayList<>();

        /** 暂时返回空列表，后续需要调用document服务获取用户收藏 */
        Map<String, Object> result = new HashMap<>();
        result.put("list", favorites);
        result.put("total", 0);
        return R.ok(result);
    }

    /**
     * 构建用户资料VO
     */
    private SysUserProfileVo buildUserProfileVo(SysUser user) {
        SysUserProfileVo vo = new SysUserProfileVo();
        vo.setId(String.valueOf(user.getId()));
        vo.setUsername(user.getUserName());
        vo.setName(user.getNickName());
        vo.setAvatar(user.getAvatar());
        vo.setBanner("");
        vo.setLevel(1);
        vo.setTags(new ArrayList<>());
        vo.setBio(user.getRemark());
        vo.setLocation("");
        vo.setWebsite("");
        vo.setIsFollowing(false);

        /** 统计数据 */
        SysUserProfileVo.StatsInfo stats = new SysUserProfileVo.StatsInfo();
        stats.setFollowers(0);
        stats.setFollowing(0);
        stats.setWorks(0);
        stats.setLikes(0);
        vo.setStats(stats);

        return vo;
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
