<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import Swal from "sweetalert2";

const router = useRouter();
const userInfo = ref(null);

const loadUserInfo = () => {
  const data = localStorage.getItem("userInfo");
  userInfo.value = data ? JSON.parse(data) : null;
};

onMounted(() => {
  loadUserInfo();
  window.addEventListener("login-state-changed", loadUserInfo);
});

onBeforeUnmount(() => {
  window.removeEventListener("login-state-changed", loadUserInfo);
});

const logout = async () => {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("userInfo");

  userInfo.value = null;
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

<template>
  <div class="layout-wrapper">
    <!-- Navbar -->
    <nav class="landing-navbar">
      <div class="container">
        <div class="navbar navbar-expand-lg">
          <!-- Logo -->
          <RouterLink to="/home" class="navbar-brand app-logo">
            <img
              src="../assets/images/logo.png"
              alt="旭日 Logo"
              class="logo-image"
            />
            <span class="logo-text new-tegomin-regular">敘日</span>
          </RouterLink>

          <!-- 縮小視窗按鈕 -->
          <button
            class="navbar-toggler border-0"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#navbarNav">
            <i class="navbar-toggler-icon"></i>
          </button>

          <!-- Menu -->
          <div class="collapse navbar-collapse" id="navbarNav">
            <!-- right -->
            <ul class="navbar-nav ms-auto">
              <li class="nav-item">
                <RouterLink class="nav-link" to="/home">首頁</RouterLink>
              </li>

              <li class="nav-item">
                <RouterLink class="nav-link" to="/reservation">訂位</RouterLink>
              </li>

              <li class="nav-item">
                <RouterLink class="nav-link" to="/menu">菜單</RouterLink>
              </li>

              <li class="nav-item">
                <RouterLink class="nav-link" to="/order">點餐</RouterLink>
              </li>

              <li class="nav-item">
                <RouterLink class="nav-link" to="/store">分店資訊</RouterLink>
              </li>
              <li v-if="!userInfo" class="nav-item ms-lg-5">
                <RouterLink to="/login" class="login-btn">
                  <i class="bi bi-person"></i>
                  會員登入
                </RouterLink>
              </li>

              <li v-else class="nav-item dropdown ms-lg-5">
                <a
                  class="login-btn dropdown-toggle"
                  href="#"
                  role="button"
                  data-bs-toggle="dropdown"
                  aria-expanded="false">
                  <i class="bi bi-person"></i>
                  Hi，{{ userInfo.name }}
                </a>

                <ul class="dropdown-menu dropdown-menu-end">
                  <li>
                    <RouterLink class="dropdown-item" to="/profile">
                      個人資料
                    </RouterLink>
                  </li>

                  <li>
                    <button
                      class="dropdown-item text-danger"
                      type="button"
                      @click="logout">
                      登出
                    </button>
                  </li>
                </ul>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main -->
    <main class="main-content">
      <div>
        <RouterView />
      </div>
    </main>

    <!-- Footer -->
    <footer class="footer text-white py-4">
      <div class="container text-center">
        <h5 class="mb-2">敘日</h5>
        <small>© 2026 敘日 Restaurant System.</small>
      </div>
    </footer>

    <div class="layout-overlay layout-menu-toggle"></div>
    <div class="drag-target"></div>
  </div>
</template>

<style scoped>

/* 字型 */
@import url("https://fonts.googleapis.com/css2?family=Yuji+Boku&display=swap");
@import url("https://fonts.googleapis.com/css2?family=New+Tegomin&display=swap");

.yuji-boku-regular {
  font-family: "Yuji Boku", serif;
  font-weight: 400;
  font-style: normal;
}
.new-tegomin-regular {
  font-family: "New Tegomin", serif;
  font-weight: 400 bold;
  font-style: normal;
}

/* logo */

.app-logo {
  display: flex;
  align-items: center;
  text-decoration: none;
}

.logo-text {
  font-size: 30px;
  font-weight: 700;
  color: #566a7f;
}

/* layout */

.layout-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* navbar */

.landing-navbar {
  position: fixed;
  top: 20px;
  left: 0;
  width: 100%;
  z-index: 999;
}

.landing-navbar .navbar {
  background: white;
  border-radius: 10px;
  padding: 10px 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* nav */

.navbar-nav .nav-link {
  font-weight: 500;
  color: #566a7f;
  margin: 0 10px;
  transition: all 0.25s ease;
  font-size: 20px;
  /* .new-tegomin-regular */
  font-family: "New Tegomin", serif;
  font-weight: 400;
  font-style: normal;
}

.navbar-nav .nav-link:hover {
  color: #e3ac7f;
}

/* 點到目前所選頁面 */
.navbar-nav .nav-link.router-link-exact-active:not(.login-btn) {
  color: #e3ac7f;
  font-weight: bold;
  font-size: 22px;
}

/* right buttons */

.theme-btn {
  width: 42px;
  height: 42px;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: #566a7f;
  transition: 0.25s;
}

.theme-btn:hover {
  background: #f5f5ff;
  color: #e3ac7f;
}

.login-btn {
  background: #e3ac7f;
  color: white;
  padding: 12px 22px;
  border-radius: 12px;
  text-decoration: none;
  font-weight: 500;
  transition: 0.25s;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(105, 108, 255, 0.3);
}

.login-btn:hover {
  background: #e3ac7f;
  transform: translateY(-1px);
}

/* main */
/* 頁面主要背景 */
.main-content {
  flex: 1;
  min-height: calc(100vh - 160px);
  background-attachment: scroll;
  /* background-color:#f8f3ed; */
  background: url('../assets/images/background.png');
}

/* footer */

.footer {
  margin-top: auto;
  background-color: #4c4332b8;
}

/* mobile */

@media (max-width: 991px) {
  .landing-navbar .navbar {
    border-radius: 16px;
  }
  .navbar-collapse {
    padding-top: 20px;
  }
  .navbar-nav {
    margin-bottom: 20px;
  }
  .navbar-nav .nav-link {
    margin: 10px 0;
  }
}
</style>
