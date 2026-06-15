package com.restaurant.member.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 回傳給後台顯示的員工清單或個人資訊之回應資料。
 * 封裝使用者資料（User）與員工資料（Staff）。
 */
@Data
public class StaffResponse {
    private Long userId;
    private Long staffId;
    private String email;
    private String name;
    private String phone;
    private String roleName;
    private Long storeId;
    private String staffNo;
    private LocalDate hireDate;
    private String status;
}