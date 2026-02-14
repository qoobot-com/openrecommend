package com.qoobot.openrecommend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openrecommend.entity.Image;
import com.qoobot.openrecommend.mapper.ImageMapper;
import com.qoobot.openrecommend.service.ImageService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 图片服务实现
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
@Service
public class ImageServiceImpl implements ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageServiceImpl.class);

    @Resource
    private ImageMapper imageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upload(Image image) {
        log.info("上传图片: title={}, uploaderId={}", image.getTitle(), image.getUploaderId());

        // 设置初始状态
        image.setPublishTime(LocalDateTime.now());
        image.setViewCount(0L);
        image.setLikeCount(0);
        image.setCollectCount(0);
        image.setDownloadCount(0);

        // 计算质量分数
        double qualityScore = calculateQualityScore(image);
        image.setQualityScore(qualityScore);

        // 保存图片
        imageMapper.insert(image);

        log.info("图片上传成功: id={}, qualityScore={}", image.getId(), qualityScore);

        return image.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Image image) {
        log.info("更新图片: id={}", image.getId());

        int result = imageMapper.updateById(image);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long imageId) {
        log.info("删除图片: id={}", imageId);

        Image image = imageMapper.selectById(imageId);
        if (image == null) {
            log.warn("图片不存在: id={}", imageId);
            return false;
        }

        image.setIsDeleted(1);
        int result = imageMapper.updateById(image);

        return result > 0;
    }

    @Override
    public Image getDetail(Long imageId) {
        log.debug("获取图片详情: id={}", imageId);

        Image image = imageMapper.selectById(imageId);
        if (image == null) {
            log.warn("图片不存在: id={}", imageId);
            return null;
        }

        // 增加浏览量
        if (image.getStatus() == 1) {
            incrementViewCount(imageId);
        }

        return image;
    }

    @Override
    public IPage<Image> list(int pageNum, int pageSize) {
        log.debug("查询图片列表: pageNum={}, pageSize={}", pageNum, pageSize);

        Page<Image> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<Image>()
            .eq(Image::getStatus, 1)
            .eq(Image::getIsDeleted, 0)
            .orderByDesc(Image::getQualityScore)
            .orderByDesc(Image::getPublishTime);

        return imageMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Image> listByCategory(Long categoryId, int pageNum, int pageSize) {
        log.debug("按分类查询图片: categoryId={}, pageNum={}, pageSize={}", categoryId, pageNum, pageSize);

        Page<Image> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<Image>()
            .eq(Image::getStatus, 1)
            .eq(Image::getIsDeleted, 0)
            .eq(Image::getCategoryId, categoryId)
            .orderByDesc(Image::getQualityScore)
            .orderByDesc(Image::getPublishTime);

        return imageMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Image> listByUploader(Long uploaderId, int pageNum, int pageSize) {
        log.debug("按上传者查询图片: uploaderId={}, pageNum={}, pageSize={}", uploaderId, pageNum, pageSize);

        Page<Image> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<Image>()
            .eq(Image::getStatus, 1)
            .eq(Image::getIsDeleted, 0)
            .eq(Image::getUploaderId, uploaderId)
            .orderByDesc(Image::getPublishTime);

        return imageMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Image> search(String keyword, int pageNum, int pageSize) {
        log.debug("搜索图片: keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);

        Page<Image> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<Image>()
            .eq(Image::getStatus, 1)
            .eq(Image::getIsDeleted, 0)
            .and(w -> w.like(Image::getTitle, keyword)
                .or()
                .like(Image::getDescription, keyword))
            .orderByDesc(Image::getQualityScore);

        return imageMapper.selectPage(page, wrapper);
    }

    @Override
    public java.util.List<Image> getByIds(java.util.List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return java.util.List.of();
        }
        return imageMapper.selectBatchIds(imageIds);
    }

    // ========== 私有方法 ==========

    /**
     * 计算图片质量分数
     */
    private double calculateQualityScore(Image image) {
        double score = 50.0;

        // 标题质量（0-20分）
        if (image.getTitle() != null && !image.getTitle().isEmpty()) {
            score += 20.0;
        }

        // 描述质量（0-15分）
        if (image.getDescription() != null && !image.getDescription().isEmpty()) {
            score += 15.0;
        }

        // 图片分辨率（0-20分）
        if (image.getWidth() != null && image.getHeight() != null) {
            int width = image.getWidth();
            int height = image.getHeight();
            if (width >= 1920 && height >= 1080) {
                score += 20.0;
            } else if (width >= 1280 && height >= 720) {
                score += 15.0;
            } else if (width >= 800 && height >= 600) {
                score += 10.0;
            }
        }

        // 缩略图（0-10分）
        if (image.getThumbnailUrl() != null && !image.getThumbnailUrl().isEmpty()) {
            score += 10.0;
        }

        // 标签（0-15分）
        if (image.getTags() != null) {
            score += 15.0;
        }

        return Math.max(0.0, Math.min(100.0, score));
    }

    /**
     * 增加浏览量（异步）
     */
    @Async("taskExecutor")
    protected void incrementViewCount(Long imageId) {
        try {
            Image image = imageMapper.selectById(imageId);
            if (image != null) {
                image.setViewCount(image.getViewCount() + 1);
                imageMapper.updateById(image);
            }
        } catch (Exception e) {
            log.error("增加浏览量失败: imageId={}", imageId, e);
        }
    }
}
