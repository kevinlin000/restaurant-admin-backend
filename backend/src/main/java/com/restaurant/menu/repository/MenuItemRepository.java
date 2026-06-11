package com.restaurant.menu.repository;

import com.restaurant.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    // 繼承了 JpaRepository，自動獲得萬能的增刪改查大招！
}