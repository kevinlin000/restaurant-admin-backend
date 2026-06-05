package com.restaurant.repository;  //這是在宣告這份檔案的住址。就像「這是在 repository 資料夾下的檔案」。

import com.restaurant.entity.CategoryMenu;  //引入你寫好的entity「身分證」。告訴管理員：「妳等一下要搬的貨物，長得就跟 CategoryMenu 一樣」。
import org.springframework.data.jpa.repository.JpaRepository; //引入 Spring Boot 的「超能力手冊」。沒有這行，管理員就不會自動學會 SQL 語法。
import org.springframework.stereotype.Repository;   //引入 Spring 框架中的「Repository（倉庫）」標籤。

/**
 * 這就是妳的資料搬運工 (Repository)
 * 繼承了 JpaRepository，妳就不用自己寫 SQL 語法了！
 * 它會自動學會：新增、修改、刪除、查詢。
 */
import java.util.List;   //引入 Java 工具箱裡的「清單（List）」規格。

@Repository        //這是一個標籤，告訴 Spring Boot：「這份檔案是專門負責跟資料庫溝通的（倉庫管理員）」。
public interface CategoryMenuRepository extends JpaRepository<CategoryMenu, Integer> {
    // 妳同學要求的重點邏輯：
    // 1. findByIsActiveTrue: 只找 is_active 為 1 (true) 的
    // 2. OrderBySortOrderAsc: 按照 sort_order 從小到大排序
    List<CategoryMenu> findByIsActiveTrueOrderBySortOrderAsc();
}