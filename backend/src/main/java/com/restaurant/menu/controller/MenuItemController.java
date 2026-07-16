package com.restaurant.menu.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.restaurant.common.ApiResponse;
import com.restaurant.menu.dto.MenuCreateDTO; // 🎯 100% 配合總監本機的大寫 DTO
import com.restaurant.menu.dto.MenuEditDTO;   // 🎯 100% 配合總監本機的大寫 DTO
import com.restaurant.menu.dto.StoreMenuDisplayResponse;
import com.restaurant.menu.entity.MenuItem;
import com.restaurant.menu.service.MenuItemService;

@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {

    @Autowired
    private MenuItemService menuItemService;

    // ==================== 【查詢管線】 ====================

    // 獲取所有餐點 (總部主表)
    @GetMapping
    public ApiResponse<List<MenuItem>> getAllMenuItems() {
        return ApiResponse.success(menuItemService.getAllMenuItems());
    }

    // 根據餐點 ID 查詢單一品項
    @GetMapping("/{id}")
    public ApiResponse<MenuItem> getMenuItemById(@PathVariable Long id) {
        MenuItem item = menuItemService.getMenuItemById(id);
        if (item != null) {
            return ApiResponse.success(item);
        } else {
            return ApiResponse.error("找不到該品項");
        }
    }

    // 🚀 讓前端一秒調用該店專屬的菜單 displayList！
    @GetMapping("/store/{storeId}")
    public ApiResponse<List<StoreMenuDisplayResponse>> getStoreMenu(@PathVariable String storeId) {
    if ("ALL".equalsIgnoreCase(storeId)) {
        // 🌐 ADMIN 選「全台」時，後台列表用總部基準價顯示，預設用 store_id = 1 撈取顯示用列表
        List<StoreMenuDisplayResponse> menuList = menuItemService.getStoreMenu(1L);
        return ApiResponse.success(menuList);
    }
    Long parsedStoreId = Long.parseLong(storeId);
    List<StoreMenuDisplayResponse> menuList = menuItemService.getStoreMenu(parsedStoreId);
    return ApiResponse.success(menuList);
}


    // ==================== 【店長多租戶隔離寫入管線】 ====================

    /**
     * 🎯 【新增菜單隔離版】
     * 接收前端送過來的新餐點，並自動與當前店長 storeId 綁定，寫入 store_menu 關聯表
     */
    // ✅ 改成 String 接收，可判斷 "ALL"
    @PostMapping("/store/{storeId}")
    public ApiResponse<MenuItem> createStoreMenuItem(
        @PathVariable String storeId, 
        @RequestBody MenuCreateDTO dto) {
    
    if ("ALL".equalsIgnoreCase(storeId)) {
        // 🌐 ADMIN 全台統一定價：只建立總部 menu_item，不寫入 store_menu
        MenuItem item = menuItemService.createMenuItem(dto);
        return ApiResponse.success(item);
    }

    // 🏪 特定分店：原本的分店隔離邏輯
    Long parsedStoreId = Long.parseLong(storeId);
        MenuItem item = menuItemService.createStoreMenuItem(parsedStoreId, dto);
        return ApiResponse.success(item);
    }

    /**
     * 🎯 【修改菜單隔離版】
     * 當店長修改菜單時，絕對不碰觸總部總表，只更新該 storeId 在 store_menu 裡的 price 與 is_available
     */
    // ✅ 改成 String 接收，可判斷 "ALL"
    @PutMapping("/{id}/store/{storeId}")
    public ApiResponse<MenuItem> updateStoreMenuItem(
        @PathVariable Long id,
        @PathVariable String storeId, 
        @RequestBody MenuEditDTO dto) {
    
    if ("ALL".equalsIgnoreCase(storeId)) {
        // 🌐 ADMIN 全台統一定價：只修改總部 menu_item 的 base_price
        MenuItem item = menuItemService.updateMenuItem(id, dto);
        return ApiResponse.success(item);
    }

    // 🏪 特定分店：原本的分店隔離邏輯
    Long parsedStoreId = Long.parseLong(storeId);
        MenuItem item = menuItemService.updateStoreMenuItem(id, parsedStoreId, dto);
        return ApiResponse.success(item);
}


    // ==================== 【下架管線】 ====================

    // 下架（軟刪除）餐點 
    // (註：如果是分店店長下架，實務上通常是去改 store_menu 的 is_available = false，此處保留原功能提供給總部或做大範圍調整)
    @DeleteMapping("/{id}")
    public ApiResponse<MenuItem> deleteMenuItem(@PathVariable Long id) {
        MenuItem item = menuItemService.deleteMenuItem(id);
        return ApiResponse.success(item);
    }

    /*
     * 🎯 【ADMIN 專用：永久刪除】
     * 真正從資料庫移除這筆餐點與所有分店的關聯，無法復原！
     */
    @DeleteMapping("/{id}/permanent")
    public ApiResponse<String> permanentlyDeleteMenuItem(@PathVariable Long id) {
        menuItemService.permanentlyDeleteMenuItem(id);
        return ApiResponse.success("已永久刪除");
    }
}