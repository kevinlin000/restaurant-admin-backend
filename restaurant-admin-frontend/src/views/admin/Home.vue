<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import Swal from "sweetalert2";
import { getMemberAdminSummary } from "@/api/member";

const router = useRouter();

const isLoading = ref(false);
const loadError = ref("");
const summary = ref({
  totalMembers: 0,
  todayNewMembers: 0,
  bronzeCount: 0,
  silverCount: 0,
  goldCount: 0,
  diamondCount: 0,
  activeStaffCount: 0,
});

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

const todayText = computed(() => {
  return new Intl.DateTimeFormat("zh-TW", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    weekday: "long",
  }).format(new Date());
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

const hasPermission = (roles) => roles.includes(roleName.value);

const showNoPermission = () => {
  Swal.fire({
    icon: "warning",
    title: "權限不足",
    text: "您的角色無法使用此功能",
    confirmButtonText: "我知道了",
    confirmButtonColor: "#e3ac7f",
  });
};

const goTo = (item) => {
  if (item.disabled) {
    Swal.fire({
      icon: "info",
      title: "等待對接",
      text: item.pendingText || "此功能待其他模組完成後串接。",
      confirmButtonText: "我知道了",
      confirmButtonColor: "#e3ac7f",
    });
    return;
  }

  if (!hasPermission(item.roles)) {
    showNoPermission();
    return;
  }

  router.push(item.path);
};

const statCards = computed(() => [
  {
    label: "會員總數",
    value: summary.value.totalMembers,
    unit: "人",
    icon: "bx bx-user-circle",
    note: "僅統計 CUSTOMER 會員",
  },
  {
    label: "今日新增會員",
    value: summary.value.todayNewMembers,
    unit: "人",
    icon: "bx bx-user-plus",
    note: "依會員建立時間統計",
  },
  {
    label: "在職員工",
    value: summary.value.activeStaffCount,
    unit: "人",
    icon: "bx bx-id-card",
    note: "員工管理資料來源",
  },
  {
    label: "最高等級會員",
    value: summary.value.diamondCount,
    unit: "人",
    icon: "bx bx-diamond",
    note: "鑽石會員人數",
  },
]);

const memberLevelRows = computed(() => [
  { level: "銅卡", value: summary.value.bronzeCount, point: "0 - 29 點" },
  { level: "銀卡", value: summary.value.silverCount, point: "30 - 59 點" },
  { level: "金卡", value: summary.value.goldCount, point: "60 - 99 點" },
  { level: "鑽石", value: summary.value.diamondCount, point: "100 點以上" },
]);

const featureCards = computed(() => [
  {
    title: "員工管理",
    description: "新增員工帳號、查看員工清單與設定離職狀態。",
    path: "/admin/member",
    icon: "bx bx-group",
    roles: ["ADMIN"],
    tag: "Member 模組",
  },
  {
    title: "會員中心架構",
    description: "前台會員登入後可查看基本資料、點數紀錄與待對接區塊。",
    icon: "bx bx-user",
    roles: ["STAFF", "MANAGER", "ADMIN"],
    tag: "Member 模組",
    disabled: true,
    pendingText: "會員中心位於前台 /profile，需以 CUSTOMER 會員帳號登入查看。",
  },
  {
    title: "門市管理",
    description: "維護門市與桌位資料。",
    path: "/admin/store",
    icon: "bx bx-buildings",
    roles: ["MANAGER", "ADMIN"],
    tag: "既有功能",
  },
  {
    title: "菜單管理",
    description: "維護餐點分類、價格、描述與上下架狀態。",
    path: "/admin/menu-setting",
    icon: "bx bx-food-menu",
    roles: ["MANAGER", "ADMIN"],
    tag: "既有功能",
  },
  {
    title: "消費紀錄",
    description: "等待訂單模組提供會員訂單紀錄 API 後串接。",
    icon: "bx bx-receipt",
    roles: ["STAFF", "MANAGER", "ADMIN"],
    tag: "待訂單組對接",
    disabled: true,
    pendingText: "會員消費紀錄需要訂單組提供 GET /api/orders/me。",
  },
  {
    title: "訂位紀錄",
    description: "等待訂位模組提供會員訂位紀錄 API 後串接。",
    icon: "bx bx-calendar-check",
    roles: ["STAFF", "MANAGER", "ADMIN"],
    tag: "待訂位組對接",
    disabled: true,
    pendingText: "會員訂位紀錄需要訂位組提供 GET /api/reservations/me。",
  },
]);

const availableFeatures = computed(() =>
  featureCards.value.filter((item) => hasPermission(item.roles)),
);

const loadSummary = async () => {
  isLoading.value = true;
  loadError.value = "";

  try {
    const res = await getMemberAdminSummary();
    summary.value = {
      ...summary.value,
      ...(res.data?.data || {}),
    };
  } catch (err) {
    loadError.value =
      err.response?.data?.message || "會員統計載入失敗，請確認後端已啟動。";
  } finally {
    isLoading.value = false;
  }
};

onMounted(loadSummary);
</script>

<template>
  <section class="admin-home">
    <div class="hero-card">
      <div>
        <p class="eyebrow">後台管理首頁</p>
        <h1>歡迎回來，{{ displayName }}</h1>
        <p class="hero-desc">
          今天是 {{ todayText }}，您目前的角色為
          <span class="role-pill">{{ roleText }}</span>
          。此首頁先整合 member 模組可獨立完成的統計與入口。
        </p>
      </div>

      <button class="refresh-btn" type="button" :disabled="isLoading" @click="loadSummary">
        <i class="bx bx-refresh"></i>
        {{ isLoading ? "更新中" : "重新整理" }}
      </button>
    </div>

    <p v-if="loadError" class="alert-box">
      <i class="bx bx-error-circle"></i>
      {{ loadError }}
    </p>

    <div class="stat-grid">
      <article v-for="card in statCards" :key="card.label" class="stat-card">
        <div class="stat-icon">
          <i :class="card.icon"></i>
        </div>
        <div>
          <p class="stat-label">{{ card.label }}</p>
          <div class="stat-value">
            {{ card.value ?? 0 }}
            <span>{{ card.unit }}</span>
          </div>
          <p class="stat-note">{{ card.note }}</p>
        </div>
      </article>
    </div>

    <div class="content-grid">
      <section class="panel">
        <div class="section-header">
          <div>
            <h2>會員等級分布</h2>
            <p>依 member 模組點數等級統計，不依賴訂單或訂位資料。</p>
          </div>
        </div>

        <div class="level-list">
          <div v-for="row in memberLevelRows" :key="row.level" class="level-row">
            <div>
              <strong>{{ row.level }}</strong>
              <span>{{ row.point }}</span>
            </div>
            <b>{{ row.value ?? 0 }} 人</b>
          </div>
        </div>
      </section>

      <section class="panel">
        <div class="section-header">
          <div>
            <h2>跨模組對接狀態</h2>
            <p>消費、訂位資料來源屬於其他組，member 端先保留位置。</p>
          </div>
        </div>

        <div class="todo-list">
          <div class="todo-item done">
            <i class="bx bx-check-circle"></i>
            <div>
              <h3>會員資料 / 點數紀錄</h3>
              <p>已由 member 模組提供資料與畫面。</p>
            </div>
          </div>
          <div class="todo-item pending">
            <i class="bx bx-time-five"></i>
            <div>
              <h3>消費紀錄</h3>
              <p>待訂單組提供 GET /api/orders/me。</p>
            </div>
          </div>
          <div class="todo-item pending">
            <i class="bx bx-time-five"></i>
            <div>
              <h3>訂位紀錄</h3>
              <p>待訂位組提供 GET /api/reservations/me。</p>
            </div>
          </div>
        </div>
      </section>
    </div>

    <section class="panel">
      <div class="section-header">
        <div>
          <h2>功能入口</h2>
          <p>僅啟用已註冊路由或 member 範圍內可完成的功能。</p>
        </div>
      </div>

      <div class="feature-grid">
        <button
          v-for="item in availableFeatures"
          :key="item.title"
          type="button"
          class="feature-card"
          :class="{ disabled: item.disabled }"
          @click="goTo(item)"
        >
          <div class="feature-top">
            <div class="feature-icon">
              <i :class="item.icon"></i>
            </div>
            <span>{{ item.tag }}</span>
          </div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
        </button>
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
.panel,
.stat-card {
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.05);
}

