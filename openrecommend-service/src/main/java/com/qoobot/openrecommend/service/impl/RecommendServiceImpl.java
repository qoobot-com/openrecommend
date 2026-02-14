package com.qoobot.openrecommend.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoobot.openrecommend.api.dto.RecommendItem;
import com.qoobot.openrecommend.api.dto.RecommendRequest;
import com.qoobot.openrecommend.api.dto.RecommendResponse;
import com.qoobot.openrecommend.algorithm.CollaborativeFiltering;
import com.qoobot.openrecommend.algorithm.ContentBasedRecommender;
import com.qoobot.openrecommend.common.enums.ContentType;
import com.qoobot.openrecommend.entity.*;
import com.qoobot.openrecommend.mapper.ArticleMapper;
import com.qoobot.openrecommend.mapper.ImageMapper;
import com.qoobot.openrecommend.mapper.UserBehaviorMapper;
import com.qoobot.openrecommend.mapper.VideoMapper;
import com.qoobot.openrecommend.service.CacheService;
import com.qoobot.openrecommend.service.RecommendService;
import com.qoobot.openrecommend.service.UserProfileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 推荐服务实现
 *
 * @author OpenRecommend
 * @since 1.0.0
 */
@Slf4j
@Service
public class RecommendServiceImpl implements RecommendService {

    @Resource
    private UserProfileService userProfileService;

    @Resource
    private CacheService cacheService;

    @Resource
    private ContentBasedRecommender contentBasedRecommender;

