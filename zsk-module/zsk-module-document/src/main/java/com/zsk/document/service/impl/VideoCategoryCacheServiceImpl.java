package com.zsk.document.service.impl;

import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.core.constant.DictTypeConstants;
import com.zsk.common.core.context.CacheContext;
import com.zsk.common.core.domain.R;
import com.zsk.common.redis.service.RedisService;
import com.zsk.document.domain.vo.VideoCategoryVO;
import com.zsk.document.domain.vo.VideoTagVO;
import com.zsk.document.service.IVideoCategoryCacheService;
import com.zsk.system.api.RemoteDictService;
import com.zsk.system.api.domain.SysDictDataApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 视频分类标签缓存Service业务层处理
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoCategoryCacheServiceImpl implements IVideoCategoryCacheService {

    private final RedisService redisService;
    private final RemoteDictService remoteDictService;

    /**
     * 从缓存获取视频分类列表
     *
     * @return 分类列表
     */
    @Override
    public List<VideoCategoryVO> getCategoryListFromCache() {
        log.info("从缓存获取视频分类列表");
        List<VideoCategoryVO> list = redisService.getCacheObject(CacheConstants.CACHE_VIDEO_CATEGORY);
        if (list == null) {
            log.info("视频分类缓存未命中, 从字典服务加载");
            list = buildCategoriesFromDict();
            if (!list.isEmpty()) {
                redisService.setCacheObject(CacheConstants.CACHE_VIDEO_CATEGORY, list,
                        CacheContext.getVideoCategoryCacheExpireHours(), TimeUnit.HOURS);
                log.info("视频分类缓存已更新, 共{}个分类", list.size());
            }
        } else {
            log.info("从缓存获取视频分类列表完成, 共{}个分类", list.size());
        }
        return list;
    }

    /**
     * 从缓存获取视频标签列表
     *
     * @return 标签列表
     */
    @Override
    public List<VideoTagVO> getTagListFromCache() {
        log.info("从缓存获取视频标签列表");
        List<VideoTagVO> list = redisService.getCacheObject(CacheConstants.CACHE_VIDEO_TAG);
        if (list == null) {
            log.info("视频标签缓存未命中, 从字典服务加载");
            list = buildTagsFromDict();
            if (!list.isEmpty()) {
                redisService.setCacheObject(CacheConstants.CACHE_VIDEO_TAG, list,
                        CacheContext.getVideoTagCacheExpireHours(), TimeUnit.HOURS);
                log.info("视频标签缓存已更新, 共{}个标签", list.size());
            }
        } else {
            log.info("从缓存获取视频标签列表完成, 共{}个标签", list.size());
        }
        return list;
    }

    /**
     * 刷新分类缓存
     */
    @Override
    public void refreshCategoryCache() {
        log.info("刷新视频分类缓存");
        redisService.deleteObject(CacheConstants.CACHE_VIDEO_CATEGORY);
        List<VideoCategoryVO> list = buildCategoriesFromDict();
        if (!list.isEmpty()) {
            redisService.setCacheObject(CacheConstants.CACHE_VIDEO_CATEGORY, list,
                    CacheContext.getVideoCategoryCacheExpireHours(), TimeUnit.HOURS);
            log.info("刷新视频分类缓存完成, 共{}个分类", list.size());
        } else {
            log.warn("刷新视频分类缓存失败, 未获取到分类数据");
        }
    }

    /**
     * 刷新标签缓存
     */
    @Override
    public void refreshTagCache() {
        log.info("刷新视频标签缓存");
        redisService.deleteObject(CacheConstants.CACHE_VIDEO_TAG);
        List<VideoTagVO> list = buildTagsFromDict();
        if (!list.isEmpty()) {
            redisService.setCacheObject(CacheConstants.CACHE_VIDEO_TAG, list,
                    CacheContext.getVideoTagCacheExpireHours(), TimeUnit.HOURS);
            log.info("刷新视频标签缓存完成, 共{}个标签", list.size());
        } else {
            log.warn("刷新视频标签缓存失败, 未获取到标签数据");
        }
    }

    /**
     * 从字典表构建分类列表
     *
     * @return 分类列表
     */
    private List<VideoCategoryVO> buildCategoriesFromDict() {
        try {
            R<List<SysDictDataApi>> result = remoteDictService.getDictDataByType(DictTypeConstants.VIDEO_CATEGORY);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return buildCategoryTree(result.getData());
            }
        } catch (Exception e) {
            log.error("从字典服务获取视频分类失败: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * 构建分类树形结构
     *
     * @param dictDataList 字典数据列表
     * @return 分类树
     */
    private List<VideoCategoryVO> buildCategoryTree(List<SysDictDataApi> dictDataList) {
        // 按父级值分组
        Map<String, List<SysDictDataApi>> parentMap = dictDataList.stream()
                .collect(Collectors.groupingBy(d -> d.getParentValue() == null ? "" : d.getParentValue()));

        // 获取顶级分类（parentValue为空或null）
        List<SysDictDataApi> topCategories = parentMap.getOrDefault("", new ArrayList<>());

        return topCategories.stream()
                .map(dict -> {
                    VideoCategoryVO vo = new VideoCategoryVO();
                    vo.setId(dict.getDictValue());
                    vo.setName(dict.getDictLabel());
                    // 获取子分类
                    List<SysDictDataApi> children = parentMap.getOrDefault(dict.getDictValue(), new ArrayList<>());
                    vo.setChildren(children.stream()
                            .map(child -> {
                                VideoCategoryVO childVo = new VideoCategoryVO();
                                childVo.setId(child.getDictValue());
                                childVo.setName(child.getDictLabel());
                                childVo.setChildren(new ArrayList<>());
                                return childVo;
                            })
                            .collect(Collectors.toList()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 从字典表构建标签列表
     *
     * @return 标签列表
     */
    private List<VideoTagVO> buildTagsFromDict() {
        try {
            R<List<SysDictDataApi>> result = remoteDictService.getDictDataByType(DictTypeConstants.VIDEO_TAG);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return result.getData().stream()
                        .map(dict -> {
                            VideoTagVO vo = new VideoTagVO();
                            vo.setId(dict.getDictValue());
                            vo.setName(dict.getDictLabel());
                            return vo;
                        })
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("从字典服务获取视频标签失败: {}", e.getMessage());
        }
        return Collections.emptyList();
    }
}
