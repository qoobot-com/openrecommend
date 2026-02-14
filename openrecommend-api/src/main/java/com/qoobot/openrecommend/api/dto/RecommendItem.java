package com.qoobot.openrecommend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推荐项DTO
 *
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "推荐项")
public class RecommendItem {

    @Schema(description = "内容ID", example = "1001")
    private Long contentId;

    @Schema(description = "内容类型", example = "article")
    private String contentType;

    @Schema(description = "推荐得分", example = "0.95")
    private Double score;

    @Schema(description = "推荐理由", example = "基于您的兴趣推荐")
    private String reason;
}
