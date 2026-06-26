<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { reservationAdminApi, reservationSettingApi } from '@/api/reservation'
import { storeApi } from '@/api/store'
import {
  buildStoreNameLookup,
  canManageReservationSettings,
  canUseAllManagedStores,
  dateRangeText,
  formatDateInput,
  getApiErrorMessage,
  isInRangeFor,
  isRangeEndFor,
  isRangeStartFor,
  nextDateRangeSelection,
  resolveManagedStoreSelection,
  useMonthCalendar,
  usePagination,
} from '@/assets/js/reservationUi'

const stores = ref([])
const slots = ref([])
const storeHolidays = ref([])
const capacitiesBySlot = reactive({})

const selectedStoreId = ref('')
const selectedStartDate = ref(formatDateInput(new Date()))
const selectedEndDate = ref('')
const selectedWeekdays = ref([])
const selectedStartTime = ref('00:00')
const selectedEndTime = ref('00:00')
const listMode = ref('date')
const pageSizeOptions = [5, 10, 15, 20, 25, 30]
const pageSize = ref(10)
const currentPage = ref(1)
const calendarMonth = ref(new Date(new Date().getFullYear(), new Date().getMonth(), 1))
const reservationOpenDays = ref(30)
const loading = ref(false)
const saving = ref(false)
const updating = ref(false)
const savingOpenDays = ref(false)
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDateRangeDropdown = ref(false)
const showFormDateRangeDropdown = ref(false)
const showCustomDateDropdown = ref(false)
const showEditDateDropdown = ref(false)
const showWeekdayDropdown = ref(false)
const showRuleDropdown = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const createModalError = ref('')
const editModalError = ref('')
const editingSlotId = ref(null)
const editingSlots = ref([])
const openSlotDetails = reactive({})
const canSelectAllStores = computed(() => canUseAllManagedStores())
const canManageSettings = computed(() => canManageReservationSettings())
const fixedStoreName = computed(() => stores.value[0]?.storeName || '尚無可管理分店')
const currentStoreLabel = computed(() => {
  if (!selectedStoreId.value) return canSelectAllStores.value ? '全部分店' : fixedStoreName.value
  return stores.value.find((store) => String(store.storeId) === String(selectedStoreId.value))?.storeName || fixedStoreName.value
})

// 新增時段 modal 表單
const form = reactive({
  storeId: '',
  startDate: '',
  endDate: '',
  ruleSelections: [],
  customDate: '',
  customDates: [],
  startTime: '12:00',
  endTime: '14:00',
  isOpen: true,
  requiresDeposit: false,
  depositAmount: 0,
})

// 修改時段 modal 表單
const editForm = reactive({
  storeId: '',
  reservationDate: '',
  startTime: '',
  endTime: '',
  isOpen: true,
  requiresDeposit: false,
  depositAmount: 0,
})

// 新增時段的日期規則(多選下拉選單)
const ruleOptions = [
  { label: '星期一', value: 'WEEKDAY_1' },
  { label: '星期二', value: 'WEEKDAY_2' },
  { label: '星期三', value: 'WEEKDAY_3' },
  { label: '星期四', value: 'WEEKDAY_4' },
  { label: '星期五', value: 'WEEKDAY_5' },
  { label: '星期六', value: 'WEEKDAY_6' },
  { label: '星期日', value: 'WEEKDAY_7' },
]

// 查詢、顯示的星期選項(多選下拉選單)
const weekdayOptions = [
  { label: '星期一', value: '1' },
  { label: '星期二', value: '2' },
  { label: '星期三', value: '3' },
  { label: '星期四', value: '4' },
  { label: '星期五', value: '5' },
  { label: '星期六', value: '6' },
  { label: '星期日', value: '7' },
]

// 時間選擇器時、分
const hourOptions = Array.from({ length: 24 }, (_, index) => String(index).padStart(2, '0'))
const minuteOptions = Array.from({ length: 60 }, (_, index) => String(index).padStart(2, '0'))

// ＊依目前選擇的分店 ID 找完整分店資料，開放天數設定會使用。＊
const selectedStore = computed(() => stores.value.find((store) => String(store.storeId) === String(selectedStoreId.value)))

// 顯示分店名稱
const storeNameById = computed(() => buildStoreNameLookup(stores.value))

// 日期選取區器：可選起日、區間
const selectedDateRangeText = computed(() => {
  return dateRangeText(selectedStartDate.value, selectedEndDate.value)
})

// 新增 modal 日期區間文字提示
const formDateRangeText = computed(() => {
  if (form.startDate && form.endDate) return `${form.startDate} ~ ${form.endDate}`
  if (form.startDate) return form.startDate
  return '請選擇日期區間'
})
const customDateText = computed(() => form.customDate || '請選擇自訂日期')
const editDateText = computed(() => editForm.reservationDate || '請選擇日期')

// 查詢日期區間日曆
const { calendarTitle, calendarDays, shiftCalendarMonth } = useMonthCalendar(calendarMonth)

// 多選星期篩選按鈕：選取顯示的文字
const selectedWeekdayText = computed(() => {
  if (!selectedWeekdays.value.length) return '全部星期'
  return weekdayOptions
    .filter((weekday) => selectedWeekdays.value.includes(weekday.value))
    .map((weekday) => weekday.label)
    .join('、')
})

// 新增 modal 日期規則文字提示
const selectedRuleText = computed(() => {
  if (!form.ruleSelections.length) return '請選擇日期規則'
  return ruleOptions
    .filter((rule) => form.ruleSelections.includes(rule.value))
    .map((rule) => rule.label)
    .join('、')
})

// 日期轉星期文字
const weekdayLabel = (date) => {
  if (!date) return ''
  const weekday = new Date(`${date}T00:00:00`).getDay() || 7
  return weekdayOptions.find((item) => item.value === String(weekday))?.label || ''
}
const slotDayOfWeek = (slot) => String(slot.dayOfWeek || (new Date(`${slot.reservationDate}T00:00:00`).getDay() || 7))
const slotWeekdayLabel = (slot) => weekdayOptions.find((item) => item.value === slotDayOfWeek(slot))?.label || weekdayLabel(slot.reservationDate)

// 依查詢條件過濾時段資料
const filteredSlots = computed(() => slots.value.filter((slot) => {
  const slotWeekday = slotDayOfWeek(slot)
  const hasStartTimeFilter = selectedStartTime.value && selectedStartTime.value !== '00:00'
  const hasEndTimeFilter = selectedEndTime.value && selectedEndTime.value !== '00:00'
  const matchDateRange = !selectedStartDate.value
    ? true
    : selectedEndDate.value
      ? slot.reservationDate >= selectedStartDate.value && slot.reservationDate <= selectedEndDate.value
      : slot.reservationDate >= selectedStartDate.value
  const matchWeekday = !selectedWeekdays.value.length || selectedWeekdays.value.includes(slotWeekday)
  const matchStart = !hasStartTimeFilter || slot.startTime?.slice(0, 5) >= selectedStartTime.value
  const matchEnd = !hasEndTimeFilter || slot.endTime?.slice(0, 5) <= selectedEndTime.value
  return matchDateRange && matchWeekday && matchStart && matchEnd
}))

