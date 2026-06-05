package com.restaurant.controller;     //宣告檔案位置。這份檔案住在「控制中心 (controller)」資料夾。

import com.restaurant.entity.CategoryMenu;    //把「菜單身分證 (Entity)」拿過來，讓接待員知道他要處理的貨物長什麼樣。
import com.restaurant.repository.CategoryMenuRepository;   //把「倉庫管理員 (Repository)」請過來，因為接待員需要他去拿資料。
import org.springframework.beans.factory.annotation.Autowired;    //導入「自動連線」工具。沒有它，接待員沒辦法自動叫到管理員。
import org.springframework.web.bind.annotation.*;      //導入「網頁對話規則」。這包含處理網址、回傳 JSON 等等必備工具。

import java.util.List;

@RestController // 告訴 Spring 這是接待員，它會自動把 Java 資料轉換成網頁看得懂的 JSON 格式。
@RequestMapping("/api/categories") // 這就像是這間餐廳的電話分機，當客人在網址輸入 /api/categories，就會由這位接待員負責。
public class CategoryMenuController {

    @Autowired   //「自動媒合」。這是 Spring 最強的功能，它會自動把妳剛寫好的 CategoryMenuRepository 裝進來，妳不用自己去 new 一個出來。
    private CategoryMenuRepository categoryMenuRepository; // 在接待員旁邊安排一個專屬的管理員位置，方便隨時喊他。

    // 接待員：負責接聽電話，並把所有菜單分類端出來
    @GetMapping
    public List<CategoryMenu> getAllCategories() {
        return categoryMenuRepository.findAll(); // 直接呼叫 findAll() 拿走所有資料
    }
}