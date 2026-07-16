// 訂位頁面讀取分店資料
import http from './http'
import { demoStores, getDemoStoreDetail } from './demoData'

export const storeApi = {
  async getStores(options = {}) {
    try {
      const response = await http.get(options.admin ? '/admin/stores' : '/stores')
      if (!options.admin && !Array.isArray(response?.data)) return { data: demoStores }
      return response
    } catch (error) {
      if (options.admin) throw error
      return { data: demoStores }
    }
  },

  async getStoreDetail(storeId, options = {}) {
    try {
      const response = await http.get(`${options.admin ? '/admin/stores' : '/stores'}/${storeId}`)
      if (!options.admin && (!response?.data || typeof response.data !== 'object')) {
        return { data: getDemoStoreDetail(storeId) }
      }
      return response
    } catch (error) {
      if (options.admin) throw error
      return { data: getDemoStoreDetail(storeId) }
    }
  },

  updateStore(storeId, payload) {
    return http.put(`/admin/stores/${storeId}`, payload)
  },

  getStoreTables(storeId) {
    return http.get(`/admin/stores/${storeId}/tables`)
  },
}
