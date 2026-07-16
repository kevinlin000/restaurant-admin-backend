package com.restaurant.faq.service;

import com.restaurant.faq.dto.FaqItemRequest;
import com.restaurant.faq.dto.FaqItemResponse;
import com.restaurant.faq.dto.FaqSearchResponse;
import com.restaurant.faq.entity.FaqCategory;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface FaqItemService {

    List<FaqItemResponse> getPublishedFaqs(FaqCategory category);

    FaqSearchResponse searchPublishedFaqs(String query);

    List<FaqItemResponse> getAdminFaqs(Authentication authentication);

    FaqItemResponse getAdminFaqDetail(Long faqId, Authentication authentication);

    FaqItemResponse createFaq(FaqItemRequest request, Authentication authentication);

    FaqItemResponse updateFaq(Long faqId, FaqItemRequest request, Authentication authentication);

    void deleteFaq(Long faqId, Authentication authentication);
}
