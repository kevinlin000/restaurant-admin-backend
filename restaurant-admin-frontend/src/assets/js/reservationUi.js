import { computed } from 'vue'

// 從 localStorage 讀取登入者資料
// 登入成功 userInfo 存在 localStorage，判斷角色與分店選單顯示
export function getCurrentUserInfo() {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || '{}')
  } catch (error) {
    localStorage.removeItem('userInfo')
    return {}
  }
}

// 取得目前登入者角色名稱（ADMIN、MANAGER、STAFF、CUSTOMER）
export const getCurrentRoleName = () => getCurrentUserInfo()?.roleName || ''

// 只有 ADMIN 可以管理全部分店
export const canUseAllManagedStores = () => getCurrentRoleName() === 'ADMIN'

// 訂位日期時段、開放天數 -> 只有MANAGER店長可以設定權限
export const canManageReservationSettings = () => ['MANAGER', 'ADMIN'].includes(getCurrentRoleName())

// 後台訂位狀態顯示文字，訂位總覽、訂位名單共用
export const reservationStatusText = {
  PENDING: '未配桌',
  RESERVED: '已保留',
  ASSIGNED: '已配桌',
  CHECKED_IN: '已入座',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  NO_SHOW: '未到',
}

// 後台訂位狀態樣式
export const reservationStatusClass = {
  PENDING: 'bg-label-danger',
  RESERVED: 'bg-label-warning',
  ASSIGNED: 'bg-label-info',
  CHECKED_IN: 'bg-label-success',
  COMPLETED: 'bg-label-primary',
  CANCELLED: 'bg-label-secondary',
  NO_SHOW: 'bg-label-dark',
}

// 後台分店選單共用預設值：
export function resolveManagedStoreSelection(stores, currentStoreId, canUseAllStores = canUseAllManagedStores()) {
  const storeList = stores || []
  const currentValue = currentStoreId == null ? '' : String(currentStoreId)
  const hasCurrentStore = storeList.some((store) => String(store.storeId) === currentValue)

  if (canUseAllStores && (!currentValue || hasCurrentStore)) return currentValue
  if (hasCurrentStore) return currentValue
  return storeList[0]?.storeId ? String(storeList[0].storeId) : ''
}

// ＊後端有些 API 回傳 data，有些回傳 { stores }，用同一個方法整理成陣列＊
export function extractStoreList(payload) {
  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload?.stores)) return payload.stores
  if (Array.isArray(payload?.data)) return payload.data
  return []
}

// ＊分店詳情 API 可能回傳 store 或直接回傳物件＊
export const extractStoreDetail = (payload) => payload?.store || payload || null

// 產生 storeId -> storeName 對照表（ADMIN 頁面顯示分店欄位）
export const buildStoreNameLookup = (stores) => (stores || []).reduce((lookup, store) => {
  lookup[String(store.storeId)] = store.storeName
  return lookup
}, {})

export const storeDisplayName = (lookup, storeId) => lookup[String(storeId)] || `分店 ${storeId}`

