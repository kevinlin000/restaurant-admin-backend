package com.restaurant.member.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 後台管理員建立新員工帳號時傳入的請求資料
 */
@Data
public class StaffCreateRequest {
    private String email;
    private String password;
    private String name;
    private String phone;
    private Long storeId;
    private String staffNo;
    private LocalDate hireDate;
}