// 把多個日期同時段的 slot 合併成「星期列表的顯示資料」
// sourceSlots 保留資料庫中原本的 slot，之後容量明細、統一修改、統一刪除依照他處理原本每筆單日的資料
const buildAggregateSlots = (dateGroups) => {
  const slotsByTime = dateGroups
    .flatMap((group) => group.slots)
    .reduce((items, slot) => {
      const key = `${slot.startTime?.slice(0, 5)}-${slot.endTime?.slice(0, 5)}-${slot.isOpen ? 1 : 0}-${slot.requiresDeposit ? 1 : 0}-${slot.depositAmount || 0}`
      if (!items[key]) {
        items[key] = {
          ...slot,
          aggregateKey: key,
          sourceSlots: [],
          sourceDates: [],
          isAggregate: true,
        }
      }
      items[key].sourceSlots.push(slot)
      if (!items[key].sourceDates.includes(slot.reservationDate)) {
        items[key].sourceDates.push(slot.reservationDate)
      }
      return items
    }, {})

  return Object.values(slotsByTime)
    .map((slot) => ({
      ...slot,
      sourceDates: slot.sourceDates.sort(),
      sourceSlots: slot.sourceSlots.sort((a, b) => `${a.reservationDate} ${a.startTime}`.localeCompare(`${b.reservationDate} ${b.startTime}`)),
    }))
    .sort((a, b) => a.startTime.localeCompare(b.startTime))
}

// 依「日期」分組顯示名單
const dateSlotGroups = computed(() => {
  const groups = filteredSlots.value.reduce((items, slot) => {
    const key = `${slot.storeId}-${slot.reservationDate}`
    if (!items[key]) {
      items[key] = {
        key,
        storeId: slot.storeId,
        reservationDate: slot.reservationDate,
        dayOfWeek: slotDayOfWeek(slot),
        weekday: slotWeekdayLabel(slot),
        slots: [],
      }
    }
    items[key].slots.push(slot)
    return items
  }, {})
  return Object.values(groups)
    .map((group) => ({
      ...group,
      slots: group.slots.sort((a, b) => a.startTime.localeCompare(b.startTime)),
    }))
    .sort((a, b) => {
      const dateOrder = a.reservationDate.localeCompare(b.reservationDate)
      if (dateOrder !== 0) return dateOrder
      return String(a.storeId).localeCompare(String(b.storeId))
    })
})

// 依「星期」分組顯示名單
const weekdaySlotGroups = computed(() => {
  const groupsByWeekday = filteredSlots.value.reduce((items, slot) => {
    if (!slot.ruleGenerated) return items
    const key = `${slot.storeId}-${slotDayOfWeek(slot)}`
    if (!items[key]) {
      items[key] = {
        storeId: slot.storeId,
        dayOfWeek: slotDayOfWeek(slot),
        weekday: slotWeekdayLabel(slot),
        slots: [],
      }
    }
    items[key].slots.push(slot)
    return items
  }, {})

  const weekdayGroups = Object.values(groupsByWeekday).map((group) => {
    const dateGroups = Object.values(group.slots.reduce((items, slot) => {
      if (!items[slot.reservationDate]) {
        items[slot.reservationDate] = {
          key: `${slot.storeId}-${slot.reservationDate}`,
          storeId: slot.storeId,
          reservationDate: slot.reservationDate,
          dayOfWeek: slotDayOfWeek(slot),
          weekday: slotWeekdayLabel(slot),
          slots: [],
        }
      }
      items[slot.reservationDate].slots.push(slot)
      return items
    }, {}))
    return {
      key: `weekday-${group.storeId}-${group.dayOfWeek}`,
      storeId: group.storeId,
      dayOfWeek: group.dayOfWeek,
      weekday: group.weekday,
      reservationDate: '',
      dates: dateGroups.map((dateGroup) => dateGroup.reservationDate).sort(),
      // dateCount: dateGroups.length,
      isWeekdayGroup: true,
      slots: buildAggregateSlots(dateGroups),
    }
  })

  const exceptionGroups = dateSlotGroups.value
    .filter((group) => group.slots.some((slot) => !slot.ruleGenerated))
    .map((group) => ({
      ...group,
      isException: true,
      slots: group.slots.filter((slot) => !slot.ruleGenerated),
    }))

  return [...weekdayGroups, ...exceptionGroups]
    .sort((a, b) => {
      const storeOrder = String(a.storeId).localeCompare(String(b.storeId))
      if (storeOrder !== 0) return storeOrder
      const weekdayOrder = Number(a.dayOfWeek || 0) - Number(b.dayOfWeek || 0)
      if (weekdayOrder !== 0) return weekdayOrder
      return String(a.reservationDate || '').localeCompare(String(b.reservationDate || ''))
    })
})

const {
  totalPages,
  pagedItems: pagedDateSlotGroups,
  paginationText,
  prevPage,
  nextPage,
  clampPage,
} = usePagination(dateSlotGroups, pageSize, currentPage)
const slotGroups = computed(() => listMode.value === 'weekday' ? weekdaySlotGroups.value : pagedDateSlotGroups.value)



// 計算每個桌型的剩餘數
const capacityRows = (slotId) => (capacitiesBySlot[slotId] || [])
  .map((item) => ({
    ...item,
    remainingCount: Math.max(Number(item.totalCount || 0) - Number(item.reservedCount || 0), 0),
  }))
  .sort((a, b) => Number(a.tableSize) - Number(b.tableSize))

// 日期列表： slotId 當時段收合；星期列表：有多個單一 日期 slotId，所以用 aggregateKey
const slotDisplayKey = (slot) => slot.isAggregate ? slot.aggregateKey : slot.slotId
// 判斷修改 modal 是否批次修改星期列表的多筆時段
const isBulkEditing = computed(() => editingSlots.value.length > 1)
const editingDateSummary = computed(() => {
  if (!isBulkEditing.value) return ''
  const dates = editingSlots.value.map((slot) => slot.reservationDate).sort()
  return `${dates[0]} ~ ${dates[dates.length - 1]}，共 ${dates.length} 天`
})

const timeHour = (time) => time?.slice(0, 2) || ''
const timeMinute = (time) => time?.slice(3, 5) || ''

