<template>
  <div class="payment-success-page">
    <div class="success-card">
      <div class="success-icon">✓</div>
      <h1>付款成功</h1>
      <p>付款已完成，訂單已成立。</p>

      <div class="order-id">
        訂單編號：#{{ orderId || "查詢中" }}
      </div>

      <button type="button" @click="goDetail">
        查看訂單明細
      </button>

      <button type="button" class="outline" @click="goHome">
        回首頁
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

const orderId = computed(() => route.query.orderId);

const getUserInfo = () => {
  try {
    return JSON.parse(localStorage.getItem("userInfo") || "{}");
  } catch {
    return {};
  }
};

const goDetail = () => {
  const token = localStorage.getItem("accessToken");
  const userInfo = getUserInfo();
  const roleName = userInfo.roleName;

  if (!token) {
    router.push({
      name: "GuestOrderDetail",
      query: { orderId: orderId.value },
    });
    return;
  }

  if (roleName === "CUSTOMER") {
    router.push({
      name: "CustomerProfile",
      query: { tab: "orders" },
    });
    return;
  }

  router.push({
    name: "AdminOrderManage",
    query: { orderId: orderId.value },
  });
};

const goHome = () => {
  router.push({ name: "CustomerHome" });
};
</script>

<style scoped>
.payment-success-page {
  min-height: 100vh;
  padding: 150px 20px 70px;
  background: #f8f3ed;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-card {
  width: min(520px, 100%);
  padding: 44px 36px;
  border-radius: 24px;
  background: white;
  text-align: center;
  box-shadow: 0 18px 45px rgba(100, 80, 50, 0.15);
  color: #344b68;
}

.success-icon {
  width: 76px;
  height: 76px;
  margin: 0 auto 20px;
  border-radius: 50%;
  background: #e8f8ef;
  color: #22a06b;
  font-size: 42px;
  line-height: 76px;
  font-weight: 900;
}

h1 {
  margin: 0 0 12px;
  font-size: 34px;
}

p {
  margin: 10px 0;
  color: #718096;
}

.order-id {
  margin: 24px 0;
  padding: 14px;
  border-radius: 14px;
  background: #fff6ef;
  font-weight: 900;
}

button {
  display: block;
  width: 100%;
  margin-top: 12px;
  border: none;
  border-radius: 12px;
  padding: 13px 24px;
  background: #e4a775;
  color: white;
  font-weight: 900;
  cursor: pointer;
}

button.outline {
  background: #fff7ef;
  color: #d18f5e;
}
</style>