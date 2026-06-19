package com.restaurant.menu.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.restaurant.menu.dto.MenuCreateDTO; // 🎯 完美引進大寫 DTO
import com.restaurant.menu.dto.MenuEditDTO;   // 🎯 完美引進大寫 DTO
import com.restaurant.menu.entity.MenuItem;
import com.restaurant.menu.entity.StoreMenu;
import com.restaurant.menu.repository.MenuItemRepository;

import com.restaurant.menu.dto.StoreMenuDisplayResponse; // 🎯 引入動態回傳規格
import com.restaurant.menu.repository.StoreMenuRepository; // 🎯 引入新分店數據庫鑰匙
import java.util.ArrayList; // 🎯 順便引入 Java 萬能大籃子 ArrayList

@Service
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    // 💡 補上全新電話線，讓 Service 能同時讀取總部與分店兩張表！
    @Autowired
    private StoreMenuRepository storeMenuRepository;

    // 獲取所有餐點
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    // 根據餐點 ID 查詢單一品項
    public MenuItem getMenuItemById(Long id) {
        return menuItemRepository.findById(id).orElse(null);
    }

    // 🚀 絕招一：學會接收 MenuCreateDTO 包裹，並存入資料庫
    public MenuItem createMenuItem(MenuCreateDTO dto) {
        MenuItem menuItem = new MenuItem();
        menuItem.setCategoryId(dto.getCategoryId());
        menuItem.setItemName(dto.getItemName());
        menuItem.setDescription(dto.getDescription());
        menuItem.setPrice(dto.getPrice());
        menuItem.setImageUrl(dto.getImageUrl());
        menuItem.setAllergenInfo(dto.getAllergenInfo());
        menuItem.setIsActive(dto.getIsActive());

        return menuItemRepository.save(menuItem);
    }

    // 🚀 絕招二：學會接收 MenuEditDTO 包裹，並更新資料庫
    public MenuItem updateMenuItem(Long id, MenuEditDTO dto) {
        MenuItem existingItem = menuItemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("找不到該品項，無法修改！"));

        existingItem.setCategoryId(dto.getCategoryId());
        existingItem.setItemName(dto.getItemName());
        existingItem.setDescription(dto.getDescription());
        existingItem.setPrice(dto.getPrice());
        existingItem.setImageUrl(dto.getImageUrl());
        existingItem.setAllergenInfo(dto.getAllergenInfo());
        existingItem.setIsActive(dto.getIsActive());

        return menuItemRepository.save(existingItem);
    }

    // 軟刪除（下架）
    public MenuItem deleteMenuItem(Long id) {
        MenuItem existingItem = menuItemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("找不到該品項，無法下架！"));

        existingItem.setIsActive(false); // 🎯 完美對齊最新的布林值下架！
        return menuItemRepository.save(existingItem);
    }

    // 🚀 高階商務邏輯：動態計算各店專屬菜單
    public List<StoreMenuDisplayResponse> getStoreMenu(Long storeId) {
        List<StoreMenuDisplayResponse> displayList = new ArrayList<>();

        // 步驟 A：去 store_menu 撈出該分店「有供應 (is_available = true)」的所有設定
        List<StoreMenu> storeMenuItems = storeMenuRepository.findByStoreIdAndIsAvailableTrue(storeId);

        for (StoreMenu storeMenu : storeMenuItems) {
            // 步驟 B：拿著關聯的 menu_item_id，去總部菜單表把餐點細節（品名、描述、圖片、過敏原）撈出來
            MenuItem item = menuItemRepository.findById(storeMenu.getMenuItemId()).orElse(null);

            // 步驟 C：確保總部沒有把這道菜大下架 (is_active = true)
            if (item != null && item.getIsActive()) {
                StoreMenuDisplayResponse response = new StoreMenuDisplayResponse();
                
                // 1. 填入餐點基本 ID
                response.setId(item.getId());
                
                // 🎯 核心防禦點修正：如果總部資料庫的 categoryId 剛好是 null，自動給 1L (Long) 保底，絕對不噴 500 空指標異常！
                response.setCategoryId(item.getCategoryId() != null ? item.getCategoryId() : 1L);
                
                response.setItemName(item.getItemName());
                response.setDescription(item.getDescription());
                response.setImageUrl(item.getImageUrl());
                response.setAllergenInfo(item.getAllergenInfo());

                // 🔥 核心商業邏輯：如果分店有客製化售價，就用分店價；如果為 null，自動退回總部建議售價！
                if (storeMenu.getPrice() != null) {
                    response.setFinalPrice(storeMenu.getPrice());
                } else {
                    response.setFinalPrice(item.getPrice());
                }

                // 🔥 售罄實時連動邏輯：
                // 1. 如果分店設定為不可供應 (isAvailable == false)，則直接鎖定按鈕！
                // 2. 如果總部把這道菜全台灣停售了 (isActive == false)，也直接鎖定按鈕！
                if (storeMenu.getIsAvailable() != null && !storeMenu.getIsAvailable()) {
                    response.setIsSelectable(false); // 鎖定按鈕，顯示已售罄！
                } else if (item.getIsActive() != null && !item.getIsActive()) {
                    response.setIsSelectable(false); // 總部停售，鎖定按鈕！
                } else {
                    response.setIsSelectable(true);  // 正常開放加入購物車！
                }

                // 🤝 初始化特色標籤籃子，完美預留組長前台展示空間！
                response.setFeatureTags(new ArrayList<>()); 

                displayList.add(response);
            }
        }    
        return displayList;
    }
}