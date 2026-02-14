package com.qoobot.openrecommend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qoobot.openrecommend.common.enums.BehaviorType;
import com.qoobot.openrecommend.entity.UserBehavior;

import java.util.List;
import java.util.Map;

/**
 * 用户行为服务接口
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
public interface UserBehaviorService {

    /**
     * 记录用户行为
     * 
     * @param behavior 用户行为
     * @return 是否成功
     */
    boolean recordBehavior(UserBehavior behavior);

    /**
     * 批量记录用户行为
     * 
     * @param behaviors 行为列表
     * @return 是否成功
     */
    boolean batchRecord(List<UserBehavior> behaviors);

    /**
     * 获取用户行为历史
     * 
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 行为列表
     */
    IPage<UserBehavior> getHistory(Long userId, int pageNum, int pageSize);

    /**
     * 获取用户行为统计
     * 
     * @param userId 用户ID
     * @param days 最近天数
     * @return 统计数据
     */
    Map<String, Object> getStatistics(Long userId, int days);

    /**
     * 获取用户指定内容的行为
     * 
     * @param userId 用户ID
     * @param contentType 内容类型
     * @param contentId 内容ID
     * @return 行为列表
     */
    List<UserBehavior> getUserBehavior(Long userId, String contentType, Long contentId);

    /**
     * 检查用户是否执行过某种行为
     * 
     * @param userId 用户ID
     * @param contentType 内容类型
     * @param contentId 内容ID
     * @param behaviorType 行为类型
     * @return 是否执行过
     */
    boolean hasBehavior(Long userId, String contentType, Long contentId, BehaviorType behaviorType);

    /**
     * 获取用户最近浏览的内容ID
     * 
     * @param userId 用户ID
     * @param contentType 内容类型
     * @param limit 限制数量
     * @return 内容ID列表
     */
    List<Long> getRecentViewedContentIds(Long userId, String contentType, int limit);
}