.hero-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  padding: 32px;
  background: linear-gradient(135deg, #ffffff 0%, #fff7ef 100%);
}

.eyebrow {
  margin: 0 0 8px;
  color: #e3ac7f;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.hero-card h1 {
  margin: 0;
  color: #566a7f;
  font-size: 32px;
  font-weight: 800;
}

.hero-desc {
  margin: 14px 0 0;
  color: #6f7f8f;
  line-height: 1.8;
}

.role-pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  margin: 0 4px;
  border-radius: 999px;
  background: #e3ac7f;
  color: #fff;
  font-weight: 800;
}

.refresh-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: none;
  border-radius: 12px;
  padding: 12px 18px;
  background: #e3ac7f;
  color: #fff;
  font-weight: 800;
  cursor: pointer;
  flex-shrink: 0;
}

.refresh-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.alert-box {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  padding: 14px 18px;
  border-radius: 12px;
  background: #fff2ef;
  color: #c0392b;
  font-weight: 700;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 18px;
}

.stat-card {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  padding: 22px;
}

.stat-icon,
.feature-icon {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: #fff1e5;
  color: #e3ac7f;
  flex-shrink: 0;
}

.stat-icon i,
.feature-icon i {
  font-size: 28px;
}

.stat-label,
.stat-note {
  margin: 0;
  color: #8a99a8;
}

