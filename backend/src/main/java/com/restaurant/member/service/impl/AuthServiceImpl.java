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

                if (!verifiedEmails.contains(request.getEmail())) {
                        throw new BusinessException("請先完成 Email 驗證");
                }

                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new BusinessException("此 Email 已被註冊");
                }

                if (userRepository.existsByPhone(request.getPhone())) {
                        throw new BusinessException("此手機號碼已被使用");
                }

                Role memberRole = roleRepository.findByRoleName("CUSTOMER")
                                .orElseThrow(() -> new BusinessException("系統角色 CUSTOMER 不存在"));

                User user = User.builder()
                                .role(memberRole)
                                .email(request.getEmail())
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
                verifiedEmails.remove(request.getEmail());
                verificationCodes.remove(request.getEmail());

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

        // -----Staff 帳號建立（後台管理者幫員工建立核心 User + 1:1 Staff）-----
        @Override
        @Transactional
        public StaffResponse createStaff(StaffCreateRequest request) {

                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new BusinessException("此 Email 已被使用");
                }

                Role role = roleRepository.findByRoleName(request.getRoleName())
                                .orElseThrow(() -> new BusinessException("指定的系統角色不存在：" + request.getRoleName()));

                Store store = Store.builder()
                                .storeId(request.getStoreId())
                                .build();

                User user = User.builder()
                                .role(role)
                                .email(request.getEmail())
                                .passwordHash(passwordEncoder.encode(request.getPassword()))
                                .name(request.getName())
                                .phone(request.getPhone())
                                .build();

                userRepository.save(user);

                Staff staff = Staff.builder()
                                .user(user)
                                .store(store)
                                .staffNo(request.getStaffNo())
                                .hireDate(request.getHireDate())
                                .build();

                staffRepository.save(staff);

                return toStaffResponse(user, staff);
        }

        // -----統一登入入口（驗證並依據角色簽發 JWT 通行證）-----
        @Override
        @Transactional(readOnly = true)
        public LoginResponse login(LoginRequest request) {

                User user = userRepository.findByEmail(request.getEmail())
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

                if (userRepository.existsByEmail(email)) {
                        throw new BusinessException("此 Email 已被註冊");
                }

                String code = generateSixDigitCode();

                verificationCodes.put(email, code);
                verifiedEmails.remove(email);

                mailService.sendEmail(
                                email,
                                "敘日會員驗證信",
                                "您好，您的驗證碼為：" + code);
        }

        // -----驗證註冊 Email 驗證碼-----
        @Override
        public boolean verifyEmailCode(String email, String code) {

                String savedCode = verificationCodes.get(email);

                boolean success = savedCode != null && savedCode.equals(code);

                if (success) {
                        verifiedEmails.add(email);
                        verificationCodes.remove(email);
                }

                return success;
        }

        // -----忘記密碼：寄送重設密碼驗證碼-----
        @Override
        public void forgotPassword(String email) {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new BusinessException("此 Email 不存在"));

                String code = generateSixDigitCode();

                passwordResetCodes.put(user.getEmail(), code);

                mailService.sendEmail(
                                user.getEmail(),
                                "敘日會員密碼重設驗證信",
                                "您好，您的密碼重設驗證碼為：" + code);
        }

        // -----重設密碼-----
        @Override
        @Transactional
        public void resetPassword(String email, String code, String newPassword) {

                String savedCode = passwordResetCodes.get(email);

                if (savedCode == null || !savedCode.equals(code)) {
                        throw new BusinessException("驗證碼錯誤");
                }

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new BusinessException("找不到帳號"));

                user.setPasswordHash(passwordEncoder.encode(newPassword));

                userRepository.save(user);

                passwordResetCodes.remove(email);
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
                res.setRoleName(user.getRole().getRoleName());
                res.setStoreId(staff.getStore().getStoreId());
                res.setStaffNo(staff.getStaffNo());
                res.setHireDate(staff.getHireDate());
                res.setStatus(staff.getStatus() != null ? staff.getStatus().name() : null);
                return res;
        }
}
