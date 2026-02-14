package com.qoobot.openrecommend.api.controller;

import com.qoobot.openrecommend.api.dto.RecommendItem;
import com.qoobot.openrecommend.api.dto.RecommendRequest;
import com.qoobot.openrecommend.api.dto.RecommendResponse;
import com.qoobot.openrecommend.common.result.Result;
import com.qoobot.openrecommend.service.RecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 推荐控制器
 *
 * @author Qoobot Team
 * @since 1.0.0
 */
@Tag(name = "推荐服务", description = "内容推荐相关接口")
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    /**
     * 获取个性化推荐
     */
    @Operation(summary = "个性化推荐")
    @PostMapping("/personal")
    public Result<RecommendResponse> getPersonalRecommend(
            @Valid @RequestBody RecommendRequest request) {
        RecommendResponse response = recommendService.recommend(request);
        return Result.success(response);
    }

    /**
     * 获取热门内容
     */
    @Operation(summary = "热门内容推荐")
    @GetMapping("/hot/{contentType}")
    public Result<RecommendResponse> getHotContent(
            @PathVariable String contentType,
            @RequestParam(defaultValue = "20") Integer limit) {
        RecommendResponse response = recommendService.recommendHot(contentType, limit);
        return Result.success(response);
    }

    /**
     * 获取相关推荐
     */
    @Operation(summary = "相关内容推荐")
    @GetMapping("/related/{contentId}")
    public Result<RecommendResponse> getRelatedRecommend(
            @PathVariable Long contentId,
            @RequestParam(defaultValue = "10") Integer limit) {
        RecommendResponse response = recommendService.recommendRelated(contentId, limit);
        return Result.success(response);
    }
}
