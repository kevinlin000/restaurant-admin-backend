package com.restaurant.faq.service.impl;

import com.restaurant.common.BusinessException;
import com.restaurant.faq.dto.FaqItemRequest;
import com.restaurant.faq.dto.FaqItemResponse;
import com.restaurant.faq.dto.FaqSearchResponse;
import com.restaurant.faq.entity.FaqCategory;
import com.restaurant.faq.entity.FaqItem;
import com.restaurant.faq.entity.FaqStatus;
import com.restaurant.faq.repository.FaqItemRepository;
import com.restaurant.store.service.StoreAdminAccessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FaqItemServiceImplTest {

    @Mock
    private FaqItemRepository faqItemRepository;

    @Mock
    private StoreAdminAccessService storeAdminAccessService;

    @InjectMocks
    private FaqItemServiceImpl faqItemService;

    @Test
    void getPublishedFaqsReturnsOnlyPublishedItems() {
        when(faqItemRepository.findByIsDeletedFalseAndStatusOrderByIsFeaturedDescSortOrderAscFaqIdDesc(FaqStatus.PUBLISHED))
                .thenReturn(List.of(faq(1L, "訂位需要訂金嗎？", "部分時段需要支付訂金", FaqCategory.DEPOSIT, FaqStatus.PUBLISHED, true)));

        List<FaqItemResponse> result = faqItemService.getPublishedFaqs(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryLabel()).isEqualTo("訂金與取消");
        assertThat(result.get(0).getStatusLabel()).isEqualTo("已發布");
    }

    @Test
    void searchRanksQuestionAndKeywordMatchesBeforeAnswerOnlyMatches() {
        when(faqItemRepository.findByIsDeletedFalseAndStatusOrderByIsFeaturedDescSortOrderAscFaqIdDesc(FaqStatus.PUBLISHED))
                .thenReturn(List.of(
                        faq(1L, "取消訂位後訂金可以退嗎？", "符合取消期限可退款", FaqCategory.DEPOSIT, FaqStatus.PUBLISHED, true),
                        faq(2L, "付款方式有哪些？", "訂金與尾款可依頁面指示付款", FaqCategory.PAYMENT, FaqStatus.PUBLISHED, false),
                        faq(3L, "如何查詢門市？", "可查詢地址與交通方式", FaqCategory.STORE, FaqStatus.PUBLISHED, false),
                        faq(4L, "可以先線上點餐嗎？", "外帶訂單可不指定桌位", FaqCategory.ORDER, FaqStatus.PUBLISHED, true)
                ));

        FaqSearchResponse result = faqItemService.searchPublishedFaqs("訂金");

        assertThat(result.getResults()).extracting(FaqItemResponse::getFaqId).containsExactly(1L, 2L);
        assertThat(result.getSuggestions()).hasSize(4);
    }

    @Test
    void adminCanCreateFaq() {
        Authentication auth = adminAuth();
        when(faqItemRepository.save(any(FaqItem.class))).thenAnswer(invocation -> {
            FaqItem faq = invocation.getArgument(0);
            faq.setFaqId(9L);
            return faq;
        });

        FaqItemResponse result = faqItemService.createFaq(request(), auth);

        verify(storeAdminAccessService).requireAdmin(auth);
        assertThat(result.getFaqId()).isEqualTo(9L);
        assertThat(result.getQuestion()).isEqualTo("訂位需要訂金嗎？");
        assertThat(result.getIsFeatured()).isTrue();
    }

    @Test
    void updateTrimsTextAndKeepsStatus() {
        Authentication auth = adminAuth();
        FaqItem faq = faq(3L, "舊問題", "舊回答", FaqCategory.ORDER, FaqStatus.DRAFT, false);
        when(faqItemRepository.findByFaqIdAndIsDeletedFalse(3L)).thenReturn(Optional.of(faq));
        when(faqItemRepository.save(any(FaqItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FaqItemRequest request = request();
        request.setQuestion("  新問題  ");
        request.setStatus(FaqStatus.PUBLISHED);

        FaqItemResponse result = faqItemService.updateFaq(3L, request, auth);

        assertThat(result.getQuestion()).isEqualTo("新問題");
        assertThat(result.getStatus()).isEqualTo(FaqStatus.PUBLISHED);
    }

    @Test
    void createRejectsBlankQuestion() {
        FaqItemRequest request = request();
        request.setQuestion(" ");

        assertThatThrownBy(() -> faqItemService.createFaq(request, adminAuth()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("FAQ 欄位不可空白");
    }

    private static FaqItemRequest request() {
        FaqItemRequest request = new FaqItemRequest();
        request.setCategory(FaqCategory.DEPOSIT);
        request.setStatus(FaqStatus.PUBLISHED);
        request.setQuestion("訂位需要訂金嗎？");
        request.setAnswer("熱門餐期或大人數訂位可能需要訂金。");
        request.setKeywords("訂金,預約,付款");
        request.setIsFeatured(true);
        request.setSortOrder(10);
        return request;
    }

    private static FaqItem faq(Long id, String question, String answer, FaqCategory category, FaqStatus status, boolean featured) {
        return FaqItem.builder()
                .faqId(id)
                .category(category)
                .status(status)
                .question(question)
                .answer(answer)
                .keywords(question + "," + answer)
                .isFeatured(featured)
                .sortOrder(id.intValue())
                .isDeleted(false)
                .build();
    }

    private static Authentication adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}
