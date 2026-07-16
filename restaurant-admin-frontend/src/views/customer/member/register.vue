<template>
  <div class="register-page">
    <div class="member-layout">
      <!-- 左側資訊區 -->
      <aside class="member-sidebar">
        <div class="sidebar-heading">
          <span class="sidebar-kicker">JOIN US</span>
          <h2 class="member-name">敘日會員註冊</h2>
        </div>

        <div class="sidebar-divider"></div>

        <div class="benefit-panel">
          <p class="note-title">成為會員即可享有</p>

          <ul class="benefit-list">
            <li class="benefit-item">
              <div class="benefit-copy">
                <span class="benefit-icon">
                  <i class="bi bi-stars"></i>
                </span>
                <div class="benefit-text">
                  <strong>會員點數</strong>
                  <small>累積會員點數，達門檻即可升級會員等級。</small>
                </div>
              </div>
              <div class="benefit-thumb thumb-pudding" aria-hidden="true">
                <img :src="puddingImg" alt="" />
              </div>
            </li>

            <li class="benefit-item">
              <div class="benefit-copy">
                <span class="benefit-icon">
                  <i class="bi bi-wallet2"></i>
                </span>
                <div class="benefit-text">
                  <strong>折抵點數</strong>
                  <small>點餐消費可累積點數，結帳時可折抵金額。</small>
                </div>
              </div>
              <div class="benefit-thumb thumb-sushi" aria-hidden="true">
                <img :src="sushiImg" alt="" />
              </div>
            </li>

            <li class="benefit-item">
              <div class="benefit-copy">
                <span class="benefit-icon">
                  <i class="bi bi-gift"></i>
                </span>
                <div class="benefit-text">
                  <strong>生日專屬優惠</strong>
                  <small>生日月份消費，可享會員專屬生日禮遇。</small>
                </div>
              </div>
              <div class="benefit-thumb thumb-dessert" aria-hidden="true">
                <img :src="dessertImg" alt="" />
              </div>
            </li>
          </ul>
        </div>
      </aside>

      <!-- 右側主內容 -->
      <section v-if="registrationComplete" class="content-card success-card">
        <div class="success-icon">
          <i class="bi bi-check2-circle"></i>
        </div>
        <h1>註冊成功！</h1>
        <p class="success-message">
          歡迎加入敘日會員，{{ registeredName }}。<br />
          現在開始訂位、點餐，並累積會員點數。
        </p>

        <div class="success-actions">
          <button class="submit-btn" type="button" @click="goToProfile">
            個人資料
          </button>
          <button class="secondary-btn" type="button" @click="goToMenu">
            瀏覽菜單
          </button>
        </div>
      </section>

      <section v-else class="content-card">
        <div class="card-header">
          <h1>敘日會員基本資料</h1>
        </div>

        <div class="form-table">
          <!-- 姓名 -->
          <div class="form-row">
            <label>會員姓名</label>
            <div class="field-area">
              <input
                v-model.trim="form.name"
                type="text"
                placeholder="請輸入姓名"
              />
            </div>
          </div>

          <!-- 電話 -->
          <div class="form-row">
            <label>手機號碼</label>
            <div class="field-area">
              <input
                v-model.trim="form.phone"
                type="text"
                placeholder="請輸入手機號碼，例如 0912345678"
                maxlength="10"
              />
              <p
                v-if="form.phone"
                class="field-hint"
                :class="phoneValid ? 'valid' : 'invalid'"
              >
                {{
                  phoneValid ? "手機格式正確" : "手機格式需為 09 開頭，共 10 碼"
                }}
              </p>
            </div>
          </div>

          <!-- Email -->
          <div class="form-row">
            <label>Email</label>
            <div class="field-area">
              <div class="email-inline">
                <input
                  v-model.trim="form.email"
                  type="email"
                  placeholder="請輸入電子信箱"
                />
                <button
                  type="button"
                  class="send-code-btn"
                  :disabled="isSendingCode || countdown > 0 || !emailValid"
                  @click="handleSendVerifyEmail"
                >
                  <span v-if="isSendingCode">發送中...</span>
                  <span v-else-if="countdown > 0"
                    >重新發送 {{ countdown }}s</span
                  >
                  <span v-else>發送驗證信</span>
                </button>
              </div>

              <p
                v-if="form.email"
                class="field-hint"
                :class="emailValid ? 'valid' : 'invalid'"
              >
                {{ emailStatus }}
              </p>

              <div v-if="verificationSent" class="verify-code-box">
                <input
                  v-model.trim="form.verifyCode"
                  type="text"
                  placeholder="請輸入 6 位數驗證碼"
                  maxlength="6"
                  :disabled="emailVerified"
                />
                <button
                  type="button"
                  class="verify-btn"
                  :class="{ verified: emailVerified }"
                  :disabled="emailVerified"
                  @click="handleCheckVerifyCode"
                >
                  <span v-if="emailVerified">✓ 驗證成功</span>
                  <span v-else>驗證</span>
                </button>
              </div>

              <p
                v-if="verificationSent"
                class="field-hint"
                :class="emailVerified ? 'valid' : 'neutral'"
              >
                {{
                  emailVerified
                    ? "Email 已完成驗證"
                    : "請至信箱收取 6 位數驗證碼"
                }}
              </p>
            </div>
          </div>

          <!-- 生日 -->
          <div class="form-row">
            <label>生日</label>
            <div class="field-area">
              <input v-model="form.birthday" type="date" />
            </div>
          </div>

          <!-- 密碼 -->
          <div class="form-row">
            <label>密碼</label>
            <div class="field-area">
              <div class="password-wrapper">
                <input
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="請輸入密碼"
                />
                <span class="eye-icon" @click="showPassword = !showPassword">
                  <i
                    :class="showPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"
                  ></i>
                </span>
              </div>

              <ul class="rule-list">
                <li :class="passwordLengthValid ? 'valid' : ''">
                  <i class="bi bi-check-circle-fill"></i>
                  密碼長度需為 8 到 20 個字元
                </li>
              </ul>
            </div>
          </div>

          <!-- 確認密碼 -->
          <div class="form-row">
            <label>確認密碼</label>
            <div class="field-area">
              <div class="password-wrapper">
                <input
                  v-model="form.confirmPassword"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  placeholder="請再次輸入密碼"
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

              <p
                v-if="form.confirmPassword"
                class="field-hint"
                :class="passwordConfirmed ? 'valid' : 'invalid'"
              >
                {{
                  passwordConfirmed
                    ? "兩次輸入的密碼一致"
                    : "兩次輸入的密碼不一致"
                }}
              </p>
            </div>
          </div>
        </div>

        <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

        <div class="action-row">
          <button
            class="submit-btn"
            type="button"
            :disabled="!isFormValid || isLoading"
            @click="handleRegister"
          >
            {{ isLoading ? "註冊中..." : "成為敘日會員" }}
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { register, sendEmailCode, verifyEmailCode } from "@/api/member";
import Swal from "sweetalert2";
import puddingImg from "@/assets/images/caramel-pudding.jpg";
import sushiImg from "@/assets/images/aburi-salmon-sushi.jpg";
import dessertImg from "@/assets/images/matcha-dessert.jpg";

