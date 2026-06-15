package com.restaurant.store.repository; // 👈 門牌號碼在 store 棟的倉庫！

import com.restaurant.store.entity.Category; // 👈 引進剛剛蓋好的正牌分類地基！
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 自動獲得分類的增刪改查大招！
}