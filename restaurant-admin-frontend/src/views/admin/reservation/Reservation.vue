<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { reservationAdminApi } from '@/api/reservation'
import { storeApi } from '@/api/store'

const stores = ref([])
const tables = ref([])
const allReservations = ref([])
const overview = ref({
  totalCount: 0,
  assignedCount: 0,
  unassignedCount: 0,
  checkedInCount: 0,
  reservations: [],
})
const selectedStoreId = ref('')
const selectedTables = reactive({})
const reassigningReservations = reactive({})
const openTimeGroups = reactive({})
const filters = reactive({
  name: '',
  phone: '',
})
const loading = ref(false)
const errorMessage = ref('')
const rangeOptions = [
  { label: '今天', value: 1 },
  { label: '近三日', value: 3 },
  { label: '近五日', value: 5 },
  { label: '近一週', value: 7 },
  { label: '近一個月', value: 30 },
]
const selectedRangeDays = ref(1)

// 日期格式：用本地時間 yyyy-MM-dd，避免 toISOString() 造成 UTC 時區問題(非現在時間)
function formatDateInput(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
const selectedDate = ref(formatDateInput(new Date()))

// 依選到的天數值，轉換標題顯示文字
const selectedRangeLabel = computed(() => {
  return rangeOptions.find((option) => option.value === selectedRangeDays.value)?.label || '近三日'
})

// 依下拉選單取得指定範圍日期
const overviewDates = computed(() => {
  const startDate = new Date(`${selectedDate.value}T00:00:00`)
  return Array.from({ length: selectedRangeDays.value }, (_, index) => {
    const date = new Date(startDate)
    date.setDate(startDate.getDate() + index)
    return formatDateInput(date)
  })
})

// 訂位總覽：全部訂位數量統計
const allStats = computed(() => {
  const confirmedStatuses = ['RESERVED', 'ASSIGNED']
  const activeReservations = allReservations.value.filter(isCountableReservation)
  return {
    totalCount: activeReservations.length,
    reservedCount: activeReservations.filter((item) => confirmedStatuses.includes(item.status)).length,
    unassignedCount: activeReservations.filter((item) => ['PENDING', 'RESERVED'].includes(item.status) && !item.tableIds?.length).length,
  }
})

// 訂位總覽：今日訂位數量統計
const todayStats = computed(() => {
  const reservations = (overview.value.reservations || []).filter(
    (item) => item.reservationDate === selectedDate.value && isCountableReservation(item),
  )
  return {
    totalCount: reservations.length,
    reservedCount: reservations.filter((item) => ['RESERVED', 'ASSIGNED'].includes(item.status)).length,
    unassignedCount: reservations.filter((item) => ['PENDING', 'RESERVED'].includes(item.status) && !item.tableIds?.length).length,
    checkedInCount: reservations.filter((item) => item.status === 'CHECKED_IN').length,
  }
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

// 訂位狀態樣式
const statusClass = {
  PENDING: 'bg-label-danger',
  RESERVED: 'bg-label-warning',
  ASSIGNED: 'bg-label-info',
  CHECKED_IN: 'bg-label-success',
  COMPLETED: 'bg-label-primary',
  CANCELLED: 'bg-label-secondary',
  NO_SHOW: 'bg-label-dark',
}

const formatTime = (time) => time?.slice(0, 5) || ''

// 搜尋手機忽略格式 "-" 也可以搜尋
const normalizePhone = (phone) => String(phone || '').replace(/\D/g, '')

// 判斷訂位是否已超過時段；排除在「接下來訂位總數」
const isPastReservationTime = (item) => {
  if (!item.reservationDate || !item.endTime) return false
  return new Date(`${item.reservationDate}T${formatTime(item.endTime)}:00`).getTime() < Date.now()
}

// 訂位總量只算接下來的訂位（已取消、未到、已入座/完成、已過時間都不算）
const isCountableReservation = (item) => {
  if (['CANCELLED', 'NO_SHOW', 'CHECKED_IN', 'COMPLETED'].includes(item.status)) return false
  if (isPastReservationTime(item)) return false
  return true
}

const isVisibleOverviewReservation = (item) => !['CANCELLED', 'NO_SHOW'].includes(item.status)

// 日期顯示星期(訂位總覽名單)
const weekdayLabel = (date) => {
  if (!date) return ''
  return ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][new Date(`${date}T00:00:00`).getDay()]
}

// 訂位總覽名單分組
const groupedReservations = computed(() => {
  const dateGroups = new Map()
  overviewDates.value.forEach((date) => {
    dateGroups.set(date, new Map())
  })

  ;(overview.value.reservations || [])
    .filter(isVisibleOverviewReservation)
    .filter((item) => {
      const matchName = !filters.name || item.customerName?.includes(filters.name)
      const matchPhone = !filters.phone || normalizePhone(item.customerPhone).includes(normalizePhone(filters.phone))
      return matchName && matchPhone
    })
    .forEach((item) => {
      const timeLabel = `${formatTime(item.startTime)} - ${formatTime(item.endTime)}`
      if (!dateGroups.has(item.reservationDate)) dateGroups.set(item.reservationDate, new Map())

      const timeGroups = dateGroups.get(item.reservationDate)
      if (!timeGroups.has(timeLabel)) timeGroups.set(timeLabel, [])
      timeGroups.get(timeLabel).push(item)
    })
  return [...dateGroups.entries()].map(([date, timeGroups]) => {
    const slots = [...timeGroups.entries()]
      .map(([timeLabel, items]) => ({
        label: `${date}｜${timeLabel}`,
        timeLabel,
        items,
      }))
      .sort((a, b) => a.timeLabel.localeCompare(b.timeLabel))
    return {
      date,
      totalCount: slots.reduce((sum, slot) => sum + slot.items.length, 0),
      slots,
    }
  })
})

// ＊載入後台可管理分店；目前未做權限時先允許全部，未來可由登入身分限制 stores。＊
const loadStores = async () => {
  try {
    const res = await storeApi.getStores({ admin: true })
    stores.value = res.data || []
    await loadDashboard()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '讀取分店資料失敗，請確認後端已啟動'
  }
}

// 載入指定日期範圍的訂位名單總覽
const loadOverview = async () => {
  if (!selectedStoreId.value && !stores.value.length) return
  loading.value = true
  errorMessage.value = ''
  try {
    if (selectedStoreId.value) {
      const [overviewResults, tableRes] = await Promise.all([
        Promise.all(overviewDates.value.map((date) => reservationAdminApi.getDailyOverview(selectedStoreId.value, date))),
        storeApi.getStoreTables(selectedStoreId.value),
      ])
      const reservations = overviewResults
        .flatMap((result) => result.data?.reservations || [])
        .filter(isVisibleOverviewReservation)
      const countableReservations = reservations.filter(isCountableReservation)
      overview.value = {
        totalCount: countableReservations.length,
        assignedCount: countableReservations.filter((item) => item.status === 'ASSIGNED').length,
        unassignedCount: countableReservations.filter((item) => ['PENDING', 'RESERVED'].includes(item.status) && !item.tableIds?.length).length,
        checkedInCount: reservations.filter((item) => item.status === 'CHECKED_IN').length,
        reservations: reservations.sort((a, b) => `${a.reservationDate} ${a.startTime}`.localeCompare(`${b.reservationDate} ${b.startTime}`)),
      }
      tables.value = (tableRes.data || []).map((table) => ({ ...table, storeId: Number(selectedStoreId.value) }))
    } else {
      const results = await Promise.all(stores.value.map(async (store) => {
        const [overviewResults, tableRes] = await Promise.all([
          Promise.all(overviewDates.value.map((date) => reservationAdminApi.getDailyOverview(store.storeId, date))),
          storeApi.getStoreTables(store.storeId),
        ])
        return {
          overviews: overviewResults.map((result) => result.data || {}),
          tables: (tableRes.data || []).map((table) => ({ ...table, storeId: store.storeId })),
        }
      }))
      const reservations = results
        .flatMap((result) => result.overviews.flatMap((daily) => daily.reservations || []))
        .filter(isVisibleOverviewReservation)
      const countableReservations = reservations.filter(isCountableReservation)
      overview.value = {
        totalCount: countableReservations.length,
        assignedCount: countableReservations.filter((item) => item.status === 'ASSIGNED').length,
        unassignedCount: countableReservations.filter((item) => ['PENDING', 'RESERVED'].includes(item.status) && !item.tableIds?.length).length,
        checkedInCount: reservations.filter((item) => item.status === 'CHECKED_IN').length,
        reservations: reservations.sort((a, b) => `${a.reservationDate} ${a.startTime}`.localeCompare(`${b.reservationDate} ${b.startTime}`)),
      }
      tables.value = results.flatMap((result) => result.tables)
    }
    groupedReservations.value.forEach((dateGroup) => {
      dateGroup.slots.forEach((slot) => {
        if (openTimeGroups[slot.label] === undefined) openTimeGroups[slot.label] = true
      })
    })
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '讀取訂位總覽失敗'
  } finally {
    loading.value = false
  }
}

// 載入全部訂位名單
const loadAllReservations = async () => {
  if (!selectedStoreId.value && !stores.value.length) return
  try {
    if (selectedStoreId.value) {
      const res = await reservationAdminApi.getReservations(selectedStoreId.value)
      allReservations.value = res.data || []
      return
    }

    const results = await Promise.all(stores.value.map((store) => reservationAdminApi.getReservations(store.storeId)))
    allReservations.value = results.flatMap((res) => res.data || [])
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '讀取全部訂位統計失敗'
  }
}

// 同時刷新保持畫面最新
const loadDashboard = async () => {
  await Promise.all([loadOverview(), loadAllReservations()])
}

// 儲存桌位分配
const assignTable = async (reservationId) => {
  const tableId = selectedTables[reservationId]
  if (!tableId) {
    errorMessage.value = '請先選擇桌位'
    return
  }
  await reservationAdminApi.assignTables({ reservationId, tableIds: [Number(tableId)] })
  selectedTables[reservationId] = ''
  reassigningReservations[reservationId] = false
  await loadDashboard()
}

// 店家端直接保留訂位，PENDING 改 RESERVED。
const reserveReservation = async (item) => {
  if (!window.confirm(`確認保留 ${item.customerName} 的訂位？`)) return
  await reservationAdminApi.reserve(item.reservationId)
  await loadDashboard()
}

// 勾選實際入座
const checkIn = async (item) => {
  if (item.status === 'CHECKED_IN') return
  if (!window.confirm(`確認 ${item.customerName} 已實際入座？確認後不能再更改桌位、編輯或刪除。`)) {
    await loadDashboard()
    return
  }
  await reservationAdminApi.checkIn(item.reservationId)
  await loadDashboard()
}

// 訂位名單：沒有桌位 -> 顯示未分配
const tableText = (item) => item.tableNumbers?.length ? item.tableNumbers.join('、') : '未分配'

// 同一日期時段已經被其他訂位選走的桌位，不再出現在下拉選單
const isSameDateTimeSlot = (source, target) => {
  return source.reservationDate === target.reservationDate
    && formatTime(source.startTime) === formatTime(target.startTime)
    && formatTime(source.endTime) === formatTime(target.endTime)
}

const usedTableIdsForReservation = (item) => {
  return new Set((overview.value.reservations || [])
    .filter((reservation) => reservation.reservationId !== item.reservationId)
    .filter((reservation) => String(reservation.storeId) === String(item.storeId))
    .filter(isVisibleOverviewReservation)
    .filter((reservation) => isSameDateTimeSlot(reservation, item))
    .flatMap((reservation) => reservation.tableIds || [])
    .map(String))
}

// ＊只顯示該訂位分店底下的桌位，並扣掉同時段其他訂位已使用桌位。＊
const tablesForReservation = (item) => {
  const usedTableIds = usedTableIdsForReservation(item)
  return tables.value.filter((table) => {
    return String(table.storeId) === String(item.storeId)
      && !usedTableIds.has(String(table.tableId))
  })
}

// 已入座、已完成、已取消、未到不能再編輯、配桌
const canEdit = (item) => !['CHECKED_IN', 'COMPLETED', 'CANCELLED', 'NO_SHOW'].includes(item.status)
const canAssignTable = (item) => ['PENDING', 'RESERVED', 'ASSIGNED'].includes(item.status)

// 已配桌後 -> 只顯示桌號 ; 按重新配桌後 -> 打開下拉選單
const canSelectTable = (item) => item.status !== 'ASSIGNED' || reassigningReservations[item.reservationId]

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

// 訂位總覽名單：時段列表收合
const toggleTimeGroup = (timeLabel) => {
  openTimeGroups[timeLabel] = !openTimeGroups[timeLabel]
}

// 切換分店 or 日期時 -> 重新載入整個總覽
watch([selectedStoreId, selectedDate, selectedRangeDays], loadDashboard)
onMounted(loadStores)
</script>

<template>
  <div class="content-wrapper">
    <div class="container-xxl flex-grow-1 container-p-y">
      <div class="d-flex flex-wrap align-items-center gap-3 py-3 mb-4">
        <h2 class="mb-0">訂位管理總覽<span class="text-muted fw-light"> / Reservation</span></h2>
        <select v-model="selectedStoreId" class="form-select ms-auto control-select">
          <option value="">全部可管理分店</option>
          <option v-for="store in stores" :key="store.storeId" :value="String(store.storeId)">
            {{ store.storeName }}
          </option>
        </select>
      </div>

      <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
      <div v-if="loading" class="alert alert-info">讀取訂位總覽中...</div>

      <div class="row">
        <!-- 全部訂位數統計 card -->
        <div class="col-sm-6 col-lg-4 mb-4">
          <RouterLink
            class="card dashboard-card card-border-shadow-primary h-100 text-reset text-decoration-none"
            :to="{ name: 'AdminReservationList', query: { storeId: selectedStoreId } }"
          >
            <div class="card-body">
              <div class="d-flex align-items-center mb-2 pb-1">
                <div class="avatar me-2"><span class="avatar-initial rounded bg-label-primary"><i class="bx bx-calendar bx-sm"></i></span></div>
                <h3 class="ms-1 mb-0">{{ allStats.totalCount }}</h3>
              </div>
              <p class="mb-0">訂位總數</p>
            </div>
          </RouterLink>
        </div>
        <div class="col-sm-6 col-lg-4 mb-4">
          <RouterLink
            class="card dashboard-card card-border-shadow-warning h-100 text-reset text-decoration-none"
            :to="{ name: 'AdminReservationList', query: { storeId: selectedStoreId, status: 'RESERVED' } }"
          >
            <div class="card-body">
              <div class="d-flex align-items-center mb-2 pb-1">
                <div class="avatar me-2"><span class="avatar-initial rounded bg-label-warning"><i class="bx bx-check-double"></i></span></div>
                <h3 class="ms-1 mb-0">{{ allStats.reservedCount }}</h3>
              </div>
              <p class="mb-0">所有已確定保留訂位</p>
            </div>
          </RouterLink>
        </div>
        <div class="col-sm-6 col-lg-4 mb-4">
          <RouterLink
            class="card dashboard-card card-border-shadow-danger h-100 text-reset text-decoration-none"
            :to="{ name: 'AdminReservationTable', query: { storeId: selectedStoreId } }"
          >
            <div class="card-body">
              <div class="d-flex align-items-center mb-2 pb-1">
                <div class="avatar me-2"><span class="avatar-initial rounded bg-label-danger"><i class="bx bx-error"></i></span></div>
                <h3 class="ms-1 mb-0">{{ allStats.unassignedCount }}</h3>
              </div>
              <p class="mb-0">所有未配桌</p>
            </div>
          </RouterLink>
        </div>
      </div>

      <div class="card mb-4">
        <!-- 今日訂位數統計 card -->
        <div class="card-widget-separator-wrapper">
          <div class="card-body card-widget-separator">
            <div class="row gy-4 gy-sm-1">
              <div class="col-sm-6 col-lg-3">
                <div class="d-flex justify-content-between align-items-start card-widget-1 border-end pb-3 pb-sm-0">
                  <div>
                    <h3 class="mb-2">{{ todayStats.totalCount }}</h3>
                    <p class="mb-0">今日訂位</p>
                  </div>
                  <div class="avatar me-sm-4">
                    <span class="avatar-initial rounded bg-label-primary">
                      <i class="bx bx-calendar bx-sm"></i>
                    </span>
                  </div>
                </div>
                <hr class="d-none d-sm-block d-lg-none me-4" />
              </div>
              <div class="col-sm-6 col-lg-3">
                <div class="d-flex justify-content-between align-items-start card-widget-2 border-end pb-3 pb-sm-0">
                  <div>
                    <h3 class="mb-2">{{ todayStats.reservedCount }}</h3>
                    <p class="mb-0">今日保留</p>
                  </div>
                  <div class="avatar me-lg-4">
                    <span class="avatar-initial rounded bg-label-warning">
                      <i class="bx bx-check-double bx-sm"></i>
                    </span>
                  </div>
                </div>
                <hr class="d-none d-sm-block d-lg-none" />
              </div>
              <div class="col-sm-6 col-lg-3">
                <div class="d-flex justify-content-between align-items-start border-end pb-3 pb-sm-0 card-widget-3">
                  <div>
                    <h3 class="mb-2">{{ todayStats.unassignedCount }}</h3>
                    <p class="mb-0">今日未配桌</p>
                  </div>
                  <div class="avatar me-sm-4">
                    <span class="avatar-initial rounded bg-label-danger">
                      <i class="bx bx-error bx-sm"></i>
                    </span>
                  </div>
                </div>
              </div>
              <div class="col-sm-6 col-lg-3">
                <div class="d-flex justify-content-between align-items-start">
                  <div>
                    <h3 class="mb-2">{{ todayStats.checkedInCount }}</h3>
                    <p class="mb-0">今日入座</p>
                  </div>
                  <div class="avatar">
                    <span class="avatar-initial rounded bg-label-success">
                      <i class="bx bx-user-check bx-sm"></i>
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <!-- 指定範圍訂位名單 -->
        <div class="card-header d-flex flex-wrap align-items-center gap-3">
          <h5 class="mb-0">{{ selectedRangeLabel }}訂位名單</h5>
          <select v-model.number="selectedRangeDays" class="form-select range-select">
            <option v-for="option in rangeOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
          <input v-model.trim="filters.name" type="text" class="form-control ms-auto search-control" placeholder="搜尋姓名" />
          <input v-model.trim="filters.phone" type="text" class="form-control search-control" placeholder="搜尋手機" />
        </div>
        <div class="three-day-reservation-list">
          <div v-if="!loading && groupedReservations.length === 0" class="text-center text-muted py-4">{{ selectedRangeLabel }}沒有訂位資料</div>
          <div v-for="group in groupedReservations" :key="group.date" class="time-slot-row">
            <div class="time-slot-date">
              <div class="fw-semibold">{{ group.date }}</div>
              <div class="text-muted small">{{ weekdayLabel(group.date) }}</div>
              <span class="badge bg-label-primary">{{ group.totalCount }} 筆訂位</span>
            </div>
            <div class="time-slot-items">
              <div v-if="group.slots.length === 0" class="text-muted small empty-date-message">這一天沒有訂位</div>
              <div v-for="slot in group.slots" :key="slot.label" class="time-slot-item">
                <div class="time-slot-item-summary">
                  <button type="button" class="btn btn-sm btn-label-secondary slot-toggle" @click="toggleTimeGroup(slot.label)">
                    <i :class="openTimeGroups[slot.label] ? 'bx bx-chevron-down' : 'bx bx-chevron-right'"></i>
                  </button>
                  <div class="time-slot-item-main">
                    <div class="d-flex align-items-center flex-wrap gap-2">
                      <span class="fw-semibold">{{ slot.timeLabel }}</span>
                      <span class="badge bg-label-secondary">{{ slot.items.length }} 筆訂位</span>
                    </div>
                  </div>
                </div>
                <div v-if="openTimeGroups[slot.label]" class="three-day-reservation-items">
                  <div v-for="item in slot.items" :key="item.reservationId" class="three-day-reservation-row" :class="{ 'is-locked': !canEdit(item) }">
                    <div class="three-day-guest">
                      <input
                        type="checkbox"
                        class="form-check-input"
                        :checked="item.status === 'CHECKED_IN'"
                        :disabled="item.status === 'CHECKED_IN' || item.status === 'CANCELLED' || item.status === 'NO_SHOW' || item.status === 'PENDING'"
                        @change="checkIn(item)"/>
                      <div>
                        <div class="fw-semibold">{{ item.customerName }}</div>
                        <div class="text-muted small">{{ item.customerPhone }}</div>
                      </div>
                    </div>
                    <div>{{ item.partySize }} 位</div>
                    <div>
                      <span class="badge me-1" :class="statusClass[item.status]">{{ statusText[item.status] || item.status }}</span>
                    </div>
                    <div class="three-day-table-control">
                      <span v-if="!canEdit(item) || !canAssignTable(item) || !canSelectTable(item)">{{ tableText(item) }}</span>
                      <select v-else v-model="selectedTables[item.reservationId]" class="form-select">
                        <option value="">{{ tableText(item) }}</option>
                        <option v-for="table in tablesForReservation(item)" :key="table.tableId" :value="String(table.tableId)">
                          {{ table.tableNumber }}（{{ table.tableSize }}人桌）
                        </option>
                      </select>
                    </div>
                    <div class="three-day-actions">
                      <button v-if="item.status === 'PENDING'" type="button" class="btn btn-sm btn-label-warning" @click="reserveReservation(item)">
                        確認保留
                      </button>
                      <button v-if="canEdit(item) && canAssignTable(item) && canSelectTable(item)" type="button" class="btn btn-sm btn-primary" @click="assignTable(item.reservationId)">
                        分配
                      </button>
                      <button v-if="canEdit(item) && item.status === 'ASSIGNED' && canSelectTable(item)" type="button" class="btn btn-sm btn-label-secondary" @click="cancelReassign(item.reservationId)">
                        取消
                      </button>
                      <button v-if="canEdit(item) && item.status === 'ASSIGNED' && !canSelectTable(item)" type="button" class="btn btn-sm btn-label-primary" @click="startReassign(item)">
                        重新配桌
                      </button>
                    </div>
                    <div v-if="item.specialRequest" class="special-request">
                      備註：{{ item.specialRequest }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.control-select {
  max-width: 250px;
}

.search-control {
  max-width: 220px;
}

.range-select {
  max-width: 150px;
}

.dashboard-card {
  display: block;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.dashboard-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.08);
}

/* 訂位範圍名單 */

.three-day-reservation-list {
  border-top: 1px solid #e6e8eb;
}

.time-slot-row {
  display: grid;
  grid-template-columns: minmax(150px, 190px) minmax(0, 1fr);
  gap: 1rem;
  padding: 1rem;
  border-top: 1px solid #e6e8eb;
}

.time-slot-row:first-child {
  border-top: 0;
}

.time-slot-date {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.35rem;
  margin-left: 10px;
}

.empty-date-message {
  padding: 0.75rem;
  border: 1px dashed #e6e8eb;
  border-radius: 0.5rem;
  background: #fff;
}

.time-slot-items {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.time-slot-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 0.75rem;
  padding: 0.75rem;
  border: 1px solid #edf0f2;
  border-radius: 0.5rem;
  background: #fff;
}

.time-slot-item-summary {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  min-width: 0;
}

.time-slot-item-main {
  min-width: 0;
}

.slot-toggle {
  width: 2rem;
  height: 2rem;
  padding: 0;
  flex: 0 0 auto;
}

.three-day-reservation-items {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 0.75rem;
  border-radius: 0.5rem;
  background: #f8f9fb;
}

.three-day-reservation-row {
  display: grid;
  grid-template-columns: minmax(180px, 1.2fr) minmax(70px, 0.35fr) minmax(90px, 0.45fr) minmax(180px, 1fr) auto;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  border: 1px solid #edf0f2;
  border-radius: 0.375rem;
  background: #fff;
}

.three-day-reservation-row.is-locked {
  background: #f5f5f9;
  color: #8b98a7;
}

.three-day-guest {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  min-width: 0;
}

.three-day-table-control {
  min-width: 180px;
}

.three-day-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.special-request {
  grid-column: 1 / -1;
  color: #8b98a7;
  font-size: 0.8125rem;
  line-height: 1.5;
  word-break: break-word;
}

@media (max-width: 992px) {
  .time-slot-row,
  .three-day-reservation-row {
    grid-template-columns: 1fr;
  }

  .today-actions {
    justify-content: flex-start;
  }
}
</style>