// 查詢時間選擇器分時、分，最後回 HH:mm
const setFilterTimePart = (targetRef, part, value) => {
  if (!value && part === 'hour') {
    targetRef.value = ''
    return
  }
  const hour = part === 'hour' ? value : timeHour(targetRef.value) || '00'
  const minute = part === 'minute' ? value : timeMinute(targetRef.value) || '00'
  targetRef.value = `${hour}:${minute}`
}

// 新增、修改 modal 的時間選擇器，同樣時、分組成 HH:mm
const setFormTimePart = (target, field, part, value) => {
  const hour = part === 'hour' ? value : timeHour(target[field]) || '00'
  const minute = part === 'minute' ? value : timeMinute(target[field]) || '00'
  target[field] = `${hour}:${minute}`
}

// 控制某個時段容量明細的收合
const toggleSlotDetails = (slotId) => {
  openSlotDetails[slotId] = !openSlotDetails[slotId]
}

// 依新增 modal 的規則 ->轉換成實際日期
const generatedDates = computed(() => {
  const dates = new Set(form.customDates)
  if (form.startDate && form.ruleSelections.length) {
    const start = new Date(`${form.startDate}T00:00:00`)
    const end = new Date(`${form.endDate || form.startDate}T00:00:00`)
    const selectedWeekdays = form.ruleSelections
      .filter((rule) => rule.startsWith('WEEKDAY_'))
      .map((rule) => Number(rule.replace('WEEKDAY_', '')))

    for (const cursor = new Date(start); cursor <= end; cursor.setDate(cursor.getDate() + 1)) {
      const weekday = cursor.getDay() || 7
      if (selectedWeekdays.includes(weekday)) {
        dates.add(formatDateInput(cursor))
      }
    }

    if (form.ruleSelections.includes('HOLIDAY')) {
      storeHolidays.value
        .map((holiday) => holiday.holidayDate)
        .filter((date) => date >= form.startDate && date <= form.endDate)
        .forEach((date) => dates.add(date))
    }
  }
  return [...dates].sort()
})
const isRuleGeneratedDate = (reservationDate) => {
  return form.ruleSelections.length > 0 && !form.customDates.includes(reservationDate)
}

// 載入分店後，載入頁面資料
const loadStores = async () => {
  try {
    const res = await reservationAdminApi.getManageableStores()
    stores.value = res.data || []
    selectedStoreId.value = resolveManagedStoreSelection(stores.value, selectedStoreId.value, canSelectAllStores.value)
    reservationOpenDays.value = selectedStore.value?.reservationOpenDays || 30
    await loadPageData()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '讀取分店資料失敗，請確認後端已啟動'
  }
}

// 進入頁面自動重整載入桌位、時段、容量
const loadPageData = async ({ silent = false } = {}) => {
  if (!stores.value.length) return
  if (!silent) {
    loading.value = true
  }
  errorMessage.value = ''
  successMessage.value = ''
  try {
    Object.keys(capacitiesBySlot).forEach((key) => delete capacitiesBySlot[key])
    if (selectedStoreId.value) {
      const slotRes = await reservationSettingApi.getTimeSlots(selectedStoreId.value)
      slots.value = slotRes.data || []
    } else {
      const results = await Promise.all(stores.value.map(async (store) => {
        const slotRes = await reservationSettingApi.getTimeSlots(store.storeId)
        return {
          slots: slotRes.data || [],
        }
      }))
      slots.value = results
        .flatMap((result) => result.slots)
        .sort((a, b) => `${a.reservationDate} ${a.startTime}`.localeCompare(`${b.reservationDate} ${b.startTime}`))
    }
    await Promise.all(slots.value.map(async (slot) => {
      if (canManageSettings.value) {
        try {
          await reservationSettingApi.rebuildCapacity(slot.slotId)
        } catch (error) {
          // Keep the list usable even if one slot cannot be recalculated.
        }
      }
      try {
        const res = await reservationSettingApi.getCapacity(slot.slotId)
        capacitiesBySlot[slot.slotId] = res.data || []
      } catch (error) {
        capacitiesBySlot[slot.slotId] = []
      }
    }))
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '讀取訂位時段設定失敗'
  } finally {
    if (!silent) {
      loading.value = false
    }
  }
}

// 新增訂位日期時段
const createTimeSlot = async () => {
  if (!form.storeId) {
    createModalError.value = '請選擇要新增時段的分店'
    return
  }
  if (!generatedDates.value.length) {
    createModalError.value = '請至少選擇一個星期規則、區間，或新增自訂日期'
    return
  }
  if (form.requiresDeposit && Number(form.depositAmount || 0) <= 0) {
    createModalError.value = '請輸入大於 0 的訂金金額'
    return
  }
  saving.value = true
  createModalError.value = ''
  successMessage.value = ''
  try {
    const results = await Promise.allSettled(generatedDates.value.map((reservationDate) => reservationSettingApi.createTimeSlot({
      storeId: Number(form.storeId),
      reservationDate,
      startTime: `${form.startTime}:00`,
      endTime: `${form.endTime}:00`,
      isOpen: form.isOpen,
      ruleGenerated: isRuleGeneratedDate(reservationDate),
      requiresDeposit: form.requiresDeposit,
      depositAmount: form.requiresDeposit ? Number(form.depositAmount || 0) : 0,
    })))
    const successCount = results.filter((result) => result.status === 'fulfilled').length
    const failedCount = results.length - successCount
    selectedStartDate.value = formatDateInput(new Date())
    selectedEndDate.value = ''
    await loadPageData({ silent: true })
    if (failedCount) {
      const firstFailed = results.find((result) => result.status === 'rejected')
      createModalError.value = getApiErrorMessage(firstFailed?.reason, `有 ${failedCount} 筆不在營業時間、重複或新增失敗，請調整後再新增`)
      if (successCount) {
        successMessage.value = `已新增 ${successCount} 筆，${failedCount} 筆失敗`
      }
      return
    }
    successMessage.value = `已新增 ${successCount} 筆訂位時段`
    showCreateModal.value = false
  } catch (error) {
    createModalError.value = getApiErrorMessage(error, '新增訂位時段失敗')
  } finally {
    saving.value = false
  }
}

