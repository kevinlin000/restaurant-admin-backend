package com.restaurant.news.dto;

import com.restaurant.news.entity.NewsCategory;
import com.restaurant.news.entity.NewsStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class NewsArticleResponse {

    private Long newsId;
    private NewsCategory category;
    private String categoryLabel;
    private NewsStatus status;
    private String statusLabel;
    private String title;
    private String summary;
    private String content;
    private String coverImageUrl;
    private LocalDate publishedAt;
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodLabel;
    private Long storeId;
    private String storeName;
    private String storeScope;
    private Boolean isFeatured;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
