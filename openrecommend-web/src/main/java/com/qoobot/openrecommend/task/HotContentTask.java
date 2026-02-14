package com.qoobot.openrecommend.task;

import com.qoobot.openrecommend.entity.Article;
import com.qoobot.openrecommend.entity.Image;
import com.qoobot.openrecommend.entity.Video;
import com.qoobot.openrecommend.mapper.ArticleMapper;
import com.qoobot.openrecommend.mapper.ImageMapper;
import com.qoobot.openrecommend.mapper.VideoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 热门内容计算定时任务
 * 定期计算热门内容并更新缓存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HotContentTask {

    private final ArticleMapper articleMapper;
    private final ImageMapper imageMapper;
    private final VideoMapper videoMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String HOT_ARTICLES_KEY = "recommend:hot:articles";
    private static final String HOT_IMAGES_KEY = "recommend:hot:images";
    private static final String HOT_VIDEOS_KEY = "recommend:hot:videos";
    private static final int HOT_CONTENT_SIZE = 100;

    /**
     * 计算热门文章
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void calculateHotArticles() {
        log.info("开始计算热门文章...");

        try {
            List<Article> articles = articleMapper.selectHotArticles(HOT_CONTENT_SIZE);

            if (articles != null && !articles.isEmpty()) {
                // 计算热度分数
                for (Article article : articles) {
                    double hotScore = calculateArticleHotScore(article);
                    article.setQualityScore(hotScore);
                }

                // 按热度分数排序
                articles.sort((a, b) -> Double.compare(b.getQualityScore(), a.getQualityScore()));

                // 存入Redis
                redisTemplate.opsForValue().set(HOT_ARTICLES_KEY, articles, 2, TimeUnit.HOURS);

                log.info("热门文章计算完成，数量: {}", articles.size());
            } else {
                log.warn("未找到热门文章");
            }
        } catch (Exception e) {
            log.error("计算热门文章失败", e);
        }
    }

    /**
     * 计算热门图片
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void calculateHotImages() {
        log.info("开始计算热门图片...");

        try {
            List<Image> images = imageMapper.selectHotImages(HOT_CONTENT_SIZE);

            if (images != null && !images.isEmpty()) {
                // 计算热度分数
                for (Image image : images) {
                    double hotScore = calculateImageHotScore(image);
                    image.setQualityScore(hotScore);
                }

                // 按热度分数排序
                images.sort((a, b) -> Double.compare(b.getQualityScore(), a.getQualityScore()));

                // 存入Redis
                redisTemplate.opsForValue().set(HOT_IMAGES_KEY, images, 2, TimeUnit.HOURS);

                log.info("热门图片计算完成，数量: {}", images.size());
            } else {
                log.warn("未找到热门图片");
            }
        } catch (Exception e) {
            log.error("计算热门图片失败", e);
        }
    }

    /**
     * 计算热门视频
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void calculateHotVideos() {
        log.info("开始计算热门视频...");

        try {
            List<Video> videos = videoMapper.selectHotVideos(HOT_CONTENT_SIZE);

            if (videos != null && !videos.isEmpty()) {
                // 计算热度分数
                for (Video video : videos) {
                    double hotScore = calculateVideoHotScore(video);
                    video.setQualityScore(hotScore);
                }

                // 按热度分数排序
                videos.sort((a, b) -> Double.compare(b.getQualityScore(), a.getQualityScore()));

                // 存入Redis
                redisTemplate.opsForValue().set(HOT_VIDEOS_KEY, videos, 2, TimeUnit.HOURS);

                log.info("热门视频计算完成，数量: {}", videos.size());
            } else {
                log.warn("未找到热门视频");
            }
        } catch (Exception e) {
            log.error("计算热门视频失败", e);
        }
    }

    /**
     * 统一计算所有热门内容
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void calculateAllHotContent() {
        log.info("开始统一计算所有热门内容...");
        calculateHotArticles();
        calculateHotImages();
        calculateHotVideos();
        log.info("所有热门内容计算完成");
    }

    /**
     * 手动触发热门内容计算
     */
    public void triggerHotContentCalculation() {
        log.info("手动触发热门内容计算...");
        calculateAllHotContent();
    }

    /**
     * 从缓存获取热门文章
     */
    @SuppressWarnings("unchecked")
    public List<Article> getHotArticles() {
        try {
            Object cached = redisTemplate.opsForValue().get(HOT_ARTICLES_KEY);
            if (cached != null) {
                return (List<Article>) cached;
            }
        } catch (Exception e) {
            log.error("获取热门文章缓存失败", e);
        }

        // 缓存未命中，从数据库查询
        return articleMapper.selectHotArticles(HOT_CONTENT_SIZE);
    }

    /**
     * 从缓存获取热门图片
     */
    @SuppressWarnings("unchecked")
    public List<Image> getHotImages() {
        try {
            Object cached = redisTemplate.opsForValue().get(HOT_IMAGES_KEY);
            if (cached != null) {
                return (List<Image>) cached;
            }
        } catch (Exception e) {
            log.error("获取热门图片缓存失败", e);
        }

        // 缓存未命中，从数据库查询
        return imageMapper.selectHotImages(HOT_CONTENT_SIZE);
    }

    /**
     * 从缓存获取热门视频
     */
    @SuppressWarnings("unchecked")
    public List<Video> getHotVideos() {
        try {
            Object cached = redisTemplate.opsForValue().get(HOT_VIDEOS_KEY);
            if (cached != null) {
                return (List<Video>) cached;
            }
        } catch (Exception e) {
            log.error("获取热门视频缓存失败", e);
        }

        // 缓存未命中，从数据库查询
        return videoMapper.selectHotVideos(HOT_CONTENT_SIZE);
    }

    /**
     * 计算文章热度分数
     */
    private double calculateArticleHotScore(Article article) {
        long viewCount = article.getViewCount() != null ? article.getViewCount() : 0;
        long likeCount = article.getLikeCount() != null ? article.getLikeCount() : 0;
        long commentCount = article.getCommentCount() != null ? article.getCommentCount() : 0;
        long collectCount = article.getCollectCount() != null ? article.getCollectCount() : 0;
        long shareCount = article.getShareCount() != null ? article.getShareCount() : 0;

        // 基础热度：浏览量 * 1 + 点赞数 * 5 + 评论数 * 3 + 收藏数 * 4 + 分享数 * 5
        double baseHotScore = viewCount * 1.0 + likeCount * 5.0 +
                commentCount * 3.0 + collectCount * 4.0 + shareCount * 5.0;

        // 时间衰减：越新的内容热度越高
        LocalDateTime publishTime = article.getPublishTime();
        if (publishTime != null) {
            long hoursDiff = ChronoUnit.HOURS.between(publishTime, LocalDateTime.now());
            double timeDecay = Math.exp(-hoursDiff / 168.0); // 168小时 = 7天
            baseHotScore *= timeDecay;
        }

        return baseHotScore;
    }

    /**
     * 计算图片热度分数
     */
    private double calculateImageHotScore(Image image) {
        long viewCount = image.getViewCount() != null ? image.getViewCount() : 0;
        long likeCount = image.getLikeCount() != null ? image.getLikeCount() : 0;
        long collectCount = image.getCollectCount() != null ? image.getCollectCount() : 0;

        // 基础热度：浏览量 * 1 + 点赞数 * 5 + 收藏数 * 4
        double baseHotScore = viewCount * 1.0 + likeCount * 5.0 + collectCount * 4.0;

        // 时间衰减
        LocalDateTime publishTime = image.getPublishTime();
        if (publishTime != null) {
            long hoursDiff = ChronoUnit.HOURS.between(publishTime, LocalDateTime.now());
            double timeDecay = Math.exp(-hoursDiff / 168.0);
            baseHotScore *= timeDecay;
        }

        return baseHotScore;
    }

    /**
     * 计算视频热度分数
     */
    private double calculateVideoHotScore(Video video) {
        long viewCount = video.getViewCount() != null ? video.getViewCount() : 0;
        long likeCount = video.getLikeCount() != null ? video.getLikeCount() : 0;
        long commentCount = video.getCommentCount() != null ? video.getCommentCount() : 0;
        long collectCount = video.getCollectCount() != null ? video.getCollectCount() : 0;
        long shareCount = video.getShareCount() != null ? video.getShareCount() : 0;

        // 基础热度：播放量 * 1 + 点赞数 * 5 + 评论数 * 3 + 收藏数 * 4 + 分享数 * 5
        double baseHotScore = viewCount * 1.0 + likeCount * 5.0 +
                commentCount * 3.0 + collectCount * 4.0 + shareCount * 5.0;

        // 时间衰减
        LocalDateTime publishTime = video.getPublishTime();
        if (publishTime != null) {
            long hoursDiff = ChronoUnit.HOURS.between(publishTime, LocalDateTime.now());
            double timeDecay = Math.exp(-hoursDiff / 168.0);
            baseHotScore *= timeDecay;
        }

        return baseHotScore;
    }

    /**
     * 更新热门内容缓存（手动触发）
     */
    public void updateHotContentCache() {
        log.info("手动更新热门内容缓存...");
        triggerHotContentCalculation();
    }

    /**
     * 获取所有热门内容的统计信息
     */
    public Map<String, Object> getHotContentStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<Article> hotArticles = getHotArticles();
        List<Image> hotImages = getHotImages();
        List<Video> hotVideos = getHotVideos();

        stats.put("hotArticleCount", hotArticles != null ? hotArticles.size() : 0);
        stats.put("hotImageCount", hotImages != null ? hotImages.size() : 0);
        stats.put("hotVideoCount", hotVideos != null ? hotVideos.size() : 0);
        stats.put("lastUpdateTime", LocalDateTime.now());

        return stats;
    }
}
