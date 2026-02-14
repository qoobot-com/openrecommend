package com.qoobot.openrecommend.algorithm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoobot.openrecommend.common.enums.ContentType;
import com.qoobot.openrecommend.entity.Article;
import com.qoobot.openrecommend.entity.Image;
import com.qoobot.openrecommend.entity.Video;
import com.qoobot.openrecommend.mapper.ArticleMapper;
import com.qoobot.openrecommend.mapper.ImageMapper;
import com.qoobot.openrecommend.mapper.VideoMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于内容的推荐算法
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
@Component
public class ContentBasedRecommender {

    private static final Logger log = LoggerFactory.getLogger(ContentBasedRecommender.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private ImageMapper imageMapper;

    @Resource
    private VideoMapper videoMapper;

    /**
     * 基于标签推荐内容
     * 
     * @param contentType 内容类型
     * @param tags 标签及权重
     * @param limit 推荐数量
     * @return 推荐结果（内容ID -> 相似度）
     */
    public Map<Long, Double> recommendByTags(ContentType contentType, Map<String, Double> tags, int limit) {
        log.info("基于标签推荐: contentType={}, tags={}", contentType, tags);

        if (tags == null || tags.isEmpty()) {
            return new HashMap<>();
        }

        Set<String> tagSet = tags.keySet();

        return switch (contentType) {
            case ARTICLE -> recommendArticlesByTags(tagSet, limit);
            case IMAGE -> recommendImagesByTags(tagSet, limit);
            case VIDEO -> recommendVideosByTags(tagSet, limit);
            default -> new HashMap<>();
        };
    }

    /**
     * 基于相似内容推荐（基于已查看内容的相似性）
     * 
     * @param contentType 内容类型
     * @param viewedContentIds 已查看的内容ID列表
     * @param excludeIds 排除的内容ID列表
     * @param limit 推荐数量
     * @return 推荐结果（内容ID -> 相似度）
     */
    public Map<Long, Double> recommendBySimilarity(ContentType contentType, 
                                                     List<Long> viewedContentIds,
                                                     List<Long> excludeIds, 
                                                     int limit) {
        log.info("基于相似内容推荐: contentType={}, viewedCount={}", contentType, viewedContentIds.size());

        if (viewedContentIds == null || viewedContentIds.isEmpty()) {
            return new HashMap<>();
        }

        // 合并排除列表
        Set<Long> excludeSet = new HashSet<>();
        if (excludeIds != null) {
            excludeSet.addAll(excludeIds);
        }
        excludeSet.addAll(viewedContentIds);

        return switch (contentType) {
            case ARTICLE -> recommendSimilarArticles(viewedContentIds, excludeSet, limit);
            case IMAGE -> recommendSimilarImages(viewedContentIds, excludeSet, limit);
            case VIDEO -> recommendSimilarVideos(viewedContentIds, excludeSet, limit);
            default -> new HashMap<>();
        };
    }

    /**
     * 基于分类推荐
     * 
     * @param contentType 内容类型
     * @param categoryIds 分类ID列表
     * @param limit 推荐数量
     * @return 推荐结果（内容ID -> 相似度）
     */
    public Map<Long, Double> recommendByCategory(ContentType contentType, 
                                                   List<Long> categoryIds,
                                                   int limit) {
        log.info("基于分类推荐: contentType={}, categories={}", contentType, categoryIds);

        if (categoryIds == null || categoryIds.isEmpty()) {
            return new HashMap<>();
        }

        Map<Long, Double> result = new LinkedHashMap<>();

        for (Long categoryId : categoryIds) {
            List<? extends Object> contents = switch (contentType) {
                case ARTICLE -> articleMapper.selectByCategory(categoryId, limit);
                case IMAGE -> imageMapper.selectByCategory(categoryId, limit);
                case VIDEO -> videoMapper.selectByCategory(categoryId, limit);
                default -> Collections.emptyList();
            };

            contents.forEach(content -> {
                long contentId = getContentId(content);
                result.putIfAbsent(contentId, 1.0); // 分类推荐默认相似度为1.0
            });

            if (result.size() >= limit) {
                break;
            }
        }

        return result.entrySet().stream()
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
     * 基于标签推荐文章
     */
    private Map<Long, Double> recommendArticlesByTags(Set<String> tags, int limit) {
        List<Article> articles = articleMapper.selectByTags(tags, limit * 3);
        return calculateArticleScores(articles, tags).entrySet().stream()
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
     * 基于标签推荐图片
     */
    private Map<Long, Double> recommendImagesByTags(Set<String> tags, int limit) {
        List<Image> images = imageMapper.selectByTags(tags, limit * 3);
        return calculateImageScores(images, tags).entrySet().stream()
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
     * 基于标签推荐视频
     */
    private Map<Long, Double> recommendVideosByTags(Set<String> tags, int limit) {
        List<Video> videos = videoMapper.selectByTags(tags, limit * 3);
        return calculateVideoScores(videos, tags).entrySet().stream()
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
     * 推荐相似文章
     */
    private Map<Long, Double> recommendSimilarArticles(List<Long> viewedIds, Set<Long> excludeIds, int limit) {
        // 获取已查看文章
        List<Article> viewedArticles = articleMapper.selectBatchIds(viewedIds);
        
        // 提取已查看文章的特征（标签）
        Map<String, Double> userPreference = extractArticleTags(viewedArticles);
        
        // 查询候选文章（排除已查看的）
        List<Article> candidates = articleMapper.selectNotInIds(new ArrayList<>(excludeIds), limit * 5);
        
        // 计算相似度
        Map<Long, Double> result = new LinkedHashMap<>();
        for (Article article : candidates) {
            Map<String, Double> articleTags = extractArticleTags(List.of(article));
            double similarity = SimilarityCalculator.cosineSimilarity(userPreference, articleTags);
            
            // 融合质量分数和热度
            double qualityScore = article.getQualityScore() != null ? article.getQualityScore() : 0;
            double popularityScore = Math.log(article.getViewCount() + 1) / 10;
            double finalScore = 0.6 * similarity + 0.3 * qualityScore + 0.1 * popularityScore;
            
            result.put(article.getId(), finalScore);
        }

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
     * 推荐相似图片
     */
    private Map<Long, Double> recommendSimilarImages(List<Long> viewedIds, Set<Long> excludeIds, int limit) {
        List<Image> viewedImages = imageMapper.selectBatchIds(viewedIds);
        Map<String, Double> userPreference = extractImageTags(viewedImages);
        
        // 查询候选图片
        List<Image> candidates = imageMapper.selectList(null).stream()
            .filter(img -> !excludeIds.contains(img.getId()))
            .limit(limit * 5)
            .collect(Collectors.toList());
        
        Map<Long, Double> result = new LinkedHashMap<>();
        for (Image image : candidates) {
            Map<String, Double> imageTags = extractImageTags(List.of(image));
            double similarity = SimilarityCalculator.cosineSimilarity(userPreference, imageTags);
            result.put(image.getId(), similarity);
        }

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
     * 推荐相似视频
     */
    private Map<Long, Double> recommendSimilarVideos(List<Long> viewedIds, Set<Long> excludeIds, int limit) {
        List<Video> viewedVideos = videoMapper.selectBatchIds(viewedIds);
        Map<String, Double> userPreference = extractVideoTags(viewedVideos);
        
        // 查询候选视频
        List<Video> candidates = videoMapper.selectList(null).stream()
            .filter(video -> !excludeIds.contains(video.getId()))
            .limit(limit * 5)
            .collect(Collectors.toList());
        
        Map<Long, Double> result = new LinkedHashMap<>();
        for (Video video : candidates) {
            Map<String, Double> videoTags = extractVideoTags(List.of(video));
            double similarity = SimilarityCalculator.cosineSimilarity(userPreference, videoTags);
            result.put(video.getId(), similarity);
        }

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
     * 计算文章得分
     */
    private Map<Long, Double> calculateArticleScores(List<Article> articles, Set<String> userTags) {
        Map<Long, Double> result = new LinkedHashMap<>();
        
        for (Article article : articles) {
            Set<String> articleTags = extractTagsFromJson(article.getTags());
            double matchScore = SimilarityCalculator.jaccardSimilarity(userTags, articleTags);
            
            // 综合得分
            double qualityScore = article.getQualityScore() != null ? article.getQualityScore() : 0;
            double finalScore = 0.7 * matchScore + 0.3 * qualityScore;
            
            result.put(article.getId(), finalScore);
        }
        
        return result;
    }

    /**
     * 计算图片得分
     */
    private Map<Long, Double> calculateImageScores(List<Image> images, Set<String> userTags) {
        Map<Long, Double> result = new LinkedHashMap<>();
        
        for (Image image : images) {
            Set<String> imageTags = extractTagsFromJson(image.getTags());
            double matchScore = SimilarityCalculator.jaccardSimilarity(userTags, imageTags);
            result.put(image.getId(), matchScore);
        }
        
        return result;
    }

    /**
     * 计算视频得分
     */
    private Map<Long, Double> calculateVideoScores(List<Video> videos, Set<String> userTags) {
        Map<Long, Double> result = new LinkedHashMap<>();
        
        for (Video video : videos) {
            Set<String> videoTags = extractTagsFromJson(video.getTags());
            double matchScore = SimilarityCalculator.jaccardSimilarity(userTags, videoTags);
            result.put(video.getId(), matchScore);
        }
        
        return result;
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
     * 提取文章标签（聚合）
     */
    private Map<String, Double> extractArticleTags(List<Article> articles) {
        Map<String, Integer> tagCount = new HashMap<>();
        
        for (Article article : articles) {
            Set<String> tags = extractTagsFromJson(article.getTags());
            tags.forEach(tag -> tagCount.merge(tag, 1, Integer::sum));
        }
        
        // 转换为权重
        int total = tagCount.values().stream().mapToInt(Integer::sum).sum();
        return tagCount.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> (double) e.getValue() / total,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    /**
     * 提取图片标签（聚合）
     */
    private Map<String, Double> extractImageTags(List<Image> images) {
        Map<String, Integer> tagCount = new HashMap<>();
        
        for (Image image : images) {
            Set<String> tags = extractTagsFromJson(image.getTags());
            tags.forEach(tag -> tagCount.merge(tag, 1, Integer::sum));
        }
        
        int total = tagCount.values().stream().mapToInt(Integer::sum).sum();
        return tagCount.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> (double) e.getValue() / total,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    /**
     * 提取视频标签（聚合）
     */
    private Map<String, Double> extractVideoTags(List<Video> videos) {
        Map<String, Integer> tagCount = new HashMap<>();
        
        for (Video video : videos) {
            Set<String> tags = extractTagsFromJson(video.getTags());
            tags.forEach(tag -> tagCount.merge(tag, 1, Integer::sum));
        }
        
        int total = tagCount.values().stream().mapToInt(Integer::sum).sum();
        return tagCount.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> (double) e.getValue() / total,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    /**
     * 获取内容ID
     */
    private long getContentId(Object content) {
        if (content instanceof Article) {
            return ((Article) content).getId();
        } else if (content instanceof Image) {
            return ((Image) content).getId();
        } else if (content instanceof Video) {
            return ((Video) content).getId();
        }
        return 0L;
    }
}
