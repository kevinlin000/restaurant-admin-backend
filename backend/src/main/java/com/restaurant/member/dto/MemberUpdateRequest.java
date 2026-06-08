package com.restaurant.member.dto;

import java.time.LocalDate;

import lombok.Data;

/**
 * 會員更新時回傳的請求
 */
@Data
public class MemberUpdateRequest {
    private String name;
    private String phone;
    private LocalDate birthday;
}