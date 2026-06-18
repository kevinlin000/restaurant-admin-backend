<template>
  <div class="member-page">
    <div class="member-shell">
      <!-- 載入中 -->
      <div v-if="isLoading" class="state-box">載入中...</div>

      <!-- 錯誤訊息 -->
      <div v-else-if="errorMsg" class="state-box error-msg">
        {{ errorMsg }}
      </div>

      <template v-else>
        <!-- 左側會員摘要 -->
        <aside class="member-sidebar">
          <div class="sidebar-card level-card">
            <p class="sidebar-title">會員等級</p>

            <div class="level-badge">{{ memberLevelText }}</div>
          </div>

          <div class="sidebar-card point-card">
            <p class="sidebar-title">目前點數</p>

            <div class="point-main">
              <span class="point-number">{{ pointInfo.pointBalance }}</span>
              <span class="point-unit">點</span>
            </div>

            <p v-if="pointInfo.nextLevel" class="upgrade-note">
              離升級{{ getLevelText(pointInfo.nextLevel) }}還差
              <span class="highlight-point">{{
                pointInfo.pointsToNextLevel
              }}</span>
              點
            </p>

            <p v-else class="upgrade-note">您已達最高等級：鑽石卡會員</p>

            <button
              class="point-rule-btn"
              type="button"
              @click="showPointRuleModal = true"
            >
              會員集點規則
            </button>
          </div>

          <div class="birthday-card">
            <div class="birthday-title">🎂 生日優惠</div>
            <div class="birthday-text">
              生日當月於敘日消費，<br />
              即可獲得焦糖布丁 1 份。
            </div>
          </div>
        </aside>

        <!-- 右側內容 -->
        <section class="member-content">
          <!-- 基本資料 -->
          <template v-if="currentView === 'profile'">
            <div class="section-header">
              <h2>會員基本資料</h2>

              <button
                v-if="!isEditingProfile"
                class="edit-profile-btn"
                type="button"
                @click="startEditProfile"
              >
                編輯資料
              </button>
            </div>

            <div class="info-card">
              <div class="info-row">
                <div class="info-label">會員姓名</div>

                <div v-if="!isEditingProfile" class="info-value">
                  {{ userInfo.name }}
                </div>

                <div v-else class="edit-field">
                  <input
                    v-model.trim="profileForm.name"
                    type="text"
                    placeholder="請輸入姓名"
                    maxlength="50"
                  />
                </div>
              </div>

              <div class="info-row">
                <div class="info-label">電話</div>

                <div v-if="!isEditingProfile" class="info-value">
                  {{ userInfo.phone }}
                </div>

                <div v-else class="edit-field">
                  <input
                    v-model.trim="profileForm.phone"
                    type="tel"
                    placeholder="請輸入手機號碼，例如 0912345678"
                    maxlength="10"
                  />

                  <p
                    v-if="profileForm.phone && !isPhoneValid"
                    class="field-error"
                  >
                    手機號碼格式必須為 09xxxxxxxx
                  </p>
                </div>
              </div>

              <div class="info-row">
                <div class="info-label">Email</div>
                <div class="info-value">{{ userInfo.email }}</div>
              </div>

              <div class="info-row">
                <div class="info-label">生日</div>
                <div class="info-value">
                  {{ formatBirthday(userInfo.birthday) }}
                </div>
              </div>

              <div class="info-row password-row">
                <div class="info-label">密碼</div>
                <div class="info-value password-dots">••••••••</div>
                <button
                  class="edit-profile-btn"
                  type="button"
                  @click="openPasswordView"
                >
                  修改密碼
                </button>
              </div>

              <div v-if="isEditingProfile" class="profile-actions">
                <button
                  class="cancel-profile-btn"
                  type="button"
                  :disabled="isSavingProfile"
                  @click="cancelEditProfile"
                >
                  取消
                </button>

                <button
                  class="save-profile-btn"
                  type="button"
                  :disabled="!canSubmitProfile || isSavingProfile"
                  @click="handleUpdateProfile"
                >
                  {{ isSavingProfile ? "儲存中..." : "儲存修改" }}
                </button>
              </div>
            </div>
          </template>

          <!-- 修改密碼 -->
          <template v-else>
            <div class="section-header password-header">
              <button class="back-btn" type="button" @click="backToProfile">
                返回基本資料
              </button>
            </div>

            <div class="password-panel">
              <h2 class="password-title">
                <i class="bi bi-lock-fill"></i>
                修改密碼
              </h2>

              <form
                class="password-form"
                @submit.prevent="handleUpdatePassword"
              >
                <div class="form-group">
                  <label>目前密碼</label>
                  <div class="password-input-wrap">
                    <input
                      v-model="passwordForm.oldPassword"
                      :type="showOldPassword ? 'text' : 'password'"
                      placeholder="請輸入目前密碼"
                    />
                    <span
                      class="eye-icon"
                      @click="showOldPassword = !showOldPassword"
                    >
                      <i
                        :class="
                          showOldPassword ? 'bi bi-eye' : 'bi bi-eye-slash'
                        "
                      ></i>
                    </span>
                  </div>
                </div>

                <div class="form-group">
                  <label>新密碼</label>
                  <div class="password-input-wrap">
                    <input
                      v-model="passwordForm.newPassword"
                      :type="showNewPassword ? 'text' : 'password'"
                      placeholder="請輸入新密碼"
                    />
                    <span
                      class="eye-icon"
                      @click="showNewPassword = !showNewPassword"
                    >
                      <i
                        :class="
                          showNewPassword ? 'bi bi-eye' : 'bi bi-eye-slash'
                        "
                      ></i>
                    </span>
                  </div>
                </div>

                <div class="form-group">
                  <label>確認新密碼</label>
                  <div class="password-input-wrap">
                    <input
                      v-model="passwordForm.confirmPassword"
                      :type="showConfirmPassword ? 'text' : 'password'"
                      placeholder="再次輸入新密碼"
                    />
                    <span
                      class="eye-icon"
                      @click="showConfirmPassword = !showConfirmPassword"
                    >
                      <i
                        :class="
                          showConfirmPassword ? 'bi bi-eye' : 'bi bi-eye-slash'
                        "
                      ></i>
                    </span>
                  </div>
                </div>

                <p v-if="passwordError" class="password-error">
                  <i class="bi bi-exclamation-circle-fill"></i>
                  {{ passwordError }}
                </p>

                <ul class="password-rules">
                  <li :class="{ passed: isLengthValid }">
                    <i class="bi bi-check-circle-fill"></i>
                    密碼長度需為 8 到 20 個字元
                  </li>
                  <li :class="{ passed: isConfirmMatched }">
                    <i class="bi bi-check-circle-fill"></i>
                    兩次輸入的新密碼需一致
                  </li>
                  <li :class="{ passed: isDifferentFromOld }">
                    <i class="bi bi-check-circle-fill"></i>
                    新密碼不可與目前密碼相同
                  </li>
                </ul>

                <div class="password-actions">
                  <button
                    class="submit-btn"
                    type="submit"
                    :disabled="!canSubmitPassword || isChangingPassword"
                  >
                    {{ isChangingPassword ? "修改中..." : "確認修改" }}
                  </button>
                </div>
              </form>
            </div>
          </template>
        </section>
      </template>
    </div>

    <!-- 集點規則彈窗 -->
    <div
      v-if="showPointRuleModal"
      class="rule-modal-mask"
      @click.self="showPointRuleModal = false"
    >
      <div class="rule-modal">
        <button
          class="rule-close-btn"
          type="button"
          aria-label="關閉集點規則"
          @click="showPointRuleModal = false"
        >
          ×
        </button>

        <h3>集點規則</h3>

        <div class="rule-content">
          <div class="rule-section">
            <h4>累積規則</h4>
            <p>每消費 $100 即可累積 1 點。</p>
          </div>

          <div class="rule-section">
            <h4>會員升級</h4>
            <ul>
              <li>銅卡會員：0 ~ 29 點，來店消費享95折</li>
              <li>銀卡會員：30 ~ 59 點，來店消費享9折</li>
              <li>金卡會員：60 ~ 99 點，來店消費享85折</li>
              <li>鑽石會員：達到100 點，來店消費享8折</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import Swal from "sweetalert2";