// 資料帶入修改 modal editForm；星期列表，會把底下所有 slot 放進 editingSlots 統一修改
const openEditModal = (slot) => {
  const sourceSlots = slot.isAggregate ? slot.sourceSlots : [slot]
  const firstSlot = sourceSlots[0]
  editingSlots.value = sourceSlots
  editingSlotId.value = firstSlot.slotId
  editForm.storeId = String(firstSlot.storeId)
  editForm.reservationDate = firstSlot.reservationDate
  editForm.startTime = firstSlot.startTime?.slice(0, 5) || ''
  editForm.endTime = firstSlot.endTime?.slice(0, 5) || ''
  editForm.isOpen = Boolean(firstSlot.isOpen)
  editForm.requiresDeposit = Boolean(firstSlot.requiresDeposit)
  editForm.depositAmount = Number(firstSlot.depositAmount || 0)
  errorMessage.value = ''
  editModalError.value = ''
  successMessage.value = ''
  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
  editingSlotId.value = null
  editingSlots.value = []
  editModalError.value = ''
  showEditDateDropdown.value = false
}

// 儲存修改訂位時段
const updateTimeSlot = async () => {
  if (!editingSlotId.value || !editingSlots.value.length) return
  if (editForm.requiresDeposit && Number(editForm.depositAmount || 0) <= 0) {
    editModalError.value = '請輸入大於 0 的訂金金額'
    return
  }
  updating.value = true
  editModalError.value = ''
  successMessage.value = ''
  try {
    await Promise.all(editingSlots.value.map((slot) => reservationSettingApi.updateTimeSlot(slot.slotId, {
      storeId: Number(editForm.storeId),
      reservationDate: isBulkEditing.value ? slot.reservationDate : editForm.reservationDate,
      startTime: `${editForm.startTime}:00`,
      endTime: `${editForm.endTime}:00`,
      isOpen: editForm.isOpen,
      ruleGenerated: isBulkEditing.value,
      requiresDeposit: editForm.requiresDeposit,
      depositAmount: editForm.requiresDeposit ? Number(editForm.depositAmount || 0) : 0,
    })))
    await loadPageData()
    successMessage.value = isBulkEditing.value ? `已統一更新 ${editingSlots.value.length} 筆星期時段` : '訂位時段已更新'
    closeEditModal()
  } catch (error) {
    editModalError.value = getApiErrorMessage(error, '更新訂位時段失敗')
  } finally {
    updating.value = false
  }
}

// 刪除時段的 confirm 提示視窗
const deleteTimeSlot = async (slot) => {
  const sourceSlots = slot.isAggregate ? slot.sourceSlots : [slot]
  const label = slot.isAggregate
    ? `${slot.sourceDates[0]} ~ ${slot.sourceDates[slot.sourceDates.length - 1]} 共 ${slot.sourceDates.length} 天 ${slot.startTime?.slice(0, 5)} - ${slot.endTime?.slice(0, 5)}`
    : `${slot.reservationDate} ${slot.startTime?.slice(0, 5)} - ${slot.endTime?.slice(0, 5)}`
  const confirmed = window.confirm(`確定刪除 ${label} 的訂位時段？`)
  if (!confirmed) return
  errorMessage.value = ''
  successMessage.value = ''
  try {
    await Promise.all(sourceSlots.map((sourceSlot) => reservationSettingApi.deleteTimeSlot(sourceSlot.slotId)))
    await loadPageData()
    successMessage.value = sourceSlots.length > 1 ? `已刪除 ${sourceSlots.length} 筆星期時段` : '訂位時段已刪除'
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '刪除訂位時段失敗'
  }
}

// 新增自訂日期
const addCustomDate = () => {
  if (!form.customDate || form.customDates.includes(form.customDate)) return
  form.customDates.push(form.customDate)
  form.customDate = ''
}

// 移除自訂日期
const removeCustomDate = (date) => {
  form.customDates = form.customDates.filter((item) => item !== date)
}

// 查詢的日期區間選取
const selectDateRangeDay = (date) => {
  const nextRange = nextDateRangeSelection(date, selectedStartDate.value, selectedEndDate.value)
  selectedStartDate.value = nextRange.startDate
  selectedEndDate.value = nextRange.endDate
  if (nextRange.completed) showDateRangeDropdown.value = false
}

// 新增 modal 的日期區間選取
const selectFormDateRangeDay = (date) => {
  const nextRange = nextDateRangeSelection(date, form.startDate, form.endDate)
  form.startDate = nextRange.startDate
  form.endDate = nextRange.endDate
  if (nextRange.completed) showFormDateRangeDropdown.value = false
}

// 自訂日期選取
const selectCustomDate = (date) => {
  form.customDate = date
  showCustomDateDropdown.value = false
}

// 修改 modal 日期選取
const selectEditDate = (date) => {
  editForm.reservationDate = date
  showEditDateDropdown.value = false
}

// 清除查詢日期區間
const clearDateRange = () => {
  selectedStartDate.value = ''
  selectedEndDate.value = ''
}

const isRangeStart = (date) => isRangeStartFor(date, selectedStartDate.value)
const isRangeEnd = (date) => isRangeEndFor(date, selectedEndDate.value)
const isInRange = (date) => isInRangeFor(date, selectedStartDate.value, selectedEndDate.value)

// 查詢的星期多選切換
const toggleWeekday = (weekday) => {
  selectedWeekdays.value = selectedWeekdays.value.includes(weekday)
    ? selectedWeekdays.value.filter((item) => item !== weekday)
    : [...selectedWeekdays.value, weekday]
}

// 新增 modal 日期規則多選切換
const toggleRule = (rule) => {
  form.ruleSelections = form.ruleSelections.includes(rule)
    ? form.ruleSelections.filter((item) => item !== rule)
    : [...form.ruleSelections, rule]
}

// 清除查詢條件，顯示全部
const clearSlotFilters = () => {
  selectedStoreId.value = resolveManagedStoreSelection(stores.value, '', canSelectAllStores.value)
  selectedStartDate.value = ''
  selectedEndDate.value = ''
  showDateRangeDropdown.value = false
  selectedWeekdays.value = []
  showWeekdayDropdown.value = false
  selectedStartTime.value = '00:00'
  selectedEndTime.value = '00:00'
}

// 讀取 modal 分店的公休日資料
const loadModalStoreHolidays = () => {
  if (!form.storeId) {
    storeHolidays.value = []
    return
  }
  storeApi.getStoreDetail(form.storeId, { admin: true })
    .then((res) => {
      storeHolidays.value = res.data?.upcomingHolidays || res.data?.holidays || []
    })
    .catch(() => {
      storeHolidays.value = []
    })
}

// ＊開啟新增 modal：預設分店不受最上方查詢分店控制，但若已選分店就先帶入。＊
const openCreateModal = () => {
  errorMessage.value = ''
  createModalError.value = ''
  successMessage.value = ''
  form.storeId = selectedStoreId.value || (stores.value[0]?.storeId ? String(stores.value[0].storeId) : '')
  form.startDate = ''
  form.endDate = ''
  form.ruleSelections = []
  form.customDate = ''
  form.customDates = []
  form.requiresDeposit = false
  form.depositAmount = 0
  loadModalStoreHolidays()
  showCreateModal.value = true
  showRuleDropdown.value = false
}

