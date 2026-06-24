package com.restaurant.news.service;

import com.restaurant.news.dto.NewsArticleRequest;
import com.restaurant.news.dto.NewsArticleResponse;
import com.restaurant.news.entity.NewsCategory;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface NewsArticleService {

    List<NewsArticleResponse> getPublishedNews(NewsCategory category);

    NewsArticleResponse getPublishedNewsDetail(Long newsId);

    List<NewsArticleResponse> getAdminNews(Authentication authentication);

    NewsArticleResponse getAdminNewsDetail(Long newsId, Authentication authentication);

    NewsArticleResponse createNews(NewsArticleRequest request, Authentication authentication);

    NewsArticleResponse updateNews(Long newsId, NewsArticleRequest request, Authentication authentication);

    void deleteNews(Long newsId, Authentication authentication);
}
