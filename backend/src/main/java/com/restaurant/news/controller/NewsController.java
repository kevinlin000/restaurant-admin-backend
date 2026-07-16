package com.restaurant.news.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.news.dto.NewsArticleResponse;
import com.restaurant.news.entity.NewsCategory;
import com.restaurant.news.service.NewsArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
@Tag(name = "前台最新消息", description = "顧客端消息查詢")
public class NewsController {

    private final NewsArticleService newsArticleService;

    @GetMapping
    @Operation(summary = "取得已發布最新消息")
    public ApiResponse<List<NewsArticleResponse>> getPublishedNews(
            @RequestParam(required = false) NewsCategory category) {
        return ApiResponse.success(newsArticleService.getPublishedNews(category));
    }

    @GetMapping("/{newsId}")
    @Operation(summary = "取得已發布消息詳情")
    public ApiResponse<NewsArticleResponse> getPublishedNewsDetail(@PathVariable Long newsId) {
        return ApiResponse.success(newsArticleService.getPublishedNewsDetail(newsId));
    }
}
