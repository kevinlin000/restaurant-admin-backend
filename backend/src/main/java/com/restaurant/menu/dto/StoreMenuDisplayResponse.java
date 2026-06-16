package com.restaurant.menu.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List; // 🎯 記得引入 List 套件

@Data
public class StoreMenuDisplayResponse {
    private Long id;              // menu_item_id
    private String itemName;      // 餐點名稱
    private String description;   // 餐點描述
    private BigDecimal finalPrice;// 💥 精髓：經過後端判斷後的「該店最終售價」
    private String imageUrl;      // 圖片 URL
    private String allergenInfo;  // 過敏原
    
    // 🤝 曾總監跨模組黃金接引管線：完美咬合組長今早新增的門市特色標籤（如：包廂、親子友善）
    private List<String> featureTags; 
    // 🎯 曾總監實時售罄控制信號：true 代表可以點餐，false 代表按鈕變灰
    private Boolean isSelectable = true;
}
