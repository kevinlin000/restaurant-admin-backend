<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { reservationAdminApi } from '@/api/reservation'
import { storeApi } from '@/api/store'

const route = useRoute()

const stores = ref([])
const tables = ref([])
const reservations = ref([])
const selectedStoreId = ref('')
const selectedStartDate = ref(formatDateInput(new Date()))
const selectedEndDate = ref('')
const selectedStatuses = ref(['PENDING', 'RESERVED', 'ASSIGNED'])
const selectedTables = reactive({})
const reassigningReservations = reactive({})
const calendarMonth = ref(new Date(new Date().getFullYear(), new Date().getMonth(), 1))
const showDateRangeDropdown = ref(false)
const showStatusDropdown = ref(false)
const loading = ref(false)
const errorMessage = ref('')

const statusOptions = [
  { value: 'PENDING', label: '未配桌' },
  { value: 'RESERVED', label: '已保留' },
  { value: 'ASSIGNED', label: '已配桌' },
]

// 日期格式：本地日期，篩選預設今天起
function formatDateInput(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 日期選取區器：可選起日、區間
const selectedDateRangeText = computed(() => {
  if (!selectedStartDate.value && !selectedEndDate.value) return '全部日期區間'
  if (selectedStartDate.value && selectedEndDate.value) return `${selectedStartDate.value} ~ ${selectedEndDate.value}`
  if (selectedStartDate.value) return `${selectedStartDate.value} 起`
  return `${selectedEndDate.value} 前`
})

// 查詢日期區間日曆
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

// 依日期區間、多選狀態篩選
const filteredReservations = computed(() => reservations.value.filter((item) => {
  const matchDate = !selectedStartDate.value
    ? true
    : selectedEndDate.value
      ? item.reservationDate >= selectedStartDate.value && item.reservationDate <= selectedEndDate.value
      : item.reservationDate >= selectedStartDate.value
  const matchStatus = selectedStatuses.value.length === 0 || selectedStatuses.value.includes(item.status)
  return matchDate && matchStatus
}))

// 訂位名單：依「日期 + 時段」的標題分組
const groupedReservations = computed(() => {
  return filteredReservations.value.reduce((groups, item) => {
    const key = `${item.reservationDate}｜${formatTime(item.startTime)} - ${formatTime(item.endTime)}`
    groups[key] = groups[key] || []
    groups[key].push(item)
    return groups
  }, {})
})

const statusText = {
  PENDING: '未配桌',
  RESERVED: '已保留',
  ASSIGNED: '已配桌',
}

const statusClass = {
  PENDING: 'bg-label-danger',
  RESERVED: 'bg-label-warning',
  ASSIGNED: 'bg-label-info',
}

// ＊只列出該訂位分店的桌位，避免跨店分配。＊
const tablesForReservation = (item) => tables.value.filter((table) => String(table.storeId) === String(item.storeId))
const formatTime = (time) => time?.slice(0, 5) || ''
const tableText = (item) => item.tableNumbers?.length ? item.tableNumbers.join('、') : '未分配'

// 已配桌後 -> 只顯示桌號 ; 按重新配桌後 -> 打開下拉選單
const canSelectTable = (item) => item.status !== 'ASSIGNED' || reassigningReservations[item.reservationId]

// 多選狀態篩選按鈕：選取顯示的文字
const selectedStatusText = computed(() => {
  if (selectedStatuses.value.length === statusOptions.length) return '全部狀態'
  if (selectedStatuses.value.length === 0) return '未選狀態'
  return statusOptions
    .filter((option) => selectedStatuses.value.includes(option.value))
    .map((option) => option.label)
    .join('、')
})

// 切換多選狀態篩選按鈕
const toggleStatus = (status) => {
  if (selectedStatuses.value.includes(status)) {
    selectedStatuses.value = selectedStatuses.value.filter((item) => item !== status)
  } else {
    selectedStatuses.value = [...selectedStatuses.value, status]
  }
}

// ＊載入分店後再讀取桌位與訂位。＊
const loadStores = async () => {
  try {
    const res = await storeApi.getStores({ admin: true })
    stores.value = res.data || []
    await loadPageData()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '讀取分店資料失敗，請確認後端已啟動'
  }
}

// 載入配桌頁面資料
const loadPageData = async () => {
  if (!selectedStoreId.value && !stores.value.length) return
  loading.value = true
  errorMessage.value = ''
  try {
    if (selectedStoreId.value) {
      const [tableRes, reservationRes] = await Promise.all([
        storeApi.getStoreTables(selectedStoreId.value),
        reservationAdminApi.getUnassignedReservations(selectedStoreId.value),
      ])
      tables.value = (tableRes.data || []).map((table) => ({ ...table, storeId: Number(selectedStoreId.value) }))
      reservations.value = reservationRes.data || []
      return
    }

    const results = await Promise.all(stores.value.map(async (store) => {
      const [tableRes, reservationRes] = await Promise.all([
        storeApi.getStoreTables(store.storeId),
        reservationAdminApi.getUnassignedReservations(store.storeId),
      ])
      return {
        tables: (tableRes.data || []).map((table) => ({ ...table, storeId: store.storeId })),
        reservations: reservationRes.data || [],
      }
    }))
    tables.value = results.flatMap((result) => result.tables)
    reservations.value = results
      .flatMap((result) => result.reservations)
      .sort((a, b) => `${a.reservationDate} ${a.startTime}`.localeCompare(`${b.reservationDate} ${b.startTime}`))
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '讀取未配桌資料失敗'
  } finally {
    loading.value = false
  }
}

// 切換日曆月份
const shiftCalendarMonth = (offset) => {
  calendarMonth.value = new Date(calendarMonth.value.getFullYear(), calendarMonth.value.getMonth() + offset, 1)
}

// 日期區間選取
const selectDateRangeDay = (date) => {
  if (!selectedStartDate.value || (selectedStartDate.value && selectedEndDate.value)) {
    selectedStartDate.value = date
    selectedEndDate.value = ''
    return
  }
  if (date < selectedStartDate.value) {
    selectedEndDate.value = selectedStartDate.value
    selectedStartDate.value = date
  } else {
    selectedEndDate.value = date
  }
  showDateRangeDropdown.value = false
}

// 清空日期區間，顯示全部日期。
const clearDateRange = () => {
  selectedStartDate.value = ''
  selectedEndDate.value = ''
}

const isRangeStart = (date) => date === selectedStartDate.value
const isRangeEnd = (date) => date === selectedEndDate.value
const isInRange = (date) => selectedStartDate.value && selectedEndDate.value && date > selectedStartDate.value && date < selectedEndDate.value

// 點空白處關閉下拉選單
const handleOutsideClick = (event) => {
  if (event.target.closest('.dropdown-closable')) return
  showDateRangeDropdown.value = false
  showStatusDropdown.value = false
}

// 儲存桌位分配，狀態改成 ASSIGNED
const assignTable = async (reservationId) => {
  const tableId = selectedTables[reservationId]
  if (!tableId) {
    errorMessage.value = '請先選擇桌位'
    return
  }
  await reservationAdminApi.assignTables({
    reservationId,
    tableIds: [Number(tableId)],
  })
  selectedTables[reservationId] = ''
  reassigningReservations[reservationId] = false
  await loadPageData()
}

// 重新配桌
const startReassign = (item) => {
  reassigningReservations[item.reservationId] = true
  selectedTables[item.reservationId] = item.tableIds?.[0] ? String(item.tableIds[0]) : ''
}

// 取消重新配桌，恢復顯示桌號
const cancelReassign = (reservationId) => {
  reassigningReservations[reservationId] = false
  selectedTables[reservationId] = ''
}

// ＊切換分店時重查；掛載時讀取 query 預設分店與狀態。＊
watch(selectedStoreId, loadPageData)

onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
  if (route.query.storeId) {
    selectedStoreId.value = String(route.query.storeId)
  }
  if (route.query.status) {
    const status = String(route.query.status)
    selectedStatuses.value = statusOptions.some((option) => option.value === status) ? [status] : ['PENDING', 'RESERVED', 'ASSIGNED']
  }
  loadStores()
})
onBeforeUnmount(() => {
  document.removeEventListener('click', handleOutsideClick)
})
</script>

