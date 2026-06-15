import { createRouter, createWebHistory } from "vue-router";

import AdminLayout from "@/layouts/AdminLayout.vue";
import CustomerLayout from "@/layouts/CustomerLayout.vue";
import AdminHome from "@/views/admin/Home.vue";
import CustomerHome from "@/views/customer/Home.vue";

import AdminReservatioin from "@/views/admin/reservation/Reservation.vue";
import CustomerReservation from "@/views/customer/reservation/Reservation.vue";
import AdminReservationList from "@/views/admin/reservation/ReservationList.vue";
import AdminReservationTable from "@/views/admin/reservation/ReservationTable.vue";
import AdminReservationSetting from "@/views/admin/reservation/ReservationSetting.vue";

import AdminMenuCreate from "@/views/admin/menu/menu-create.vue";
import AdminMenuEdit from "@/views/admin/menu/menu-edit.vue";
import AdminMenuSetting from "@/views/admin/menu/menu-setting.vue";
import CustomerMenu from "@/views/customer/menu/menu.vue";

import CustomerOrder from "@/views/customer/order/order.vue";
import CreditCardView from "@/views/customer/order/CreditCardView.vue";
import LinePayView from "@/views/customer/order/LinePayView.vue";
import PaymentSuccessView from "@/views/customer/order/payment/PaymentSuccessView.vue";

import CustomerStore from "@/views/customer/store/store.vue";

import CustomerReservationSuccess from "@/views/customer/reservation/Reservation-success.vue";

const routers = [
  {
    path: "/",
    redirect: "/home",
    name: "CustomerLayout",
    component: CustomerLayout,
    children: [
      {
        path: "home",
        name: "CustomerHome",
        component: CustomerHome,
      },
      {
        path: "reservation",
        name: "CustomerReservation",
        component: CustomerReservation,
      },
      {
        path: "reservation-success",
        name: "CustomerReservationSuccess",
        component: CustomerReservationSuccess,
      },
      {
        path: "menu",
        name: "CustomerMenu",
        component: CustomerMenu,
      },
      {
        path: "order",
        name: "CustomerOrder",
        component: CustomerOrder,
      },
      {
        path: "store",
        name: "CustomerStore",
        component: CustomerStore,
      },
      {
        path: "login",
        name: "CustomerLogin",
        component: () => import("@/views/customer/member/login.vue"),
      },
      {
        path: "register",
        name: "CustomerRegister",
        component: () => import("@/views/customer/member/register.vue"),
      },
      {
        path: "profile",
        name: "CustomerProfile",
        component: () => import("@/views/customer/member/profile.vue"),
      },
    ],
  },
  {
    path: "/payment/linepay/:orderId",
    name: "linepay",
    component: LinePayView,
  },
  {
    path: "/payment/card/:orderId",
    name: "creditcard",
    component: CreditCardView,
  },
  {
    path: "/payment-success",
    name: "PaymentSuccess",
    component: PaymentSuccessView,
  },
  {
    path: "/admin",
    name: "AdminLayout",
    redirect: "/admin/home",
    component: AdminLayout,
    children: [
      {
        path: "home",
        name: "AdminHome",
        component: AdminHome,
      },
      {
        path: "reservation",
        name: "AdminReservation",
        component: AdminReservatioin,
      },
      {
        path: "reservation-list",
        name: "AdminReservationList",
        component: AdminReservationList,
      },
      {
        path: "reservation-table",
        name: "AdminReservationTable",
        component: AdminReservationTable,
      },
      {
        path: "reservation-setting",
        name: "AdminReservationSetting",
        component: AdminReservationSetting,
      },
      {
        path: "menu-create",
        name: "AdminMenuCreate",
        component: AdminMenuCreate,
      },
      {
        path: "menu-edit/:id",
        name: "AdminMenuEdit",
        component: AdminMenuEdit,
      },
      {
        path: "menu-setting",
        name: "AdminMenuSetting",
        component: AdminMenuSetting,
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: routers,
});

export default router;