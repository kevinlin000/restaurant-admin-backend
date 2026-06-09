package com.restaurant.menu.dto;

import lombok.Data;

/**
 * 🚀 菜單模組 - 修改與軟刪除專用 DTO (日本料理完全體)
 */
@Data
public class MenuEditDTO {
    
    // 1. 它是哪一筆餐點？(Primary Key)
    private Long id;
    
    // 2. 餐點名稱
    private String name;
    
    // 3. 餐點價格
    private Integer price;
    
    // 4. 上架狀態
    private String status;

    // 5. ✨ 新增：餐點詳細描述
    private String description;

    // 6. ✨ 新增：餐點圖片網址
    private String imageUrl;

    // 7. ✨ 新增：餐點分類
    private String category;

    // ✨ 新增：過敏原資訊 (例如：含有甲殼類、花生等)
    private String allergenInfo;
}