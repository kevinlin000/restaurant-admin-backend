import { createRouter, createWebHistory } from "vue-router";

import AdminLayout from "@/layouts/AdminLayout.vue";
import CustomerLayout from "@/layouts/CustomerLayout.vue";
import AdminHome from "@/views/admin/Home.vue";
import CustomerHome from "@/views/customer/Home.vue";

import AdminReservatioin from "@/views/admin/reservation/Reservation.vue";
import CustomerReservation from "@/views/customer/reservation/Reservation.vue";
import AdminReservationList from "@/views/admin/reservation/ReservationList.vue";
import AdminReservationTable from "@/views/admin/reservation/ReservationTable.vue";
import CustomerReservationSuccess from "@/views/customer/reservation/Reservation-success.vue";
import AdminReservationTimeSetting from '@/views/admin/reservation/ReservationTimeSetting.vue'
import AdminMenuCreate from "@/views/admin/menu/menu-create.vue";
import AdminMenuEdit from "@/views/admin/menu/menu-edit.vue";
import AdminMenuSetting from "@/views/admin/menu/menu-setting.vue";
import AdminStore from "@/views/admin/store/store.vue";
import CustomerMenu from "@/views/customer/menu/menu.vue";
import CustomerOrder from "@/views/customer/order/order.vue";
import CustomerStore from "@/views/customer/store/store.vue";
import AdminOrderManage from "@/views/admin/order/AdminOrderManage.vue";
// 開發時測試用，正式

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
        path: "reservation-time-setting",
        name: "AdminReservationTimeSetting",
        component: AdminReservationTimeSetting
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
      {
        path: "order-manage",
        name: "AdminOrderManage",
        component: AdminOrderManage,
      },
      {
        path: "store",
        name: "AdminStore",
        component: AdminStore,
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: routers,
});

const getDefaultPathByRole = (roleName) => {
  if (["CUSTOMER", "STAFF", "MANAGER"].includes(roleName)) {
    return "/home";
  }

  if (roleName === "ADMIN") {
    return "/admin/home";
  }

  return "/login";
};

const getUserInfo = () => {
  try {
    return JSON.parse(localStorage.getItem("userInfo") || "{}");
  } catch (error) {
    localStorage.removeItem("userInfo");
    return {};
  }
};

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("accessToken");
  const userInfo = getUserInfo();
  const roleName = userInfo.roleName;

  const isAdminPage = to.path.startsWith("/admin");
  const isCustomerProfilePage = to.path === "/profile";

  // 未登入不能進會員中心或後台
  if (!token && (isAdminPage || isCustomerProfilePage)) {
    next("/login");
    return;
  }

  // 已登入後，不要再進登入或註冊頁
  if (token && (to.path === "/login" || to.path === "/register")) {
    next(getDefaultPathByRole(roleName));
    return;
  }

  // CUSTOMER 不能進後台
  if (isAdminPage && roleName === "CUSTOMER") {
    next("/profile");
    return;
  }
  next();
});
export default router;
