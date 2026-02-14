package com.qoobot.openrecommend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 视频实体
 *
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("video")
public class Video extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 视频ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 视频描述
     */
    private String description;

    /**
     * 封面图URL
     */
    private String coverUrl;

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 视频时长（秒）
     */
    private Integer duration;

    /**
     * 视频宽度（像素）
     */
    private Integer width;

    /**
     * 视频高度（像素）
     */
    private Integer height;

    /**
     * 分辨率
     */
    private String resolution;

    /**
     * 视频格式
     */
    private String format;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 比特率（kbps）
     */
    private Integer bitrate;

    /**
     * 帧率
     */
    private BigDecimal fps;

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
     * 关键帧特征（JSON）
     */
    private String keyframeFeatures;

    /**
     * 视觉特征向量（JSON）
     */
    private String visualFeatures;

    /**
     * 文本特征向量（JSON）
     */
    private String textFeatures;

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
     * 播放量
     */
    private Long viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 分享数
     */
    private Integer shareCount;

    /**
     * 完播数
     */
    private Integer finishWatchCount;

    /**
     * 平均观看时长（秒）
     */
    private Integer avgWatchDuration;

    /**
     * 质量得分（0-100）
     */
    private Double qualityScore;
}
