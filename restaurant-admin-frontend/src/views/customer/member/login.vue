<template>
  <div class="login-container">
    <div class="login-box">
      <!-- 登入畫面 -->
      <template v-if="currentView === 'login'">
        <h2 class="title">會員登入</h2>

        <div class="form-group">
          <label>電子信箱 (Email)</label>
          <input v-model="form.email" type="email" placeholder="請輸入 Email" />
        </div>

        <div class="form-group">
          <label>密碼</label>
          <div class="password-wrapper">
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="請輸入密碼"
            />
            <span class="eye-icon" @click="showPassword = !showPassword">
              <i :class="showPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"></i>
            </span>
          </div>
        </div>

        <div class="helper-row">
          <button type="button" class="forgot-link" @click="goForgotPassword">
            忘記密碼？
          </button>
        </div>

        <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

        <button class="submit-btn" @click="handleLogin" :disabled="isLoading">
          {{ isLoading ? "登入中..." : "登入" }}
        </button>

        <div class="register-link">
          還沒有會員嗎？<RouterLink to="/register">立即註冊</RouterLink>
        </div>
      </template>

      <!-- 忘記密碼畫面 -->
      <template v-else>
        <h2 class="title">重設密碼</h2>
        <p class="forgot-desc">
          請輸入註冊時使用的電子信箱，我們會寄送密碼重設驗證碼
        </p>

        <div class="form-group">
          <label>電子信箱 (Email)</label>
          <div class="email-row">
            <input
              v-model="forgotForm.email"
              type="email"
              placeholder="請輸入電子信箱"
            />
            <button
              type="button"
              class="verify-btn"
              @click="sendResetCode"
              :disabled="
                isSendingCode || resetCodeCountdown > 0 || !forgotEmailValid
              "
            >
              <span v-if="isSendingCode">發送中...</span>
              <span v-else-if="resetCodeCountdown > 0"
                >重新發送 {{ resetCodeCountdown }}s</span
              >
              <span v-else>發送驗證碼</span>
            </button>
          </div>
          <p
            v-if="forgotEmailStatus"
            class="status-text"
            :class="forgotEmailStatusClass"
          >
            {{ forgotEmailStatus }}
          </p>
        </div>

        <div class="form-group">
          <label>驗證碼</label>
          <div class="code-row">
            <input
              v-model="forgotForm.verifyCode"
              type="text"
              maxlength="6"
              placeholder="請輸入 6 位數驗證碼"
            />
            <button
              type="button"
              class="verify-btn code-verify-btn"
              @click="verifyResetCode"
              :disabled="!canVerifyResetCode"
            >
              <span v-if="isVerifyingResetCode">驗證中...</span>
              <span v-else-if="resetCodeVerified">已驗證</span>
              <span v-else>驗證</span>
            </button>
          </div>
          <p
            v-if="forgotForm.verifyCode || resetCodeVerified"
            class="status-text"
            :class="resetCodeVerified ? 'status-success' : 'status-muted'"
          >
            {{
              resetCodeVerified
                ? "驗證碼已通過，請設定新密碼"
                : "請先完成驗證碼驗證"
            }}
          </p>
        </div>

        <div class="form-group">
          <label>新密碼</label>
          <div class="password-wrapper">
            <input
              v-model="forgotForm.newPassword"
              :type="showNewPassword ? 'text' : 'password'"
              placeholder="至少 8 碼，最多 20 碼"
              :disabled="!resetCodeVerified"
            />
            <span class="eye-icon" @click="showNewPassword = !showNewPassword">
              <i :class="showNewPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"></i>
            </span>
          </div>
        </div>

        <div class="rule-list">
          <p :class="passwordLengthValid ? 'rule-pass' : 'rule-muted'">
            <i class="bi bi-check-circle-fill"></i>
            密碼長度需為 8 到 20 個字元
          </p>
          <p :class="passwordMixedValid ? 'rule-pass' : 'rule-muted'"></p>
        </div>

        <div class="form-group">
          <label>確認新密碼</label>
          <div class="password-wrapper">
            <input
              v-model="forgotForm.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              placeholder="再次輸入新密碼"
              :disabled="!resetCodeVerified"
            />
            <span
              class="eye-icon"
              @click="showConfirmPassword = !showConfirmPassword"
            >
              <i
                :class="showConfirmPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"
              ></i>
            </span>
          </div>
          <p
            v-if="forgotForm.confirmPassword"
            class="status-text"
            :class="passwordConfirmed ? 'status-success' : 'status-error'"
          >
            {{
              passwordConfirmed ? "兩次輸入的密碼一致" : "兩次輸入的密碼不一致"
            }}
          </p>
        </div>

        <p v-if="forgotErrorMsg" class="error-msg">{{ forgotErrorMsg }}</p>

        <button
          class="submit-btn"
          type="button"
          @click="handleResetPassword"
          :disabled="!canSubmitResetPassword"
        >
          {{ isResettingPassword ? "重設中..." : "重設密碼" }}
        </button>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onUnmounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  forgotPassword,
  login,
  resetPassword,
  verifyPasswordResetCode,
} from "@/api/member";
import Swal from "sweetalert2";

