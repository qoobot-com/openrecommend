-- =============================================
-- OpenRecommend 数据库表结构
-- 版本: 1.0.0
-- 日期: 2026-02-15
-- 说明: 多内容类型智能推荐系统数据库表结构及初始数据
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS openrecommend
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE openrecommend;

-- =============================================
-- 一、用户相关表
-- =============================================

-- 1.1 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `nickname` VARCHAR(100) COMMENT '昵称',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
    `phone` VARCHAR(20) UNIQUE COMMENT '手机号',
    `password` VARCHAR(200) COMMENT '加密后的密码',
    `salt` VARCHAR(50) COMMENT '密码盐值',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常，2-锁定',
    `register_source` VARCHAR(20) COMMENT '注册来源：web,app,mini',
    `register_ip` VARCHAR(50) COMMENT '注册IP',
    `register_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`),
    INDEX `idx_phone` (`phone`),
    INDEX `idx_status` (`status`),
    INDEX `idx_register_time` (`register_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入测试用户数据
INSERT INTO `user` (`id`, `username`, `nickname`, `avatar`, `email`, `phone`, `password`, `salt`, `status`, `register_source`, `register_ip`, `register_time`, `last_login_time`) VALUES
(1001, 'testuser', '测试用户', 'https://example.com/avatar/1001.jpg', 'test@example.com', '13800138000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'test', 1, 'web', '192.168.1.100', '2026-02-15 09:00:00', '2026-02-15 10:00:00'),
(1002, 'admin', '管理员', 'https://example.com/avatar/1002.jpg', 'admin@example.com', '13800138001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin', 1, 'web', '192.168.1.101', '2026-02-14 09:00:00', '2026-02-15 09:30:00'),
(1003, 'author1', '技术达人', 'https://example.com/avatar/1003.jpg', 'author1@example.com', '13800138002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'author1', 1, 'app', '192.168.1.102', '2026-02-13 09:00:00', '2026-02-15 08:00:00'),
(1004, 'photographer', '摄影师小王', 'https://example.com/avatar/1004.jpg', 'photo@example.com', '13800138003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'photo', 1, 'app', '192.168.1.103', '2026-02-12 09:00:00', '2026-02-15 07:00:00'),
(1005, 'teacher', '编程讲师', 'https://example.com/avatar/1005.jpg', 'teacher@example.com', '13800138004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'teacher', 1, 'web', '192.168.1.104', '2026-02-11 09:00:00', '2026-02-15 06:00:00');

-- 1.2 用户画像表
DROP TABLE IF EXISTS `user_profile`;
CREATE TABLE `user_profile` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '画像ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `age_range` VARCHAR(20) COMMENT '年龄段：18-24,25-34,35-44,45-54,55+',
    `gender` TINYINT COMMENT '性别：0-未知，1-男，2-女',
    `interest_tags` JSON COMMENT '兴趣标签及权重：{"科技":0.8,"娱乐":0.6}',
    `content_preference` JSON COMMENT '内容类型偏好：{"article":0.5,"image":0.3,"video":0.2}',
    `category_preference` JSON COMMENT '分类偏好：{"科技":0.7,"娱乐":0.3}',
    `active_periods` JSON COMMENT '活跃时段：[8,12,18,22]',
    `device_preference` VARCHAR(20) COMMENT '设备偏好：mobile,pc,tablet',
    `read_preference` JSON COMMENT '阅读偏好：{"short":0.6,"medium":0.3,"long":0.1}',
    `total_read_time` INT DEFAULT 0 COMMENT '总阅读时长（分钟）',
    `total_view_count` INT DEFAULT 0 COMMENT '总浏览量',
    `total_like_count` INT DEFAULT 0 COMMENT '总点赞数',
    `total_collect_count` INT DEFAULT 0 COMMENT '总收藏数',
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_id` (`user_id`),
    INDEX `idx_last_update_time` (`last_update_time`),
    INDEX `idx_interest_tags` ((CAST(interest_tags AS CHAR(255)))),
    CONSTRAINT `fk_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户画像表';

-- 插入测试用户画像数据
INSERT INTO `user_profile` (`user_id`, `age_range`, `gender`, `interest_tags`, `content_preference`, `category_preference`, `active_periods`, `device_preference`, `read_preference`, `total_read_time`, `total_view_count`, `total_like_count`, `total_collect_count`) VALUES
(1001, '25-34', 1, '{"科技":0.8,"娱乐":0.6,"教育":0.5,"体育":0.4}', '{"article":0.5,"image":0.3,"video":0.2}', '{"科技":0.7,"教育":0.2,"体育":0.1}', '[8,12,18,22]', 'mobile', '{"short":0.6,"medium":0.3,"long":0.1}', 1250, 1520, 86, 34),
(1002, '35-44', 1, '{"科技":0.9,"管理":0.7,"教育":0.6}', '{"article":0.6,"video":0.4}', '{"科技":0.6,"管理":0.4}', '[9,14,19]', 'pc', '{"medium":0.5,"long":0.5}', 2500, 3200, 156, 89),
(1003, '25-34', 1, '{"科技":1.0,"编程":0.9,"教育":0.7}', '{"article":0.7,"video":0.3}', '{"科技":0.8,"教育":0.2}', '[10,14,20,22]', 'pc', '{"long":0.7,"medium":0.3}', 5000, 6500, 320, 150),
(1004, '25-34', 2, '{"艺术":0.9,"风景":0.8,"摄影":0.8}', '{"image":0.8,"video":0.2}', '{"风景":0.6,"艺术":0.4}', '[8,13,18,21]', 'mobile', '{"short":0.8,"medium":0.2}', 800, 1200, 78, 45),
(1005, '35-44', 1, '{"教育":0.9,"编程":0.8,"科技":0.7}', '{"video":0.7,"article":0.3}', '{"教育":0.8,"科技":0.2}', '[9,12,15,19]', 'pc', '{"medium":0.6,"long":0.4}', 3000, 4200, 210, 98);

-- 1.3 用户行为表
DROP TABLE IF EXISTS `user_behavior`;
CREATE TABLE `user_behavior` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '行为ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content_type` VARCHAR(20) NOT NULL COMMENT '内容类型：article,image,video',
    `content_id` BIGINT NOT NULL COMMENT '内容ID',
    `behavior_type` TINYINT NOT NULL COMMENT '行为类型：1-浏览，2-点赞，3-收藏，4-分享，5-评论',
    `duration` INT COMMENT '停留时长（秒），仅浏览行为记录',
    `scroll_depth` DECIMAL(5,2) COMMENT '滚动深度（百分比），0-100',
    `is_finish` TINYINT DEFAULT 0 COMMENT '是否完播/完读：0-否，1-是',
    `device` VARCHAR(20) COMMENT '设备类型：mobile,pc,tablet',
    `os` VARCHAR(20) COMMENT '操作系统：iOS,Android,Windows,Mac,Linux',
    `browser` VARCHAR(50) COMMENT '浏览器',
    `ip` VARCHAR(50) COMMENT 'IP地址',
    `location` VARCHAR(100) COMMENT '地理位置',
    `extra_info` JSON COMMENT '额外信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '行为时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_content` (`content_type`, `content_id`),
    INDEX `idx_behavior_type` (`behavior_type`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_user_content` (`user_id`, `content_type`, `content_id`),
    INDEX `idx_user_time` (`user_id`, `create_time`),
    INDEX `idx_content_time` (`content_type`, `content_id`, `create_time`),
    CONSTRAINT `fk_behavior_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为表'
PARTITION BY RANGE (TO_DAYS(create_time)) (
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')),
    PARTITION p202603 VALUES LESS THAN (TO_DAYS('2026-04-01')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);

-- =============================================
-- 二、文章相关表
-- =============================================

-- 2.1 文章分类表
DROP TABLE IF EXISTS `article_category`;
CREATE TABLE `article_category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `icon` VARCHAR(100) COMMENT '图标',
    `description` VARCHAR(500) COMMENT '描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_sort` (`sort`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章分类表';

-- 插入文章分类数据
INSERT INTO `article_category` (`id`, `name`, `parent_id`, `sort`, `icon`, `description`, `status`) VALUES
(1, '科技', 0, 1, 'tech', '科技相关内容', 1),
(2, '娱乐', 0, 2, 'entertainment', '娱乐相关内容', 1),
(3, '体育', 0, 3, 'sports', '体育相关内容', 1),
(4, '教育', 0, 4, 'education', '教育相关内容', 1),
(11, '人工智能', 1, 1, 'ai', '人工智能技术', 1),
(12, '编程开发', 1, 2, 'code', '编程开发技术', 1),
(13, '数码产品', 1, 3, 'digital', '数码产品资讯', 1),
(21, '电影', 2, 1, 'movie', '电影相关', 1),
(22, '音乐', 2, 2, 'music', '音乐相关', 1);

-- 2.2 文章表
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文章ID',
    `title` VARCHAR(500) NOT NULL COMMENT '文章标题',
    `summary` VARCHAR(1000) COMMENT '文章摘要',
    `content` LONGTEXT NOT NULL COMMENT '文章内容',
    `cover_image` VARCHAR(500) COMMENT '封面图URL',
    `category_id` BIGINT COMMENT '分类ID',
    `tags` JSON COMMENT '标签：["科技","AI"]',
    `keywords` JSON COMMENT '关键词（TF-IDF提取）及权重：{"人工智能":0.9,"深度学习":0.8}',
    `author_id` BIGINT COMMENT '作者ID',
    `source` VARCHAR(100) COMMENT '来源',
    `source_url` VARCHAR(500) COMMENT '来源URL',
    `is_original` TINYINT DEFAULT 1 COMMENT '是否原创：0-否，1-是',
    `word_count` INT COMMENT '字数',
    `read_duration` INT COMMENT '预估阅读时长（分钟）',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-草稿，1-待审核，2-已发布，3-下架，4-审核拒绝',
    `reject_reason` VARCHAR(500) COMMENT '审核拒绝原因',
    `publish_time` DATETIME COMMENT '发布时间',
    `view_count` BIGINT DEFAULT 0 COMMENT '浏览量',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `collect_count` INT DEFAULT 0 COMMENT '收藏数',
    `share_count` INT DEFAULT 0 COMMENT '分享数',
    `finish_read_count` INT DEFAULT 0 COMMENT '完读数',
    `avg_read_duration` INT COMMENT '平均阅读时长（秒）',
    `quality_score` DECIMAL(5,2) DEFAULT 0.00 COMMENT '质量得分（0-100）',
    `content_vector` JSON COMMENT '内容向量（特征）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_author_id` (`author_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_publish_time` (`publish_time`),
    INDEX `idx_quality_score` (`quality_score`),
    INDEX `idx_view_count` (`view_count`),
    FULLTEXT INDEX `ft_title_content` (`title`, `content`),
    CONSTRAINT `fk_article_category` FOREIGN KEY (`category_id`) REFERENCES `article_category` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_article_author` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 插入测试文章数据
INSERT INTO `article` (`id`, `title`, `summary`, `content`, `cover_image`, `category_id`, `tags`, `keywords`, `author_id`, `source`, `is_original`, `word_count`, `read_duration`, `status`, `publish_time`, `view_count`, `like_count`, `comment_count`, `collect_count`, `share_count`, `finish_read_count`, `avg_read_duration`, `quality_score`) VALUES
(1001, 'Spring Boot 3.5.10新特性详解', '本文详细介绍Spring Boot 3.5.10的新特性和改进', 'Spring Boot 3.5.10带来了许多新特性，包括虚拟线程支持、性能优化、配置改进等。虚拟线程是Java 21引入的一个重要特性，可以大幅提高并发性能...', 'https://example.com/covers/springboot.jpg', 1, '["科技", "Java", "Spring Boot"]', '{"Spring Boot":0.9,"虚拟线程":0.85,"性能优化":0.8}', 1003, '原创', 1, 3500, 8, 2, '2026-02-15 09:00:00', 1234, 56, 12, 23, 8, 567, 280, 92.50),
(1002, '深度学习入门指南', '从零开始学习深度学习的基础知识', '深度学习是人工智能的核心技术之一。本文将从神经网络的基础概念开始，逐步介绍卷积神经网络、循环神经网络等重要概念...', 'https://example.com/covers/deeplearning.jpg', 11, '["科技", "AI", "深度学习"]', '{"深度学习":0.95,"神经网络":0.9,"机器学习":0.8}', 1003, '原创', 1, 5000, 12, 2, '2026-02-14 09:00:00', 2345, 123, 28, 56, 15, 890, 420, 95.00),
(1003, 'Java 21新特性全面解析', 'Java 21带来了许多令人兴奋的新特性', 'Java 21是LTS版本，带来了虚拟线程、记录模式、模式匹配增强等重要特性。本文将详细介绍这些新特性的使用方法...', 'https://example.com/covers/java21.jpg', 12, '["科技", "Java"]', '{"Java 21":0.95,"虚拟线程":0.9,"LTS":0.8}', 1003, '原创', 1, 4200, 10, 2, '2026-02-13 09:00:00', 3456, 167, 35, 78, 22, 1234, 380, 94.00),
(1004, 'MySQL 8.4性能优化实践', 'MySQL 8.4性能优化的实战经验分享', 'MySQL 8.4在性能方面有很多改进，包括查询优化器增强、索引优化、缓存策略改进等。本文结合实际案例分享性能优化经验...', 'https://example.com/covers/mysql.jpg', 12, '["科技", "数据库", "MySQL"]', '{"MySQL":0.9,"性能优化":0.85,"索引":0.8}', 1003, '原创', 1, 3800, 9, 2, '2026-02-12 09:00:00', 1567, 89, 18, 42, 11, 678, 300, 91.50),
(1005, 'Redis缓存设计与实战', '深入理解Redis缓存设计模式', 'Redis作为高性能缓存系统，在实际项目中有广泛的应用。本文介绍Redis的数据结构、缓存策略、分布式锁等重要概念...', 'https://example.com/covers/redis.jpg', 12, '["科技", "缓存", "Redis"]', '{"Redis":0.9,"缓存":0.85,"分布式":0.8}', 1003, '原创', 1, 4500, 11, 2, '2026-02-11 09:00:00', 2890, 134, 30, 67, 19, 1012, 410, 93.00);

-- =============================================
-- 三、图片相关表
-- =============================================

-- 3.1 图片分类表
DROP TABLE IF EXISTS `image_category`;
CREATE TABLE `image_category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `icon` VARCHAR(100) COMMENT '图标',
    `description` VARCHAR(500) COMMENT '描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_sort` (`sort`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片分类表';

-- 插入图片分类数据
INSERT INTO `image_category` (`id`, `name`, `parent_id`, `sort`, `icon`, `description`, `status`) VALUES
(1, '风景', 0, 1, 'landscape', '风景类图片', 1),
(2, '人像', 0, 2, 'portrait', '人像类图片', 1),
(3, '建筑', 0, 3, 'architecture', '建筑类图片', 1),
(4, '艺术', 0, 4, 'art', '艺术类图片', 1),
(11, '日落', 1, 1, 'sunset', '日落风景', 1),
(12, '山脉', 1, 2, 'mountain', '山脉风景', 1),
(13, '海滩', 1, 3, 'beach', '海滩风景', 1);

-- 3.2 图片表
DROP TABLE IF EXISTS `image`;
CREATE TABLE `image` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '图片ID',
    `title` VARCHAR(200) COMMENT '图片标题',
    `description` VARCHAR(1000) COMMENT '图片描述',
    `url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `thumbnail_url` VARCHAR(500) COMMENT '缩略图URL',
    `width` INT COMMENT '图片宽度（像素）',
    `height` INT COMMENT '图片高度（像素）',
    `aspect_ratio` DECIMAL(5,3) COMMENT '宽高比',
    `file_size` BIGINT COMMENT '文件大小（字节）',
    `format` VARCHAR(10) COMMENT '图片格式：jpg,png,gif,webp',
    `color_depth` INT COMMENT '颜色深度',
    `category_id` BIGINT COMMENT '分类ID',
    `tags` JSON COMMENT '标签：["风景","日落"]',
    `keywords` JSON COMMENT '关键词及权重',
    `uploader_id` BIGINT COMMENT '上传者ID',
    `source` VARCHAR(100) COMMENT '来源',
    `color_histogram` JSON COMMENT '颜色直方图特征',
    `dominant_colors` JSON COMMENT '主色调：["#FF5733","#C70039"]',
    `visual_features` JSON COMMENT '视觉特征向量',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-待审核，1-已发布，2-下架，3-审核拒绝',
    `reject_reason` VARCHAR(500) COMMENT '审核拒绝原因',
    `publish_time` DATETIME COMMENT '发布时间',
    `view_count` BIGINT DEFAULT 0 COMMENT '浏览量',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `collect_count` INT DEFAULT 0 COMMENT '收藏数',
    `download_count` INT DEFAULT 0 COMMENT '下载次数',
    `quality_score` DECIMAL(5,2) DEFAULT 0.00 COMMENT '质量得分（0-100）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_uploader_id` (`uploader_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_publish_time` (`publish_time`),
    INDEX `idx_quality_score` (`quality_score`),
    INDEX `idx_view_count` (`view_count`),
    CONSTRAINT `fk_image_category` FOREIGN KEY (`category_id`) REFERENCES `image_category` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_image_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片表';

-- 插入测试图片数据
INSERT INTO `image` (`id`, `title`, `description`, `url`, `thumbnail_url`, `width`, `height`, `aspect_ratio`, `file_size`, `format`, `category_id`, `tags`, `keywords`, `uploader_id`, `source`, `dominant_colors`, `status`, `publish_time`, `view_count`, `like_count`, `collect_count`, `download_count`, `quality_score`) VALUES
(2001, '美丽的日落', '在海边拍摄的美丽日落', 'https://example.com/images/sunset.jpg', 'https://example.com/images/sunset_thumb.jpg', 1920, 1080, 1.778, 2048576, 'jpg', 1, '["风景", "日落", "海滩"]', '{"日落":0.95,"海滩":0.8,"美景":0.7}', 1004, '原创', '["#FF5733", "#C70039", "#900C3F"]', 1, '2026-02-15 09:00:00', 567, 34, 18, 12, 88.50),
(2002, '雪山风光', '壮观的雪山景色', 'https://example.com/images/mountain.jpg', 'https://example.com/images/mountain_thumb.jpg', 1920, 1200, 1.600, 3145728, 'jpg', 2, '["风景", "山脉", "雪山"]', '{"山脉":0.9,"雪山":0.85,"壮观":0.8}', 1004, '原创', '["#FFFFFF", "#E0E0E0", "#87CEEB"]', 1, '2026-02-14 09:00:00', 789, 56, 28, 23, 91.00),
(2003, '人像摄影', '专业的人像摄影作品', 'https://example.com/images/portrait.jpg', 'https://example.com/images/portrait_thumb.jpg', 2048, 2048, 1.000, 4194304, 'jpg', 2, '["人像", "摄影", "艺术"]', '{"人像":0.9,"摄影":0.85,"艺术":0.8}', 1004, '原创', '["#FFE4C4", "#DEB887", "#D2691E"]', 1, '2026-02-13 09:00:00', 456, 45, 22, 18, 89.50),
(2004, '现代建筑', '现代城市建筑摄影', 'https://example.com/images/architecture.jpg', 'https://example.com/images/architecture_thumb.jpg', 1920, 1080, 1.778, 2621440, 'jpg', 3, '["建筑", "现代", "城市"]', '{"建筑":0.9,"现代":0.85,"城市":0.8}', 1004, '原创', '["#708090", "#778899", "#B0C4DE"]', 1, '2026-02-12 09:00:00', 345, 28, 15, 10, 87.00),
(2005, '艺术画作', '精美的艺术画作', 'https://example.com/images/art.jpg', 'https://example.com/images/art_thumb.jpg', 1920, 1080, 1.778, 2359296, 'jpg', 4, '["艺术", "画作", "精美"]', '{"艺术":0.95,"画作":0.9,"精美":0.85}', 1004, '原创', '["#FFD700", "#FFA500", "#FF8C00"]', 1, '2026-02-11 09:00:00', 678, 67, 35, 28, 92.00);

-- =============================================
-- 四、视频相关表
-- =============================================

-- 4.1 视频分类表
DROP TABLE IF EXISTS `video_category`;
CREATE TABLE `video_category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `icon` VARCHAR(100) COMMENT '图标',
    `description` VARCHAR(500) COMMENT '描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_sort` (`sort`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频分类表';

-- 插入视频分类数据
INSERT INTO `video_category` (`id`, `name`, `parent_id`, `sort`, `icon`, `description`, `status`) VALUES
(1, '搞笑', 0, 1, 'funny', '搞笑类视频', 1),
(2, '生活', 0, 2, 'life', '生活类视频', 1),
(3, '教育', 0, 3, 'education', '教育类视频', 1),
(4, '音乐', 0, 4, 'music', '音乐类视频', 1),
(11, '喜剧', 1, 1, 'comedy', '喜剧视频', 1),
(12, '段子', 1, 2, 'joke', '段子视频', 1),
(13, '编程教程', 3, 1, 'code', '编程教学视频', 1);

-- 4.2 视频表
DROP TABLE IF EXISTS `video`;
CREATE TABLE `video` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '视频ID',
    `title` VARCHAR(500) NOT NULL COMMENT '视频标题',
    `description` TEXT COMMENT '视频描述',
    `cover_url` VARCHAR(500) NOT NULL COMMENT '封面图URL',
    `video_url` VARCHAR(500) NOT NULL COMMENT '视频URL',
    `duration` INT NOT NULL COMMENT '视频时长（秒）',
    `width` INT COMMENT '视频宽度（像素）',
    `height` INT COMMENT '视频高度（像素）',
    `resolution` VARCHAR(20) COMMENT '分辨率：720p,1080p,4K',
    `format` VARCHAR(20) COMMENT '视频格式：mp4,avi,mov',
    `file_size` BIGINT COMMENT '文件大小（字节）',
    `bitrate` INT COMMENT '比特率（kbps）',
    `fps` DECIMAL(5,2) COMMENT '帧率',
    `category_id` BIGINT COMMENT '分类ID',
    `tags` JSON COMMENT '标签：["搞笑","生活"]',
    `keywords` JSON COMMENT '关键词及权重',
    `uploader_id` BIGINT COMMENT '上传者ID',
    `source` VARCHAR(100) COMMENT '来源',
    `keyframe_features` JSON COMMENT '关键帧特征',
    `visual_features` JSON COMMENT '视觉特征向量',
    `text_features` JSON COMMENT '文本特征向量',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-待审核，1-已发布，2-下架，3-审核拒绝',
    `reject_reason` VARCHAR(500) COMMENT '审核拒绝原因',
    `publish_time` DATETIME COMMENT '发布时间',
    `view_count` BIGINT DEFAULT 0 COMMENT '播放量',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `collect_count` INT DEFAULT 0 COMMENT '收藏数',
    `share_count` INT DEFAULT 0 COMMENT '分享数',
    `finish_watch_count` INT DEFAULT 0 COMMENT '完播数',
    `avg_watch_duration` INT COMMENT '平均观看时长（秒）',
    `quality_score` DECIMAL(5,2) DEFAULT 0.00 COMMENT '质量得分（0-100）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_uploader_id` (`uploader_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_publish_time` (`publish_time`),
    INDEX `idx_quality_score` (`quality_score`),
    INDEX `idx_view_count` (`view_count`),
    INDEX `idx_duration` (`duration`),
    CONSTRAINT `fk_video_category` FOREIGN KEY (`category_id`) REFERENCES `video_category` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_video_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频表';

-- 插入测试视频数据
INSERT INTO `video` (`id`, `title`, `description`, `cover_url`, `video_url`, `duration`, `width`, `height`, `resolution`, `file_size`, `bitrate`, `fps`, `category_id`, `tags`, `keywords`, `uploader_id`, `source`, `status`, `publish_time`, `view_count`, `like_count`, `comment_count`, `collect_count`, `share_count`, `finish_watch_count`, `avg_watch_duration`, `quality_score`) VALUES
(3001, 'Spring Boot入门教程', '从零开始学习Spring Boot框架', 'https://example.com/covers/springboot_video.jpg', 'https://example.com/videos/springboot_tutorial.mp4', 3600, 1920, 1080, '1080p', 524288000, 2000, 30.00, 3, '["教育", "编程", "Spring Boot"]', '{"Spring Boot":0.95,"编程":0.9,"教程":0.85}', 1005, '原创', 1, '2026-02-15 09:00:00', 2345, 123, 45, 67, 23, 890, 2400, 90.00),
(3002, 'Java并发编程实战', '深入讲解Java并发编程的核心概念', 'https://example.com/covers/java_concurrent.jpg', 'https://example.com/videos/java_concurrent.mp4', 4800, 1920, 1080, '1080p', 734003200, 2200, 30.00, 3, '["教育", "编程", "Java"]', '{"Java":0.95,"并发":0.9,"编程":0.85}', 1005, '原创', 1, '2026-02-14 09:00:00', 3456, 178, 56, 89, 34, 1234, 3200, 92.00),
(3003, '搞笑段子合集', '精选搞笑段子，让你笑不停', 'https://example.com/covers/funny_joke.jpg', 'https://example.com/videos/funny_joke.mp4', 600, 1280, 720, '720p', 104857600, 1500, 30.00, 1, '["搞笑", "段子", "喜剧"]', '{"搞笑":0.95,"段子":0.9,"喜剧":0.85}', 1005, '原创', 1, '2026-02-13 09:00:00', 5678, 456, 123, 234, 89, 2345, 450, 88.00),
(3004, '生活记录Vlog', '记录日常生活点滴', 'https://example.com/covers/life_vlog.jpg', 'https://example.com/videos/life_vlog.mp4', 900, 1920, 1080, '1080p', 157286400, 1800, 30.00, 2, '["生活", "Vlog", "记录"]', '{"生活":0.9,"Vlog":0.85,"记录":0.8}', 1005, '原创', 1, '2026-02-12 09:00:00', 1234, 89, 23, 45, 12, 567, 600, 85.00),
(3005, '音乐MV', '原创音乐MV作品', 'https://example.com/covers/music_mv.jpg', 'https://example.com/videos/music_mv.mp4', 240, 1920, 1080, '1080p', 83886080, 2500, 30.00, 4, '["音乐", "MV", "原创"]', '{"音乐":0.95,"MV":0.9,"原创":0.85}', 1005, '原创', 1, '2026-02-11 09:00:00', 4567, 234, 67, 123, 45, 1234, 180, 89.00);

-- =============================================
-- 五、推荐相关表
-- =============================================

-- 5.1 推荐结果缓存表
DROP TABLE IF EXISTS `recommend_cache`;
CREATE TABLE `recommend_cache` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '缓存ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content_type` VARCHAR(20) COMMENT '内容类型：all,article,image,video',
    `recommend_type` VARCHAR(50) COMMENT '推荐类型：personal,popular,latest',
    `recommend_result` JSON NOT NULL COMMENT '推荐结果JSON',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user_type` (`user_id`, `content_type`, `recommend_type`),
    INDEX `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐结果缓存表';

-- 5.2 物品相似度表
DROP TABLE IF EXISTS `item_similarity`;
CREATE TABLE `item_similarity` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `content_type` VARCHAR(20) NOT NULL COMMENT '内容类型',
    `item_id_1` BIGINT NOT NULL COMMENT '物品1的ID',
    `item_id_2` BIGINT NOT NULL COMMENT '物品2的ID',
    `similarity` DECIMAL(10,6) NOT NULL COMMENT '相似度',
    `calculate_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '计算时间',
    UNIQUE KEY `uk_items` (`content_type`, `item_id_1`, `item_id_2`),
    INDEX `idx_item_1` (`content_type`, `item_id_1`),
    INDEX `idx_similarity` (`similarity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品相似度表';

-- =============================================
-- 六、插入测试行为数据
-- =============================================

-- 插入用户行为数据
INSERT INTO `user_behavior` (`user_id`, `content_type`, `content_id`, `behavior_type`, `duration`, `scroll_depth`, `is_finish`, `device`, `os`, `browser`, `ip`, `location`, `create_time`) VALUES
-- 用户1001的行为
(1001, 'article', 1001, 1, 300, 80.5, 1, 'mobile', 'iOS', 'Safari', '192.168.1.100', '北京市', '2026-02-15 09:30:00'),
(1001, 'article', 1001, 2, NULL, NULL, NULL, 'mobile', 'iOS', 'Safari', '192.168.1.100', '北京市', '2026-02-15 09:35:00'),
(1001, 'article', 1002, 1, 420, 65.0, 0, 'mobile', 'iOS', 'Safari', '192.168.1.100', '北京市', '2026-02-15 10:00:00'),
(1001, 'image', 2001, 1, 30, NULL, NULL, 'mobile', 'iOS', 'Safari', '192.168.1.100', '北京市', '2026-02-15 10:10:00'),
(1001, 'image', 2001, 2, NULL, NULL, NULL, 'mobile', 'iOS', 'Safari', '192.168.1.100', '北京市', '2026-02-15 10:11:00'),
(1001, 'video', 3001, 1, 600, NULL, 0, 'mobile', 'iOS', 'Safari', '192.168.1.100', '北京市', '2026-02-15 10:20:00'),
(1001, 'video', 3003, 1, 600, NULL, 1, 'mobile', 'iOS', 'Safari', '192.168.1.100', '北京市', '2026-02-15 10:30:00'),
(1001, 'video', 3003, 2, NULL, NULL, NULL, 'mobile', 'iOS', 'Safari', '192.168.1.100', '北京市', '2026-02-15 10:31:00'),
-- 用户1002的行为
(1002, 'article', 1001, 1, 280, 75.0, 1, 'pc', 'Windows', 'Chrome', '192.168.1.101', '上海市', '2026-02-15 09:00:00'),
(1002, 'article', 1002, 1, 480, 90.0, 1, 'pc', 'Windows', 'Chrome', '192.168.1.101', '上海市', '2026-02-15 09:30:00'),
(1002, 'article', 1003, 1, 400, 70.0, 0, 'pc', 'Windows', 'Chrome', '192.168.1.101', '上海市', '2026-02-15 10:00:00'),
(1002, 'video', 3001, 1, 2400, NULL, 0, 'pc', 'Windows', 'Chrome', '192.168.1.101', '上海市', '2026-02-15 10:30:00'),
(1002, 'video', 3002, 1, 3000, NULL, 1, 'pc', 'Windows', 'Chrome', '192.168.1.101', '上海市', '2026-02-15 11:00:00'),
(1002, 'video', 3002, 2, NULL, NULL, NULL, 'pc', 'Windows', 'Chrome', '192.168.1.101', '上海市', '2026-02-15 11:50:00'),
-- 用户1003的行为
(1003, 'article', 1001, 1, 320, 85.0, 1, 'pc', 'Windows', 'Chrome', '192.168.1.102', '广州市', '2026-02-15 08:00:00'),
(1003, 'article', 1002, 1, 500, 95.0, 1, 'pc', 'Windows', 'Chrome', '192.168.1.102', '广州市', '2026-02-15 08:30:00'),
(1003, 'article', 1003, 1, 450, 80.0, 1, 'pc', 'Windows', 'Chrome', '192.168.1.102', '广州市', '2026-02-15 09:00:00'),
(1003, 'article', 1004, 1, 380, 75.0, 0, 'pc', 'Windows', 'Chrome', '192.168.1.102', '广州市', '2026-02-15 09:30:00'),
(1003, 'article', 1005, 1, 420, 82.0, 1, 'pc', 'Windows', 'Chrome', '192.168.1.102', '广州市', '2026-02-15 10:00:00'),
-- 用户1004的行为
(1004, 'image', 2001, 1, 45, NULL, NULL, 'mobile', 'Android', 'Chrome', '192.168.1.103', '深圳市', '2026-02-15 07:00:00'),
(1004, 'image', 2001, 2, NULL, NULL, NULL, 'mobile', 'Android', 'Chrome', '192.168.1.103', '深圳市', '2026-02-15 07:01:00'),
(1004, 'image', 2002, 1, 50, NULL, NULL, 'mobile', 'Android', 'Chrome', '192.168.1.103', '深圳市', '2026-02-15 07:10:00'),
(1004, 'image', 2003, 1, 60, NULL, NULL, 'mobile', 'Android', 'Chrome', '192.168.1.103', '深圳市', '2026-02-15 07:20:00'),
(1004, 'image', 2003, 2, NULL, NULL, NULL, 'mobile', 'Android', 'Chrome', '192.168.1.103', '深圳市', '2026-02-15 07:21:00'),
-- 用户1005的行为
(1005, 'video', 3001, 1, 3000, NULL, 1, 'pc', 'Windows', 'Chrome', '192.168.1.104', '杭州市', '2026-02-15 06:00:00'),
(1005, 'video', 3002, 1, 3600, NULL, 1, 'pc', 'Windows', 'Chrome', '192.168.1.104', '杭州市', '2026-02-15 07:00:00'),
(1005, 'video', 3003, 1, 580, NULL, 0, 'pc', 'Windows', 'Chrome', '192.168.1.104', '杭州市', '2026-02-15 08:00:00'),
(1005, 'video', 3003, 2, NULL, NULL, NULL, 'pc', 'Windows', 'Chrome', '192.168.1.104', '杭州市', '2026-02-15 08:10:00'),
(1005, 'video', 3004, 1, 750, NULL, 0, 'pc', 'Windows', 'Chrome', '192.168.1.104', '杭州市', '2026-02-15 09:00:00');

-- =============================================
-- 七、验证数据
-- =============================================

-- 查询数据统计
SELECT '用户表数据量' AS table_name, COUNT(*) AS count FROM `user`
UNION ALL
SELECT '用户画像表数据量', COUNT(*) FROM `user_profile`
UNION ALL
SELECT '用户行为表数据量', COUNT(*) FROM `user_behavior`
UNION ALL
SELECT '文章分类表数据量', COUNT(*) FROM `article_category`
UNION ALL
SELECT '文章表数据量', COUNT(*) FROM `article`
UNION ALL
SELECT '图片分类表数据量', COUNT(*) FROM `image_category`
UNION ALL
SELECT '图片表数据量', COUNT(*) FROM `image`
UNION ALL
SELECT '视频分类表数据量', COUNT(*) FROM `video_category`
UNION ALL
SELECT '视频表数据量', COUNT(*) FROM `video`;

-- 查询表结构
SHOW TABLES;