// 點空白處關閉下拉選單
const closeDropdowns = () => {
  showDateRangeDropdown.value = false
  showFormDateRangeDropdown.value = false
  showCustomDateDropdown.value = false
  showEditDateDropdown.value = false
  showWeekdayDropdown.value = false
  showRuleDropdown.value = false
}

// 沒點到 dropdown-closable，關閉下拉選單
const handleOutsideClick = (event) => {
  if (event.target.closest('.dropdown-closable')) return
  closeDropdowns()
}

// ＊儲存分店可提前幾天訂位＊
const saveReservationOpenDays = async () => {
  if (!selectedStore.value) return
  savingOpenDays.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    const store = selectedStore.value
    await storeApi.updateStore(store.storeId, {
      brandId: store.brandId,
      storeCode: store.storeCode,
      storeName: store.storeName,
      city: store.city,
      district: store.district,
      address: store.address,
      phone: store.phone,
      latitude: store.latitude,
      longitude: store.longitude,
      mrtInfo: store.mrtInfo,
      parkingInfo: store.parkingInfo,
      description: store.description,
      mainImageUrl: store.mainImageUrl,
      status: store.status,
      reservationOpenDays: Number(reservationOpenDays.value),
    })
    store.reservationOpenDays = Number(reservationOpenDays.value)
    successMessage.value = '訂位開放天數已更新'
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '更新訂位開放天數失敗'
  } finally {
    savingOpenDays.value = false
  }
}

