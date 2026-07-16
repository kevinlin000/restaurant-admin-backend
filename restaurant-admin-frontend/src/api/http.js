import axios from "axios";
import router from "@/router";

/**
 * 全域 axios 實例
 * 後端 API 統一前綴 /api（dev 由 vite proxy 轉到 localhost:8080）
 *
 * 登入後主要靠 HttpOnly Cookie 驗證；sessionStorage token 只保留作為舊版相容。
 */
const http = axios.create({
  baseURL: "/api",
  timeout: 30000,
  withCredentials: true,
});

// 請求攔截器：舊版相容用，正式驗證優先使用 HttpOnly Cookie。
http.interceptors.request.use((config) => {
  const token = sessionStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

// 回應攔截器：拆出後端 ApiResponse 的 body（{ success, message, data }）
http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("accessToken");
      sessionStorage.removeItem("accessToken");
      localStorage.removeItem("userInfo");
      window.dispatchEvent(new Event("login-state-changed"));

      if (router.currentRoute.value.path !== "/login") {
        router.push({
          path: "/login",
          query: { redirect: router.currentRoute.value.fullPath },
        });
      }
    }

    return Promise.reject(error);
  },
);

export default http;
