<script setup>
import { computed, onMounted, ref } from "vue";
import {
  getDashboardSummary,
  getDailyRevenue,
  getTopMenuItems,
  getPaymentMethodRatio,
  getOrderStatusRatio,
} from "@/api/dashboardApi";

const summary = ref({
  todayRevenue: 0,
  monthRevenue: 0,
  todayOrders: 0,
  averageOrderAmount: 0,
});

const dailyRevenue = ref([]);
const topMenuItems = ref([]);
const paymentMethodRatio = ref([]);
const orderStatusRatio = ref([]);

const loading = ref(false);
const errorMessage = ref("");

const formatMoney = (value) => {
  return `$${Number(value || 0).toLocaleString()}`;
};

const formatDate = (date) => {
  if (!date) return "-";
  return date.slice(5).replace("-", "/");
};

const paymentText = (method) => {
  const map = {
    CASH: "現金",
    CREDIT_CARD: "信用卡",
    LINE_PAY: "LINE Pay",
  };
  return map[method] || method || "-";
};

const statusText = (status) => {
  const map = {
    PENDING: "待處理",
    CONFIRMED: "已確認",
    PREPARING: "製作中",
    READY: "可取餐",
    COMPLETED: "已完成",
    CANCELLED: "已取消",
  };
  return map[status] || status || "-";
};

const maxRevenue = computed(() => {
  return Math.max(...dailyRevenue.value.map((item) => Number(item.revenue || 0)), 1);
});

const maxQuantity = computed(() => {
  return Math.max(...topMenuItems.value.map((item) => Number(item.quantity || 0)), 1);
});

const paymentTotal = computed(() => {
  return paymentMethodRatio.value.reduce((sum, item) => sum + Number(item.count || 0), 0);
});

const statusTotal = computed(() => {
  return orderStatusRatio.value.reduce((sum, item) => sum + Number(item.count || 0), 0);
});

const getPercent = (count, total) => {
  if (!total) return 0;
  return Math.round((Number(count || 0) / total) * 100);
};

const loadDashboard = async () => {
  loading.value = true;
  errorMessage.value = "";

  try {
    const [summaryRes, dailyRes, topMenuRes, paymentRes, statusRes] =
      await Promise.all([
        getDashboardSummary(),
        getDailyRevenue(),
        getTopMenuItems(),
        getPaymentMethodRatio(),
        getOrderStatusRatio(),
      ]);
   
    summary.value = summaryRes || {
      todayRevenue: 0,
      monthRevenue: 0,
      todayOrders: 0,
      averageOrderAmount: 0,
    };

    dailyRevenue.value = dailyRes || [];
    topMenuItems.value = topMenuRes || [];
    paymentMethodRatio.value = paymentRes || [];
    orderStatusRatio.value = statusRes || [];
  } catch (error) {
    console.error("載入營收分析失敗", error);
    errorMessage.value = "營收分析資料載入失敗，請稍後再試。";
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadDashboard();
});
</script>

<template>
  <div class="dashboard-page">
    <div class="page-header">
      <div>
        <h1>營收分析</h1>
        <p>查看營業額、訂單數、付款方式與熱門餐點排行。</p>
      </div>

      <button class="refresh-btn" type="button" @click="loadDashboard">
        重新整理
      </button>
    </div>

    <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

    <div class="summary-grid">
      <div class="summary-card">
        <p>今日營收</p>
        <h2>{{ formatMoney(summary.todayRevenue) }}</h2>
      </div>

      <div class="summary-card">
        <p>本月營收</p>
        <h2>{{ formatMoney(summary.monthRevenue) }}</h2>
      </div>

      <div class="summary-card">
        <p>今日完成訂單</p>
        <h2>{{ summary.todayOrders }} 筆</h2>
      </div>

      <div class="summary-card">
        <p>平均客單價</p>
        <h2>{{ formatMoney(summary.averageOrderAmount) }}</h2>
      </div>
    </div>

    <div v-if="loading" class="loading-box">資料載入中...</div>

    <template v-else>
      <section class="panel">
        <div class="panel-header">
          <h2>每日營業額</h2>
          <span>近 7 日完成訂單營收</span>
        </div>

        <div v-if="dailyRevenue.length" class="bar-chart">
          <div v-for="item in dailyRevenue" :key="item.date" class="bar-item">
            <div class="bar-value">{{ formatMoney(item.revenue) }}</div>
            <div class="bar-track">
              <div class="bar-fill"
                :style="{ height: `${Math.max((Number(item.revenue || 0) / maxRevenue) * 100, 4)}%` }"></div>
            </div>
            <div class="bar-label">{{ formatDate(item.date) }}</div>
          </div>
        </div>

        <div v-else class="empty-box">目前沒有營收資料。</div>
      </section>

      <div class="two-column">
        <section class="panel">
          <div class="panel-header">
            <h2>付款方式比例</h2>
            <span>本月訂單付款方式</span>
          </div>

          <div v-if="paymentMethodRatio.length" class="ratio-list">
            <div v-for="item in paymentMethodRatio" :key="item.paymentMethod" class="ratio-item">
              <div class="ratio-name">{{ paymentText(item.paymentMethod) }}</div>
              <div class="ratio-track">
                <div class="ratio-fill" :style="{ width: `${getPercent(item.count, paymentTotal)}%` }"></div>
              </div>
              <div class="ratio-count">
                {{ item.count }} 筆 / {{ getPercent(item.count, paymentTotal) }}%
              </div>
            </div>
          </div>

          <div v-else class="empty-box">目前沒有付款資料。</div>
        </section>

        <section class="panel">
          <div class="panel-header">
            <h2>訂單狀態比例</h2>
            <span>本月訂單狀態分布</span>
          </div>

          <div v-if="orderStatusRatio.length" class="ratio-list">
            <div v-for="item in orderStatusRatio" :key="item.status" class="ratio-item">
              <div class="ratio-name">{{ statusText(item.status) }}</div>
              <div class="ratio-track">
                <div class="ratio-fill" :style="{ width: `${getPercent(item.count, statusTotal)}%` }"></div>
              </div>
              <div class="ratio-count">
                {{ item.count }} 筆 / {{ getPercent(item.count, statusTotal) }}%
              </div>
            </div>
          </div>

          <div v-else class="empty-box">目前沒有狀態資料。</div>
        </section>
      </div>

      <section class="panel">
        <div class="panel-header">
          <h2>熱門餐點排行</h2>
          <span>本月銷售數量 Top 10</span>
        </div>

        <div v-if="topMenuItems.length" class="top-list">
          <div v-for="(item, index) in topMenuItems" :key="item.itemName" class="top-item">
            <div class="rank">{{ index + 1 }}</div>
            <div class="top-name">{{ item.itemName }}</div>
            <div class="top-bar">
              <div class="top-fill"
                :style="{ width: `${Math.max((Number(item.quantity || 0) / maxQuantity) * 100, 6)}%` }"></div>
            </div>
            <div class="top-count">{{ item.quantity }} 份</div>
          </div>
        </div>

        <div v-else class="empty-box">目前沒有餐點銷售資料。</div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.dashboard-page {
  padding: 8px 0 28px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px;
  color: #2f4b6c;
  font-size: 34px;
  font-weight: 900;
}