<template>
  <div class="container-xxl flex-grow-1 container-p-y">
    <div class="d-flex flex-wrap align-items-center gap-3 py-3 mb-4">
      <h2 class="mb-0">桌位分配<span class="text-muted fw-light"> / Reservation Table</span></h2>
    </div>

    <div class="content-wrapper">
      <div class="card">
        <!-- 查詢篩選 -->
        <div class="card-header d-flex flex-wrap align-items-center gap-2">
          <h5 class="mb-0">桌位分配名單</h5>
          <select v-model="selectedStoreId" class="form-select ms-auto store-select">
            <option value="">全部可管理分店</option>
            <option v-for="store in stores" :key="store.storeId" :value="String(store.storeId)">
              {{ store.storeName }}
            </option>
          </select>
          <div class="multi-select dropdown-closable date-select" @click.stop>
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
                  @click="selectDateRangeDay(day.value)"
                >
                  {{ day.day }}
                </button>
              </div>
              <div class="date-range-hint">
                {{ selectedStartDate || '請選起日' }} <span>~</span> {{ selectedEndDate || '可只選起日' }}
              </div>
              <div class="d-flex justify-content-end gap-2 mt-3">
                <button type="button" class="btn btn-sm btn-label-secondary" @click="clearDateRange">清除</button>
                <button type="button" class="btn btn-sm btn-primary" @click="showDateRangeDropdown = false">關閉</button>
              </div>
            </div>
          </div>
          <div class="multi-select dropdown-closable status-select" @click.stop>
            <button type="button" class="form-select text-start" @click="showStatusDropdown = !showStatusDropdown">
              {{ selectedStatusText }}
            </button>
            <div v-if="showStatusDropdown" class="multi-select-menu status-select-menu">
              <label v-for="option in statusOptions" :key="option.value" class="status-option">
                <input
                  type="checkbox"
                  class="form-check-input"
                  :checked="selectedStatuses.includes(option.value)"
                  @change="toggleStatus(option.value)"
                />
                <span>{{ option.label }}</span>
              </label>
            </div>
          </div>
        </div>
        <div v-if="errorMessage" class="alert alert-danger mx-4">{{ errorMessage }}</div>
        <div v-if="loading" class="alert alert-info mx-4">讀取未配桌名單中...</div>

        <!-- 配桌名單 -->
        <div class="table-responsive text-nowrap">
          <table class="table">
            <template v-if="!loading && filteredReservations.length">
              <template v-for="(items, timeLabel) in groupedReservations" :key="timeLabel">
                <thead class="table-dark reservation-group-header">
                  <tr>
                    <th>{{ timeLabel }}</th>
                    <th>Phone</th>
                    <th>Email</th>
                    <th>Persons</th>
                    <th>Status</th>
                    <th>Table</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody class="table-border-bottom-0">
                  <tr v-for="item in items" :key="item.reservationId">
                    <td><span class="fw-medium">{{ item.customerName }}</span></td>
                    <td>{{ item.customerPhone }}</td>
                    <td>{{ item.customerEmail || '-' }}</td>
                    <td>{{ item.partySize }} 位</td>
                    <td><span class="badge" :class="statusClass[item.status]">{{ statusText[item.status] || item.status }}</span></td>
                    <td>
                      <span v-if="!canSelectTable(item)" class="fw-medium">{{ tableText(item) }}</span>
                      <select v-else v-model="selectedTables[item.reservationId]" class="form-select">
                        <option value="">請選擇</option>
                        <option v-for="table in tablesForReservation(item)" :key="table.tableId" :value="String(table.tableId)">
                          {{ table.tableNumber }}（{{ table.tableSize }}人桌）
                        </option>
                      </select>
                    </td>
                    <td>
                      <button
                        v-if="item.status === 'ASSIGNED' && !reassigningReservations[item.reservationId]"
                        type="button"
                        class="btn btn-sm btn-label-primary"
                        @click="startReassign(item)">
                        重新配桌
                      </button>
                      <button v-else type="button" class="btn btn-sm btn-primary" @click="assignTable(item.reservationId)">
                        分配
                      </button>
                      <button
                        v-if="reassigningReservations[item.reservationId]"
                        type="button"
                        class="btn btn-sm btn-label-secondary ms-2"
                        @click="cancelReassign(item.reservationId)">
                        取消
                      </button>
                    </td>
                  </tr>
                </tbody>
              </template>
            </template>
            <tbody v-else-if="!loading">
              <tr>
                <td class="text-center text-muted py-4">目前沒有符合條件的桌位分配資料</td>
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
  max-width: 200px;
}

.date-select {
  width: 260px;
  max-width: 100%;
}

.status-select {
  width: 190px;
  max-width: 100%;
}

.multi-select {
  position: relative;
}

.multi-select-menu {
  position: absolute;
  z-index: 1090;
  top: calc(100% + 4px);
  right: 0;
  width: 300px;
  padding: 0.5rem;
  background: #fff;
  border: 1px solid #d9dee3;
  border-radius: 0.375rem;
  box-shadow: 0 0.25rem 1rem rgba(67, 89, 113, 0.12);
}

/* 多選狀態下拉選單 */

.status-select-menu {
  width: 190px;
  min-width: 190px;
}

.status-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.45rem 0.5rem;
  margin: 0;
  border-radius: 0.375rem;
  color: #566a7f;
  cursor: pointer;
}

.status-option:hover {
  background: #f5f5f9;
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
