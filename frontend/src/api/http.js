import axios from 'axios'

/**
 * 全域 axios 實例
 * 所有 API 模組共用，baseURL 指向後端 /api
 */
const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// 請求攔截器：自動帶上 JWT（之後會員模組登入後存 token）
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 回應攔截器：統一拆出後端 ApiResponse 的 data
http.interceptors.response.use(
  (response) => response.data,
  (error) => Promise.reject(error),
)

export default http