const router = useRouter();

const currentView = ref("login");
const showPassword = ref(false);
const showNewPassword = ref(false);
const showConfirmPassword = ref(false);

const errorMsg = ref("");
const forgotErrorMsg = ref("");
const isLoading = ref(false);
const isSendingCode = ref(false);
const isVerifyingResetCode = ref(false);
const isResettingPassword = ref(false);
const codeSent = ref(false);
const resetCodeVerified = ref(false);
const resetCodeCountdown = ref(0);
let resetCodeTimer = null;

const form = reactive({
  email: "",
  password: "",
});

const forgotForm = reactive({
  email: "",
  verifyCode: "",
  newPassword: "",
  confirmPassword: "",
});

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const forgotEmailValid = computed(() => emailRegex.test(forgotForm.email));

const forgotEmailStatus = computed(() => {
  if (!forgotForm.email) return "";
  return forgotEmailValid.value ? "電子信箱格式正確" : "電子信箱格式不正確";
});

const forgotEmailStatusClass = computed(() => {
  if (!forgotForm.email) return "";
  return forgotEmailValid.value ? "status-success" : "status-error";
});

const passwordLengthValid = computed(() => {
  const length = forgotForm.newPassword.length;
  return length >= 8 && length <= 20;
});

const passwordMixedValid = computed(() => {
  return (
    /[A-Za-z]/.test(forgotForm.newPassword) && /\d/.test(forgotForm.newPassword)
  );
});

const passwordConfirmed = computed(() => {
  return (
    forgotForm.confirmPassword.length > 0 &&
    forgotForm.newPassword === forgotForm.confirmPassword
  );
});

const resetCodeFormatValid = computed(() => /^\d{6}$/.test(forgotForm.verifyCode));

const canVerifyResetCode = computed(() => {
  return (
    codeSent.value &&
    forgotEmailValid.value &&
    resetCodeFormatValid.value &&
    !resetCodeVerified.value &&
    !isVerifyingResetCode.value
  );
});

const canSubmitResetPassword = computed(() => {
  return (
    resetCodeVerified.value &&
    passwordLengthValid.value &&
    passwordConfirmed.value &&
    !isResettingPassword.value
  );
});

const startResetCodeCountdown = () => {
  resetCodeCountdown.value = 60;

  if (resetCodeTimer) {
    clearInterval(resetCodeTimer);
  }

  resetCodeTimer = setInterval(() => {
    resetCodeCountdown.value -= 1;

    if (resetCodeCountdown.value <= 0) {
      clearInterval(resetCodeTimer);
      resetCodeTimer = null;
    }
  }, 1000);
};

const stopResetCodeCountdown = () => {
  resetCodeCountdown.value = 0;

  if (resetCodeTimer) {
    clearInterval(resetCodeTimer);
    resetCodeTimer = null;
  }
};

watch(
  () => forgotForm.email,
  () => {
    codeSent.value = false;
    resetCodeVerified.value = false;
    forgotForm.verifyCode = "";
    forgotForm.newPassword = "";
    forgotForm.confirmPassword = "";
    stopResetCodeCountdown();
  },
);

watch(
  () => forgotForm.verifyCode,
  () => {
    resetCodeVerified.value = false;
    forgotForm.newPassword = "";
    forgotForm.confirmPassword = "";
  },
);

onUnmounted(() => {
  stopResetCodeCountdown();
});

const goForgotPassword = () => {
  currentView.value = "forgot";
  errorMsg.value = "";
  forgotErrorMsg.value = "";
};

