package com.restaurant.menu.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "menu_item") // 🚀 1. 檢查：ER圖上是小寫底線的 menu_item
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_item_id") // 🚀 2. 對齊 ER 圖的主鍵名字
    private Long id;

    // 🚀 3. 補上最核心的死角：分類 ID 外鍵（對齊 ER 圖的 category_id bigint）
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "description")
    private String description;

    @Column(name = "base_price", nullable = false) // 🚀 4. 對齊 ER 圖的 base_price
    private BigDecimal price;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    private String status = "AVAILABLE";

    // 🚀 終極神救援：對齊 ER 圖最底層的 allergen_info (varchar)
    @Column(name = "allergen_info")
    private String allergenInfo;
}