package com.restaurant.store.repository;

import com.restaurant.store.entity.StoreFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreFeatureRepository extends JpaRepository<StoreFeature, Long> {
    List<StoreFeature> findByStoreIdOrderBySortOrderAscFeatureIdAsc(Long storeId);
    List<StoreFeature> findByStoreIdInOrderByStoreIdAscSortOrderAscFeatureIdAsc(List<Long> storeIds);
}
