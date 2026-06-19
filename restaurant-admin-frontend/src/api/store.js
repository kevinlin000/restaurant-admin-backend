// 測試：訂位頁面讀取分店資料
import http from './http'

export const storeApi = {
  getStores(options = {}) {
    return http.get(options.admin ? '/admin/stores' : '/stores')
  },

  getStoreDetail(storeId, options = {}) {
    return http.get(`${options.admin ? '/admin/stores' : '/stores'}/${storeId}`)
  },

  updateStore(storeId, payload) {
    return http.put(`/admin/stores/${storeId}`, payload)
  },

  getStoreTables(storeId) {
    return http.get(`/admin/stores/${storeId}/tables`)
  },
}
