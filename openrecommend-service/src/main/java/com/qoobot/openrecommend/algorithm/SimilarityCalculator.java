package com.qoobot.openrecommend.algorithm;

import java.util.Map;
import java.util.Set;

/**
 * 相似度计算工具类
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
public class SimilarityCalculator {

    /**
     * 计算余弦相似度
     * 
     * @param vector1 向量1（特征名 -> 权重）
     * @param vector2 向量2（特征名 -> 权重）
     * @return 余弦相似度 [0, 1]
     */
    public static double cosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        if (vector1 == null || vector2 == null || vector1.isEmpty() || vector2.isEmpty()) {
            return 0.0;
        }

        // 找出共同的特征
        Set<String> commonKeys = vector1.keySet();
        commonKeys.retainAll(vector2.keySet());

        if (commonKeys.isEmpty()) {
            return 0.0;
        }

        // 计算点积
        double dotProduct = commonKeys.stream()
            .mapToDouble(key -> vector1.get(key) * vector2.get(key))
            .sum();

        // 计算向量1的模
        double norm1 = Math.sqrt(vector1.values().stream()
            .mapToDouble(v -> v * v)
            .sum());

        // 计算向量2的模
        double norm2 = Math.sqrt(vector2.values().stream()
            .mapToDouble(v -> v * v)
            .sum());

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (norm1 * norm2);
    }

    /**
     * 计算Jaccard相似度
     * 
     * @param set1 集合1
     * @param set2 集合2
     * @param <T> 类型
     * @return Jaccard相似度 [0, 1]
     */
    public static <T> double jaccardSimilarity(Set<T> set1, Set<T> set2) {
        if (set1 == null || set2 == null) {
            return 0.0;
        }

        if (set1.isEmpty() && set2.isEmpty()) {
            return 1.0;
        }

        // 交集大小
        Set<T> intersection = new java.util.HashSet<>(set1);
        intersection.retainAll(set2);
        int intersectionSize = intersection.size();

        // 并集大小
        Set<T> union = new java.util.HashSet<>(set1);
        union.addAll(set2);
        int unionSize = union.size();

        return unionSize == 0 ? 0.0 : (double) intersectionSize / unionSize;
    }

    /**
     * 计算皮尔逊相关系数
     * 
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 皮尔逊相关系数 [-1, 1]
     */
    public static double pearsonCorrelation(Map<String, Double> vector1, Map<String, Double> vector2) {
        if (vector1 == null || vector2 == null || vector1.isEmpty() || vector2.isEmpty()) {
            return 0.0;
        }

        // 找出共同的特征
        Set<String> commonKeys = new java.util.HashSet<>(vector1.keySet());
        commonKeys.retainAll(vector2.keySet());

        if (commonKeys.size() < 2) {
            return 0.0;
        }

        int n = commonKeys.size();

        // 计算平均值
        double avg1 = commonKeys.stream().mapToDouble(vector1::get).average().orElse(0.0);
        double avg2 = commonKeys.stream().mapToDouble(vector2::get).average().orElse(0.0);

        // 计算协方差和标准差
        double covariance = 0.0;
        double variance1 = 0.0;
        double variance2 = 0.0;

        for (String key : commonKeys) {
            double diff1 = vector1.get(key) - avg1;
            double diff2 = vector2.get(key) - avg2;

            covariance += diff1 * diff2;
            variance1 += diff1 * diff1;
            variance2 += diff2 * diff2;
        }

        if (variance1 == 0 || variance2 == 0) {
            return 0.0;
        }

        return covariance / Math.sqrt(variance1 * variance2);
    }

    /**
     * 计算欧氏距离
     * 
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 欧氏距离
     */
    public static double euclideanDistance(Map<String, Double> vector1, Map<String, Double> vector2) {
        if (vector1 == null || vector2 == null) {
            return Double.MAX_VALUE;
        }

        // 找出所有特征
        Set<String> allKeys = new java.util.HashSet<>(vector1.keySet());
        allKeys.addAll(vector2.keySet());

        double sum = allKeys.stream()
            .mapToDouble(key -> {
                double v1 = vector1.getOrDefault(key, 0.0);
                double v2 = vector2.getOrDefault(key, 0.0);
                return (v1 - v2) * (v1 - v2);
            })
            .sum();

        return Math.sqrt(sum);
    }

    /**
     * 将欧氏距离转换为相似度
     * 
     * @param distance 欧氏距离
     * @param maxDistance 最大距离（归一化用）
     * @return 相似度 [0, 1]
     */
    public static double distanceToSimilarity(double distance, double maxDistance) {
        if (maxDistance <= 0) {
            return 0.0;
        }
        return Math.max(0.0, 1.0 - distance / maxDistance);
    }

    /**
     * 归一化向量
     * 
     * @param vector 原向量
     * @return 归一化后的向量
     */
    public static Map<String, Double> normalize(Map<String, Double> vector) {
        if (vector == null || vector.isEmpty()) {
            return vector;
        }

        // 计算L2范数
        double norm = Math.sqrt(vector.values().stream()
            .mapToDouble(v -> v * v)
            .sum());

        if (norm == 0) {
            return vector;
        }

        // 归一化
        return vector.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue() / norm,
                (e1, e2) -> e1,
                java.util.LinkedHashMap::new
            ));
    }
}
