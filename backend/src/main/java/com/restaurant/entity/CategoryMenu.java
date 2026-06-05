package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity               //告訴程式這是一個要對齊資料庫的檔案。
@Table(name = "category_menu")
@Data                  //自動產生getter和setter
@Builder               // 讓妳之後可以用 CategoryMenu.builder()... 這種優雅的方式建立物件
@NoArgsConstructor    // 自動生成無參數建構子 (JPA 運作必須要有)
@AllArgsConstructor   // 自動生成全參數建構子
public class CategoryMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id") // 參考同學的命名習慣，主鍵通常會寫清楚名稱
    private Integer id;

    @Column(name = "category_name", nullable = false, length = 100) // 參考同學的欄位設定細節
    private String name;

    @Column(name = "sort_order")
    private Integer sortOrder;

}