const router = useRouter();
const showPassword = ref(false);
const showConfirmPassword = ref(false);
const errorMsg = ref("");
const isLoading = ref(false);
const registrationComplete = ref(false);
const registeredName = ref("新會員");
const verificationSent = ref(false);
const emailVerified = ref(false);
const isSendingCode = ref(false);
const countdown = ref(0);
let countdownTimer = null;
const form = reactive({
  name: "",
  phone: "",
  email: "",
  birthday: "",
  password: "",
  confirmPassword: "",
  verifyCode: "",
});

const emailValid = computed(() =>
  /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email),
);
const phoneValid = computed(() => /^09\d{8}$/.test(form.phone));
const passwordLengthValid = computed(
  () => form.password.length >= 8 && form.password.length <= 20,
);
const passwordConfirmed = computed(
  () => form.password && form.password === form.confirmPassword,
);

const emailStatus = computed(() => {
  if (!form.email) return "";
  return emailValid.value ? "此電子信箱可使用" : "電子信箱格式不正確";
});

const isFormValid = computed(() => {
  return (
    form.name.trim() &&
    phoneValid.value &&
    emailValid.value &&
    emailVerified.value &&
    form.birthday &&
    passwordLengthValid.value &&
    passwordConfirmed.value
  );
});

const handleSendVerifyEmail = async () => {
  errorMsg.value = "";

  if (isSendingCode.value || countdown.value > 0) {
    return;
  }

  if (!form.email) {
    await Swal.fire({
      icon: "warning",
      title: "請先輸入電子信箱",
      confirmButtonColor: "#d9a372",
    });
    return;
  }

  if (!emailValid.value) {
    await Swal.fire({
      icon: "error",
      title: "電子信箱格式不正確",
      text: "請確認後再發送驗證信",
      confirmButtonColor: "#d9a372",
    });
    return;
  }

  isSendingCode.value = true;

  try {
    await sendEmailCode(form.email);

    verificationSent.value = true;
    emailVerified.value = false;
    form.verifyCode = "";

    startCountdown();

    await Swal.fire({
      icon: "success",
      title: "驗證信已發送",
      text: `我們已將驗證碼寄送至 ${form.email}`,
      confirmButtonColor: "#d9a372",
    });
  } catch (err) {
    verificationSent.value = false;
    emailVerified.value = false;

    await Swal.fire({
      icon: "error",
      title: "驗證信發送失敗",
      text: err.response?.data?.message || "請稍後再試",
      confirmButtonColor: "#d9a372",
    });
  } finally {
    isSendingCode.value = false;
  }
};

