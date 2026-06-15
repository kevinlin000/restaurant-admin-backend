package com.restaurant.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {

    @NotBlank(message = "Email 不能為空")
    @Email(message = "Email 格式不正確")
    private String email;
}
