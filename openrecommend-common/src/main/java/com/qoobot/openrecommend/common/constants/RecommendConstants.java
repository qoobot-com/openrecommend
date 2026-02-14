package com.qoobot.openrecommend.common.constants;

/**
 * 推荐常量定义
 *
 * @author Qoobot Team
 * @since 1.0.0
 */
public class RecommendConstants {

    /**
     * 推荐类型
     */
    public static final String RECOMMEND_TYPE_PERSONAL = "personal";
    public static final String RECOMMEND_TYPE_HOT = "hot";
    public static final String RECOMMEND_TYPE_RELATED = "related";

    /**
     * 内容类型
     */
    public static final String CONTENT_TYPE_ARTICLE = "article";
    public static final String CONTENT_TYPE_IMAGE = "image";
    public static final String CONTENT_TYPE_VIDEO = "video";
    public static final String CONTENT_TYPE_ALL = "all";

    /**
     * 权重配置 - 新用户
     */
    public static final double WEIGHT_CONTENT_NEW_USER = 0.7;
    public static final double WEIGHT_CF_NEW_USER = 0.1;
    public static final double WEIGHT_POPULARITY = 0.2;

    /**
     * 权重配置 - 老用户
     */
    public static final double WEIGHT_CONTENT_OLD_USER = 0.3;
    public static final double WEIGHT_CF_OLD_USER = 0.6;
    public static final double WEIGHT_HOT = 0.1;

    /**
     * 缓存TTL配置（秒）
     */
    public static final int CACHE_TTL_USER_PROFILE = 3600;
    public static final int CACHE_TTL_RECOMMEND_RESULT = 1800;
    public static final int CACHE_TTL_HOT_CONTENT = 3600;
    public static final int CACHE_TTL_CONTENT_FEATURE = 86400;

    /**
     * Redis Key前缀
     */
    public static final String REDIS_KEY_USER_PROFILE = "user_profile:";
    public static final String REDIS_KEY_RECOMMEND = "recommend:";
    public static final String REDIS_KEY_HOT_CONTENT = "hot_content:";
    public static final String REDIS_KEY_CONTENT_FEATURE = "content_feature:";
    public static final String REDIS_KEY_USER_BEHAVIOR_QUEUE = "user_behavior_queue";

    private RecommendConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
