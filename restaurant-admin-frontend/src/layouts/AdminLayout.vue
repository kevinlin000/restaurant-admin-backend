<script setup>
import { ref } from 'vue'

const openMenu = ref({
  dashboard: true,
  reservation: true,
  menu: false,
  order: false,
  store: false
})

const toggleMenu = (menu) => {
  openMenu.value[menu] = !openMenu.value[menu]
}
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

      <!-- 系統後台管理 -->
      <li class="menu-group">
            <RouterLink class="menu-title" to="/admin/home">
            <div>
                <i class='bx bx-home-circle'></i>
                系統總覽
            </div>
            </RouterLink>
      </li>

      <!-- 用戶設定 -->
      <li class="menu-group">

        <div class="menu-title " @click="toggleMenu('dashboard')">
          <div>
            <i class='bx bx-user'></i>
            用戶設定
          </div>
          <i class='bx bx-chevron-down'></i>
        </div>

        <ul v-show="openMenu.dashboard" class="submenu">
          <li>
            <RouterLink to="/admin/profile">店家檔案設定</RouterLink>
          </li>
          <li>
            <RouterLink to="/admin/system-setting">設定</RouterLink>
          </li>
          <hr>
        </ul>
      </li>

      <!-- 訂位管理 -->
      <li class="menu-group">

        <div class="menu-title" @click="toggleMenu('reservation')">

          <div>
            <i class='bx bx-calendar'></i>
            訂位管理
          </div>

          <i class='bx bx-chevron-down'></i>
        </div>

        <ul v-show="openMenu.reservation" class="submenu">
          <li>
            <RouterLink to="/admin/reservation">訂位總覽</RouterLink>
          </li>
          <li>
            <RouterLink to="/admin/reservation-list">訂位名單</RouterLink>
          </li>
          <li>
            <RouterLink to="/admin/reservation-table">分配桌位</RouterLink>
          </li>
          <!-- <li>
            <RouterLink to="/admin/table-setting">桌位設定</RouterLink>
          </li> -->
          <li>
            <RouterLink to="/admin/reservation-setting">設定</RouterLink>
          </li>
          <!-- <li>
            <RouterLink to="/admin/reservation-time">開放預約日期＆時段</RouterLink>
          </li> -->
          <hr>
        </ul>
      </li>

      <!-- 菜單管理 -->
      <li class="menu-group">

        <div class="menu-title " @click="toggleMenu('menu')">
          <div>
            <i class='bx bx-food-menu'></i>
            菜單管理
          </div>
          <i class='bx bx-chevron-down'></i>
        </div>

        <ul v-show="openMenu.menu" class="submenu">
          <li>
            <RouterLink to="/admin/menu-create">新增菜單</RouterLink>
          </li>
          <li>
            <RouterLink to="/admin/menu-edit">修改菜單</RouterLink>
          </li>
          <li>
            <RouterLink to="/admin/menu-setting">設定</RouterLink>
          </li>
          <hr>
        </ul>
      </li>

      <!-- 訂餐管理 -->
      <li class="menu-group">

        <div class="menu-title " @click="toggleMenu('order')">
          <div>
            <i class='bx bx-cart'></i>
            訂餐管理
          </div>
          <i class='bx bx-chevron-down'></i>
        </div>

        <ul v-show="openMenu.order" class="submenu">
          <li>
            <RouterLink to="/admin/order-manage">管理訂單</RouterLink>
          </li>
          <li>
            <RouterLink to="/admin/order-create">新增餐點</RouterLink>
          </li>
          <li>
            <RouterLink to="/admin/order-edit">修改餐點</RouterLink>
          </li>
          <li>
            <RouterLink to="/admin/order-setting">設定</RouterLink>
          </li>
          <hr>
        </ul>
      </li>

      <!-- 分店管理 -->
      <li class="menu-group">

        <div class="menu-title " @click="toggleMenu('store')">
          <div>
            <i class='bx bx-store'></i>
            分店管理
          </div>
          <i class='bx bx-chevron-down'></i>
        </div>

        <ul v-show="openMenu.store" class="submenu">
          <li>
            <RouterLink to="/admin/store-create">新增分店</RouterLink>
          </li>
          <li>
            <RouterLink to="/admin/store-edit">修改分店</RouterLink>
          </li>
          <li>
            <RouterLink to="/admin/store-setting">設定</RouterLink>
          </li>
        </ul>
      </li>
    </ul>

    <!-- 登入在左側導覽頁下方 -->
    <!-- <div class="sidebar-footer">
    <div class="profile-box">
        <img src="../assets/images/logo.png" class="logo-image"/>
        <div>
        <div class="profile-name">店家</div>
        <div class="profile-role">管理員</div>
        </div>
    </div>
        <hr>
        <i class="bx bx-log-out me-2 text-danger"></i>
        <RouterLink class="text-danger" to="/admin/login">登出</RouterLink>
    </div> -->
    
  </aside>

  <!-- 右上角的 dropdown menu -->
  <!-- Main -->
  <div class="main-wrapper">
    <!-- Navbar -->
    <nav class="top-navbar">
      <!-- Right -->
      <div class="navbar-right dropdown ms-auto">
        <div class="profile-box dropdown-toggle" data-bs-toggle="dropdown">
          <img
            src="../assets/images/logo.png"
            class="logo-image" />
          <div>
            <div class="profile-name">店家</div>
            <div class="profile-role">管理員</div>
          </div>
        </div>

        <!-- Dropdown -->
        <ul class="dropdown-menu dropdown-menu-end">
          <li>
            <a class="dropdown-item">店家檔案管理</a>
          </li>

          <li>
            <a class="dropdown-item">設定</a>
          </li>

          <li><hr class="dropdown-divider"></li>

          <li>
            <a class="dropdown-item text-danger">登出</a>
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

