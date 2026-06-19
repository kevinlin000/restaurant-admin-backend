<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { reservationApi } from '@/api/reservation'
import { storeApi } from '@/api/store'
import reservationHeroImage from '@/assets/images/reservation.jpg'

const route = useRoute()
const router = useRouter()

const stores = ref([])
const selectedRegion = ref('')
const selectedStoreDetail = ref(null)
const slots = ref([])
const capacity = ref([])
const loading = ref(false)
const submitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const successReservation = ref(null)
const editingReservationId = ref(null)
const showDateDropdown = ref(false)
const calendarMonth = ref(new Date(new Date().getFullYear(), new Date().getMonth(), 1))

// 訂位預設從明天起(無當日訂位)
const today = new Date()
const tomorrow = new Date(today)
tomorrow.setDate(today.getDate() + 1)

// 日期格式：用本地時間 yyyy-MM-dd，避免 toISOString() 造成 UTC 時區問題(非現在時間)
const formatDateInput = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 訂位表單送出時直接組成 API payload（json)
const form = reactive({
  customerName: '',
  customerPhone: '',
  customerEmail: '',
  storeId: '',
  reservationDate: formatDateInput(tomorrow),
  slotId: '',
  partySize: 2,
  specialRequest: '',
})

const regionOptions = [
  { label: '北部', cities: ['台北市', '新北市', '基隆市', '桃園市', '新竹市', '新竹縣', '宜蘭縣'] },
  { label: '中部', cities: ['苗栗縣', '台中市', '彰化縣', '南投縣', '雲林縣'] },
  { label: '南部', cities: ['嘉義市', '嘉義縣', '台南市', '高雄市', '屏東縣'] },
  { label: '東部', cities: ['花蓮縣', '台東縣'] },
]

// 區域篩選分店
const filteredStores = computed(() => {
  if (!selectedRegion.value) return stores.value
  const region = regionOptions.find((item) => item.label === selectedRegion.value)
  if (!region) return stores.value
  return stores.value.filter((store) => region.cities.includes(store.city))
})

// 顧客訂位可選的日期（依據設定的 time_slot）
const availableDates = computed(() => [...new Set(slots.value.map((slot) => slot.reservationDate))])
const availableDateSet = computed(() => new Set(availableDates.value))
const slotsForDate = computed(() => slots.value.filter((slot) => slot.reservationDate === form.reservationDate))
const selectedDateText = computed(() => form.reservationDate || '請選擇日期')

// 日曆版面。
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
      isAvailable: availableDateSet.value.has(formatDateInput(date)),
    }
  })
})

// 轉換格式前台只顯示 HH:mm (後端 time 格式可能含 ss 秒)
const formatTime = (time) => time?.slice(0, 5) || ''

// 手機輸入自動格式化 0900-000-000
const formatPhoneNumber = (value) => {
  const digits = String(value || '').replace(/\D/g, '').slice(0, 10)
  if (digits.length <= 4) return digits
  if (digits.length <= 7) return `${digits.slice(0, 4)}-${digits.slice(4)}`
  return `${digits.slice(0, 4)}-${digits.slice(4, 7)}-${digits.slice(7)}`
}

// 輸入手機就套用手機格式（降低使用者輸入錯誤）
const handlePhoneInput = (event) => {
  form.customerPhone = formatPhoneNumber(event.target.value)
}

// 讀取訂位選擇的分店，在訂位成功頁顯示
const getSelectedStoreName = () => {
  return selectedStoreDetail.value?.storeName
    || stores.value.find((store) => String(store.storeId) === String(form.storeId))?.storeName
    || ''
}

// 訂位成功後姓名、電話、分店放 sessionStorage（避免後端回傳資料暫時缺欄位時成功頁空白）
const cacheSuccessReservation = (reservationId) => {
  if (!reservationId) return
  sessionStorage.setItem(`reservation-success-${reservationId}`, JSON.stringify({
    customerName: form.customerName,
    customerPhone: form.customerPhone,
    storeName: getSelectedStoreName(),
  }))
}

// 切換日曆月份
const shiftCalendarMonth = (offset) => {
  calendarMonth.value = new Date(calendarMonth.value.getFullYear(), calendarMonth.value.getMonth() + offset, 1)
}

// ＊選指定日期所在月份（載入預設日期、編輯訂位時）＊
const setCalendarMonthByDate = (dateText) => {
  if (!dateText) return
  const date = new Date(`${dateText}T00:00:00`)
  calendarMonth.value = new Date(date.getFullYear(), date.getMonth(), 1)
}

