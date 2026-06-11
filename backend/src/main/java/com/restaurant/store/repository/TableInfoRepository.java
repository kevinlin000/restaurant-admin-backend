package com.restaurant.store.repository;

import com.restaurant.store.entity.TableInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TableInfoRepository extends JpaRepository<TableInfo, Long> {
    List<TableInfo> findByStoreId(Long storeId);
    List<TableInfo> findByStoreIdAndStatus(Long storeId, String status);
    boolean existsByStoreIdAndTableNumber(Long storeId, String tableNumber);
    Optional<TableInfo> findByTableIdAndStoreId(Long tableId, Long storeId);
}
