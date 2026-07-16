package com.restaurant.member.service.impl;

import com.restaurant.common.BusinessException;
import com.restaurant.common.ResourceNotFoundException;
import com.restaurant.member.dto.*;
import com.restaurant.member.entity.*;
import com.restaurant.member.entity.Staff.StaffStatus;
import com.restaurant.member.repository.*;
import com.restaurant.member.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final StaffRepository staffRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 取得會員個人資料
     */
    @Override
    @Transactional
    public MemberProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("使用者不存在"));
        MemberProfile profile = getOrCreateMemberProfile(user);

        return toMemberProfileResponse(user, profile);
    }

    /**
     * 更新會員個人資料
     */
    @Override
    @Transactional
    public MemberProfileResponse updateProfile(Long userId, MemberUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("使用者不存在"));
        MemberProfile profile = getOrCreateMemberProfile(user);

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
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
                .orElseThrow(() -> new ResourceNotFoundException("使用者不存在"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("舊密碼錯誤");
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
     * 後台首頁會員 / 員工摘要。
     * 只使用 member 模組內資料，避免依賴 order / reservation。
     */
    @Override
    @Transactional(readOnly = true)
    public MemberAdminSummaryResponse getAdminSummary() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        return MemberAdminSummaryResponse.builder()
                .totalMembers(userRepository.countByRole_RoleName("CUSTOMER"))
                .todayNewMembers(userRepository.countByRole_RoleNameAndCreatedAtBetween(
                        "CUSTOMER", todayStart, tomorrowStart))
                .bronzeCount(memberProfileRepository.countByMemberLevelAndUser_Role_RoleName(
                        MemberProfile.MemberLevel.BRONZE, "CUSTOMER"))
                .silverCount(memberProfileRepository.countByMemberLevelAndUser_Role_RoleName(
                        MemberProfile.MemberLevel.SILVER, "CUSTOMER"))
                .goldCount(memberProfileRepository.countByMemberLevelAndUser_Role_RoleName(
                        MemberProfile.MemberLevel.GOLD, "CUSTOMER"))
                .diamondCount(memberProfileRepository.countByMemberLevelAndUser_Role_RoleName(
                        MemberProfile.MemberLevel.DIAMOND, "CUSTOMER"))
                .activeStaffCount(staffRepository.countByStatus(StaffStatus.ACTIVE))
                .build();
    }

    /**
     * 取得特定員工詳細資料
     */
    @Override
    @Transactional(readOnly = true)
    public StaffResponse getStaff(Long staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到員工資料"));
        return toStaffResponse(staff.getUser(), staff);
    }

    /**
     * 取得所有員工清單，包含在職與離職。
     * 前端會依狀態篩選顯示，讓「離職人員」統計與篩選能正確運作。
     */
    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> getActiveStaffs() {
        return staffRepository.findAll().stream()
                .sorted(Comparator
                        .comparing((Staff staff) -> staff.getStatus() == StaffStatus.RESIGNED ? 1 : 0)
                        .thenComparing(Staff::getStaffId))
                .map(staff -> toStaffResponse(staff.getUser(), staff))
                .collect(Collectors.toList());
    }

    /**
     * 變更員工狀態為離職。
     * 離職後保留會員身分與點數，但移除後台權限。
     */
    @Override
    @Transactional
    public void resignStaff(Long staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到員工資料"));

        Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new BusinessException("系統角色 CUSTOMER 不存在"));

        User user = staff.getUser();
        user.setRole(customerRole);
        staff.setStatus(StaffStatus.RESIGNED);

        userRepository.save(user);
        staffRepository.save(staff);
    }


    /**
     * 取得或建立個人會員資料。
     * 員工 / 店長 / 管理員也是 User，也可能到店消費與累積點數，
     * 所以若早期員工帳號沒有 members 資料，就在第一次查詢個人資料時自動補上。
     */
    private MemberProfile getOrCreateMemberProfile(User user) {
        return memberProfileRepository.findByUserUserId(user.getUserId())
                .orElseGet(() -> memberProfileRepository.save(
                        MemberProfile.builder()
                                .user(user)
                                .build()));
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
                .pointLevel(profile.getPointLevel())
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
        res.setBirthday(user.getBirthday());
        res.setRoleName(user.getRole().getRoleName());
        res.setStoreId(staff.getStore().getStoreId());
        res.setStaffNo(staff.getStaffNo());
        res.setHireDate(staff.getHireDate());
        res.setStatus(staff.getStatus() != null ? staff.getStatus().name() : null);
        return res;
    }
}