const handleCheckVerifyCode = async () => {
  errorMsg.value = "";

  if (!form.verifyCode) {
    await Swal.fire({
      icon: "warning",
      title: "請輸入驗證碼",
      confirmButtonColor: "#d9a372",
    });
    return;
  }

  if (!/^\d{6}$/.test(form.verifyCode)) {
    await Swal.fire({
      icon: "error",
      title: "驗證碼格式不正確",
      text: "請輸入 6 位數字驗證碼",
      confirmButtonColor: "#d9a372",
    });
    return;
  }

  try {
    await verifyEmailCode(form.email, form.verifyCode);

    emailVerified.value = true;
    await Swal.fire({
      icon: "success",
      title: "Email 驗證成功",
      timer: 1200,
      showConfirmButton: false,
    });
  } catch (err) {
    emailVerified.value = false;
    await Swal.fire({
      icon: "error",
      title: "驗證碼錯誤",
      text: err.response?.data?.message || "請確認驗證碼是否正確",
      confirmButtonColor: "#d9a372",
    });
  }
};

const startCountdown = () => {
  countdown.value = 60;

  if (countdownTimer) {
    clearInterval(countdownTimer);
  }

  countdownTimer = setInterval(() => {
    countdown.value -= 1;

    if (countdown.value <= 0) {
      clearInterval(countdownTimer);
      countdownTimer = null;
    }
  }, 1000);
};

const goToProfile = () => {
  router.push("/profile");
};

const goToMenu = () => {
  router.push("/menu");
};

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer);
  }
});
const handleRegister = async () => {
  errorMsg.value = "";

  if (!emailVerified.value) {
    errorMsg.value = "請先完成 Email 驗證";
    return;
  }

  if (!isFormValid.value) {
    errorMsg.value = "請確認資料填寫正確";
    return;
  }

  isLoading.value = true;

  try {
    const res = await register({
      name: form.name,
      phone: form.phone,
      email: form.email,
      birthday: form.birthday,
      password: form.password,
    });

    const data = res.data.data;

    sessionStorage.setItem("accessToken", data.accessToken);
    localStorage.removeItem("accessToken");
    localStorage.setItem(
      "userInfo",
      JSON.stringify({
        userId: data.userId,
        name: data.name,
        roleName: data.roleName,
      }),
    );

    window.dispatchEvent(new Event("login-state-changed"));

    registeredName.value = data.name || form.name || "新會員";
    registrationComplete.value = true;
  } catch (err) {
    errorMsg.value = err.response?.data?.message || "註冊失敗，請稍後再試";
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.register-page {
  position: relative;
  isolation: isolate;
  overflow: hidden;
  min-height: 100vh;
  padding: 120px 7vw 70px;
  background:
    linear-gradient(
      115deg,
      rgba(9, 7, 5, 0.82) 0%,
      rgba(39, 26, 16, 0.5) 52%,
      rgba(9, 7, 5, 0.78) 100%
    ),
    url("@/assets/images/caramel-pudding.jpg") center / cover no-repeat;
}

.register-page::before {
  content: "";
  position: absolute;
  inset: 0;
  z-index: -1;
  background:
    radial-gradient(
      circle at 20% 18%,
      rgba(227, 172, 127, 0.26),
      transparent 32%
    ),
    linear-gradient(rgba(255, 255, 255, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1px, transparent 1px);
  background-size:
    auto,
    110px 110px,
    110px 110px;
}

.register-page::after {
  content: "";
  position: absolute;
  inset: 0;
  z-index: -1;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.08), rgba(0, 0, 0, 0.38));
}

.member-layout {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(340px, 4fr) minmax(0, 6fr);
  gap: 28px;
  max-width: 1280px;
  margin: 0 auto;
}

.member-sidebar {
  align-self: start;
  padding: 30px 28px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 24px;
  color: rgba(255, 255, 255, 0.86);
  background: rgba(0, 0, 0, 0.34);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.24);
  backdrop-filter: blur(8px);
}

