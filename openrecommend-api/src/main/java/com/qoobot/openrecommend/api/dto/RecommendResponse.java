package com.qoobot.openrecommend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 推荐响应DTO
 *
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "推荐响应")
public class RecommendResponse {

    @Schema(description = "推荐项列表")
    private List<RecommendItem> items;

    @Schema(description = "时间戳")
    private Long timestamp;
}
