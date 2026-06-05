package com.restaurant.repository;

import com.restaurant.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.stereotype.Repository; 
import java.util.List;

@Repository 
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> { // 👈 2. 規格換成 <MenuItem, Long>

    // 👈 3. 回傳的清單換成餐點清單，功能一模一樣：只找供應中、且按順序排好！
    List<MenuItem> findByIsActiveTrueOrderBySortOrderAsc();
}