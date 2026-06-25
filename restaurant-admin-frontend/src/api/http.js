import axios from "axios";
import router from "@/router";

/**
 * 全域 axios 實例
 * 後端 API 統一前綴 /api（dev 由 vite proxy 轉到 localhost:8080）
 *
 * 注意：會員登入成功後 token 存在 localStorage.accessToken。
 * 原本此檔案讀取 token 並帶入不存在的 accessToken 變數，
 * 會導致使用 http.js 的後台 API 無法正常送出 JWT。
 */
const http = axios.create({
  baseURL: "/api",
  timeout: 10000,
});

// 請求攔截器：自動帶 JWT
http.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
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
