package com.qoobot.openrecommend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openrecommend.common.enums.BehaviorType;
import com.qoobot.openrecommend.entity.Article;
import com.qoobot.openrecommend.entity.UserBehavior;
import com.qoobot.openrecommend.mapper.ArticleMapper;
import com.qoobot.openrecommend.service.ArticleService;
import com.qoobot.openrecommend.service.UserBehaviorService;
import com.qoobot.openrecommend.service.UserProfileService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * 文章服务实现
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    private static final Logger log = LoggerFactory.getLogger(ArticleServiceImpl.class);

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private UserProfileService userProfileService;

    @Resource
    private UserBehaviorService userBehaviorService;

    private final Random random = new Random();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(Article article) {
        log.info("发布文章: title={}, authorId={}", article.getTitle(), article.getAuthorId());

        // 设置初始状态
        article.setPublishTime(LocalDateTime.now());
        article.setViewCount(0L);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setCollectCount(0);
        article.setShareCount(0);
        article.setFinishReadCount(0);
        
        // 计算质量分数（基于标题长度、摘要、内容长度）
        double qualityScore = calculateQualityScore(article);
        article.setQualityScore(qualityScore);

        // 保存文章
        articleMapper.insert(article);

        log.info("文章发布成功: id={}, qualityScore={}", article.getId(), qualityScore);

        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Article article) {
        log.info("更新文章: id={}", article.getId());

        // 重新计算质量分数
        if (StringUtils.hasText(article.getContent())) {
            double qualityScore = calculateQualityScore(article);
            article.setQualityScore(qualityScore);
        }

        int result = articleMapper.updateById(article);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long articleId) {
        log.info("删除文章: id={}", articleId);

        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            log.warn("文章不存在: id={}", articleId);
            return false;
        }

        // 逻辑删除
        article.setIsDeleted(1);
        int result = articleMapper.updateById(article);

        return result > 0;
    }

    @Override
    public Article getDetail(Long articleId) {
        log.debug("获取文章详情: id={}", articleId);

        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            log.warn("文章不存在: id={}", articleId);
            return null;
        }

        // 如果是已发布文章，增加浏览量
        if (article.getStatus() == 1) {
            incrementViewCount(articleId);
        }

        return article;
    }

    @Override
    public IPage<Article> list(int pageNum, int pageSize) {
        log.debug("查询文章列表: pageNum={}, pageSize={}", pageNum, pageSize);

        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
            .eq(Article::getStatus, 1)
            .eq(Article::getIsDeleted, 0)
            .orderByDesc(Article::getQualityScore)
            .orderByDesc(Article::getPublishTime);

        return articleMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Article> listByCategory(Long categoryId, int pageNum, int pageSize) {
        log.debug("按分类查询文章: categoryId={}, pageNum={}, pageSize={}", categoryId, pageNum, pageSize);

        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
            .eq(Article::getStatus, 1)
            .eq(Article::getIsDeleted, 0)
            .eq(Article::getCategoryId, categoryId)
            .orderByDesc(Article::getQualityScore)
            .orderByDesc(Article::getPublishTime);

        return articleMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Article> listByAuthor(Long authorId, int pageNum, int pageSize) {
        log.debug("按作者查询文章: authorId={}, pageNum={}, pageSize={}", authorId, pageNum, pageSize);

        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
            .eq(Article::getStatus, 1)
            .eq(Article::getIsDeleted, 0)
            .eq(Article::getAuthorId, authorId)
            .orderByDesc(Article::getPublishTime);

        return articleMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Article> search(String keyword, int pageNum, int pageSize) {
        log.debug("搜索文章: keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);

        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
            .eq(Article::getStatus, 1)
            .eq(Article::getIsDeleted, 0)
            .and(w -> w.like(Article::getTitle, keyword)
                .or()
                .like(Article::getSummary, keyword))
            .orderByDesc(Article::getQualityScore);

        return articleMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Article> getByIds(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return List.of();
        }
        return articleMapper.selectBatchIds(articleIds);
    }

    // ========== 私有方法 ==========

    /**
     * 计算文章质量分数
     */
    private double calculateQualityScore(Article article) {
        double score = 50.0; // 基础分

        // 标题质量（0-20分）
        if (article.getTitle() != null) {
            int titleLength = article.getTitle().length();
            if (titleLength >= 10 && titleLength <= 50) {
                score += 20.0;
            } else if (titleLength >= 5) {
                score += 10.0;
            }
        }

        // 摘要质量（0-15分）
        if (article.getSummary() != null && !article.getSummary().isEmpty()) {
            score += 15.0;
        }

        // 内容质量（0-15分）
        if (article.getContent() != null) {
            int contentLength = article.getContent().length();
            if (contentLength >= 500) {
                score += 15.0;
            } else if (contentLength >= 200) {
                score += 10.0;
            } else if (contentLength >= 100) {
                score += 5.0;
            }
        }

        // 原创性（0-10分）
        if (article.getIsOriginal() != null && article.getIsOriginal() == 1) {
            score += 10.0;
        }

        // 封面图（0-10分）
        if (article.getCoverImage() != null && !article.getCoverImage().isEmpty()) {
            score += 10.0;
        }

        // 限制在0-100之间
        return Math.max(0.0, Math.min(100.0, score));
    }

    /**
     * 增加浏览量（异步）
     */
    @Async("taskExecutor")
    protected void incrementViewCount(Long articleId) {
        try {
            Article article = articleMapper.selectById(articleId);
            if (article != null) {
                article.setViewCount(article.getViewCount() + 1);
                articleMapper.updateById(article);
            }
        } catch (Exception e) {
            log.error("增加浏览量失败: articleId={}", articleId, e);
        }
    }
}