.stat-label {
  font-weight: 800;
}

.stat-value {
  margin: 7px 0;
  color: #566a7f;
  font-size: 30px;
  font-weight: 900;
}

.stat-value span {
  font-size: 15px;
  color: #8a99a8;
  font-weight: 700;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 20px;
}

.panel {
  padding: 26px;
}

.section-header h2 {
  margin: 0;
  color: #566a7f;
  font-size: 22px;
  font-weight: 900;
}

.section-header p {
  margin: 8px 0 0;
  color: #8a99a8;
  line-height: 1.7;
}

.level-list,
.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

.level-row,
.todo-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding: 16px;
  border-radius: 14px;
  background: #fbfaf8;
  border: 1px solid #f0e2d5;
}

.level-row div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.level-row strong,
.todo-item h3 {
  color: #566a7f;
  font-weight: 900;
}

.level-row span,
.todo-item p {
  color: #8a99a8;
}

.level-row b {
  color: #e3ac7f;
  white-space: nowrap;
}

.todo-item {
  justify-content: flex-start;
  align-items: flex-start;
}

.todo-item i {
  font-size: 26px;
  margin-top: 2px;
}

.todo-item.done i {
  color: #2e9f5e;
}

.todo-item.pending i {
  color: #e3ac7f;
}

.todo-item h3,
.todo-item p {
  margin: 0;
}

.todo-item p {
  margin-top: 5px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 18px;
  margin-top: 20px;
}

.feature-card {
  min-height: 190px;
  padding: 22px;
  border: 1px solid #f0e2d5;
  border-radius: 16px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: 0.25s;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 32px rgba(0, 0, 0, 0.08);
}

.feature-card.disabled {
  background: #fbfaf8;
}

.feature-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 18px;
}

.feature-top span {
  padding: 5px 10px;
  border-radius: 999px;
  background: #fff7ef;
  color: #d18f5e;
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
}

.feature-card h3 {
  margin: 0;
  color: #566a7f;
  font-size: 19px;
  font-weight: 900;
}

.feature-card p {
  margin: 10px 0 0;
  color: #7d8b9a;
  line-height: 1.7;
}

@media (max-width: 992px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero-card {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
