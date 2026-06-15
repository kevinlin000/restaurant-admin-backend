package com.restaurant.store.repository;

import com.restaurant.store.entity.StoreFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreFeatureRepository extends JpaRepository<StoreFeature, Long> {
    List<StoreFeature> findByStoreIdOrderBySortOrderAscFeatureIdAsc(Long storeId);
    List<StoreFeature> findByStoreIdInOrderByStoreIdAscSortOrderAscFeatureIdAsc(List<Long> storeIds);
    Optional<StoreFeature> findByFeatureIdAndStoreId(Long featureId, Long storeId);
    Optional<StoreFeature> findByStoreIdAndFeatureKey(Long storeId, String featureKey);
}
