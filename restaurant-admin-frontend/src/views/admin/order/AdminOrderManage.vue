<template>
  <div class="admin-order-page">
    <h1>訂單管理</h1>
    <p class="subtitle">查看訂單、付款狀態與出餐流程。</p>

    <div class="summary-row">
      <div class="summary-card">
        <p>全部訂單</p>
        <h2>{{ orders.length }}</h2>
      </div>

      <div class="summary-card">
        <p>進行中</p>
        <h2>{{ activeCount }}</h2>
      </div>

      <div class="summary-card">
        <p>已完成</p>
        <h2>{{ completedCount }}</h2>
      </div>
    </div>

    <div class="table-card">
      <div class="table-header">
        <h2>訂單列表</h2>

        <select v-model="statusFilter">
          <option value="">全部狀態</option>
          <option value="PENDING">待處理</option>
          <option value="CONFIRMED">已確認</option>
          <option value="PREPARING">製作中</option>
          <option value="READY">待取餐</option>
          <option value="COMPLETED">已完成</option>
          <option value="CANCELLED">已取消</option>
        </select>
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
              <select class="status-select" :value="order.status" :disabled="order.status === 'COMPLETED' ||
                order.status === 'CANCELLED'
                " @change="changeStatus(order.orderId, $event.target.value)">
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
 
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import Swal from 'sweetalert2'
import {
  getAdminOrders,
  getAdminOrderById,
  updateAdminOrderStatus
} from '@/api/orderAdminApi'

const orders = ref([])
const selectedOrder = ref(null)
const statusFilter = ref('')

onMounted(() => {
  loadOrders()
})

const isStatusLocked = (order) => {
  return order.status === 'COMPLETED' ||
    order.status === 'CANCELLED'
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
  if (!statusFilter.value) {
    return orders.value
  }

  return orders.value.filter(order => order.status === statusFilter.value)
})

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
  grid-template-columns: repeat(3, 1fr);
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.table-header h2 {
  margin: 0;
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
  width: 700px;
  max-height: 85vh;
  overflow-y: auto;
  z-index: 2147483647;
  opacity: 1 !important;
  visibility: visible !important;
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