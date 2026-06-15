package com.restaurant.menu.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StoreMenuDisplayResponse {
    private Long id;              // menu_item_id
    private String itemName;      // 餐點名稱
    private String description;   // 餐點描述
    private BigDecimal finalPrice;// 💥 精髓：經過後端判斷後的「該店最終售價」
    private String imageUrl;      // 圖片 URL
    private String allergenInfo;  // 過敏原
}
