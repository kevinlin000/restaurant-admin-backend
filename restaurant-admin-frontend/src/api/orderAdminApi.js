import axios from 'axios'

const API_URL = 'http://localhost:8080/api/admin/orders'

const getAuthConfig = () => {
  const token = sessionStorage.getItem('accessToken')

  return {
    withCredentials: true,
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    }
  }
}

export const getAdminOrders = () => {
  return axios.get(API_URL, getAuthConfig())
}

export const getAdminOrderById = (orderId) => {
  return axios.get(`${API_URL}/${orderId}`, getAuthConfig())
}

export const updateAdminOrderStatus = (orderId, status) => {
  return axios.patch(
    `${API_URL}/${orderId}/status`,
    { status },
    getAuthConfig()
  )
}
export const markAdminOrderPaymentPaid = (orderId, paymentMethod) => {
  return axios.patch(
    `${API_URL}/${orderId}/payment/paid`,
    { paymentMethod },
    getAuthConfig()
  )
}
