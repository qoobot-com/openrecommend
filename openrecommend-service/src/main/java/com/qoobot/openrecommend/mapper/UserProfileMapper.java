package com.qoobot.openrecommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openrecommend.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户画像数据访问层
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {

    /**
     * 根据用户ID查询画像
     * 
     * @param userId 用户ID
     * @return 用户画像
     */
    @Select("SELECT * FROM user_profile WHERE user_id = #{userId}")
    UserProfile selectByUserId(@Param("userId") Long userId);

    /**
     * 查询需要更新的画像列表
     * 
     * @param hours 小时数
     * @param limit 限制数量
     * @return 画像列表
     */
    @Select("SELECT * FROM user_profile " +
            "WHERE last_update_time <= DATE_SUB(NOW(), INTERVAL #{hours} HOUR) " +
            "ORDER BY last_update_time ASC " +
            "LIMIT #{limit}")
    List<UserProfile> selectNeedUpdate(@Param("hours") int hours, @Param("limit") int limit);

    /**
     * 查询需要更新的画像列表（简化版，仅使用limit参数）
     *
     * @param limit 限制数量
     * @return 画像列表
     */
    @Select("SELECT * FROM user_profile " +
            "WHERE last_update_time <= DATE_SUB(NOW(), INTERVAL 6 HOUR) " +
            "ORDER BY last_update_time ASC " +
            "LIMIT #{limit}")
    List<UserProfile> selectNeedUpdate(@Param("limit") int limit);

    /**
     * 查询不完整的画像列表
     *
     * @param limit 限制数量
     * @return 画像列表
     */
    @Select("SELECT * FROM user_profile " +
            "WHERE (interest_tags IS NULL OR JSON_LENGTH(interest_tags) = 0) " +
            "   OR (content_preference IS NULL OR JSON_LENGTH(content_preference) = 0) " +
            "LIMIT #{limit}")
    List<UserProfile> selectIncompleteProfiles(@Param("limit") int limit);

    /**
     * 查询总用户数
     *
     * @return 用户总数
     */
    @Select("SELECT COUNT(*) FROM user")
    int selectTotalUserCount();

    /**
     * 查询有画像的用户数
     *
     * @return 画像数量
     */
    @Select("SELECT COUNT(*) FROM user_profile")
    int selectProfilesCount();

    /**
     * 查询需要更新的画像数量
     *
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM user_profile " +
            "WHERE last_update_time <= DATE_SUB(NOW(), INTERVAL 6 HOUR)")
    int selectNeedUpdateCount();

    /**
     * 查询不完整的画像数量
     *
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM user_profile " +
            "WHERE (interest_tags IS NULL OR JSON_LENGTH(interest_tags) = 0) " +
            "   OR (content_preference IS NULL OR JSON_LENGTH(content_preference) = 0)")
    int selectIncompleteProfilesCount();
}