// 查詢分店變更時，重新載入列表
watch(selectedStoreId, () => {
  reservationOpenDays.value = selectedStore.value?.reservationOpenDays || 30
  storeHolidays.value = []
  loadPageData()
})
watch([dateSlotGroups, pageSize, listMode], () => {
  currentPage.value = 1
})
watch(totalPages, () => {
  clampPage()
})
// 新增 modal 分店變更時，重新載入列表
watch(() => form.storeId, () => {
  if (!showCreateModal.value) return
  createModalError.value = ''
  loadModalStoreHolidays()
})
// 掛載時：註冊點擊事件載入資料
// 離開頁面時：移除事件
onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
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
        <h1>訂位日期＆時段</h1>
        <p>設定分店可訂位日期、時段、開放天數與查詢剩餘桌數。</p>
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

    <div class="card">
      <!-- 查詢時段區 -->
      <div class="card-header py-4">
        <div class="row g-3 align-items-end">
          <div class="col-12 col-md-5">
            <label class="form-label">查詢區間</label>
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
                  {{ selectedStartDate || '請選起日' }} <span>~</span> {{ selectedEndDate || '請選迄日' }}
                </div>
                <div class="d-flex justify-content-end gap-2 mt-3">
                  <button type="button" class="btn btn-sm btn-label-secondary" @click="clearDateRange">清除</button>
                  <button type="button" class="btn btn-sm btn-primary" @click="showDateRangeDropdown = false">關閉</button>
                </div>
              </div>
            </div>
          </div>
          <div class="col-6 col-md-5">
            <label class="form-label">星期</label>
            <div class="multi-select dropdown-closable" @click.stop>
              <button type="button" class="form-select text-start" @click="showWeekdayDropdown = !showWeekdayDropdown">
                {{ selectedWeekdayText }}
              </button>
              <div v-if="showWeekdayDropdown" class="multi-select-menu">
                <label v-for="weekday in weekdayOptions" :key="weekday.value" class="multi-select-option">
                  <input
                    type="checkbox"
                    class="form-check-input"
                    :checked="selectedWeekdays.includes(weekday.value)"
                    @change="toggleWeekday(weekday.value)"/>
                  <span>{{ weekday.label }}</span>
                </label>
              </div>
            </div>
          </div>
          <div class="col-6 col-md-4">
            <label class="form-label">開始時間</label>
            <div class="time-picker">
              <select class="form-select" :value="timeHour(selectedStartTime)" @change="setFilterTimePart(selectedStartTime, 'hour', $event.target.value)">
                <option value="" disabled>時</option>
                <option v-for="hour in hourOptions" :key="`filter-start-hour-${hour}`" :value="hour">{{ hour }}</option>
              </select>
              <span>:</span>
              <select class="form-select" :value="timeMinute(selectedStartTime)" @change="setFilterTimePart(selectedStartTime, 'minute', $event.target.value)">
                <option value="" disabled>分</option>
                <option v-for="minute in minuteOptions" :key="`filter-start-minute-${minute}`" :value="minute">{{ minute }}</option>
              </select>
            </div>
          </div>
          <div class="col-6 col-md-4">
            <label class="form-label">結束時間</label>
            <div class="time-picker">
              <select class="form-select" :value="timeHour(selectedEndTime)" @change="setFilterTimePart(selectedEndTime, 'hour', $event.target.value)">
                <option value="" disabled>時</option>
                <option v-for="hour in hourOptions" :key="`filter-end-hour-${hour}`" :value="hour">{{ hour }}</option>
              </select>
              <span>:</span>
              <select class="form-select" :value="timeMinute(selectedEndTime)" @change="setFilterTimePart(selectedEndTime, 'minute', $event.target.value)">
                <option value="" disabled>分</option>
                <option v-for="minute in minuteOptions" :key="`filter-end-minute-${minute}`" :value="minute">{{ minute }}</option>
              </select>
            </div>
          </div>
          <div class="col-6 col-md-4 d-flex flex-wrap gap-3">
            <button type="button" class="btn btn-dark" @click="clearSlotFilters">顯示全部</button>
            <button type="button" class="btn btn-dark" :disabled="loading" @click="loadPageData">
              查詢剩餘桌位
            </button>
          </div>
        </div>
      </div>

      <div class="card-body">
        <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
        <div v-if="successMessage" class="alert alert-success">{{ successMessage }}</div>
        <div v-if="loading" class="alert alert-info">讀取時段中...</div>

        <div class="section-divider"></div>

        <!-- 開放天數設定 -->
        <div class="row g-3 align-items-end mb-4">
          <div class="col-12 col-md-4">
            <label class="form-label">統一開放幾天前訂位</label>
            <input v-model.number="reservationOpenDays" type="number" min="1" class="form-control" :disabled="!canManageSettings" />
          </div>
          <div class="col-12 col-md-3">
            <button type="button" class="btn btn-dark w-100" :disabled="savingOpenDays || !selectedStoreId || !canManageSettings" @click="saveReservationOpenDays">
              {{ savingOpenDays ? '儲存中...' : '儲存開放天數' }}
            </button>
          </div>
          <div class="col-12 col-md-5 text-muted">套用所有已設定的訂位時段</div>
        </div>

        <div class="section-divider"></div>

        <!-- 日期＆時段名單 -->
        <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
          <div class="d-flex align-items-center flex-wrap gap-2">
            <i class="bx bx-time-five"></i>
            <h5 class="mb-0">訂位時段{{ listMode === 'weekday' ? '星期' : '日期' }}列表</h5>
            <button
              type="button"
              class="btn btn-sm rounded-pill"
              :class="listMode === 'weekday' ? 'btn-secondary' : 'btn-secondary'"
              @click="listMode = listMode === 'weekday' ? 'date' : 'weekday'">
              {{ listMode === 'weekday' ? '切換「日期」列表顯示' : '切換「星期」列表顯示' }}
            </button>
          </div>
          <button v-if="canManageSettings" type="button" class="btn btn-label-primary" :disabled="loading" @click="openCreateModal">
            ＋ 新增訂位時段
          </button>
        </div>
        <div class="pt-2">
          <div v-if="listMode === 'date'" class="pagination-toolbar px-1 pb-3">
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
        </div>
          

        <div class="time-slot-list">
          <div v-if="!loading && slotGroups.length === 0" class="text-center text-muted py-4">尚未設定符合條件的訂位時段</div>
          <div v-for="group in slotGroups" :key="group.key" class="time-slot-row">
            <div class="time-slot-date">
              <div v-if="!selectedStoreId" class="text-muted small mb-1">
                {{ storeNameById[String(group.storeId)] || `分店 ${group.storeId}` }}
              </div>
              <template v-if="group.isWeekdayGroup">
                <div class="fw-semibold py-3 px-2">{{ group.weekday }}</div>
                <!-- <span class="badge bg-label-primary">共 {{ group.dateCount }} 天</span> -->
              </template>
              <template v-else>
                <div class="fw-semibold">{{ group.reservationDate }}</div>
                <div class="d-flex align-items-center gap-3 mt-1">
                  <span class="text-muted small fw-normal">{{ group.weekday }}</span>
                  <span v-if="group.isException" class="btn btn-sm btn-label-secondary">單日調整</span>
                </div>
              </template>
            </div>
            <div class="time-slot-items">
              <div v-for="slot in group.slots" :key="slotDisplayKey(slot)" class="time-slot-item">
                <div class="time-slot-item-summary">
                  <button type="button" class="btn btn-sm btn-label-secondary slot-toggle" @click="toggleSlotDetails(slotDisplayKey(slot))">
                    <i :class="openSlotDetails[slotDisplayKey(slot)] ? 'bx bx-chevron-down' : 'bx bx-chevron-right'"></i>
                  </button>
                  <div class="time-slot-item-main">
                    <div class="d-flex align-items-center flex-wrap gap-2">
                      <span class="fw-semibold">{{ slot.startTime?.slice(0, 5) }} - {{ slot.endTime?.slice(0, 5) }}</span>
                      <span class="badge" :class="slot.isOpen ? 'bg-label-success' : 'bg-label-secondary'">
                        {{ slot.isOpen ? '開放' : '未開放' }}
                      </span>
                      <span v-if="slot.requiresDeposit" class="badge bg-label-warning">
                        訂金 ${{ Number(slot.depositAmount || 0).toLocaleString() }}
                      </span>
                      <!-- <span v-if="slot.isAggregate" class="badge bg-label-info">
                        共 {{ slot.sourceDates.length }} 天
                      </span> -->
                    </div>
                  </div>
                </div>
                <div v-if="canManageSettings" class="time-slot-actions">
                  <button type="button" class="btn btn-sm btn-label-secondary" @click="openEditModal(slot)"><i class="bx bx-edit-alt"></i></button>
                  <button type="button" class="btn btn-sm btn-label-secondary" @click="deleteTimeSlot(slot)"><i class="bx bx-trash"></i></button>
                </div>
                <div v-if="openSlotDetails[slotDisplayKey(slot)]" class="slot-capacity-list">
                  <template v-if="slot.isAggregate">
                    <div v-for="dateSlot in slot.sourceSlots" :key="dateSlot.slotId" class="slot-capacity-date-group">
                      <div class="slot-capacity-date">{{ dateSlot.reservationDate }}</div>
                      <div v-if="capacityRows(dateSlot.slotId).length === 0" class="text-muted small">尚未產生容量</div>
                      <div v-for="capacity in capacityRows(dateSlot.slotId)" :key="`${dateSlot.slotId}-${capacity.tableSize}`" class="slot-capacity-row">
                        <span>{{ capacity.tableSize }}人桌</span>
                        <div class="slot-capacity-counts">
                          <span class="slot-capacity-count">
                            <span class="count-label">總數: {{ capacity.totalCount }}</span>
                          </span>
                          <span class="slot-capacity-count">
                            <span class="count-label">已訂: {{ capacity.reservedCount }}</span>
                          </span>
                          <strong class="slot-capacity-count">
                            <span class="count-label">剩餘: {{ capacity.remainingCount }}</span>
                          </strong>
                        </div>
                      </div>
                    </div>
                  </template>
                  <template v-else>
                    <div v-if="capacityRows(slot.slotId).length === 0" class="text-muted small">尚未產生容量</div>
                    <div v-for="capacity in capacityRows(slot.slotId)" :key="`${slot.slotId}-${capacity.tableSize}`" class="slot-capacity-row">
                      <span>{{ capacity.tableSize }}人桌</span>
                      <div class="slot-capacity-counts">
                        <span class="slot-capacity-count">
                          <span class="count-label">總數: {{ capacity.totalCount }}</span>
                        </span>
                        <span class="slot-capacity-count">
                          <span class="count-label">已訂: {{ capacity.reservedCount }}</span>
                        </span>
                        <strong class="slot-capacity-count">
                          <span class="count-label">剩餘: {{ capacity.remainingCount }}</span>
                        </strong>
                      </div>
                    </div>
                  </template>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增時段 modal -->
    <div v-if="showCreateModal" class="modal fade show d-block reservation-modal" tabindex="-1" aria-modal="true" role="dialog">
      <div class="modal-dialog modal-dialog-centered">
        <form class="modal-content" @submit.prevent="createTimeSlot">
          <div class="modal-header">
            <h5 class="modal-title">新增訂位時段</h5>
            <button type="button" class="btn-close" aria-label="Close" @click="showCreateModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="row g-3">
              <div class="col-12">
                <label class="form-label">分店</label>
                <select v-model="form.storeId" class="form-select" required>
                  <option value="" disabled>請選擇分店</option>
                  <option v-for="store in stores" :key="store.storeId" :value="String(store.storeId)">
                    {{ store.storeName }}
                  </option>
                </select>
              </div>
              <div class="col-12">
                <label class="form-label">日期規則</label>
                <div class="multi-select dropdown-closable" @click.stop>
                  <button type="button" class="form-select text-start" @click="showRuleDropdown = !showRuleDropdown">
                    {{ selectedRuleText }}
                  </button>
                  <div v-if="showRuleDropdown" class="multi-select-menu">
                    <label v-for="option in ruleOptions" :key="option.value" class="multi-select-option">
                      <input
                        type="checkbox"
                        class="form-check-input"
                        :checked="form.ruleSelections.includes(option.value)"
                        @change="toggleRule(option.value)"/>
                      <span>{{ option.label }}</span>
                    </label>
                  </div>
                </div>
              </div>
              <div class="col-12">
                <label class="form-label">日期區間</label>
                <div class="multi-select dropdown-closable" @click.stop>
                  <button type="button" class="form-select text-start" @click="showFormDateRangeDropdown = !showFormDateRangeDropdown">
                    {{ formDateRangeText }}
                  </button>
                  <div v-if="showFormDateRangeDropdown" class="multi-select-menu date-range-menu">
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
                          'is-selected': isRangeStartFor(day.value, form.startDate) || isRangeEndFor(day.value, form.endDate),
                          'is-in-range': isInRangeFor(day.value, form.startDate, form.endDate),
                        }"
                        @click="selectFormDateRangeDay(day.value)">
                        {{ day.day }}
                      </button>
                    </div>
                    <div class="date-range-hint">
                      {{ form.startDate || '請選起日' }} <span>~</span> {{ form.endDate || '可只選單日' }}
                    </div>
                    <div class="d-flex justify-content-end gap-2 mt-3">
                      <button type="button" class="btn btn-sm btn-label-secondary" @click="form.startDate = ''; form.endDate = ''">清除</button>
                      <button type="button" class="btn btn-sm btn-primary" @click="showFormDateRangeDropdown = false">關閉</button>
                    </div>
                  </div>
                </div>
              </div>
              <div class="col-8">
                <label class="form-label">自訂日期</label>
                <div class="multi-select dropdown-closable" @click.stop>
                  <button type="button" class="form-select text-start" @click="showCustomDateDropdown = !showCustomDateDropdown">
                    {{ customDateText }}
                  </button>
                  <div v-if="showCustomDateDropdown" class="multi-select-menu date-range-menu">
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
                      <span>一</span>
                      <span>二</span>
                      <span>三</span>
                      <span>四</span>
                      <span>五</span>
                      <span>六</span>
                      <span>日</span>
                    </div>
                    <div class="calendar-grid">
                      <button
                        v-for="day in calendarDays"
                        :key="day.value"
                        type="button"
                        class="calendar-day"
                        :class="{
                          'is-muted': !day.currentMonth,
                          'is-selected': form.customDate === day.value,
                        }"
                        @click="selectCustomDate(day.value)">
                        {{ day.day }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              <div class="col-4 d-flex align-items-end">
                <button type="button" class="btn btn-label-primary w-100" @click="addCustomDate">加入</button>
              </div>
              <div v-if="form.customDates.length" class="col-12">
                <span v-for="date in form.customDates" :key="date" class="badge bg-label-primary me-2 mb-2">
                  {{ date }}
                  <button type="button" class="btn-close btn-close-sm ms-1" aria-label="Remove" @click="removeCustomDate(date)"></button>
                </span>
              </div>
              <div class="col-6">
                <label class="form-label">開始時間</label>
                <div class="time-picker">
                  <select class="form-select" :value="timeHour(form.startTime)" required @change="setFormTimePart(form, 'startTime', 'hour', $event.target.value)">
                    <option v-for="hour in hourOptions" :key="`form-start-hour-${hour}`" :value="hour">{{ hour }}</option>
                  </select>
                  <span>:</span>
                  <select class="form-select" :value="timeMinute(form.startTime)" required @change="setFormTimePart(form, 'startTime', 'minute', $event.target.value)">
                    <option v-for="minute in minuteOptions" :key="`form-start-minute-${minute}`" :value="minute">{{ minute }}</option>
                  </select>
                </div>
              </div>
              <div class="col-6">
                <label class="form-label">結束時間</label>
                <div class="time-picker">
                  <select class="form-select" :value="timeHour(form.endTime)" required @change="setFormTimePart(form, 'endTime', 'hour', $event.target.value)">
                    <option v-for="hour in hourOptions" :key="`form-end-hour-${hour}`" :value="hour">{{ hour }}</option>
                  </select>
                  <span>:</span>
                  <select class="form-select" :value="timeMinute(form.endTime)" required @change="setFormTimePart(form, 'endTime', 'minute', $event.target.value)">
                    <option v-for="minute in minuteOptions" :key="`form-end-minute-${minute}`" :value="minute">{{ minute }}</option>
                  </select>
                </div>
              </div>
              <div class="col-12">
                <label class="form-check">
                  <input v-model="form.isOpen" class="form-check-input" type="checkbox" />
                  <span class="form-check-label">開放訂位</span>
                </label>
              </div>
              <div class="col-12">
                <label class="form-check">
                  <input v-model="form.requiresDeposit" class="form-check-input" type="checkbox" />
                  <span class="form-check-label">需支付訂金</span>
                </label>
              </div>
              <div v-if="form.requiresDeposit" class="col-12">
                <label class="form-label">訂金金額</label>
                <input
                  v-model.number="form.depositAmount"
                  type="number"
                  min="1"
                  step="1"
                  class="form-control"
                  placeholder="請輸入訂金金額"
                  required />
              </div>
              <div class="col-12">
                <div class="alert alert-info mb-0">
                  預計新增 {{ generatedDates.length }} 個日期時段
                </div>
                <div v-if="createModalError" class="alert alert-danger mt-2 mb-0">
                  {{ createModalError }}
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-label-secondary" @click="showCreateModal = false">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="saving || !form.storeId">
              新增
            </button>
          </div>
        </form>
      </div>
    </div>
    <div v-if="showCreateModal" class="modal-backdrop fade show"></div>

    <!-- 修改時段 modal -->
    <div v-if="showEditModal" class="modal fade show d-block reservation-modal" tabindex="-1" aria-modal="true" role="dialog">
      <div class="modal-dialog modal-dialog-centered">
        <form class="modal-content" @submit.prevent="updateTimeSlot">
          <div class="modal-header">
            <h5 class="modal-title">修改訂位時段</h5>
            <button type="button" class="btn-close" aria-label="Close" @click="closeEditModal"></button>
          </div>
          <div class="modal-body">
            <div class="row g-3">
              <div class="col-12">
                <label class="form-label">分店</label>
                <select v-model="editForm.storeId" class="form-select" required>
                  <option value="" disabled>請選擇分店</option>
                  <option v-for="store in stores" :key="store.storeId" :value="String(store.storeId)">
                    {{ store.storeName }}
                  </option>
                </select>
              </div>
              <div class="col-12">
                <label class="form-label">日期</label>
                <div v-if="isBulkEditing" class="alert alert-info mb-0">
                  統一修改 {{ editingDateSummary }} 的此星期時段
                </div>
                <div class="multi-select dropdown-closable" @click.stop>
                  <button
                    type="button"
                    class="form-select text-start"
                    :disabled="isBulkEditing"
                    @click="showEditDateDropdown = !showEditDateDropdown">
                    {{ editDateText }}
                  </button>
                  <div v-if="showEditDateDropdown && !isBulkEditing" class="multi-select-menu date-range-menu">
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
                      <span>一</span>
                      <span>二</span>
                      <span>三</span>
                      <span>四</span>
                      <span>五</span>
                      <span>六</span>
                      <span>日</span>
                    </div>
                    <div class="calendar-grid">
                      <button
                        v-for="day in calendarDays"
                        :key="day.value"
                        type="button"
                        class="calendar-day"
                        :class="{
                          'is-muted': !day.currentMonth,
                          'is-selected': editForm.reservationDate === day.value,
                        }"
                        @click="selectEditDate(day.value)">
                        {{ day.day }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              <div class="col-6">
                <label class="form-label">開始時間</label>
                <div class="time-picker">
                  <select class="form-select" :value="timeHour(editForm.startTime)" required @change="setFormTimePart(editForm, 'startTime', 'hour', $event.target.value)">
                    <option v-for="hour in hourOptions" :key="`edit-start-hour-${hour}`" :value="hour">{{ hour }}</option>
                  </select>
                  <span>:</span>
                  <select class="form-select" :value="timeMinute(editForm.startTime)" required @change="setFormTimePart(editForm, 'startTime', 'minute', $event.target.value)">
                    <option v-for="minute in minuteOptions" :key="`edit-start-minute-${minute}`" :value="minute">{{ minute }}</option>
                  </select>
                </div>
              </div>
              <div class="col-6">
                <label class="form-label">結束時間</label>
                <div class="time-picker">
                  <select class="form-select" :value="timeHour(editForm.endTime)" required @change="setFormTimePart(editForm, 'endTime', 'hour', $event.target.value)">
                    <option v-for="hour in hourOptions" :key="`edit-end-hour-${hour}`" :value="hour">{{ hour }}</option>
                  </select>
                  <span>:</span>
                  <select class="form-select" :value="timeMinute(editForm.endTime)" required @change="setFormTimePart(editForm, 'endTime', 'minute', $event.target.value)">
                    <option v-for="minute in minuteOptions" :key="`edit-end-minute-${minute}`" :value="minute">{{ minute }}</option>
                  </select>
                </div>
              </div>
              <div class="col-12">
                <label class="form-check">
                  <input v-model="editForm.isOpen" class="form-check-input" type="checkbox" />
                  <span class="form-check-label">開放訂位</span>
                </label>
              </div>
              <div class="col-12">
                <label class="form-check">
                  <input v-model="editForm.requiresDeposit" class="form-check-input" type="checkbox" />
                  <span class="form-check-label">需支付訂金</span>
                </label>
              </div>
              <div v-if="editForm.requiresDeposit" class="col-12">
                <label class="form-label">訂金金額</label>
                <input
                  v-model.number="editForm.depositAmount"
                  type="number"
                  min="1"
                  step="1"
                  class="form-control"
                  placeholder="請輸入訂金金額"
                  required />
              </div>
              <div v-if="editModalError" class="col-12">
                <div class="alert alert-danger mb-0">
                  {{ editModalError }}
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-label-secondary" @click="closeEditModal">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="updating">
              {{ updating ? '儲存中...' : '儲存修改' }}
            </button>
          </div>
        </form>
      </div>
    </div>
    <div v-if="showEditModal" class="modal-backdrop fade show"></div>
  </div>
</template>

<style scoped>
.date-select {
  max-width: 160px;
}

.time-select {
  max-width: 160px;
}

.reservation-modal {
  z-index: 1080;
}

.modal-backdrop {
  z-index: 1070;
}

.section-divider {
  border-top: 1px solid #e6e8eb;
  margin: 1.25rem 0;
}

.time-slot-list {
  border: 1px solid #e6e8eb;
  border-radius: 0.5rem;
  overflow: hidden;
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
}

.time-slot-items {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.time-slot-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 1rem;
  align-items: center;
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

.time-slot-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 0.5rem;
}

.slot-capacity-list {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 0.75rem;
  border-radius: 0.5rem;
  background: #f8f9fb;
}

.slot-capacity-date-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.slot-capacity-date {
  color: #566a7f;
  font-size: 0.875rem;
  font-weight: 600;
}

.slot-capacity-row {
  display: grid;
  grid-template-columns: minmax(90px, 1fr) auto;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0.75rem;
  border: 1px solid #edf0f2;
  border-radius: 0.375rem;
  background: #fff;
}

.slot-capacity-counts {
  display: grid;
  grid-template-columns: repeat(3, minmax(86px, 1fr));
  align-items: center;
  justify-content: flex-end;
  gap: 0.75rem;
  color: #697a8d;
  white-space: nowrap;
  text-align: left;
}

.slot-capacity-count {
  display: grid;
  grid-template-columns: 2.25rem auto;
  justify-content: start;
}

.count-label,
.count-value {
  display: inline-block;
}

.slot-capacity-counts strong {
  color: #2f3a4a;
}

.time-picker {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
  align-items: center;
  gap: 0.5rem;
}

.time-picker span {
  color: #697a8d;
  font-weight: 600;
}

@media (max-width: 767.98px) {
  .time-slot-row,
  .time-slot-item {
    grid-template-columns: 1fr;
  }

  .time-slot-actions {
    justify-content: flex-start;
  }

  .slot-capacity-row {
    grid-template-columns: 1fr;
    gap: 0.25rem;
  }

  .slot-capacity-counts {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    justify-content: stretch;
    text-align: left;
    white-space: normal;
  }
}

.date-range-menu {
  min-width: 320px;
}

.reservation-modal .date-range-menu {
  min-width: 260px;
  width: 260px;
}

.reservation-modal .calendar-header {
  margin-bottom: 0.5rem;
}

.reservation-modal .calendar-grid {
  gap: 0.125rem;
}

.reservation-modal .calendar-day {
  font-size: 0.8125rem;
  border-radius: 0.25rem;
}

.reservation-modal .date-range-hint {
  margin-top: 0.5rem;
  padding: 0.375rem 0.5rem;
}

</style>
