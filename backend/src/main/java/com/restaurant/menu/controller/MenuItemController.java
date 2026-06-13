package com.restaurant.menu.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.restaurant.common.ApiResponse;
import com.restaurant.menu.dto.MenuCreateDTO; // 🎯 100% 配合總監本機的大寫 DTO
import com.restaurant.menu.dto.MenuEditDTO;   // 🎯 100% 配合總監本機的大寫 DTO
import com.restaurant.menu.entity.MenuItem;
import com.restaurant.menu.service.MenuItemService;

@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {

    @Autowired
    private MenuItemService menuItemService;

    // 獲取所有餐點
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

    // 接收前端送過來的新餐點，登記上架
    @PostMapping
    public ApiResponse<MenuItem> createMenuItem(@RequestBody MenuCreateDTO dto) { // 🎯 參數對齊大寫
        return ApiResponse.success(menuItemService.createMenuItem(dto));
    }

    // 修改餐點
    @PutMapping("/{id}")
    public ApiResponse<MenuItem> updateMenuItem(@PathVariable Long id, @RequestBody MenuEditDTO dto) { // 🎯 參數對齊大寫
        MenuItem item = menuItemService.updateMenuItem(id, dto);
        return ApiResponse.success(item);
    }

    // 下架（軟刪除）餐點
    @DeleteMapping("/{id}")
    public ApiResponse<MenuItem> deleteMenuItem(@PathVariable Long id) {
        MenuItem item = menuItemService.deleteMenuItem(id);
        return ApiResponse.success(item);
    }
}