.sidebar-heading {
  padding: 6px 0 2px;
}

.sidebar-kicker {
  display: inline-block;
  margin-bottom: 10px;
  color: #e3ac7f;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.member-name {
  font-size: 34px;
  font-weight: 900;
  color: #fff;
  margin: 0 0 12px;
  line-height: 1.25;
}

.sidebar-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.22);
  margin: 28px 0;
}

.benefit-panel {
  padding: 22px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.note-title {
  color: #fff;
  font-weight: 700;
  margin: 0 0 14px;
}

.benefit-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.benefit-list li {
  border-top: 1px solid rgba(255, 255, 255, 0.16);
}

.benefit-list li:first-child {
  border-top: none;
}

.benefit-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 14px 0;
}

.benefit-item:first-child {
  padding-top: 0;
}

.benefit-item:last-child {
  padding-bottom: 0;
}

.benefit-copy {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex: 1 1 auto;
}

.benefit-icon {
  width: 36px;
  height: 36px;
  flex: 0 0 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #fff4eb;
  color: #e3ac7f;
  font-size: 18px;
}

.benefit-text {
  min-width: 0;
}

.benefit-list strong {
  display: block;
  color: #fff;
  font-size: 15px;
  margin-bottom: 4px;
}

.benefit-list small {
  display: block;
  color: rgba(255, 255, 255, 0.74);
  font-size: 12px;
  line-height: 1.55;
}

.benefit-thumb {
  width: 52px;
  height: 52px;
  flex: 0 0 52px;
  border-radius: 16px;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.28);
  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.22);
  background: rgba(255, 255, 255, 0.12);
}

.benefit-thumb img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.thumb-pudding {
  animation: benefitFloatA 4.2s ease-in-out infinite;
}

.thumb-sushi {
  animation: benefitFloatB 4.8s ease-in-out infinite;
}

.thumb-dessert {
  animation: benefitFloatC 5.1s ease-in-out infinite;
}

@keyframes benefitFloatA {
  0%,
  100% {
    transform: translateY(0) rotate(-4deg) scale(1);
  }
  50% {
    transform: translateY(-5px) rotate(0deg) scale(1.03);
  }
}

@keyframes benefitFloatB {
  0%,
  100% {
    transform: translateY(0) rotate(4deg) scale(1);
  }
  50% {
    transform: translateY(5px) rotate(1deg) scale(1.03);
  }
}

@keyframes benefitFloatC {
  0%,
  100% {
    transform: translateY(0) rotate(-3deg) scale(1);
  }
  50% {
    transform: translateY(-4px) rotate(2deg) scale(1.03);
  }
}

.content-card {
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.46);
  border-radius: 24px;
  padding: 32px 36px 34px;
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.22);
  backdrop-filter: blur(10px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.card-header h1 {
  color: #2f3f4f;
  font-size: 28px;
  margin: 0;
}

.success-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 540px;
  text-align: center;
}

