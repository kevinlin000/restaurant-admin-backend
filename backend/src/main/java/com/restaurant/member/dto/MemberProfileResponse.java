package com.restaurant.member.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.restaurant.member.entity.MemberProfile.MemberLevel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

/**
 * 回傳給前台會員的基本個資與等級資訊
 */

public class MemberProfileResponse {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private Integer pointBalance;
    private MemberLevel memberLevel;
    private LocalDate birthday;
    private LocalDateTime createdAt;
}