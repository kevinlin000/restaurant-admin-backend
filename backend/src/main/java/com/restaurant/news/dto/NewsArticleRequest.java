package com.restaurant.news.dto;

import com.restaurant.news.entity.NewsCategory;
import com.restaurant.news.entity.NewsStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NewsArticleRequest {

    @NotNull(message = "請選擇消息分類")
    private NewsCategory category;

    @NotNull(message = "請選擇發布狀態")
    private NewsStatus status = NewsStatus.DRAFT;

    @NotBlank(message = "請輸入消息標題")
    @Size(max = 160, message = "消息標題不可超過 160 字")
    private String title;

    @NotBlank(message = "請輸入消息摘要")
    @Size(max = 320, message = "消息摘要不可超過 320 字")
    private String summary;

    private String content;

    @Size(max = 500, message = "封面圖片網址不可超過 500 字")
    private String coverImageUrl;

    @NotNull(message = "請設定發布日期")
    private LocalDate publishedAt;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long storeId;

    private Boolean isFeatured = false;

    private Integer sortOrder = 0;
}