// ＊選日期時只接受後台開放的日期 ＊
const selectReservationDate = (date) => {
  if (!availableDateSet.value.has(date)) return
  form.reservationDate = date
  showDateDropdown.value = false
}
const isSelectedDate = (date) => date === form.reservationDate

// 點頁面空白處可關閉日下拉選單
const closeDropdowns = () => {
  showDateDropdown.value = false
}
const handleOutsideClick = (event) => {
  if (event.target.closest('.dropdown-closable')) return
  closeDropdowns()
}

// 訂位須知使用分店描述（未設定描述顯示預設文字）
const reservationNotice = computed(() => {
  return selectedStoreDetail.value?.description || '請至少提前一天預約訂位。可選日期與時段會依店家開放設定顯示。'
})

// 載入訂位分店下拉選單（預設第一間）
const loadStores = async () => {
  try {
    const res = await storeApi.getStores()
    stores.value = res.data || []
    if (!form.storeId && filteredStores.value.length) {
      form.storeId = String(filteredStores.value[0].storeId)
    }
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '讀取分店資料失敗，請確認後端已啟動'
  }
}

// 讀取分店詳細資訊
const loadStoreDetail = async () => {
  selectedStoreDetail.value = null
  if (!form.storeId) return
  const res = await storeApi.getStoreDetail(form.storeId)
  selectedStoreDetail.value = res.data?.store || res.data || null
}

// 依分店查可訂時段
const loadSlots = async () => {
  if (!form.storeId) return
  loading.value = true
  errorMessage.value = ''
  try {
    await loadStoreDetail()
    const endDate = new Date(today)
    endDate.setDate(today.getDate() + (selectedStoreDetail.value?.reservationOpenDays || 30))
    const res = await reservationApi.getAvailableSlots({
      storeId: form.storeId,
      startDate: formatDateInput(tomorrow),
      endDate: formatDateInput(endDate),
    })
    slots.value = res.data || []
    if (!availableDates.value.includes(form.reservationDate)) {
      form.reservationDate = availableDates.value[0] || formatDateInput(tomorrow)
    }
    setCalendarMonthByDate(form.reservationDate)
    form.slotId = slotsForDate.value[0]?.slotId ? String(slotsForDate.value[0].slotId) : ''
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '讀取可訂位時段失敗'
  } finally {
    loading.value = false
  }
}

// 依選取的時段查桌型容量
const loadCapacity = async () => {
  capacity.value = []
  if (!form.slotId) return
  const res = await reservationApi.getSlotCapacity(form.slotId)
  capacity.value = res.data || []
}

// 送出訂位、修改訂位：成功後切到訂位成功頁
const submitReservation = async () => {
  submitting.value = true
  errorMessage.value = ''
  successMessage.value = ''
  const payload = {
    storeId: Number(form.storeId),
    slotId: Number(form.slotId),
    customerName: form.customerName,
    customerPhone: form.customerPhone,
    customerEmail: form.customerEmail,
    partySize: Number(form.partySize),
    specialRequest: form.specialRequest,
  }
  try {
    const isEditing = Boolean(editingReservationId.value)
    const res = editingReservationId.value
      ? await reservationApi.updateReservation(editingReservationId.value, payload)
      : await reservationApi.createReservation(payload)
    cacheSuccessReservation(res.data?.reservationId)
    successReservation.value = res.data
    editingReservationId.value = null
    successMessage.value = isEditing ? '訂位已更新' : ''
    router.push({ name: 'CustomerReservationSuccess', query: { id: res.data.reservationId } })
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '訂位失敗，請稍後再試'
  } finally {
    submitting.value = false
  }
}

// 編輯訂位：導回訂位頁時，依 editId 載入訂位資料並填回表單
const loadReservationForEdit = async () => {
  const editId = route.query.editId
  if (!editId) return
  loading.value = true
  errorMessage.value = ''
  try {
    const res = await reservationApi.getReservation(editId)
    const item = res.data
    editingReservationId.value = item.reservationId
    form.customerName = item.customerName || ''
    form.customerPhone = formatPhoneNumber(item.customerPhone || '')
    form.customerEmail = item.customerEmail || ''
    form.storeId = item.storeId ? String(item.storeId) : ''
    form.partySize = item.partySize || 2
    form.specialRequest = item.specialRequest || ''
    if (form.storeId) {
      await loadSlots()
    }
    form.reservationDate = item.reservationDate || form.reservationDate
    setCalendarMonthByDate(form.reservationDate)
    form.slotId = item.slotId ? String(item.slotId) : ''
    await loadCapacity()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '讀取訂位資料失敗'
  } finally {
    loading.value = false
  }
}

