<template>
  <div class="login-container">
    <div class="login-box">
      <button class="back-btn" @click="$router.push('/')">返回首頁</button>

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

      <button class="submit-btn" @click="handleLogin">登入</button>

      <div class="register-link">
        還沒有會員嗎？<RouterLink to="/register">立即註冊</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const showPassword = ref(false);
const form = reactive({
  email: "",
  password: "",
});

const handleLogin = () => {
  console.log("登入資料:", form);
};
</script>

<style scoped>
.back-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  background: none;
  border: none;
  color: #888;
  cursor: pointer;
  font-size: 14px;
}
.title {
  text-align: center;
  color: #55606e;
  margin-bottom: 25px;
}

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

.password-wrapper {
  position: relative;
  width: 100%;
}
.eye-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  color: #888;
}
.password-wrapper input {
  padding-right: 40px;
}

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 150px;
  min-height: 80vh;
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
}
.register-link {
  margin-top: 30px;
  text-align: center;
  font-size: 15px;
}
.register-link a {
  color: #e3ac7f;
}
</style>
