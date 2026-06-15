package com.restaurant.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "Email 不能為空")
    @Email(message = "Email 格式不正確")
    private String email;

    @NotBlank(message = "驗證碼不能為空")
    private String code;

    @NotBlank(message = "新密碼不能為空")
    @Size(min = 8, max = 20, message = "新密碼長度需為 8 到 20 個字元")
    private String newPassword;
}
