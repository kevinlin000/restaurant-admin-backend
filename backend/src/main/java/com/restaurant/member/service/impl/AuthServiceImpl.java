package com.restaurant.member.service.impl;

import com.restaurant.common.BusinessException;
import com.restaurant.member.dto.LoginRequest;
import com.restaurant.member.dto.LoginResponse;
import com.restaurant.member.dto.MemberRegisterRequest;
import com.restaurant.member.dto.StaffCreateRequest;
import com.restaurant.member.dto.StaffResponse;
import com.restaurant.member.entity.MemberProfile;
import com.restaurant.member.entity.Role;
import com.restaurant.member.entity.Staff;
import com.restaurant.member.entity.User;
import com.restaurant.member.repository.MemberProfileRepository;
import com.restaurant.member.repository.RoleRepository;
import com.restaurant.member.repository.StaffRepository;
import com.restaurant.member.repository.UserRepository;
import com.restaurant.member.service.AuthService;
import com.restaurant.member.service.MailService;
import com.restaurant.member.util.JwtUtil;
import com.restaurant.store.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

        private final UserRepository userRepository;
        private final MemberProfileRepository memberProfileRepository;
        private final StaffRepository staffRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;
        private final MailService mailService;

        // 註冊 Email 驗證用
        private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();
        private final Set<String> verifiedEmails = ConcurrentHashMap.newKeySet();

        // 忘記密碼驗證用，避免跟註冊驗證混在一起
        private final Map<String, String> passwordResetCodes = new ConcurrentHashMap<>();

        private static final SecureRandom RANDOM = new SecureRandom();

        // -----Member 帳號註冊（核心 User + 1:1 MemberProfile）-----
        @Override
        @Transactional
        public LoginResponse registerMember(MemberRegisterRequest request) {

                String email = normalizeEmail(request.getEmail());

                if (!verifiedEmails.contains(email)) {
                        throw new BusinessException("請先完成 Email 驗證");
                }

                if (userRepository.existsByEmail(email)) {
                        throw new BusinessException("此 Email 已被註冊");
                }

                if (userRepository.existsByPhone(request.getPhone())) {
                        throw new BusinessException("此手機號碼已被使用");
                }

                Role memberRole = roleRepository.findByRoleName("CUSTOMER")
                                .orElseThrow(() -> new BusinessException("系統角色 CUSTOMER 不存在"));

                User user = User.builder()
                                .role(memberRole)
                                .email(email)
                                .passwordHash(passwordEncoder.encode(request.getPassword()))
                                .name(request.getName())
                                .phone(request.getPhone())
                                .birthday(request.getBirthday())
                                .build();

                userRepository.save(user);

                MemberProfile profile = MemberProfile.builder()
                                .user(user)
                                .build();

                memberProfileRepository.save(profile);

                // 註冊成功後，清掉這次的驗證狀態，避免同一個 email 驗證狀態殘留
                verifiedEmails.remove(email);
                verificationCodes.remove(email);

                String token = jwtUtil.generateToken(
                                user.getUserId(),
                                user.getRole().getRoleName());

                return LoginResponse.builder()
                                .accessToken(token)
                                .tokenType("Bearer")
                                .userId(user.getUserId())
                                .name(user.getName())
                                .roleName(user.getRole().getRoleName())
                                .build();
        }

        // -----Staff 帳號建立（後台管理者建立新員工，或將既有會員轉為員工 / 店長）-----
        @Override
        @Transactional
        public StaffResponse createStaff(StaffCreateRequest request) {

                String email = normalizeEmail(request.getEmail());
                String roleName = normalizeRoleName(request.getRoleName());
                String staffNo = normalizeText(request.getStaffNo());
                String phone = normalizeNullableText(request.getPhone());
                String name = normalizeNullableText(request.getName());
                String password = normalizeNullableText(request.getPassword());

                if (email == null || email.isBlank()) {
                        throw new BusinessException("Email 不可為空");
                }

                if (roleName == null || !("STAFF".equals(roleName) || "MANAGER".equals(roleName))) {
                        throw new BusinessException("員工管理僅能建立員工或店長帳號");
                }

                if (staffNo == null || staffNo.isBlank()) {
                        throw new BusinessException("員工編號不可為空");
                }

                if (request.getStoreId() == null) {
                        throw new BusinessException("門市不可為空");
                }

                if (request.getHireDate() == null) {
                        throw new BusinessException("到職日不可為空");
                }

                Role role = roleRepository.findByRoleName(roleName)
                                .orElseThrow(() -> new BusinessException("指定的系統角色不存在：" + roleName));

                Store store = Store.builder()
                                .storeId(request.getStoreId())
                                .build();

                User existingUser = userRepository.findByEmail(email).orElse(null);

                validateStaffNoUsable(staffNo, existingUser);
                validatePhoneUsable(phone, existingUser);

                if (existingUser == null) {
                        return createNewStaffUser(request, email, password, name, phone, role, store, staffNo);
                }

                return promoteExistingUserToStaff(request, existingUser, role, store, staffNo, phone, name);
        }

        /**
         * Email 不存在時：建立全新的 User + Staff。
         */
        private StaffResponse createNewStaffUser(
                        StaffCreateRequest request,
                        String email,
                        String password,
                        String name,
                        String phone,
                        Role role,
                        Store store,
                        String staffNo) {

                if (password == null || password.isBlank()) {
                        throw new BusinessException("新員工帳號需設定初始密碼");
                }

                if (password.length() < 8 || password.length() > 20) {
                        throw new BusinessException("密碼長度需介於 8 到 20 字元");
                }

                if (name == null || name.isBlank()) {
                        throw new BusinessException("姓名不可為空");
                }

                if (request.getBirthday() == null) {
                        throw new BusinessException("生日不可為空");
                }

                User user = User.builder()
                                .role(role)
                                .email(email)
                                .passwordHash(passwordEncoder.encode(password))
                                .name(name)
                                .phone(phone)
                                .birthday(request.getBirthday())
                                .build();

                userRepository.save(user);

                Staff staff = Staff.builder()
                                .user(user)
                                .store(store)
                                .staffNo(staffNo)
                                .hireDate(request.getHireDate())
                                .status(Staff.StaffStatus.ACTIVE)
                                .build();

                staffRepository.save(staff);

                ensureMemberProfile(user);

                return toStaffResponse(user, staff);
        }

        /**
         * Email 已存在時：若是一般會員，轉為 STAFF / MANAGER，保留原會員密碼、點數與會員資料。
         */
        private StaffResponse promoteExistingUserToStaff(
                        StaffCreateRequest request,
                        User user,
                        Role role,
                        Store store,
                        String staffNo,
                        String phone,
                        String name) {

                String currentRoleName = user.getRole() != null ? user.getRole().getRoleName() : null;

                if ("ADMIN".equals(currentRoleName)) {
                        throw new BusinessException("管理員帳號不可轉為員工或店長");
                }

                Staff existingStaff = staffRepository.findByUser_UserId(user.getUserId()).orElse(null);

                if (existingStaff != null && existingStaff.getStatus() == Staff.StaffStatus.ACTIVE) {
                        throw new BusinessException("此會員已是員工或店長");
                }

                // 保留原會員密碼與點數，只補齊或更新員工需要的基本資料。
                if (name != null && !name.isBlank()) {
                        user.setName(name);
                }

                if (phone != null) {
                        user.setPhone(phone);
                }

                if (request.getBirthday() != null) {
                        user.setBirthday(request.getBirthday());
                }

                if (user.getName() == null || user.getName().isBlank()) {
                        throw new BusinessException("姓名不可為空");
                }

                if (user.getBirthday() == null) {
                        throw new BusinessException("生日不可為空");
                }

                user.setRole(role);
                userRepository.save(user);

                Staff staff;
                if (existingStaff != null) {
                        // 曾經離職的人員重新入職：沿用同一筆 staff，改回 ACTIVE。
                        staff = existingStaff;
                        staff.setStore(store);
                        staff.setStaffNo(staffNo);
                        staff.setHireDate(request.getHireDate());
                        staff.setStatus(Staff.StaffStatus.ACTIVE);
                } else {
                        staff = Staff.builder()
                                        .user(user)
                                        .store(store)
                                        .staffNo(staffNo)
                                        .hireDate(request.getHireDate())
                                        .status(Staff.StaffStatus.ACTIVE)
                                        .build();
                }

                staffRepository.save(staff);
                ensureMemberProfile(user);

                return toStaffResponse(user, staff);
        }

        /**
         * 員工編號不可與其他員工重複；同一位離職員工復職時可沿用自己的員編。
         */
        private void validateStaffNoUsable(String staffNo, User targetUser) {
                Staff staff = staffRepository.findByStaffNo(staffNo).orElse(null);
                if (staff == null) {
                        return;
                }

                if (targetUser != null && staff.getUser() != null
                                && staff.getUser().getUserId().equals(targetUser.getUserId())) {
                        return;
                }

                throw new BusinessException("此員工編號已被使用");
        }

        /**
         * 手機不可與其他帳號重複；既有會員升級員工時可沿用自己的手機。
         */
        private void validatePhoneUsable(String phone, User targetUser) {
                if (phone == null || phone.isBlank()) {
                        return;
                }

                User user = userRepository.findByPhone(phone).orElse(null);
                if (user == null) {
                        return;
                }

                if (targetUser != null && user.getUserId().equals(targetUser.getUserId())) {
                        return;
                }

                throw new BusinessException("此手機號碼已被使用");
        }

        /**
         * 員工 / 店長也是會員；既有會員升級時要保留原本會員資料，新員工則補建會員資料。
         */
        private void ensureMemberProfile(User user) {
                memberProfileRepository.findByUserUserId(user.getUserId())
                                .orElseGet(() -> memberProfileRepository.save(
                                                MemberProfile.builder()
                                                                .user(user)
                                                                .build()));
        }

        // -----統一登入入口（驗證並依據角色簽發 JWT 通行證）-----
        @Override
        @Transactional(readOnly = true)
        public LoginResponse login(LoginRequest request) {

                User user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                                .orElseThrow(() -> new BusinessException("找不到帳號"));

                boolean passwordMatched = passwordEncoder.matches(
                                request.getPassword(),
                                user.getPasswordHash());

                if (!passwordMatched) {
                        throw new BusinessException("帳號或密碼錯誤");
                }

                String token = jwtUtil.generateToken(
                                user.getUserId(),
                                user.getRole().getRoleName());

                return LoginResponse.builder()
                                .accessToken(token)
                                .tokenType("Bearer")
                                .userId(user.getUserId())
                                .name(user.getName())
                                .roleName(user.getRole().getRoleName())
                                .build();
        }

        // -----寄送註冊 Email 驗證碼-----
        @Override
        public void sendEmailVerificationCode(String email) {

                String normalizedEmail = normalizeEmail(email);

                if (userRepository.existsByEmail(normalizedEmail)) {
                        throw new BusinessException("此 Email 已被註冊");
                }

                String code = generateSixDigitCode();

                verificationCodes.put(normalizedEmail, code);
                verifiedEmails.remove(normalizedEmail);

                mailService.sendEmail(
                                normalizedEmail,
                                "敘日會員驗證信",
                                "您好，您的驗證碼為：" + code);
        }

        // -----驗證註冊 Email 驗證碼-----
        @Override
        public boolean verifyEmailCode(String email, String code) {

                String normalizedEmail = normalizeEmail(email);
                String normalizedCode = normalizeCode(code);

                String savedCode = verificationCodes.get(normalizedEmail);

                boolean success = savedCode != null && savedCode.equals(normalizedCode);

                if (success) {
                        verifiedEmails.add(normalizedEmail);
                        verificationCodes.remove(normalizedEmail);
                }

                return success;
        }

        // -----忘記密碼：寄送重設密碼驗證碼-----
        @Override
        public void forgotPassword(String email) {

                String normalizedEmail = normalizeEmail(email);

                User user = userRepository.findByEmail(normalizedEmail)
                                .orElseThrow(() -> new BusinessException("此 Email 不存在"));

                String code = generateSixDigitCode();

                passwordResetCodes.put(normalizeEmail(user.getEmail()), code);

                mailService.sendEmail(
                                user.getEmail(),
                                "敘日會員密碼重設驗證信",
                                "您好，您的密碼重設驗證碼為：" + code);
        }

        // -----忘記密碼：驗證重設密碼驗證碼-----
        @Override
        public boolean verifyPasswordResetCode(String email, String code) {

                String normalizedEmail = normalizeEmail(email);
                String normalizedCode = normalizeCode(code);

                String savedCode = passwordResetCodes.get(normalizedEmail);

                return savedCode != null && savedCode.equals(normalizedCode);
        }

        // -----重設密碼-----
        @Override
        @Transactional
        public void resetPassword(String email, String code, String newPassword) {

                String normalizedEmail = normalizeEmail(email);
                String normalizedCode = normalizeCode(code);

                String savedCode = passwordResetCodes.get(normalizedEmail);

                if (savedCode == null || !savedCode.equals(normalizedCode)) {
                        throw new BusinessException("驗證碼錯誤");
                }

                User user = userRepository.findByEmail(normalizedEmail)
                                .orElseThrow(() -> new BusinessException("找不到帳號"));

                user.setPasswordHash(passwordEncoder.encode(newPassword));

                userRepository.save(user);

                passwordResetCodes.remove(normalizedEmail);
        }

        /**
         * Email 驗證碼 Map 使用統一格式當 key，避免大小寫或空白導致驗證失敗。
         */
        private String normalizeEmail(String email) {
                return email == null ? null : email.trim().toLowerCase();
        }

        private String normalizeRoleName(String roleName) {
                return roleName == null ? null : roleName.trim().toUpperCase();
        }

        private String normalizeText(String text) {
                return text == null ? null : text.trim();
        }

        private String normalizeNullableText(String text) {
                String normalized = normalizeText(text);
                return normalized == null || normalized.isBlank() ? null : normalized;
        }

        /**
         * 驗證碼只保留前後去空白後的內容，避免複製信件時帶到空白。
         */
        private String normalizeCode(String code) {
                return code == null ? null : code.trim();
        }

        /**
         * 產生 6 位數驗證碼
         */
        private String generateSixDigitCode() {
                return String.valueOf(RANDOM.nextInt(900000) + 100000);
        }

        /**
         * 封裝員工回傳格式
         */
        private StaffResponse toStaffResponse(User user, Staff staff) {
                StaffResponse res = new StaffResponse();
                res.setUserId(user.getUserId());
                res.setStaffId(staff.getStaffId());
                res.setEmail(user.getEmail());
                res.setName(user.getName());
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
