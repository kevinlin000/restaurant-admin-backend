package com.restaurant.faq.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FaqSearchQueryStatResponse {
    private String query;
    private Long searchCount;
    private LocalDateTime lastSearchedAt;
}
