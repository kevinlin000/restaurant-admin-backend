package com.restaurant.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 修改密碼請求。
 */
@Getter
@Setter
public class PasswordUpdateRequest {

    @NotBlank(message = "舊密碼不可為空")
    private String oldPassword;

    @NotBlank(message = "新密碼不可為空")
    @Size(min = 8, max = 20, message = "密碼長度需介於 8 到 20 字元")
    private String newPassword;
}