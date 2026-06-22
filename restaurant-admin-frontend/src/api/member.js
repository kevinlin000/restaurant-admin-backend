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

// 忘記密碼：寄送驗證碼
export const forgotPassword = (email) =>
  api.post("/api/members/password/forgot", { email });

// 忘記密碼：重設密碼
export const resetPassword = (data) =>
  api.post("/api/members/password/reset", data);

// 查詢會員目前點數
export const getPointBalance = () => api.get("/api/members/me/points");

// 查詢會員點數紀錄
export const getPointHistory = () => api.get("/api/members/me/points/history");

// 後台：會員/員工統計摘要
export const getMemberAdminSummary = () => api.get("/api/admin/members/summary");

// 後台：員工管理
export const getStaffList = () => api.get("/api/members/staff");
export const createStaff = (data) => api.post("/api/members/staff", data);
export const resignStaff = (staffId) =>
  api.put(`/api/members/staff/${staffId}/resign`);
