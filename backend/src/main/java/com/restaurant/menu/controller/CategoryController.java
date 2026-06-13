package com.restaurant.menu.controller;

import com.restaurant.common.ApiResponse; // 🎯 對齊組長規定的共用回傳格式
import com.restaurant.menu.entity.Category;
import com.restaurant.menu.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/menu-categories") // 🎯 對齊組長規定的單數加複數路由規範
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public ApiResponse<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        // 🎯 完美對齊組長公告提醒 2：回傳格式統一是 { success, message, data }
        return ApiResponse.success(categories); 
    }
}
