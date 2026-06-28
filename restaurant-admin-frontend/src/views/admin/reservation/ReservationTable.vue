<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { reservationAdminApi } from '@/api/reservation'
import {
  buildStoreNameLookup,
  canUseAllManagedStores,
  dateRangeText,
  formatDateInput,
  formatTime,
  groupReservationsByDateAndTime,
  isInRangeFor,
  isRangeEndFor,
  isRangeStartFor,
  isSameReservationDateTime,
  matchesDateRange,
  nextDateRangeSelection,
  resolveManagedStoreSelection,
  sortByReservationDateTime,
  storeDisplayName,
  useMonthCalendar,
  usePagination,
} from '@/assets/js/reservationUi'

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
const pageSizeOptions = [5, 10, 15, 20, 25, 30]
const pageSize = ref(10)
const currentPage = ref(1)
const canSelectAllStores = computed(() => canUseAllManagedStores())
const fixedStoreName = computed(() => stores.value[0]?.storeName || '尚無可管理分店')
const currentStoreLabel = computed(() => {
  if (!selectedStoreId.value) return canSelectAllStores.value ? '全部分店' : fixedStoreName.value
  return stores.value.find((store) => String(store.storeId) === String(selectedStoreId.value))?.storeName || fixedStoreName.value
})
const showStoreColumn = computed(() => canSelectAllStores.value)
const storeNameById = computed(() => buildStoreNameLookup(stores.value))
const storeName = (storeId) => storeDisplayName(storeNameById.value, storeId)

const statusOptions = [
  { value: 'PENDING', label: '未配桌' },
  { value: 'RESERVED', label: '已保留' },
  { value: 'ASSIGNED', label: '已配桌' },
]

// 日期選取區器：可選起日、區間
const selectedDateRangeText = computed(() => {
  return dateRangeText(selectedStartDate.value, selectedEndDate.value)
})

// 查詢日期區間日曆
const { calendarTitle, calendarDays, shiftCalendarMonth } = useMonthCalendar(calendarMonth)

// 依日期區間、多選狀態篩選
const filteredReservations = computed(() => reservations.value.filter((item) => {
  const matchDate = matchesDateRange(item.reservationDate, selectedStartDate.value, selectedEndDate.value)
  const matchStatus = selectedStatuses.value.length === 0 || selectedStatuses.value.includes(item.status)
  return matchDate && matchStatus
}))

// 桌位分配名單：依「訂位日期 + 開始時間」排序，由近到遠排列
const sortedFilteredReservations = computed(() => {
  return sortByReservationDateTime(filteredReservations.value)
})

const {
  totalPages,
  pagedItems: pagedReservations,
  paginationText,
  prevPage,
  nextPage,
  clampPage,
} = usePagination(sortedFilteredReservations, pageSize, currentPage)

