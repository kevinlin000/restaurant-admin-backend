<script setup>
import { computed, ref } from "vue";
import axios from "axios";
import tofuImg from "@/assets/images/tofu.jpg";
import seafoodSaladImg from "@/assets/images/seafood-salad.jpg";

import sashimiImg from "@/assets/images/sashimi.jpg";
import salmonSashimiImg from "@/assets/images/salmon-sashimi.jpg";

import sushiImg from "@/assets/images/sushi.jpg";
import aburiSalmonSushiImg from "@/assets/images/aburi-salmon-sushi.jpg";

import sukiyakiImg from "@/assets/images/sukiyaki.jpg";
import tempuraImg from "@/assets/images/tempura.jpg";

import matchaDessertImg from "@/assets/images/matcha-dessert.jpg";
import caramelPuddingImg from "@/assets/images/caramel-pudding.jpg";

import calpisImg from "@/assets/images/calpis.jpg";
import japaneseTeaImg from "@/assets/images/japanese-tea.jpg";

import asahiBeerImg from "@/assets/images/asahi-beer.jpg";
import japaneseSakeImg from "@/assets/images/japanese-sake.jpg";

const categories = ref([
    { id: 1, name: "前菜" },
    { id: 2, name: "刺身" },
    { id: 3, name: "握壽司" },
    { id: 4, name: "主餐" },
    { id: 6, name: "甜點" },
    { id: 7, name: "飲品" },
    { id: 8, name: "酒類" },
]);

const activeCategory = ref(1);

const orderForm = ref({
    userId: 1,
    storeId: 1,
    tableId: 1,
    reservationId: 1,
    orderType: "DINE_IN",
    pointsUsed: 0,
});

const menuItems = ref([
    {
        id: 1,
        categoryId: 1,
        itemName: "胡麻豆腐",
        description: "手工研磨胡麻醬，搭配嫩滑豆腐",
        price: 80,
        imageUrl: tofuImg,
        status: "AVAILABLE",
        allergenInfo: "含堅果、大豆",
    },
    {
        id: 2,
        categoryId: 1,
        itemName: "海鮮沙拉",
        description: "新鮮時蔬搭配鮮蝦、花枝",
        price: 180,
        imageUrl: seafoodSaladImg,
        status: "AVAILABLE",
        allergenInfo: "含甲殼類",
    },
    {
        id: 3,
        categoryId: 2,
        itemName: "綜合生魚片",
        description: "每日嚴選新鮮漁獲，主廚搭配",
        price: 580,
        imageUrl: sashimiImg,
        status: "AVAILABLE",
        allergenInfo: "含生食",
    },
    {
        id: 4,
        categoryId: 2,
        itemName: "鮭魚刺身",
        description: "挪威鮭魚薄切",
        price: 320,
        imageUrl: salmonSashimiImg,
        status: "AVAILABLE",
        allergenInfo: "含生食",
    },
    {
        id: 5,
        categoryId: 3,
        itemName: "握壽司盛合",
        description: "主廚推薦 8 貫握壽司",
        price: 520,
        imageUrl: sushiImg,
        status: "AVAILABLE",
        allergenInfo: "含生食",
    },
    {
        id: 6,
        categoryId: 3,
        itemName: "炙燒鮭魚壽司",
        description: "炙燒表面焦香，入口即化",
        price: 260,
        imageUrl: aburiSalmonSushiImg,
        status: "AVAILABLE",
        allergenInfo: "含生食",
    },
    {
        id: 7,
        categoryId: 4,
        itemName: "和牛壽喜燒",
        description: "澳洲和牛搭配特製壽喜燒醬汁",
        price: 680,
        imageUrl: sukiyakiImg,
        status: "AVAILABLE",
        allergenInfo: "含大豆",
    },
    {
        id: 8,
        categoryId: 4,
        itemName: "天婦羅拼盤",
        description: "嚴選蝦、蔬菜酥炸",
        price: 380,
        imageUrl: tempuraImg,
        status: "AVAILABLE",
        allergenInfo: "含麩質、甲殼類",
    },
    {
        id: 9,
        categoryId: 6,
        itemName: "抹茶提拉米蘇",
        description: "宇治抹茶搭配 Mascarpone",
        price: 180,
        imageUrl: matchaDessertImg,
        status: "AVAILABLE",
        allergenInfo: "含乳製品、蛋",
    },
    {
        id: 10,
        categoryId: 6,
        itemName: "焦糖布丁",
        description: "法式經典焦糖布丁",
        price: 120,
        imageUrl: caramelPuddingImg,
        status: "AVAILABLE",
        allergenInfo: "含乳製品、蛋",
    },
    {
        id: 11,
        categoryId: 7,
        itemName: "可爾必思",
        description: "日本原裝進口",
        price: 90,
        imageUrl: calpisImg,
        status: "AVAILABLE",
        allergenInfo: "含乳製品",
    },
    {
        id: 12,
        categoryId: 7,
        itemName: "烏龍茶",
        description: "台灣高山烏龍",
        price: 80,
        imageUrl: japaneseTeaImg,
        status: "AVAILABLE",
        allergenInfo: "無",
    },
    {
        id: 13,
        categoryId: 8,
        itemName: "朝日生啤",
        description: "日本直送 350ml",
        price: 150,
        imageUrl: asahiBeerImg,
        status: "AVAILABLE",
        allergenInfo: "無",
    },
    {
        id: 14,
        categoryId: 8,
        itemName: "獺祭純米大吟釀",
        description: "山口縣產，一合",
        price: 380,
        imageUrl: japaneseSakeImg,
        status: "AVAILABLE",
        allergenInfo: "無",
    },
]);
const cartItems = ref([]);

