package com.restaurant.faq.repository;

import com.restaurant.faq.entity.FaqCategory;
import com.restaurant.faq.entity.FaqItem;
import com.restaurant.faq.entity.FaqStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FaqItemRepository extends JpaRepository<FaqItem, Long> {

    List<FaqItem> findByIsDeletedFalseOrderByIsFeaturedDescSortOrderAscFaqIdDesc();

    List<FaqItem> findByIsDeletedFalseAndStatusOrderByIsFeaturedDescSortOrderAscFaqIdDesc(FaqStatus status);

    List<FaqItem> findByIsDeletedFalseAndStatusAndCategoryOrderByIsFeaturedDescSortOrderAscFaqIdDesc(
            FaqStatus status,
            FaqCategory category);

    Optional<FaqItem> findByFaqIdAndIsDeletedFalse(Long faqId);
}
