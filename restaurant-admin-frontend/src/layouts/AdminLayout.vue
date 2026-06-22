<script setup>
import { ref, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import Swal from "sweetalert2";

const router = useRouter();
const route = useRoute();

const openMenu = ref({
  dashboard: false,
  reservation: false,
  menu: false,
  order: false,
  store: false,
  member: false,
});

const toggleMenu = (menu) => {
  openMenu.value[menu] = !openMenu.value[menu];
};

const getUserInfo = () => {
  try {
    return JSON.parse(localStorage.getItem("userInfo") || "{}");
  } catch (error) {
    localStorage.removeItem("userInfo");
    return {};
  }
};

const userInfo = ref(getUserInfo());

const roleName = computed(() => userInfo.value?.roleName || "");
const displayName = computed(() => userInfo.value?.name || "使用者");

const roleText = computed(() => {
  const roleMap = {
    STAFF: "員工",
    MANAGER: "店長",
    ADMIN: "管理員",
    CUSTOMER: "會員",
  };

  return roleMap[roleName.value] || "使用者";
});

const hasPermission = (roles) => {
  return roles.includes(roleName.value);
};

const showNoPermission = () => {
  Swal.fire({
    icon: "warning",
    title: "權限不足",
    text: "您的角色無法使用此功能",
    confirmButtonText: "我知道了",
    confirmButtonColor: "#e3ac7f",
  });
};

const goTo = (path, roles) => {
  if (!hasPermission(roles)) {
    showNoPermission();
    return;
  }

  router.push(path);
};

const isActive = (path) => {
  return route.path === path || route.path.startsWith(`${path}/`);
};

const adminDropdownItems = computed(() => {
  const adminHomeItem = {
    label: "後台首頁",
    subtitle: "返回工作總覽",
    path: "/admin/home",
    icon: "bx bx-tachometer",
    roles: ["STAFF", "MANAGER", "ADMIN"],
    menuClass: "admin-home-entry",
  };

  const items = [
    adminHomeItem,
    {
      label: "個人資料",
      path: "/profile",
      icon: "bx bx-user-circle",
      roles: ["STAFF", "MANAGER"],
    },
    {
      label: "我的訂位紀錄",
      path: { path: "/profile", query: { tab: "reservations" } },
      icon: "bx bx-calendar-check",
      roles: ["STAFF", "MANAGER"],
    },
    {
      label: "我的消費紀錄",
      path: { path: "/profile", query: { tab: "orders" } },
      icon: "bx bx-receipt",
      roles: ["STAFF", "MANAGER"],
    },
    {
      label: "訂位管理",
      path: "/admin/reservation",
      icon: "bx bx-calendar-check",
      roles: ["STAFF", "MANAGER", "ADMIN"],
    },
    {
      label: "訂單管理",
      path: "/admin/order-manage",
      icon: "bx bx-receipt",
      roles: ["STAFF", "MANAGER", "ADMIN"],
    },
    {
      label: "菜單管理",
      path: "/admin/menu-setting",
      icon: "bx bx-food-menu",
      roles: ["MANAGER", "ADMIN"],
    },
    {
      label: "分店管理",
      path: "/admin/store",
      icon: "bx bx-store",
      roles: ["MANAGER", "ADMIN"],
    },
    {
      label: "員工管理",
      path: "/admin/member",
      icon: "bx bx-group",
      roles: ["ADMIN"],
    },
    {
      label: "門市管理",
      path: "/admin/store",
      icon: "bx bx-buildings",
      roles: ["ADMIN"],
    },
  ];

  return items.filter((item) => item.roles.includes(roleName.value));
});

const logout = () => {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("userInfo");
  window.dispatchEvent(new Event("login-state-changed"));
  router.push("/login");
};
</script>

<template>
  <div class="admin-layout">
    <!-- Sidebar -->
    <aside class="sidebar">
      <!-- Logo -->
      <div class="sidebar-logo">
        <img src="../assets/images/logo.png" class="logo-image" />
        <span>後台管理系統</span>
      </div>

      <!-- Menu -->
      <ul class="sidebar-menu">
        <!-- 系統總覽：STAFF 也可以進首頁，但只能使用部分功能 -->
        <li class="menu-group">
          <button
            type="button"
            class="menu-title menu-button"
            :class="{ active: isActive('/admin/home') }"
            @click="goTo('/admin/home', ['STAFF', 'MANAGER', 'ADMIN'])"
          >
            <div>
              <i class="bx bx-home-circle"></i>
              系統總覽
            </div>
          </button>
        </li>

        <!-- 用戶設定：店長 / 管理員 -->
        <li class="menu-group">
          <div class="menu-title" @click="toggleMenu('dashboard')">
            <div>
              <i class="bx bx-user"></i>
              用戶設定
            </div>
            <i class="bx bx-chevron-down"></i>
          </div>

          <ul v-show="openMenu.dashboard" class="submenu">
            <li>
              <a
                href="#"
                @click.prevent="goTo('/admin/store', ['MANAGER', 'ADMIN'])"
              >
                分店管理
              </a>
            </li>
            <li>
              <a
                href="#"
                @click.prevent="
                  goTo('/admin/system-setting', ['MANAGER', 'ADMIN'])
                "
              >
                設定
              </a>
            </li>
            <hr />
          </ul>
        </li>

        <!-- 訂位管理：員工 / 店長 / 管理員 -->
        <li class="menu-group">
          <div class="menu-title" @click="toggleMenu('reservation')">
            <div>
              <i class="bx bx-calendar"></i>
              訂位管理
            </div>
            <i class="bx bx-chevron-down"></i>
          </div>

          <ul v-show="openMenu.reservation" class="submenu">
            <li>
              <a
                href="#"
                @click.prevent="
                  goTo('/admin/reservation', ['STAFF', 'MANAGER', 'ADMIN'])
                "
              >
                訂位總覽
              </a>
            </li>
            <li>
              <a
                href="#"
                @click.prevent="
                  goTo('/admin/reservation-list', ['STAFF', 'MANAGER', 'ADMIN'])
                "
              >
                訂位名單
              </a>
            </li>
            <li>
              <a
                href="#"
                @click.prevent="
                  goTo('/admin/reservation-table', [
                    'STAFF',
                    'MANAGER',
                    'ADMIN',
                  ])
                "
              >
                分配桌位
              </a>
            </li>
            <li>
              <a
                href="#"
                @click.prevent="
                  goTo('/admin/reservation-time-setting', [
                    'STAFF',
                    'MANAGER',
                    'ADMIN',
                  ])
                "
              >
                訂位日期＆時段
              </a>
            </li>
            <hr />
          </ul>
        </li>

        <!-- 菜單管理：店長 / 管理員 -->
        <li class="menu-group">
          <div class="menu-title" @click="toggleMenu('menu')">
            <div>
              <i class="bx bx-food-menu"></i>
              菜單管理
            </div>
            <i class="bx bx-chevron-down"></i>
          </div>

          <ul v-show="openMenu.menu" class="submenu">
            <li>
              <a
                href="#"
                @click.prevent="
                  goTo('/admin/menu-create', ['MANAGER', 'ADMIN'])
                "
              >
                新增菜單
              </a>
            </li>
            <li>
              <a
                href="#"
                @click.prevent="
                  goTo('/admin/menu-edit/1', ['MANAGER', 'ADMIN'])
                "
              >
                修改菜單
              </a>
            </li>
            <li>
              <a
                href="#"
                @click.prevent="
                  goTo('/admin/menu-setting', ['MANAGER', 'ADMIN'])
                "
              >
                設定
              </a>
            </li>
            <hr />
          </ul>
        </li>

        <!-- 訂餐管理：員工 / 店長 / 管理員 -->
        <li class="menu-group">
          <div class="menu-title" @click="toggleMenu('order')">
            <div>
              <i class="bx bx-cart"></i>
              訂餐管理
            </div>
            <i class="bx bx-chevron-down"></i>
          </div>

          <ul v-show="openMenu.order" class="submenu">
            <li>
              <a
                href="#"
                @click.prevent="
                  goTo('/admin/order-manage', ['STAFF', 'MANAGER', 'ADMIN'])
                "
              >
                管理訂單
              </a>
            </li>
            <li>
              <a
                href="#"
                @click.prevent="
                  goTo('/admin/order-create', ['STAFF', 'MANAGER', 'ADMIN'])
                "
              >
                新增餐點
              </a>
            </li>
            <li>
              <a
                href="#"
                @click.prevent="
                  goTo('/admin/order-edit', ['STAFF', 'MANAGER', 'ADMIN'])
                "
              >
                修改餐點
              </a>
            </li>
            <li>
              <a
                href="#"
                @click.prevent="
                  goTo('/admin/order-setting', ['STAFF', 'MANAGER', 'ADMIN'])
                "
              >
                設定
              </a>
            </li>
            <hr />
          </ul>
        </li>


        <!-- 會員 / 員工管理：管理員 -->
        <li class="menu-group">
          <button
            type="button"
            class="menu-title menu-button"
            :class="{ active: isActive('/admin/member') }"
            @click="goTo('/admin/member', ['ADMIN'])"
          >
            <div>
              <i class="bx bx-group"></i>
              員工管理
            </div>
          </button>
        </li>

        <!-- 分店管理：管理員 -->
        <li class="menu-group">
          <div class="menu-title" @click="toggleMenu('store')">
            <div>
              <i class="bx bx-store"></i>
              分店管理
            </div>
            <i class="bx bx-chevron-down"></i>
          </div>

          <ul v-show="openMenu.store" class="submenu">
            <li>
              <a
                href="#"
                @click.prevent="goTo('/admin/store', ['MANAGER', 'ADMIN'])"
              >
                分店與桌位
              </a>
            </li>
          </ul>
        </li>
      </ul>
    </aside>

    <!-- Main -->
    <div class="main-wrapper">
      <!-- Navbar -->
      <nav class="top-navbar">
        <div class="navbar-right dropdown ms-auto">
          <div class="profile-box dropdown-toggle" data-bs-toggle="dropdown">
            <img src="../assets/images/logo.png" class="logo-image" />
            <div>
              <div class="profile-name">{{ displayName }}</div>
              <div class="profile-role">{{ roleText }}</div>
            </div>
          </div>

          <!-- Dropdown -->
          <ul class="dropdown-menu dropdown-menu-end admin-user-menu">
            <li>
              <RouterLink class="dropdown-item" to="/home">
                <i class="bx bx-arrow-back"></i>
                <span>返回餐廳首頁</span>
              </RouterLink>
            </li>

            <li><hr class="dropdown-divider" /></li>

            <li v-for="item in adminDropdownItems" :key="item.label">
              <a
                href="#"
                :class="['dropdown-item', item.menuClass]"
                @click.prevent="goTo(item.path, item.roles)"
              >
                <i :class="item.icon"></i>
                <span class="dropdown-text">
                  <span>{{ item.label }}</span>
                  <small v-if="item.subtitle">{{ item.subtitle }}</small>
                </span>
              </a>
            </li>

            <li><hr class="dropdown-divider" /></li>

            <li>
              <button
                class="dropdown-item text-danger"
                type="button"
                @click="logout"
              >
                <i class="bx bx-log-out"></i>
                <span>登出</span>
              </button>
            </li>
          </ul>
        </div>
      </nav>

      <!-- 頁面內容 -->
      <main class="main-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f8f3ed;
}

