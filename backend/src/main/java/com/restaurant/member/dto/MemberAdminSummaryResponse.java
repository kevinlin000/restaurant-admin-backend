package com.restaurant.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 後台首頁顯示的會員 / 員工摘要。
 * 只統計 member 模組可獨立取得的資料，不依賴訂單或訂位模組。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberAdminSummaryResponse {
    private Long totalMembers;
    private Long todayNewMembers;
    private Long bronzeCount;
    private Long silverCount;
    private Long goldCount;
    private Long diamondCount;
    private Long activeStaffCount;
}
