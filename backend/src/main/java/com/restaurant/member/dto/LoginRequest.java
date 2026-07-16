package com.restaurant.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 處理登入功能時前端傳入的請求資料 (會員與員工皆通)
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    private String email;

    @NotBlank(message = "密碼不可為空")
    @Size(min = 8, max = 20, message = "密碼長度需為 8 到 20 個字元")
    private String password;
}