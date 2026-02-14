package com.qoobot.openrecommend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openrecommend.entity.Video;
import com.qoobot.openrecommend.mapper.VideoMapper;
import com.qoobot.openrecommend.service.VideoService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 视频服务实现
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
@Service
public class VideoServiceImpl implements VideoService {

    private static final Logger log = LoggerFactory.getLogger(VideoServiceImpl.class);

    @Resource
    private VideoMapper videoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upload(Video video) {
        log.info("上传视频: title={}, uploaderId={}", video.getTitle(), video.getUploaderId());

        // 设置初始状态
        video.setPublishTime(LocalDateTime.now());
        video.setViewCount(0L);
        video.setLikeCount(0);
        video.setCommentCount(0);
        video.setCollectCount(0);
        video.setShareCount(0);
        video.setFinishWatchCount(0);

        // 计算质量分数
        double qualityScore = calculateQualityScore(video);
        video.setQualityScore(qualityScore);

        // 保存视频
        videoMapper.insert(video);

        log.info("视频上传成功: id={}, qualityScore={}", video.getId(), qualityScore);

        return video.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Video video) {
        log.info("更新视频: id={}", video.getId());

        int result = videoMapper.updateById(video);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long videoId) {
        log.info("删除视频: id={}", videoId);

        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            log.warn("视频不存在: id={}", videoId);
            return false;
        }

        video.setIsDeleted(1);
        int result = videoMapper.updateById(video);

        return result > 0;
    }

    @Override
    public Video getDetail(Long videoId) {
        log.debug("获取视频详情: id={}", videoId);

        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            log.warn("视频不存在: id={}", videoId);
            return null;
        }

        // 增加播放量
        if (video.getStatus() == 1) {
            incrementViewCount(videoId);
        }

        return video;
    }

    @Override
    public IPage<Video> list(int pageNum, int pageSize) {
        log.debug("查询视频列表: pageNum={}, pageSize={}", pageNum, pageSize);

        Page<Video> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>()
            .eq(Video::getStatus, 1)
            .eq(Video::getIsDeleted, 0)
            .orderByDesc(Video::getQualityScore)
            .orderByDesc(Video::getPublishTime);

        return videoMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Video> listByCategory(Long categoryId, int pageNum, int pageSize) {
        log.debug("按分类查询视频: categoryId={}, pageNum={}, pageSize={}", categoryId, pageNum, pageSize);

        Page<Video> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>()
            .eq(Video::getStatus, 1)
            .eq(Video::getIsDeleted, 0)
            .eq(Video::getCategoryId, categoryId)
            .orderByDesc(Video::getQualityScore)
            .orderByDesc(Video::getPublishTime);

        return videoMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Video> listByUploader(Long uploaderId, int pageNum, int pageSize) {
        log.debug("按上传者查询视频: uploaderId={}, pageNum={}, pageSize={}", uploaderId, pageNum, pageSize);

        Page<Video> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>()
            .eq(Video::getStatus, 1)
            .eq(Video::getIsDeleted, 0)
            .eq(Video::getUploaderId, uploaderId)
            .orderByDesc(Video::getPublishTime);

        return videoMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Video> search(String keyword, int pageNum, int pageSize) {
        log.debug("搜索视频: keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);

        Page<Video> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>()
            .eq(Video::getStatus, 1)
            .eq(Video::getIsDeleted, 0)
            .and(w -> w.like(Video::getTitle, keyword)
                .or()
                .like(Video::getDescription, keyword))
            .orderByDesc(Video::getQualityScore);

        return videoMapper.selectPage(page, wrapper);
    }

    @Override
    public java.util.List<Video> getByIds(java.util.List<Long> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return java.util.List.of();
        }
        return videoMapper.selectBatchIds(videoIds);
    }

    // ========== 私有方法 ==========

    /**
     * 计算视频质量分数
     */
    private double calculateQualityScore(Video video) {
        double score = 50.0;

        // 标题质量（0-20分）
        if (video.getTitle() != null && !video.getTitle().isEmpty()) {
            score += 20.0;
        }

        // 描述质量（0-15分）
        if (video.getDescription() != null && !video.getDescription().isEmpty()) {
            score += 15.0;
        }

        // 视频时长（0-15分）
        if (video.getDuration() != null) {
            int duration = video.getDuration();
            if (duration >= 300 && duration <= 1800) {
                score += 15.0; // 5-30分钟
            } else if (duration >= 60) {
                score += 10.0; // 1分钟以上
            }
        }

        // 视频清晰度（0-20分）
        if (video.getResolution() != null) {
            if ("4K".equals(video.getResolution())) {
                score += 20.0;
            } else if ("1080p".equals(video.getResolution())) {
                score += 15.0;
            } else if ("720p".equals(video.getResolution())) {
                score += 10.0;
            }
        }

        // 封面图（0-10分）
        if (video.getCoverUrl() != null && !video.getCoverUrl().isEmpty()) {
            score += 10.0;
        }

        // 标签（0-10分）
        if (video.getTags() != null) {
            score += 10.0;
        }

        return Math.max(0.0, Math.min(100.0, score));
    }

    /**
     * 增加播放量（异步）
     */
    @Async("taskExecutor")
    protected void incrementViewCount(Long videoId) {
        try {
            Video video = videoMapper.selectById(videoId);
            if (video != null) {
                video.setViewCount(video.getViewCount() + 1);
                videoMapper.updateById(video);
            }
        } catch (Exception e) {
            log.error("增加播放量失败: videoId={}", videoId, e);
        }
    }
}
