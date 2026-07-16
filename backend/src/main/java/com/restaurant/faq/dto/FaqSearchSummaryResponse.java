package com.restaurant.faq.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FaqSearchSummaryResponse {
    private String windowLabel;
    private Long totalSearches;
    private Long missedSearches;
    private Long uniqueQueries;
    private Double hitRate;
}
