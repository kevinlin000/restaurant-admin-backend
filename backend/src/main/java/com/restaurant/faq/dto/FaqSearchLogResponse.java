package com.restaurant.faq.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FaqSearchLogResponse {
    private Long logId;
    private String queryText;
    private Boolean matched;
    private Integer resultCount;
    private Long topFaqId;
    private String topQuestion;
    private LocalDateTime createdAt;
}
