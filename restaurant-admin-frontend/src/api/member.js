import api from "./axios";

// 登入
export const login = (data) => api.post("/api/members/login", data);

// 註冊
export const register = (data) => api.post("/api/members/register", data);

// 取得個人資料
export const getProfile = () => api.get("/api/members/me");

// 修改個人資料
export const updateProfile = (data) => api.put("/api/members/me", data);

// 修改密碼
export const updatePassword = (data) =>
  api.put("/api/members/me/password", data);

// 刪除帳號
export const deleteAccount = () => api.delete("/api/members/me");

// 寄信至Email
export const sendEmailCode = (email) =>
  api.post("/api/members/email/send-code", { email });

export const verifyEmailCode = (email, code) =>
  api.post("/api/members/email/verify-code", {
    email,
    code,
  });
