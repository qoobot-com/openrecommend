package com.qoobot.openrecommend.service;

import com.qoobot.openrecommend.entity.UserProfile;

/**
 * 用户画像服务接口
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
public interface UserProfileService {

    /**
     * 根据用户ID获取用户画像
     * 
     * @param userId 用户ID
     * @return 用户画像
     */
    UserProfile getByUserId(Long userId);

    /**
     * 异步更新用户画像
     * 
     * @param userId 用户ID
     */
    void updateAsync(Long userId);

    /**
     * 计算用户画像
     * 
     * @param userId 用户ID
     * @return 用户画像
     */
    UserProfile calculateProfile(Long userId);

    /**
     * 保存或更新用户画像
     * 
     * @param profile 用户画像
     * @return 是否成功
     */
    boolean saveOrUpdate(UserProfile profile);
}
