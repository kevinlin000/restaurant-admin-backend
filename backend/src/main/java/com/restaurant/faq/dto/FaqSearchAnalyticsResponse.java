package com.restaurant.faq.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FaqSearchAnalyticsResponse {
    private FaqSearchSummaryResponse summary;
    private List<FaqSearchQueryStatResponse> popularQueries;
    private List<FaqSearchQueryStatResponse> missedQueries;
    private List<FaqSearchLogResponse> recentLogs;
}
