package com.qoobot.openrecommend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 图片实体
 *
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("image")
public class Image extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 图片ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 图片标题
     */
    private String title;

    /**
     * 图片描述
     */
    private String description;

    /**
     * 图片URL
     */
    private String url;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 图片宽度（像素）
     */
    private Integer width;

    /**
     * 图片高度（像素）
     */
    private Integer height;

    /**
     * 宽高比
     */
    private BigDecimal aspectRatio;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 图片格式
     */
    private String format;

    /**
     * 颜色深度
     */
    private Integer colorDepth;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 标签（JSON）
     */
    private String tags;

    /**
     * 关键词（JSON）
     */
    private String keywords;

    /**
     * 上传者ID
     */
    private Long uploaderId;

    /**
     * 来源
     */
    private String source;

    /**
     * 颜色直方图特征（JSON）
     */
    private String colorHistogram;

    /**
     * 主色调（JSON）
     */
    private String dominantColors;

    /**
     * 视觉特征向量（JSON）
     */
    private String visualFeatures;

    /**
     * 状态：0-待审核，1-已发布，2-下架，3-审核拒绝
     */
    private Integer status;

    /**
     * 审核拒绝原因
     */
    private String rejectReason;

    /**
     * 发布时间
     */
    private String publishTime;

    /**
     * 浏览量
     */
    private Long viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 质量得分（0-100）
     */
    private Double qualityScore;
}
