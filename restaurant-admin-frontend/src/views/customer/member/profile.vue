<template>
  <div class="member-page">
    <div class="member-shell">
      <div v-if="isLoading" class="state-box">載入中...</div>

      <div v-else-if="errorMsg" class="state-box error-msg">
        {{ errorMsg }}
      </div>

      <template v-else>
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
              距離升級{{ getLevelText(pointInfo.nextLevel) }}還差
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

          <div class="birthday-card" :class="{ active: isBirthdayMonth }">
            <div class="birthday-title">🎂 生日優惠</div>
            <div class="birthday-text">
              生日當月於敘日消費，<br />即可獲得焦糖布丁 1 份。
            </div>
          </div>
        </aside>

        <section class="member-content">
          <div class="tab-bar">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              class="tab-btn"
              :class="{ active: activeTab === tab.key }"
              type="button"
              @click="switchTab(tab.key)"
            >
              <i :class="tab.icon"></i>
              {{ tab.label }}
            </button>
          </div>

          <template v-if="activeTab === 'profile'">
            <div class="section-header">
              <div>
                <h2>{{ profileTitle }}</h2>
                <p v-if="isEditingProfile">{{ profileDescription }}</p>
              </div>

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
                <div class="info-label">{{ nameLabel }}</div>
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

              <div class="info-row readonly-row">
                <div class="info-label">Email</div>
                <div class="info-value">{{ userInfo.email }}</div>
              </div>

              <div class="info-row readonly-row">
                <div class="info-label">生日</div>
                <div class="info-value">
                  {{ formatBirthday(userInfo.birthday) }}
                </div>
              </div>

              <div class="info-row password-row">
                <div class="info-label">密碼</div>
                <div class="info-value password-dots">••••••••</div>
                <button
                  class="edit-profile-btn outline"
                  type="button"
                  @click="openPasswordModal"
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

          <template v-else-if="activeTab === 'reservations'">
            <div class="section-header">
              <div>
                <h2>訂位紀錄</h2>
                <p>查看會員的訂位紀錄。</p>
              </div>
            </div>
            <div class="empty-card">
              <i class="bx bx-calendar-check"></i>
              <h3>尚無訂位紀錄</h3>
              <p>目前沒有可顯示的訂位資料。</p>
            </div>
          </template>

          <template v-else-if="activeTab === 'orders'">
            <div class="section-header">
              <div>
                <h2>消費紀錄</h2>
                <p>查看會員的消費與點數累積紀錄。</p>
              </div>
            </div>
            <div class="empty-card">
              <i class="bx bx-receipt"></i>
              <h3>尚無消費紀錄</h3>
              <p>目前沒有可顯示的消費資料。</p>
            </div>
          </template>
        </section>
      </template>
    </div>

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
              <li>銅卡會員：0 ~ 29 點，來店消費享 95 折</li>
              <li>銀卡會員：30 ~ 59 點，來店消費享 9 折</li>
              <li>金卡會員：60 ~ 99 點，來店消費享 85 折</li>
              <li>鑽石會員：達到 100 點，來店消費享 8 折</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="showPasswordModal"
      class="rule-modal-mask"
      @click.self="closePasswordModal"
    >
      <div class="password-modal">
        <button
          class="rule-close-btn"
          type="button"
          aria-label="關閉修改密碼"
          @click="closePasswordModal"
        >
          ×
        </button>
        <h3>修改密碼</h3>
        <form class="password-form" @submit.prevent="handleUpdatePassword">
          <label>
            目前密碼
            <input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="請輸入目前密碼"
            />
          </label>
          <label>
            新密碼
            <input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="8 到 20 個字元"
            />
          </label>
          <label>
            確認新密碼
            <input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="再次輸入新密碼"
            />
          </label>

          <p v-if="passwordError" class="field-error">{{ passwordError }}</p>

          <ul class="password-rules">
            <li :class="{ passed: isLengthValid }">
              密碼長度需為 8 到 20 個字元
            </li>
            <li :class="{ passed: isConfirmMatched }">
              兩次輸入的新密碼需一致
            </li>
            <li :class="{ passed: isDifferentFromOld }">
              新密碼不可與目前密碼相同
            </li>
          </ul>

          <button
            class="save-profile-btn full"
            type="submit"
            :disabled="!canSubmitPassword || isChangingPassword"
          >
            {{ isChangingPassword ? "修改中..." : "確認修改" }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import Swal from "sweetalert2";
import {
  getPointBalance,
  getProfile,
  updatePassword,
  updateProfile,
} from "@/api/member";

const router = useRouter();
const route = useRoute();
const isLoading = ref(true);
const errorMsg = ref("");
const activeTab = ref("profile");
const showPointRuleModal = ref(false);
const showPasswordModal = ref(false);

const getStoredUserInfo = () => {
  try {
    return JSON.parse(localStorage.getItem("userInfo") || "{}");
  } catch (error) {
    localStorage.removeItem("userInfo");
    return {};
  }
};

const roleName = computed(() => getStoredUserInfo()?.roleName || "CUSTOMER");

const profileTitle = computed(() => "會員基本資料");
const profileDescription = computed(
  () => "Email與生日會影響帳號與優惠，故不開放自行修改。",
);
const nameLabel = computed(() => "會員姓名");

const tabs = computed(() => [
  { key: "profile", label: "基本資料", icon: "bx bx-user" },
  { key: "reservations", label: "訂位紀錄", icon: "bx bx-calendar-check" },
  { key: "orders", label: "消費紀錄", icon: "bx bx-receipt" },
]);

const validTabKeys = computed(() => tabs.value.map((tab) => tab.key));

const syncActiveTabFromRoute = () => {
  const tab = String(route.query.tab || "profile");
  activeTab.value = validTabKeys.value.includes(tab) ? tab : "profile";
};

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

const loadMemberPageData = async () => {
  // 先取得個人資料，再取得點數。
  // 這樣可避免 STAFF / MANAGER 舊測試帳號第一次進會員中心時，
  // /me 與 /points 同時嘗試補建 members 資料造成唯一鍵衝突。
  const profileRes = await getProfile();
  const profileData = profileRes.data.data;

  const pointRes = await getPointBalance();
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

onMounted(async () => {
  syncActiveTabFromRoute();

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

watch(
  () => route.query.tab,
  () => {
    syncActiveTabFromRoute();
  },
);

const switchTab = (tabKey) => {
  if (!validTabKeys.value.includes(tabKey)) return;

  activeTab.value = tabKey;
  router.replace({
    path: "/profile",
    query: tabKey === "profile" ? {} : { tab: tabKey },
  });
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

const memberLevelText = computed(() =>
  getLevelText(getLevelByPoint(pointInfo.value.pointBalance)),
);

const isPhoneValid = computed(() => /^09\d{8}$/.test(profileForm.value.phone));

const canSubmitProfile = computed(() => {
  const name = profileForm.value.name.trim();
  const phone = profileForm.value.phone.trim();
  const hasChanged =
    name !== userInfo.value.name || phone !== userInfo.value.phone;
  return Boolean(name) && isPhoneValid.value && hasChanged;
});

const isBirthdayMonth = computed(() => {
  if (!userInfo.value?.birthday) return false;
  const month = new Date(userInfo.value.birthday).getMonth() + 1;
  const currentMonth = new Date().getMonth() + 1;
  return month === currentMonth;
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
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword)
    return false;
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

    Swal.fire({
      icon: "success",
      title: "資料已更新",
      text: "會員姓名與電話已成功修改。",
      confirmButtonColor: "#d9a372",
    });
  } catch (err) {
    Swal.fire({
      icon: "error",
      title: "資料更新失敗",
      text: err.response?.data?.message || "請稍後再試。",
      confirmButtonColor: "#d9a372",
    });
  } finally {
    isSavingProfile.value = false;
  }
};

const resetPasswordForm = () => {
  passwordForm.value = {
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  };
  passwordError.value = "";
};

const openPasswordModal = () => {
  resetPasswordForm();
  showPasswordModal.value = true;
};

const closePasswordModal = () => {
  resetPasswordForm();
  showPasswordModal.value = false;
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
  if (!validatePasswordForm() || isChangingPassword.value) return;
  isChangingPassword.value = true;

  try {
    await updatePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
    });

    closePasswordModal();
    Swal.fire({
      icon: "success",
      title: "密碼已更新",
      text: "下次登入請使用新密碼。",
      confirmButtonColor: "#d9a372",
    });
  } catch (err) {
    passwordError.value =
      err.response?.data?.message || "密碼修改失敗，請稍後再試";
  } finally {
    isChangingPassword.value = false;
  }
};

const formatBirthday = (birthday) => {
  if (!birthday) return "未提供";
  return String(birthday).replaceAll("-", "/");
};
</script>

<style scoped>
.member-page {
  min-height: 100vh;
  padding: 150px 24px 70px;
  background: #f8f3ed;
}

.member-shell {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 28px;
  width: min(1180px, 100%);
  margin: 0 auto;
}

.member-sidebar,
.member-content {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.sidebar-card,
.info-card,
.empty-card,
.record-card,
.state-box {
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.05);
}

.sidebar-card {
  padding: 24px;
}

.sidebar-title {
  margin: 0 0 12px;
  color: #8a99a8;
  font-weight: 900;
}

.level-badge {
  display: inline-flex;
  padding: 10px 16px;
  border-radius: 999px;
  background: #e3ac7f;
  color: #fff;
  font-size: 20px;
  font-weight: 900;
}

.point-main {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-top: 4px;
}

.point-number {
  color: #566a7f;
  font-size: 46px;
  font-weight: 900;
}

.point-unit,
.upgrade-note {
  color: #8a99a8;
}

.upgrade-note {
  margin: 10px 0 16px;
  line-height: 1.7;
}

.highlight-point {
  color: #e3ac7f;
  font-weight: 900;
}

.point-rule-btn,
.edit-profile-btn,
.save-profile-btn,
.cancel-profile-btn,
.tab-btn {
  border: none;
  border-radius: 12px;
  font-weight: 900;
  cursor: pointer;
}

.point-rule-btn,
.edit-profile-btn,
.save-profile-btn {
  background: #e3ac7f;
  color: #fff;
}

.point-rule-btn,
.edit-profile-btn {
  padding: 10px 14px;
}

.edit-profile-btn.outline,
.cancel-profile-btn {
  background: #fff7ef;
  color: #d18f5e;
}

.birthday-card {
  padding: 22px;
  border-radius: 18px;
  background: #fff7ef;
  border: 1px dashed #e3ac7f;
}

.birthday-card.active {
  background: #fff1e5;
}

.birthday-title {
  color: #566a7f;
  font-weight: 900;
  margin-bottom: 8px;
}

.birthday-text {
  color: #7d8b9a;
  line-height: 1.8;
}

.tab-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 12px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.05);
}

.tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 11px 15px;
  background: #fbfaf8;
  color: #7d8b9a;
}

.tab-btn.active {
  background: #e3ac7f;
  color: #fff;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
}

.section-header h2 {
  margin: 0;
  color: #566a7f;
  font-size: 26px;
  font-weight: 900;
}

.section-header p {
  margin: 8px 0 0;
  color: #8a99a8;
  line-height: 1.7;
}

.info-card {
  margin-top: 6px;
  padding: 26px;
}

.info-row {
  display: grid;
  grid-template-columns: 140px minmax(0, 1fr) auto;
  gap: 18px;
  align-items: center;
  min-height: 58px;
  padding: 14px 0;
  border-bottom: 1px solid #f0e2d5;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  color: #8a99a8;
  font-weight: 900;
}

.info-value {
  color: #566a7f;
  font-weight: 800;
}

.readonly-row .info-value {
  color: #8a99a8;
}

.password-dots {
  letter-spacing: 3px;
}

.edit-field input,
.password-form input {
  width: 100%;
  border: 1px solid #ead8c8;
  border-radius: 10px;
  padding: 11px 12px;
  color: #566a7f;
}

.edit-field input:focus,
.password-form input:focus {
  outline: none;
  border-color: #e3ac7f;
  box-shadow: 0 0 0 3px rgba(227, 172, 127, 0.18);
}

