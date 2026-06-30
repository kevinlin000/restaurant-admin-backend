package com.restaurant.faq.service.impl;

import com.restaurant.faq.dto.FaqSearchAnalyticsResponse;
import com.restaurant.faq.dto.FaqSearchLogResponse;
import com.restaurant.faq.dto.FaqSearchQueryStatResponse;
import com.restaurant.faq.dto.FaqSearchSummaryResponse;
import com.restaurant.faq.entity.FaqSearchLog;
import com.restaurant.faq.repository.FaqSearchLogRepository;
import com.restaurant.faq.service.FaqSearchAnalyticsService;
import com.restaurant.store.service.StoreAdminAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FaqSearchAnalyticsServiceImpl implements FaqSearchAnalyticsService {

    private static final int ANALYTICS_WINDOW_DAYS = 30;
    private static final int QUERY_STAT_LIMIT = 8;

    private final FaqSearchLogRepository faqSearchLogRepository;
    private final StoreAdminAccessService storeAdminAccessService;

    @Override
    @Transactional(readOnly = true)
    public FaqSearchAnalyticsResponse getAnalytics(Authentication authentication) {
        storeAdminAccessService.requireAdmin(authentication);

        LocalDateTime since = LocalDateTime.now().minusDays(ANALYTICS_WINDOW_DAYS);
        long totalSearches = faqSearchLogRepository.countByCreatedAtAfter(since);
        long missedSearches = faqSearchLogRepository.countByMatchedFalseAndCreatedAtAfter(since);
        long uniqueQueries = faqSearchLogRepository.countDistinctQueriesSince(since);

        return FaqSearchAnalyticsResponse.builder()
                .summary(FaqSearchSummaryResponse.builder()
                        .windowLabel("近 30 天")
                        .totalSearches(totalSearches)
                        .missedSearches(missedSearches)
                        .uniqueQueries(uniqueQueries)
                        .hitRate(calculateHitRate(totalSearches, missedSearches))
                        .build())
                .popularQueries(toQueryStats(faqSearchLogRepository.findPopularQueries(
                        since,
                        PageRequest.of(0, QUERY_STAT_LIMIT))))
                .missedQueries(toQueryStats(faqSearchLogRepository.findMissedQueries(
                        since,
                        PageRequest.of(0, QUERY_STAT_LIMIT))))
                .recentLogs(faqSearchLogRepository.findTop50ByOrderByCreatedAtDesc()
                        .stream()
                        .map(this::toLogResponse)
                        .toList())
                .build();
    }

    private Double calculateHitRate(long totalSearches, long missedSearches) {
        if (totalSearches == 0) {
            return 0.0;
        }
        double hitRate = (totalSearches - missedSearches) * 100.0 / totalSearches;
        return Math.round(hitRate * 10.0) / 10.0;
    }

    private List<FaqSearchQueryStatResponse> toQueryStats(List<Object[]> rows) {
        return rows.stream()
                .map(row -> FaqSearchQueryStatResponse.builder()
                        .query((String) row[0])
                        .searchCount((Long) row[1])
                        .lastSearchedAt((LocalDateTime) row[2])
                        .build())
                .toList();
    }

    private FaqSearchLogResponse toLogResponse(FaqSearchLog log) {
        return FaqSearchLogResponse.builder()
                .logId(log.getLogId())
                .queryText(log.getQueryText())
                .matched(Boolean.TRUE.equals(log.getMatched()))
                .resultCount(log.getResultCount())
                .topFaqId(log.getTopFaqId())
                .topQuestion(log.getTopQuestion())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