.success-icon {
  display: grid;
  place-items: center;
  width: 86px;
  height: 86px;
  margin-bottom: 18px;
  border-radius: 50%;
  color: #fff;
  background: linear-gradient(135deg, #e3ac7f, #d8925c);
  box-shadow: 0 18px 38px rgba(227, 172, 127, 0.32);
  font-size: 44px;
}

.success-card h1 {
  margin: 10px 0 14px;
  color: #566a7f;
  font-size: 34px;
  font-weight: 900;
}

.success-message {
  margin: 0;
  color: #7d8b9a;
  line-height: 1.9;
  font-weight: 700;
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 14px;
  flex-wrap: wrap;
  margin-top: 28px;
}

.success-actions .submit-btn,
.success-actions .secondary-btn {
  width: 150px;
  min-width: 150px;
}

.secondary-btn {
  min-width: 150px;
  border: 1px solid #e5c4aa;
  border-radius: 14px;
  padding: 14px 24px;
  background: #fff;
  color: #c47d4e;
  font-weight: 900;
  cursor: pointer;
}

.form-table {
  border-top: 1px solid #e6ddd4;
}

.form-row {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr);
  gap: 26px;
  align-items: start;
  padding: 22px 0;
  border-bottom: 1px solid #e6ddd4;
}

.form-row label {
  color: #566a7f;
  font-weight: 700;
  padding-top: 10px;
}

.field-area input {
  width: 100%;
  height: 44px;
  padding: 10px 14px;
  border: 1px solid #d9c8b8;
  border-radius: 8px;
  color: #34495e;
  background: #fff;
  outline: none;
  transition: 0.2s;
}

.field-area input:focus {
  border-color: #e3ac7f;
  box-shadow: 0 0 0 3px rgba(227, 172, 127, 0.16);
}

.email-inline,
.verify-code-box {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
}

.verify-code-box {
  margin-top: 12px;
}

.verify-btn,
.send-code-btn,
.check-code-btn {
  height: 44px;
  border: none;
  border-radius: 10px;
  padding: 0 18px;
  background: #e3ac7f;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
}

.verify-btn:disabled,
.send-code-btn:disabled {
  background: #c8c8c8;
  color: #ffffff;
  cursor: not-allowed;
  opacity: 0.8;
}

.check-code-btn {
  background: #566a7f;
}

.password-wrapper {
  position: relative;
}

.password-wrapper input {
  padding-right: 44px;
}

.eye-icon {
  position: absolute;
  top: 50%;
  right: 14px;
  transform: translateY(-50%);
  color: #8a97a6;
  cursor: pointer;
}

.field-hint {
  margin: 8px 0 0;
  font-size: 14px;
  color: #9aa7b3;
}

.field-hint.valid {
  color: #3c9b63;
}

.field-hint.invalid {
  color: #d85c5c;
}

.field-hint.neutral {
  color: #8a7b6d;
}

.rule-list {
  list-style: none;
  padding: 0;
  margin: 12px 0 0;
}

.rule-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 7px;
  font-size: 14px;
  color: #9aa7b3;
}

.rule-list li.valid {
  color: #3c9b63;
}

.error-msg {
  margin: 20px 0 0;
  color: #d85c5c;
  font-weight: 600;
}

.action-row {
  margin-top: 28px;
}

.submit-btn {
  height: 48px;
  border: none;
  border-radius: 12px;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  background: #e3ac7f;
}

.submit-btn:disabled {
  background: #ead3bf;
  cursor: not-allowed;
}

.verify-btn.verified,
.verify-btn.verified:disabled {
  background: #28a745;
  color: #ffffff;
  cursor: default;
  opacity: 1;
}

.verify-btn.verified:hover {
  background: #28a745;
}

@media (max-width: 900px) {
  .register-page {
    padding: 105px 22px 50px;
    background-position: 56% center;
  }

  .member-layout {
    grid-template-columns: 1fr;
  }

  .content-card {
    padding: 28px 24px;
  }

  .benefit-item {
    gap: 10px;
  }

  .benefit-thumb {
    width: 46px;
    height: 46px;
    flex-basis: 46px;
    border-radius: 14px;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .form-row label {
    padding-top: 0;
  }

  .email-inline,
  .verify-code-box,
  .action-row {
    grid-template-columns: 1fr;
  }

  .success-benefits {
    grid-template-columns: 1fr;
  }
}
</style>