import {
  getPointBalance,
  getProfile,
  updatePassword,
  updateProfile,
} from "@/api/member";

const router = useRouter();
const isLoading = ref(true);
const errorMsg = ref("");
const currentView = ref("profile");
const showPointRuleModal = ref(false);
const userInfo = ref({
  name: "",
  email: "",
  phone: "",
  birthday: "",
  memberLevel: "BRONZE",
  pointBalance: 0,
});

const pointInfo = ref({
  pointBalance: 0,
  memberLevel: "BRONZE",
  nextLevel: "SILVER",
  pointsToNextLevel: 30,
  earnRuleText: "每消費 $100 即可累積 1 點",
});

const profileForm = ref({
  name: "",
  phone: "",
});

const isEditingProfile = ref(false);
const isSavingProfile = ref(false);

const passwordForm = ref({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const passwordError = ref("");
const isChangingPassword = ref(false);

const showOldPassword = ref(false);
const showNewPassword = ref(false);
const showConfirmPassword = ref(false);

onMounted(async () => {
  const token = localStorage.getItem("accessToken");

  if (!token) {
    router.push("/login");
    return;
  }

  try {
    await loadMemberPageData();
  } catch (err) {
    errorMsg.value = "無法取得會員資料，請重新登入";
  } finally {
    isLoading.value = false;
  }
});

const loadMemberPageData = async () => {
  const [profileRes, pointRes] = await Promise.all([
    getProfile(),
    getPointBalance(),
  ]);

  const profileData = profileRes.data.data;
  const pointData = pointRes.data.data;

  pointInfo.value = {
    pointBalance: pointData.pointBalance ?? 0,
    memberLevel: pointData.memberLevel || "BRONZE",
    nextLevel: pointData.nextLevel || null,
    pointsToNextLevel: pointData.pointsToNextLevel ?? 0,
    earnRuleText: pointData.earnRuleText || "每消費 $100 即可累積 1 點",
  };

  userInfo.value = {
    name: profileData.name,
    email: profileData.email,
    phone: profileData.phone,
    birthday: profileData.birthday,
    memberLevel: pointInfo.value.memberLevel || profileData.memberLevel,
    pointBalance: pointInfo.value.pointBalance,
  };
};

const getLevelText = (level) => {
  const levels = {
    BRONZE: "銅卡會員",
    SILVER: "銀卡會員",
    GOLD: "金卡會員",
    DIAMOND: "鑽石卡會員",
  };

  return levels[level] || "一般會員";
};

const getLevelByPoint = (point) => {
  const points = Number(point) || 0;
  if (points >= 100) return "DIAMOND";
  if (points >= 60) return "GOLD";
  if (points >= 30) return "SILVER";
  return "BRONZE";
};

const memberLevelText = computed(() => {
  return getLevelText(getLevelByPoint(pointInfo.value.pointBalance));
});

const pointUpgradeHint = computed(() => {
  const nextLevelText = getLevelText(pointInfo.value.nextLevel);

  if (!pointInfo.value.nextLevel) {
    return "您已達最高等級：鑽石卡會員";
  }

  return `距離升級${nextLevelText}還差 ${pointInfo.value.pointsToNextLevel} 點`;
});

const isPhoneValid = computed(() => {
  return /^09\d{8}$/.test(profileForm.value.phone);
});

const canSubmitProfile = computed(() => {
  const name = profileForm.value.name.trim();
  const phone = profileForm.value.phone.trim();

  const hasChanged =
    name !== userInfo.value.name || phone !== userInfo.value.phone;

  return Boolean(name) && isPhoneValid.value && hasChanged;
});

const birthdayMonth = computed(() => {
  if (!userInfo.value.birthday) return null;

  return new Date(userInfo.value.birthday).getMonth() + 1;
});

const isBirthdayMonth = computed(() => {
  if (!userInfo.value?.birthday) return false;

  const birthdayMonth = new Date(userInfo.value.birthday).getMonth() + 1;
  const currentMonth = new Date().getMonth() + 1;

  return birthdayMonth === currentMonth;
});

const isLengthValid = computed(() => {
  const length = passwordForm.value.newPassword.length;
  return length >= 8 && length <= 20;
});

const isConfirmMatched = computed(() => {
  if (!passwordForm.value.confirmPassword) return false;
  return passwordForm.value.newPassword === passwordForm.value.confirmPassword;
});

const isDifferentFromOld = computed(() => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    return false;
  }

  return passwordForm.value.oldPassword !== passwordForm.value.newPassword;
});