// 桌位分配名單：先依日期分組，日期底下再依時段分組；同一天只顯示一次日期列
const groupedReservations = computed(() => {
  return groupReservationsByDateAndTime(pagedReservations.value)
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

const tableText = (item) => item.tableNumbers?.length ? item.tableNumbers.join('、') : '未分配'

// 同一日期時段已經被其他訂位選走的桌位，不再出現在下拉選單
const usedTableIdsForReservation = (item) => {
  return new Set(reservations.value
    .filter((reservation) => reservation.reservationId !== item.reservationId)
    .filter((reservation) => String(reservation.storeId) === String(item.storeId))
    .filter((reservation) => reservation.status !== 'CANCELLED')
    .filter((reservation) => isSameReservationDateTime(reservation, item))
    .flatMap((reservation) => reservation.tableIds || [])
    .map(String))
}

// 只列出該訂位分店的桌位，並扣掉同時段其他已使用桌位
const tablesForReservation = (item) => {
  const usedTableIds = usedTableIdsForReservation(item)
  return tables.value.filter((table) => {
    return String(table.storeId) === String(item.storeId)
      && !usedTableIds.has(String(table.tableId))
  })
}

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

// 載入分店後，讀取桌位與訂位
const loadStores = async () => {
  try {
    const res = await reservationAdminApi.getManageableStores()
    stores.value = res.data || []
    selectedStoreId.value = resolveManagedStoreSelection(stores.value, selectedStoreId.value, canSelectAllStores.value)
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
        reservationAdminApi.getStoreTables(selectedStoreId.value),
        reservationAdminApi.getUnassignedReservations(selectedStoreId.value),
      ])
      tables.value = (tableRes.data || []).map((table) => ({ ...table, storeId: Number(selectedStoreId.value) }))
      reservations.value = reservationRes.data || []
      return
    }

    const results = await Promise.all(stores.value.map(async (store) => {
      const [tableRes, reservationRes] = await Promise.all([
        reservationAdminApi.getStoreTables(store.storeId),
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

// 日期區間選取
const selectDateRangeDay = (date) => {
  const nextRange = nextDateRangeSelection(date, selectedStartDate.value, selectedEndDate.value)
  selectedStartDate.value = nextRange.startDate
  selectedEndDate.value = nextRange.endDate
  if (nextRange.completed) showDateRangeDropdown.value = false
}

// 清空日期區間，顯示全部日期。
const clearDateRange = () => {
  selectedStartDate.value = ''
  selectedEndDate.value = ''
}

const isRangeStart = (date) => isRangeStartFor(date, selectedStartDate.value)
const isRangeEnd = (date) => isRangeEndFor(date, selectedEndDate.value)
const isInRange = (date) => isInRangeFor(date, selectedStartDate.value, selectedEndDate.value)

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
watch([filteredReservations, pageSize], () => {
  currentPage.value = 1
})
watch(totalPages, () => {
  clampPage()
})

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
    <header class="admin-ops-header">
      <div>
        <span>RESERVATION OPS</span>
        <h1>桌位分配</h1>
        <p>管理未配桌、已保留與已配桌訂位。</p>
      </div>
      <div class="admin-current-store">
        <small>store</small>
        <select v-if="canSelectAllStores || stores.length > 1" v-model="selectedStoreId" class="form-select">
          <option v-if="canSelectAllStores" value="">全部可管理分店</option>
          <option v-for="store in stores" :key="store.storeId" :value="String(store.storeId)">
            {{ store.storeName }}
          </option>
        </select>
        <strong v-else>{{ currentStoreLabel }}</strong>
      </div>
    </header>

    <div class="content-wrapper">
      <div class="card">
        <!-- 查詢篩選 -->
        <div class="card-header d-flex flex-wrap align-items-center gap-2">
          <h5 class="mb-0">桌位分配名單<span class="text-muted fs-5 fw-normal"> / Table List </span></h5>
          <div class="multi-select dropdown-closable date-select ms-auto" @click.stop>
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

        <div class="table-responsive text-nowrap">
          <table class="table">
            <template v-if="!loading && filteredReservations.length">
              <template v-for="dateGroup in groupedReservations" :key="dateGroup.date">
                <tbody class="reservation-date-header">
                  <tr>
                    <td :colspan="showStoreColumn ? 8 : 7">
                      <i class="bx bx-calendar"></i>&nbsp;
                      {{ dateGroup.dateLabel }}</td>
                  </tr>
                </tbody>
                <template v-for="slot in dateGroup.slots" :key="slot.label">
                  <tbody class="reservation-group-header">
                    <tr>
                      <th>{{ slot.timeLabel }}&nbsp; |</th>
                      <th v-if="showStoreColumn">Store</th>
                      <th>Phone</th>
                      <th>Email</th>
                      <th>Persons</th>
                      <th>Status</th>
                      <th>Table</th>
                      <th></th>
                    </tr>
                  </tbody>
                  <tbody class="table-border-bottom-0">
                    <tr v-for="item in slot.items" :key="item.reservationId">
                      <td><span class="fw-medium">{{ item.customerName }}</span></td>
                      <td v-if="showStoreColumn">{{ storeName(item.storeId) }}</td>
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
                          class="btn btn-sm btn-outline-dark"
                          @click="startReassign(item)">
                          重新配桌
                        </button>
                        <button v-else type="button" class="btn btn-sm btn-dark" @click="assignTable(item.reservationId)">
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
            </template>
            <tbody v-else-if="!loading">
              <tr>
                <td :colspan="showStoreColumn ? 8 : 7" class="text-center text-muted py-4">目前沒有符合條件的桌位分配資料</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.date-select {
  width: 260px;
  max-width: 100%;
}

.status-select {
  width: 190px;
  max-width: 100%;
}

.status-select-menu {
  width: 190px;
  min-width: 190px;
}
</style>
