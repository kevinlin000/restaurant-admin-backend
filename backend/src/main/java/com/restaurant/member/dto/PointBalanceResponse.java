package com.restaurant.member.dto;

import com.restaurant.member.entity.MemberProfile.MemberLevel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointBalanceResponse {
    /** 可用折抵點數。 */
    private Integer pointBalance;

    /** 會員升級點數，只累積不因折抵減少。 */
    private Integer pointLevel;

    private MemberLevel memberLevel;
    private MemberLevel nextLevel;
    private Integer pointsToNextLevel;
    private String earnRuleText;
}
