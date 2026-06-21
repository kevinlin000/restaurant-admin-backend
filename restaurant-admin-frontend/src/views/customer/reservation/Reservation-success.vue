<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Swal from 'sweetalert2'
import { reservationApi } from '@/api/reservation'
import { storeApi } from '@/api/store'

const route = useRoute()
const router = useRouter()

const reservation = ref(null)
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const cachedReservation = ref({})
const storeName = ref('')

// 轉換格式前台只顯示 HH:mm (後端 time 格式可能含 ss 秒)
const formatTime = (time) => time?.slice(0, 5) || ''

// 訂位成功顯示名字、分店
const displayName = computed(() => reservation.value?.customerName || cachedReservation.value.customerName || '訂位顧客')
const displayStoreName = computed(() => storeName.value || cachedReservation.value.storeName || '未指定分店')
const pageTitle = computed(() => route.query.mode === 'query' ? '查詢成功' : '訂位成功')

// 讀取訂位頁送出暫存顧客資訊（避免重新整理或後端欄位差）
const loadCachedReservation = (reservationId) => {
  const cache = sessionStorage.getItem(`reservation-success-${reservationId}`)
  try {
    cachedReservation.value = cache ? JSON.parse(cache) : {}
  } catch {
    cachedReservation.value = {}
  }
}

// ＊依 storeId 查分店名稱，成功頁需要顯示顧客訂的是哪一間店。＊
const loadStoreName = async (storeId) => {
  storeName.value = ''
  if (!storeId) return
  try {
    const res = await storeApi.getStoreDetail(storeId)
    const store = res.data?.store || res.data
    storeName.value = store?.storeName || ''
  } catch {
    storeName.value = ''
  }
}

// query id 讀取單筆訂位
const loadReservation = async () => {
  if (!route.query.id) {
    errorMessage.value = '找不到訂位編號'
    return
  }
  loading.value = true
  try {
    const res = await reservationApi.getReservation(route.query.id)
    reservation.value = res.data
    loadCachedReservation(res.data?.reservationId)
    await loadStoreName(res.data?.storeId)
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '讀取訂位資料失敗'
  } finally {
    loading.value = false
  }
}

// 取消訂位（SweetAlert） 
const cancelReservation = async () => {
  if (!reservation.value) return
  errorMessage.value = ''
  const result = await Swal.fire({
    title: '確定取消訂位？',
    text: '取消後若要用餐需要重新訂位。',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: '確定取消',
    cancelButtonText: '返回',
    confirmButtonColor: '#dc3545',
    cancelButtonColor: '#8592a3',
  })

  if (!result.isConfirmed) return

  try {
    await reservationApi.cancelReservation(reservation.value.reservationId)
    await Swal.fire({
      title: '已取消訂位',
      text: '您的訂位已取消。',
      icon: 'success',
      confirmButtonText: '確定',
      confirmButtonColor: '#e3ac7f',
    })
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '取消訂位失敗，請稍後再試'
    return
  }
  router.push({ name: 'CustomerReservation' })
}

// 保留訂位（SweetAlert） 
const reserveReservation = async () => {
  if (!reservation.value) return
  errorMessage.value = ''
  successMessage.value = ''
  const result = await Swal.fire({
    title: '確定保留訂位？',
    text: '保留後如需更改訂位資訊，請致電。',
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: '確定保留',
    cancelButtonText: '返回',
    confirmButtonColor: '#e3ac7f',
    cancelButtonColor: '#8592a3',
  })

  if (!result.isConfirmed) return

  try {
    const res = await reservationApi.reserveReservation(reservation.value.reservationId)
    reservation.value = res.data
    successMessage.value = '已保留訂位，店家會依現場安排桌位。如需更改訂位資訊，請致電'
    await Swal.fire({
      title: '已保留訂位',
      text: '您的訂位已成功保留。',
      icon: 'success',
      confirmButtonText: '確認',
      confirmButtonColor: '#e3ac7f',
    })
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '保留訂位失敗，請稍後再試'
  }
}