const canSubmitPassword = computed(() => {
  return (
    Boolean(passwordForm.value.oldPassword) &&
    Boolean(passwordForm.value.newPassword) &&
    Boolean(passwordForm.value.confirmPassword) &&
    isLengthValid.value &&
    isConfirmMatched.value &&
    isDifferentFromOld.value
  );
});

const startEditProfile = () => {
  profileForm.value = {
    name: userInfo.value.name || "",
    phone: userInfo.value.phone || "",
  };

  isEditingProfile.value = true;
};

const cancelEditProfile = () => {
  profileForm.value = {
    name: userInfo.value.name || "",
    phone: userInfo.value.phone || "",
  };

  isEditingProfile.value = false;
};

const handleUpdateProfile = async () => {
  if (!canSubmitProfile.value || isSavingProfile.value) return;

  isSavingProfile.value = true;

  try {
    const res = await updateProfile({
      name: profileForm.value.name.trim(),
      phone: profileForm.value.phone.trim(),
    });

    const data = res.data.data;

    userInfo.value = {
      ...userInfo.value,
      name: data.name,
      phone: data.phone,
    };

    localStorage.setItem(
      "userInfo",
      JSON.stringify({
        ...JSON.parse(localStorage.getItem("userInfo") || "{}"),
        name: data.name,
        phone: data.phone,
      }),
    );

    window.dispatchEvent(new Event("login-state-changed"));

    isEditingProfile.value = false;

    await Swal.fire({
      icon: "success",
      title: "資料已更新",
      text: "會員姓名與電話已成功修改",
      confirmButtonColor: "#d9a372",
    });
  } catch (err) {
    await Swal.fire({
      icon: "error",
      title: "資料更新失敗",
      text: err.response?.data?.message || "請稍後再試",
      confirmButtonColor: "#d9a372",
    });
  } finally {
    isSavingProfile.value = false;
  }
};

