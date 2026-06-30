<template>
  <div class="guest-order-page">
    <div class="detail-card">
      <div v-if="loading" class="state-box">載入訂單中...</div>

      <div v-else-if="errorMsg" class="state-box error">
        {{ errorMsg }}
      </div>

      <template v-else>
        <div class="success-icon">✓</div>
        <h1>訂單明細</h1>
        <p class="order-no">訂單編號：#{{ order.orderId }}</p>

        <div class="info-grid">
          <div>
            <span>訂單類型</span>
            <strong>{{ formatOrderType(order.orderType) }}</strong>
          </div>

          <div>
            <span>付款方式</span>
            <strong>{{ formatPaymentMethod(order.paymentMethod) }}</strong>
          </div>

          <div>
            <span>付款狀態</span>
            <strong>{{ formatPaymentStatus(order.paymentStatus) }}</strong>
          </div>

          <div>
            <span>訂單狀態</span>
            <strong>{{ formatOrderStatus(order.status) }}</strong>
          </div>

          <div>
            <span>門市</span>
            <strong>{{ order.storeName || `門市 #${order.storeId}` }}</strong>
          </div>

          <div v-if="order.tableNumber || order.tableId">
            <span>桌號</span>
            <strong>{{ order.tableNumber || order.tableId }}</strong>
          </div>

          <div>
            <span>建立時間</span>
            <strong>{{ formatDate(order.createdAt) }}</strong>
          </div>
        </div>

        <h2>餐點內容</h2>

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
            <tr v-for="item in order.items || []" :key="item.menuItemId">
              <td>{{ item.itemName }}</td>
              <td>{{ item.quantity }}</td>
              <td>NT${{ item.unitPrice }}</td>
              <td>NT${{ item.subtotal }}</td>
            </tr>
          </tbody>
        </table>

        <div class="total-box">
          <p>餐點總額：NT${{ order.totalAmount }}</p>
          <p>點數折抵：NT${{ order.pointsUsed || 0 }}</p>
          <h2>實付金額：NT${{ order.finalAmount }}</h2>
        </div>

        <div class="actions">
          <button type="button" @click="goHome">回首頁</button>
          <button type="button" class="outline" @click="goOrder">繼續點餐</button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "axios";

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const errorMsg = ref("");
const order = ref({});

onMounted(async () => {
  const orderId = route.query.orderId;

  if (!orderId) {
    errorMsg.value = "缺少訂單編號";
    loading.value = false;
    return;
  }

  try {
    const res = await axios.get(`/api/orders/${orderId}`);
    order.value = res.data?.data ?? res.data;
  } catch (error) {
    console.error(error);
    errorMsg.value =
      error.response?.data?.message || "查詢訂單失敗，請稍後再試";
  } finally {
    loading.value = false;
  }
});

const goHome = () => {
  router.push({ name: "CustomerHome" });
};

const goOrder = () => {
  router.push({ name: "CustomerOrder" });
};

const formatOrderType = (type) => {
  if (type === "DINE_IN") return "內用";
  if (type === "TAKEOUT" || type === "TAKE_OUT") return "外帶";
  return type || "未設定";
};

const formatPaymentMethod = (method) => {
  if (method === "CASH") return "現金";
  if (method === "CREDIT_CARD") return "信用卡";
  if (method === "LINE_PAY") return "LINE Pay";
  return method || "未設定";
};

const formatPaymentStatus = (status) => {
  if (status === "UNPAID") return "待付款";
  if (status === "PAID") return "已付款";
  if (status === "REFUNDED") return "已退款";
  return status || "未設定";
};

const formatOrderStatus = (status) => {
  if (status === "PENDING") return "待處理";
  if (status === "CONFIRMED") return "已確認";
  if (status === "PREPARING") return "製作中";
  if (status === "READY") return "待取餐";
  if (status === "COMPLETED") return "已完成";
  if (status === "CANCELLED") return "已取消";
  return status || "未設定";
};

const formatDate = (date) => {
  if (!date) return "未設定";
  return new Date(date).toLocaleString("zh-TW");
};
</script>

<style scoped>
.guest-order-page {
  min-height: 100vh;
  padding: 150px 20px 70px;
  background: #f8f3ed;
}

.detail-card {
  width: min(900px, 100%);
  margin: 0 auto;
  padding: 42px;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 18px 45px rgba(90, 70, 45, 0.12);
  color: #344b68;
}

.success-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 18px;
  border-radius: 50%;
  background: #e7f8ec;
  color: #2e9f5e;
  font-size: 42px;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
}

h1 {
  text-align: center;
  margin: 0;
  font-size: 34px;
}

.order-no {
  margin: 14px auto 28px;
  padding: 16px;
  border-radius: 14px;
  background: #fff7ef;
  text-align: center;
  font-size: 20px;
  font-weight: 900;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
  margin-bottom: 30px;
}

.info-grid div {
  padding: 16px;
  border: 1px solid #f0e2d5;
  border-radius: 14px;
  background: #fffdfb;
}

.info-grid span {
  display: block;
  margin-bottom: 8px;
  color: #8a99a8;
  font-size: 14px;
  font-weight: 900;
}

.info-grid strong {
  color: #344b68;
}

h2 {
  margin: 28px 0 14px;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  background: #fff6ef;
  color: #344b68;
}

th,
td {
  padding: 14px;
  border-bottom: 1px solid #f0e2d5;
  text-align: left;
}

.total-box {
  margin-top: 24px;
  text-align: right;
}

.total-box p {
  margin: 8px 0;
  color: #566a7f;
}

.total-box h2 {
  color: #d9945f;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 14px;
  margin-top: 34px;
}

button {
  border: none;
  border-radius: 12px;
  padding: 12px 22px;
  background: #e4a775;
  color: #fff;
  font-weight: 900;
  cursor: pointer;
}

button.outline {
  background: #fff7ef;
  color: #d18f5e;
}

.state-box {
  padding: 50px;
  text-align: center;
  color: #8a99a8;
  font-weight: 900;
}

.state-box.error {
  color: #c0392b;
}

@media (max-width: 700px) {
  .detail-card {
    padding: 28px 18px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>