    @Resource
    private CollaborativeFiltering collaborativeFiltering;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private ImageMapper imageMapper;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private UserBehaviorMapper userBehaviorMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "taskExecutor")
    private Executor taskExecutor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public RecommendResponse recommend(RecommendRequest request) {
        Long userId = request.getUserId();
        String contentTypeStr = request.getContentType();
        int limit = request.getLimit();
        String recommendType = request.getRecommendType();

        log.info("执行综合推荐，用户ID: {}, 内容类型: {}, 推荐类型: {}, 推荐数量: {}",
                userId, contentTypeStr, recommendType, limit);

        // 1. 检查缓存
        RecommendResponse cachedResponse = cacheService.getRecommend(userId, contentTypeStr, recommendType);
        if (cachedResponse != null) {
            log.info("推荐结果缓存命中: userId={}", userId);
            return cachedResponse;
        }

        // 2. 获取用户画像
        UserProfile userProfile = userProfileService.getByUserId(userId);
        Map<String, Double> interestTags = extractInterestTags(userProfile);

        // 3. 获取用户历史内容ID
        List<Long> historyContentIds = userBehaviorMapper.selectHistoryContentIds(
            userId, contentTypeStr, 100);

        // 4. 并行执行多种推荐策略
        ContentType contentType = ContentType.fromString(contentTypeStr);

        List<CompletableFuture<List<RecommendItem>>> futures = new ArrayList<>();

        // 基于内容推荐
        if (recommendType == null || "personal".equals(recommendType)) {
            futures.add(CompletableFuture.supplyAsync(() -> 
                recommendByContentInternal(userId, contentType, interestTags, historyContentIds, limit), 
                taskExecutor));
        }

        // 协同过滤推荐
        if (recommendType == null || "personal".equals(recommendType)) {
            futures.add(CompletableFuture.supplyAsync(() -> 
                recommendByCollaborativeFilteringInternal(userId, contentTypeStr, limit), 
                taskExecutor));
        }

        // 热门推荐
        if (recommendType == null || "popular".equals(recommendType)) {
            futures.add(CompletableFuture.supplyAsync(() -> 
                recommendHotInternal(contentTypeStr, limit), 
                taskExecutor));
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 5. 合并结果
        List<RecommendItem> allItems = futures.stream()
            .flatMap(future -> future.join().stream())
            .collect(Collectors.toList());

        // 6. 去重并排序
        List<RecommendItem> resultItems = deduplicateAndRank(allItems, limit);

        // 7. 构建响应
        RecommendResponse response = RecommendResponse.builder()
                .items(resultItems)
                .timestamp(System.currentTimeMillis())
                .build();

        // 8. 缓存结果
        cacheService.setRecommend(userId, contentTypeStr, recommendType, response, 30, TimeUnit.MINUTES);

        return response;
    }

    @Override
    public RecommendResponse recommendByContent(Long userId, String contentType, int limit) {
        log.info("执行基于内容的推荐，用户ID: {}, 内容类型: {}, 推荐数量: {}",
                userId, contentType, limit);

        // 获取用户画像
        UserProfile userProfile = userProfileService.getByUserId(userId);
        Map<String, Double> interestTags = extractInterestTags(userProfile);

        // 获取用户历史内容ID
        List<Long> historyContentIds = userBehaviorMapper.selectHistoryContentIds(
            userId, contentType, 100);

        return RecommendResponse.builder()
                .items(recommendByContentInternal(userId, ContentType.fromString(contentType), 
                    interestTags, historyContentIds, limit))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @Override
    public RecommendResponse recommendByCollaborativeFiltering(Long userId, String contentType, int limit) {
        log.info("执行协同过滤推荐，用户ID: {}, 内容类型: {}, 推荐数量: {}",
                userId, contentType, limit);

        return RecommendResponse.builder()
                .items(recommendByCollaborativeFilteringInternal(userId, contentType, limit))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @Override
    public RecommendResponse recommendHot(String contentType, int limit) {
        log.info("执行热门推荐，内容类型: {}, 推荐数量: {}", contentType, limit);

        return RecommendResponse.builder()
                .items(recommendHotInternal(contentType, limit))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @Override
    public RecommendResponse recommendRelated(Long contentId, int limit) {
        log.info("执行相关推荐，内容ID: {}, 推荐数量: {}", contentId, limit);

        List<RecommendItem> items = new ArrayList<>();

        // 尝试从不同内容类型中查找
        // 先尝试文章
        Article article = articleMapper.selectById(contentId);
        if (article != null) {
            items = recommendRelatedArticle(article, limit);
        } else {
            // 尝试图片
            Image image = imageMapper.selectById(contentId);
            if (image != null) {
                items = recommendRelatedImage(image, limit);
            } else {
                // 尝试视频
                Video video = videoMapper.selectById(contentId);
                if (video != null) {
                    items = recommendRelatedVideo(video, limit);
                }
            }
        }

        return RecommendResponse.builder()
                .items(items)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // ========== 私有方法 ==========

    /**
     * 基于内容推荐（内部实现）
     */
    private List<RecommendItem> recommendByContentInternal(Long userId, ContentType contentType,
                                                            Map<String, Double> interestTags,
                                                            List<Long> historyContentIds,
                                                            int limit) {
        try {
            // 基于标签推荐
            Map<Long, Double> tagRecommendations = contentBasedRecommender.recommendByTags(
                contentType, interestTags, limit);

            // 基于相似内容推荐
            Map<Long, Double> similarityRecommendations = contentBasedRecommender.recommendBySimilarity(
                contentType, historyContentIds, historyContentIds, limit);

            // 合并结果
            Map<Long, Double> merged = new LinkedHashMap<>(tagRecommendations);
            similarityRecommendations.forEach((id, score) -> 
                merged.merge(id, score * 0.8, Double::sum));

            return convertToRecommendItems(merged, contentType, limit);
        } catch (Exception e) {
            log.error("基于内容推荐失败: userId={}, contentType={}", userId, contentType, e);
            return Collections.emptyList();
        }
    }

    /**
     * 协同过滤推荐（内部实现）
     */
    private List<RecommendItem> recommendByCollaborativeFilteringInternal(Long userId, String contentType, 
                                                                              int limit) {
        try {
            Map<Long, Double> recommendations = collaborativeFiltering.hybridCF(userId, contentType, limit);
            return convertToRecommendItems(recommendations, ContentType.fromString(contentType), limit);
        } catch (Exception e) {
            log.error("协同过滤推荐失败: userId={}, contentType={}", userId, contentType, e);
            return Collections.emptyList();
        }
    }

    /**
     * 热门推荐（内部实现）
     */
    private List<RecommendItem> recommendHotInternal(String contentType, int limit) {
        try {
            ContentType ct = ContentType.fromString(contentType);
            Map<Long, Double> hotContentIds = switch (ct) {
                case ARTICLE -> getHotArticleIds(limit);
                case IMAGE -> getHotImageIds(limit);
                case VIDEO -> getHotVideoIds(limit);
            };

            return convertToRecommendItems(hotContentIds, ct, limit);
        } catch (Exception e) {
            log.error("热门推荐失败: contentType={}", contentType, e);
            return Collections.emptyList();
        }
    }

    /**
     * 相关文章推荐
     */
    private List<RecommendItem> recommendRelatedArticle(Article article, int limit) {
        try {
            Set<String> tags = extractTagsFromJson(article.getTags());
            Map<String, Double> tagWeights = tags.stream()
                .collect(Collectors.toMap(tag -> tag, tag -> 1.0));

            Map<Long, Double> recommendations = contentBasedRecommender.recommendByTags(
                ContentType.ARTICLE, tagWeights, limit + 1);

            // 排除当前文章
            recommendations.remove(article.getId());

            return convertToRecommendItems(recommendations, ContentType.ARTICLE, limit);
        } catch (Exception e) {
            log.error("相关文章推荐失败: articleId={}", article.getId(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 相关图片推荐
     */
    private List<RecommendItem> recommendRelatedImage(Image image, int limit) {
        try {
            Set<String> tags = extractTagsFromJson(image.getTags());
            Map<String, Double> tagWeights = tags.stream()
                .collect(Collectors.toMap(tag -> tag, tag -> 1.0));

            Map<Long, Double> recommendations = contentBasedRecommender.recommendByTags(
                ContentType.IMAGE, tagWeights, limit + 1);

            recommendations.remove(image.getId());

            return convertToRecommendItems(recommendations, ContentType.IMAGE, limit);
        } catch (Exception e) {
            log.error("相关图片推荐失败: imageId={}", image.getId(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 相关视频推荐
     */
    private List<RecommendItem> recommendRelatedVideo(Video video, int limit) {
        try {
            Set<String> tags = extractTagsFromJson(video.getTags());
            Map<String, Double> tagWeights = tags.stream()
                .collect(Collectors.toMap(tag -> tag, tag -> 1.0));

            Map<Long, Double> recommendations = contentBasedRecommender.recommendByTags(
                ContentType.VIDEO, tagWeights, limit + 1);

            recommendations.remove(video.getId());

            return convertToRecommendItems(recommendations, ContentType.VIDEO, limit);
        } catch (Exception e) {
            log.error("相关视频推荐失败: videoId={}", video.getId(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 转换为推荐项
     */
    private List<RecommendItem> convertToRecommendItems(Map<Long, Double> contentIds, 
                                                          ContentType contentType, int limit) {
        List<RecommendItem> items = new ArrayList<>();

        if (contentIds.isEmpty()) {
            return items;
        }

        List<Long> ids = contentIds.keySet().stream()
            .limit(limit)
            .collect(Collectors.toList());

        switch (contentType) {
            case ARTICLE -> {
                List<Article> articles = articleMapper.selectBatchIds(ids);
                items = articles.stream()
                    .map(article -> RecommendItem.builder()
                        .contentId(article.getId())
                        .contentType("article")
                        .title(article.getTitle())
                        .coverImage(article.getCoverImage())
                        .score(contentIds.getOrDefault(article.getId(), 0.0))
                        .publishTime(article.getPublishTime())
                        .viewCount(article.getViewCount())
                        .build())
                    .sorted(Comparator.comparing(RecommendItem::getScore).reversed())
                    .collect(Collectors.toList());
            }
            case IMAGE -> {
                List<Image> images = imageMapper.selectBatchIds(ids);
                items = images.stream()
                    .map(image -> RecommendItem.builder()
                        .contentId(image.getId())
                        .contentType("image")
                        .title(image.getTitle())
                        .coverImage(image.getUrl())
                        .score(contentIds.getOrDefault(image.getId(), 0.0))
                        .publishTime(image.getPublishTime())
                        .viewCount(image.getViewCount())
                        .build())
                    .sorted(Comparator.comparing(RecommendItem::getScore).reversed())
                    .collect(Collectors.toList());
            }
            case VIDEO -> {
                List<Video> videos = videoMapper.selectBatchIds(ids);
                items = videos.stream()
                    .map(video -> RecommendItem.builder()
                        .contentId(video.getId())
                        .contentType("video")
                        .title(video.getTitle())
                        .coverImage(video.getCoverUrl())
                        .score(contentIds.getOrDefault(video.getId(), 0.0))
                        .publishTime(video.getPublishTime())
                        .viewCount(video.getViewCount())
                        .duration(video.getDuration())
                        .build())
                    .sorted(Comparator.comparing(RecommendItem::getScore).reversed())
                    .collect(Collectors.toList());
            }
        }

        return items;
    }

    /**
     * 去重并排序
     */
    private List<RecommendItem> deduplicateAndRank(List<RecommendItem> items, int limit) {
        Map<Long, RecommendItem> uniqueItems = new LinkedHashMap<>();

        for (RecommendItem item : items) {
            Long key = item.getContentId();
            if (uniqueItems.containsKey(key)) {
                // 合并得分
                RecommendItem existing = uniqueItems.get(key);
                existing.setScore(Math.max(existing.getScore(), item.getScore()));
            } else {
                uniqueItems.put(key, item);
            }
        }

        return uniqueItems.values().stream()
            .sorted(Comparator.comparing(RecommendItem::getScore).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * 提取用户兴趣标签
     */
    private Map<String, Double> extractInterestTags(UserProfile userProfile) {
        try {
            if (userProfile == null || userProfile.getInterestTags() == null) {
                return new HashMap<>();
            }
            return objectMapper.convertValue(userProfile.getInterestTags(), 
                new TypeReference<Map<String, Double>>() {});
        } catch (Exception e) {
            log.error("提取用户兴趣标签失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 从JSON提取标签
     */
    private Set<String> extractTagsFromJson(Object tagsJson) {
        try {
            if (tagsJson == null) {
                return new HashSet<>();
            }
            String jsonStr = objectMapper.writeValueAsString(tagsJson);
            return objectMapper.readValue(jsonStr, new TypeReference<List<String>>() {})
                .stream()
                .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("解析标签失败", e);
            return new HashSet<>();
        }
    }

    /**
     * 获取热门文章ID
     */
    private Map<Long, Double> getHotArticleIds(int limit) {
        List<Article> articles = articleMapper.selectHotArticles(7, limit);
        return articles.stream()
            .collect(Collectors.toMap(
                Article::getId,
                article -> (double) article.getViewCount(),
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    /**
     * 获取热门图片ID
     */
    private Map<Long, Double> getHotImageIds(int limit) {
        List<Image> images = imageMapper.selectHotImages(7, limit);
        return images.stream()
            .collect(Collectors.toMap(
                Image::getId,
                image -> (double) image.getViewCount(),
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    /**
     * 获取热门视频ID
     */
    private Map<Long, Double> getHotVideoIds(int limit) {
        List<Video> videos = videoMapper.selectHotVideos(7, limit);
        return videos.stream()
            .collect(Collectors.toMap(
                Video::getId,
                video -> (double) video.getViewCount(),
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
}
