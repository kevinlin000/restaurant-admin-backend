package com.restaurant.menu.repository;

import com.restaurant.menu.entity.StoreMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StoreMenuRepository extends JpaRepository<StoreMenu, Long> {
    
    // 🎯 核心大絕招：一秒撈出該分店「目前有供應 (isAvailable = true)」的所有菜單設定！
    List<StoreMenu> findByStoreIdAndIsAvailableTrue(Long storeId);
}