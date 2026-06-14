package com.restaurant.member.service;

import com.restaurant.member.dto.LoginRequest;
import com.restaurant.member.dto.LoginResponse;
import com.restaurant.member.dto.MemberRegisterRequest;
import com.restaurant.member.dto.StaffCreateRequest;
import com.restaurant.member.dto.StaffResponse;

public interface AuthService {

    /**
     * 會員註冊：建立核心帳號 User + 初始化 MemberProfile
     */
    LoginResponse registerMember(MemberRegisterRequest request);

    /**
     * 員工註冊：由管理員建立核心 User + 初始化 Staff
     */
    StaffResponse createStaff(StaffCreateRequest request);

    /**
     * 統一登入入口：驗證帳密後，依角色回傳 JWT Access Token
     */
    LoginResponse login(LoginRequest request);

    /**
     * 發送電子郵件驗證碼。
     * 系統會生成一組隨機驗證碼並發送到指定的信箱，通常用於用戶註冊、修改密碼或身分驗證。
     * 
     * @param email 接收驗證碼的電子郵件地址
     */
    void sendEmailVerificationCode(String email);

    /**
     * 驗證電子郵件驗證碼是否正確。
     * 
     * @param email 待驗證的電子郵件地址
     * @param code  用戶輸入的驗證碼
     * @return {@code true} 如果驗證碼正確且未過期；{@code false} 否則
     */
    boolean verifyEmailCode(
            String email,
            String code);

    void forgotPassword(String email);

    void resetPassword(
            String email,
            String code,
            String newPassword);
}
