<template>
  <div class="admin-order-page">
    <h1>訂單管理</h1>
    <p class="subtitle">查看訂單、付款狀態與出餐流程。</p>

    <div class="summary-row">
      <div class="summary-card clickable" :class="{ active: quickFilter === 'ALL' }" @click="setQuickFilter('ALL')">
        <p>全部訂單</p>
        <h2>{{ orders.length }}</h2>
      </div>

      <div class="summary-card clickable" :class="{ active: quickFilter === 'ACTIVE' }"
        @click="setQuickFilter('ACTIVE')">
        <p>進行中</p>
        <h2>{{ activeCount }}</h2>
      </div>

      <div class="summary-card clickable" :class="{ active: quickFilter === 'COMPLETED' }"
        @click="setQuickFilter('COMPLETED')">
        <p>已完成</p>
        <h2>{{ completedCount }}</h2>
      </div>

      <div class="summary-card clickable" :class="{ active: quickFilter === 'UNPAID' }"
        @click="setQuickFilter('UNPAID')">
        <p>待付款</p>
        <h2>{{ unpaidCount }}</h2>
      </div>

      <div class="summary-card clickable" @click="setQuickFilter('TODAY_REVENUE')"
:class="{ active: quickFilter === 'TODAY_REVENUE' }">
        <p>今日營收</p>
        <h2>${{ todayRevenue }}</h2>
      </div>
    </div>

    <div class="table-card">
      <div class="table-header">
        <h2>訂單列表</h2>

        <div class="filter-panel">
         <input
    v-model="keyword"
    class="search-input"
    type="text"
    placeholder="搜尋訂單編號 / 會員編號"
  />

  <select v-model="dateFilter" class="date-select">
    <option value="">全部日期</option>
    <option value="TODAY">今天</option>
    <option value="YESTERDAY">昨天</option>
    <option value="WEEK">近 7 天</option>
    <option value="MONTH">近 30 天</option>
  </select>
          <div class="filter-row">
            <select v-model="paymentStatusFilter">
              <option value="">全部付款狀態</option>
              <option value="UNPAID">待付款</option>
              <option value="PAID">已付款</option>
              <option value="REFUNDED">已退款</option>
            </select>

            <select v-model="paymentMethodFilter">
              <option value="">全部付款方式</option>
              <option value="CASH">現金</option>
              <option value="CREDIT_CARD">信用卡</option>
              <option value="LINE_PAY">LINE PAY</option>
            </select>

            <select v-model="statusFilter">
              <option value="">全部狀態</option>
              <option value="PENDING">待處理</option>
              <option value="CONFIRMED">已確認</option>
              <option value="PREPARING">製作中</option>
              <option value="READY">待取餐</option>
              <option value="COMPLETED">已完成</option>
              <option value="CANCELLED">已取消</option>
            </select>

            <select v-model="sortType">
              <option value="NEWEST">最新訂單</option>
              <option value="OLDEST">最舊訂單</option>
              <option value="AMOUNT_DESC">金額高到低</option>
              <option value="AMOUNT_ASC">金額低到高</option>
            </select>

            <select v-model="pageSize">
              <option :value="10">每頁 10 筆</option>
              <option :value="20">每頁 20 筆</option>
              <option :value="50">每頁 50 筆</option>
            </select>

            <button type="button" class="reset-btn" @click="resetFilters">
              重設篩選
            </button>
          </div>
        </div>
      </div>
    </div>

    <table>
      <thead>
        <tr>
          <th>訂單編號</th>
          <th>會員編號</th>
          <th>類型</th>
          <th>金額</th>
          <th>付款方式</th>
          <th>付款狀態</th>
          <th>訂單狀態</th>
          <th>建立時間</th>
          <th>操作</th>
        </tr>
      </thead>

      <tbody>
        <tr v-for="order in filteredOrders" :key="order.orderId">
          <td>#{{ order.orderId }}</td>
          <td>{{ order.userId }}</td>
          <td>{{ formatOrderType(order.orderType) }}</td>
          <td>${{ order.finalAmount }}</td>
          <td>{{ formatPaymentMethod(order.paymentMethod) }}</td>
          <td>{{ formatPaymentStatus(order.paymentStatus) }}</td>
          <td>
            <select class="status-select" :class="getStatusClass(order.status)" :value="order.status"
              :disabled="order.status === 'COMPLETED' || order.status === 'CANCELLED'"
              @change="changeStatus(order.orderId, $event.target.value)">
              <option value="PENDING">⏳ 待處理</option>
              <option value="CONFIRMED">✅ 已確認</option>
              <option value="PREPARING">👨‍🍳 製作中</option>
              <option value="READY">🔔 待取餐</option>
              <option value="COMPLETED">🎉 已完成</option>
              <option value="CANCELLED">❌ 已取消</option>
            </select>
          </td>
          <td>{{ formatDate(order.createdAt) }}</td>
          <td>
            <button type="button" @click="openDetail(order)">
              查看
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="filteredOrders.length === 0" class="empty">
      目前沒有訂單資料
    </div>
    <div class="pagination">
      <button type="button" :disabled="currentPage === 1" @click="goPrevPage">
        上一頁
      </button>

      <span>
        第 {{ currentPage }} / {{ totalPages }} 頁，共 {{ filteredTotalCount }} 筆
      </span>

      <button type="button" :disabled="currentPage === totalPages" @click="goNextPage">
        下一頁
      </button>
    </div>
  </div>
  <div v-if="selectedOrder" class="modal-mask" @click.self="selectedOrder = null">
    <div class="modal">
      <div class="modal-header">
        <h2>訂單明細 #{{ selectedOrder.orderId }}</h2>
        <button type="button" @click="selectedOrder = null">×</button>
      </div>

      <div class="detail-grid">
        <p><strong>會員編號：</strong>{{ selectedOrder.userId }}</p>
        <p><strong>門市編號：</strong>{{ selectedOrder.storeId }}</p>
        <p><strong>桌號：</strong>{{ selectedOrder.tableId || '無' }}</p>
        <p><strong>訂單類型：</strong>{{ formatOrderType(selectedOrder.orderType) }}</p>
        <p><strong>付款方式：</strong>{{ formatPaymentMethod(selectedOrder.paymentMethod) }}</p>
        <p><strong>付款狀態：</strong>{{ formatPaymentStatus(selectedOrder.paymentStatus) }}</p>
        <p><strong>訂單狀態：</strong>{{ formatOrderStatus(selectedOrder.status) }}</p>
        <p><strong>發票類型：</strong>{{ selectedOrder.invoiceType || 'NONE' }}</p>
        <p><strong>載具 / 統編：</strong>{{ selectedOrder.carrierNumber || '無' }}</p>
      </div>

      <h3>餐點內容</h3>

      <table>
        <thead>
          <tr>
            <th>餐點</th>
            <th>數量</th>
            <th>單價</th>
            <th>小計</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="item in selectedOrder.items || []" :key="item.menuItemId">
            <td>{{ item.itemName }}</td>
            <td>{{ item.quantity }}</td>
            <td>${{ item.unitPrice }}</td>
            <td>${{ item.subtotal }}</td>
          </tr>
        </tbody>
      </table>

      <div v-if="!selectedOrder.items || selectedOrder.items.length === 0" class="empty">
        目前沒有餐點明細
      </div>

      <div class="total-box">
        <p>原始金額：${{ selectedOrder.totalAmount }}</p>
        <p>點數折抵：${{ selectedOrder.pointsUsed || 0 }}</p>
        <h2>實付金額：${{ selectedOrder.finalAmount }}</h2>
      </div>
    </div>
  </div>


