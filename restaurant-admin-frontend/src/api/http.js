import axios from "axios";

/**
 * 全域 axios 實例
 * 後端 API 統一前綴 /api（dev 由 vite proxy 轉到 localhost:8080）
 */
const http = axios.create({
  baseURL: "/api",
  timeout: 10000,
});

// 請求攔截器：自動帶 JWT（會員登入後存 localStorage）
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
  (error) => Promise.reject(error),
);

export default http;
