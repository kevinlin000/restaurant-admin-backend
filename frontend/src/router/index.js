import { createRouter, createWebHistory } from 'vue-router'
import CustomerLayout from '@/layouts/CustomerLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

const routes = [
  // 顧客端：套 CustomerLayout
  {
    path: '/',
    component: CustomerLayout,
    children: [
      { path: '', name: 'home', component: () => import('@/views/customer/HomeView.vue') },
      // 各模組顧客頁之後補上，例如：
      // { path: 'stores', name: 'stores', component: () => import('@/views/customer/StoreListView.vue') },
      // { path: 'reservations', name: 'reservations', component: () => import('@/views/customer/ReservationView.vue') },
    ],
  },
  // 後台：套 AdminLayout，整段一次掛權限守衛
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      { path: '', name: 'dashboard', component: () => import('@/views/admin/DashboardView.vue') },
      // 各模組管理頁之後補上，例如：
      // { path: 'orders', name: 'admin-orders', component: () => import('@/views/admin/OrderManageView.vue') },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
