package com.zsk.document.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 视频合集关联表对象 document_video_collection_item
 * <p>
 * 对应数据库表 document_video_collection_item，用于维护合集与视频的多对多关联关系。
 * 通过 sort_order 字段控制视频在合集中的播放顺序，实现用户自定义排序能力。
 * 数据库层面通过唯一索引 uk_dvci_collection_video 防止同一视频被重复添加到同一合集。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("document_video_collection_item")
@Schema(description = "视频合集关联对象")
public class DocVideoCollectionItem extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 合集ID
     * <p>
     * 关联 document_video_collection.id，标识该记录属于哪个合集。
     * </p>
     */
    @Schema(description = "合集ID")
    private Long collectionId;

    /**
     * 视频ID
     * <p>
     * 关联 document_video.id，标识合集中包含的视频。
     * </p>
     */
    @Schema(description = "视频ID")
    private Long videoId;

    /**
     * 视频在合集中的排序
     * <p>
     * 控制视频在合集中的展示/播放顺序，数值越小越靠前。
     * 默认按添加顺序递增，用户可通过排序接口调整。
     * </p>
     */
    @Schema(description = "视频在合集中的排序（越小越靠前）")
    private Integer sortOrder;

}