// 分店更改：重新載入可訂時段
watch(() => form.storeId, loadSlots)

// 區域更改：如果原本選的分店不在此區域，自動改選該區第一間分店
watch(selectedRegion, () => {
  if (!filteredStores.value.some((store) => String(store.storeId) === String(form.storeId))) {
    form.storeId = filteredStores.value[0]?.storeId ? String(filteredStores.value[0].storeId) : ''
  }
})

// ＊日期更改：切換日曆月份並預設選該日第一個時段＊
watch(() => form.reservationDate, () => {
  setCalendarMonthByDate(form.reservationDate)
  form.slotId = slotsForDate.value[0]?.slotId ? String(slotsForDate.value[0].slotId) : ''
})

// 時段更改：重新查容量
watch(() => form.slotId, loadCapacity)

// 掛載時：綁定全頁點擊事件，並載入分店
onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
  loadStores().then(loadReservationForEdit)
})
// 卸載時：移除事件避免記憶體洩漏
onBeforeUnmount(() => {
  document.removeEventListener('click', handleOutsideClick)
})
</script>

<template>
  <main class="reservation-page">
    <!-- 訂位頁首 -->
    <section class="reservation-hero" :style="{ backgroundImage: `linear-gradient(90deg, rgba(22, 28, 34, 0.76), rgba(22, 28, 34, 0.2)), url(${reservationHeroImage})` }">
      <div class="container reservation-hero-shell">
        <div class="reservation-hero-content">
          <span class="eyebrow">線上訂位</span>
          <h2 class="mb-3">訂位<span class="text-muted fw-light"> / Reservation</span></h2>
        </div>
      </div>
    </section>

    <div class="page-container reservation-content">
    <!-- 分店篩選 -->
    <div v-if="!successReservation" class="card mb-4 store-filter-card">
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-5">
            <label class="form-label" for="reservation-region-switch">區域</label>
            <select id="reservation-region-switch" v-model="selectedRegion" class="form-select">
              <option value="">全部區域</option>
              <option v-for="region in regionOptions" :key="region.label" :value="region.label">
                {{ region.label }}
              </option>
            </select>
          </div>
          <div class="col-md-7">
            <label class="form-label" for="reservation-store-switch">選擇分店</label>
            <select id="reservation-store-switch" v-model="form.storeId" class="form-select" required>
              <option value="">請選擇分店</option>
              <option v-for="store in filteredStores" :key="store.storeId" :value="String(store.storeId)">
                {{ store.storeName }}｜{{ store.city }}{{ store.district }}
              </option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- 訂位表單 -->
    <div v-if="!successReservation" class="card mb-4">
      <h5 class="card-header text-muted">訂位須知：</h5>
      <ul class="text-muted">
        <li>{{ reservationNotice }}</li>
        <li>可選日期與時段會依店家後台設定顯示，公休日不可預訂。</li>
        <li>送出後系統會即時確認剩餘可訂桌數。</li>
      </ul>
      <hr class="my-3" />

      <form class="card-body" @submit.prevent="submitReservation">
        <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
        <div v-if="loading" class="alert alert-info">讀取可訂位資料中...</div>

        <h5>訂位資訊</h5>
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label" for="reservation-name">姓名</label>
            <input id="reservation-name" v-model.trim="form.customerName" type="text" class="form-control" required />
          </div>
          <div class="col-md-6">
            <label class="form-label" for="reservation-phone">手機</label>
            <input
              id="reservation-phone"
              v-model.trim="form.customerPhone"
              type="tel"
              class="form-control"
              placeholder="0900-000-000"
              maxlength="12"
              required
              @input="handlePhoneInput"/>
          </div>
          <div class="col-md-6">
            <label class="form-label" for="reservation-email">Email</label>
            <input id="reservation-email" v-model.trim="form.customerEmail" type="email" class="form-control" placeholder="name@gmail.com" />
          </div>
          <div class="col-md-6">
            <label class="form-label" for="reservation-party-size">人數</label>
            <select id="reservation-party-size" v-model.number="form.partySize" class="form-select" required>
              <option v-for="size in 12" :key="size" :value="size">{{ size }} 人</option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label" for="reservation-date">日期</label>
            <!-- 自訂日曆下拉 -->
            <div class="reservation-date-picker dropdown-closable" @click.stop>
              <button
                id="reservation-date"
                type="button"
                class="form-select text-start"
                :disabled="loading || !availableDates.length"
                @click="showDateDropdown = !showDateDropdown">
                {{ availableDates.length ? selectedDateText : '目前沒有可訂日期' }}
              </button>
              <div v-if="showDateDropdown" class="reservation-calendar-menu">
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
                      'is-available': day.isAvailable,
                      'is-selected': isSelectedDate(day.value),
                    }"
                    :disabled="!day.isAvailable"
                    @click="selectReservationDate(day.value)">
                    {{ day.day }}
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div class="col-md-6">
            <label class="form-label" for="reservation-slot">時段</label>
            <select id="reservation-slot" v-model="form.slotId" class="form-select" required>
              <option value="">請選擇</option>
              <option v-for="slot in slotsForDate" :key="slot.slotId" :value="String(slot.slotId)">
                {{ formatTime(slot.startTime) }} - {{ formatTime(slot.endTime) }}
              </option>
            </select>
          </div>
          <div class="col-12">
            <label class="form-label" for="reservation-request">特殊需求</label>
            <textarea id="reservation-request" v-model.trim="form.specialRequest" class="form-control" rows="3"></textarea>
          </div>
        </div>

        <div class="pt-4">
          <button type="submit" class="btn btn-primary me-sm-3 me-1" :disabled="submitting || !form.slotId">
            {{ submitting ? '送出中...' : (editingReservationId ? '修改訂位' : '確認訂位') }}
          </button>
          <button type="reset" class="btn btn-label-secondary" @click="errorMessage = ''">取消</button>
        </div>
      </form>
    </div>
    </div>
  </main>
