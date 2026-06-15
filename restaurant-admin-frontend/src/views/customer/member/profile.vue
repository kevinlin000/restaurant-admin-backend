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
          <div class="member-name">{{ userInfo.name }}</div>
          <div class="member-meta">
            <span class="level-badge">{{ memberLevelText }}</span>
          </div>

          <div class="side-divider"></div>

          <div class="side-info">
            <p class="side-label">目前點數</p>
            <p class="side-value">{{ userInfo.pointBalance }} 點</p>
          </div>

          <div class="side-info">
            <p class="side-label">每消費 $100 即可累積 1 點</p>
            <p class="side-note">{{ upgradeHint }}</p>
          </div>

          <div v-if="birthdayCountdown !== null" class="birthday-box">
            <p v-if="birthdayCountdown === 0" class="birthday-active">
              🎉 生日快樂！快來店領取您的生日專屬禮！
            </p>
            <p v-else>
              距離您的生日還有
              <strong>{{ birthdayCountdown }}</strong
              >天
            </p>
          </div>

          <button class="logout-link" type="button" @click="logout">
            登出
          </button>
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
                  class="text-action"
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
                ← 返回基本資料
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import Swal from "sweetalert2";
import { getProfile, updatePassword, updateProfile } from "@/api/member";

const router = useRouter();

const isLoading = ref(true);
const errorMsg = ref("");
const currentView = ref("profile");

const userInfo = ref({
  name: "",
  email: "",
  phone: "",
  birthday: "",
  memberLevel: "BRONZE",
  pointBalance: 0,
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
    const res = await getProfile();
    const data = res.data.data;

    userInfo.value = {
      name: data.name,
      email: data.email,
      phone: data.phone,
      birthday: data.birthday,
      memberLevel: data.memberLevel,
      pointBalance: data.pointBalance,
    };
  } catch (err) {
    errorMsg.value = "無法取得會員資料，請重新登入";
  } finally {
    isLoading.value = false;
  }
});

const memberLevelText = computed(() => {
  const levels = {
    BRONZE: "銅卡會員",
    SILVER: "銀卡會員",
    GOLD: "金卡會員",
    DIAMOND: "鑽石卡會員",
  };

  return levels[userInfo.value.memberLevel] || "一般會員";
});

const upgradeHint = computed(() => {
  const points = Number(userInfo.value.pointBalance) || 0;

  if (points < 30) {
    return `距離升級銀卡會員還差 ${30 - points} 點`;
  }

  if (points < 60) {
    return `距離升級金卡會員還差 ${60 - points} 點`;
  }

  if (points < 100) {
    return `距離升級鑽石卡會員還差 ${100 - points} 點`;
  }

  return "您已達最高等級：鑽石卡會員";
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

const birthdayCountdown = computed(() => {
  if (!userInfo.value.birthday) return null;

  const today = new Date();
  const birthday = new Date(userInfo.value.birthday);
  const birthdayThisYear = new Date(
    today.getFullYear(),
    birthday.getMonth(),
    birthday.getDate(),
  );

  let nextBirthday = birthdayThisYear;

  if (today > birthdayThisYear) {
    nextBirthday = new Date(
      today.getFullYear() + 1,
      birthday.getMonth(),
      birthday.getDate(),
    );
  }

  return Math.ceil((nextBirthday - today) / (1000 * 60 * 60 * 24));
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

.member-sidebar,
.member-content {
  background: transparent;
}

.member-sidebar {
  float: left;
  width: 220px;
  padding-right: 28px;
}

.member-content {
  margin-left: 250px;
}

.member-name {
  font-size: 28px;
  font-weight: 800;
  color: #3d4651;
  margin-bottom: 8px;
}

.member-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
}

.level-badge {
  padding: 4px 10px;
  border: 1px solid #ead5c3;
  border-radius: 6px;
  background: #fffaf7;
  color: #e3ac7f;
  font-size: 13px;
}

.side-divider {
  height: 1px;
  background: #e6ded5;
  margin: 24px 0;
}

.side-info {
  margin-bottom: 20px;
}

.side-label {
  margin: 0 0 6px;
  font-size: 14px;
  color: #7b8794;
}

.side-value {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #e3ac7f;
}

.side-note {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #8a6f5a;
}

.birthday-box {
  margin: 20px 0;
  padding: 14px;
  border-left: 4px solid #e3ac7f;
  border-radius: 8px;
  background: #fff7f0;
  color: #8a6f5a;
  font-size: 14px;
  line-height: 1.6;
}

.birthday-box p {
  margin: 0;
}

.birthday-active {
  color: #d97706;
  font-weight: 700;
}

.logout-link {
  margin-top: 10px;
  padding: 0;
  border: none;
  background: transparent;
  color: #566a7f;
  font-size: 15px;
  cursor: pointer;
}

.logout-link:hover {
  color: #e3ac7f;
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
  border: none;
  background: transparent;
  color: #8a6f5a;
  font-size: 15px;
  cursor: pointer;
}

.back-btn:hover {
  color: #e3ac7f;
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

  .text-action {
    grid-column: 2 / 3;
    justify-self: flex-start;
  }
}
</style>
