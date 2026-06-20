<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import Swal from 'sweetalert2'

const router = useRouter()

const getUserInfo = () => {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || '{}')
  } catch (error) {
    localStorage.removeItem('userInfo')
    return {}
  }
}

const userInfo = computed(() => getUserInfo())
const roleName = computed(() => userInfo.value?.roleName || '')
const displayName = computed(() => userInfo.value?.name || '使用者')

const roleText = computed(() => {
  const roleMap = {
    STAFF: '員工',
    MANAGER: '店長',
    ADMIN: '管理員',
    CUSTOMER: '會員'
  }

  return roleMap[roleName.value] || '使用者'
})

const hasPermission = (roles) => {
  return roles.includes(roleName.value)
}

const showNoPermission = () => {
  Swal.fire({
    icon: 'warning',
    title: '權限不足',
    text: '您的角色無法使用此功能',
    confirmButtonText: '我知道了',
    confirmButtonColor: '#e3ac7f'
  })
}

const goTo = (item) => {
  if (!hasPermission(item.roles)) {
    showNoPermission()
    return
  }

  router.push(item.path)
}

const featureCards = [
  {
    title: '訂位管理',
    description: '查看訂位總覽、訂位名單與桌位分配。',
    path: '/admin/reservation',
    icon: 'bx bx-calendar-check',
    roles: ['STAFF', 'MANAGER', 'ADMIN']
  },
  {
    title: '訂單管理',
    description: '管理顧客訂單、出餐流程與訂單狀態。',
    path: '/admin/order-manage',
    icon: 'bx bx-receipt',
    roles: ['STAFF', 'MANAGER', 'ADMIN']
  },
  {
    title: '菜單管理',
    description: '維護餐點分類、價格、描述與上下架狀態。',
    path: '/admin/menu-setting',
    icon: 'bx bx-food-menu',
    roles: ['MANAGER', 'ADMIN']
  },
  {
    title: '店家檔案設定',
    description: '維護店家基本資訊、營業設定與相關資料。',
    path: '/admin/profile',
    icon: 'bx bx-store',
    roles: ['MANAGER', 'ADMIN']
  },
  {
    title: '員工管理',
    description: '管理員工帳號、角色與基本資料。',
    path: '/admin/member',
    icon: 'bx bx-group',
    roles: ['ADMIN']
  },
  {
    title: '門市管理',
    description: '管理門市與桌位資料。',
    path: '/admin/store',
    icon: 'bx bx-buildings',
    roles: ['ADMIN']
  }
]

const availableFeatures = computed(() => {
  return featureCards.filter((item) => hasPermission(item.roles))
})

const rolePermissionRows = [
  {
    role: 'STAFF',
    title: '員工',
    description: '負責日常現場作業，可處理訂位與訂單。',
    permissions: ['訂位管理', '訂單管理']
  },
  {
    role: 'MANAGER',
    title: '店長',
    description: '負責店內營運，可管理訂位、訂單、菜單與店家資料。',
    permissions: ['訂位管理', '訂單管理', '菜單管理', '店家檔案設定']
  },
  {
    role: 'ADMIN',
    title: '管理員',
    description: '擁有系統最高權限，可使用所有後台功能。',
    permissions: ['全部功能']
  }
]
</script>

