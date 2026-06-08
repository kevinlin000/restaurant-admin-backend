import { createRouter, createWebHistory } from 'vue-router'

import AdminLayout from '@/layouts/AdminLayout.vue'
import CustomerLayout from '@/layouts/CustomerLayout.vue'
import AdminHome from '@/views/admin/Home.vue'
import CustomerHome from '@/views/customer/Home.vue'
import AdminReservatioin from '@/views/admin/reservation/Reservation.vue'
import CustomerReservation from '@/views/customer/reservation/Reservation.vue'
import AdminReservationList from '@/views/admin/reservation/ReservationList.vue'
import AdminReservationTable from '@/views/admin/reservation/ReservationTable.vue'
import AdminReservationSetting from '@/views/admin/reservation/ReservationSetting.vue'
import CustomerMenu from '@/views/customer/menu/menu.vue'
import CustomerOrder from '@/views/customer/order/order.vue'
import CustomerStore from '@/views/customer/store/store.vue'
// ＊刪除(改不切頁)＊
import CustomerReservationSuccess from '@/views/customer/reservation/Reservation-success.vue'

const routers = [
  {
    path: '/',
    redirect: '/home',
    name: 'CustomerLayout',
    component: CustomerLayout,
    children: [
      {
        path: 'home',
        name: 'CustomerHome',
        component: CustomerHome,
      },
      {
        path: 'reservation',
        name: 'CustomerReservation',
        component: CustomerReservation
      },
      {
        path: 'reservation-success', // ＊刪除(改不切頁)＊
        name: 'CustomerReservationSuccess',
        component: CustomerReservationSuccess
      },
      {
        path: 'menu',
        name: 'CustomerMenu',
        component: CustomerMenu
      },
      {
        path: 'order',
        name: 'CustomerOrder',
        component: CustomerOrder
      },
      {
        path: 'store',
        name: 'CustomerStore',
        component: CustomerStore
      }
    ],
  },
  {
    path: '/admin',
    name: 'AdminLayout',
    redirect: '/admin/home',
    component: AdminLayout,
    children: [
      {
        path: 'home',
        name: 'AdminHome',
        component: AdminHome
      },
      {
        path: 'reservation',
        name: 'AdminReservation',
        component: AdminReservatioin
      },
      {
        path: 'reservation-list',
        name: 'AdminReservationList',
        component: AdminReservationList
      }
      ,
      {
        path: 'reservation-table',
        name: 'AdminReservationTable',
        component: AdminReservationTable
      }
      ,
      {
        path: 'reservation-setting',
        name: 'AdminReservationSetting',
        component: AdminReservationSetting
      }
    ],
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: routers
});

export default router
