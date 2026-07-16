package com.restaurant.news.service.impl;

import com.restaurant.common.BusinessException;
import com.restaurant.news.dto.NewsArticleRequest;
import com.restaurant.news.dto.NewsArticleResponse;
import com.restaurant.news.entity.NewsArticle;
import com.restaurant.news.entity.NewsCategory;
import com.restaurant.news.entity.NewsStatus;
import com.restaurant.news.repository.NewsArticleRepository;
import com.restaurant.store.entity.Store;
import com.restaurant.store.repository.StoreRepository;
import com.restaurant.store.service.StoreAdminAccessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsArticleServiceImplTest {

    @Mock
    private NewsArticleRepository newsArticleRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreAdminAccessService storeAdminAccessService;

    @InjectMocks
    private NewsArticleServiceImpl newsArticleService;

    @Test
    void getPublishedNewsReturnsOnlyPublishedArticles() {
        when(newsArticleRepository.findByIsDeletedFalseAndStatusOrderByIsFeaturedDescSortOrderAscPublishedAtDescNewsIdDesc(
                NewsStatus.PUBLISHED))
                .thenReturn(List.of(article(1L, "夏旬和食祭", NewsStatus.PUBLISHED, null)));

        List<NewsArticleResponse> result = newsArticleService.getPublishedNews(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("夏旬和食祭");
        assertThat(result.get(0).getStatusLabel()).isEqualTo("已發布");
        assertThat(result.get(0).getStoreScope()).isEqualTo("全門市適用");
    }

    @Test
    void adminCanCreateBrandWideNews() {
        Authentication auth = adminAuth();
        when(storeAdminAccessService.isAdmin(auth)).thenReturn(true);
        when(newsArticleRepository.save(any(NewsArticle.class))).thenAnswer(invocation -> {
            NewsArticle article = invocation.getArgument(0);
            article.setNewsId(10L);
            return article;
        });

        NewsArticleResponse result = newsArticleService.createNews(request(null), auth);

        assertThat(result.getNewsId()).isEqualTo(10L);
        assertThat(result.getStoreId()).isNull();
        assertThat(result.getStoreScope()).isEqualTo("全門市適用");
    }

    @Test
    void managerCreateNewsIsScopedToOwnStoreWhenStoreIdIsMissing() {
        Authentication auth = managerAuth();
        Store store = store(2L);
        when(storeAdminAccessService.isAdmin(auth)).thenReturn(false);
        when(storeAdminAccessService.currentManagerStoreId(auth)).thenReturn(2L);
        when(storeRepository.findByStoreIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(store));
        when(newsArticleRepository.save(any(NewsArticle.class))).thenAnswer(invocation -> {
            NewsArticle article = invocation.getArgument(0);
            article.setNewsId(11L);
            return article;
        });

        NewsArticleResponse result = newsArticleService.createNews(request(null), auth);

        assertThat(result.getStoreId()).isEqualTo(2L);
        assertThat(result.getStoreScope()).isEqualTo("敘日信義店");
    }

    @Test
    void managerCannotCreateNewsForOtherStore() {
        Authentication auth = managerAuth();
        when(storeAdminAccessService.isAdmin(auth)).thenReturn(false);
        when(storeAdminAccessService.currentManagerStoreId(auth)).thenReturn(2L);

        assertThatThrownBy(() -> newsArticleService.createNews(request(3L), auth))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("只能管理自己所屬門市");
    }

    @Test
    void managerCannotUpdateOtherStoreNews() {
        Authentication auth = managerAuth();
        when(newsArticleRepository.findByNewsIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(article(1L, "門市公告", NewsStatus.DRAFT, store(3L))));
        when(storeAdminAccessService.isAdmin(auth)).thenReturn(false);

        assertThatThrownBy(() -> newsArticleService.updateNews(1L, request(2L), auth))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectsInvalidPeriod() {
        Authentication auth = adminAuth();
        NewsArticleRequest request = request(null);
        request.setStartDate(LocalDate.of(2026, 8, 1));
        request.setEndDate(LocalDate.of(2026, 7, 1));

        assertThatThrownBy(() -> newsArticleService.createNews(request, auth))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("結束日期不可早於開始日期");
    }

    private static NewsArticleRequest request(Long storeId) {
        NewsArticleRequest request = new NewsArticleRequest();
        request.setCategory(NewsCategory.EVENT);
        request.setStatus(NewsStatus.PUBLISHED);
        request.setTitle(" 夏旬和食祭 ");
        request.setSummary("限定菜單上市");
        request.setContent("活動內容");
        request.setPublishedAt(LocalDate.of(2026, 6, 24));
        request.setStartDate(LocalDate.of(2026, 6, 24));
        request.setEndDate(LocalDate.of(2026, 8, 31));
        request.setStoreId(storeId);
        request.setIsFeatured(true);
        request.setSortOrder(1);
        return request;
    }

    private static NewsArticle article(Long id, String title, NewsStatus status, Store store) {
        return NewsArticle.builder()
                .newsId(id)
                .category(NewsCategory.EVENT)
                .status(status)
                .title(title)
                .summary("摘要")
                .publishedAt(LocalDate.of(2026, 6, 24))
                .startDate(LocalDate.of(2026, 6, 24))
                .endDate(LocalDate.of(2026, 8, 31))
                .store(store)
                .isFeatured(true)
                .sortOrder(1)
                .isDeleted(false)
                .build();
    }

    private static Store store(Long storeId) {
        return Store.builder()
                .storeId(storeId)
                .storeName("敘日信義店")
                .isDeleted(false)
                .build();
    }

    private static Authentication adminAuth() {
        return auth(1L, "ROLE_ADMIN");
    }

    private static Authentication managerAuth() {
        return auth(2L, "ROLE_MANAGER");
    }

    private static Authentication auth(Long userId, String authority) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority(authority))
        );
    }
}
