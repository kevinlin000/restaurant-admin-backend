import http from './http'

// ========== 顧客端訂位 API ==========
export const reservationApi = {
  
  // 顯示可訂日期時段
  getAvailableSlots(params) {
    return http.get('/reservations/slots', { params })
  },

  // 剩餘可訂桌位數
  getSlotCapacity(slotId) {
    return http.get(`/reservations/slots/${slotId}/capacity`)
  },

  // 送出訂位時，檢查容量扣 reserved_count。
  createReservation(payload) {
    return http.post('/reservations', payload)
  },

  // 編輯訂位，重新檢查時段、容量
  updateReservation(reservationId, payload) {
    return http.put(`/reservations/${reservationId}`, payload)
  },

  // 保留訂位，狀態從 PENDING 改為 RESERVED。
  reserveReservation(reservationId) {
    return http.patch(`/reservations/${reservationId}/reserve`)
  },

  // 訂位成功、編輯，用來讀取單筆訂位最新資料。
  getReservation(reservationId) {
    return http.get(`/reservations/${reservationId}`)
  },

  // 登入會員依 JWT 查詢自己的訂位
  getMyReservations() {
    return http.get('/reservations/me')
  },

  // 依 userId 查訂位
  getUserReservations(userId) {
    return http.get(`/reservations/user/${userId}`)
  },

  // 取消訂位，恢復容量、移除已配桌資料
  cancelReservation(reservationId) {
    return http.delete(`/reservations/${reservationId}`)
  },
}

// ========== 後台訂位管理 API (訂位總覽、訂位名單、分配桌位) ==========
export const reservationAdminApi = {

  // 查全部訂位 (訂位名單、總數統計)
  getReservations(storeId) {
    return http.get('/admin/reservations', { params: { storeId } })
  },

  // 查分配桌位：未配桌、已保留、已配桌 
  getUnassignedReservations(storeId) {
    return http.get('/admin/reservations/unassigned', { params: { storeId } })
  },

  // 分配桌位頁面：依日期、狀態查
  getUnassignedReservationsByDate(storeId, date, status) {
    return http.get('/admin/reservations/unassigned', { params: { storeId, date, status } })
  },

  // 查今日總覽
  getDailyOverview(storeId, date) {
    return http.get('/admin/reservations/daily-overview', { params: { storeId, date } })
  },

  // 分配實體桌位，寫入 reservation_table。
  assignTables(payload) {
    return http.post('/admin/reservations/assign-tables', payload)
  },

  // 後台幫顧客保留訂位，狀態 PENDING 改 RESERVED。
  reserve(reservationId) {
    return http.patch('/admin/reservations/reserve', null, { params: { reservationId } })
  },

  // 今日總覽：勾選實際入座，狀態改 CHECKED_IN ，不能再編輯、配桌
  checkIn(reservationId) {
    return http.patch('/admin/reservations/check-in', null, { params: { reservationId } })
  },

  // 訂位名單：編輯
  updateReservationInfo(reservationId, payload) {
    return http.put(`/admin/reservations/${reservationId}`, payload)
  },
}

// ========== 後台訂位設定 API （可訂日期＆時段、時段的容量）==========
export const reservationSettingApi = {

  // 查詢已設定的時段
  getTimeSlots(storeId, date) {
    return http.get('/admin/reservation-settings/time-slots', { params: { storeId, date: date || undefined } })
  },

  // 新增訂位時段（依分店桌位數產生 reservation_capacity）
  createTimeSlot(payload) {
    return http.post('/admin/reservation-settings/time-slots', payload)
  },

  // 修改訂位時段的日期、時間 or 是否開放訂位
  updateTimeSlot(slotId, payload) {
    return http.put(`/admin/reservation-settings/time-slots/${slotId}`, payload)
  },

  // 刪除訂位時段
  deleteTimeSlot(slotId) {
    return http.delete(`/admin/reservation-settings/time-slots/${slotId}`)
  },

  // 重新計算某時段容量
  rebuildCapacity(slotId) {
    return http.post(`/admin/reservation-settings/time-slots/${slotId}/rebuild-capacity`)
  },

  // 查個時段剩餘可訂數
  getCapacity(slotId) {
    return http.get(`/admin/reservation-settings/time-slots/${slotId}/capacity`)
  },
}
