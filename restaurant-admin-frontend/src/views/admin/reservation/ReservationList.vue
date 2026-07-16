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
  matchesDateRange,
  nextDateRangeSelection,
  normalizePhone,
  reservationTimeLabel,
  reservationStatusClass,
  reservationStatusText,
  resolveManagedStoreSelection,
  sortByReservationDateTime,
  storeDisplayName,
  useMonthCalendar,
  usePagination,
} from '@/assets/js/reservationUi'

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
const canSelectAllStores = computed(() => canUseAllManagedStores())
const fixedStoreName = computed(() => stores.value[0]?.storeName || '尚無可管理分店')
const currentStoreLabel = computed(() => {
  if (!selectedStoreId.value) return canSelectAllStores.value ? '全部分店' : fixedStoreName.value
  return stores.value.find((store) => String(store.storeId) === String(selectedStoreId.value))?.storeName || fixedStoreName.value
})
const showStoreColumn = computed(() => canSelectAllStores.value)
const storeNameById = computed(() => buildStoreNameLookup(stores.value))
const storeName = (storeId) => storeDisplayName(storeNameById.value, storeId)

// 編輯使用獨立表單
const editForm = reactive({
  customerName: '',
  customerPhone: '',
  customerEmail: '',
  partySize: 1,
})

const statusText = reservationStatusText
const statusClass = reservationStatusClass

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
const timeLabel = reservationTimeLabel
const depositText = (item) => {
  const amount = Number(item.depositAmount || 0)
  if (amount <= 0) return '-'
  return `${item.paymentStatus === 'PAID' ? '已支付' : '未付款'} $${amount.toLocaleString()}`
}
const depositClass = (item) => item.paymentStatus === 'PAID' ? 'bg-label-success' : 'bg-label-warning'

// 不能編輯的狀態
const canEdit = (item) => ['PENDING', 'RESERVED', 'ASSIGNED'].includes(item.status)

// 日期選取區器：可選起日、區間
const selectedDateRangeText = computed(() => {
  return dateRangeText(filters.startDate, filters.endDate)
})

// 查詢日期區間日曆：固定 42 格，保持下拉高度
const { calendarTitle, calendarDays, shiftCalendarMonth } = useMonthCalendar(calendarMonth)

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
  const matchDate = matchesDateRange(item.reservationDate, filters.startDate, filters.endDate)
  const matchTime = !filters.time || timeLabel(item) === filters.time
  const matchPartySize = !filters.partySize || item.partySize === Number(filters.partySize)
  const matchPhone = !filters.phone || normalizePhone(item.customerPhone).includes(normalizePhone(filters.phone))
  const matchStatus = !selectedStatuses.value.length || selectedStatuses.value.includes(item.status)
  return matchName && matchDate && matchTime && matchPartySize && matchPhone && matchStatus
}))

// 訂位名單：查詢結果依「訂位日期 + 開始時間」排序
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

// 訂位名單：先依日期分組，日期底下再依時段分組；同一天只顯示一次日期列
const groupedReservations = computed(() => {
  return groupReservationsByDateAndTime(pagedReservations.value, timeLabel)
})

// 日期區間選取
const selectDateRangeDay = (date) => {
  const nextRange = nextDateRangeSelection(date, filters.startDate, filters.endDate)
  filters.startDate = nextRange.startDate
  filters.endDate = nextRange.endDate
  if (nextRange.completed) showDateRangeDropdown.value = false
}

// 清除日期區間，回到全部日期
const clearDateRange = () => {
  filters.startDate = ''
  filters.endDate = ''
}

const isRangeStart = (date) => isRangeStartFor(date, filters.startDate)
const isRangeEnd = (date) => isRangeEndFor(date, filters.endDate)
const isInRange = (date) => isInRangeFor(date, filters.startDate, filters.endDate)

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

// 載入可管理分店後，再載入訂位資料
const loadStores = async () => {
  try {
    const res = await reservationAdminApi.getManageableStores()
    stores.value = res.data || []
    selectedStoreId.value = resolveManagedStoreSelection(stores.value, selectedStoreId.value, canSelectAllStores.value)
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
  await reservationAdminApi.cancelReservation(reservationId)
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
  clampPage()
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
    <header class="admin-ops-header">
      <div>
        <span>RESERVATION OPS</span>
        <h1>訂位查詢</h1>
        <p>查詢所有訂位資料，依日期、時段、狀態與顧客資訊篩選。</p>
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
                    <td :colspan="showStoreColumn ? 10 : 9">
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
                      <th>Table</th>
                      <th>Status</th>
                      <th>Deposit</th>
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
                    <td v-if="showStoreColumn">{{ storeName(item.storeId) }}</td>
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
                      <span v-if="Number(item.depositAmount || 0) > 0" class="badge" :class="depositClass(item)">
                        {{ depositText(item) }}
                      </span>
                      <span v-else class="text-muted">-</span>
                    </td>
                    <td class="special-request-cell">{{ item.specialRequest || '-' }}</td>
                    <td>
                      <div class="d-flex">
                        <template v-if="editingReservationId === item.reservationId">
                          <button type="button" class="btn btn-sm btn-outline-success" @click="saveEdit(item)"><i class="bx bx-check"></i></button>
                          <button type="button" class="btn btn-sm btn-outline-danger" @click="cancelEdit"><i class="bx bx-x"></i></button>
                        </template>
                        <template v-else>
                          <button
                            v-if="canEdit(item)"
                            type="button"
                            class="btn btn-sm"
                            @click="startEdit(item)">
                            <i class="bx bx-edit-alt"></i>
                          </button>
                          <button
                            v-if="!['CHECKED_IN', 'COMPLETED', 'CANCELLED'].includes(item.status)"
                            type="button"
                            class="btn btn-sm"
                            @click="cancelReservation(item.reservationId)">
                            <i class="bx bx-trash"></i>
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
                <td :colspan="showStoreColumn ? 10 : 9" class="text-center text-muted py-4">目前沒有符合條件的訂位</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
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
