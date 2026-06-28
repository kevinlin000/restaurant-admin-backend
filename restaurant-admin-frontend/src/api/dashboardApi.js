import http from "./http";

export const getDashboardSummary = () =>
  http.get("/admin/dashboard/summary");

export const getDailyRevenue = () =>
  http.get("/admin/dashboard/daily-revenue");

export const getTopMenuItems = () =>
  http.get("/admin/dashboard/top-menu-items");

export const getPaymentMethodRatio = () =>
  http.get("/admin/dashboard/payment-method-ratio");

export const getOrderStatusRatio = () =>
  http.get("/admin/dashboard/order-status-ratio");