</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import Swal from 'sweetalert2'
import {
  getAdminOrders,
  getAdminOrderById,
  updateAdminOrderStatus
} from '@/api/orderAdminApi'

const orders = ref([])
const selectedOrder = ref(null)
const statusFilter = ref('')
const paymentStatusFilter = ref('')
const paymentMethodFilter = ref('')
const keyword = ref('')
const route = useRoute()
const sortType = ref('NEWEST')
const pageSize = ref(10)
const currentPage = ref(1)
const quickFilter = ref('ALL')
const dateFilter = ref('')
const lastOrderCount = ref(0)


onMounted(async () => {
  await loadOrders()

  const orderId = route.query.orderId

  if (orderId) {
    const targetOrder = orders.value.find(
      order => String(order.orderId) === String(orderId)
    )

    if (targetOrder) {
      openDetail(targetOrder)
    } else {
      Swal.fire({
        icon: 'warning',
        title: '找不到訂單',
        text: `找不到訂單編號 #${orderId}`,
        confirmButtonText: '我知道了'
      })
    }
  }
})

const isStatusLocked = (order) => {
  return order.status === 'COMPLETED' ||
    order.status === 'CANCELLED'
}
const unpaidCount = computed(() => {
  return orders.value.filter(order => order.paymentStatus === 'UNPAID').length
})

