package com.restaurant.member.service;

import com.restaurant.member.dto.*;
import java.util.List;

public interface UserService {

    /**
     * 取得會員完整個人資料，讓會員查看自己的基本資料、點數、等級。
     */
    MemberProfileResponse getProfile(Long userId);

    /**
     * 更新會員基本資料，讓會員修改自己的姓名、電話、生日。
     */
    MemberProfileResponse updateProfile(Long userId, MemberUpdateRequest request);

    /**
     * 軟刪除，可以註銷帳號。
     */
    void deleteAccount(Long userId);

    /**
     * 取得單一員工資料（後台管理員或店長查詢特定員工）
     */
    StaffResponse getStaff(Long staffId);

    /**
     * 取得所有員工清單（後台管理系統列出目前所有員工）
     */
    List<StaffResponse> getActiveStaffs();

    /**
     * 員工離職（後台管理員將員工狀態設為離職，並同步軟刪除其 User 登入權限）
     */
    void resignStaff(Long staffId);
}