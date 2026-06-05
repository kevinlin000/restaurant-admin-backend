<script setup>
import { RouterLink, RouterView } from 'vue-router'

// 顧客端導覽列：訂位、瀏覽門市/菜單
const navLinks = [
  { name: '首頁', path: '/' },
  { name: '門市', path: '/stores' },
  { name: '菜單', path: '/menu' },
  { name: '訂位', path: '/reservations' },
  { name: '我的訂單', path: '/orders' },
]
</script>

<template>
  <div class="min-h-screen flex flex-col bg-gray-50 text-gray-800">
    <!-- 顧客導覽列 -->
    <header class="bg-white shadow-sm">
      <nav class="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between">
        <RouterLink to="/" class="text-xl font-bold text-rose-600">
          敘日餐廳
        </RouterLink>
        <ul class="flex gap-6 text-sm font-medium">
          <li v-for="link in navLinks" :key="link.path">
            <RouterLink
              :to="link.path"
              class="hover:text-rose-600 transition-colors"
              active-class="text-rose-600"
            >
              {{ link.name }}
            </RouterLink>
          </li>
        </ul>
      </nav>
    </header>

    <!-- 顧客內容 -->
    <main class="flex-1 max-w-6xl w-full mx-auto px-4 py-8">
      <RouterView />
    </main>

    <!-- 頁尾 -->
    <footer class="bg-white border-t">
      <div class="max-w-6xl mx-auto px-4 py-6 text-center text-sm text-gray-500">
        © 2026 敘日餐廳訂位系統 · 期末專案
      </div>
    </footer>
  </div>
</template>
