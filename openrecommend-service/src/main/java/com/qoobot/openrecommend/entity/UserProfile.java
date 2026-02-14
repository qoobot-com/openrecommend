package com.qoobot.openrecommend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户画像实体
 *
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_profile")
public class UserProfile extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 画像ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 年龄段：18-24,25-34,35-44,45-54,55+
     */
    private String ageRange;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 兴趣标签及权重（JSON）
     */
    private String interestTags;

    /**
     * 内容类型偏好（JSON）
     */
    private String contentPreference;

    /**
     * 分类偏好（JSON）
     */
    private String categoryPreference;

    /**
     * 活跃时段（JSON）
     */
    private String activePeriods;

    /**
     * 设备偏好：mobile,pc,tablet
     */
    private String devicePreference;

    /**
     * 阅读偏好（JSON）
     */
    private String readPreference;

    /**
     * 总阅读时长（分钟）
     */
    private Integer totalReadTime;

    /**
     * 总浏览量
     */
    private Integer totalViewCount;

    /**
     * 总点赞数
     */
    private Integer totalLikeCount;

    /**
     * 总收藏数
     */
    private Integer totalCollectCount;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;
}
