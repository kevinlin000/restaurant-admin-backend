<script setup>
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
const message = ref("付款結果處理中...");
const orderId = route.query.orderId;

onMounted(() => {
    if (!orderId) {
        message.value = "找不到訂單編號";
        return;
    }

    message.value = "付款成功，訂單狀態已更新";
});
</script>

<template>
    <div class="container mt-5">
        <h1>付款完成</h1>
        <p>訂單編號：{{ orderId }}</p>
        <p>{{ message }}</p>
    </div>
</template>