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
const calendarMonth = ref(new Date(new Date().getFullYear(), new Date().getMonth(), 1))
const editingReservationId = ref(null)
const filters = reactive({
  name: '',
  startDate: '',
  endDate: '',
  time: '',
  partySize: '',
  phone: '',
  status: '',
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

const tableText = (item) => item.tableNumbers?.length ? item.tableNumbers.join('、') : '未分配'
const formatTime = (time) => time?.slice(0, 5) || ''

// 搜尋手機忽略格式 "-" 也可以搜尋
const normalizePhone = (phone) => String(phone || '').replace(/\D/g, '')
const timeLabel = (item) => `${formatTime(item.startTime)} - ${formatTime(item.endTime)}`
const groupLabel = (item) => `${item.reservationDate}｜${timeLabel(item)}`

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
  const matchStatus = !filters.status || item.status === filters.status
  return matchName && matchDate && matchTime && matchPartySize && matchPhone && matchStatus
}))

// 訂位名單：排序讓列表以日期時間近～遠顯示
const sortedFilteredReservations = computed(() => {
  return [...filteredReservations.value].sort((a, b) => {
    return `${a.reservationDate} ${a.startTime}`.localeCompare(`${b.reservationDate} ${b.startTime}`)
  })
})

// 訂位名單：依「日期 + 時段」的標題分組
const groupedReservations = computed(() => {
  const groups = new Map()
  sortedFilteredReservations.value.forEach((item) => {
    const key = groupLabel(item)
    if (!groups.has(key)) groups.set(key, [])
    groups.get(key).push(item)
  })
  return [...groups.entries()].map(([label, items]) => ({ label, items }))
})

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

// 點空白處關閉下拉選單
const handleOutsideClick = (event) => {
  if (event.target.closest('.dropdown-closable')) return
  showDateRangeDropdown.value = false
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
onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
  if (route.query.storeId) {
    selectedStoreId.value = String(route.query.storeId)
  }
  if (route.query.status) {
    filters.status = String(route.query.status) === 'ACTIVE' ? 'RESERVED' : String(route.query.status)
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
    <h2 class="py-3 mb-4">訂位名單<span class="text-muted fw-light"> / Reservation List</span></h2>

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
              <select v-model="filters.status" class="form-select">
                <option value="">全部</option>
                <option value="PENDING">未配桌</option>
                <option value="RESERVED">已保留</option>
                <option value="ASSIGNED">已配桌</option>
                <option value="CHECKED_IN">已入座</option>
                <option value="CANCELLED">已取消</option>
              </select>
            </div>
          </div>
        </div>

        <!-- 訂位名單 -->
        <hr class="mt-0" />
        <div v-if="errorMessage" class="alert alert-danger mx-4">{{ errorMessage }}</div>
        <div v-if="loading" class="alert alert-info mx-4">讀取訂位名單中...</div>

        <div class="card-datatable table-responsive">
          <table class="dt-advanced-search table border-top">
            <template v-if="!loading && groupedReservations.length">
              <template v-for="group in groupedReservations" :key="group.label">
                <thead class="table-dark reservation-group-header">
                  <tr>
                    <th>{{ group.label }}</th>
                    <th>Phone</th>
                    <th>Email</th>
                    <th>Persons</th>
                    <th>Table</th>
                    <th>Status</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody class="table-border-bottom-0">
                  <tr v-for="item in group.items" :key="item.reservationId">
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
            <tbody v-else-if="!loading">
              <tr>
                <td colspan="7" class="text-center text-muted py-4">目前沒有符合條件的訂位</td>
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

.multi-select {
  position: relative;
}

.multi-select-menu {
  position: absolute;
  z-index: 1090;
  top: calc(100% + 4px);
  left: 0;
  width: 100%;
  min-width: 300px;
  padding: 0.5rem;
  background: #fff;
  border: 1px solid #d9dee3;
  border-radius: 0.375rem;
  box-shadow: 0 0.25rem 1rem rgba(67, 89, 113, 0.12);
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 0.25rem;
}

.calendar-weekdays {
  color: #697a8d;
  font-size: 0.75rem;
  text-align: center;
  margin-bottom: 0.25rem;
}

.calendar-day {
  width: 100%;
  aspect-ratio: 1;
  border: 0;
  border-radius: 0.375rem;
  background: transparent;
  color: #566a7f;
}

.calendar-day:hover {
  background: #f5f5f9;
}

.calendar-day.is-muted {
  color: #b4bdc6;
}

.calendar-day.is-in-range {
  background: #e7e7ff;
  color: #696cff;
}

.calendar-day.is-selected {
  background: #696cff;
  color: #fff;
}

.date-range-hint {
  margin-top: 0.75rem;
  padding: 0.5rem 0.75rem;
  border-radius: 0.375rem;
  background: #f5f5f9;
  color: #566a7f;
  text-align: center;
}

.reservation-group-header th {
  color: #fff !important;
}

</style>