<template>
  <section class="admin-home">
    <!-- Welcome -->
    <div class="hero-card">
      <div>
        <p class="eyebrow">後台管理系統</p>
        <h1>歡迎回來，{{ displayName }}</h1>
        <p class="hero-desc">
          您目前的角色為
          <span class="role-pill">{{ roleText }}</span>
          ，可依權限使用對應的後台管理功能。
        </p>
      </div>

      <div class="hero-badge">
        <i class="bx bx-shield-quarter"></i>
        <span>Role-based Access</span>
      </div>
    </div>

    <!-- Available features -->
    <div class="section-header">
      <div>
        <h2>您目前可以使用的功能</h2>
        <p>依據登入角色顯示可操作的後台入口。</p>
      </div>
    </div>

    <div class="feature-grid">
      <button
        v-for="item in availableFeatures"
        :key="item.title"
        type="button"
        class="feature-card"
        @click="goTo(item)"
      >
        <div class="feature-icon">
          <i :class="item.icon"></i>
        </div>
        <div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
        </div>
      </button>
    </div>

    <!-- All feature permission preview -->
    <div class="section-header permission-title">
      <div>
        <h2>功能權限總覽</h2>
        <p>沒有權限的功能仍可顯示為入口，點擊時會提示權限不足。</p>
      </div>
    </div>

    <div class="all-feature-grid">
      <button
        v-for="item in featureCards"
        :key="item.title"
        type="button"
        class="permission-card"
        :class="{ locked: !hasPermission(item.roles) }"
        @click="goTo(item)"
      >
        <div class="permission-card-top">
          <i :class="item.icon"></i>
          <span v-if="hasPermission(item.roles)" class="status allowed">可使用</span>
          <span v-else class="status denied">權限不足</span>
        </div>

        <h3>{{ item.title }}</h3>
        <p>{{ item.description }}</p>
      </button>
    </div>

    <!-- Role explanation -->
    <div class="role-panel">
      <div class="role-panel-title">
        <h2>角色權限說明</h2>
        <p>目前系統依 STAFF、MANAGER、ADMIN 控制後台功能權限。</p>
      </div>

      <div class="role-list">
        <div
          v-for="row in rolePermissionRows"
          :key="row.role"
          class="role-row"
          :class="{ current: row.role === roleName }"
        >
          <div class="role-row-main">
            <div class="role-avatar">{{ row.role.charAt(0) }}</div>
            <div>
              <h3>{{ row.title }}</h3>
              <p>{{ row.description }}</p>
            </div>
          </div>

          <div class="permission-tags">
            <span
              v-for="permission in row.permissions"
              :key="permission"
            >
              {{ permission }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="future-note">
      <i class="bx bx-line-chart"></i>
      <div>
        <h3>後續可升級為資料儀表板</h3>
        <p>
          等訂位、訂單、菜單 API 完成後，可在此頁加入今日訂位、今日訂單、會員總數與菜單數量等即時統計。
        </p>
      </div>
    </div>
  </section>
</template>

<style scoped>
.admin-home{
  display:flex;
  flex-direction:column;
  gap:24px;
}

/* Welcome */

.hero-card{
  display:flex;
  justify-content:space-between;
  align-items:center;
  gap:24px;
  background:linear-gradient(135deg, #ffffff 0%, #fff7ef 100%);
  border-radius:16px;
  padding:32px;
  box-shadow:0 8px 28px rgba(0,0,0,0.05);
}

.eyebrow{
  margin:0 0 8px;
  color:#e3ac7f;
  font-weight:700;
  letter-spacing:.08em;
}

.hero-card h1{
  margin:0;
  font-size:32px;
  font-weight:800;
  color:#566a7f;
}

.hero-desc{
  margin:14px 0 0;
  color:#6f7f8f;
  line-height:1.8;
}

.role-pill{
  display:inline-flex;
  align-items:center;
  padding:4px 12px;
  margin:0 4px;
  border-radius:999px;
  background:#e3ac7f;
  color:white;
  font-weight:700;
}

.hero-badge{
  display:flex;
  align-items:center;
  gap:10px;
  flex-shrink:0;
  padding:14px 18px;
  border-radius:14px;
  background:white;
  color:#566a7f;
  font-weight:700;
  box-shadow:0 6px 18px rgba(0,0,0,0.06);
}

.hero-badge i{
  font-size:28px;
  color:#e3ac7f;
}

/* Section */

.section-header{
  display:flex;
  justify-content:space-between;
  align-items:flex-end;
  margin-top:4px;
}

.section-header h2,
.role-panel-title h2{
  margin:0;
  font-size:22px;
  color:#566a7f;
  font-weight:800;
}

.section-header p,
.role-panel-title p{
  margin:8px 0 0;
  color:#8a99a8;
}

.permission-title{
  margin-top:8px;
}

/* Feature cards */

.feature-grid{
  display:grid;
  grid-template-columns:repeat(auto-fit, minmax(240px, 1fr));
  gap:18px;
}

.feature-card{
  display:flex;
  align-items:flex-start;
  gap:16px;
  min-height:142px;
  padding:24px;
  border:0;
  border-radius:16px;
  background:white;
  text-align:left;
  cursor:pointer;
  box-shadow:0 8px 24px rgba(0,0,0,0.05);
  transition:.25s;
}

.feature-card:hover{
  transform:translateY(-4px);
  box-shadow:0 14px 32px rgba(0,0,0,0.08);
}

.feature-icon{
  width:52px;
  height:52px;
  border-radius:14px;
  display:flex;
  align-items:center;
  justify-content:center;
  background:#fff1e5;
  color:#e3ac7f;
  flex-shrink:0;
}

.feature-icon i{
  font-size:28px;
}

.feature-card h3,
.permission-card h3{
  margin:0;
  color:#566a7f;
  font-size:19px;
  font-weight:800;
}

.feature-card p,
.permission-card p{
  margin:10px 0 0;
  color:#7d8b9a;
  line-height:1.7;
}

/* Permission cards */

.all-feature-grid{
  display:grid;
  grid-template-columns:repeat(auto-fit, minmax(220px, 1fr));
  gap:16px;
}

.permission-card{
  border:1px solid #f0e2d5;
  border-radius:16px;
  background:white;
  padding:20px;
  text-align:left;
  cursor:pointer;
  transition:.25s;
}

.permission-card:hover{
  transform:translateY(-3px);
  box-shadow:0 10px 24px rgba(0,0,0,0.06);
}

.permission-card.locked{
  background:#fbfaf8;
  opacity:.82;
}

.permission-card-top{
  display:flex;
  justify-content:space-between;
  align-items:center;
  margin-bottom:16px;
}

.permission-card-top i{
  font-size:26px;
  color:#e3ac7f;
}

.status{
  padding:4px 10px;
  border-radius:999px;
  font-size:13px;
  font-weight:700;
}

.status.allowed{
  background:#eaf7ef;
  color:#2e9f5e;
}

.status.denied{
  background:#fff2ef;
  color:#e35d4f;
}

/* Role panel */

.role-panel{
  background:white;
  border-radius:16px;
  padding:26px;
  box-shadow:0 8px 24px rgba(0,0,0,0.05);
}

.role-list{
  display:flex;
  flex-direction:column;
  gap:14px;
  margin-top:20px;
}

.role-row{
  display:flex;
  align-items:center;
  justify-content:space-between;
  gap:18px;
  padding:18px;
  border-radius:14px;
  background:#fbfaf8;
  border:1px solid #f0e2d5;
}

.role-row.current{
  border-color:#e3ac7f;
  background:#fff7ef;
}

.role-row-main{
  display:flex;
  align-items:center;
  gap:14px;
}

.role-avatar{
  width:44px;
  height:44px;
  border-radius:14px;
  display:flex;
  align-items:center;
  justify-content:center;
  background:#e3ac7f;
  color:white;
  font-size:20px;
  font-weight:800;
  flex-shrink:0;
}

.role-row h3{
  margin:0;
  color:#566a7f;
  font-weight:800;
}

.role-row p{
  margin:6px 0 0;
  color:#7d8b9a;
}

.permission-tags{
  display:flex;
  flex-wrap:wrap;
  justify-content:flex-end;
  gap:8px;
}

.permission-tags span{
  padding:6px 10px;
  border-radius:999px;
  background:white;
  color:#566a7f;
  font-size:13px;
  font-weight:700;
  border:1px solid #f0e2d5;
}

/* Future */

.future-note{
  display:flex;
  align-items:flex-start;
  gap:16px;
  padding:22px 24px;
  border-radius:16px;
  background:#fff7ef;
  border:1px dashed #e3ac7f;
}

.future-note i{
  font-size:30px;
  color:#e3ac7f;
}

.future-note h3{
  margin:0;
  color:#566a7f;
  font-weight:800;
}

.future-note p{
  margin:8px 0 0;
  color:#7d8b9a;
  line-height:1.7;
}

/* Responsive */

@media(max-width:768px){
  .hero-card{
    flex-direction:column;
    align-items:flex-start;
  }

  .role-row{
    flex-direction:column;
    align-items:flex-start;
  }

  .permission-tags{
    justify-content:flex-start;
  }
}
</style>
