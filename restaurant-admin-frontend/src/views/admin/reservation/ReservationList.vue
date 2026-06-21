<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { reservationAdminApi, reservationApi } from '@/api/reservation'
import { storeApi } from '@/api/store'

const route = useRoute()

const stores = ref([])
const reservations = ref([])
const loading = ref(false)
const errorMessage = ref('')
const selectedStoreId = ref('')
const showDateRangeDropdown = ref(false)
const showStatusDropdown = ref(false)
const calendarMonth = ref(new Date(new Date().getFullYear(), new Date().getMonth(), 1))
const editingReservationId = ref(null)
const selectedStatuses = ref([])
const pageSizeOptions = [5, 10, 15, 20, 25, 30]
const pageSize = ref(10)
const currentPage = ref(1)
const filters = reactive({
  name: '',
  startDate: formatDateInput(new Date()),
  endDate: '',
  time: '',
  partySize: '',
  phone: '',
})

// 編輯使用獨立表單
const editForm = reactive({
  customerName: '',
  customerPhone: '',
  customerEmail: '',
  partySize: 1,
})

const statusText = {
  PENDING: '未配桌',
  RESERVED: '已保留',
  ASSIGNED: '已配桌',
  CHECKED_IN: '已入座',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  NO_SHOW: '未到',
}

const statusClass = {
  PENDING: 'bg-label-danger',
  RESERVED: 'bg-label-warning',
  ASSIGNED: 'bg-label-info',
  CHECKED_IN: 'bg-label-success',
  COMPLETED: 'bg-label-primary',
  CANCELLED: 'bg-label-secondary',
  NO_SHOW: 'bg-label-dark',
}

