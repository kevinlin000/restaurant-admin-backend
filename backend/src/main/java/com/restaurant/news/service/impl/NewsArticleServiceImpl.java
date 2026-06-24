package com.restaurant.news.service.impl;

import com.restaurant.common.BusinessException;
import com.restaurant.common.ResourceNotFoundException;
import com.restaurant.news.dto.NewsArticleRequest;
import com.restaurant.news.dto.NewsArticleResponse;
import com.restaurant.news.entity.NewsArticle;
import com.restaurant.news.entity.NewsCategory;
import com.restaurant.news.entity.NewsStatus;
import com.restaurant.news.repository.NewsArticleRepository;
import com.restaurant.news.service.NewsArticleService;
import com.restaurant.store.entity.Store;
import com.restaurant.store.repository.StoreRepository;
import com.restaurant.store.service.StoreAdminAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsArticleServiceImpl implements NewsArticleService {

    private final NewsArticleRepository newsArticleRepository;
    private final StoreRepository storeRepository;
    private final StoreAdminAccessService storeAdminAccessService;

    @Override
    @Transactional(readOnly = true)
    public List<NewsArticleResponse> getPublishedNews(NewsCategory category) {
        List<NewsArticle> articles = category == null
                ? newsArticleRepository.findByIsDeletedFalseAndStatusOrderByIsFeaturedDescSortOrderAscPublishedAtDescNewsIdDesc(NewsStatus.PUBLISHED)
                : newsArticleRepository.findByIsDeletedFalseAndStatusAndCategoryOrderByIsFeaturedDescSortOrderAscPublishedAtDescNewsIdDesc(NewsStatus.PUBLISHED, category);
        return articles.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NewsArticleResponse getPublishedNewsDetail(Long newsId) {
        NewsArticle article = getNewsOrThrow(newsId);
        if (article.getStatus() != NewsStatus.PUBLISHED) {
            throw new ResourceNotFoundException("找不到此最新消息");
        }
        return toResponse(article);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsArticleResponse> getAdminNews(Authentication authentication) {
        if (storeAdminAccessService.isAdmin(authentication)) {
            return newsArticleRepository
                    .findByIsDeletedFalseOrderByIsFeaturedDescSortOrderAscPublishedAtDescNewsIdDesc()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        Long storeId = storeAdminAccessService.currentManagerStoreId(authentication);
        return newsArticleRepository
                .findByStore_StoreIdAndIsDeletedFalseOrderByIsFeaturedDescSortOrderAscPublishedAtDescNewsIdDesc(storeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NewsArticleResponse getAdminNewsDetail(Long newsId, Authentication authentication) {
        NewsArticle article = getNewsOrThrow(newsId);
        requireManageAccess(authentication, article);
        return toResponse(article);
    }

    @Override
    @Transactional
    public NewsArticleResponse createNews(NewsArticleRequest request, Authentication authentication) {
        validatePeriod(request.getStartDate(), request.getEndDate());

        Store store = resolveStoreForWrite(request.getStoreId(), authentication);
        NewsArticle article = NewsArticle.builder()
                .category(request.getCategory())
                .status(request.getStatus())
                .title(clean(request.getTitle()))
                .summary(clean(request.getSummary()))
                .content(cleanNullable(request.getContent()))
                .coverImageUrl(cleanNullable(request.getCoverImageUrl()))
                .publishedAt(request.getPublishedAt())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .store(store)
                .isFeatured(Boolean.TRUE.equals(request.getIsFeatured()))
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .isDeleted(false)
                .build();

        return toResponse(newsArticleRepository.save(article));
    }

    @Override
    @Transactional
    public NewsArticleResponse updateNews(Long newsId, NewsArticleRequest request, Authentication authentication) {
        validatePeriod(request.getStartDate(), request.getEndDate());

        NewsArticle article = getNewsOrThrow(newsId);
        requireManageAccess(authentication, article);
        Store store = resolveStoreForWrite(request.getStoreId(), authentication);

        article.setCategory(request.getCategory());
        article.setStatus(request.getStatus());
        article.setTitle(clean(request.getTitle()));
        article.setSummary(clean(request.getSummary()));
        article.setContent(cleanNullable(request.getContent()));
        article.setCoverImageUrl(cleanNullable(request.getCoverImageUrl()));
        article.setPublishedAt(request.getPublishedAt());
        article.setStartDate(request.getStartDate());
        article.setEndDate(request.getEndDate());
        article.setStore(store);
        article.setIsFeatured(Boolean.TRUE.equals(request.getIsFeatured()));
        article.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());

        return toResponse(newsArticleRepository.save(article));
    }

    @Override
    @Transactional
    public void deleteNews(Long newsId, Authentication authentication) {
        NewsArticle article = getNewsOrThrow(newsId);
        requireManageAccess(authentication, article);
        article.setIsDeleted(true);
        newsArticleRepository.save(article);
    }

    private NewsArticle getNewsOrThrow(Long newsId) {
        return newsArticleRepository.findByNewsIdAndIsDeletedFalse(newsId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到此最新消息"));
    }

    private Store resolveStoreForWrite(Long storeId, Authentication authentication) {
        if (storeAdminAccessService.isAdmin(authentication)) {
            if (storeId == null) {
                return null;
            }
            return storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                    .orElseThrow(() -> new ResourceNotFoundException("找不到指定門市"));
        }

        Long managerStoreId = storeAdminAccessService.currentManagerStoreId(authentication);
        Long targetStoreId = storeId == null ? managerStoreId : storeId;
        if (!managerStoreId.equals(targetStoreId)) {
            throw new AccessDeniedException("只能管理自己所屬門市的最新消息");
        }
        return storeRepository.findByStoreIdAndIsDeletedFalse(targetStoreId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到指定門市"));
    }

    private void requireManageAccess(Authentication authentication, NewsArticle article) {
        if (storeAdminAccessService.isAdmin(authentication)) {
            return;
        }
        if (article.getStore() == null) {
            throw new AccessDeniedException("全品牌公告僅限系統管理員管理");
        }
        storeAdminAccessService.requireStoreAccess(authentication, article.getStore().getStoreId());
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException("活動結束日期不可早於開始日期");
        }
    }

    private NewsArticleResponse toResponse(NewsArticle article) {
        Store store = article.getStore();
        return NewsArticleResponse.builder()
                .newsId(article.getNewsId())
                .category(article.getCategory())
                .categoryLabel(categoryLabel(article.getCategory()))
                .status(article.getStatus())
                .statusLabel(statusLabel(article.getStatus()))
                .title(article.getTitle())
                .summary(article.getSummary())
                .content(article.getContent())
                .coverImageUrl(article.getCoverImageUrl())
                .publishedAt(article.getPublishedAt())
                .startDate(article.getStartDate())
                .endDate(article.getEndDate())
                .periodLabel(periodLabel(article.getStartDate(), article.getEndDate()))
                .storeId(store == null ? null : store.getStoreId())
                .storeName(store == null ? null : store.getStoreName())
                .storeScope(store == null ? "全門市適用" : store.getStoreName())
                .isFeatured(Boolean.TRUE.equals(article.getIsFeatured()))
                .sortOrder(article.getSortOrder())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }

    private String periodLabel(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return "長期公告";
        }
        if (startDate != null && endDate == null) {
            return startDate + " 起";
        }
        if (startDate == null) {
            return endDate + " 止";
        }
        return startDate + " - " + endDate;
    }

    private String categoryLabel(NewsCategory category) {
        return switch (category) {
            case EVENT -> "活動";
            case NOTICE -> "公告";
            case OPENING -> "展店";
            case MEMBER -> "會員";
        };
    }

    private String statusLabel(NewsStatus status) {
        return switch (status) {
            case DRAFT -> "草稿";
            case PUBLISHED -> "已發布";
            case ARCHIVED -> "已封存";
        };
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private String cleanNullable(String value) {
        String cleaned = clean(value);
        return cleaned == null || cleaned.isBlank() ? null : cleaned;
    }
}
