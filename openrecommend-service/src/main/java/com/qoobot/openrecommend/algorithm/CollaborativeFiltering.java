package com.qoobot.openrecommend.algorithm;

import com.qoobot.openrecommend.entity.UserBehavior;
import com.qoobot.openrecommend.mapper.UserBehaviorMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 协同过滤推荐算法
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
@Component
public class CollaborativeFiltering {

    private static final Logger log = LoggerFactory.getLogger(CollaborativeFiltering.class);

    @Resource
    private UserBehaviorMapper userBehaviorMapper;

    /**
     * 基于用户的协同过滤 (User-based CF)
     * 
     * @param userId 目标用户ID
     * @param contentType 内容类型
     * @param k 相似用户数量
     * @param limit 推荐数量
     * @return 推荐结果（内容ID -> 预测得分）
     */
    public Map<Long, Double> userBasedCF(Long userId, String contentType, int k, int limit) {
        log.info("基于用户的协同过滤: userId={}, contentType={}, k={}", userId, contentType, k);

        // 1. 获取目标用户的行为
        List<UserBehavior> targetBehaviors = userBehaviorMapper.selectRecentBehaviors(userId, 30, 1000);
        if (targetBehaviors.isEmpty()) {
            log.warn("用户无历史行为: userId={}", userId);
            return new HashMap<>();
        }

        // 2. 获取目标用户交互的内容ID集合
        Set<Long> targetContentIds = targetBehaviors.stream()
            .filter(b -> contentType == null || contentType.equals(b.getContentType()))
            .map(UserBehavior::getContentId)
            .collect(Collectors.toSet());

        if (targetContentIds.isEmpty()) {
            return new HashMap<>();
        }

        // 3. 找到与目标用户相似的用户（简化版：基于内容交互重叠）
        Map<Long, Double> similarUsers = findSimilarUsers(userId, contentType, targetContentIds, k);
        if (similarUsers.isEmpty()) {
            return new HashMap<>();
        }

        // 4. 找出相似用户喜欢但目标用户未互动的内容
        Map<Long, List<Double>> candidateScores = new HashMap<>();
        for (Map.Entry<Long, Double> entry : similarUsers.entrySet()) {
            Long similarUserId = entry.getKey();
            Double similarity = entry.getValue();

            List<UserBehavior> similarUserBehaviors = userBehaviorMapper.selectRecentBehaviors(similarUserId, 30, 1000);
            for (UserBehavior behavior : similarUserBehaviors) {
                Long contentId = behavior.getContentId();
                if (!targetContentIds.contains(contentId)) {
                    double score = getBehaviorScore(behavior.getBehaviorType());
                    candidateScores.computeIfAbsent(contentId, k1 -> new ArrayList<>())
                        .add(score * similarity);
                }
            }
        }

        // 5. 计算预测得分（加权平均）
        Map<Long, Double> result = new LinkedHashMap<>();
        for (Map.Entry<Long, List<Double>> entry : candidateScores.entrySet()) {
            double predictedScore = entry.getValue().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
            result.put(entry.getKey(), predictedScore);
        }

        // 6. 排序并返回Top N
        return result.entrySet().stream()
            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    /**
     * 基于物品的协同过滤 (Item-based CF)
     * 
     * @param userId 目标用户ID
     * @param contentType 内容类型
     * @param k 相似物品数量
     * @param limit 推荐数量
     * @return 推荐结果（内容ID -> 预测得分）
     */
    public Map<Long, Double> itemBasedCF(Long userId, String contentType, int k, int limit) {
        log.info("基于物品的协同过滤: userId={}, contentType={}, k={}", userId, contentType, k);

        // 1. 获取目标用户的历史行为内容ID
        List<Long> userContentIds = userBehaviorMapper.selectHistoryContentIds(userId, contentType, 100);
        if (userContentIds.isEmpty()) {
            return new HashMap<>();
        }

        // 2. 获取用户的行为数据，用于计算物品相似度
        List<UserBehavior> allBehaviors = userBehaviorMapper.selectRecentBehaviors(userId, 90, 10000);

        // 3. 计算物品相似度（简化版：基于共同用户）
        Map<Long, Map<Long, Double>> itemSimilarity = calculateItemSimilarity(allBehaviors, contentType);

        // 4. 为每个历史物品找相似物品
        Map<Long, Double> candidateScores = new HashMap<>();
        for (Long contentId : userContentIds) {
            Map<Long, Double> similarItems = itemSimilarity.getOrDefault(contentId, new HashMap<>());
            
            // 取Top K相似物品
            similarItems.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(k)
                .forEach(entry -> {
                    Long similarItemId = entry.getKey();
                    Double similarity = entry.getValue();
                    
                    // 排除用户已经互动过的物品
                    if (!userContentIds.contains(similarItemId)) {
                        double existingScore = candidateScores.getOrDefault(similarItemId, 0.0);
                        candidateScores.put(similarItemId, existingScore + similarity);
                    }
                });
        }

        // 5. 排序并返回Top N
        return candidateScores.entrySet().stream()
            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    /**
     * 混合协同过滤（结合User-based和Item-based）
     * 
     * @param userId 目标用户ID
     * @param contentType 内容类型
     * @param limit 推荐数量
     * @return 推荐结果
     */
    public Map<Long, Double> hybridCF(Long userId, String contentType, int limit) {
        log.info("混合协同过滤: userId={}, contentType={}", userId, contentType);

        // 获取User-based和Item-based的结果
        Map<Long, Double> userBasedResult = userBasedCF(userId, contentType, 10, limit * 2);
        Map<Long, Double> itemBasedResult = itemBasedCF(userId, contentType, 10, limit * 2);

        // 加权融合
        Map<Long, Double> result = new LinkedHashMap<>();

        // User-based权重：0.4，Item-based权重：0.6
        userBasedResult.forEach((contentId, score) -> {
            result.merge(contentId, score * 0.4, Double::sum);
        });

        itemBasedResult.forEach((contentId, score) -> {
            result.merge(contentId, score * 0.6, Double::sum);
        });

        // 排序并返回Top N
        return result.entrySet().stream()
            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    // ========== 私有方法 ==========

    /**
     * 找到相似用户
     */
    private Map<Long, Double> findSimilarUsers(Long userId, String contentType, 
                                               Set<Long> targetContentIds, int k) {
        // 简化实现：找到与目标用户内容交互重叠最多的用户
        // 实际项目中应该使用更精确的相似度计算（如余弦相似度）

        // 这里简化处理，只找有共同内容交互的用户
        // TODO: 实际应该查询所有用户的行为数据来计算相似度

        // 临时返回空Map，需要完善
        log.warn("相似用户查找未完善: userId={}", userId);
        return new HashMap<>();
    }

    /**
     * 计算物品相似度
     */
    private Map<Long, Map<Long, Double>> calculateItemSimilarity(List<UserBehavior> behaviors, 
                                                                   String contentType) {
        Map<Long, Map<Long, Integer>> coOccurrence = new HashMap<>();

        // 统计物品共现次数
        for (int i = 0; i < behaviors.size(); i++) {
            for (int j = i + 1; j < behaviors.size(); j++) {
                UserBehavior b1 = behaviors.get(i);
                UserBehavior b2 = behaviors.get(j);

                // 过滤内容类型
                if (contentType != null && 
                    (!contentType.equals(b1.getContentType()) || !contentType.equals(b2.getContentType()))) {
                    continue;
                }

                // 同一用户的不同物品
                if (b1.getUserId().equals(b2.getUserId()) && 
                    !b1.getContentId().equals(b2.getContentId())) {
                    Long item1 = b1.getContentId();
                    Long item2 = b2.getContentId();

                    coOccurrence.computeIfAbsent(item1, k -> new HashMap<>())
                        .merge(item2, 1, Integer::sum);
                    coOccurrence.computeIfAbsent(item2, k -> new HashMap<>())
                        .merge(item1, 1, Integer::sum);
                }
            }
        }

        // 计算相似度（Jaccard相似度简化版）
        Map<Long, Map<Long, Double>> similarity = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Integer>> entry : coOccurrence.entrySet()) {
            Long item1 = entry.getKey();
            Map<Long, Integer> coItems = entry.getValue();

            Map<Long, Double> itemSim = new HashMap<>();
            for (Map.Entry<Long, Integer> coEntry : coItems.entrySet()) {
                Long item2 = coEntry.getKey();
                Integer coCount = coEntry.getValue();

                // 简化计算：直接使用共现次数作为相似度
                // 实际应该除以各自的出现次数
                itemSim.put(item2, (double) coCount);
            }

            similarity.put(item1, itemSim);
        }

        return similarity;
    }

    /**
     * 获取行为得分
     */
    private double getBehaviorScore(Integer behaviorType) {
        if (behaviorType == null) {
            return 1.0;
        }
        return switch (behaviorType) {
            case 1 -> 1.0;  // 浏览
            case 2 -> 3.0;  // 点赞
            case 3 -> 5.0;  // 收藏
            case 4 -> 4.0;  // 分享
            case 5 -> 3.0;  // 评论
            default -> 1.0;
        };
    }
}
