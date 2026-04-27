package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 视频合集视图对象
 * <p>
 * 用于返回视频合集的基础信息，适用于合集列表展示场景。
 * 包含封面文件信息（通过 DocFileInfoVo 封装），便于前端直接展示封面缩略图。
 * 不包含视频列表详情，如需获取合集中的视频列表，请使用 {@link DocVideoCollectionDtlVo}。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-28
 */
@Data
@Schema(description = "视频合集视图对象")
public class DocVideoCollectionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 合集ID
     * <p>合集的唯一标识，由雪花算法生成。</p>
     */
    @Schema(description = "合集ID")
    private Long id;

    /**
     * 合集名称
     * <p>用户自定义的合集标题。</p>
     */
    @Schema(description = "合集名称")
    private String collectionName;

    /**
     * 合集描述
     * <p>对合集内容的简要说明，可能为空。</p>
     */
    @Schema(description = "合集描述")
    private String description;

    /**
     * 封面图片文件信息
     * <p>
     * 封装封面文件的ID和访问URL，便于前端展示封面缩略图。
     * 若合集未设置封面，此字段可能为 null。
     * </p>
     */
    @Schema(description = "封面图片文件信息")
    private DocFileInfoVo cover;

    /**
     * 视频数量
     * <p>该合集中包含的视频总数，由业务层自动维护。</p>
     */
    @Schema(description = "视频数量")
    private Integer videoCount;

    /**
     * 合集排序值
     * <p>数值越大在列表中展示越靠前，默认值为0。</p>
     */
    @Schema(description = "合集排序（越大越靠前）")
    private Integer sortOrder;

    /**
     * 合集状态
     * <p>1-公开（其他用户可见），2-私密（仅自己可见）。</p>
     */
    @Schema(description = "状态（1-公开 2-私密）")
    private Integer status;

    /**
     * 创建时间
     * <p>合集的创建时间，格式 yyyy-MM-dd HH:mm:ss。</p>
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     * <p>合集的最后修改时间，格式 yyyy-MM-dd HH:mm:ss。</p>
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