/* Sidebar */

.sidebar {
  width: 280px;
  background: white;
  padding: 24px;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.05);
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 40px;
  font-size: 24px;
  font-weight: 700;
  color: black;
}

.sidebar-menu {
  list-style: none;
  padding: 0;
  flex: 1;
  overflow-y: auto;
}

.menu-group {
  margin-bottom: 14px;
}

.menu-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 14px 18px;
  border-radius: 12px;
  cursor: pointer;
  transition: 0.25s;
  color: #566a7f;
  font-weight: 700;
}

.menu-button {
  border: none;
  background: transparent;
  text-align: left;
}

.menu-title:hover,
.menu-title.active {
  background: #e3ac7f;
  color: white;
}

.menu-title div {
  display: flex;
  align-items: center;
  gap: 12px;
}

.menu-title i {
  font-size: 22px;
}

/* submenu */

.submenu {
  list-style: none;
  margin-top: 8px;
  padding-left: 18px;
}

.submenu li {
  margin: 8px 0;
}

.submenu a {
  display: block;
  padding: 10px 14px;
  border-radius: 10px;
  text-decoration: none;
  color: #566a7f;
  transition: 0.25s;
}

.submenu a:hover {
  background: #e3ac7f;
  color: white;
}

.router-link-active {
  background: #e3ac7f;
  color: white !important;
}