.page-header p {
  margin: 0;
  color: #7f8fa0;
  font-weight: 700;
}

.refresh-btn {
  border: none;
  border-radius: 12px;
  padding: 12px 20px;
  background: #e3ac7f;
  color: #fff;
  font-weight: 900;
  cursor: pointer;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.summary-card,
.panel {
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.05);
}

.summary-card {
  padding: 24px;
  border: 1px solid #f1d8c4;
}

.summary-card p {
  margin: 0 0 12px;
  color: #009944;
  font-size: 15px;
  font-weight: 900;
}

.summary-card h2 {
  margin: 0;
  color: #2f4b6c;
  font-size: 30px;
  font-weight: 900;
}

.panel {
  padding: 26px 28px;
  margin-bottom: 20px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 22px;
}

.panel-header h2 {
  margin: 0;
  color: #2f4b6c;
  font-size: 24px;
  font-weight: 900;
}

.panel-header span {
  color: #8a99a8;
  font-weight: 800;
}

.bar-chart {
  display: flex;
  align-items: flex-end;
  gap: 18px;
  height: 260px;
  padding-top: 24px;
}

.bar-item {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.bar-value {
  margin-bottom: 8px;
  color: #566a7f;
  font-size: 13px;
  font-weight: 800;
}

.bar-track {
  flex: 1;
  width: 42px;
  display: flex;
  align-items: flex-end;
  border-radius: 999px;
  background: #fff1e5;
  overflow: hidden;
}

.bar-fill {
  width: 100%;
  border-radius: 999px 999px 0 0;
  background: linear-gradient(180deg, #e3ac7f, #d88b4c);
}

.bar-label {
  margin-top: 10px;
  color: #7f8fa0;
  font-size: 13px;
  font-weight: 800;
}

.two-column {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.ratio-list,
.top-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ratio-item {
  display: grid;
  grid-template-columns: 90px 1fr 120px;
  align-items: center;
  gap: 12px;
}

.ratio-name,
.ratio-count,
.top-name,
.top-count {
  color: #566a7f;
  font-weight: 800;
}

.ratio-count,
.top-count {
  text-align: right;
  font-size: 14px;
}

.ratio-track,
.top-bar {
  height: 12px;
  border-radius: 999px;
  background: #fff1e5;
  overflow: hidden;
}

.ratio-fill,
.top-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #e3ac7f, #d88b4c);
}

.top-item {
  display: grid;
  grid-template-columns: 40px 160px 1fr 80px;
  align-items: center;
  gap: 12px;
}

.rank {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #fff;
  background: #e3ac7f;
  font-weight: 900;
}

.loading-box,
.empty-box,
.error-message {
  padding: 18px 20px;
  border-radius: 14px;
  font-weight: 800;
}

.loading-box,
.empty-box {
  color: #7f8fa0;
  background: #fff;
}

.error-message {
  margin-bottom: 16px;
  color: #c0392b;
  background: #fff1f1;
}

@media (max-width: 1200px) {

  .summary-grid,
  .two-column {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .top-item {
    grid-template-columns: 40px 120px 1fr 70px;
  }
}

@media (max-width: 768px) {

  .summary-grid,
  .two-column {
    grid-template-columns: 1fr;
  }

  .page-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 14px;
  }

  .bar-chart {
    overflow-x: auto;
  }

  .bar-item {
    min-width: 70px;
  }

  .ratio-item,
  .top-item {
    grid-template-columns: 1fr;
  }

  .ratio-count,
  .top-count {
    text-align: left;
  }
}
</style>