const formatBirthday = (birthday) => {
  if (!birthday) return "未提供";
  return birthday.replaceAll("-", "/");
};

const openPasswordView = () => {
  resetPasswordForm();
  currentView.value = "password";
};

const backToProfile = () => {
  resetPasswordForm();
  currentView.value = "profile";
};

const resetPasswordForm = () => {
  passwordForm.value = {
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  };

  passwordError.value = "";
  showOldPassword.value = false;
  showNewPassword.value = false;
  showConfirmPassword.value = false;
};

const validatePasswordForm = () => {
  if (
    !passwordForm.value.oldPassword ||
    !passwordForm.value.newPassword ||
    !passwordForm.value.confirmPassword
  ) {
    passwordError.value = "請完整填寫所有欄位";
    return false;
  }

  if (!isLengthValid.value) {
    passwordError.value = "新密碼長度需為 8 到 20 個字元";
    return false;
  }

  if (!isConfirmMatched.value) {
    passwordError.value = "兩次輸入的新密碼不一致";
    return false;
  }

  if (!isDifferentFromOld.value) {
    passwordError.value = "新密碼不可與目前密碼相同";
    return false;
  }

  passwordError.value = "";
  return true;
};

const handleUpdatePassword = async () => {
  if (!validatePasswordForm()) return;

  isChangingPassword.value = true;

  try {
    await updatePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
    });

    await Swal.fire({
      icon: "success",
      title: "密碼修改成功",
      text: "請使用新密碼登入您的帳號",
      confirmButtonColor: "#d9a372",
    });

    resetPasswordForm();
    currentView.value = "profile";
  } catch (err) {
    passwordError.value =
      err.response?.data?.message || "密碼修改失敗，請稍後再試";
  } finally {
    isChangingPassword.value = false;
  }
};

