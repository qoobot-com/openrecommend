package com.qoobot.openrecommend.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qoobot.openrecommend.api.dto.RecommendItem;
import com.qoobot.openrecommend.common.result.Result;
import com.qoobot.openrecommend.entity.Article;
import com.qoobot.openrecommend.entity.Image;
import com.qoobot.openrecommend.entity.Video;
import com.qoobot.openrecommend.service.ArticleService;
import com.qoobot.openrecommend.service.ImageService;
import com.qoobot.openrecommend.service.VideoService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 内容管理控制器
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Resource
    private ArticleService articleService;

    @Resource
    private ImageService imageService;

    @Resource
    private VideoService videoService;

    // ========== 文章相关 ==========

    /**
     * 发布文章
     */
    @PostMapping("/article")
    public Result<Long> publishArticle(@Valid @RequestBody Article article) {
        log.info("发布文章: title={}", article.getTitle());
        Long articleId = articleService.publish(article);
        return Result.success(articleId);
    }

    /**
     * 更新文章
     */
    @PutMapping("/article/{id}")
    public Result<Void> updateArticle(@PathVariable Long id, @Valid @RequestBody Article article) {
        log.info("更新文章: id={}", id);
        article.setId(id);
        boolean success = articleService.update(article);
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/article/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        log.info("删除文章: id={}", id);
        boolean success = articleService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/article/{id}")
    public Result<Article> getArticleDetail(@PathVariable Long id) {
        log.debug("获取文章详情: id={}", id);
        Article article = articleService.getDetail(id);
        return article != null ? Result.success(article) : Result.error("文章不存在");
    }

    /**
     * 文章列表
     */
    @GetMapping("/article/list")
    public Result<IPage<Article>> listArticles(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("查询文章列表: pageNum={}, pageSize={}", pageNum, pageSize);
        IPage<Article> page = articleService.list(pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 按分类查询文章
     */
    @GetMapping("/article/category/{categoryId}")
    public Result<IPage<Article>> listArticlesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("按分类查询文章: categoryId={}", categoryId);
        IPage<Article> page = articleService.listByCategory(categoryId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 搜索文章
     */
    @GetMapping("/article/search")
    public Result<IPage<Article>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("搜索文章: keyword={}", keyword);
        IPage<Article> page = articleService.search(keyword, pageNum, pageSize);
        return Result.success(page);
    }

    // ========== 图片相关 ==========

    /**
     * 上传图片
     */
    @PostMapping("/image")
    public Result<Long> uploadImage(@Valid @RequestBody Image image) {
        log.info("上传图片: title={}", image.getTitle());
        Long imageId = imageService.upload(image);
        return Result.success(imageId);
    }

    /**
     * 更新图片
     */
    @PutMapping("/image/{id}")
    public Result<Void> updateImage(@PathVariable Long id, @Valid @RequestBody Image image) {
        log.info("更新图片: id={}", id);
        image.setId(id);
        boolean success = imageService.update(image);
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 删除图片
     */
    @DeleteMapping("/image/{id}")
    public Result<Void> deleteImage(@PathVariable Long id) {
        log.info("删除图片: id={}", id);
        boolean success = imageService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    /**
     * 获取图片详情
     */
    @GetMapping("/image/{id}")
    public Result<Image> getImageDetail(@PathVariable Long id) {
        log.debug("获取图片详情: id={}", id);
        Image image = imageService.getDetail(id);
        return image != null ? Result.success(image) : Result.error("图片不存在");
    }

    /**
     * 图片列表
     */
    @GetMapping("/image/list")
    public Result<IPage<Image>> listImages(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("查询图片列表: pageNum={}, pageSize={}", pageNum, pageSize);
        IPage<Image> page = imageService.list(pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 按分类查询图片
     */
    @GetMapping("/image/category/{categoryId}")
    public Result<IPage<Image>> listImagesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("按分类查询图片: categoryId={}", categoryId);
        IPage<Image> page = imageService.listByCategory(categoryId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 搜索图片
     */
    @GetMapping("/image/search")
    public Result<IPage<Image>> searchImages(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("搜索图片: keyword={}", keyword);
        IPage<Image> page = imageService.search(keyword, pageNum, pageSize);
        return Result.success(page);
    }

    // ========== 视频相关 ==========

    /**
     * 上传视频
     */
    @PostMapping("/video")
    public Result<Long> uploadVideo(@Valid @RequestBody Video video) {
        log.info("上传视频: title={}", video.getTitle());
        Long videoId = videoService.upload(video);
        return Result.success(videoId);
    }

    /**
     * 更新视频
     */
    @PutMapping("/video/{id}")
    public Result<Void> updateVideo(@PathVariable Long id, @Valid @RequestBody Video video) {
        log.info("更新视频: id={}", id);
        video.setId(id);
        boolean success = videoService.update(video);
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 删除视频
     */
    @DeleteMapping("/video/{id}")
    public Result<Void> deleteVideo(@PathVariable Long id) {
        log.info("删除视频: id={}", id);
        boolean success = videoService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    /**
     * 获取视频详情
     */
    @GetMapping("/video/{id}")
    public Result<Video> getVideoDetail(@PathVariable Long id) {
        log.debug("获取视频详情: id={}", id);
        Video video = videoService.getDetail(id);
        return video != null ? Result.success(video) : Result.error("视频不存在");
    }

    /**
     * 视频列表
     */
    @GetMapping("/video/list")
    public Result<IPage<Video>> listVideos(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("查询视频列表: pageNum={}, pageSize={}", pageNum, pageSize);
        IPage<Video> page = videoService.list(pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 按分类查询视频
     */
    @GetMapping("/video/category/{categoryId}")
    public Result<IPage<Video>> listVideosByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("按分类查询视频: categoryId={}", categoryId);
        IPage<Video> page = videoService.listByCategory(categoryId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 搜索视频
     */
    @GetMapping("/video/search")
    public Result<IPage<Video>> searchVideos(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("搜索视频: keyword={}", keyword);
        IPage<Video> page = videoService.search(keyword, pageNum, pageSize);
        return Result.success(page);
    }
}