</template>

<style scoped>
.reservation-page {
  min-height: 100vh;
  /* background: #f7f3ee; */
  background: url('../../../assets/images/background.png');
}

.reservation-hero {
  min-height: 340px;
  padding: 138px 0 62px;
  background-position: center;
  background-size: cover;
}

.reservation-hero-shell {
  display: flex;
  align-items: end;
  min-height: 180px;
}

.reservation-hero-content {
  max-width: 750px;
  padding-left: 50px;
  padding-bottom: 15px;
  color: #ffffff;
}

.eyebrow {
  display: inline-block;
  margin-bottom: 12px;
  color: #e5b582;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.12em;
}

.reservation-hero-content h1 {
  margin: 0 0 14px;
  font-size: 56px;
  font-weight: 900;
  letter-spacing: 0;
}

.reservation-hero-content p {
  max-width: 660px;
  margin: 0;
  font-size: 18px;
  line-height: 1.8;
}

.reservation-content {
  position: relative;
  padding-top: 0;
  padding-bottom: 78px;
}

.store-filter-card {
  position: relative;
  z-index: 5;
  margin-top: -62px;
  box-shadow: 0 0.75rem 1.75rem rgba(52, 64, 81, 0.12);
  margin-top: -60px;
  border-radius: 15px;
  margin-right: auto;
  margin-left: auto;
}

.reservation-success-card {
  max-width: 600px;
  margin: 0 auto;
}

/* 自訂日曆樣式 */

.reservation-date-picker {
  position: relative;
}

.reservation-calendar-menu {
  position: absolute;
  z-index: 20;
  top: calc(100% + 6px);
  left: 0;
  width: 320px;
  max-width: min(320px, 92vw);
  padding: 0.75rem;
  border: 1px solid #e4d9ce;
  border-radius: 0.5rem;
  background: #ffffff;
  box-shadow: 0 0.5rem 1.25rem rgba(52, 64, 81, 0.12);
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
  margin-bottom: 0.25rem;
  color: #697386;
  font-size: 0.75rem;
  text-align: center;
}

.calendar-day {
  width: 100%;
  aspect-ratio: 1;
  border: 0;
  border-radius: 0.375rem;
  background: transparent;
  color: #c4b8ad;
  cursor: not-allowed;
}

.calendar-day.is-muted {
  color: #d8d0c8;
}

.calendar-day.is-available {
  background: #faf3ea;
  color: #8c552e;
  cursor: pointer;
}

.calendar-day.is-available:hover {
  background: #f1dac7;
}

.calendar-day.is-selected {
  background: #b1642f;
  color: #ffffff;
}

.calendar-day:disabled {
  opacity: 1;
}

@media (max-width: 768px) {
  .reservation-hero {
    min-height: 280px;
    padding: 112px 0 44px;
  }

  .reservation-hero-content h1 {
    font-size: 40px;
  }
}
</style>
