package com.restaurant.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailCodeRequest {

    @NotBlank(message = "Email 不能為空")
    @Email(message = "Email 格式不正確")
    private String email;

    @NotBlank(message = "驗證碼不能為空")
    @Pattern(regexp = "^\\d{6}$", message = "驗證碼必須為 6 位數字")
    private String code;
}