const filteredMenuItems = computed(() => {
    return menuItems.value.filter(
        (item) =>
            item.categoryId === activeCategory.value &&
            item.status === "AVAILABLE"
    );
});

const activeCategoryName = computed(() => {
    const category = categories.value.find(
        (item) => item.id === activeCategory.value
    );

    return category ? category.name : "";
});

const totalAmount = computed(() => {
    return cartItems.value.reduce((sum, item) => {
        return sum + item.price * item.quantity;
    }, 0);
});

function addItem(menuItem) {
    const existItem = cartItems.value.find(
        (item) => item.menuItemId === menuItem.id
    );

    if (existItem) {
        existItem.quantity += 1;
    } else {
        cartItems.value.push({
            menuItemId: menuItem.id,
            categoryId: menuItem.categoryId,
            itemName: menuItem.itemName,
            price: menuItem.price,
            imageUrl: menuItem.imageUrl,
            allergenInfo: menuItem.allergenInfo,
            quantity: 1,
            note: "",
        });
    }
}

function increaseQuantity(item) {
    item.quantity += 1;
}

function decreaseQuantity(item) {
    if (item.quantity > 1) {
        item.quantity -= 1;
    } else {
        removeItem(item.menuItemId);
    }
}

function removeItem(menuItemId) {
    cartItems.value = cartItems.value.filter(
        (item) => item.menuItemId !== menuItemId
    );
}

async function submitOrder() {
    const request = {
        userId: orderForm.value.userId,
        storeId: orderForm.value.storeId,
        tableId: orderForm.value.tableId,
        reservationId: orderForm.value.reservationId,
        orderType: orderForm.value.orderType,
        pointsUsed: orderForm.value.pointsUsed,
        items: cartItems.value.map((item) => ({
            menuItemId: item.menuItemId,
            quantity: item.quantity,
        })),
    };

    console.log("送出的訂單資料：", request);

    const response = await axios.post("/api/orders", request);
    console.log("後端回傳：", response.data);
}
</script>

<template>
    <main class="order-page">
        <section class="order-header">
            <div>
                <h1>點餐</h1>
                <p>選擇餐點加入購物車，確認後送出訂單。</p>
            </div>

            <div class="order-type">
                <button :class="{ active: orderForm.orderType === 'DINE_IN' }" @click="orderForm.orderType = 'DINE_IN'">
                    內用
                </button>

                <button :class="{ active: orderForm.orderType === 'TAKE_OUT' }"
                    @click="orderForm.orderType = 'TAKE_OUT'">
                    外帶
                </button>
            </div>
        </section>

        <section class="category-tabs">
            <button v-for="category in categories" :key="category.id"
                :class="{ active: activeCategory === category.id }" @click="activeCategory = category.id">
                {{ category.name }}
            </button>
        </section>

        <section class="content-layout">
            <div class="menu-section">
                <h2>{{ activeCategoryName }}</h2>

                <div class="menu-grid">
                    <article v-for="item in filteredMenuItems" :key="item.id" class="menu-card">
                        <div class="menu-info">
                            <span class="menu-id">#{{ item.id }}</span>

                            <h3>{{ item.itemName }}</h3>

                            <p class="description">
                                {{ item.description }}
                            </p>

                            <p class="category">
                                分類：
                                {{
                                    categories.find((category) => category.id === item.categoryId)
                                        ?.name
                                }}
                            </p>

                            <p class="status">狀態：{{ item.status }}</p>

                            <small class="allergen">
                                過敏原：{{ item.allergenInfo }}
                            </small>

                            <div class="menu-bottom">
                                <button @click="addItem(item)">＋</button>
                                <strong>NT${{ item.price }}</strong>
                            </div>
                        </div>

                        <div class="image-wrapper">
                            <img :src="item.imageUrl" :alt="item.itemName" />
                        </div>
                    </article>
                </div>
            </div>

            <aside class="cart-section">
                <h2>您的訂單</h2>

                <div v-if="cartItems.length === 0" class="empty-cart">
                    尚未加入餐點
                </div>

                <div v-else class="cart-list">
                    <div v-for="item in cartItems" :key="item.menuItemId" class="cart-item">
                        <img :src="item.imageUrl" :alt="item.itemName" />

                        <div class="cart-info">
                            <small>#{{ item.menuItemId }}</small>

                            <h4>{{ item.itemName }}</h4>

                            <p>NT${{ item.price }}</p>

                            <small>過敏原：{{ item.allergenInfo }}</small>

                            <textarea v-model="item.note" placeholder="餐點備註，例如：不要蔥、少辣"></textarea>
                        </div>

                        <div class="quantity-control">
                            <button @click="decreaseQuantity(item)">－</button>
                            <span>{{ item.quantity }}</span>
                            <button @click="increaseQuantity(item)">＋</button>
                        </div>

                        <div class="cart-price">
                            NT${{ item.price * item.quantity }}
                            <button @click="removeItem(item.menuItemId)">移除</button>
                        </div>
                    </div>
                </div>

                <div class="points-box">
                    <label>使用點數</label>
                    <input v-model.number="orderForm.pointsUsed" type="number" min="0" />
                </div>

                <div class="total-box">
                    <span>預估總金額</span>
                    <strong>NT${{ totalAmount }}</strong>
                </div>

                <button class="submit-btn" :disabled="cartItems.length === 0" @click="submitOrder">
                    送出訂單
                </button>
            </aside>
        </section>
    </main>
