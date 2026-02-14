package com.qoobot.openrecommend.api.controller;

import com.qoobot.openrecommend.common.result.Result;
import com.qoobot.openrecommend.entity.User;
import com.qoobot.openrecommend.entity.UserProfile;
import com.qoobot.openrecommend.mapper.UserMapper;
import com.qoobot.openrecommend.service.UserProfileService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserProfileService userProfileService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Long> register(
            @RequestParam @NotNull String username,
            @RequestParam @NotNull String password,
            @RequestParam String nickname,
            @RequestParam String email,
            @RequestParam String phone) {
        log.info("用户注册: username={}", username);

        // 检查用户名是否已存在
        User existingUser = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
        );

        if (existingUser != null) {
            return Result.error("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        // TODO: 实际应该对密码进行加密
        user.setPassword(password);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus(1);
        user.setRegisterTime(java.time.LocalDateTime.now());

        userMapper.insert(user);

        log.info("用户注册成功: id={}", user.getId());
        return Result.success(user.getId());
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<User> login(
            @RequestParam @NotNull String username,
            @RequestParam @NotNull String password) {
        log.info("用户登录: username={}", username);

        // 查询用户
        User user = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
        );

        if (user == null) {
            return Result.error("用户不存在");
        }

        // TODO: 实际应该对密码进行加密验证
        if (!password.equals(user.getPassword())) {
            return Result.error("密码错误");
        }

        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        // 更新最后登录时间
        user.setLastLoginTime(java.time.LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户登录成功: id={}", user.getId());
        return Result.success(user);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info/{userId}")
    public Result<User> getUserInfo(@PathVariable Long userId) {
        log.debug("获取用户信息: userId={}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 不返回密码
        user.setPassword(null);

        return Result.success(user);
    }

    /**
     * 获取用户画像
     */
    @GetMapping("/profile/{userId}")
    public Result<UserProfile> getUserProfile(@PathVariable Long userId) {
        log.debug("获取用户画像: userId={}", userId);

        UserProfile profile = userProfileService.getByUserId(userId);
        return Result.success(profile);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info/{userId}")
    public Result<Void> updateUserInfo(
            @PathVariable Long userId,
            @RequestParam String nickname,
            @RequestParam String avatar,
            @RequestParam String email,
            @RequestParam String phone) {
        log.info("更新用户信息: userId={}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setNickname(nickname);
        user.setAvatar(avatar);
        user.setEmail(email);
        user.setPhone(phone);

        int result = userMapper.updateById(user);
        return result > 0 ? Result.success() : Result.error("更新失败");
    }

    /**
     * 手动触发更新用户画像
     */
    @PostMapping("/profile/{userId}/update")
    public Result<Void> updateUserProfile(@PathVariable Long userId) {
        log.info("手动更新用户画像: userId={}", userId);

        userProfileService.updateAsync(userId);

        return Result.success("画像更新任务已提交");
    }
}
