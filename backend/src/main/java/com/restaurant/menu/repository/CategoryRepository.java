package com.restaurant.menu.repository; // 👈 門牌號碼在 store 棟的倉庫！

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurant.menu.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 自動獲得分類的增刪改查大招！
}