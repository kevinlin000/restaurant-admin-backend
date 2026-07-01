<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import Swal from "sweetalert2";
import SupportChatWidget from "@/components/support/SupportChatWidget.vue";

const router = useRouter();
const userInfo = ref(null);
const isNavbarCompact = ref(false);
const isNavbarHidden = ref(false);
const isMobileMenuOpen = ref(false);

let lastScrollY = 0;
let scrollFrame = 0;

const loadUserInfo = () => {
  const data = localStorage.getItem("userInfo");
  userInfo.value = data ? JSON.parse(data) : null;
};

const roleName = computed(() => userInfo.value?.roleName || "");

const dropdownItems = computed(() => {
  if (!userInfo.value) {
    return [];
  }

  // 前台首頁的下拉選單依角色顯示功能。
  // MEMBER：顯示個人資料、訂位紀錄、消費紀錄。
  // MANAGER / ADMIN：可以回後台，也可以切回餐廳首頁，但不進會員個人頁。
  if (roleName.value === "ADMIN" || roleName.value === "MANAGER") {
    return [
      {
        label: "返回後台首頁",
        path: "/admin",
        icon: "bi-speedometer2",
        subtitle: "返回工作總覽",
        menuClass: "back-office-item",
      },
      {
        label: "前往餐廳首頁",
        path: "/home",
        icon: "bi-house",
      },
    ];
  }

  return [
    {
      label: "個人資料",
      path: "/profile",
      icon: "bi-person",
    },
    {
      label: "訂位紀錄",
      path: { path: "/profile", query: { tab: "reservations" } },
      icon: "bi-calendar-check",
    },
    {
      label: "消費紀錄",
      path: { path: "/profile", query: { tab: "orders" } },
      icon: "bi-receipt",
    },
  ];
});

const updateNavbarState = () => {
  const currentY = Math.max(window.scrollY || 0, 0);
  const scrollDelta = currentY - lastScrollY;
  const scrollingDown = scrollDelta > 8;
  const scrollingUp = scrollDelta < -6;

  isNavbarCompact.value = currentY > 72;

  if (currentY < 120 || isMobileMenuOpen.value || scrollingUp) {
    isNavbarHidden.value = false;
  } else if (scrollingDown && currentY > 220) {
    isNavbarHidden.value = true;
  }

  lastScrollY = currentY;
  scrollFrame = 0;
};

const handleScroll = () => {
  if (scrollFrame) return;
  scrollFrame = window.requestAnimationFrame(updateNavbarState);
};

const handlePointerIntent = (event) => {
  if (event.clientY <= 96) {
    isNavbarHidden.value = false;
  }
};

const revealNavbar = () => {
  isNavbarHidden.value = false;
};

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value;
  isNavbarHidden.value = false;
};

const closeMobileMenu = () => {
  isMobileMenuOpen.value = false;
};

onMounted(() => {
  loadUserInfo();
  lastScrollY = Math.max(window.scrollY || 0, 0);
  updateNavbarState();
  window.addEventListener("login-state-changed", loadUserInfo);
  window.addEventListener("scroll", handleScroll, { passive: true });
  window.addEventListener("pointermove", handlePointerIntent, { passive: true });
});

