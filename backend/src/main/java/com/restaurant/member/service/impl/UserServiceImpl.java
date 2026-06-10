package com.restaurant.member.service.impl;

import com.restaurant.member.dto.*;
import com.restaurant.member.entity.*;
import com.restaurant.member.entity.Staff.StaffStatus;
import com.restaurant.member.repository.*;
import com.restaurant.member.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 取得會員個人資料
     */
    @Override
    @Transactional(readOnly = true)
    public MemberProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("使用者不存在"));
        MemberProfile profile = memberProfileRepository.findByUserUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("找不到會員資料"));

        return toMemberProfileResponse(user, profile);
    }

    /**
     * 更新會員個人資料
     */
    @Override
    @Transactional
    public MemberProfileResponse updateProfile(Long userId, MemberUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("使用者不存在"));
        MemberProfile profile = memberProfileRepository.findByUserUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("找不到會員資料"));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        userRepository.save(user);

        return toMemberProfileResponse(user, profile);
    }

    /**
     * 修改會員密碼
     */
    @Override
    @Transactional
    public void updatePassword(Long userId, PasswordUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("使用者不存在"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("舊密碼錯誤");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * 會員帳號軟刪除
     */
    @Override
    @Transactional
    public void deleteAccount(Long userId) {
        // 先刪除對應的 MemberProfile
        memberProfileRepository.findByUserUserId(userId)
                .ifPresent(memberProfileRepository::delete);

        // 再軟刪除 User（@SQLDelete 會自動轉成 UPDATE SET is_deleted = true）
        userRepository.deleteById(userId);
    }

    /**
     * 取得特定員工詳細資料
     */
    @Override
    @Transactional(readOnly = true)
    public StaffResponse getStaff(Long staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("找不到員工資料"));
        return toStaffResponse(staff.getUser(), staff);
    }

    /**
     * 取得指定門市的在職員工清單，依 storeId 過濾
     */
    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> getActiveStaffs() {
        return staffRepository.findByStatus(StaffStatus.ACTIVE).stream()
                .map(staff -> toStaffResponse(staff.getUser(), staff))
                .collect(Collectors.toList());
    }

    /**
     * 變更員工狀態為離職
     */
    @Override
    @Transactional
    public void resignStaff(Long staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("找不到員工資料"));

        staff.setStatus(StaffStatus.RESIGNED);
        staffRepository.save(staff);
    }

    /**
     * 將 User 和 MemberProfile 轉換為 MemberProfileResponse DTO
     */
    private MemberProfileResponse toMemberProfileResponse(User user, MemberProfile profile) {
        return MemberProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .pointBalance(profile.getPointBalance())
                .memberLevel(profile.getMemberLevel())
                .birthday(user.getBirthday())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 將 User 和 Staff 轉換為 StaffResponse DTO
     */
    private StaffResponse toStaffResponse(User user, Staff staff) {
        StaffResponse res = new StaffResponse();
        res.setUserId(user.getUserId());
        res.setStaffId(staff.getStaffId());
        res.setName(user.getName());
        res.setEmail(user.getEmail());
        res.setPhone(user.getPhone());
        res.setRoleName(user.getRole().getRoleName());
        res.setStoreId(staff.getStore().getStoreId());
        res.setStaffNo(staff.getStaffNo());
        res.setHireDate(staff.getHireDate());
        res.setStatus(staff.getStatus() != null ? staff.getStatus().name() : null);
        return res;
    }
}