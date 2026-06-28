package com.restaurant.faq.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FaqSearchResponse {
    private String query;
    private List<FaqItemResponse> results;
    private List<FaqItemResponse> suggestions;
}