const logout = async () => {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("userInfo");
  window.dispatchEvent(new Event("login-state-changed"));

  await Swal.fire({
    icon: "success",
    title: "已登出",
    text: "期待再次與您見面",
    timer: 1500,
    showConfirmButton: false,
  });

  router.push("/home");
};
</script>

<style scoped>
.member-page {
  min-height: 100vh;
  padding: 150px 32px 80px;
  background: #f8f3ed;
  color: #566a7f;
}

.member-shell {
  width: min(1180px, 100%);
  margin: 0 auto;
}

.state-box {
  width: min(520px, 100%);
  margin: 80px auto;
  padding: 40px;
  border-radius: 16px;
  background: #fff;
  text-align: center;
  box-shadow: 0 8px 24px rgba(86, 106, 127, 0.12);
}

.error-msg {
  color: #e11d48;
}

.member-shell > template,
.member-shell {
  position: relative;
}

.member-content {
  background: transparent;
}

.member-sidebar {
  float: left;
  width: 240px;
  padding-top: 8px;
  padding-right: 10px;
}

.member-content {
  margin-left: 280px;
}

.sidebar-card {
  width: 240px;
  margin-bottom: 26px;
  padding-bottom: 26px;
  border-bottom: 1px solid #e6ded5;
}

.sidebar-title {
  margin: 0 0 14px;
  color: #3d4651;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: 0.02em;
}

.level-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 240px;
  min-height: 52px;
  box-sizing: border-box;
  padding: 0 12px;
  border: 1px solid #ead5c3;
  border-radius: 9px;
  background: rgba(255, 250, 247, 0.9);
  color: #e3ac7f;
  font-size: 18px;
  font-weight: 800;
  line-height: 1;
}

.point-card {
  padding-top: 0;
}

.point-main {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin: 4px 0 12px;
}

.point-number {
  color: #e3ac7f;
  font-size: 48px;
  font-weight: 900;
  line-height: 1;
}

.point-unit {
  color: #e3ac7f;
  font-size: 20px;
  font-weight: 800;
}

.point-rule {
  margin: 0 0 10px;
  color: #6b7c8f;
  font-size: 16px;
  line-height: 2;
}

.upgrade-note {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  margin: 0;
  color: #7a5f4c;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.8;
}

.highlight-point {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 30px;
  height: 30px;
  margin: 0 2px;
  border-radius: 999px;
  background: #e3ac7f;
  color: #fff;
  font-size: 18px;
  font-weight: 900;
  box-shadow: 0 6px 14px rgba(227, 172, 127, 0.45);
  animation: pointPulse 1.5s ease-in-out infinite;
}

@keyframes pointPulse {
  0%,
  100% {
    transform: scale(1);
    box-shadow: 0 6px 14px rgba(227, 172, 127, 0.45);
  }

  50% {
    transform: scale(1.12);
    box-shadow: 0 9px 20px rgba(227, 172, 127, 0.65);
  }
}

.birthday-card {
  width: 240px;
  margin-top: 0;
  padding: 18px 20px;
  border-left: 4px solid #e3ac7f;
  border-radius: 10px;
  background: rgba(255, 247, 240, 0.92);
  color: #8a6f5a;
}

.birthday-title {
  margin-bottom: 8px;
  color: #d97706;
  font-size: 15px;
  font-weight: 800;
}

.birthday-text {
  color: #8a6f5a;
  font-size: 15px;
  line-height: 1.75;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22px;
}

.section-header h2 {
  margin: 0;
  color: #3d4651;
  font-size: 28px;
  font-weight: 800;
}

