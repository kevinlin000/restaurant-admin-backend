package com.restaurant.menu.service; // 1. 專業門牌完全對齊！

import com.restaurant.menu.entity.MenuItem;
import com.restaurant.menu.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service // 2. 貼上動態標籤
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository; // 3. 劃齊你左邊黑色的斬舟刀！

    // 絕招一：查看所有餐點
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    // 絕招二：新增餐點
    public MenuItem createMenuItem(MenuItem menuItem) {
        // 🚀 核心概念：因為你現在是直接傳入 Entity (menuItem)，
        // 只要前端有傳 allergenInfo 和新格式的 status，這裡呼叫 .save 就會直接完美寫入！
        return menuItemRepository.save(menuItem);
    }

    // 💡 絕招三：回歸你最踏實的「修改餐點功能」核心靈魂！
    public MenuItem updateMenuItem(Long id, MenuItem updatedItem) {
        // 使用 Java 內建宇宙通用的 IllegalArgumentException，再也不用怕同學沒給檔案！
        MenuItem existingItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("找不到該品項，無法修改！"));

        // 你的擦掉並改寫邏輯
        existingItem.setItemName(updatedItem.getItemName());
        existingItem.setDescription(updatedItem.getDescription());
        
        // 🚀 核心修正一：因為你的 Entity 欄位昨天改成了 basePrice，請確認你的 Entity getter/setter 名字
        // 如果你的 Entity 叫 setBasePrice，這裡就用 setBasePrice；如果叫 setPrice，就維持下行：
        existingItem.setPrice(updatedItem.getPrice());
        existingItem.setImageUrl(updatedItem.getImageUrl());

        // 🚀 核心修正二：搬運最新字串型態的 Status ("AVAILABLE" / "UNAVAILABLE")
        existingItem.setStatus(updatedItem.getStatus());

        // 🚀 核心新增三：搬運昨天最新擴充的過敏原欄位
        existingItem.setAllergenInfo(updatedItem.getAllergenInfo());

        return menuItemRepository.save(existingItem);
    }

    // 💡 絕招四：回歸你最踏實的「軟刪除（下架）」功能！
    public MenuItem deleteMenuItem(Long id) {
        MenuItem existingItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("找不到該品項，無法下架！"));

        // 🚀 核心修正四：昨天狀態已經全面「字串化」，下架不再是 true/false，而是直接灌入英文字串！
        existingItem.setStatus("UNAVAILABLE"); // 💡 均勻塗上強效下架大絕！
        
        return menuItemRepository.save(existingItem);
    }
}