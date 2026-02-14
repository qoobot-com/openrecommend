package com.qoobot.openrecommend.service;

import com.qoobot.openrecommend.dto.RecommendItem;

import java.util.List;

/**
 * 排序服务接口
 * 提供推荐结果的排序、重排序和多样性处理功能
 */
public interface RankingService {

    /**
     * 对推荐结果进行排序打分
     *
     * @param items 待排序的推荐项列表
     * @param userId 用户ID（用于个性化打分）
     * @return 排序后的推荐项列表
     */
    List<RecommendItem> rank(List<RecommendItem> items, Long userId);

    /**
     * 重排序推荐结果
     * 结合多种因素进行综合排序
     *
     * @param items 待重排序的推荐项列表
     * @param userId 用户ID
     * @param context 上下文信息（如时间、设备等）
     * @return 重排序后的推荐项列表
     */
    List<RecommendItem> rerank(List<RecommendItem> items, Long userId, RankingContext context);

    /**
     * 多样性处理
     * 确保推荐结果的多样性，避免过于集中的推荐
     *
     * @param items 推荐项列表
     * @param diversityLevel 多样性级别（1-10，越大越多样）
     * @return 多样性处理后的推荐项列表
     */
    List<RecommendItem> diversify(List<RecommendItem> items, int diversityLevel);

    /**
     * 混合排序
     * 结合相关性和多样性
     *
     * @param items 推荐项列表
     * @param userId 用户ID
     * @param alpha 相关性权重（0-1）
     * @param beta 多样性权重（0-1）
     * @return 排序后的推荐项列表
     */
    List<RecommendItem> mixedRank(List<RecommendItem> items, Long userId, double alpha, double beta);

    /**
     * 新鲜度排序
     * 优先推荐最新内容
     *
     * @param items 推荐项列表
     * @param decayTime 衰减时间（小时）
     * @return 排序后的推荐项列表
     */
    List<RecommendItem> rankByFreshness(List<RecommendItem> items, long decayTime);

    /**
     * 热度排序
     * 优先推荐热门内容
     *
     * @param items 推荐项列表
     * @param timeWindow 时间窗口（小时）
     * @return 排序后的推荐项列表
     */
    List<RecommendItem> rankByPopularity(List<RecommendItem> items, long timeWindow);

    /**
     * 个性化排序
     * 基于用户偏好进行排序
     *
     * @param items 推荐项列表
     * @param userId 用户ID
     * @return 排序后的推荐项列表
     */
    List<RecommendItem> personalizedRank(List<RecommendItem> items, Long userId);

    /**
     * 排序上下文信息
     */
    class RankingContext {
        private String device;
        private String timeOfDay;
        private String location;
        private int pageSize;

        public RankingContext() {
        }

        public RankingContext(String device, String timeOfDay, String location, int pageSize) {
            this.device = device;
            this.timeOfDay = timeOfDay;
            this.location = location;
            this.pageSize = pageSize;
        }

        public String getDevice() {
            return device;
        }

        public void setDevice(String device) {
            this.device = device;
        }

        public String getTimeOfDay() {
            return timeOfDay;
        }

        public void setTimeOfDay(String timeOfDay) {
            this.timeOfDay = timeOfDay;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }
    }
}