const todayRevenue = computed(() => {
  const today = new Date().toLocaleDateString('zh-TW')

  return orders.value
    .filter(order => {
      if (!order.createdAt) return false
      const orderDate = new Date(order.createdAt).toLocaleDateString('zh-TW')
      return orderDate === today && order.paymentStatus === 'PAID'
    })
    .reduce((sum, order) => sum + Number(order.finalAmount || 0), 0)
})
const setQuickFilter = (type) => {
  quickFilter.value = type
  currentPage.value = 1
}


const loadOrders = async () => {
  try {
    const res = await getAdminOrders()
    orders.value = res.data
  } catch (error) {
    console.error(error)
    alert('取得訂單資料失敗')
  }
}



const filteredOrders = computed(() => {
  let result = [...orders.value]

  if (quickFilter.value === 'ACTIVE') {
    result = result.filter(order =>
      order.status !== 'COMPLETED' &&
      order.status !== 'CANCELLED'
    )
  }

  if (quickFilter.value === 'COMPLETED') {
    result = result.filter(order => order.status === 'COMPLETED')
  }

  if (quickFilter.value === 'UNPAID') {
    result = result.filter(order => order.paymentStatus === 'UNPAID')
  }

  if (quickFilter.value === 'TODAY_REVENUE') {
  const today = new Date().toLocaleDateString('zh-TW')

  result = result.filter(order => {
    if (!order.createdAt) return false

    const orderDate = new Date(order.createdAt).toLocaleDateString('zh-TW')

    return orderDate === today && order.paymentStatus === 'PAID'
  })
}

  if (statusFilter.value) {
    result = result.filter(order => order.status === statusFilter.value)
  }

  if (paymentStatusFilter.value) {
    result = result.filter(order => order.paymentStatus === paymentStatusFilter.value)
  }

  if (paymentMethodFilter.value) {
    result = result.filter(order => order.paymentMethod === paymentMethodFilter.value)
  }

  const text = keyword.value.trim()

  if (text) {
    result = result.filter(order =>
      String(order.orderId).includes(text) ||
      String(order.userId).includes(text)
    )
  }

  if (sortType.value === 'NEWEST') {
    result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  }

  if (sortType.value === 'OLDEST') {
    result.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
  }

  if (sortType.value === 'AMOUNT_DESC') {
    result.sort((a, b) => Number(b.finalAmount) - Number(a.finalAmount))
  }

  if (sortType.value === 'AMOUNT_ASC') {
    result.sort((a, b) => Number(a.finalAmount) - Number(b.finalAmount))
  }

  const start = (currentPage.value - 1) * Number(pageSize.value)
  const end = start + Number(pageSize.value)

  return result.slice(start, end)
})

const filteredTotalCount = computed(() => {
  let result = [...orders.value]
  if (quickFilter.value === 'ACTIVE') {
    result = result.filter(order =>
      order.status !== 'COMPLETED' &&
      order.status !== 'CANCELLED'
    )
  }

  if (quickFilter.value === 'COMPLETED') {
    result = result.filter(order => order.status === 'COMPLETED')
  }

  if (quickFilter.value === 'UNPAID') {
    result = result.filter(order => order.paymentStatus === 'UNPAID')
  }
  


  if (statusFilter.value) {
    result = result.filter(order => order.status === statusFilter.value)
  }

  if (paymentStatusFilter.value) {
    result = result.filter(order => order.paymentStatus === paymentStatusFilter.value)
  }

  if (paymentMethodFilter.value) {
    result = result.filter(order => order.paymentMethod === paymentMethodFilter.value)
  }

  const text = keyword.value.trim()

  if (text) {
    result = result.filter(order =>
      String(order.orderId).includes(text) ||
      String(order.userId).includes(text)
    )
  }

  return result.length
})