const statusOptions = [
  { value: 'PENDING', label: '未配桌' },
  { value: 'RESERVED', label: '已保留' },
  { value: 'ASSIGNED', label: '已配桌' },
  { value: 'CHECKED_IN', label: '已入座' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' },
  { value: 'NO_SHOW', label: '未到' },
]

const tableText = (item) => item.tableNumbers?.length ? item.tableNumbers.join('、') : '未分配'
const formatTime = (time) => time?.slice(0, 5) || ''

// 搜尋手機忽略格式 "-" 也可以搜尋
const normalizePhone = (phone) => String(phone || '').replace(/\D/g, '')
const timeLabel = (item) => `${formatTime(item.startTime)} - ${formatTime(item.endTime)}`
const dateLabel = (date) => {
  if (!date) return ''
  const [, month, day] = date.split('-')
  return `${Number(month)}/${Number(day)}`
}

// 不能編輯的狀態
const canEdit = (item) => ['PENDING', 'RESERVED', 'ASSIGNED'].includes(item.status)

// 日期格式：用本地時間 yyyy-MM-dd
function formatDateInput(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 日期選取區器：可選起日、區間
const selectedDateRangeText = computed(() => {
  if (!filters.startDate && !filters.endDate) return '全部日期區間'
  if (filters.startDate && filters.endDate) return `${filters.startDate} ~ ${filters.endDate}`
  if (filters.startDate) return `${filters.startDate} 起`
  return `${filters.endDate} 前`
})

// 查詢日期區間日曆：固定 42 格，保持下拉高度
const calendarTitle = computed(() => {
  const year = calendarMonth.value.getFullYear()
  const month = String(calendarMonth.value.getMonth() + 1).padStart(2, '0')
  return `${year} / ${month}`
})
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

// 時段下拉選項 -> 訂位資料產生
const timeOptions = computed(() => {
  const options = new Set(reservations.value.map((item) => timeLabel(item)))
  return [...options].sort()
})

// 人數下拉選項 -> 訂位資料產生
const partySizeOptions = computed(() => {
  const options = new Set(reservations.value.map((item) => item.partySize))
  return [...options].sort((a, b) => a - b)
})

// 多選狀態篩選按鈕
const selectedStatusText = computed(() => {
  if (!selectedStatuses.value.length || selectedStatuses.value.length === statusOptions.length) return '全部狀態'
  return statusOptions
    .filter((option) => selectedStatuses.value.includes(option.value))
    .map((option) => option.label)
    .join('、')
})

// 訂位名單所有查詢條件篩選
const filteredReservations = computed(() => reservations.value.filter((item) => {
  const matchName = !filters.name || item.customerName?.includes(filters.name)
  const matchDate = !filters.startDate
    ? true
    : filters.endDate
      ? item.reservationDate >= filters.startDate && item.reservationDate <= filters.endDate
      : item.reservationDate >= filters.startDate
  const matchTime = !filters.time || timeLabel(item) === filters.time
  const matchPartySize = !filters.partySize || item.partySize === Number(filters.partySize)
  const matchPhone = !filters.phone || normalizePhone(item.customerPhone).includes(normalizePhone(filters.phone))
  const matchStatus = !selectedStatuses.value.length || selectedStatuses.value.includes(item.status)
  return matchName && matchDate && matchTime && matchPartySize && matchPhone && matchStatus
}))

// 訂位名單：查詢結果依「訂位日期 + 開始時間」排序
const sortedFilteredReservations = computed(() => {
  return [...filteredReservations.value].sort((a, b) => {
    return `${a.reservationDate} ${a.startTime}`.localeCompare(`${b.reservationDate} ${b.startTime}`)
  })
})

const totalPages = computed(() => Math.max(1, Math.ceil(sortedFilteredReservations.value.length / pageSize.value)))
const pageStart = computed(() => (currentPage.value - 1) * pageSize.value)
const pagedReservations = computed(() => sortedFilteredReservations.value.slice(pageStart.value, pageStart.value + pageSize.value))

// 訂位名單：先依日期分組，日期底下再依時段分組；同一天只顯示一次日期列
const groupedReservations = computed(() => {
  const dateGroups = new Map()
  pagedReservations.value.forEach((item) => {
    if (!dateGroups.has(item.reservationDate)) dateGroups.set(item.reservationDate, new Map())
    const timeGroups = dateGroups.get(item.reservationDate)
    const time = timeLabel(item)
    if (!timeGroups.has(time)) timeGroups.set(time, [])
    timeGroups.get(time).push(item)
  })
  return [...dateGroups.entries()].map(([date, timeGroups]) => ({
    date,
    dateLabel: dateLabel(date),
    slots: [...timeGroups.entries()].map(([time, items]) => ({
      label: `${date}-${time}`,
      timeLabel: time,
      items,
    })),
  }))
})

const paginationText = computed(() => {
  if (!sortedFilteredReservations.value.length) return '顯示 0 筆，共 0 筆'
  const start = pageStart.value + 1
  const end = Math.min(pageStart.value + pageSize.value, sortedFilteredReservations.value.length)
  return `顯示 ${start}-${end} 筆，共 ${sortedFilteredReservations.value.length} 筆`
})

const prevPage = () => {
  currentPage.value = Math.max(1, currentPage.value - 1)
}

const nextPage = () => {
  currentPage.value = Math.min(totalPages.value, currentPage.value + 1)
}

// 切換日曆月份
const shiftCalendarMonth = (offset) => {
  calendarMonth.value = new Date(calendarMonth.value.getFullYear(), calendarMonth.value.getMonth() + offset, 1)
}

// 日期區間選取
const selectDateRangeDay = (date) => {
  if (!filters.startDate || (filters.startDate && filters.endDate)) {
    filters.startDate = date
    filters.endDate = ''
    return
  }
  if (date < filters.startDate) {
    filters.endDate = filters.startDate
    filters.startDate = date
  } else {
    filters.endDate = date
  }
  showDateRangeDropdown.value = false
}

// 清除日期區間，回到全部日期
const clearDateRange = () => {
  filters.startDate = ''
  filters.endDate = ''
}

const isRangeStart = (date) => date === filters.startDate
const isRangeEnd = (date) => date === filters.endDate
const isInRange = (date) => filters.startDate && filters.endDate && date > filters.startDate && date < filters.endDate

// 切換多選狀態篩選
const toggleStatus = (status) => {
  if (selectedStatuses.value.includes(status)) {
    selectedStatuses.value = selectedStatuses.value.filter((item) => item !== status)
  } else {
    selectedStatuses.value = [...selectedStatuses.value, status]
  }
}

// 點空白處關閉下拉選單
const handleOutsideClick = (event) => {
  if (event.target.closest('.dropdown-closable')) return
  showDateRangeDropdown.value = false
  showStatusDropdown.value = false
}

// ＊載入可管理分店後再載入訂位資料。＊
const loadStores = async () => {
  try {
    const res = await storeApi.getStores({ admin: true })
    stores.value = res.data || []
    await loadReservations()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '讀取分店資料失敗，請確認後端已啟動'
  }
}

// 載入全部訂位名單
const loadReservations = async () => {
  if (!selectedStoreId.value && !stores.value.length) return
  loading.value = true
  errorMessage.value = ''
  try {
    if (selectedStoreId.value) {
      const res = await reservationAdminApi.getReservations(selectedStoreId.value)
      reservations.value = res.data || []
      return
    }

    const results = await Promise.all(stores.value.map((store) => reservationAdminApi.getReservations(store.storeId)))
    reservations.value = results
      .flatMap((res) => res.data || [])
      .sort((a, b) => `${a.reservationDate} ${a.startTime}`.localeCompare(`${b.reservationDate} ${b.startTime}`))
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '讀取訂位名單失敗'
  } finally {
    loading.value = false
  }
}

// 取消訂位後 -> 重新載入列表、狀態立即更新
const cancelReservation = async (reservationId) => {
  await reservationApi.cancelReservation(reservationId)
  await loadReservations()
}

// 進入編輯模式 -> 筆資料複製到 editForm。
const startEdit = (item) => {
  editingReservationId.value = item.reservationId
  editForm.customerName = item.customerName || ''
  editForm.customerPhone = item.customerPhone || ''
  editForm.customerEmail = item.customerEmail || ''
  editForm.partySize = item.partySize || 1
}

// 離開編輯模式，不送出資料。
const cancelEdit = () => {
  editingReservationId.value = null
}

// 儲存編輯資料
const saveEdit = async (item) => {
  await reservationAdminApi.updateReservationInfo(item.reservationId, {
    storeId: item.storeId,
    slotId: item.slotId,
    customerName: editForm.customerName,
    customerPhone: editForm.customerPhone,
    customerEmail: editForm.customerEmail,
    partySize: Number(editForm.partySize),
    specialRequest: item.specialRequest,
  })
  editingReservationId.value = null
  await loadReservations()
}

// ＊切換分店時重查；頁面掛載時也會讀取 query 參數設定預設篩選。＊
watch(selectedStoreId, loadReservations)
watch([filteredReservations, pageSize], () => {
  currentPage.value = 1
})
watch(totalPages, () => {
  if (currentPage.value > totalPages.value) currentPage.value = totalPages.value
})
onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
  if (route.query.storeId) {
    selectedStoreId.value = String(route.query.storeId)
  }
  if (route.query.status) {
    const status = String(route.query.status) === 'ACTIVE' ? 'RESERVED' : String(route.query.status)
    selectedStatuses.value = statusOptions.some((option) => option.value === status) ? [status] : []
  }
  if (route.query.time) {
    filters.time = String(route.query.time)
  }
  if (route.query.partySize) {
    filters.partySize = String(route.query.partySize)
  }
  loadStores()
})
onBeforeUnmount(() => {
  document.removeEventListener('click', handleOutsideClick)
})
</script>

