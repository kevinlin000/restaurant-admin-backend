package com.restaurant.member.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.restaurant.member.entity.MemberProfile.MemberLevel;

/**
 * 回傳給前台會員的基本個資與等級資訊
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