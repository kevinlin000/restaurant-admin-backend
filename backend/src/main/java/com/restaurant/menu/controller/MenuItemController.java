package com.restaurant.menu.controller; // 👈 1. 門牌號碼完全對齊新家！

import com.restaurant.menu.entity.MenuItem;
import com.restaurant.menu.service.MenuItemService;
import com.restaurant.common.ApiResponse; // 👈 2. 引入早上發現的「公共交誼廳」萬能包裹紙箱！
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@RestController // 💡 貼上貼紙：告訴 Spring Boot 這是一個可以用 JSON 傳包裹的「高階櫃檯」！
@RequestMapping("/api/menu-items") // 💡 告訴前台：只要網址輸入這個，就能走到我的餐點櫃檯！
public class MenuItemController {

    @Autowired
    private MenuItemService menuItemService; // 💡 自動拉好電話線，指派你的「四大絕招業務員」在櫃檯後面待命

    // 🚪 櫃檯窗口一：給前台「獲取所有餐點」的包裹
    @GetMapping
    public ApiResponse<List<MenuItem>> getAllMenuItems() {
        List<MenuItem> items = menuItemService.getAllMenuItems();
        // 運用同學在 common 裡寫好的 ApiResponse 萬能紙箱，把餐點打包成漂亮的成功包裹（200 OK）！
        return ApiResponse.success(items);
    }

    // 🚪 櫃檯窗口二：接收前台送來的新餐點，登記上架
    @PostMapping
    public ApiResponse<MenuItem> createMenuItem(@RequestBody MenuItem menuItem) {
        MenuItem createdItem = menuItemService.createMenuItem(menuItem);
        return ApiResponse.success(createdItem);
    }

    // 🚪 櫃檯窗口三：前台拿著號碼牌（id）和新包裹，要求「修改餐點」
    @PutMapping("/{id}")
    public ApiResponse<MenuItem> updateMenuItem(@PathVariable Long id, @RequestBody MenuItem updatedItem) {
        MenuItem item = menuItemService.updateMenuItem(id, updatedItem);
        return ApiResponse.success(item);
    }

    // 🚪 櫃檯窗口四：前台拿著號碼牌（id），要求「下架（軟刪除）餐點」
    @DeleteMapping("/{id}")
    public ApiResponse<MenuItem> deleteMenuItem(@PathVariable Long id) {
        MenuItem item = menuItemService.deleteMenuItem(id);
        return ApiResponse.success(item);
    }
}