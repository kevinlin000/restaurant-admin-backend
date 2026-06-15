package com.restaurant.member.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * 顧客填寫會員註冊時傳入的請求資料
 */
@Data
public class MemberRegisterRequest {

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    @Size(max = 100, message = "Email 長度不可超過 100 字元")
    private String email;

    @NotBlank(message = "密碼不可為空")
    @Size(min = 8, max = 20, message = "密碼長度需介於 8 到 20 字元")
    private String password;

    @NotBlank(message = "姓名不可為空")
    @Size(max = 50, message = "姓名長度不可超過 50 字元")
    private String name;

    @Pattern(regexp = "^09\\d{8}$", message = "手機號碼格式不正確")
    private String phone;

    @Past(message = "生日必須是過去的日期")
    private LocalDate birthday;
}