package com.qoobot.openrecommend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章实体
 *
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article")
public class Article extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 封面图URL
     */
    private String coverImage;

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
     * 作者ID
     */
    private Long authorId;

    /**
     * 来源
     */
    private String source;

    /**
     * 来源URL
     */
    private String sourceUrl;

    /**
     * 是否原创
     */
    private Integer isOriginal;

    /**
     * 字数
     */
    private Integer wordCount;

    /**
     * 预估阅读时长（分钟）
     */
    private Integer readDuration;

    /**
     * 状态：0-草稿，1-待审核，2-已发布，3-下架，4-审核拒绝
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
     * 完读数
     */
    private Integer finishReadCount;

    /**
     * 平均阅读时长（秒）
     */
    private Integer avgReadDuration;

    /**
     * 质量得分（0-100）
     */
    private Double qualityScore;

    /**
     * 内容向量（JSON）
     */
    private String contentVector;
}
