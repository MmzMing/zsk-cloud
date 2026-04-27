package com.zsk.document.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 视频合集表对象 document_video_collection
 * <p>
 * 对应数据库表 document_video_collection，用于存储用户创建的视频合集信息。
 * 合集是用户自定义的视频分组，一个合集可包含多个视频，一个视频也可属于多个合集。
 * 通过 video_count 冗余字段优化列表查询性能，避免频繁关联统计。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("document_video_collection")
@Schema(description = "视频合集对象")
public class DocVideoCollection extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属用户ID
     * <p>
     * 标识该合集的创建者，用于权限隔离和列表查询。
     * 每个用户只能查看和操作自己创建的合集。
     * </p>
     */
    @Schema(description = "所属用户ID")
    private Long userId;

    /**
     * 合集名称
     * <p>
     * 用户自定义的合集标题，用于展示和搜索。
     * 必填字段，最大长度100字符。
     * </p>
     */
    @Schema(description = "合集名称")
    private String collectionName;

    /**
     * 合集描述
     * <p>
     * 对合集内容的简要说明，可选字段。
     * 最大长度500字符，可为空。
     * </p>
     */
    @Schema(description = "合集描述")
    private String description;

    /**
     * 封面图片文件ID
     * <p>
     * 关联 document_files.id，用于展示合集封面缩略图。
     * 可选字段，为空时前端可显示默认封面。
     * </p>
     */
    @Schema(description = "封面图片文件ID")
    private Long coverFileId;

    /**
     * 视频数量（冗余字段）
     * <p>
     * 缓存该合集中的视频总数，用于列表页快速展示，避免频繁关联查询。
     * 增删视频时由业务层自动维护同步。
     * </p>
     */
    @Schema(description = "视频数量")
    private Integer videoCount;

    /**
     * 合集排序值
     * <p>
     * 控制合集在列表中的展示顺序，数值越大越靠前。
     * 默认值为0，用户可通过调整排序值改变合集展示优先级。
     * </p>
     */
    @Schema(description = "合集排序（越大越靠前）")
    private Integer sortOrder;

    /**
     * 合集状态
     * <p>
     * 1-公开：其他用户可见；2-私密：仅自己可见。
     * 默认状态为公开（1）。
     * </p>
     */
    @Schema(description = "状态（1-公开 2-私密）")
    private Integer status;

}