.save-text {
  color: #8a6f5a;
  font-size: 15px;
}

.info-card,
.password-panel {
  min-height: 560px;
  padding: 38px 42px;
  border: 1px solid #e6ded5;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 10px 24px rgba(86, 106, 127, 0.08);
}

.info-row {
  display: grid;
  grid-template-columns: 90px 1fr auto;
  gap: 18px;
  align-items: center;
  min-height: 74px;
  border-bottom: 1px solid #e6ded5;
}

.info-label {
  color: #566a7f;
  font-size: 14px;
}

.info-value {
  color: #2f3a45;
  font-size: 16px;
  font-weight: 600;
  word-break: break-word;
}

.password-dots {
  letter-spacing: 8px;
  color: #566a7f;
}

.text-action {
  border: none;
  background: transparent;
  color: #8a6f5a;
  font-size: 15px;
  cursor: pointer;
}

.text-action:hover {
  color: #e3ac7f;
}

.edit-profile-btn {
  padding: 10px 18px;
  border: none;
  border-radius: 8px;
  background: #e3ac7f;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  transition: 0.2s;
}

.edit-profile-btn:hover {
  background: #d49a68;
}

.edit-field {
  width: 100%;
}

.edit-field input {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  border: 1px solid #d7ccc0;
  border-radius: 8px;
  outline: none;
  background: #fff;
  color: #3d4651;
  font-size: 15px;
  transition: 0.2s;
}

.edit-field input:focus {
  border-color: #e3ac7f;
  box-shadow: 0 0 0 3px rgba(227, 172, 127, 0.16);
}

.field-error {
  margin: 6px 0 0;
  color: #e11d48;
  font-size: 13px;
}

.profile-actions {
  display: flex;
  justify-content: center;
  gap: 14px;
  margin-top: 32px;
}

.cancel-profile-btn,
.save-profile-btn {
  width: 160px;
  height: 46px;
  border: none;
  border-radius: 8px;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  transition: 0.2s;
}

.cancel-profile-btn {
  background: #8b98a6;
}

.cancel-profile-btn:hover:not(:disabled) {
  background: #7b8794;
}

.save-profile-btn {
  background: #e3ac7f;
}

.save-profile-btn:hover:not(:disabled) {
  background: #d49a68;
}

.cancel-profile-btn:disabled,
.save-profile-btn:disabled {
  background: #c8c8c8;
  color: #ffffff;
  opacity: 0.8;
  cursor: not-allowed;
}

.password-header {
  justify-content: flex-start;
}

.back-btn {
  background: #e3ac7f;
  color: #fff;
  border: none;
  border-radius: 12px;
  padding: 12px 24px;
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.25s ease;
}

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(227, 172, 127, 0.3);
}

.password-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.password-title {
  display: flex;
  gap: 10px;
  align-items: center;
  margin: 20px 0 34px;
  color: #3d4651;
  font-size: 30px;
  font-weight: 800;
}

.password-title i {
  color: #e3ac7f;
}

.password-form {
  width: min(440px, 100%);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #566a7f;
  font-weight: 700;
}

.password-input-wrap {
  position: relative;
}

.password-input-wrap input {
  width: 100%;
  height: 48px;
  padding: 0 46px 0 14px;
  border: 1px solid #d7ccc0;
  border-radius: 8px;
  outline: none;
  background: #fff;
  color: #3d4651;
  font-size: 15px;
  transition: 0.2s;
}

.password-input-wrap input:focus {
  border-color: #e3ac7f;
  box-shadow: 0 0 0 3px rgba(227, 172, 127, 0.16);
}

.eye-icon {
  position: absolute;
  top: 50%;
  right: 14px;
  transform: translateY(-50%);
  color: #7b8794;
  cursor: pointer;
}

.eye-icon:hover {
  color: #e3ac7f;
}

.password-error {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 0 16px;
  color: #e11d48;
  font-size: 14px;
}

