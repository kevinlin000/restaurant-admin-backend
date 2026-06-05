<script setup>
import { RouterLink, RouterView } from 'vue-router'

// 後台側邊欄：管理各模組
const menu = [
  { name: '儀表板', path: '/admin' },
  { name: '門市管理', path: '/admin/stores' },
  { name: '會員管理', path: '/admin/members' },
  { name: '訂位管理', path: '/admin/reservations' },
  { name: '訂單管理', path: '/admin/orders' },
  { name: '菜單管理', path: '/admin/menu' },
]
</script>

<template>
  <div class="min-h-screen flex bg-gray-100 text-gray-800">
    <!-- 側邊欄 -->
    <aside class="w-56 bg-gray-900 text-gray-100 flex flex-col">
      <div class="h-16 flex items-center px-6 text-lg font-bold border-b border-gray-700">
        後台管理
      </div>
      <nav class="flex-1 p-4 space-y-1 text-sm">
        <RouterLink
          v-for="item in menu"
          :key="item.path"
          :to="item.path"
          class="block px-3 py-2 rounded hover:bg-gray-700 transition-colors"
          active-class="bg-rose-600 text-white"
          :end="item.path === '/admin'"
        >
          {{ item.name }}
        </RouterLink>
      </nav>
    </aside>

    <!-- 後台內容 -->
    <main class="flex-1 p-8 overflow-auto">
      <RouterView />
    </main>
  </div>
</template>
