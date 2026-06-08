package com.restaurant.menu.service; // 👈 1. 新家門牌完全對齊！

import com.restaurant.menu.entity.MenuItem;
import com.restaurant.menu.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service // 👈 2. 貼上動腦業務員貼紙
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository; // 👈 3. 對齊你左邊綠色的新倉庫！

    // 絕招一：查詢所有餐點
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    // 絕招二：新增餐點
    public MenuItem createMenuItem(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }

    // 💡 絕招三：回歸你最踏實的「修改餐點功能」核心靈魂！
    public MenuItem updateMenuItem(Long id, MenuItem updatedItem) {
        // 使用 Java 內建全宇宙通用的 IllegalArgumentException，再也不用怕同學沒給檔案！
        MenuItem existingItem = menuItemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("找不到該品項，無法修改！"));
        
        // 你的擦掉並改寫邏輯
        existingItem.setItemName(updatedItem.getItemName());
        existingItem.setDescription(updatedItem.getDescription());
        existingItem.setPrice(updatedItem.getPrice());
        existingItem.setImageUrl(updatedItem.getImageUrl());
        
        return menuItemRepository.save(existingItem);
    }

    // 💡 絕招四：回歸你最踏實的「軟刪除（下架）功能」！
    public MenuItem deleteMenuItem(Long id) {
        MenuItem existingItem = menuItemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("找不到該品項，無法下架！"));
        
        existingItem.setIsActive(false); // 你的左手塗改軟刪除大招！
        return menuItemRepository.save(existingItem);
    }
}