.password-rules {
  list-style: none;
  padding: 0;
  margin: 8px 0 28px;
}

.password-rules li {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #9aa3ad;
  font-size: 14px;
}

.password-rules li.passed {
  color: #2f9e44;
}

.password-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.submit-btn {
  width: 280px;
  height: 48px;
  border: none;
  border-radius: 8px;
  background: #e3ac7f;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  transition: 0.2s;
}

.submit-btn:hover:not(:disabled) {
  background: #d49a68;
}

.submit-btn:disabled {
  background: #c8c8c8;
  color: #ffffff;
  opacity: 0.8;
  cursor: not-allowed;
}

.points-section {
  margin-top: 24px;
}

.points-summary-card {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: center;
}

.points-card-label {
  margin: 0 0 6px;
  color: #7b8794;
  font-size: 14px;
}

.points-summary-card h3 {
  margin: 0;
  color: #e3ac7f;
  font-size: 32px;
  font-weight: 800;
}

.points-summary-detail {
  color: #566a7f;
  font-size: 15px;
  line-height: 1.7;
  text-align: right;
}

.points-summary-detail p {
  margin: 0;
}

.points-history-card {
  margin-top: 18px;
}

.points-history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.points-history-header h3 {
  margin: 0;
  color: #3d4651;
  font-size: 22px;
  font-weight: 800;
}

.points-history-header span {
  color: #8a6f5a;
  font-size: 14px;
}

.empty-history {
  padding: 22px;
  border-radius: 10px;
  background: #fff7f0;
  color: #8a6f5a;
  text-align: center;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.point-rule-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 240px;
  min-height: 46px;
  box-sizing: border-box;
  margin-top: 12px;
  padding: 0 12px;
  border: 1px solid #ead5c3;
  border-radius: 999px;
  background: rgba(255, 250, 247, 0.95);
  color: #d99a63;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
  transition: 0.2s;
}

.point-rule-btn:hover {
  background: #e3ac7f;
  color: #fff;
  border-color: #e3ac7f;
}

.rule-modal-mask {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(47, 58, 69, 0.42);
}

.rule-modal {
  position: relative;
  width: min(420px, 100%);
  padding: 30px 32px 28px;
  border-radius: 18px;
  background: #fffaf6;
  box-shadow: 0 18px 48px rgba(47, 58, 69, 0.2);
}

.rule-close-btn {
  position: absolute;
  top: 14px;
  right: 18px;
  border: none;
  background: transparent;
  color: #8a6f5a;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}

.rule-close-btn:hover {
  color: #d49a68;
}

.rule-modal h3 {
  margin: 0 0 20px;
  color: #3d4651;
  font-size: 26px;
  font-weight: 900;
}

.rule-section {
  padding: 16px 0;
  border-top: 1px solid #eadfd4;
}

.rule-section h4 {
  margin: 0 0 10px;
  color: #d97706;
  font-size: 16px;
  font-weight: 900;
}

.rule-section p {
  margin: 0;
  color: #6b7c8f;
  font-size: 15px;
  line-height: 1.8;
}

.rule-section ul {
  margin: 0;
  padding-left: 20px;
  color: #6b7c8f;
  font-size: 15px;
  line-height: 2;
}

@media (max-width: 900px) {
  .member-page {
    padding: 130px 18px 60px;
  }

  .member-sidebar {
    float: none;
    width: 100%;
    padding-right: 0;
    margin-bottom: 24px;
  }

  .member-content {
    margin-left: 0;
  }

  .birthday-card {
    width: 100%;
  }

  .info-card,
  .password-panel {
    min-height: auto;
    padding: 28px 22px;
  }

  .info-row {
    grid-template-columns: 82px 1fr;
  }

  .profile-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .cancel-profile-btn,
  .save-profile-btn {
    width: 100%;
  }

  .points-summary-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .points-summary-detail {
    text-align: left;
  }

  .history-item {
    align-items: flex-start;
    gap: 12px;
  }

  .text-action {
    grid-column: 2 / 3;
    justify-self: flex-start;
  }
}
</style>
