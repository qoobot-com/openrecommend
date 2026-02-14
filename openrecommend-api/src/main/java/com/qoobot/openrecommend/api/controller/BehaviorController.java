package com.qoobot.openrecommend.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qoobot.openrecommend.common.enums.BehaviorType;
import com.qoobot.openrecommend.common.enums.ContentType;
import com.qoobot.openrecommend.common.result.Result;
import com.qoobot.openrecommend.entity.UserBehavior;
import com.qoobot.openrecommend.service.UserBehaviorService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户行为控制器
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/behavior")
@Validated
public class BehaviorController {

    @Resource
    private UserBehaviorService userBehaviorService;

    /**
     * 记录用户行为
     */
    @PostMapping("/record")
    public Result<Void> recordBehavior(@Valid @RequestBody UserBehavior behavior) {
        log.info("记录用户行为: userId={}, contentType={}, contentId={}, behaviorType={}",
            behavior.getUserId(), behavior.getContentType(), behavior.getContentId(), behavior.getBehaviorType());

        boolean success = userBehaviorService.recordBehavior(behavior);
        return success ? Result.success() : Result.error("记录失败");
    }

    /**
     * 快捷记录浏览行为
     */
    @PostMapping("/view")
    public Result<Void> recordView(
            @RequestParam @NotNull Long userId,
            @RequestParam @NotNull ContentType contentType,
            @RequestParam @NotNull Long contentId,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) String device,
            @RequestParam(required = false) String ip) {
        
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setContentType(contentType.getValue());
        behavior.setContentId(contentId);
        behavior.setBehaviorType(BehaviorType.VIEW.getCode());
        behavior.setDuration(duration);
        behavior.setDevice(device);
        behavior.setIp(ip);

        boolean success = userBehaviorService.recordBehavior(behavior);
        return success ? Result.success() : Result.error("记录失败");
    }

    /**
     * 快捷记录点赞行为
     */
    @PostMapping("/like")
    public Result<Void> recordLike(
            @RequestParam @NotNull Long userId,
            @RequestParam @NotNull ContentType contentType,
            @RequestParam @NotNull Long contentId) {
        
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setContentType(contentType.getValue());
        behavior.setContentId(contentId);
        behavior.setBehaviorType(BehaviorType.LIKE.getCode());

        boolean success = userBehaviorService.recordBehavior(behavior);
        return success ? Result.success() : Result.error("记录失败");
    }

    /**
     * 快捷记录收藏行为
     */
    @PostMapping("/collect")
    public Result<Void> recordCollect(
            @RequestParam @NotNull Long userId,
            @RequestParam @NotNull ContentType contentType,
            @RequestParam @NotNull Long contentId) {
        
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setContentType(contentType.getValue());
        behavior.setContentId(contentId);
        behavior.setBehaviorType(BehaviorType.COLLECT.getCode());

        boolean success = userBehaviorService.recordBehavior(behavior);
        return success ? Result.success() : Result.error("记录失败");
    }

    /**
     * 快捷记录分享行为
     */
    @PostMapping("/share")
    public Result<Void> recordShare(
            @RequestParam @NotNull Long userId,
            @RequestParam @NotNull ContentType contentType,
            @RequestParam @NotNull Long contentId) {
        
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setContentType(contentType.getValue());
        behavior.setContentId(contentId);
        behavior.setBehaviorType(BehaviorType.SHARE.getCode());

        boolean success = userBehaviorService.recordBehavior(behavior);
        return success ? Result.success() : Result.error("记录失败");
    }

    /**
     * 获取用户行为历史
     */
    @GetMapping("/history/{userId}")
    public Result<IPage<UserBehavior>> getHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("获取用户行为历史: userId={}", userId);

        IPage<UserBehavior> page = userBehaviorService.getHistory(userId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 获取用户行为统计
     */
    @GetMapping("/statistics/{userId}")
    public Result<Map<String, Object>> getStatistics(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        log.info("获取用户行为统计: userId={}, days={}", userId, days);

        Map<String, Object> statistics = userBehaviorService.getStatistics(userId, days);
        return Result.success(statistics);
    }

    /**
     * 获取用户对指定内容的行为
     */
    @GetMapping("/user/{userId}/content/{contentType}/{contentId}")
    public Result<List<UserBehavior>> getUserBehavior(
            @PathVariable Long userId,
            @PathVariable String contentType,
            @PathVariable Long contentId) {
        log.debug("获取用户行为: userId={}, contentType={}, contentId={}", userId, contentType, contentId);

        List<UserBehavior> behaviors = userBehaviorService.getUserBehavior(userId, contentType, contentId);
        return Result.success(behaviors);
    }

    /**
     * 检查用户是否执行过某种行为
     */
    @GetMapping("/check")
    public Result<Boolean> hasBehavior(
            @RequestParam @NotNull Long userId,
            @RequestParam @NotNull String contentType,
            @RequestParam @NotNull Long contentId,
            @RequestParam @NotNull BehaviorType behaviorType) {
        log.debug("检查用户行为: userId={}, contentType={}, contentId={}, behaviorType={}",
            userId, contentType, contentId, behaviorType);

        boolean hasBehavior = userBehaviorService.hasBehavior(userId, contentType, contentId, behaviorType);
        return Result.success(hasBehavior);
    }

    /**
     * 获取用户最近浏览的内容ID
     */
    @GetMapping("/recent-viewed/{userId}")
    public Result<List<Long>> getRecentViewedContentIds(
            @PathVariable Long userId,
            @RequestParam String contentType,
            @RequestParam(defaultValue = "20") int limit) {
        log.debug("获取用户最近浏览内容: userId={}, contentType={}, limit={}", userId, contentType, limit);

        List<Long> contentIds = userBehaviorService.getRecentViewedContentIds(userId, contentType, limit);
        return Result.success(contentIds);
    }
}
