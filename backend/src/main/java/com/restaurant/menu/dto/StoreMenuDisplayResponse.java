package com.restaurant.menu.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List; // 🎯 記得引入 List 套件

@Data // 🎯 曾總監的萬能鑰匙：自動生成全欄位的 Getter / Setter 
public class StoreMenuDisplayResponse {
    
    private Long id;            // menu_item_id
    
    // 💡 終極修正：必須在這裡宣告 categoryId，Service 塞進來的分類 ID 才能成功變成 JSON 運到前台！
    private Long categoryId;    // 分類 ID
    
    private String itemName;    // 餐點名稱
    private String description; // 餐點描述
    
    // 💥 精髓：經過後端判斷後的「該店最終售價」
    private BigDecimal finalPrice;
    
    private String imageUrl;    // 圖片 URL
    private String allergenInfo;// 過敏原
    
    // 🤝 曾總監跨模組黃金接引管線：完美咬合組長今早新增的門市特色標籤
    private List<String> featureTags; 
    
    // 🎯 曾總監實時售罄控制信號：true 代表可以點餐，false 代表按鈕變灰
    private Boolean isSelectable = true;
}