const backToLogin = () => {
  currentView.value = "login";
  forgotErrorMsg.value = "";
};

const sendResetCode = async () => {
  forgotErrorMsg.value = "";

  if (isSendingCode.value || resetCodeCountdown.value > 0) {
    return;
  }

  if (!forgotForm.email) {
    forgotErrorMsg.value = "請先輸入電子信箱";
    return;
  }

  if (!forgotEmailValid.value) {
    forgotErrorMsg.value = "電子信箱格式不正確";
    return;
  }

  isSendingCode.value = true;

  try {
    const res = await forgotPassword(forgotForm.email);

    await Swal.fire({
      icon: "success",
      title: "驗證碼已寄出",
      text: res.data.message || "請至信箱查看驗證碼",
      confirmButtonColor: "#d9a372",
    });

    codeSent.value = true;
    resetCodeVerified.value = false;
    forgotForm.verifyCode = "";
    forgotForm.newPassword = "";
    forgotForm.confirmPassword = "";
    startResetCodeCountdown();
  } catch (err) {
    forgotErrorMsg.value = err.response?.data?.message || "寄送驗證碼失敗";
  } finally {
    isSendingCode.value = false;
  }
};

const verifyResetCode = async () => {
  forgotErrorMsg.value = "";

  if (!codeSent.value) {
    forgotErrorMsg.value = "請先發送驗證碼";
    return;
  }

  if (!forgotEmailValid.value) {
    forgotErrorMsg.value = "電子信箱格式不正確";
    return;
  }

  if (!resetCodeFormatValid.value) {
    forgotErrorMsg.value = "請輸入 6 位數字驗證碼";
    return;
  }

  isVerifyingResetCode.value = true;

  try {
    const res = await verifyPasswordResetCode(
      forgotForm.email,
      forgotForm.verifyCode,
    );

    resetCodeVerified.value = true;

    await Swal.fire({
      icon: "success",
      title: "驗證成功",
      text: res.data.message || "請繼續設定新密碼",
      confirmButtonColor: "#d9a372",
    });
  } catch (err) {
    resetCodeVerified.value = false;

    if (err.response?.status === 401 || err.response?.status === 403) {
      forgotErrorMsg.value =
        "驗證碼驗證 API 尚未開放，請重新啟動後端後再試";
    } else {
      forgotErrorMsg.value = err.response?.data?.message || "驗證碼錯誤";
    }
  } finally {
    isVerifyingResetCode.value = false;
  }
};

const handleResetPassword = async () => {
  forgotErrorMsg.value = "";

  if (!forgotForm.email || !forgotForm.verifyCode) {
    forgotErrorMsg.value = "請先輸入電子信箱與驗證碼";
    return;
  }

  if (!codeSent.value) {
    forgotErrorMsg.value = "請先發送驗證碼";
    return;
  }

  if (!resetCodeVerified.value) {
    forgotErrorMsg.value = "請先完成驗證碼驗證";
    return;
  }

  if (!forgotForm.newPassword || !forgotForm.confirmPassword) {
    forgotErrorMsg.value = "請完整填寫新密碼與確認密碼";
    return;
  }

  if (!passwordLengthValid.value) {
    forgotErrorMsg.value = "新密碼長度需為 8 到 20 個字元";
    return;
  }

  if (!passwordConfirmed.value) {
    forgotErrorMsg.value = "兩次輸入的新密碼不一致";
    return;
  }

  isResettingPassword.value = true;

  try {
    const res = await resetPassword({
      email: forgotForm.email,
      code: forgotForm.verifyCode,
      newPassword: forgotForm.newPassword,
    });

    await Swal.fire({
      icon: "success",
      title: "密碼重設成功",
      text: res.data.message || "請使用新密碼重新登入",
      confirmButtonColor: "#d9a372",
    });

    forgotForm.verifyCode = "";
    forgotForm.newPassword = "";
    forgotForm.confirmPassword = "";
    codeSent.value = false;
    resetCodeVerified.value = false;
    stopResetCodeCountdown();

    currentView.value = "login";
    form.email = forgotForm.email;
    form.password = "";
  } catch (err) {
    forgotErrorMsg.value = err.response?.data?.message || "密碼重設失敗";
  } finally {
    isResettingPassword.value = false;
  }
};

