package com.qoobot.openrecommend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openrecommend.common.enums.BehaviorType;
import com.qoobot.openrecommend.entity.UserBehavior;
import com.qoobot.openrecommend.mapper.UserBehaviorMapper;
import com.qoobot.openrecommend.service.UserBehaviorService;
import com.qoobot.openrecommend.service.UserProfileService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户行为服务实现
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
@Service
public class UserBehaviorServiceImpl implements UserBehaviorService {

    private static final Logger log = LoggerFactory.getLogger(UserBehaviorServiceImpl.class);

    @Resource
    private UserBehaviorMapper userBehaviorMapper;

    @Resource
    private UserProfileService userProfileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recordBehavior(UserBehavior behavior) {
        log.info("记录用户行为: userId={}, contentType={}, contentId={}, behaviorType={}",
            behavior.getUserId(), behavior.getContentType(), behavior.getContentId(), behavior.getBehaviorType());

        // 设置创建时间
        behavior.setCreateTime(LocalDateTime.now());

        // 保存行为记录
        int result = userBehaviorMapper.insert(behavior);

        // 异步更新用户画像
        if (result > 0) {
            updateProfileAsync(behavior.getUserId());
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchRecord(List<UserBehavior> behaviors) {
        if (behaviors == null || behaviors.isEmpty()) {
            return false;
        }

        log.info("批量记录用户行为: count={}", behaviors.size());

        // 设置创建时间
        LocalDateTime now = LocalDateTime.now();
        behaviors.forEach(b -> b.setCreateTime(now));

        // 批量保存
        for (UserBehavior behavior : behaviors) {
            userBehaviorMapper.insert(behavior);
            
            // 异步更新用户画像
            updateProfileAsync(behavior.getUserId());
        }

        return true;
    }

    @Override
    public IPage<UserBehavior> getHistory(Long userId, int pageNum, int pageSize) {
        log.debug("获取用户行为历史: userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);

        Page<UserBehavior> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<UserBehavior>()
            .eq(UserBehavior::getUserId, userId)
            .orderByDesc(UserBehavior::getCreateTime);

        return userBehaviorMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> getStatistics(Long userId, int days) {
        log.info("获取用户行为统计: userId={}, days={}", userId, days);

        Map<String, Object> statistics = new HashMap<>();

        // 统计各类型行为次数
        statistics.put("view", userBehaviorMapper.countByBehaviorType(userId, BehaviorType.VIEW.getCode(), days));
        statistics.put("like", userBehaviorMapper.countByBehaviorType(userId, BehaviorType.LIKE.getCode(), days));
        statistics.put("collect", userBehaviorMapper.countByBehaviorType(userId, BehaviorType.COLLECT.getCode(), days));
        statistics.put("share", userBehaviorMapper.countByBehaviorType(userId, BehaviorType.SHARE.getCode(), days));
        statistics.put("comment", userBehaviorMapper.countByBehaviorType(userId, BehaviorType.COMMENT.getCode(), days));

        // 统计各内容类型互动次数
        List<UserBehavior> behaviors = userBehaviorMapper.selectRecentBehaviors(userId, days, 10000);
        
        long articleCount = behaviors.stream().filter(b -> "article".equals(b.getContentType())).count();
        long imageCount = behaviors.stream().filter(b -> "image".equals(b.getContentType())).count();
        long videoCount = behaviors.stream().filter(b -> "video".equals(b.getContentType())).count();

        statistics.put("articleInteract", articleCount);
        statistics.put("imageInteract", imageCount);
        statistics.put("videoInteract", videoCount);

        // 统计总互动时长（仅浏览行为）
        int totalDuration = behaviors.stream()
            .filter(b -> b.getBehaviorType() == BehaviorType.VIEW.getCode() && b.getDuration() != null)
            .mapToInt(UserBehavior::getDuration)
            .sum();

        statistics.put("totalDuration", totalDuration); // 秒

        return statistics;
    }

    @Override
    public List<UserBehavior> getUserBehavior(Long userId, String contentType, Long contentId) {
        log.debug("获取用户行为: userId={}, contentType={}, contentId={}", userId, contentType, contentId);

        return userBehaviorMapper.selectByUserAndContent(userId, contentType, contentId);
    }

    @Override
    public boolean hasBehavior(Long userId, String contentType, Long contentId, BehaviorType behaviorType) {
        List<UserBehavior> behaviors = getUserBehavior(userId, contentType, contentId);
        return behaviors.stream().anyMatch(b -> b.getBehaviorType().equals(behaviorType.getCode()));
    }

    @Override
    public List<Long> getRecentViewedContentIds(Long userId, String contentType, int limit) {
        log.debug("获取用户最近浏览内容ID: userId={}, contentType={}, limit={}", userId, contentType, limit);

        return userBehaviorMapper.selectHistoryContentIds(userId, contentType, limit);
    }

    // ========== 私有方法 ==========

    /**
     * 异步更新用户画像
     */
    @Async("taskExecutor")
    protected void updateProfileAsync(Long userId) {
        try {
            log.debug("触发用户画像更新: userId={}", userId);
            userProfileService.updateAsync(userId);
        } catch (Exception e) {
            log.error("异步更新用户画像失败: userId={}", userId, e);
        }
    }
}
