package com.restaurant.menu.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "menu_item") // 🎯 100% 對齊組長最新的單數表名
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_item_id") // 🎯 對齊主鍵名稱
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false) // 🎯 100% 對齊 SQL 欄位
    private BigDecimal price; // 變數保留為 price，完美契合我們下午改好的前端 axios 接口

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "allergen_info", length = 200)
    private String allergenInfo;

    @Column(name = "is_active", nullable = false) // 🎯 史詩級修正：對齊最新 SQL 的布林型態
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "feature_tags", length = 255)
    private String featureTags; // 儲存格式如: "👑 店長推薦"
}