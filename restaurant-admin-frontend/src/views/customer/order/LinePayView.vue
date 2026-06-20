<script setup>
import { useRoute } from "vue-router";

const route = useRoute();
const orderId = route.params.orderId;
</script>

<template>
    <h1>LINE Pay 測試付款</h1>

    <p>訂單編號：{{ orderId }}</p>

    <button>
        模擬付款成功
    </button>
</template>