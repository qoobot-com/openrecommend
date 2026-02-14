package com.qoobot.openrecommend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户行为实体
 *
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_behavior")
public class UserBehavior extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 行为ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 内容类型：article,image,video
     */
    private String contentType;

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 行为类型：1-浏览，2-点赞，3-收藏，4-分享，5-评论
     */
    private Integer behaviorType;

    /**
     * 停留时长（秒），仅浏览行为记录
     */
    private Integer duration;

    /**
     * 滚动深度（百分比），0-100
     */
    private BigDecimal scrollDepth;

    /**
     * 是否完播/完读：0-否，1-是
     */
    private Integer isFinish;

    /**
     * 设备类型：mobile,pc,tablet
     */
    private String device;

    /**
     * 操作系统：iOS,Android,Windows,Mac,Linux
     */
    private String os;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 地理位置
     */
    private String location;

    /**
     * 额外信息（JSON）
     */
    private String extraInfo;

    /**
     * 行为时间
     */
    private LocalDateTime createTime;
}
