package com.qoobot.openrecommend.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 内容类型枚举
 *
 * @author Qoobot Team
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ContentType {

    /**
     * 文章
     */
    ARTICLE("article", "文章"),

    /**
     * 图片
     */
    IMAGE("image", "图片"),

    /**
     * 视频
     */
    VIDEO("video", "视频"),

    /**
     * 全部
     */
    ALL("all", "全部");

    private final String code;
    private final String desc;

    public static ContentType fromCode(String code) {
        for (ContentType contentType : values()) {
            if (contentType.getCode().equals(code)) {
                return contentType;
            }
        }
        throw new IllegalArgumentException("Unknown content type: " + code);
    }
}
