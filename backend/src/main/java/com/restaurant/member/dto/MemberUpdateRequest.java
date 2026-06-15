package com.restaurant.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

/**
 * 會員修改個人資料時傳入的請求資料
 */
@Data
public class MemberUpdateRequest {

    @NotBlank(message = "姓名不能為空")
    @Size(max = 50, message = "姓名長度不能超過 50 個字元")
    private String name;

    @NotBlank(message = "手機號碼不能為空")
    @Pattern(regexp = "^09\\d{8}$", message = "手機號碼格式必須為 09xxxxxxxx")
    private String phone;

    private LocalDate birthday;
}