package com.restaurant.member.dto;

import com.restaurant.member.entity.MemberProfile.MemberLevel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointBalanceResponse {
    private Integer pointBalance;
    private MemberLevel memberLevel;
    private MemberLevel nextLevel;
    private Integer pointsToNextLevel;
    private String earnRuleText;
}
