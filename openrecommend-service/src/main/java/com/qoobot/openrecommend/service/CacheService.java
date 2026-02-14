package com.qoobot.openrecommend.service;

import com.qoobot.openrecommend.api.dto.RecommendResponse;
import com.qoobot.openrecommend.entity.UserProfile;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
public interface CacheService {

    /**
     * 获取用户画像缓存
     * 
     * @param userId 用户ID
     * @return 用户画像
     */
    UserProfile getUserProfile(Long userId);

    /**
     * 设置用户画像缓存
     * 
     * @param userId 用户ID
     * @param profile 用户画像
     * @param ttl 过期时间（秒）
     */
    void setUserProfile(Long userId, UserProfile profile, long ttl);

    /**
     * 删除用户画像缓存
     * 
     * @param userId 用户ID
     */
    void deleteUserProfile(Long userId);

    /**
     * 获取推荐结果缓存
     * 
     * @param userId 用户ID
     * @param contentType 内容类型
     * @param recommendType 推荐类型
     * @return 推荐结果
     */
    RecommendResponse getRecommend(Long userId, String contentType, String recommendType);

    /**
     * 设置推荐结果缓存
     * 
     * @param userId 用户ID
     * @param contentType 内容类型
     * @param recommendType 推荐类型
     * @param response 推荐结果
     * @param ttl 过期时间
     * @param timeUnit 时间单位
     */
    void setRecommend(Long userId, String contentType, String recommendType, 
                      RecommendResponse response, long ttl, TimeUnit timeUnit);

    /**
     * 删除推荐结果缓存
     * 
     * @param userId 用户ID
     */
    void deleteRecommend(Long userId);

    /**
     * 获取热门内容缓存
     * 
     * @param contentType 内容类型
     * @param period 周期（hour, day, week）
     * @return 内容ID列表
     */
    String getHotContent(String contentType, String period);

    /**
     * 设置热门内容缓存
     * 
     * @param contentType 内容类型
     * @param period 周期
     * @param contentIds 内容ID列表（JSON格式）
     * @param ttl 过期时间（秒）
     */
    void setHotContent(String contentType, String period, String contentIds, long ttl);

    /**
     * 获取内容特征缓存
     * 
     * @param contentType 内容类型
     * @param contentId 内容ID
     * @return 特征JSON
     */
    String getContentFeature(String contentType, Long contentId);

    /**
     * 设置内容特征缓存
     * 
     * @param contentType 内容类型
     * @param contentId 内容ID
     * @param feature 特征JSON
     * @param ttl 过期时间（秒）
     */
    void setContentFeature(String contentType, Long contentId, String feature, long ttl);

    /**
     * 清除所有缓存
     */
    void clearAll();
}
