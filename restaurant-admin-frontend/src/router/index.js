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
import AdminReservationTimeSetting from "@/views/admin/reservation/ReservationTimeSetting.vue";
import AdminMenuCreate from "@/views/admin/menu/menu-create.vue";
import AdminMenuEdit from "@/views/admin/menu/menu-edit.vue";
import AdminMenuSetting from "@/views/admin/menu/menu-setting.vue";
import AdminStore from "@/views/admin/store/store.vue";
import AdminNews from "@/views/admin/news/news.vue";
import AdminFaq from "@/views/admin/faq/faq.vue";
import AdminFaqAnalytics from "@/views/admin/faq/FaqAnalytics.vue";
import AdminHomepage from "@/views/admin/homepage/HomepageAdmin.vue";
import AdminMember from "@/views/admin/member/member.vue";
import AdminOrderManage from "@/views/admin/order/AdminOrderManage.vue";
import CustomerMenu from "@/views/customer/menu/menu.vue";
import CustomerOrder from "@/views/customer/order/order.vue";
import CustomerStore from "@/views/customer/store/store.vue";
import CustomerNews from "@/views/customer/news/news.vue";
import CustomerFaq from "@/views/customer/faq/faq.vue";
import AdminOrderDashboard from "@/views/admin/order/AdminOrderDashboard.vue";
const ROLE = {
  STAFF: "STAFF",
  MANAGER: "MANAGER",
  ADMIN: "ADMIN",
  CUSTOMER: "CUSTOMER",
};

const ADMIN_ROLES = [ROLE.STAFF, ROLE.MANAGER, ROLE.ADMIN];
const MANAGER_ROLES = [ROLE.MANAGER, ROLE.ADMIN];
const ADMIN_ONLY = [ROLE.ADMIN];

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
        path: "payment-success",
        name: "PaymentSuccess",
        component: () => import("@/views/customer/order/PaymentSuccessView.vue"),
      },
      {
        path: "guest-order-detail",
        name: "GuestOrderDetail",
        component: () => import("@/views/customer/order/GuestOrderDetail.vue"),
      },
      {
        path: "store",
        name: "CustomerStore",
        component: CustomerStore,
      },
      {
        path: "news",
        name: "CustomerNews",
        component: CustomerNews,
      },
      {
        path: "faq",
        name: "CustomerFaq",
        component: CustomerFaq,
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
        meta: { requiresAuth: true },
      },
    ],
  },
  {
    path: "/admin",
    name: "AdminLayout",
    redirect: "/admin/home",
    component: AdminLayout,
    meta: { requiresAuth: true, roles: ADMIN_ROLES },
    children: [
      {
        path: "home",
        name: "AdminHome",
        component: AdminHome,
        meta: { roles: ADMIN_ROLES },
      },
      {
        path: "reservation",
        name: "AdminReservation",
        component: AdminReservatioin,
        meta: { roles: ADMIN_ROLES },
      },
      {
        path: "reservation-list",
        name: "AdminReservationList",
        component: AdminReservationList,
        meta: { roles: ADMIN_ROLES },
      },
      {
        path: "reservation-table",
        name: "AdminReservationTable",
        component: AdminReservationTable,
        meta: { roles: ADMIN_ROLES },
      },
      {
        path: "reservation-time-setting",
        name: "AdminReservationTimeSetting",
        component: AdminReservationTimeSetting,
        meta: { roles: ADMIN_ROLES },
      },
      {
        path: "menu-create",
        name: "AdminMenuCreate",
        component: AdminMenuCreate,
        meta: { roles: MANAGER_ROLES },
      },
      {
        path: "menu-edit/:id",
        name: "AdminMenuEdit",
        component: AdminMenuEdit,
        meta: { roles: MANAGER_ROLES },
      },
      {
        path: "menu-setting",
        name: "AdminMenuSetting",
        component: AdminMenuSetting,
        meta: { roles: MANAGER_ROLES },
      },
      {
        path: "order-manage",
        name: "AdminOrderManage",
        component: AdminOrderManage,
        meta: { roles: ADMIN_ROLES },
      },
      {
        path: "order-dashboard",
        name: "AdminOrderDashboard",
        component: AdminOrderDashboard,
        meta: { roles: ADMIN_ROLES },
      },
      {
        path: "member",
        name: "AdminMember",
        component: AdminMember,
        meta: { roles: ADMIN_ONLY },
      },
      {
        path: "store",
        name: "AdminStore",
        component: AdminStore,
        meta: { roles: MANAGER_ROLES },
      },
      {
        path: "news",
        name: "AdminNews",
        component: AdminNews,
        meta: { roles: MANAGER_ROLES },
      },
      {
        path: "faqs",
        name: "AdminFaq",
        component: AdminFaq,
        meta: { roles: ADMIN_ONLY },
      },
      {
        path: "faq-analytics",
        name: "AdminFaqAnalytics",
        component: AdminFaqAnalytics,
        meta: { roles: ADMIN_ONLY },
      },
      {
        path: "homepage",
        name: "AdminHomepage",
        component: AdminHomepage,
        meta: { roles: ADMIN_ONLY },
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: routers,
  scrollBehavior(to, from, savedPosition) {
    return savedPosition || { top: 0 };
  },
});

const getDefaultPathByRole = (roleName) => {
  if (roleName === ROLE.CUSTOMER) {
    return "/profile";
  }

  if (ADMIN_ROLES.includes(roleName)) {
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

const getRouteRoles = (to) => {
  const roleMeta = [...to.matched]
    .reverse()
    .find((record) => Array.isArray(record.meta?.roles));

  return roleMeta?.meta?.roles || null;
};

router.beforeEach((to, from, next) => {
  const userInfo = getUserInfo();
  const roleName = userInfo.roleName;
  const isLoggedIn = Boolean(userInfo.userId && roleName);

  const isAdminPage = to.path.startsWith("/admin");
  const isProfilePage = to.path === "/profile";
  const requiresAuth = to.matched.some((record) => record.meta?.requiresAuth);
  const allowedRoles = getRouteRoles(to);

  if (!isLoggedIn && (requiresAuth || isAdminPage || isProfilePage)) {
    next({
      path: "/login",
      query: { redirect: to.fullPath },
    });
    return;
  }

  if (isLoggedIn && (to.path === "/login" || to.path === "/register")) {
    next(getDefaultPathByRole(roleName));
    return;
  }

  if (isAdminPage && roleName === ROLE.CUSTOMER) {
    next("/profile");
    return;
  }

  if (isProfilePage && roleName === ROLE.ADMIN) {
    next("/admin/home");
    return;
  }

  if (allowedRoles && !allowedRoles.includes(roleName)) {
    next(getDefaultPathByRole(roleName));
    return;
  }

  next();
});

export default router;
