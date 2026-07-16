import axios from "axios";
import router from "@/router";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "",
  timeout: 30000,
  withCredentials: true,
});

api.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem("accessToken");

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error),
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;

    // 401：沒有登入或 token 失效，才清除登入狀態。
    if (status === 401) {
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

    // 403：代表有登入但權限不足，不應該把使用者登出。
    return Promise.reject(error);
  },
);

export default api;