/* Main */

.main-wrapper {
  flex: 1;
  margin-left: 280px;
}

/* Navbar */

.top-navbar {
  height: 80px;
  background: white;
  margin: 20px;
  border-radius: 10px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

/* Right */

.profile-box {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.profile-name {
  font-weight: 700;
}

.profile-role {
  font-size: 14px;
  color: gray;
}

/* Dropdown */

.admin-user-menu {
  min-width: 210px;
  padding: 10px 0;
  border: none;
  border-radius: 4px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.admin-user-menu .dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 18px;
  color: #566a7f;
  font-weight: 500;
}

.admin-user-menu .dropdown-item i {
  font-size: 18px;
  color: #8592a3;
}

.admin-user-menu .dropdown-item:hover {
  background: #e3ac7f;
  color: white !important;
}

.admin-user-menu .dropdown-item:hover i {
  color: white;
}


.admin-user-menu .dropdown-text {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.admin-user-menu .dropdown-text small {
  margin-top: 2px;
  font-size: 12px;
  font-weight: 500;
  opacity: 0.78;
}

.admin-user-menu .admin-home-entry {
  margin: 0 8px 8px;
  padding: 12px 14px;
  border-radius: 12px;
  background: linear-gradient(135deg, #e3ac7f 0%, #d5905f 100%);
  color: #fff !important;
  box-shadow: 0 8px 18px rgba(227, 172, 127, 0.32);
}

.admin-user-menu .admin-home-entry i,
.admin-user-menu .admin-home-entry small {
  color: #fff !important;
}

.admin-user-menu .admin-home-entry:hover {
  background: linear-gradient(135deg, #df9f6d 0%, #c98250 100%);
  color: #fff !important;
  transform: translateY(-1px);
}

.admin-user-menu .dropdown-divider {
  margin: 8px 0;
}

/* Content */

.main-content {
  padding: 0 20px 20px;
}

.logo-image {
  width: 48px;
  height: 48px;
  object-fit: contain;
}
</style>
