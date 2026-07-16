package com.restaurant.menu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@RestController
// 🎯 修正：把網址改成專屬 menu 組件，100% 避開全組所有人！
@RequestMapping("/api/menu-component/stores") 
@CrossOrigin(origins = "*") // 🎯 升級跨域全開，確保前端通車順暢
public class MenuStoreController {

    @Autowired
    private DataSource dataSource;

    // 🎯 這裡保持最純淨的 @GetMapping 即可，它會自動拼接成 /api/menu-component/stores
    @GetMapping 
    public ResponseEntity<List<Map<String, Object>>> getAllStores() {
        List<Map<String, Object>> stores = new ArrayList<>();
        
        // 🎯 修正點：移除會引發 500 錯誤的 WHERE is_active = 1 條件，改成全撈，避免各同學本機欄位不對齊的問題！
        String sql = "SELECT store_id, store_name FROM store"; 
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> store = new HashMap<>();
                store.put("id", rs.getInt("store_id"));
                store.put("name", "🏪 " + rs.getString("store_name"));
                stores.add(store);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.ok(stores);
    }
}