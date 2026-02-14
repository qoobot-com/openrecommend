package com.qoobot.openrecommend.service;

import com.qoobot.openrecommend.api.dto.RecommendRequest;
import com.qoobot.openrecommend.api.dto.RecommendResponse;

/**
 * 推荐服务接口
 *
 * @author Qoobot Team
 * @since 1.0.0
 */
public interface RecommendService {

    /**
     * 综合推荐
     *
     * @param request 推荐请求
     * @return 推荐响应
     */
    RecommendResponse recommend(RecommendRequest request);

    /**
     * 基于内容的推荐
     *
     * @param userId       用户ID
     * @param contentType  内容类型
     * @param limit        推荐数量
     * @return 推荐响应
     */
    RecommendResponse recommendByContent(Long userId, String contentType, int limit);

    /**
     * 协同过滤推荐
     *
     * @param userId       用户ID
     * @param contentType  内容类型
     * @param limit        推荐数量
     * @return 推荐响应
     */
    RecommendResponse recommendByCollaborativeFiltering(Long userId, String contentType, int limit);

    /**
     * 热门推荐
     *
     * @param contentType 内容类型
     * @param limit      推荐数量
     * @return 推荐响应
     */
    RecommendResponse recommendHot(String contentType, int limit);

    /**
     * 相关推荐
     *
     * @param contentId 内容ID
     * @param limit     推荐数量
     * @return 推荐响应
     */
    RecommendResponse recommendRelated(Long contentId, int limit);
}
