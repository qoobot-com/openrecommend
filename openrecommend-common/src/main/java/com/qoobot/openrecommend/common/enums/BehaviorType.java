package com.qoobot.openrecommend.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 行为类型枚举
 *
 * @author Qoobot Team
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum BehaviorType {

    /**
     * 浏览
     */
    VIEW(1, "浏览"),

    /**
     * 点赞
     */
    LIKE(2, "点赞"),

    /**
     * 收藏
     */
    COLLECT(3, "收藏"),

    /**
     * 分享
     */
    SHARE(4, "分享"),

    /**
     * 评论
     */
    COMMENT(5, "评论");

    private final Integer code;
    private final String desc;

    public static BehaviorType fromCode(Integer code) {
        for (BehaviorType behaviorType : values()) {
            if (behaviorType.getCode().equals(code)) {
                return behaviorType;
            }
        }
        throw new IllegalArgumentException("Unknown behavior type: " + code);
    }
}