.admin-layout{
  display:flex;
  min-height:100vh;
  background:#f8f3ed;
}

/* Sidebar */

.sidebar{
  width:280px;
  background:white;
  padding:24px;
  position:fixed;
  top:0;
  left:0;
  bottom:0;
  display:flex;
  flex-direction:column;
  box-shadow:0 0 20px rgba(0,0,0,0.05);
}

.sidebar-logo{
  display:flex;
  align-items:center;
  gap:8px;
  margin-bottom:40px;
  font-size:24px;
  font-weight:700;
  color: black;
}

.sidebar-menu{
  list-style:none;
  padding:0;
  flex:1;
  overflow-y:auto;
}

.menu-group{
  margin-bottom:14px;
}

.menu-title{
  display:flex;
  justify-content:space-between;
  align-items:center;
  padding:14px 18px;
  border-radius:12px;
  cursor:pointer;
  transition:.25s;
  color:#566a7f;
  font-weight:700;
}

.menu-title:hover{
  background:#f8f3ed;
  color:#e3ac7f;
}

.menu-title div{
  display:flex;
  align-items:center;
  gap:12px;
}

.menu-title i{
  font-size:22px;
}

/* submenu */

.submenu{
  list-style:none;
  margin-top:8px;
  padding-left:18px;
}

.submenu li{
  margin:8px 0;
}

.submenu a{
  display:block;
  padding:10px 14px;
  border-radius:10px;
  text-decoration:none;
  color:#566a7f;
  transition:.25s;
}

.submenu a:hover{
  background:#e3ac7f;
  color:white;
}

.router-link-active{
  background:#e3ac7f;
  color:white !important;
}

/* Main */

.main-wrapper{
  flex:1;
  margin-left:280px;
}

/* Navbar */

.top-navbar{
  height:80px;
  background:white;
  margin:20px;
  border-radius:10px;
  padding:0 24px;

  display:flex;
  align-items:center;
  justify-content:space-between;

  box-shadow:0 4px 20px rgba(0,0,0,0.05);
}

/* Search */

.search-box{
  width:320px;
  display:flex;
  align-items:center;
  gap:10px;

  background:#f5f5f9;
  padding:12px 16px;
  border-radius:12px;
}

.search-box input{
  border:none;
  outline:none;
  background:transparent;
  width:100%;
}

/* Right */

.profile-box{
  display:flex;
  align-items:center;
  cursor:pointer;
}

.profile-name{
  font-weight:700;
}

.profile-role{
  font-size:14px;
  color:gray;
}

/* Content */

.main-content{
  padding:0 20px 20px;
}

/* Mobile 

@media(max-width:992px){

  .sidebar{
    width:90px;
    padding:20px 10px;
  }

  .sidebar-logo span,
  .menu-title span,
  .submenu{
    display:none;
  }

  .main-wrapper{
    margin-left:90px;
  }

} */

</style>