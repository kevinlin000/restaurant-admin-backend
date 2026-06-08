package com.restaurant.member.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 回傳給前端顯示的員工資訊
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
    private Integer status;
}