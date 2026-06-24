<script setup>
import { computed } from "vue";
import { RouterLink } from "vue-router";

const getUserInfo = () => {
  try {
    return JSON.parse(localStorage.getItem("userInfo") || "{}");
  } catch (error) {
    localStorage.removeItem("userInfo");
    return {};
  }
};

const userInfo = computed(() => getUserInfo());
const roleName = computed(() => userInfo.value?.roleName || "");
const displayName = computed(() => userInfo.value?.name || "使用者");
const isAdmin = computed(() => roleName.value === "ADMIN");

const roleText = computed(() => {
  const roleMap = {
    STAFF: "員工",
    MANAGER: "店長",
    ADMIN: "管理員",
    CUSTOMER: "會員",
  };

  return roleMap[roleName.value] || "使用者";
});

const availableFeatures = computed(() => {
  const role = roleName.value;

  const baseFeatures = [
    {
      title: "訂位管理",
      icon: "bx bx-calendar-check",
      description: "查看顧客訂位資料與現場安排。",
      path: "/admin/reservation",
      roles: ["STAFF", "MANAGER", "ADMIN"],
    },
    {
      title: "訂單管理",
      icon: "bx bx-cart",
      description: "查看顧客訂單與處理狀態。",
      path: "/admin/order-manage",
      roles: ["STAFF", "MANAGER", "ADMIN"],
    },
    {
      title: "分店管理",
      icon: "bx bx-store",
      description: "維護分店資訊與桌位設定。",
      path: "/admin/store",
      roles: ["MANAGER", "ADMIN"],
    },
    {
      title: "菜單管理",
      icon: "bx bx-food-menu",
      description: "管理分類、價格與上下架狀態。",
      path: "/admin/menu-setting",
      roles: ["ADMIN"],
    },
    {
      title: "員工管理",
      icon: "bx bx-group",
      description: "新增員工與店長，管理離職狀態。",
      path: "/admin/member",
      roles: ["ADMIN"],
    },
  ];

  return baseFeatures.filter((feature) => feature.roles.includes(role));
});

const roleSummary = computed(() => {
  if (roleName.value === "STAFF") {
    return "可處理訂位與訂單相關作業。";
  }

  if (roleName.value === "MANAGER") {
    return "可處理門市訂位、訂單與分店資料。";
  }

  if (roleName.value === "ADMIN") {
    return "管理員具備最高後台權限，可使用所有管理功能。";
  }

  return "目前帳號尚未取得後台角色權限。";
});
</script>

<template>
  <section class="admin-home">
    <section class="hero-card">
      <p class="permission-line">
        {{ displayName }}，您現在的權限身分是
        <span class="role-pill">{{ roleText }}</span>
      </p>
    </section>

    <section class="feature-panel">
      <div class="section-header">
        <h2>
          可使用功能
          <span>{{ roleSummary }}</span>
        </h2>
      </div>

      <div
        v-if="availableFeatures.length"
        class="feature-grid"
        :class="{ 'is-admin-grid': isAdmin }"
      >
        <RouterLink
          v-for="feature in availableFeatures"
          :key="feature.title"
          class="feature-card"
          :to="feature.path"
        >
          <div class="feature-icon">
            <i :class="feature.icon"></i>
          </div>
          <div class="feature-content">
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.description }}</p>
          </div>
          <i class="bx bx-chevron-right feature-arrow"></i>
        </RouterLink>
      </div>

      <div v-else class="empty-state">
        <i class="bx bx-lock-alt"></i>
        <p>目前帳號沒有可使用的後台功能。</p>
      </div>
    </section>
  </section>
</template>

<style scoped>
.admin-home {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.hero-card,
.feature-panel {
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.05);
}

.hero-card {
  padding: 40px 36px;
  background: linear-gradient(135deg, #ffffff 0%, #fff7ef 100%);
}

.permission-line {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin: 0;
  color: #566a7f;
  font-size: 32px;
  font-weight: 900;
  line-height: 1.35;
}

.role-pill {
  display: inline-flex;
  align-items: center;
  padding: 8px 18px;
  border-radius: 999px;
  background: #e3ac7f;
  color: #fff;
  font-size: 18px;
  line-height: 1;
  font-weight: 900;
  white-space: nowrap;
}

.feature-panel {
  padding: 30px 32px 34px;
}

.section-header {
  margin-bottom: 22px;
}

.section-header h2 {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 14px;
  margin: 0;
  color: #566a7f;
  font-size: 28px;
  font-weight: 900;
}

.section-header h2 span {
  color: #8a99a8;
  font-size: 15px;
  font-weight: 800;
  line-height: 1.6;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 14px;
}

.feature-grid.is-admin-grid {
  grid-template-columns: repeat(5, minmax(0, 1fr));
}

.feature-card {
  position: relative;
  display: flex;
  gap: 12px;
  align-items: flex-start;
  min-height: 116px;
  padding: 18px 34px 18px 18px;
  border: 1px solid #f1d8c4;
  border-radius: 16px;
  background: #fffdfb;
  color: inherit;
  text-decoration: none;
  cursor: pointer;
  transition: 0.22s ease;
}

.feature-card:hover {
  transform: translateY(-3px);
  border-color: #e3ac7f;
  box-shadow: 0 12px 24px rgba(227, 172, 127, 0.18);
  background: #fff8f1;
}

.feature-card:hover .feature-icon {
  background: #e3ac7f;
  color: #fff;
}

.feature-card:hover .feature-arrow {
  opacity: 1;
  transform: translateX(3px);
}

.feature-content {
  min-width: 0;
}

.feature-arrow {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #e3ac7f;
  font-size: 24px;
  opacity: 0.5;
  transition: 0.22s ease;
}

.feature-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 13px;
  background: #fff1e5;
  color: #e3ac7f;
  transition: 0.22s ease;
}

.feature-icon i {
  font-size: 24px;
}

.feature-card h3 {
  margin: 0 0 6px;
  color: #566a7f;
  font-size: 19px;
  font-weight: 900;
}

.feature-card p {
  margin: 0;
  color: #7f8fa0;
  font-size: 14px;
  line-height: 1.6;
  font-weight: 700;
}

.empty-state {
  min-height: 180px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #8a99a8;
  font-weight: 800;
}

.empty-state i {
  color: #e3ac7f;
  font-size: 42px;
  margin-bottom: 8px;
}

@media (max-width: 1280px) {
  .feature-grid.is-admin-grid {
    grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  }
}

@media (max-width: 768px) {
  .hero-card,
  .feature-panel {
    padding: 24px;
  }

  .permission-line {
    font-size: 26px;
  }

  .role-pill {
    font-size: 16px;
  }

  .feature-card {
    flex-direction: column;
  }
}
</style>
