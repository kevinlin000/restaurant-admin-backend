package com.restaurant.faq.service;

import com.restaurant.faq.dto.FaqSearchAnalyticsResponse;
import org.springframework.security.core.Authentication;

public interface FaqSearchAnalyticsService {

    FaqSearchAnalyticsResponse getAnalytics(Authentication authentication);
}