const getStatusClass = (status) => {
  if (status === 'PENDING') return 'status-pending'
  if (status === 'CONFIRMED') return 'status-confirmed'
  if (status === 'PREPARING') return 'status-preparing'
  if (status === 'READY') return 'status-ready'
  if (status === 'COMPLETED') return 'status-completed'
  if (status === 'CANCELLED') return 'status-cancelled'
  return ''
}

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(filteredTotalCount.value / Number(pageSize.value)))
})

const goPrevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const goNextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

const resetFilters = () => {
  keyword.value = ''
  paymentStatusFilter.value = ''
  paymentMethodFilter.value = ''
  statusFilter.value = ''
  sortType.value = 'NEWEST'
  pageSize.value = 10
  currentPage.value = 1
  quickFilter.value = 'ALL'
}

const pendingCount = computed(() =>
  orders.value.filter(order => order.status === 'PENDING').length
)

const confirmedCount = computed(() =>
  orders.value.filter(order => order.status === 'CONFIRMED').length
)

const cancelledCount = computed(() =>
  orders.value.filter(order => order.status === 'CANCELLED').length
)

const activeCount = computed(() => {
  return orders.value.filter(order =>
    order.status !== 'COMPLETED' && order.status !== 'CANCELLED'
  ).length
})

const completedCount = computed(() => {
  return orders.value.filter(order => order.status === 'COMPLETED').length
})

const changeStatus = async (orderId, status) => {
  try {
    await updateAdminOrderStatus(orderId, status)
    await loadOrders()
  } catch (error) {
    console.error(error)

    const message =
      error.response?.data?.message ||
      error.response?.data ||
      '更新訂單狀態失敗'

    Swal.fire({
      icon: 'warning',
      title: '無法更新狀態',
      text: message,
      confirmButtonText: '我知道了'
    })

    await loadOrders()
  }
}
// const openDetail = (order) => {
//   console.log('有點到')
//   console.log(order)

//   selectedOrder.value = order

//   console.log(selectedOrder.value)
// }
const openDetail = async (order) => {
  selectedOrder.value = order

  try {
    const res = await getAdminOrderById(order.orderId)
    selectedOrder.value = res.data
  } catch (error) {
    console.error(error)

    Swal.fire({
      icon: 'error',
      title: '取得完整明細失敗',
      text: '目前先顯示列表資料，請稍後再試',
      confirmButtonText: '我知道了'
    })
  }
}


const formatOrderType = (type) => {
  if (type === 'DINE_IN') return '內用'
  if (type === 'TAKEOUT') return '外帶'
  if (type === 'TAKE_OUT') return '外帶'
  return type || '未設定'
}

const formatPaymentMethod = (method) => {
  if (method === 'CASH') return '現金'
  if (method === 'CREDIT_CARD') return '信用卡'
  return method || '未設定'
}

const formatOrderStatus = (status) => {
  if (status === 'PENDING') return '⏳ 待處理'
  if (status === 'CONFIRMED') return '✅ 已確認'
  if (status === 'PREPARING') return '👨‍🍳 製作中'
  if (status === 'READY') return '🔔 待取餐'
  if (status === 'COMPLETED') return '🎉 已完成'
  if (status === 'CANCELLED') return '❌ 已取消'

  return status || '未設定'
}

const formatPaymentStatus = (status) => {
  if (status === 'UNPAID') return '待付款'
  if (status === 'PAID') return '已付款'
  if (status === 'REFUNDED') return '已退款'

  return status || '未設定'
}

const formatDate = (date) => {
  if (!date) return '未設定'
  return new Date(date).toLocaleString('zh-TW')
}
</script>

<style scoped>
.summary-card.clickable {
  cursor: pointer;
  transition: 0.2s;
}

.summary-card.clickable:hover {
  transform: translateY(-2px);
}

.summary-card.active {
  border: 2px solid #e4a775;
  background: #fff8f2;
}

.status-preparing {
  background: #fffbe6;
  border-color: #ffd666;
  color: #d48806;
}

