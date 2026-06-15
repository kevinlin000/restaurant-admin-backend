package com.restaurant.member.service;

import com.restaurant.member.dto.*;
import java.util.List;

/**
 * 負責會員與員工資料的CRUD。
 */
public interface UserService {

    /**
     * 取得個人資料。
     * 
     * @param userId 從 JWT Token 解析出的唯一識別碼
     * @return 會員詳細資料
     */
    MemberProfileResponse getProfile(Long userId);

    /**
     * 更新個人基本資料。
     * 
     * @param userId  執行更新的操作者 ID
     * @param request 包含要更新欄位的 DTO
     * @return 更新後的資料結果
     */
    MemberProfileResponse updateProfile(Long userId, MemberUpdateRequest request);

    /**
     * 修改密碼。
     * 
     * @param userId  操作者 ID
     * @param request 包含舊密碼與新密碼的 DTO
     */
    void updatePassword(Long userId, PasswordUpdateRequest request);

    /**
     * 會員註銷帳號（軟刪除）。
     * 
     * @param userId 目標刪除的帳號 ID
     */
    void deleteAccount(Long userId);

    /**
     * 取得特定員工詳細資料。
     * 
     * @param staffId 員工 ID
     * @return 員工對應的 Response DTO
     */
    StaffResponse getStaff(Long staffId);

    /**
     * 取得所有在職員工清單。
     * 
     * @return 在職員工列表
     */
    List<StaffResponse> getActiveStaffs();

    /**
     * 變更員工狀態為離職。
     * 
     * @param staffId 目標員工 ID
     */
    void resignStaff(Long staffId);
}