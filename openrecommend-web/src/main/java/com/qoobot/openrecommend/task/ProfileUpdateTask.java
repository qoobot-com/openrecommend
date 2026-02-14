package com.qoobot.openrecommend.task;

import com.qoobot.openrecommend.entity.UserProfile;
import com.qoobot.openrecommend.mapper.UserBehaviorMapper;
import com.qoobot.openrecommend.mapper.UserProfileMapper;
import com.qoobot.openrecommend.service.CacheService;
import com.qoobot.openrecommend.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 用户画像更新定时任务
 * 定期批量更新用户画像
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileUpdateTask {

    private final UserProfileService userProfileService;
    private final UserProfileMapper userProfileMapper;
    private final UserBehaviorMapper userBehaviorMapper;
    private final CacheService cacheService;
    private final ExecutorService virtualThreadExecutor;

    private static final int BATCH_SIZE = 100;

    /**
     * 批量更新需要更新的用户画像
     * 每6小时执行一次
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    public void batchUpdateProfiles() {
        log.info("开始批量更新用户画像...");

        try {
            // 查询需要更新的用户画像
            List<UserProfile> profiles = userProfileMapper.selectNeedUpdate(BATCH_SIZE);

            if (profiles == null || profiles.isEmpty()) {
                log.info("没有需要更新的用户画像");
                return;
            }

            log.info("找到{}个需要更新的用户画像", profiles.size());

            // 使用CompletableFuture并发更新
            List<CompletableFuture<Void>> futures = profiles.stream()
                    .map(profile -> CompletableFuture.runAsync(
                            () -> updateUserProfile(profile.getUserId()),
                            virtualThreadExecutor
                    ))
                    .toList();

            // 等待所有更新完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            log.info("批量更新用户画像完成，处理数量: {}", profiles.size());
        } catch (Exception e) {
            log.error("批量更新用户画像失败", e);
        }
    }

    /**
     * 更新活跃用户的画像
     * 每天凌晨执行一次
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateActiveUserProfiles() {
        log.info("开始更新活跃用户画像...");

        try {
            // 查询最近7天有行为的用户
            List<Long> activeUserIds = userBehaviorMapper.selectActiveUserIds(7);

            if (activeUserIds == null || activeUserIds.isEmpty()) {
                log.info("没有活跃用户");
                return;
            }

            log.info("找到{}个活跃用户", activeUserIds.size());

            // 分批更新
            int total = activeUserIds.size();
            int batches = (total + BATCH_SIZE - 1) / BATCH_SIZE;

            for (int i = 0; i < batches; i++) {
                int fromIndex = i * BATCH_SIZE;
                int toIndex = Math.min((i + 1) * BATCH_SIZE, total);
                List<Long> batchUserIds = activeUserIds.subList(fromIndex, toIndex);

                log.info("处理第{}/{}批，用户数量: {}", i + 1, batches, batchUserIds.size());

                // 并发更新这一批用户
                List<CompletableFuture<Void>> futures = batchUserIds.stream()
                        .map(userId -> CompletableFuture.runAsync(
                                () -> updateUserProfile(userId),
                                virtualThreadExecutor
                        ))
                        .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }

            log.info("活跃用户画像更新完成，处理数量: {}", total);
        } catch (Exception e) {
            log.error("更新活跃用户画像失败", e);
        }
    }

    /**
     * 更新单个用户画像
     */
    private void updateUserProfile(Long userId) {
        try {
            // 重新计算用户画像
            userProfileService.updateAsync(userId);

            log.debug("用户{}画像更新成功", userId);
        } catch (Exception e) {
            log.error("用户{}画像更新失败", userId, e);
        }
    }

    /**
     * 清理过期画像缓存
     * 每天执行一次
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredProfileCache() {
        log.info("开始清理过期画像缓存...");

        try {
            cacheService.clearExpiredCache();

            log.info("过期画像缓存清理完成");
        } catch (Exception e) {
            log.error("清理过期画像缓存失败", e);
        }
    }

    /**
     * 重建用户画像
     * 对于画像数据不完整的用户进行重建
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void rebuildIncompleteProfiles() {
        log.info("开始重建不完整的用户画像...");

        try {
            // 查询画像不完整的用户（兴趣标签为空或内容偏好为空）
            List<UserProfile> incompleteProfiles = userProfileMapper.selectIncompleteProfiles(BATCH_SIZE);

            if (incompleteProfiles == null || incompleteProfiles.isEmpty()) {
                log.info("没有不完整的用户画像");
                return;
            }

            log.info("找到{}个不完整的用户画像", incompleteProfiles.size());

            // 并发重建
            List<CompletableFuture<Void>> futures = incompleteProfiles.stream()
                    .map(profile -> CompletableFuture.runAsync(
                            () -> userProfileService.updateAsync(profile.getUserId()),
                            virtualThreadExecutor
                    ))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            log.info("不完整用户画像重建完成，处理数量: {}", incompleteProfiles.size());
        } catch (Exception e) {
            log.error("重建不完整用户画像失败", e);
        }
    }

    /**
     * 计算用户画像统计信息
     */
    public Map<String, Object> calculateProfileStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();

        try {
            // 总用户数
            int totalUsers = userProfileMapper.selectTotalUserCount();
            stats.put("totalUsers", totalUsers);

            // 有画像的用户数
            int profilesCount = userProfileMapper.selectProfilesCount();
            stats.put("profilesCount", profilesCount);

            // 画像覆盖率
            double coverageRate = totalUsers > 0 ? (double) profilesCount / totalUsers * 100 : 0;
            stats.put("coverageRate", String.format("%.2f%%", coverageRate));

            // 需要更新的画像数
            int needUpdateCount = userProfileMapper.selectNeedUpdateCount();
            stats.put("needUpdateCount", needUpdateCount);

            // 不完整的画像数
            int incompleteCount = userProfileMapper.selectIncompleteProfilesCount();
            stats.put("incompleteCount", incompleteCount);

            stats.put("calculateTime", java.time.LocalDateTime.now());

        } catch (Exception e) {
            log.error("计算用户画像统计信息失败", e);
        }

        return stats;
    }

    /**
     * 手动触发批量更新
     */
    public void triggerBatchUpdate() {
        log.info("手动触发批量更新用户画像...");
        batchUpdateProfiles();
    }

    /**
     * 手动触发活跃用户更新
     */
    public void triggerActiveUserUpdate() {
        log.info("手动触发活跃用户画像更新...");
        updateActiveUserProfiles();
    }

    /**
     * 清理所有画像缓存
     */
    public void clearAllProfileCache() {
        log.info("开始清理所有画像缓存...");
        try {
            cacheService.clearAllCache();
            log.info("所有画像缓存清理完成");
        } catch (Exception e) {
            log.error("清理所有画像缓存失败", e);
        }
    }
}