onBeforeUnmount(() => {
  window.removeEventListener("login-state-changed", loadUserInfo);
  window.removeEventListener("scroll", handleScroll);
  window.removeEventListener("pointermove", handlePointerIntent);
  if (scrollFrame) {
    window.cancelAnimationFrame(scrollFrame);
  }
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
    <div class="navbar-recall-zone" aria-hidden="true" @mouseenter="revealNavbar"></div>
    <nav
      :class="[
        'landing-navbar',
        {
          'is-compact': isNavbarCompact,
          'is-hidden': isNavbarHidden,
          'menu-open': isMobileMenuOpen,
        },
      ]"
    >
      <div class="container">
        <div class="navbar navbar-expand-lg">
          <!-- Logo -->
          <RouterLink to="/home" class="navbar-brand app-logo">
            <img src="../assets/images/logo.png" alt="旭日 Logo" class="logo-image" />
            <span class="logo-text new-tegomin-regular">敘日</span>
          </RouterLink>

          <!-- 縮小視窗按鈕 -->
          <button
            class="navbar-toggler border-0"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#navbarNav"
            aria-controls="navbarNav"
            :aria-expanded="isMobileMenuOpen"
            aria-label="開啟主選單"
            @click="toggleMobileMenu"
          >
            <i class="navbar-toggler-icon"></i>
          </button>

          <!-- Menu -->
          <div class="collapse navbar-collapse" id="navbarNav">
            <!-- right -->
            <ul class="navbar-nav ms-auto">
              <li class="nav-item">
                <RouterLink class="nav-link" to="/home" @click="closeMobileMenu">首頁</RouterLink>
              </li>

              <li class="nav-item">
                <RouterLink class="nav-link" to="/reservation" @click="closeMobileMenu">訂位</RouterLink>
              </li>

              <li class="nav-item">
                <RouterLink class="nav-link" to="/menu" @click="closeMobileMenu">菜單</RouterLink>
              </li>

              <li class="nav-item">
                <RouterLink class="nav-link" to="/order" @click="closeMobileMenu">點餐</RouterLink>
              </li>

              <li class="nav-item">
                <RouterLink class="nav-link" to="/store" @click="closeMobileMenu">分店資訊</RouterLink>
              </li>

              <li class="nav-item">
                <RouterLink class="nav-link" to="/news" @click="closeMobileMenu">最新消息</RouterLink>
              </li>
              <li class="nav-item">
                <RouterLink class="nav-link" to="/faq" @click="closeMobileMenu">常見問答</RouterLink>
              </li>
              <li v-if="!userInfo" class="nav-item ms-lg-5">
                <RouterLink to="/login" class="login-btn" @click="closeMobileMenu">
                  <i class="bi bi-person"></i>
                  會員登入
                </RouterLink>
              </li>

              <li v-else class="nav-item dropdown ms-lg-5">
                <a class="login-btn dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown"
                  aria-expanded="false">
                  <i class="bi bi-person"></i>
                  Hi，{{ userInfo.name }}
                </a>

                <ul class="dropdown-menu dropdown-menu-end">
                  <li v-for="item in dropdownItems" :key="item.label">
                    <RouterLink :class="['dropdown-item', item.menuClass]" :to="item.path">
                      <i v-if="item.icon" :class="['bi', item.icon]"></i>
                      <span class="dropdown-text">
                        <span>{{ item.label }}</span>
                        <small v-if="item.subtitle">{{ item.subtitle }}</small>
                      </span>
                    </RouterLink>
                  </li>

                  <li v-if="dropdownItems.length">
                    <hr class="dropdown-divider" />
                  </li>

                  <li>
                    <button class="dropdown-item text-danger" type="button" @click="logout">
                      <i class="bi bi-box-arrow-right me-2"></i>
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
    <SupportChatWidget />
  </div>
</template>

<style scoped>
/* 字型 */
@import url("https://fonts.googleapis.com/css2?family=Yuji+Boku&display=swap");
@import url("https://fonts.googleapis.com/css2?family=New+Tegomin&display=swap");

.dropdown-menu .back-office-item {
  background: #e3ac7f;
  color: white;
}

.dropdown-menu .back-office-item i {
  color: white;
}

.dropdown-menu .back-office-item:hover {
  background: #d9945f;
  color: white;
}

.dropdown-menu .back-office-item:hover i {
  color: white;
}

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
  gap: 10px;
  text-decoration: none;
}

.logo-image {
  width: 44px;
  height: 44px;
  object-fit: contain;
  transition: width 220ms ease, height 220ms ease;
}

.logo-text {
  font-size: 30px;
  font-weight: 700;
  color: #566a7f;
  letter-spacing: 0;
  transition: color 220ms ease, font-size 220ms ease;
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
  top: 22px;
  left: 0;
  width: 100%;
  z-index: 999;
  transform: translate3d(0, 0, 0);
  transition: top 240ms ease, transform 280ms ease, opacity 220ms ease;
  will-change: transform, opacity;
  pointer-events: none;
}

.navbar-recall-zone {
  position: fixed;
  top: 0;
  right: 0;
  left: 0;
  z-index: 998;
  height: 58px;
  background: transparent;
}

.landing-navbar.is-hidden:not(.menu-open) {
  opacity: 0;
  transform: translate3d(0, -120%, 0);
}

.landing-navbar.is-compact {
  top: 10px;
}

.landing-navbar .navbar {
  position: relative;
  overflow: visible;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.68);
  border-radius: 10px;
  padding: 10px 20px;
  box-shadow: 0 18px 52px rgba(32, 23, 18, 0.12);
  backdrop-filter: blur(18px);
  pointer-events: auto;
  transition: padding 240ms ease, border-radius 240ms ease, background 240ms ease, box-shadow 240ms ease;
}

.landing-navbar .navbar::after {
  position: absolute;
  right: 22px;
  bottom: 0;
  left: 22px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(220, 168, 116, 0.42), transparent);
  content: "";
  opacity: 0;
  transition: opacity 220ms ease;
}

.landing-navbar.is-compact .navbar {
  border-radius: 999px;
  padding: 6px 16px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 14px 40px rgba(32, 23, 18, 0.16);
}

.landing-navbar.is-compact .navbar::after {
  opacity: 1;
}

.landing-navbar.is-compact .logo-image {
  width: 38px;
  height: 38px;
}