const getDefaultPathByRole = (roleName) => {
  if (roleName === "CUSTOMER") {
    return "/profile";
  }

  if (["STAFF", "MANAGER", "ADMIN"].includes(roleName)) {
    return "/admin/home";
  }

  return "/home";
};

const handleLogin = async () => {
  errorMsg.value = "";
  isLoading.value = true;

  try {
    const res = await login(form);
    const data = res.data.data;

    localStorage.setItem("accessToken", data.accessToken);
    localStorage.setItem(
      "userInfo",
      JSON.stringify({
        userId: data.userId,
        name: data.name,
        roleName: data.roleName,
      }),
    );

    window.dispatchEvent(new Event("login-state-changed"));

    await Swal.fire({
      icon: "success",
      title: "登入成功",
      text: `歡迎回來，${data.name}`,
      confirmButtonColor: "#d9a372",
    });

    router.push(getDefaultPathByRole(data.roleName));
  } catch (err) {
    errorMsg.value = err.response?.data?.message || "登入失敗，請稍後再試";
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.title {
  text-align: center;
  color: #55606e;
  margin-bottom: 25px;
  font-weight: 700;
}

.login-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 180px 16px 60px;
  min-height: 100vh;
}

.login-box {
  width: 100%;
  max-width: 500px;
  padding: 50px;
  border: none;
  border-radius: 16px;
  position: relative;
  background: white;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
  color: #333;
  font-weight: 500;
}

input {
  width: 100%;
  padding: 10px;
  margin-top: 5px;
  border: 1px solid #ccc;
  border-radius: 6px;
  box-sizing: border-box;
  color: #333;
}

input:focus {
  outline: none;
  border-color: #e3ac7f;
  box-shadow: 0 0 0 3px rgba(227, 172, 127, 0.16);
}

.password-wrapper {
  position: relative;
  width: 100%;
}

.password-wrapper input {
  padding-right: 40px;
}

.eye-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  color: #888;
}

.helper-row {
  display: flex;
  justify-content: flex-end;
  margin-top: -5px;
  margin-bottom: 12px;
}

.forgot-link,
.back-link {
  border: none;
  background: transparent;
  color: #e3ac7f;
  cursor: pointer;
  font-size: 14px;
  padding: 0;
}

.forgot-link:hover,
.back-link:hover {
  text-decoration: underline;
}

.back-link {
  margin-bottom: 20px;
}

.forgot-desc {
  margin-bottom: 24px;
  color: #6d7b8a;
  line-height: 1.7;
  text-align: center;
}

.email-row,
.code-row {
  display: flex;
  gap: 10px;
  align-items: stretch;
}

.email-row input,
.code-row input {
  flex: 1;
}

.verify-btn {
  width: 130px;
  margin-top: 5px;
  border: none;
  border-radius: 6px;
  background: #e3ac7f;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}

.verify-btn:disabled {
  background: #c8c8c8;
  color: #ffffff;
  opacity: 0.8;
  cursor: not-allowed;
}

.code-verify-btn {
  width: 92px;
}

input:disabled {
  background: #f6f6f6;
  cursor: not-allowed;
}

.status-text {
  margin: 7px 0 0;
  font-size: 13px;
}

.status-success {
  color: #4f8f5b;
}

.status-error,
.error-msg {
  color: #c0392b;
}

.status-muted {
  color: #9aa6b2;
}

.error-msg {
  margin: 8px 0 0;
  font-size: 14px;
}

.rule-list {
  margin: -4px 0 14px;
}

.rule-list p {
  margin: 5px 0;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.rule-pass {
  color: #4f8f5b;
}

.rule-muted {
  color: #9aa6b2;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background: #e3ac7f;
  color: white;
  border: none;
  cursor: pointer;
  border-radius: 6px;
  margin-top: 10px;
  font-weight: 700;
}

.submit-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.register-link {
  margin-top: 30px;
  text-align: center;
  font-size: 15px;
  color: #566a7f;
}

.register-link a {
  color: #e3ac7f;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}

@media (max-width: 576px) {
  .login-container {
    padding: 140px 16px 40px;
  }

  .login-box {
    padding: 36px 24px;
  }

  .email-row,
  .code-row {
    flex-direction: column;
  }

  .verify-btn,
  .code-verify-btn {
    width: 100%;
    height: 44px;
  }
}
</style>
