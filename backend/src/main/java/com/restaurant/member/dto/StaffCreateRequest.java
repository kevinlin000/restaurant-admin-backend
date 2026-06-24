package com.restaurant.member.dto;

import lombok.Data;
import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 後台管理員建立新員工帳號時傳入的請求資料。
 */
@Data
public class StaffCreateRequest {

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    private String email;

    /**
     * 新 Email 建立員工帳號時必填。
     * 若 Email 已是一般會員，系統會保留原會員密碼，此欄可不填。
     */
    private String password;

    @NotBlank(message = "姓名不可為空")
    @Size(max = 50)
    private String name;

    @Pattern(regexp = "^09\\d{8}$", message = "手機號碼格式不正確")
    private String phone;

    /**
     * 新 Email 建立員工帳號時必填。
     * 若 Email 已是一般會員，未填時會沿用原本會員生日。
     */
    private LocalDate birthday;

    @NotNull(message = "門市不可為空")
    private Long storeId;

    @NotBlank(message = "員工編號不可為空")
    private String staffNo;

    private LocalDate hireDate;

    @NotBlank(message = "角色名稱不可為空")
    private String roleName;
}