package com.restaurant.news.repository;

import com.restaurant.news.entity.NewsArticle;
import com.restaurant.news.entity.NewsCategory;
import com.restaurant.news.entity.NewsStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    List<NewsArticle> findByIsDeletedFalseOrderByIsFeaturedDescSortOrderAscPublishedAtDescNewsIdDesc();

    List<NewsArticle> findByIsDeletedFalseAndStatusOrderByIsFeaturedDescSortOrderAscPublishedAtDescNewsIdDesc(
            NewsStatus status);

    List<NewsArticle> findByIsDeletedFalseAndStatusAndCategoryOrderByIsFeaturedDescSortOrderAscPublishedAtDescNewsIdDesc(
            NewsStatus status,
            NewsCategory category);

    List<NewsArticle> findByStore_StoreIdAndIsDeletedFalseOrderByIsFeaturedDescSortOrderAscPublishedAtDescNewsIdDesc(
            Long storeId);

    Optional<NewsArticle> findByNewsIdAndIsDeletedFalse(Long newsId);
}
