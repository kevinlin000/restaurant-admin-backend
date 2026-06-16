package com.restaurant.menu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/stores")
@CrossOrigin // 🎯 允許前端跨域呼叫
public class StoreController {

    @Autowired
    private DataSource dataSource; // 直接讀取你剛剛設定好的動態資料庫連線

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllStores() {
        List<Map<String, Object>> stores = new ArrayList<>();
        // 🎯 業界標準 JDBC 快速單表查詢：直接去抓所有人本機資料庫裡的 store 表！
        String sql = "SELECT store_id, store_name FROM store WHERE is_active = 1"; 
        
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