.field-error {
  margin: 8px 0 0;
  color: #c0392b;
  font-size: 14px;
  font-weight: 700;
}

.profile-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}

.cancel-profile-btn,
.save-profile-btn {
  padding: 12px 22px;
}

.save-profile-btn.full {
  width: 100%;
  margin-top: 14px;
}

button:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.state-box {
  grid-column: 1 / -1;
  padding: 36px;
  color: #8a99a8;
  text-align: center;
  font-weight: 900;
}

.state-box.inner {
  grid-column: auto;
}

.error-msg {
  color: #c0392b;
}

.empty-card {
  padding: 44px 34px;
  text-align: center;
}

.empty-card i {
  color: #e3ac7f;
  font-size: 48px;
}

.empty-card h3 {
  margin: 14px 0 8px;
  color: #566a7f;
  font-weight: 900;
}

.empty-card p,
.empty-card li {
  color: #7d8b9a;
  line-height: 1.8;
}

.pending-card {
  text-align: left;
}

.pending-card i,
.pending-card h3 {
  text-align: center;
  display: block;
}

.pending-card code {
  padding: 2px 6px;
  border-radius: 6px;
  background: #fff1e5;
  color: #c47d4e;
}

.record-card {
  overflow-x: auto;
}

.record-card table {
  width: 100%;
  border-collapse: collapse;
}

.record-card th,
.record-card td {
  padding: 16px 18px;
  border-bottom: 1px solid #f0e2d5;
  color: #566a7f;
  text-align: left;
}

.record-card th {
  color: #8a99a8;
  font-size: 13px;
}

.record-card .right {
  text-align: right;
  font-weight: 900;
}

.plus {
  color: #2e9f5e !important;
}

.minus {
  color: #c0392b !important;
}

.rule-modal-mask {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(0, 0, 0, 0.42);
}

.rule-modal,
.password-modal {
  position: relative;
  width: min(520px, 100%);
  padding: 30px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.18);
}

.rule-close-btn {
  position: absolute;
  top: 14px;
  right: 16px;
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 50%;
  background: #fff7ef;
  color: #d18f5e;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
}

.rule-modal h3,
.password-modal h3 {
  margin: 0 0 18px;
  color: #566a7f;
  font-weight: 900;
}

.rule-section h4 {
  margin: 18px 0 8px;
  color: #e3ac7f;
  font-weight: 900;
}

.rule-section p,
.rule-section li {
  color: #7d8b9a;
  line-height: 1.8;
}

.password-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.password-form label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #566a7f;
  font-weight: 900;
}

.password-rules {
  margin: 0;
  padding-left: 20px;
  color: #9aa6b2;
  line-height: 1.9;
}

.password-rules li.passed {
  color: #2e9f5e;
}

@media (max-width: 900px) {
  .member-shell {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .member-page {
    padding: 120px 14px 50px;
  }

  .section-header,
  .info-row,
  .password-row {
    grid-template-columns: 1fr;
    align-items: flex-start;
  }

  .section-header {
    flex-direction: column;
  }
}
</style>
