<template>
  <div class="register-page">
    <div class="member-layout">
      <!-- 左側資訊區 -->
      <aside class="member-sidebar">
        <div class="sidebar-heading">
          <span class="sidebar-kicker">JOIN US</span>
          <h2 class="member-name">會員註冊</h2>
        </div>

        <div class="sidebar-divider"></div>

        <div class="benefit-panel">
          <p class="note-title">成為會員即可享有</p>

          <ul class="benefit-list">
            <li>
              <span class="benefit-icon">
                <i class="bi bi-stars"></i>
              </span>
              <div>
                <strong>累積敘日點數</strong>
                <small>消費後累積會員點數，日後可兌換優惠。</small>
              </div>
            </li>

            <li>
              <span class="benefit-icon">
                <i class="bi bi-gift"></i>
              </span>
              <div>
                <strong>生日專屬優惠</strong>
                <small>生日月份享有會員專屬祝福與優惠。</small>
              </div>
            </li>
          </ul>
        </div>
      </aside>

      <!-- 右側主內容 -->
      <section class="content-card">
        <div class="card-header">
          <h1>會員基本資料</h1>
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
                  class="verify-btn"
                  type="button"
                  :disabled="!emailValid"
                  @click="handleSendVerifyEmail"
                >
                  發送驗證信
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
                />
                <button
                  class="check-code-btn"
                  type="button"
                  @click="handleCheckVerifyCode"
                >
                  驗證
                </button>
              </div>

              <p
                v-if="verificationSent"
                class="field-hint"
                :class="emailVerified ? 'valid' : 'neutral'"
              >
                {{ emailVerified ? "Email 已完成驗證" : "Demo 驗證碼：123456" }}
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
            {{ isLoading ? "註冊中..." : "下一步" }}
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from "vue";
import { useRouter } from "vue-router";
import { register } from "@/api/member";
import Swal from "sweetalert2";

const router = useRouter();

const showPassword = ref(false);
const showConfirmPassword = ref(false);
const errorMsg = ref("");
const isLoading = ref(false);
const verificationSent = ref(false);
const emailVerified = ref(false);

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

  verificationSent.value = true;
  emailVerified.value = false;
  form.verifyCode = "";

  await Swal.fire({
    icon: "success",
    title: "驗證信已發送",
    text: `我們已將驗證信寄送至 ${form.email}`,
    confirmButtonColor: "#d9a372",
  });
};

const handleCheckVerifyCode = async () => {
  if (form.verifyCode === "123456") {
    emailVerified.value = true;
    await Swal.fire({
      icon: "success",
      title: "Email 驗證成功",
      timer: 1200,
      showConfirmButton: false,
    });
    return;
  }

  emailVerified.value = false;
  await Swal.fire({
    icon: "error",
    title: "驗證碼錯誤",
    text: "Demo 驗證碼為 123456",
    confirmButtonColor: "#d9a372",
  });
};

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
      title: "註冊成功",
      text: `歡迎加入敘日會員，${data.name}`,
      timer: 1500,
      showConfirmButton: false,
    });

    router.push("/");
  } catch (err) {
    errorMsg.value = err.response?.data?.message || "註冊失敗，請稍後再試";
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.register-page {
  padding: 120px 7vw 70px;
  min-height: 100vh;
  background: #f8f3ed;
}

.member-layout {
  display: grid;
  grid-template-columns: 230px minmax(0, 1fr);
  gap: 34px;
  max-width: 1180px;
  margin: 0 auto;
}

.member-sidebar {
  color: #566a7f;
}

.sidebar-heading {
  padding: 6px 0 2px;
}

.sidebar-kicker {
  display: inline-block;
  margin-bottom: 10px;
  color: #d99b68;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.member-name {
  font-size: 30px;
  font-weight: 700;
  color: #2f3f4f;
  margin: 0 0 12px;
  line-height: 1.25;
}

.sidebar-divider {
  height: 1px;
  background: #e7ddd3;
  margin: 28px 0;
}

.benefit-panel {
  padding: 18px;
  border: 1px solid #eadfd5;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.65);
  box-shadow: 0 8px 24px rgba(66, 49, 34, 0.04);
}

.note-title {
  color: #566a7f;
  font-weight: 700;
  margin: 0 0 14px;
}

.benefit-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.benefit-list li {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-top: 1px solid #efe4da;
}

.benefit-list li:first-child {
  border-top: none;
  padding-top: 0;
}

.benefit-list li:last-child {
  padding-bottom: 0;
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

.benefit-list strong {
  display: block;
  color: #2f3f4f;
  font-size: 15px;
  margin-bottom: 4px;
}

.benefit-list small {
  display: block;
  color: #7d8b99;
  font-size: 13px;
  line-height: 1.6;
}

.content-card {
  background: #fff;
  border: 1px solid #eadfd5;
  border-radius: 14px;
  padding: 36px 48px 38px;
  box-shadow: 0 10px 30px rgba(66, 49, 34, 0.06);
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

.verify-btn:disabled {
  background: #ead3bf;
  cursor: not-allowed;
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
  border-radius: 8px;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  background: #e3ac7f;
}

.submit-btn:disabled {
  background: #ead3bf;
  cursor: not-allowed;
}

@media (max-width: 900px) {
  .register-page {
    padding: 105px 22px 50px;
  }

  .member-layout {
    grid-template-columns: 1fr;
  }

  .content-card {
    padding: 28px 24px;
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
}
</style>