<template>
  <div class="container-xxl flex-grow-1 container-p-y">
    <h2 class="py-3 mb-4">訂位查詢<span class="text-muted fw-light"> / Reservation Search</span></h2>

    <div class="content-wrapper">
      <div class="card">
        <div class="card-header d-flex flex-wrap align-items-center gap-3">
          <h5 class="mb-0"><i class="bx bx-search"></i> Search 查詢訂位</h5>
          <select v-model="selectedStoreId" class="form-select ms-auto store-select">
            <option value="">全部可管理分店</option>
            <option v-for="store in stores" :key="store.storeId" :value="String(store.storeId)">
              {{ store.storeName }}
            </option>
          </select>
        </div>

        <!-- 查詢訂位 -->
        <div class="card-body">
          <div class="row g-3">
            <div class="col-12 col-sm-6 col-lg-4">
              <label class="form-label">姓名 Name</label>
              <input v-model.trim="filters.name" type="text" class="form-control" />
            </div>
            <div class="col-12 col-sm-6 col-lg-4">
              <label class="form-label">日期區間 Date</label>
              <div class="multi-select dropdown-closable" @click.stop>
                <button type="button" class="form-select text-start" @click="showDateRangeDropdown = !showDateRangeDropdown">
                  {{ selectedDateRangeText }}
                </button>
                <div v-if="showDateRangeDropdown" class="multi-select-menu date-range-menu">
                  <div class="calendar-header">
                    <button type="button" class="btn btn-sm btn-label-secondary" @click="shiftCalendarMonth(-1)">
                      <i class="bx bx-chevron-left"></i>
                    </button>
                    <strong>{{ calendarTitle }}</strong>
                    <button type="button" class="btn btn-sm btn-label-secondary" @click="shiftCalendarMonth(1)">
                      <i class="bx bx-chevron-right"></i>
                    </button>
                  </div>
                  <div class="calendar-grid calendar-weekdays">
                    <span>日</span>
                    <span>一</span>
                    <span>二</span>
                    <span>三</span>
                    <span>四</span>
                    <span>五</span>
                    <span>六</span>
                  </div>
                  <div class="calendar-grid">
                    <button
                      v-for="day in calendarDays"
                      :key="day.value"
                      type="button"
                      class="calendar-day"
                      :class="{
                        'is-muted': !day.currentMonth,
                        'is-selected': isRangeStart(day.value) || isRangeEnd(day.value),
                        'is-in-range': isInRange(day.value),
                      }"
                      @click="selectDateRangeDay(day.value)">
                      {{ day.day }}
                    </button>
                  </div>
                  <div class="date-range-hint">
                    {{ filters.startDate || '請選起日' }} <span>~</span> {{ filters.endDate || '可只選起日' }}
                  </div>
                  <div class="d-flex justify-content-end gap-2 mt-3">
                    <button type="button" class="btn btn-sm btn-label-secondary" @click="clearDateRange">清除</button>
                    <button type="button" class="btn btn-sm btn-primary" @click="showDateRangeDropdown = false">關閉</button>
                  </div>
                </div>
              </div>
            </div>
            <div class="col-12 col-sm-6 col-lg-4">
              <label class="form-label">時段 Time</label>
              <select v-model="filters.time" class="form-select">
                <option value="">全部</option>
                <option v-for="time in timeOptions" :key="time" :value="time">
                  {{ time }}
                </option>
              </select>
            </div>
            <div class="col-12 col-sm-6 col-lg-4">
              <label class="form-label">手機 Phone</label>
              <input v-model.trim="filters.phone" type="text" class="form-control" />
            </div>
            <div class="col-12 col-sm-6 col-lg-4">
              <label class="form-label">人數 Persons</label>
              <select v-model="filters.partySize" class="form-select">
                <option value="">全部</option>
                <option v-for="size in partySizeOptions" :key="size" :value="String(size)">
                  {{ size }} 位
                </option>
              </select>
            </div>
            <div class="col-12 col-sm-6 col-lg-4">
              <label class="form-label">訂位狀態 Status</label>
              <div class="multi-select dropdown-closable" @click.stop>
                <button type="button" class="form-select text-start" @click="showStatusDropdown = !showStatusDropdown">
                  {{ selectedStatusText }}
                </button>
                <div v-if="showStatusDropdown" class="multi-select-menu status-select-menu">
                  <label v-for="option in statusOptions" :key="option.value" class="multi-select-option">
                    <input
                      type="checkbox"
                      class="form-check-input"
                      :checked="selectedStatuses.includes(option.value)"
                      @change="toggleStatus(option.value)"/>
                    <span>{{ option.label }}</span>
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 訂位名單 -->
        <hr class="mt-0" />
        <div v-if="errorMessage" class="alert alert-danger mx-4">{{ errorMessage }}</div>
        <div v-if="loading" class="alert alert-info mx-4">讀取訂位名單中...</div>

        <div class="pagination-toolbar px-4 pb-3">
          <div class="d-flex align-items-center gap-2">
            <span class="text-muted small">每頁顯示</span>
            <select v-model.number="pageSize" class="form-select form-select-sm page-size-select">
              <option v-for="size in pageSizeOptions" :key="size" :value="size">{{ size }} 筆</option>
            </select>
          </div>
          <div class="d-flex align-items-center gap-3">
            <span class="text-muted small">{{ paginationText }}</span>
            <div class="btn-group">
              <button type="button" class="btn btn-sm btn-secondary" :disabled="currentPage <= 1" @click="prevPage">
                上一頁
              </button>
              <button type="button" class="btn btn-sm btn-label-secondary" disabled>
                {{ currentPage }} / {{ totalPages }}
              </button>
              <button type="button" class="btn btn-sm btn-secondary" :disabled="currentPage >= totalPages" @click="nextPage">
                下一頁
              </button>
            </div>
          </div>
        </div>

        <div class="card-datatable table-responsive">
          <table class="dt-advanced-search table border-top">
            <template v-if="!loading && groupedReservations.length">
              <template v-for="dateGroup in groupedReservations" :key="dateGroup.date">
                <tbody class="reservation-date-header">
                  <tr>
                    <td colspan="8">
                      <i class="bx bx-calendar"></i>&nbsp;
                      {{ dateGroup.dateLabel }}</td>
                  </tr>
                </tbody>
                <template v-for="slot in dateGroup.slots" :key="slot.label">
                  <tbody class="reservation-group-header">
                    <tr>
                      <th>{{ slot.timeLabel }}&nbsp; |</th>
                      <th>Phone</th>
                      <th>Email</th>
                      <th>Persons</th>
                      <th>Table</th>
                      <th>Status</th>
                      <th>note</th>
                      <th></th>
                    </tr>
                  </tbody>
                  <tbody class="table-border-bottom-0">
                    <tr v-for="item in slot.items" :key="item.reservationId">
                    <td>
                      <input
                        v-if="editingReservationId === item.reservationId"
                        v-model.trim="editForm.customerName"
                        type="text"
                        class="form-control form-control-sm"/>
                      <span v-else class="fw-medium">{{ item.customerName }}</span>
                    </td>
                    <td>
                      <input
                        v-if="editingReservationId === item.reservationId"
                        v-model.trim="editForm.customerPhone"
                        type="text"
                        class="form-control form-control-sm"/>
                      <span v-else>{{ item.customerPhone }}</span>
                    </td>
                    <td>
                      <input
                        v-if="editingReservationId === item.reservationId"
                        v-model.trim="editForm.customerEmail"
                        type="email"
                        class="form-control form-control-sm"/>
                      <span v-else>{{ item.customerEmail || '-' }}</span>
                    </td>
                    <td>
                      <select
                        v-if="editingReservationId === item.reservationId"
                        v-model.number="editForm.partySize"
                        class="form-select form-select-sm">
                        <option v-for="size in 12" :key="size" :value="size">{{ size }} 位</option>
                      </select>
                      <span v-else>{{ item.partySize }} 位</span>
                    </td>
                    <td>{{ tableText(item) }}</td>
                    <td><span class="badge me-1" :class="statusClass[item.status]">{{ statusText[item.status] || item.status }}</span></td>
                    <td class="special-request-cell">{{ item.specialRequest || '-' }}</td>
                    <td>
                      <div class="d-flex gap-2">
                        <template v-if="editingReservationId === item.reservationId">
                          <button type="button" class="btn btn-sm btn-primary" @click="saveEdit(item)">儲存</button>
                          <button type="button" class="btn btn-sm btn-label-secondary" @click="cancelEdit">取消</button>
                        </template>
                        <template v-else>
                          <button
                            v-if="canEdit(item)"
                            type="button"
                            class="btn btn-sm btn-label-primary"
                            @click="startEdit(item)">
                            編輯
                          </button>
                          <button
                            v-if="!['CHECKED_IN', 'COMPLETED', 'CANCELLED'].includes(item.status)"
                            type="button"
                            class="btn btn-sm btn-label-danger"
                            @click="cancelReservation(item.reservationId)">
                            取消
                          </button>
                        </template>
                      </div>
                    </td>
                    </tr>
                  </tbody>
                </template>
              </template>
            </template>
            <tbody v-else-if="!loading">
              <tr>
                <td colspan="8" class="text-center text-muted py-4">目前沒有符合條件的訂位</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.store-select {
  max-width: 243px;
}

.special-request-cell {
  max-width: 220px;
  white-space: normal;
  word-break: break-word;
  color: #697a8d;
  font-size: 0.875rem;
}

.status-select-menu {
  min-width: 190px;
}

</style>