.status-confirmed {
  background: #f6ffed;
  border-color: #95de64;
  color: #237804;
}

.status-pending {
  background: #ffeaea;
  border: 1px solid #ff4d4f;
  color: #cf1322;
  font-weight: 600;
}

.status-ready {
  background: #e6f7ff;
  border-color: #91d5ff;
  color: #0050b3;
}

.status-completed {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #8c8c8c;
}

.status-cancelled {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #8c8c8c;
}

.status-select option {
  background: #fff;
  color: #344b68;
}

thead th {
  font-weight: 700;
  font-size: 16px;
}

thead tr {
  border-bottom: 2px solid #e4a775;
}

thead {
  background: #eee3d8;
}

tbody tr:nth-child(even) {
  background: #fcfaf8;
}

tbody tr {
  transition: all 0.2s ease;
}

tbody tr:hover {
  background: #f3e7db;
  border-left: 4px solid #e4a775;
}

tbody tr {
  border-left: 4px solid transparent;
}

.view-btn:hover {
  transform: translateY(-1px);
}

.search-row {
  margin-bottom: 10px;
  background: transparent;
  border: none;
  padding: 0;
}


.filter-panel {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 14px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
}

.pagination button {
  background: #e4a775;
}

.pagination button:disabled {
  background: #d1d5db;
  cursor: not-allowed;
}

.reset-btn {
  padding: 10px 18px;
  border: none;
  border-radius: 10px;
  background: #adb5bd;
  color: white;
  cursor: pointer;
  font-weight: 600;
}

.reset-btn:hover {
  background: #d5945c;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 320px;
  padding: 9px 12px;
  border: 1px solid #ead8c5;
  border-radius: 10px;
  color: #43546d;
  background: #fff;
}

.status-select:disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
  opacity: 0.8;
}

.status-select.locked {
  background: #f3f4f6;
  color: #999;
  cursor: not-allowed;
}

.lock-icon {
  margin-left: 8px;
  color: #999;
  font-size: 14px;
}

.admin-order-page {
  padding: 32px;
  color: #43546d;
}

h1 {
  margin: 0;
  color: #344b68;
}

.subtitle {
  margin-top: 8px;
  color: #718096;
}

.summary-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 18px;
  margin: 24px 0;
}

.summary-card,
.table-card {
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(100, 80, 50, 0.08);
}

.summary-card {
  padding: 24px;
}

.summary-card p {
  margin: 0;
  color: #718096;
}

.summary-card h2 {
  margin: 8px 0 0;
  font-size: 30px;
  color: #344b68;
}

.table-card {
  padding: 24px;
}

.table-header {
  margin-bottom: 18px;
}

.table-header h2 {
  margin: 0 0 18px;
  color: #344b68;
}

select {
  padding: 9px 12px;
  border: 1px solid #ead8c5;
  border-radius: 10px;
  color: #43546d;
  background: #fff;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  text-align: left;
  padding: 14px;
  background: #fff6ef;
  color: #344b68;
}

td {
  padding: 14px;
  border-bottom: 1px solid #f0e5dc;
}

button {
  border: none;
  background: #e4a775;
  color: white;
  padding: 8px 14px;
  border-radius: 10px;
  cursor: pointer;
}

button:hover {
  background: #d9945f;
}

.status-select {
  min-width: 110px;
}

.empty {
  text-align: center;
  padding: 28px;
  color: #718096;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 2147483647;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal {
  display: block !important;
  position: relative;
  background: #fff !important;
  color: #344b68;
  padding: 30px;
  border-radius: 20px;
  width: 900px;
  max-width: 95vw;
  max-height: 90vh;
  overflow-y: auto;
  z-index: 2147483647;
  opacity: 1 !important;
  visibility: visible !important;
}

.modal table {
  width: 100%;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  color: #344b68;
}

.modal-header button {
  background: transparent;
  color: #344b68;
  font-size: 28px;
  padding: 0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin: 20px 0;
}

.total-box {
  margin-top: 20px;
  text-align: right;
}

.total-box h2 {
  color: #d9945f;
}

@media (max-width: 900px) {
  .summary-row {
    grid-template-columns: 1fr;
  }

  .table-card {
    overflow-x: auto;
  }

  .modal {
    width: 90%;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>