package com.restaurant.faq.dto;

import com.restaurant.faq.entity.FaqCategory;
import com.restaurant.faq.entity.FaqStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FaqItemResponse {
    private Long faqId;
    private FaqCategory category;
    private String categoryLabel;
    private FaqStatus status;
    private String statusLabel;
    private String question;
    private String answer;
    private String keywords;
    private Boolean isFeatured;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
