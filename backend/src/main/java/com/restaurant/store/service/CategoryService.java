package com.restaurant.store.service; // 👈 門牌號碼在 store 棟的服務區！

import com.restaurant.store.entity.Category;
import com.restaurant.store.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository; // 👈 綁定剛剛蓋好的分類新倉庫

    // 分類絕招一：查詢所有分類
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 分類絕招二：新增分類
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    // 分類絕招三：修改分類（完美繼承你前天寫的擦掉改寫邏輯）
    public Category updateCategory(Long id, Category updatedCategory) {
        Category existingCategory = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("找不到該分類，無法修改！"));
        
        existingCategory.setCategoryName(updatedCategory.getCategoryName());
        existingCategory.setSortOrder(updatedCategory.getSortOrder());
        
        return categoryRepository.save(existingCategory);
    }

    // 分類絕招四：分類軟刪除（下架）
    public Category deleteCategory(Long id) {
        Category existingCategory = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("找不到該分類，無法下架！"));
        
        existingCategory.setIsActive(false); // 你的左手塗改軟刪除大招！
        return categoryRepository.save(existingCategory);
    }
}