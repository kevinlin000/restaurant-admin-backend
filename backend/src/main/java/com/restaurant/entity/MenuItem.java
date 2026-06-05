package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity               //告訴程式這是一個要對齊資料庫的檔案。
@Table(name = "category_items")
@Data                  //自動產生getter和setter
@Builder               // 讓妳之後可以用 CategoryMenu.builder()... 這種優雅的方式建立物件
@NoArgsConstructor    // 自動生成無參數建構子 (JPA 運作必須要有)
@AllArgsConstructor   // 自動生成全參數建構子

public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_item_id") // 👈 告訴電腦：這代表 MySQL 的 menu_item_id
    private Long id;

    @Column(name = "category_id") // 👈 告訴電腦：這代表 MySQL 的 category_id
    private Long categoryId;

    private String itemName;

    private String description;

    @Column(name = "base_price")
    private Integer price;

    private String imageUrl;

    private Integer sortOrder;

    private String allergenInfo;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

}
