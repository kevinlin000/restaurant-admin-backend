package com.restaurant.menu.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 🎯 引入事務，確保多租戶雙表寫入原子性
import com.restaurant.menu.dto.MenuCreateDTO; //  🎯  完美引進大寫 DTO
import com.restaurant.menu.dto.MenuEditDTO;   //  🎯  完美引進大寫 DTO
import com.restaurant.menu.entity.MenuItem;
import com.restaurant.menu.entity.StoreMenu;
import com.restaurant.menu.repository.MenuItemRepository;
import com.restaurant.menu.dto.StoreMenuDisplayResponse; //  🎯  引入動態回傳規格
import com.restaurant.menu.repository.StoreMenuRepository; //  🎯  引入新分店數據庫鑰匙
import java.util.ArrayList; //  🎯  順便引入 Java 萬能大籃子 ArrayList

@Service
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    //  💡  補上全新電話線，讓 Service 能同時讀取總部與分店兩張表！
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

    //  🚀  絕招一：學會接收 MenuCreateDTO 包裹，並存入資料庫
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

    //  🚀  絕招二：學會接收 MenuEditDTO 包裹，並更新資料庫
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
        existingItem.setIsActive(false); //  🎯  完美對齊最新的布林值下架！
        return menuItemRepository.save(existingItem);
    }


    // ==================== 🛠️ 店長多租戶隔離寫入核心功能 ====================

    /**
     * 🎯 【分店隔離：獨立新增菜單】
     * 規則：在總表 menu_item 新增該品項的基礎資料，並強行與 storeId 咬合存入 store_menu 關聯表！
     */
    @Transactional
    public MenuItem createStoreMenuItem(Long storeId, MenuCreateDTO dto) {
        // 🎯【全球通用安全防線：免組員依賴】
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (auth != null) ? auth.getName() : null;
        if (currentUsername == null) {
            throw new org.springframework.security.access.AccessDeniedException("🛑 警告：您尚未登入，無權新增菜單數據！");
        }

        // 1. 先在總表建立該商品的基礎屬性
        MenuItem menuItem = new MenuItem();
        menuItem.setCategoryId(dto.getCategoryId());
        menuItem.setItemName(dto.getItemName());
        menuItem.setDescription(dto.getDescription());
        menuItem.setPrice(dto.getPrice()); 
        menuItem.setImageUrl(dto.getImageUrl());
        menuItem.setAllergenInfo(dto.getAllergenInfo());
        menuItem.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        
        MenuItem savedItem = menuItemRepository.save(menuItem);

        // 2. ⚡ 核心關聯綁定：建立 StoreMenu 實體，強行咬合當前店長的 storeId
        StoreMenu storeMenu = new StoreMenu();
        storeMenu.setStoreId(storeId);
        storeMenu.setMenuItemId(savedItem.getId()); 
        storeMenu.setPrice(dto.getPrice());          // 寫入該分店的客製化售價
        storeMenu.setIsAvailable(true);              // 新增預設直接上架上線
        
        storeMenuRepository.save(storeMenu);

        return savedItem;
    }

    /**
     * 🎯 【分店隔離：獨立修改菜單】
     * 規則：絕對不碰觸總部總表 menu_item，只鎖定並更新該 storeId 在 store_menu 裡的價格與上架狀態
     */
    @Transactional
    public MenuItem updateStoreMenuItem(Long id, Long storeId, MenuEditDTO dto) {
        
        // 🎯【全球通用安全防線：免組員依賴、防範越權竄改】
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (auth != null) ? auth.getName() : null;
        if (currentUsername == null) {
            throw new org.springframework.security.access.AccessDeniedException("🛑 警告：您尚未登入，無權修改菜單數據！");
        }

        // ==================== 以下為原本安全的隔離寫入邏輯 ====================
        // 1. 拿著 (storeId, menuItemId) 去關聯表抓出這家分店的設定
        List<StoreMenu> relations = storeMenuRepository.findByStoreIdAndIsAvailableTrue(storeId);
        StoreMenu targetRelation = null;
        
        for (StoreMenu sm : relations) {
            if (sm.getMenuItemId() != null && sm.getMenuItemId().equals(id)) {
                targetRelation = sm;
                break;
            }
        }
        
        // 防呆機制：如果該分店先前在 store_menu 沒綁定這道菜，就動態幫他建立一個
        if (targetRelation == null) {
            targetRelation = new StoreMenu();
            targetRelation.setStoreId(storeId);
            targetRelation.setMenuItemId(id);
        }

        // 2. ⚡ 更新分店的專屬售價與狀態，完全不影響別家分店與總部！
        targetRelation.setPrice(dto.getPrice());
        targetRelation.setIsAvailable(dto.getIsActive() != null ? dto.getIsActive() : true);
        
        storeMenuRepository.save(targetRelation);

        // 3. 回傳總部基本資訊給前端做畫面刷新
        return menuItemRepository.findById(id).orElse(null);
    }


    //  🚀  高階商務邏輯：動態計算各店專屬菜單
    public List<StoreMenuDisplayResponse> getStoreMenu(Long storeId) {
        List<StoreMenuDisplayResponse> displayList = new ArrayList<>();
        List<StoreMenu> storeMenuItems = storeMenuRepository.findByStoreIdAndIsAvailableTrue(storeId);
        for (StoreMenu storeMenu : storeMenuItems) {
            MenuItem item = menuItemRepository.findById(storeMenu.getMenuItemId()).orElse(null);
            if (item != null && item.getIsActive()) {
                StoreMenuDisplayResponse response = new StoreMenuDisplayResponse();

                response.setId(item.getId());
                response.setCategoryId(item.getCategoryId() != null ? item.getCategoryId() : 1L);
                response.setItemName(item.getItemName());
                response.setDescription(item.getDescription());
                response.setImageUrl(item.getImageUrl());
                response.setAllergenInfo(item.getAllergenInfo());
                
                if (storeMenu.getPrice() != null) {
                    response.setFinalPrice(storeMenu.getPrice());
                } else {
                    response.setFinalPrice(item.getPrice());
                }
                
                if (storeMenu.getIsAvailable() != null && !storeMenu.getIsAvailable()) {
                    response.setIsSelectable(false); 
                } else if (item.getIsActive() != null && !item.getIsActive()) {
                    response.setIsSelectable(false); 
                } else {
                    response.setIsSelectable(true);  
                }
                
                response.setFeatureTags(new ArrayList<>());
                displayList.add(response);
            }
        }
        return displayList;
    }
}