package com.qoobot.openrecommend.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoobot.openrecommend.entity.UserBehavior;
import com.qoobot.openrecommend.entity.UserProfile;
import com.qoobot.openrecommend.mapper.UserBehaviorMapper;
import com.qoobot.openrecommend.mapper.UserProfileMapper;
import com.qoobot.openrecommend.service.UserProfileService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户画像服务实现
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
@Service
public class UserProfileServiceImpl implements UserProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileServiceImpl.class);
    private static final String CACHE_KEY_PREFIX = "user_profile:";
    private static final int CACHE_TTL_HOURS = 1;

    @Resource
    private UserProfileMapper userProfileMapper;

    @Resource
    private UserBehaviorMapper userBehaviorMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public UserProfile getByUserId(Long userId) {
        // 1. 先从缓存获取
        String cacheKey = CACHE_KEY_PREFIX + userId;
        UserProfile cachedProfile = (UserProfile) redisTemplate.opsForValue().get(cacheKey);
        if (cachedProfile != null) {
            log.debug("用户画像缓存命中: userId={}", userId);
            return cachedProfile;
        }

        // 2. 缓存未命中，从数据库查询
        UserProfile profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            // 3. 如果不存在，创建默认画像
            profile = createDefaultProfile(userId);
            userProfileMapper.insert(profile);
        }

        // 4. 写入缓存
        redisTemplate.opsForValue().set(cacheKey, profile, CACHE_TTL_HOURS, TimeUnit.HOURS);

        return profile;
    }

    @Override
    @Async("taskExecutor")
    public void updateAsync(Long userId) {
        try {
            log.info("开始异步更新用户画像: userId={}", userId);
            UserProfile profile = calculateProfile(userId);
            saveOrUpdate(profile);

            // 清除缓存
            String cacheKey = CACHE_KEY_PREFIX + userId;
            redisTemplate.delete(cacheKey);

            log.info("用户画像更新完成: userId={}", userId);
        } catch (Exception e) {
            log.error("异步更新用户画像失败: userId={}", userId, e);
        }
    }

    @Override
    public UserProfile calculateProfile(Long userId) {
        log.info("开始计算用户画像: userId={}", userId);

        // 1. 获取现有画像
        UserProfile existingProfile = userProfileMapper.selectByUserId(userId);
        if (existingProfile == null) {
            existingProfile = createDefaultProfile(userId);
        }

        // 2. 获取用户近期行为（最近30天）
        List<UserBehavior> behaviors = userBehaviorMapper.selectRecentBehaviors(userId, 30, 1000);

        if (behaviors.isEmpty()) {
            log.info("用户无历史行为，使用默认画像: userId={}", userId);
            return existingProfile;
        }

        // 3. 计算兴趣标签及权重
        Map<String, Double> interestTags = calculateInterestTags(behaviors);

        // 4. 计算内容类型偏好
        Map<String, Double> contentPreference = calculateContentPreference(behaviors);

        // 5. 计算活跃时段
        List<Integer> activePeriods = calculateActivePeriods(behaviors);

        // 6. 更新画像数据
        existingProfile.setInterestTags(objectMapper.valueToTree(interestTags));
        existingProfile.setContentPreference(objectMapper.valueToTree(contentPreference));
        existingProfile.setActivePeriods(objectMapper.valueToTree(activePeriods));
        existingProfile.setLastUpdateTime(LocalDateTime.now());

        // 7. 计算总浏览量和阅读时长
        int totalViewCount = (int) behaviors.stream()
            .filter(b -> b.getBehaviorType() == 1)
            .count();
        int totalReadTime = behaviors.stream()
            .filter(b -> b.getDuration() != null)
            .mapToInt(UserBehavior::getDuration)
            .sum() / 60; // 转换为分钟

        existingProfile.setTotalViewCount(totalViewCount);
        existingProfile.setTotalReadTime(existingProfile.getTotalReadTime() + totalReadTime);

        log.info("用户画像计算完成: userId={}, tags={}, contentPreference={}", 
            userId, interestTags, contentPreference);

        return existingProfile;
    }

    @Override
    public boolean saveOrUpdate(UserProfile profile) {
        if (profile.getId() == null) {
            return userProfileMapper.insert(profile) > 0;
        } else {
            return userProfileMapper.updateById(profile) > 0;
        }
    }

    /**
     * 创建默认用户画像
     */
    private UserProfile createDefaultProfile(Long userId) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setAgeRange("25-34");
        profile.setGender(0); // 未知

        // 默认兴趣标签
        Map<String, Double> defaultTags = new LinkedHashMap<>();
        defaultTags.put("科技", 0.5);
        defaultTags.put("生活", 0.3);
        defaultTags.put("娱乐", 0.2);
        profile.setInterestTags(objectMapper.valueToTree(defaultTags));

        // 默认内容偏好
        Map<String, Double> defaultPreference = new LinkedHashMap<>();
        defaultPreference.put("article", 0.5);
        defaultPreference.put("image", 0.3);
        defaultPreference.put("video", 0.2);
        profile.setContentPreference(objectMapper.valueToTree(defaultPreference));

        // 默认活跃时段
        profile.setActivePeriods(objectMapper.valueToTree(Arrays.asList(9, 12, 18, 21)));
        profile.setTotalReadTime(0);
        profile.setTotalViewCount(0);
        profile.setDevicePreference("mobile");
        profile.setLastUpdateTime(LocalDateTime.now());

        return profile;
    }

    /**
     * 计算兴趣标签及权重
     */
    private Map<String, Double> calculateInterestTags(List<UserBehavior> behaviors) {
        Map<String, Integer> tagCount = new HashMap<>();
        Map<String, Integer> tagWeight = new HashMap<>();

        for (UserBehavior behavior : behaviors) {
            // 这里简化处理，实际应该从内容中提取标签
            // 为演示目的，假设从行为类型推断兴趣
            String tag = inferTagFromBehavior(behavior);
            int weight = getBehaviorWeight(behavior.getBehaviorType());

            tagCount.merge(tag, 1, Integer::sum);
            tagWeight.merge(tag, weight, Integer::sum);
        }

        // 计算每个标签的权重
        Map<String, Double> result = new LinkedHashMap<>();
        int totalWeight = tagWeight.values().stream().mapToInt(Integer::sum).sum();

        if (totalWeight > 0) {
            tagWeight.forEach((tag, weight) -> {
                double normalizedWeight = (double) weight / totalWeight;
                result.put(tag, Math.round(normalizedWeight * 1000) / 1000.0);
            });
        }

        // 限制标签数量，保留前10个
        return result.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    /**
     * 计算内容类型偏好
     */
    private Map<String, Double> calculateContentPreference(List<UserBehavior> behaviors) {
        Map<String, Integer> typeCount = new HashMap<>();
        Map<String, Integer> typeWeight = new HashMap<>();

        for (UserBehavior behavior : behaviors) {
            String contentType = behavior.getContentType();
            int weight = getBehaviorWeight(behavior.getBehaviorType());

            typeCount.merge(contentType, 1, Integer::sum);
            typeWeight.merge(contentType, weight, Integer::sum);
        }

        // 计算每个内容类型的偏好权重
        Map<String, Double> result = new LinkedHashMap<>();
        int totalWeight = typeWeight.values().stream().mapToInt(Integer::sum).sum();

        if (totalWeight > 0) {
            typeWeight.forEach((type, weight) -> {
                double normalizedWeight = (double) weight / totalWeight;
                result.put(type, Math.round(normalizedWeight * 1000) / 1000.0);
            });
        }

        return result;
    }

    /**
     * 计算活跃时段
     */
    private List<Integer> calculateActivePeriods(List<UserBehavior> behaviors) {
        Map<Integer, Integer> hourCount = new HashMap<>();

        for (UserBehavior behavior : behaviors) {
            int hour = behavior.getCreateTime().getHour();
            hourCount.merge(hour, 1, Integer::sum);
        }

        // 按活跃度排序，返回前4个时段
        return hourCount.entrySet().stream()
            .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
            .limit(4)
            .map(Map.Entry::getKey)
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * 从行为推断兴趣标签（简化版）
     */
    private String inferTagFromBehavior(UserBehavior behavior) {
        // 简化处理：根据内容类型和行为类型推断
        String contentType = behavior.getContentType();
        int behaviorType = behavior.getBehaviorType();

        if (contentType.equals("article")) {
            if (behaviorType >= 3) {
                return "深度阅读";
            } else {
                return "资讯";
            }
        } else if (contentType.equals("image")) {
            return "视觉";
        } else if (contentType.equals("video")) {
            return "娱乐";
        }

        return "综合";
    }

    /**
     * 获取行为权重
     */
    private int getBehaviorWeight(Integer behaviorType) {
        if (behaviorType == null) {
            return 1;
        }
        return switch (behaviorType) {
            case 1 -> 1;  // 浏览
            case 2 -> 3;  // 点赞
            case 3 -> 5;  // 收藏
            case 4 -> 4;  // 分享
            case 5 -> 3;  // 评论
            default -> 1;
        };
    }
}
