package com.qoobot.openrecommend.service.impl;

import com.qoobot.openrecommend.api.dto.RecommendResponse;
import com.qoobot.openrecommend.entity.UserProfile;
import com.qoobot.openrecommend.service.CacheService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务实现
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
@Service
public class CacheServiceImpl implements CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheServiceImpl.class);

    // 缓存Key前缀
    private static final String USER_PROFILE_PREFIX = "user_profile:";
    private static final String RECOMMEND_PREFIX = "recommend:";
    private static final String HOT_CONTENT_PREFIX = "hot_content:";
    private static final String CONTENT_FEATURE_PREFIX = "content_feature:";

    // 默认TTL（秒）
    private static final long USER_PROFILE_TTL = 3600;        // 1小时
    private static final long RECOMMEND_RESULT_TTL = 1800;     // 30分钟
    private static final long HOT_CONTENT_TTL = 3600;        // 1小时
    private static final long CONTENT_FEATURE_TTL = 86400;   // 24小时

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public UserProfile getUserProfile(Long userId) {
        try {
            String key = USER_PROFILE_PREFIX + userId;
            UserProfile profile = (UserProfile) redisTemplate.opsForValue().get(key);
            log.debug("获取用户画像缓存: userId={}, hit={}", userId, profile != null);
            return profile;
        } catch (Exception e) {
            log.error("获取用户画像缓存失败: userId={}", userId, e);
            return null;
        }
    }

    @Override
    public void setUserProfile(Long userId, UserProfile profile, long ttl) {
        try {
            String key = USER_PROFILE_PREFIX + userId;
            redisTemplate.opsForValue().set(key, profile, ttl, TimeUnit.SECONDS);
            log.debug("设置用户画像缓存: userId={}, ttl={}s", userId, ttl);
        } catch (Exception e) {
            log.error("设置用户画像缓存失败: userId={}", userId, e);
        }
    }

    @Override
    public void deleteUserProfile(Long userId) {
        try {
            String key = USER_PROFILE_PREFIX + userId;
            redisTemplate.delete(key);
            log.debug("删除用户画像缓存: userId={}", userId);
        } catch (Exception e) {
            log.error("删除用户画像缓存失败: userId={}", userId, e);
        }
    }

    @Override
    public RecommendResponse getRecommend(Long userId, String contentType, String recommendType) {
        try {
            String key = buildRecommendKey(userId, contentType, recommendType);
            RecommendResponse response = (RecommendResponse) redisTemplate.opsForValue().get(key);
            log.debug("获取推荐结果缓存: key={}, hit={}", key, response != null);
            return response;
        } catch (Exception e) {
            log.error("获取推荐结果缓存失败: userId={}, contentType={}, recommendType={}", 
                userId, contentType, recommendType, e);
            return null;
        }
    }

    @Override
    public void setRecommend(Long userId, String contentType, String recommendType, 
                              RecommendResponse response, long ttl, TimeUnit timeUnit) {
        try {
            String key = buildRecommendKey(userId, contentType, recommendType);
            redisTemplate.opsForValue().set(key, response, ttl, timeUnit);
            log.debug("设置推荐结果缓存: key={}, ttl={}{}", key, ttl, timeUnit);
        } catch (Exception e) {
            log.error("设置推荐结果缓存失败: userId={}, contentType={}, recommendType={}", 
                userId, contentType, recommendType, e);
        }
    }

    @Override
    public void deleteRecommend(Long userId) {
        try {
            // 删除用户的所有推荐缓存
            String pattern = RECOMMEND_PREFIX + userId + ":*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            log.debug("删除用户推荐缓存: userId={}", userId);
        } catch (Exception e) {
            log.error("删除用户推荐缓存失败: userId={}", userId, e);
        }
    }

    @Override
    public String getHotContent(String contentType, String period) {
        try {
            String key = buildHotContentKey(contentType, period);
            String contentIds = (String) redisTemplate.opsForValue().get(key);
            log.debug("获取热门内容缓存: key={}, hit={}", key, contentIds != null);
            return contentIds;
        } catch (Exception e) {
            log.error("获取热门内容缓存失败: contentType={}, period={}", contentType, period, e);
            return null;
        }
    }

    @Override
    public void setHotContent(String contentType, String period, String contentIds, long ttl) {
        try {
            String key = buildHotContentKey(contentType, period);
            redisTemplate.opsForValue().set(key, contentIds, ttl, TimeUnit.SECONDS);
            log.debug("设置热门内容缓存: key={}, ttl={}s", key, ttl);
        } catch (Exception e) {
            log.error("设置热门内容缓存失败: contentType={}, period={}", contentType, period, e);
        }
    }

    @Override
    public String getContentFeature(String contentType, Long contentId) {
        try {
            String key = buildContentFeatureKey(contentType, contentId);
            String feature = (String) redisTemplate.opsForValue().get(key);
            log.debug("获取内容特征缓存: key={}, hit={}", key, feature != null);
            return feature;
        } catch (Exception e) {
            log.error("获取内容特征缓存失败: contentType={}, contentId={}", contentType, contentId, e);
            return null;
        }
    }

    @Override
    public void setContentFeature(String contentType, Long contentId, String feature, long ttl) {
        try {
            String key = buildContentFeatureKey(contentType, contentId);
            redisTemplate.opsForValue().set(key, feature, ttl, TimeUnit.SECONDS);
            log.debug("设置内容特征缓存: key={}, ttl={}s", key, ttl);
        } catch (Exception e) {
            log.error("设置内容特征缓存失败: contentType={}, contentId={}", contentType, contentId, e);
        }
    }

    @Override
    public void clearAll() {
        try {
            // 清除所有相关缓存
            redisTemplate.delete(redisTemplate.keys(USER_PROFILE_PREFIX + "*"));
            redisTemplate.delete(redisTemplate.keys(RECOMMEND_PREFIX + "*"));
            redisTemplate.delete(redisTemplate.keys(HOT_CONTENT_PREFIX + "*"));
            log.warn("已清除所有缓存");
        } catch (Exception e) {
            log.error("清除缓存失败", e);
        }
    }

    /**
     * 构建推荐缓存Key
     */
    private String buildRecommendKey(Long userId, String contentType, String recommendType) {
        return String.format("%s%d:%s:%s", RECOMMEND_PREFIX, userId, contentType, recommendType);
    }

    /**
     * 构建热门内容缓存Key
     */
    private String buildHotContentKey(String contentType, String period) {
        return String.format("%s%s:%s", HOT_CONTENT_PREFIX, contentType, period);
    }

    /**
     * 构建内容特征缓存Key
     */
    private String buildContentFeatureKey(String contentType, Long contentId) {
        return String.format("%s%s:%d", CONTENT_FEATURE_PREFIX, contentType, contentId);
    }

    // ========== 便捷方法 ==========

    /**
     * 获取用户画像（使用默认TTL）
     */
    public UserProfile getUserProfile(Long userId) {
        return getUserProfile(userId);
    }

    /**
     * 设置用户画像（使用默认TTL）
     */
    public void setUserProfile(Long userId, UserProfile profile) {
        setUserProfile(userId, profile, USER_PROFILE_TTL);
    }

    /**
     * 获取推荐结果（使用默认TTL）
     */
    public RecommendResponse getRecommend(Long userId, String contentType, String recommendType) {
        return getRecommend(userId, contentType, recommendType);
    }

    /**
     * 设置推荐结果（使用默认TTL）
     */
    public void setRecommend(Long userId, String contentType, String recommendType, 
                              RecommendResponse response) {
        setRecommend(userId, contentType, recommendType, response, RECOMMEND_RESULT_TTL, TimeUnit.SECONDS);
    }

    /**
     * 获取热门内容（使用默认TTL）
     */
    public String getHotContent(String contentType, String period) {
        return getHotContent(contentType, period);
    }

    /**
     * 设置热门内容（使用默认TTL）
     */
    public void setHotContent(String contentType, String period, String contentIds) {
        setHotContent(contentType, period, contentIds, HOT_CONTENT_TTL);
    }

    /**
     * 获取内容特征（使用默认TTL）
     */
    public String getContentFeature(String contentType, Long contentId) {
        return getContentFeature(contentType, contentId);
    }

    /**
     * 设置内容特征（使用默认TTL）
     */
    public void setContentFeature(String contentType, Long contentId, String feature) {
        setContentFeature(contentType, contentId, feature, CONTENT_FEATURE_TTL);
    }
}