// 用本地時間輸出 yyyy-MM-dd，避免 toISOString() 因 UTC 時差讓日期少一天
export function formatDateInput(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 時間顯示轉換(HH:mm:ss) -> 只要時、分(HH:mm)
export const formatTime = (time) => time?.slice(0, 5) || ''

// 查詢電話時忽略 "-"，讓 0900-000-000 和 0900000000 都能查到
export const normalizePhone = (phone) => String(phone || '').replace(/\D/g, '')

// 手機輸入自動格式化 0900-000-000。
export const formatPhoneNumber = (value) => {
  const digits = String(value || '').replace(/\D/g, '').slice(0, 10)
  if (digits.length <= 4) return digits
  if (digits.length <= 7) return `${digits.slice(0, 4)}-${digits.slice(4)}`
  return `${digits.slice(0, 4)}-${digits.slice(4, 7)}-${digits.slice(7)}`
}

// 將 yyyy-MM-dd 轉成 M/D 顯示
export const shortDateLabel = (date) => {
  if (!date) return ''
  const [, month, day] = date.split('-')
  return `${Number(month)}/${Number(day)}`
}

// 訂位列表、配桌列表依「訂位日期 + 開始時間」排序
export const sortByReservationDateTime = (items) => {
  return [...items].sort((a, b) => `${a.reservationDate} ${a.startTime}`.localeCompare(`${b.reservationDate} ${b.startTime}`))
}

// 產生時段標題，例如 12:00 - 14:00（先formatTime去掉秒數）
export const reservationTimeLabel = (item) => `${formatTime(item.startTime)} - ${formatTime(item.endTime)}`

// 日期區間篩選
export function matchesDateRange(date, startDate, endDate) {
  if (!startDate) return true
  if (endDate) return date >= startDate && date <= endDate
  return date >= startDate
}

// 訂位同時段，下拉選單扣掉已被分配的桌位。
export function isSameReservationDateTime(source, target) {
  return source.reservationDate === target.reservationDate
    && formatTime(source.startTime) === formatTime(target.startTime)
    && formatTime(source.endTime) === formatTime(target.endTime)
}

// API 錯誤訊息整理，顯示後端 message、error
export const getApiErrorMessage = (error, fallback) => {
  return error?.response?.data?.message
    || error?.response?.data?.error
    || error?.message
    || fallback
}

// ReservationList 和 ReservationTable 群組列表 (日期包含多個時段，每個時段有多筆訂位)
export function groupReservationsByDateAndTime(items, getTimeLabel = reservationTimeLabel) {
  const dateGroups = new Map()
  items.forEach((item) => {
    if (!dateGroups.has(item.reservationDate)) dateGroups.set(item.reservationDate, new Map())
    const timeGroups = dateGroups.get(item.reservationDate)
    const time = getTimeLabel(item)
    if (!timeGroups.has(time)) timeGroups.set(time, [])
    timeGroups.get(time).push(item)
  })
  return [...dateGroups.entries()].map(([date, timeGroups]) => ({
    date,
    dateLabel: shortDateLabel(date),
    slots: [...timeGroups.entries()].map(([time, slotItems]) => ({
      label: `${date}-${time}`,
      timeLabel: time,
      items: slotItems,
    })),
  }))
}

// 日期區間日曆
export function useMonthCalendar(calendarMonth) {
  const calendarTitle = computed(() => {
    const year = calendarMonth.value.getFullYear()
    const month = String(calendarMonth.value.getMonth() + 1).padStart(2, '0')
    return `${year} / ${month}`
  })
  // 固定 42 格，保持下拉高度
  const calendarDays = computed(() => {
    const year = calendarMonth.value.getFullYear()
    const month = calendarMonth.value.getMonth()
    const firstDay = new Date(year, month, 1)
    const start = new Date(firstDay)
    start.setDate(firstDay.getDate() - firstDay.getDay())
    return Array.from({ length: 42 }, (_, index) => {
      const date = new Date(start)
      date.setDate(start.getDate() + index)
      return {
        value: formatDateInput(date),
        day: date.getDate(),
        currentMonth: date.getMonth() === month,
      }
    })
  })
  // 切換月曆月份
  const shiftCalendarMonth = (offset) => {
    calendarMonth.value = new Date(calendarMonth.value.getFullYear(), calendarMonth.value.getMonth() + offset, 1)
  }
  return {
    calendarTitle,
    calendarDays,
    shiftCalendarMonth,
  }
}

// 日期區間選取(可選起日、區間)
export function dateRangeText(startDate, endDate, emptyText = '全部日期區間') {
  if (!startDate && !endDate) return emptyText
  if (startDate && endDate) return `${startDate} ~ ${endDate}`
  if (startDate) return `${startDate} 起`
  return `${endDate} 前`
}

// 日期區間選取（邏輯）
export function nextDateRangeSelection(date, startDate, endDate) {
  if (!startDate || (startDate && endDate)) {
    return { startDate: date, endDate: '', completed: false }
  }
  if (date < startDate) {
    return { startDate: date, endDate: startDate, completed: true }
  }
  return { startDate, endDate: date, completed: true }
}

// 選日期日曆的樣式判斷：選開始日、結束日、區間內日期套不同 CSS
export const isRangeStartFor = (date, startDate) => date === startDate
export const isRangeEndFor = (date, endDate) => date === endDate
export const isInRangeFor = (date, startDate, endDate) => startDate && endDate && date > startDate && date < endDate

// 分頁
export function usePagination(items, pageSize, currentPage) {
  // 至少保留 1 頁
  const totalPages = computed(() => Math.max(1, Math.ceil(items.value.length / pageSize.value)))
  // 目前頁面的第一筆資料
  const pageStart = computed(() => (currentPage.value - 1) * pageSize.value)
  const pagedItems = computed(() => items.value.slice(pageStart.value, pageStart.value + pageSize.value))
  const paginationText = computed(() => {
    if (!items.value.length) return '顯示 0 筆，共 0 筆'
    const start = pageStart.value + 1
    const end = Math.min(pageStart.value + pageSize.value, items.value.length)
    return `顯示 ${start}-${end} 筆，共 ${items.value.length} 筆`
  })
  // 前一頁最小只能到第 1 頁
  const prevPage = () => {
    currentPage.value = Math.max(1, currentPage.value - 1)
  }
  // 後一頁最大只能到 totalPages
  const nextPage = () => {
    currentPage.value = Math.min(totalPages.value, currentPage.value + 1)
  }
  // 當篩選條件改變頁碼回有效範圍內
  const clampPage = () => {
    if (currentPage.value > totalPages.value) currentPage.value = totalPages.value
  }
  return {
    totalPages,
    pageStart,
    pagedItems,
    paginationText,
    prevPage,
    nextPage,
    clampPage,
  }
}
