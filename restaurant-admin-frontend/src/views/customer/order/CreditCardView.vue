<script setup>
import { useRoute } from "vue-router";

const route = useRoute();
const orderId = route.params.orderId;

function paySuccess() {
  alert("模擬信用卡付款成功");
}
</script>

<template>
  <div class="container mt-5">
    <h1>信用卡付款</h1>

    <p>訂單編號：{{ orderId }}</p>

    <div class="mb-3">
      <label class="form-label">信用卡號</label>
      <input
        type="text"
        class="form-control"
        placeholder="4111111111111111"
      />
    </div>

    <div class="mb-3">
      <label class="form-label">到期日</label>
      <input
        type="text"
        class="form-control"
        placeholder="12/30"
      />
    </div>

    <div class="mb-3">
      <label class="form-label">CVV</label>
      <input
        type="text"
        class="form-control"
        placeholder="123"
      />
    </div>

    <button
      class="btn btn-primary"
      @click="paySuccess"
    >
      模擬付款成功
    </button>
  </div>
</template>