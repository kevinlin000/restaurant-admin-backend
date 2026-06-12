<template>
  <div class="register-container">
    <div class="register-box">
      <button class="close-btn" @click="goHome">&times;</button>

      <h2 class="title">會員註冊</h2>

      <div class="register-form-grid">
        <div class="form-column">
          <div class="form-group">
            <label>姓名</label>
            <input v-model="form.name" type="text" placeholder="請輸入姓名" />
          </div>
          <div class="form-group">
            <label>電話</label>
            <input
              v-model="form.phone"
              type="text"
              placeholder="請輸入手機號碼"
              @blur="checkPhoneExists"
            />
          </div>
          <div class="form-group">
            <label>生日 (YYYYMMDD)</label>
            <input
              v-model="form.birthday"
              type="text"
              placeholder="例如：20020101"
              maxlength="8"
            />
          </div>
        </div>

        <div class="form-column">
          <div class="form-group">
            <label>信箱</label>
            <input
              v-model="form.email"
              type="text"
              placeholder="請輸入電子信箱"
            />
          </div>
          <div class="form-group">
            <label>密碼</label>
            <div class="password-wrapper">
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="至少8碼"
              />
              <span class="eye-icon" @click="showPassword = !showPassword">
                <i :class="showPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"></i>
              </span>
            </div>
          </div>
          <div class="form-group">
            <label>確認密碼</label>
            <div class="password-wrapper">
              <input
                v-model="form.confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                placeholder="再次輸入密碼"
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
          </div>
        </div>
      </div>

      <button
        class="submit-btn"
        :disabled="!isFormValid"
        @click="handleRegister"
      >
        下一步
      </button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from "vue";
import { useRouter } from "vue-router"; // 引入 useRouter

const router = useRouter(); // 初始化路由

const form = reactive({
  name: "",
  phone: "",
  email: "",
  birthday: "",
  password: "",
  confirmPassword: "",
});

const showPassword = ref(false);
const showConfirmPassword = ref(false);

// 導向首頁函數
const goHome = () => {
  router.push("/");
};

const isFormValid = computed(() => {
  return (
    form.name &&
    /^\d{10}$/.test(form.phone) &&
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email) &&
    /^\d{8}$/.test(form.birthday) &&
    form.password.length >= 8 &&
    form.password === form.confirmPassword
  );
});

const checkPhoneExists = async () => {
  console.log("檢查電話...");
};

const handleRegister = () => {
  console.log("送出:", form);
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 130px;
  padding-bottom: 30px;
  width: 100%;
  min-height: auto;
}

.register-box {
  position: relative; /* 為了讓關閉按鈕定位 */
  width: 100%;
  max-width: 800px;
  padding: 30px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

/* 關閉按鈕樣式 */
.close-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  background: transparent;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.close-btn:hover {
  color: #333;
}

/* 標題置中 */
.title {
  text-align: center;
  margin-bottom: 25px;
  color: #55606e;
}

.register-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
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
  border: 1px solid #ccc;
  border-radius: 6px;
  box-sizing: border-box;
}

.password-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.password-wrapper input {
  padding-right: 40px;
}

.eye-icon {
  position: absolute;
  right: 12px;
  cursor: pointer;
  color: #666;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background-color: #e3ac7f;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
</style>
