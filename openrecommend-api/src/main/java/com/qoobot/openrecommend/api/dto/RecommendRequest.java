package com.qoobot.openrecommend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 推荐请求DTO
 *
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@Schema(description = "推荐请求")
public class RecommendRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true, example = "1001")
    private Long userId;

    @NotBlank(message = "内容类型不能为空")
    @Schema(description = "内容类型：article/image/video/all", required = true, example = "article")
    private String contentType;

    @Min(value = 10, message = "推荐数量最小为10")
    @Schema(description = "推荐数量", example = "20")
    private Integer limit = 20;

    @Schema(description = "设备类型：mobile/pc/tablet", example = "mobile")
    private String device;
}
