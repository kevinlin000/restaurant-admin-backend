package com.restaurant.faq.service.impl;

import com.restaurant.common.BusinessException;
import com.restaurant.common.ResourceNotFoundException;
import com.restaurant.faq.dto.FaqItemRequest;
import com.restaurant.faq.dto.FaqItemResponse;
import com.restaurant.faq.dto.FaqSearchResponse;
import com.restaurant.faq.entity.FaqCategory;
import com.restaurant.faq.entity.FaqItem;
import com.restaurant.faq.entity.FaqStatus;
import com.restaurant.faq.repository.FaqItemRepository;
import com.restaurant.faq.service.FaqItemService;
import com.restaurant.store.service.StoreAdminAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FaqItemServiceImpl implements FaqItemService {

    private static final int SEARCH_RESULT_LIMIT = 5;
    private static final int SEARCH_SUGGESTION_LIMIT = 4;

    private final FaqItemRepository faqItemRepository;
    private final StoreAdminAccessService storeAdminAccessService;

    @Override
    @Transactional(readOnly = true)
    public List<FaqItemResponse> getPublishedFaqs(FaqCategory category) {
        List<FaqItem> faqs = category == null
                ? faqItemRepository.findByIsDeletedFalseAndStatusOrderByIsFeaturedDescSortOrderAscFaqIdDesc(FaqStatus.PUBLISHED)
                : faqItemRepository.findByIsDeletedFalseAndStatusAndCategoryOrderByIsFeaturedDescSortOrderAscFaqIdDesc(
                FaqStatus.PUBLISHED,
                category);
        return faqs.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FaqSearchResponse searchPublishedFaqs(String query) {
        String normalizedQuery = cleanNullable(query);
        List<FaqItem> publishedFaqs =
                faqItemRepository.findByIsDeletedFalseAndStatusOrderByIsFeaturedDescSortOrderAscFaqIdDesc(FaqStatus.PUBLISHED);

        List<FaqItemResponse> suggestions = publishedFaqs.stream()
                .limit(SEARCH_SUGGESTION_LIMIT)
                .map(this::toResponse)
                .toList();

        if (normalizedQuery == null) {
            return FaqSearchResponse.builder()
                    .query("")
                    .results(List.of())
                    .suggestions(suggestions)
                    .build();
        }

        String loweredQuery = normalizedQuery.toLowerCase(Locale.ROOT);
        List<FaqItemResponse> results = publishedFaqs.stream()
                .map(faq -> new ScoredFaq(faq, score(faq, loweredQuery)))
                .filter(scoredFaq -> scoredFaq.score() > 0)
                .sorted(Comparator.comparingInt(ScoredFaq::score).reversed()
                        .thenComparing(scoredFaq -> scoredFaq.faq().getSortOrder())
                        .thenComparing(scoredFaq -> scoredFaq.faq().getFaqId(), Comparator.reverseOrder()))
                .limit(SEARCH_RESULT_LIMIT)
                .map(scoredFaq -> toResponse(scoredFaq.faq()))
                .toList();

        return FaqSearchResponse.builder()
                .query(normalizedQuery)
                .results(results)
                .suggestions(suggestions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaqItemResponse> getAdminFaqs(Authentication authentication) {
        storeAdminAccessService.requireAdmin(authentication);
        return faqItemRepository.findByIsDeletedFalseOrderByIsFeaturedDescSortOrderAscFaqIdDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FaqItemResponse getAdminFaqDetail(Long faqId, Authentication authentication) {
        storeAdminAccessService.requireAdmin(authentication);
        return toResponse(getFaqOrThrow(faqId));
    }

    @Override
    @Transactional
    public FaqItemResponse createFaq(FaqItemRequest request, Authentication authentication) {
        storeAdminAccessService.requireAdmin(authentication);

        FaqItem faq = FaqItem.builder()
                .category(request.getCategory())
                .status(request.getStatus())
                .question(cleanRequired(request.getQuestion()))
                .answer(cleanRequired(request.getAnswer()))
                .keywords(cleanNullable(request.getKeywords()))
                .isFeatured(Boolean.TRUE.equals(request.getIsFeatured()))
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .isDeleted(false)
                .build();

        return toResponse(faqItemRepository.save(faq));
    }

    @Override
    @Transactional
    public FaqItemResponse updateFaq(Long faqId, FaqItemRequest request, Authentication authentication) {
        storeAdminAccessService.requireAdmin(authentication);

        FaqItem faq = getFaqOrThrow(faqId);
        faq.setCategory(request.getCategory());
        faq.setStatus(request.getStatus());
        faq.setQuestion(cleanRequired(request.getQuestion()));
        faq.setAnswer(cleanRequired(request.getAnswer()));
        faq.setKeywords(cleanNullable(request.getKeywords()));
        faq.setIsFeatured(Boolean.TRUE.equals(request.getIsFeatured()));
        faq.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());

        return toResponse(faqItemRepository.save(faq));
    }

    @Override
    @Transactional
    public void deleteFaq(Long faqId, Authentication authentication) {
        storeAdminAccessService.requireAdmin(authentication);
        FaqItem faq = getFaqOrThrow(faqId);
        faq.setIsDeleted(true);
        faqItemRepository.save(faq);
    }

    private int score(FaqItem faq, String loweredQuery) {
        int score = 0;
        String question = lower(faq.getQuestion());
        String answer = lower(faq.getAnswer());
        String keywords = lower(faq.getKeywords());

        if (question.contains(loweredQuery)) score += 10;
        if (keywords.contains(loweredQuery)) score += 7;
        if (answer.contains(loweredQuery)) score += 3;

        for (String token : loweredQuery.split("\\s+")) {
            if (token.isBlank()) continue;
            if (question.contains(token)) score += 4;
            if (keywords.contains(token)) score += 3;
            if (answer.contains(token)) score += 1;
        }

        for (String keywordToken : keywords.split("[,，、\\s]+")) {
            if (keywordToken.isBlank()) continue;
            if (loweredQuery.contains(keywordToken)) {
                score += 4;
                if (question.contains(keywordToken)) score += 3;
            }
        }

        if (score > 0 && Boolean.TRUE.equals(faq.getIsFeatured())) score += 1;
        return score;
    }

    private FaqItem getFaqOrThrow(Long faqId) {
        return faqItemRepository.findByFaqIdAndIsDeletedFalse(faqId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到此常見問題"));
    }

    private FaqItemResponse toResponse(FaqItem faq) {
        return FaqItemResponse.builder()
                .faqId(faq.getFaqId())
                .category(faq.getCategory())
                .categoryLabel(categoryLabel(faq.getCategory()))
                .status(faq.getStatus())
                .statusLabel(statusLabel(faq.getStatus()))
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .keywords(faq.getKeywords())
                .isFeatured(Boolean.TRUE.equals(faq.getIsFeatured()))
                .sortOrder(faq.getSortOrder())
                .createdAt(faq.getCreatedAt())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }

    private String categoryLabel(FaqCategory category) {
        return switch (category) {
            case RESERVATION -> "訂位規則";
            case DEPOSIT -> "訂金與取消";
            case ORDER -> "點餐與外帶";
            case PAYMENT -> "付款與發票";
            case STORE -> "門市資訊";
            case MEMBER -> "會員服務";
            case SERVICE -> "用餐服務";
        };
    }

    private String statusLabel(FaqStatus status) {
        return switch (status) {
            case DRAFT -> "草稿";
            case PUBLISHED -> "已發布";
            case ARCHIVED -> "已封存";
        };
    }

    private String cleanRequired(String value) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.isBlank()) {
            throw new BusinessException("FAQ 欄位不可空白");
        }
        return cleaned;
    }

    private String cleanNullable(String value) {
        String cleaned = value == null ? null : value.trim();
        return cleaned == null || cleaned.isBlank() ? null : cleaned;
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private record ScoredFaq(FaqItem faq, int score) {
    }
}
