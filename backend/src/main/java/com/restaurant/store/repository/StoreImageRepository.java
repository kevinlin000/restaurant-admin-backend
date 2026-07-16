package com.restaurant.store.repository;

import com.restaurant.store.entity.StoreImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreImageRepository extends JpaRepository<StoreImage, Long> {
    List<StoreImage> findByStoreIdOrderBySortOrderAsc(Long storeId);
    Optional<StoreImage> findByImageIdAndStoreId(Long imageId, Long storeId);
}
