package com.restaurant.member.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 顧客填寫會員註冊時傳入的請求資料
 */
@Data
public class MemberRegisterRequest {
    private String email;
    private String password;
    private String name;
    private String phone;
    private LocalDate birthday;
}