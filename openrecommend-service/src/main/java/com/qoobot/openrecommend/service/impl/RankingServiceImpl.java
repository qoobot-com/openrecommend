package com.qoobot.openrecommend.service.impl;

import com.qoobot.openrecommend.dto.RecommendItem;
import com.qoobot.openrecommend.entity.UserProfile;
import com.qoobot.openrecommend.service.CacheService;
import com.qoobot.openrecommend.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 排序服务实现
 * 提供推荐结果的排序、重排序和多样性处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final CacheService cacheService;

    private static final double DEFAULT_ALPHA = 0.7;
    private static final double DEFAULT_BETA = 0.3;
    private static final int DEFAULT_DIVERSITY_LEVEL = 5;
    private static final long DEFAULT_DECAY_TIME = 24; // 24小时
    private static final long DEFAULT_TIME_WINDOW = 72; // 72小时

    @Override
    public List<RecommendItem> rank(List<RecommendItem> items, Long userId) {
        if (items == null || items.isEmpty()) {
            return items;
        }

        log.debug("开始对{}个推荐项进行排序，用户ID: {}", items.size(), userId);

        UserProfile userProfile = cacheService.getUserProfile(userId);

        for (RecommendItem item : items) {
            double score = calculateScore(item, userProfile);
            item.setScore(score);
        }

        // 按分数降序排序
        items.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        log.debug("排序完成，最高分: {}, 最低分: {}",
                items.get(0).getScore(), items.get(items.size() - 1).getScore());

        return items;
    }

    @Override
    public List<RecommendItem> rerank(List<RecommendItem> items, Long userId, RankingContext context) {
        if (items == null || items.isEmpty()) {
            return items;
        }

        log.debug("开始重排序，用户ID: {}, 设备: {}, 时间段: {}",
                userId, context != null ? context.getDevice() : "unknown",
                context != null ? context.getTimeOfDay() : "unknown");

        UserProfile userProfile = cacheService.getUserProfile(userId);

        for (RecommendItem item : items) {
            double score = calculateRerankScore(item, userProfile, context);
            item.setScore(score);
        }

        items.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        return items;
    }

    @Override
    public List<RecommendItem> diversify(List<RecommendItem> items, int diversityLevel) {
        if (items == null || items.isEmpty()) {
            return items;
        }

        log.debug("开始多样性处理，级别: {}, 推荐项数量: {}", diversityLevel, items.size());

        List<RecommendItem> result = new ArrayList<>();
        Map<String, Integer> categoryCount = new HashMap<>();

        // 按分数排序
        List<RecommendItem> sorted = new ArrayList<>(items);
        sorted.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // 基于多样性级别调整每个类别的最大数量
        int maxPerCategory = Math.max(1, (int) Math.ceil(items.size() / (double) (diversityLevel + 1)));

        for (RecommendItem item : sorted) {
            String category = item.getCategory() != null ? item.getCategory() : "default";
            int count = categoryCount.getOrDefault(category, 0);

            if (count < maxPerCategory) {
                result.add(item);
                categoryCount.put(category, count + 1);
            }
        }

        // 如果还有剩余位置，按分数顺序填充
        if (result.size() < items.size()) {
            for (RecommendItem item : sorted) {
                if (!result.contains(item)) {
                    result.add(item);
                }
            }
        }

        log.debug("多样性处理完成，类别分布: {}", categoryCount);

        return result;
    }

    @Override
    public List<RecommendItem> mixedRank(List<RecommendItem> items, Long userId, double alpha, double beta) {
        if (items == null || items.isEmpty()) {
            return items;
        }

        log.debug("开始混合排序，用户ID: {}, 相关性权重: {}, 多样性权重: {}", userId, alpha, beta);

        // 首先计算相关性分数
        UserProfile userProfile = cacheService.getUserProfile(userId);
        for (RecommendItem item : items) {
            item.setScore(calculateScore(item, userProfile));
        }

        // 计算多样性分数
        Map<String, List<RecommendItem>> categoryGroups = items.stream()
                .collect(Collectors.groupingBy(item ->
                        item.getCategory() != null ? item.getCategory() : "default"));

        for (RecommendItem item : items) {
            String category = item.getCategory() != null ? item.getCategory() : "default";
            int categorySize = categoryGroups.get(category).size();
            double diversityScore = 1.0 / (1.0 + categorySize);
            double combinedScore = alpha * item.getScore() + beta * diversityScore;
            item.setScore(combinedScore);
        }

        items.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        return items;
    }

    @Override
    public List<RecommendItem> rankByFreshness(List<RecommendItem> items, long decayTime) {
        if (items == null || items.isEmpty()) {
            return items;
        }

        log.debug("开始新鲜度排序，衰减时间: {} 小时", decayTime);

        LocalDateTime now = LocalDateTime.now();

        for (RecommendItem item : items) {
            LocalDateTime publishTime = item.getPublishTime();
            if (publishTime != null) {
                long hoursDiff = ChronoUnit.HOURS.between(publishTime, now);
                double freshnessScore = Math.exp(-hoursDiff / (double) decayTime);
                item.setScore(freshnessScore);
            } else {
                item.setScore(0.0);
            }
        }

        items.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        return items;
    }

    @Override
    public List<RecommendItem> rankByPopularity(List<RecommendItem> items, long timeWindow) {
        if (items == null || items.isEmpty()) {
            return items;
        }

        log.debug("开始热度排序，时间窗口: {} 小时", timeWindow);

        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(timeWindow);

        for (RecommendItem item : items) {
            LocalDateTime publishTime = item.getPublishTime();
            long viewCount = item.getViewCount() != null ? item.getViewCount() : 0;
            long likeCount = item.getLikeCount() != null ? item.getLikeCount() : 0;

            double popularityScore;

            if (publishTime != null && publishTime.isAfter(cutoffTime)) {
                // 在时间窗口内的内容，按互动数排序
                popularityScore = viewCount * 1.0 + likeCount * 5.0;
            } else {
                // 超出时间窗口的内容，降低权重
                popularityScore = (viewCount * 1.0 + likeCount * 5.0) * 0.5;
            }

            item.setScore(popularityScore);
        }

        items.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        return items;
    }

    @Override
    public List<RecommendItem> personalizedRank(List<RecommendItem> items, Long userId) {
        if (items == null || items.isEmpty()) {
            return items;
        }

        log.debug("开始个性化排序，用户ID: {}", userId);

        UserProfile userProfile = cacheService.getUserProfile(userId);
        if (userProfile == null) {
            log.warn("用户画像不存在，使用默认排序");
            return items;
        }

        Map<String, Double> interestTags = userProfile.getInterestTags();
        Map<String, Integer> contentPreference = userProfile.getContentPreference();

        for (RecommendItem item : items) {
            double score = calculatePersonalizedScore(item, interestTags, contentPreference);
            item.setScore(score);
        }

        items.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        return items;
    }

    /**
     * 计算推荐项的综合分数
     */
    private double calculateScore(RecommendItem item, UserProfile userProfile) {
        double score = 0.0;

        // 相关性分数 (40%)
        double relevanceScore = calculateRelevanceScore(item, userProfile);
        score += relevanceScore * 0.4;

        // 质量分数 (30%)
        double qualityScore = item.getQualityScore() != null ? item.getQualityScore() : 0;
        score += qualityScore * 0.3;

        // 热度分数 (20%)
        double popularityScore = calculatePopularityScore(item);
        score += popularityScore * 0.2;

        // 新鲜度分数 (10%)
        double freshnessScore = calculateFreshnessScore(item);
        score += freshnessScore * 0.1;

        return score;
    }

    /**
     * 计算重排序分数（考虑上下文）
     */
    private double calculateRerankScore(RecommendItem item, UserProfile userProfile, RankingContext context) {
        double baseScore = calculateScore(item, userProfile);

        if (context == null) {
            return baseScore;
        }

        // 根据设备类型调整分数
        if ("mobile".equals(context.getDevice())) {
            // 移动端偏好短视频和图片
            if ("video".equals(item.getContentType()) || "image".equals(item.getContentType())) {
                baseScore *= 1.1;
            }
        }

        // 根据时间段调整分数
        if (userProfile != null) {
            String timeOfDay = context.getTimeOfDay();
            Map<String, Integer> activeTimeSlots = userProfile.getActiveTimeSlots();

            if (timeOfDay != null && activeTimeSlots != null) {
                Integer activityLevel = activeTimeSlots.get(timeOfDay);
                if (activityLevel != null && activityLevel > 0) {
                    // 用户活跃时间段，适当提升分数
                    baseScore *= 1.05;
                }
            }
        }

        return baseScore;
    }

    /**
     * 计算个性化分数
     */
    private double calculatePersonalizedScore(RecommendItem item,
                                               Map<String, Double> interestTags,
                                               Map<String, Integer> contentPreference) {
        double score = 0.0;

        // 基于兴趣标签计算
        if (item.getTags() != null && interestTags != null) {
            for (String tag : item.getTags()) {
                Double tagWeight = interestTags.get(tag);
                if (tagWeight != null) {
                    score += tagWeight;
                }
            }
        }

        // 基于内容类型偏好计算
        if (item.getContentType() != null && contentPreference != null) {
            Integer preference = contentPreference.get(item.getContentType());
            if (preference != null) {
                score += preference * 0.1;
            }
        }

        // 归一化分数
        score = Math.min(score, 100.0);

        return score;
    }

    /**
     * 计算相关性分数
     */
    private double calculateRelevanceScore(RecommendItem item, UserProfile userProfile) {
        if (userProfile == null) {
            return 0.5;
        }

        double relevanceScore = 0.0;

        // 基于标签的相关性
        if (item.getTags() != null && userProfile.getInterestTags() != null) {
            for (String tag : item.getTags()) {
                Double tagWeight = userProfile.getInterestTags().get(tag);
                if (tagWeight != null) {
                    relevanceScore += tagWeight;
                }
            }
        }

        // 归一化到0-1之间
        return Math.min(relevanceScore / 10.0, 1.0);
    }

    /**
     * 计算热度分数
     */
    private double calculatePopularityScore(RecommendItem item) {
        long viewCount = item.getViewCount() != null ? item.getViewCount() : 0;
        long likeCount = item.getLikeCount() != null ? item.getLikeCount() : 0;

        // 使用对数缩放，避免热门内容占据全部推荐
        double normalizedViews = Math.log1p(viewCount);
        double normalizedLikes = Math.log1p(likeCount);

        // 归一化到0-1之间
        return (normalizedViews + normalizedLikes * 5.0) / 20.0;
    }

    /**
     * 计算新鲜度分数
     */
    private double calculateFreshnessScore(RecommendItem item) {
        LocalDateTime publishTime = item.getPublishTime();
        if (publishTime == null) {
            return 0.5;
        }

        long hoursDiff = ChronoUnit.HOURS.between(publishTime, LocalDateTime.now());

        // 使用指数衰减，越新的内容分数越高
        return Math.exp(-hoursDiff / (double) DEFAULT_DECAY_TIME);
    }
}
