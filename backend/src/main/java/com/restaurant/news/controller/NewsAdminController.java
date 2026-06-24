package com.restaurant.news.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.news.dto.NewsArticleRequest;
import com.restaurant.news.dto.NewsArticleResponse;
import com.restaurant.news.service.NewsArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/news")
@Tag(name = "後台最新消息管理", description = "最新消息 CRUD")
public class NewsAdminController {

    private final NewsArticleService newsArticleService;

    @GetMapping
    @Operation(summary = "取得後台最新消息清單")
    public ApiResponse<List<NewsArticleResponse>> getAdminNews(Authentication authentication) {
        return ApiResponse.success(newsArticleService.getAdminNews(authentication));
    }

    @GetMapping("/{newsId}")
    @Operation(summary = "取得後台最新消息詳情")
    public ApiResponse<NewsArticleResponse> getAdminNewsDetail(
            @PathVariable Long newsId,
            Authentication authentication) {
        return ApiResponse.success(newsArticleService.getAdminNewsDetail(newsId, authentication));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "新增最新消息")
    public ApiResponse<NewsArticleResponse> createNews(
            @Valid @RequestBody NewsArticleRequest request,
            Authentication authentication) {
        return ApiResponse.success("最新消息已新增", newsArticleService.createNews(request, authentication));
    }

    @PutMapping("/{newsId}")
    @Operation(summary = "更新最新消息")
    public ApiResponse<NewsArticleResponse> updateNews(
            @PathVariable Long newsId,
            @Valid @RequestBody NewsArticleRequest request,
            Authentication authentication) {
        return ApiResponse.success("最新消息已更新", newsArticleService.updateNews(newsId, request, authentication));
    }

    @DeleteMapping("/{newsId}")
    @Operation(summary = "刪除最新消息")
    public ApiResponse<Void> deleteNews(@PathVariable Long newsId, Authentication authentication) {
        newsArticleService.deleteNews(newsId, authentication);
        return ApiResponse.success("最新消息已刪除");
    }
}
