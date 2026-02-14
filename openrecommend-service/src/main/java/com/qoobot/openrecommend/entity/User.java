package com.qoobot.openrecommend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体
 *
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 加密后的密码
     */
    private String password;

    /**
     * 密码盐值
     */
    private String salt;

    /**
     * 状态：0-禁用，1-正常，2-锁定
     */
    private Integer status;

    /**
     * 注册来源：web,app,mini
     */
    private String registerSource;

    /**
     * 注册IP
     */
    private String registerIp;

    /**
     * 注册时间
     */
    private String registerTime;

    /**
     * 最后登录时间
     */
    private String lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;
}