// 編輯訂位：只有 PENDING 狀態可編輯
const editReservation = () => {
  if (!reservation.value || reservation.value.status !== 'PENDING') return
  router.push({ name: 'CustomerReservation', query: { editId: reservation.value.reservationId } })
}

// 回訂位頁
const goReservationPage = () => {
  router.push({ name: 'CustomerReservation' })
}

// 頁面載入時立即讀取訂位資料
onMounted(loadReservation)
</script>

<template>
  <div class="page-container">
    <!-- 訂位成功 card -->
    <div class="card mb-4 py-4 align-items-center reservation-success-card">
      <div class="avatar me-2">
        <span class="avatar-initial rounded-circle bg-label-success">
          <i class="bx bx-check-double bx-sm" style="color: green;"></i>
        </span>
      </div>
      <h3 class="card-header">{{ pageTitle }}</h3>

      <div class="card-body text-center w-100">
        <div v-if="loading" class="alert alert-info">讀取訂位資料中...</div>
        <div v-else-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
        <template v-else-if="reservation">
          <hr class="my-3" />
          <div class="row g-3">
            <div class="col-md-12"><p class="fs-4 mb-0">{{ displayName }}</p></div>
            <div class="col-md-12"><p class="mb-0 text-muted">{{ displayStoreName }}</p></div>
            <div class="col-md-12"><p class="fs-4 mb-0">{{ reservation.reservationDate }}</p></div>
            <div class="col-md-12"><p class="fs-4 mb-0">{{ reservation.partySize }} 位</p></div>
            <div class="col-md-12"><p class="fs-4 mb-0">{{ formatTime(reservation.startTime) }} - {{ formatTime(reservation.endTime) }}</p></div>
          </div>
          <p class="mt-3 mb-0">
            <span class="badge" :class="reservation.status === 'RESERVED' ? 'bg-label-warning' : 'bg-label-success'">
              {{ reservation.status === 'RESERVED' ? '已保留' : '待確認' }}
            </span>
          </p>
          <div v-if="successMessage" class="alert alert-success mt-3 mb-0">{{ successMessage }}</div>
          <!-- 編輯、保留、刪除按鈕 -->
          <div class="pt-5 success-card-actions">
            <button
              v-if="reservation.status === 'PENDING'"
              type="button"
              class="btn btn-label-primary me-sm-3 me-1"
              @click="editReservation">
              編輯訂位
            </button>
            <button
              v-if="reservation.status === 'PENDING'"
              type="button"
              class="btn btn-label-warning me-sm-3 me-1"
              @click="reserveReservation">
              保留訂位
            </button>
            <button type="button" class="btn btn-label-danger me-sm-3 me-1" @click="cancelReservation">取消訂位</button>
          </div>
        </template>
      </div>
    </div>
    <!-- 導覽到其他頁面 -->
    <div v-if="reservation && !loading && !errorMessage" class="success-footer-actions">
      <button type="button" class="btn btn-reservation-dark" @click="goReservationPage">
        <i class="bx bx-chevron-left"></i>
        回訂位頁面
      </button>
      <RouterLink class="btn btn-reservation-light" to="/menu">
        瀏覽菜單 <i class="bx bx-food-menu"></i>
      </RouterLink>
      <RouterLink class="btn btn-reservation-light" to="/store">
        店舖位置 <i class="bx bx-map"></i>
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
.reservation-success-card {
  max-width: 600px;
  margin: 0 auto;
}

.success-card-actions {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.success-card-actions .btn {
  margin-right: 0 !important;
  margin-left: 0 !important;
}

.success-footer-actions {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 0.75rem;
  max-width: 600px;
  margin: 1rem auto 4rem;
}

.success-footer-actions .btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.25rem;
  min-width: 126px;
}

@media (max-width: 576px) {
  .success-footer-actions {
    flex-direction: column;
  }
}
</style>
