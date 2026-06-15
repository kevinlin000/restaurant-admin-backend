package com.restaurant.menu.dto;

import lombok.Data;

/**
 * 🚀 菜單模組 - 新增餐點專用 DTO (日本料理完全體)
 */
@Data
public class MenuCreateDTO {
    
    // 1. 餐點名稱
    private String name;
    
    // 2. 餐點價格
    private Integer price;
    
    // 3. 是否上架供應
    private String status;

    // 4. ✨ 新增：餐點詳細描述 (對齊日料規格)
    private String description;

    // 5. ✨ 新增：餐點圖片網址
    private String imageUrl;

    // 6. ✨ 新增：餐點分類 (例如: 刺身/握壽司/飯麵)
    private String category;

    // ✨ 新增：過敏原資訊 (例如：含有甲殼類、花生等)
    private String allergenInfo;
}