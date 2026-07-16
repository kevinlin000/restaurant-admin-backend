<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useRouter, useRoute, RouterView, RouterLink } from "vue-router";
import Swal from "sweetalert2";

const router = useRouter();
const route = useRoute();

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

const currentTime = ref(new Date());
let clockTimer = null;

const clockDate = computed(() => {
  return currentTime.value.toLocaleDateString("zh-TW", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    weekday: "short",
  });
});

const clockTime = computed(() => {
  return currentTime.value.toLocaleTimeString("zh-TW", {
    hour12: false,
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
});

onMounted(() => {
  clockTimer = window.setInterval(() => {
    currentTime.value = new Date();
  }, 1000);
});

onBeforeUnmount(() => {
  if (clockTimer) {
    window.clearInterval(clockTimer);
  }
});

const roleText = computed(() => {
  const roleMap = {
    STAFF: "員工",
    MANAGER: "店長",
    ADMIN: "管理員",
    CUSTOMER: "會員",
  };

  return roleMap[roleName.value] || "使用者";
});

const hasPermission = (roles = []) => roles.includes(roleName.value);

const hasVisibleChild = (children = []) => {
  return children.some((child) => hasPermission(child.roles));
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

const goTo = (path, roles = []) => {
  if (!hasPermission(roles)) {
    showNoPermission();
    return;
  }

  router.push(path);
};

const isActive = (path) => {
  if (typeof path !== "string") return false;

  const [basePath, queryString] = path.split("?");

  if (queryString) {
    const params = new URLSearchParams(queryString);
    const section = params.get("section");
    return route.path === basePath && route.query.section === section;
  }

  return route.path === path || route.path.startsWith(`${path}/`);
};

const currentPageTitle = computed(() => {
  const path = route.path;

  if (path.startsWith("/admin/reservation")) return "訂位管理";
  if (path.startsWith("/admin/order-dashboard")) return "營收分析";
  if (path.startsWith("/admin/order")) return "訂單管理";
  if (path.startsWith("/admin/store")) return "分店管理";
  if (path.startsWith("/admin/menu")) return "菜單管理";
  if (path.startsWith("/admin/member")) return "員工管理";
  if (path.startsWith("/admin/homepage")) return "首頁管理";
  if (
    path.startsWith("/admin/news") ||
    path.startsWith("/admin/faqs") ||
    path.startsWith("/admin/faq-analytics")
  ) {
    return "品牌內容";
  }
  if (path.startsWith("/admin/home")) return "後台管理首頁";

  return "後台管理首頁";
});

const sidebarGroups = computed(() => [
  {
    key: "dashboard",
    type: "single",
    label: "系統總覽",
    icon: "bx bx-home-circle",
    path: "/admin/home",
    roles: ["STAFF", "MANAGER", "ADMIN"],
  },
  {
    key: "reservation",
    label: "訂位管理",
    icon: "bx bx-calendar",
    roles: ["STAFF", "MANAGER", "ADMIN"],
    children: [
      {
        label: "訂位總覽",
        path: "/admin/reservation",
        roles: ["STAFF", "MANAGER", "ADMIN"],
      },
      {
        label: "訂位名單",
        path: "/admin/reservation-list",
        roles: ["STAFF", "MANAGER", "ADMIN"],
      },
      {
        label: "分配桌位",
        path: "/admin/reservation-table",
        roles: ["STAFF", "MANAGER", "ADMIN"],
      },
      {
        label: "訂位日期＆時段",
        path: "/admin/reservation-time-setting",
        roles: ["STAFF", "MANAGER", "ADMIN"],
      },
    ],
  },
  {
    key: "order",
    label: "訂單管理",
    icon: "bx bx-cart",
    roles: ["STAFF", "MANAGER", "ADMIN"],
    children: [
      {
        label: "訂單管理",
        path: "/admin/order-manage",
        roles: ["STAFF", "MANAGER", "ADMIN"],
      },
      {
        label: "營收分析",
        path: "/admin/order-dashboard",
        roles: ["STAFF", "MANAGER", "ADMIN"],
      },
    ],
  },
  {
    key: "store",
    label: "分店管理",
    icon: "bx bx-store",
    roles: ["MANAGER", "ADMIN"],
    children: [
      {
        label: "分店與桌位",
        path: "/admin/store",
        roles: ["MANAGER", "ADMIN"],
      },
    ],
  },
  {
    key: "menu",
    label: "菜單管理",
    icon: "bx bx-food-menu",
    roles: ["MANAGER", "ADMIN"],
    children: [
      {
        label: "新增菜單",
        path: "/admin/menu-create",
        roles: ["MANAGER", "ADMIN"],
      },
      {
        label: "修改菜單",
        path: "/admin/menu-edit/1",
        roles: ["MANAGER", "ADMIN"],
      },
      {
        label: "菜單設定",
        path: "/admin/menu-setting",
        roles: ["MANAGER", "ADMIN"],
      },
    ],
  },
  {
    key: "member",
    label: "員工管理",
    icon: "bx bx-group",
    roles: ["ADMIN"],
    children: [
      {
        label: "員工帳號設定",
        path: "/admin/member?section=form",
        roles: ["ADMIN"],
      },
      {
        label: "員工狀態清單",
        path: "/admin/member?section=list",
        roles: ["ADMIN"],
      },
    ],
  },
  {
    key: "content",
    label: "品牌內容",
    icon: "bx bx-news",
    roles: ["MANAGER", "ADMIN"],
    children: [
      {
        label: "首頁管理",
        path: "/admin/homepage",
        roles: ["ADMIN"],
      },
      {
        label: "最新消息",
        path: "/admin/news",
        roles: ["MANAGER", "ADMIN"],
      },
      {
        label: "常見問答",
        path: "/admin/faqs",
        roles: ["ADMIN"],
      },
      {
        label: "客服查詢紀錄",
        path: "/admin/faq-analytics",
        roles: ["ADMIN"],
      },
    ],
  },
]);

const visibleSidebarGroups = computed(() => {
  return sidebarGroups.value.filter((group) => {
    if (!hasPermission(group.roles)) return false;
    if (!group.children) return true;
    return hasVisibleChild(group.children);
  });
});

const openMenu = ref({
  reservation: route.path.startsWith("/admin/reservation"),
  menu: route.path.startsWith("/admin/menu"),
  order: route.path.startsWith("/admin/order"),
  store: route.path.startsWith("/admin/store"),
  member: route.path.startsWith("/admin/member"),
  content:
    route.path.startsWith("/admin/news") ||
    route.path.startsWith("/admin/faqs") ||
    route.path.startsWith("/admin/faq-analytics") ||
    route.path.startsWith("/admin/homepage"),
});

const toggleMenu = (menu) => {
  openMenu.value[menu] = !openMenu.value[menu];
};

const adminDropdownItems = computed(() => {
  const items = [
    {
      label: "後台首頁",
      path: "/admin/home",
      icon: "bx bx-tachometer",
      roles: ["STAFF", "MANAGER", "ADMIN"],
      menuClass: "admin-home-entry",
    },
  ];

  return items.filter((item) => hasPermission(item.roles));
});

const logout = async () => {
  try {
    await fetch("/api/members/logout", {
      method: "POST",
      credentials: "include",
    });
  } catch {
    // 前端仍清除本地登入狀態。
  }
  localStorage.removeItem("accessToken");
  sessionStorage.removeItem("accessToken");
  localStorage.removeItem("userInfo");
  window.dispatchEvent(new Event("login-state-changed"));
  router.push("/login");
};
</script>

<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-logo">
        <img src="../assets/images/logo.png" class="logo-image" />
        <span>後台管理系統</span>
      </div>

      <ul class="sidebar-menu">
        <li v-for="group in visibleSidebarGroups" :key="group.key" class="menu-group">
          <button v-if="group.type === 'single'" type="button" class="menu-title menu-button"
            :class="{ active: isActive(group.path) }" @click="goTo(group.path, group.roles)">
            <div>
              <i :class="group.icon"></i>
              {{ group.label }}
            </div>
          </button>

          <template v-else>
            <div class="menu-title" @click="toggleMenu(group.key)">
              <div>
                <i :class="group.icon"></i>
                {{ group.label }}
              </div>
              <i class="bx bx-chevron-down menu-arrow" :class="{ open: openMenu[group.key] }"></i>
            </div>

            <ul v-show="openMenu[group.key]" class="submenu">
              <li v-for="child in group.children.filter((item) => hasPermission(item.roles))" :key="child.path">
                <a href="#" :class="{ active: isActive(child.path) }" @click.prevent="goTo(child.path, child.roles)">
                  {{ child.label }}
                </a>
              </li>
            </ul>
          </template>
        </li>
      </ul>

      <div class="sidebar-clock" aria-label="目前時間">
        <div class="clock-label">目前時間</div>
        <div class="clock-time">{{ clockTime }}</div>
        <div class="clock-date">{{ clockDate }}</div>
      </div>
    </aside>

    <div class="main-wrapper">
      <nav class="top-navbar">
        <div class="navbar-left">
          <span class="navbar-page-title">{{ currentPageTitle }}</span>
        </div>

        <div class="navbar-right dropdown">
          <div class="profile-box dropdown-toggle" data-bs-toggle="dropdown">
            <img src="../assets/images/logo.png" class="logo-image" />
            <div>
              <div class="profile-name">{{ displayName }}</div>
              <div class="profile-role">{{ roleText }}</div>
            </div>
          </div>

          <ul class="dropdown-menu dropdown-menu-end admin-user-menu">
            <li v-for="item in adminDropdownItems" :key="item.label">
              <a href="#" :class="['dropdown-item', item.menuClass]" @click.prevent="goTo(item.path, item.roles)">
                <i :class="item.icon"></i>
                <span class="dropdown-text">
                  <span>{{ item.label }}</span>
                  <small v-if="item.subtitle">{{ item.subtitle }}</small>
                </span>
              </a>
            </li>

            <li>
              <hr class="dropdown-divider" />
            </li>

            <li>
              <RouterLink class="dropdown-item" to="/home">
                <i class="bx bx-arrow-back"></i>
                <span>返回餐廳首頁</span>
              </RouterLink>
            </li>

            <li>
              <hr class="dropdown-divider" />
            </li>

            <li>
              <button class="dropdown-item text-danger" type="button" @click="logout">
                <i class="bx bx-log-out"></i>
                <span>登出</span>
              </button>
            </li>
          </ul>
        </div>
      </nav>

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

.sidebar-clock {
  margin-top: 18px;
  padding: 14px 16px;
  border: 1px solid #f1d8c4;
  border-radius: 16px;
  background: linear-gradient(135deg, #fffaf5 0%, #fff1e5 100%);
  color: #566a7f;
  box-shadow: 0 8px 20px rgba(227, 172, 127, 0.12);
}

.clock-label {
  margin-bottom: 6px;
  color: #a8754b;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.12em;
}

.clock-time {
  color: #2f3b45;
  font-size: 24px;
  font-weight: 900;
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

.clock-date {
  margin-top: 4px;
  color: #7f8fa0;
  font-size: 13px;
  font-weight: 800;
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

.menu-arrow {
  transition: transform 0.2s ease;
}

.menu-arrow.open {
  transform: rotate(180deg);
}

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

.submenu a:hover,
.submenu a.active {
  background: #e3ac7f;
  color: white;
}

.router-link-active {
  background: #e3ac7f;
  color: white !important;
}

.main-wrapper {
  flex: 1;
  margin-left: 280px;
}

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

.navbar-left {
  display: flex;
  align-items: center;
}

.navbar-page-title {
  color: #566a7f;
  font-size: 22px;
  font-weight: 900;
  letter-spacing: 0.04em;
}

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

.admin-user-menu {
  min-width: 220px;
  padding: 8px;
  border: none;
  border-radius: 14px;
  margin-top: 10px !important;
  box-shadow: 0 14px 34px rgba(86, 106, 127, 0.18);
}

.admin-user-menu .dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  box-sizing: border-box;
  padding: 11px 14px;
  border-radius: 10px;
  color: #566a7f;
  font-weight: 600;
  line-height: 1.25;
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
  margin: 0;
  padding: 12px 14px;
  border-radius: 12px;
  background: linear-gradient(135deg, #e3ac7f 0%, #d5905f 100%);
  color: #fff !important;
  box-shadow: 0 8px 18px rgba(227, 172, 127, 0.28);
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
  margin: 8px -8px;
}

.main-content {
  padding: 0 20px 20px;
}

.logo-image {
  width: 48px;
  height: 48px;
  object-fit: contain;
}
</style>
