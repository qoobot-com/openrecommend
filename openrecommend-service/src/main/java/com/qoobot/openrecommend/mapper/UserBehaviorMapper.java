package com.qoobot.openrecommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openrecommend.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户行为数据访问层
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
@Mapper
public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {

    /**
     * 查询用户近期行为
     * 
     * @param userId 用户ID
     * @param days 最近天数
     * @param limit 限制数量
     * @return 行为列表
     */
    @Select("SELECT * FROM user_behavior " +
            "WHERE user_id = #{userId} " +
            "AND create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<UserBehavior> selectRecentBehaviors(@Param("userId") Long userId, 
                                              @Param("days") int days, 
                                              @Param("limit") int limit);

    /**
     * 查询用户历史内容ID
     * 
     * @param userId 用户ID
     * @param contentType 内容类型
     * @param limit 限制数量
     * @return 内容ID列表
     */
    @Select("SELECT DISTINCT content_id FROM user_behavior " +
            "WHERE user_id = #{userId} " +
            "<if test='contentType != null'>" +
            "AND content_type = #{contentType} " +
            "</if>" +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<Long> selectHistoryContentIds(@Param("userId") Long userId, 
                                        @Param("contentType") String contentType, 
                                        @Param("limit") int limit);

    /**
     * 查询用户在指定内容上的行为
     * 
     * @param userId 用户ID
     * @param contentType 内容类型
     * @param contentId 内容ID
     * @return 行为列表
     */
    @Select("SELECT * FROM user_behavior " +
            "WHERE user_id = #{userId} " +
            "AND content_type = #{contentType} " +
            "AND content_id = #{contentId} " +
            "ORDER BY create_time DESC")
    List<UserBehavior> selectByUserAndContent(@Param("userId") Long userId,
                                                @Param("contentType") String contentType,
                                                @Param("contentId") Long contentId);

    /**
     * 统计用户行为次数
     * 
     * @param userId 用户ID
     * @param behaviorType 行为类型
     * @param days 最近天数
     * @return 行为次数
     */
    @Select("SELECT COUNT(*) FROM user_behavior " +
            "WHERE user_id = #{userId} " +
            "AND behavior_type = #{behaviorType} " +
            "AND create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    int countByBehaviorType(@Param("userId") Long userId, 
                              @Param("behaviorType") Integer behaviorType,
                              @Param("days") int days);

    /**
     * 查询活跃用户ID列表
     *
     * @param days 最近天数
     * @return 用户ID列表
     */
    @Select("SELECT DISTINCT user_id FROM user_behavior " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "ORDER BY MAX(create_time) DESC")
    List<Long> selectActiveUserIds(@Param("days") int days);
}