.landing-navbar.is-compact .logo-text {
  font-size: 26px;
  color: #3e4a56;
}

.navbar-toggler {
  width: 44px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  color: #3e4a56;
  transition: background 180ms ease, box-shadow 180ms ease;
}

.navbar-toggler:hover,
.navbar-toggler:focus {
  background: rgba(227, 172, 127, 0.14);
  box-shadow: none;
}

/* nav */

.navbar-nav .nav-link {
  position: relative;
  display: inline-flex;
  align-items: center;
  min-height: 42px;
  font-weight: 500;
  color: #566a7f;
  margin: 0 4px;
  border-radius: 999px;
  padding: 8px 13px;
  transition: color 180ms ease, background 180ms ease, transform 180ms ease;
  font-size: 18px;
  font-family: "New Tegomin", serif;
  font-weight: 400;
  font-style: normal;
  letter-spacing: 0;
}

.navbar-nav .nav-link:hover {
  background: rgba(227, 172, 127, 0.12);
  color: #a86537;
}

/* 點到目前所選頁面 */
.navbar-nav .nav-link.router-link-exact-active:not(.login-btn) {
  background: rgba(227, 172, 127, 0.16);
  color: #a86537;
  font-weight: 600;
  font-size: 18px;
}

.navbar-nav .nav-link.router-link-exact-active:not(.login-btn)::after {
  position: absolute;
  right: 14px;
  bottom: 6px;
  left: 14px;
  height: 1px;
  background: #dca874;
  content: "";
  opacity: 0.72;
}

.landing-navbar.is-compact .navbar-nav .nav-link {
  min-height: 36px;
  padding: 6px 11px;
  font-size: 17px;
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
  min-height: 44px;
  padding: 10px 20px;
  border-radius: 999px;
  text-decoration: none;
  font-weight: 700;
  letter-spacing: 0;
  transition: transform 180ms ease, background 180ms ease, box-shadow 180ms ease;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 10px 24px rgba(160, 97, 18, 0.2);
}

.login-btn:hover {
  background: #d9945f;
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 14px 30px rgba(160, 97, 18, 0.25);
}

.landing-navbar.is-compact .login-btn {
  min-height: 38px;
  padding: 8px 16px;
}

/* dropdown */

.dropdown-menu {
  min-width: 230px;
  padding: 8px;
  border: 0;
  border-radius: 14px;
  box-shadow: 0 14px 34px rgba(86, 106, 127, 0.18);
}

.dropdown-menu .dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 14px;
  border-radius: 10px;
  color: #566a7f;
  font-weight: 600;
  transition: all 0.2s ease;
}

.dropdown-menu .dropdown-item i {
  width: 18px;
  color: #7a8ca1;
  font-size: 17px;
}

.dropdown-text {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.dropdown-text small {
  margin-top: 2px;
  font-size: 12px;
  font-weight: 500;
  opacity: 0.8;
}

.dropdown-menu .dropdown-item:hover {
  background: #f8f3ed;
  color: #d5905f;
}

.dropdown-menu .dropdown-item:hover i {
  color: #d5905f;
}

/* main */
/* 頁面主要背景 */
.main-content {
  flex: 1;
  min-height: calc(100vh - 160px);
  background-attachment: scroll;
  background: url("../assets/images/background.png");
}

:global(html) {
  scroll-padding-top: 112px;
}

:global(.main-content [id]) {
  scroll-margin-top: 112px;
}

/* footer */

.footer {
  margin-top: auto;
  background-color: #2a2115e3;
}

/* mobile */

@media (max-width: 991px) {
  .landing-navbar {
    top: 12px;
  }

  .landing-navbar.is-compact {
    top: 8px;
  }

  .landing-navbar .navbar {
    border-radius: 18px;
    padding: 8px 14px;
  }

  .landing-navbar.is-compact .navbar {
    border-radius: 18px;
  }

  .navbar-collapse {
    padding-top: 16px;
  }

  .navbar-nav {
    gap: 4px;
    margin-bottom: 16px;
  }

  .navbar-nav .nav-link {
    width: 100%;
    justify-content: space-between;
    margin: 2px 0;
    padding: 10px 12px;
    font-size: 18px;
  }

  .navbar-nav .nav-link.router-link-exact-active:not(.login-btn) {
    font-size: 18px;
  }

  .login-btn {
    width: 100%;
    justify-content: center;
    margin-top: 6px;
  }

  :global(html) {
    scroll-padding-top: 96px;
  }

  :global(.main-content [id]) {
    scroll-margin-top: 96px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .landing-navbar,
  .landing-navbar .navbar,
  .logo-image,
  .logo-text,
  .navbar-nav .nav-link,
  .login-btn {
    transition: none;
  }
}
</style>
