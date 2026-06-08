package com.restaurant.member.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.restaurant.member.entity.MemberProfile.MemberLevel;

/**
 * 前台會員修改個人資料時，前端傳入的請求資料
 */
@Data
public class MemberProfileResponse {
    private Long userId;
    private String email;
    private String name;
    private String phone;
    private Integer pointBalance;
    private MemberLevel memberLevel;
    private LocalDate birthday;
    private LocalDateTime createdAt;
}