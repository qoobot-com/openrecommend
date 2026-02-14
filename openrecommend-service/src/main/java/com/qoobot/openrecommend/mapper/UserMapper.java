package com.qoobot.openrecommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openrecommend.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问层
 * 
 * @author OpenRecommend
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 基础CRUD继承自BaseMapper
}