</template>

<style scoped>
.order-page {
    max-width: 1280px;
    margin: 40px auto;
    padding: 24px;
    color: #40566f;
}

.order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.order-header h1 {
    font-size: 36px;
    margin-bottom: 8px;
}

.order-type {
    display: flex;
    gap: 12px;
}

.order-type button,
.category-tabs button {
    border: 1px solid #e8ad78;
    background: white;
    color: #40566f;
    padding: 10px 22px;
    border-radius: 999px;
    cursor: pointer;
    font-weight: 700;
}

.order-type button.active,
.category-tabs button.active {
    background: #e8ad78;
    color: white;
}

.category-tabs {
    display: flex;
    gap: 12px;
    margin-bottom: 32px;
    flex-wrap: wrap;
}

.content-layout {
    display: grid;
    grid-template-columns: 1fr 360px;
    gap: 32px;
    align-items: start;
}

.menu-section h2,
.cart-section h2 {
    margin-bottom: 20px;
}

.menu-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 24px;
}

.menu-card {
    display: grid;
    grid-template-columns: 1fr 210px;
    gap: 18px;
    background: white;
    border-radius: 16px;
    padding: 20px;
    box-shadow: 0 8px 22px rgba(0, 0, 0, 0.08);
}

.image-wrapper {
    width: 210px;
    height: 150px;
    border-radius: 12px;
    overflow: hidden;
}

.image-wrapper img,
.cart-item img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.menu-info {
    display: flex;
    flex-direction: column;
}

.menu-id,
.category,
.status,
.allergen {
    font-size: 13px;
    color: #888;
}

.menu-info h3 {
    font-size: 22px;
    margin: 8px 0 10px;
}

.description {
    line-height: 1.7;
    color: #667;
}

.menu-bottom {
    margin-top: auto;
    display: flex;
    align-items: center;
    gap: 12px;
}

.menu-bottom button {
    width: 34px;
    height: 34px;
    border: none;
    background: #ff8500;
    color: white;
    font-size: 22px;
    cursor: pointer;
}

.cart-section {
    position: sticky;
    top: 24px;
    background: white;
    border-radius: 18px;
    padding: 24px;
    box-shadow: 0 8px 22px rgba(0, 0, 0, 0.08);
}

.empty-cart {
    color: #888;
    padding: 24px 0;
}

.cart-list {
    display: flex;
    flex-direction: column;
    gap: 18px;
}

.cart-item {
    display: grid;
    grid-template-columns: 58px 1fr;
    gap: 12px;
    border-bottom: 1px solid #eee;
    padding-bottom: 16px;
}

.cart-item img {
    width: 58px;
    height: 58px;
    border-radius: 8px;
}

.cart-info h4 {
    margin: 4px 0;
}

.cart-info textarea {
    width: 100%;
    margin-top: 8px;
    padding: 8px;
    resize: vertical;
    border: 1px solid #ddd;
    border-radius: 8px;
}

.quantity-control {
    grid-column: 2;
    display: flex;
    align-items: center;
    gap: 12px;
}

.quantity-control button {
    width: 28px;
    height: 28px;
    border: 1px solid #ddd;
    background: #fff;
    cursor: pointer;
}

.cart-price {
    grid-column: 2;
    display: flex;
    justify-content: space-between;
    font-weight: 700;
}

.cart-price button {
    border: none;
    background: transparent;
    color: #ff8500;
    cursor: pointer;
}

.points-box {
    margin-top: 24px;
}

.points-box label {
    display: block;
    margin-bottom: 8px;
    font-weight: 700;
}

.points-box input {
    width: 100%;
    padding: 10px;
}

.total-box {
    margin-top: 20px;
    display: flex;
    justify-content: space-between;
    font-size: 20px;
}

.submit-btn {
    margin-top: 24px;
    width: 100%;
    border: none;
    background: #e8ad78;
    color: white;
    padding: 14px;
    border-radius: 12px;
    font-size: 18px;
    cursor: pointer;
}

.submit-btn:disabled {
    background: #ccc;
    cursor: not-allowed;
}
</style>