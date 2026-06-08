
package com.restaurant.member.dto;

import lombok.Data;

/**
 * 處理登入功能時前端傳入的請求資料 (會員與員工皆通)
 */
@Data
public class LoginRequest {
    private String email;
    private String password;
}
