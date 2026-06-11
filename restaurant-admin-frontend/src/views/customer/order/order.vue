<script setup>
// =========================
// Vue
import { computed, ref, watch } from "vue";
import axios from "axios";


// =========================
// Images

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

// =========================
// Category Data
const categories = ref([
    { id: 1, name: "前菜" },
    { id: 2, name: "刺身" },
    { id: 3, name: "握壽司" },
    { id: 4, name: "主餐" },
    { id: 6, name: "甜點" },
    { id: 7, name: "飲品" },
    { id: 8, name: "酒類" },
]);

// 之後改成 API
// =========================
const activeCategory = ref(1);

// =========================
// Order Form

const orderForm = ref({
    userId: 1,
    storeId: 1,
    tableId: 1,
    reservationId: 1,
    orderType: "DINE_IN",
    pointsUsed: 0,
});

const step = ref("MENU");
// MENU = 點餐畫面
// CHECKOUT = 結帳確認畫面

const customerForm = ref({
    customerName: "",
    phone: "",
    title: "小姐",
    paymentMethod: "CASH",
    needTableware: false,
    agreePolicy: false,
    invoiceType: "NONE",
    carrierNumber: "",
});

watch(
    () => customerForm.value.invoiceType,
    (newValue) => {
        if (
            newValue === "MOBILE_BARCODE" &&
            !customerForm.value.carrierNumber
        ) {
            customerForm.value.carrierNumber = "/";
        }
    }
);

// =========================
// Menu Data
// 之後改成 Menu API
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
// =========================
// Cart
// 之後可搬到 Pinia

const cartItems = ref([]);

// =========================
// Computed
// =========================
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

// =========================
// Cart Functions

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

function goCheckout() {
    if (cartItems.value.length === 0) {
        alert("請先加入餐點");
        return;
    }

    step.value = "CHECKOUT";
}

function backToMenu() {
    step.value = "MENU";
}

function formatCarrier() {
    let value = customerForm.value.carrierNumber;

    value = value.toUpperCase();

    if (!value.startsWith("/")) {
        value = "/" + value.replace(/\//g, "");
    }

    value =
        "/" +
        value
            .substring(1)
            .replace(/[^0-9A-Z.+-]/g, "")
            .slice(0, 7);

    customerForm.value.carrierNumber = value;
}

async function submitOrder() {
    if (
        customerForm.value.invoiceType === "MOBILE_BARCODE" &&
        !/^\/[0-9A-Z.+-]{7}$/.test(customerForm.value.carrierNumber)
    ) {
        alert("請輸入正確手機條碼載具");
        return;
    }

    if (!customerForm.value.customerName) {
        alert("請輸入姓名");
        return;
    }

    if (!customerForm.value.customerName?.trim()) {
        alert("請輸入姓名");
        return;
    }

    if (!customerForm.value.phone) {
        alert("請輸入電話號碼");
        return;
    }
    const namePattern = /^[A-Za-z\u4e00-\u9fa5\s]{2,20}$/;

    if (!namePattern.test(customerForm.value.customerName.trim())) {
        alert("姓名格式不正確");
        return;
    }

    if (!/^09\d{8}$/.test(customerForm.value.phone)) {
        alert("請輸入正確手機號碼");
        return;
    }

    if (!customerForm.value.agreePolicy) {
        alert("請先勾選同意條款");
        return;
    }


    const request = {
        userId: orderForm.value.userId,
        storeId: orderForm.value.storeId,
        tableId: orderForm.value.tableId,
        reservationId: orderForm.value.reservationId,
        orderType: orderForm.value.orderType,
        pointsUsed: orderForm.value.pointsUsed,
        invoiceType: customerForm.value.invoiceType,
        carrierNumber: customerForm.value.carrierNumber,
        paymentMethod: customerForm.value.paymentMethod,
        items: cartItems.value.map((item) => ({
            menuItemId: item.menuItemId,
            quantity: item.quantity,
        })),
    };

    console.log("顧客資料：", customerForm.value);
    console.log("送出的訂單資料：", request);

    const response = await axios.post("/api/orders", request);
    console.log("後端回傳：", response.data);

    alert("訂單送出成功");
}
// =========================
</script>

<template>
    <!-- 1. 點餐頁 Header -->
    <main class="order-page">
        <div v-if="step === 'MENU'">
            <section class="order-header">
                <div>
                    <h1>點餐</h1>
                    <p>選擇餐點加入購物車，確認後送出訂單。</p>
                </div>

                <div class="order-type">
                    <button :class="{ active: orderForm.orderType === 'DINE_IN' }"
                        @click="orderForm.orderType = 'DINE_IN'">
                        內用
                    </button>

                    <button :class="{ active: orderForm.orderType === 'TAKE_OUT' }"
                        @click="orderForm.orderType = 'TAKE_OUT'">
                        外帶
                    </button>
                </div>
            </section>
            <!-- 2. 分類按鈕：之後可拆 CategoryTabs.vue -->
            <section class="category-tabs">
                <button v-for="category in categories" :key="category.id"
                    :class="{ active: activeCategory === category.id }" @click="activeCategory = category.id">
                    {{ category.name }}
                </button>
            </section>

            <!-- 3. 菜單列表：之後可拆 MenuList.vue / MenuCard.vue -->
            <section class="content-layout">

                <div class="menu-section">
                    <h2>{{ activeCategoryName }}</h2>

                    <div class="menu-grid">

                        <!-- Menu Card -->
                        <!-- 之後可拆 MenuCard.vue -->
                        <!-- ========================= -->
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
                <!-- 4. 購物車：之後可拆 CartPanel.vue -->
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

                    <button class="submit-btn" :disabled="cartItems.length === 0" @click="goCheckout">
                        前往結帳
                    </button>
                </aside>
            </section>
        </div>
        <section v-if="step === 'CHECKOUT'" class="checkout-page">
            <div class="checkout-summary">
                <h2>確認訂單</h2>

                <div v-for="item in cartItems" :key="item.menuItemId" class="checkout-item">
                    <span>x{{ item.quantity }}</span>
                    <strong>{{ item.itemName }}</strong>
                    <span>NT${{ item.price * item.quantity }}</span>
                </div>

                <div class="checkout-total">
                    <span>總付款金額</span>
                    <strong>NT${{ totalAmount }}</strong>
                </div>
            </div>

            <div class="checkout-form">
                <h2>確認訂單與填寫聯絡資訊</h2>

                <div class="form-card">
    <h3>付款方式</h3>

    <div class="payment-tabs">
        <button
            type="button"
            :class="{ active: customerForm.paymentMethod === 'CASH' }"
            @click="customerForm.paymentMethod = 'CASH'"
        >
            現場付款
        </button>

        <button
            type="button"
            :class="{ active: customerForm.paymentMethod === 'LINE_PAY' }"
            @click="customerForm.paymentMethod = 'LINE_PAY'"
        >
            Line Pay
        </button>

        <button
            type="button"
            :class="{ active: customerForm.paymentMethod === 'CREDIT_CARD' }"
            @click="customerForm.paymentMethod = 'CREDIT_CARD'"
        >
            信用卡
        </button>
    </div>

    <div v-if="customerForm.paymentMethod === 'CASH'" class="payment-box">
        現場付款，取餐時付款。
    </div>

    <div v-if="customerForm.paymentMethod === 'LINE_PAY'" class="payment-box">
        <p>Line Pay 掃碼付款</p>
        <div class="fake-qr">QR</div>
        <small>Demo 用：正式版會由後端金流 API 產生付款連結或 QR Code。</small>
    </div>

    <div v-if="customerForm.paymentMethod === 'CREDIT_CARD'" class="payment-box">
        <label>信用卡卡號</label>
        <input type="text" placeholder="**** **** **** ****" disabled />

        <label>有效期限</label>
        <input type="text" placeholder="MM / YY" disabled />

        <label>安全碼</label>
        <input type="text" placeholder="CVV" disabled />

        <small>Demo 用：正式版不可自己儲存信用卡資料，應導向綠界 / 藍新 / Line Pay 金流頁。</small>
    </div>

    <div class="payment-total">
        付款金額
        <strong>NT${{ totalAmount }}</strong>
    </div>
</div>

                <div class="form-card">
                    <h3>發票 / 載具</h3>

                    <label class="invoice-option">
                        <input type="radio" value="NONE" v-model="customerForm.invoiceType" />
                        不使用載具
                    </label>

                    <label class="carrier-option">
                        <input type="radio" 
                        value="MOBILE_BARCODE"
                         v-model="customerForm.invoiceType"
                        />
                        手機條碼載具

                    <input v-if="customerForm.invoiceType === 'MOBILE_BARCODE'" v-model="customerForm.carrierNumber"
                        @input="formatCarrier" type="text" placeholder="/ABC1234" maxlength="8" class="carrier-input"/>
                    </label>
                </div>

                <div class="form-card">
                    <h3>聯絡資訊</h3>

                    <label>姓名 *</label>
                    <input v-model="customerForm.customerName" type="text" />

                    <div class="radio-group">
                        <label>
                            <input type="radio" value="小姐" v-model="customerForm.title" />
                            小姐
                        </label>
                        <label>
                            <input type="radio" value="先生" v-model="customerForm.title" />
                            先生
                        </label>
                        <label>
                            <input type="radio" value="其他" v-model="customerForm.title" />
                            其他
                        </label>
                    </div>

                    <label>電話 *</label>
                    <input class="phone-input" v-model="customerForm.phone" type="tel" placeholder="0912345678"
                        maxlength="10" />
                </div>

                <div class="form-card">
                    <label>
                        <input type="checkbox" v-model="customerForm.needTableware" />
                        需要免洗餐具或吸管
                    </label>
                </div>

                <div class="policy-box">
                    <label>
                        <input type="checkbox" v-model="customerForm.agreePolicy" />
                        我已同意訂單成立後無法任意取消
                    </label>
                </div>

                <button class="submit-btn" @click="submitOrder">
                    送訂單
                </button>

                <button class="back-btn" @click="backToMenu">
                    回上一步
                </button>
            </div>
        </section>
    </main>
</template>

<style scoped>
.payment-tabs {
    display: flex;
    gap: 10px;
    margin-bottom: 16px;
}

.payment-tabs button {
    flex: 1;
    padding: 10px;
    border: 1px solid #ddd;
    background: white;
    cursor: pointer;
}

.payment-tabs button.active {
    background: #e9ad75;
    color: white;
    border-color: #e9ad75;
}

.payment-box {
    margin-top: 12px;
    padding: 16px;
    border: 1px solid #eee;
    border-radius: 10px;
    background: #fafafa;
}

.payment-box input {
    width: 100%;
    margin: 6px 0 12px;
}

.fake-qr {
    width: 140px;
    height: 140px;
    border: 2px solid #333;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    margin: 12px 0;
}
.invoice-option,
.carrier-option {
    display: grid;
    grid-template-columns: 16px 100px 1fr;
    align-items: center;
    gap: 10px;
    margin-top: 12px;
}

.carrier-input {
    flex: 1;
    width: 100%;
}
.phone-input::placeholder {
    color: #bdbdbd;
}

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

.checkout-page {
    max-width: 1280px;
    margin: 40px auto;
    padding: 24px;
    display: grid;
    grid-template-columns: 380px 1fr;
    gap: 32px;
    color: #40566f;
}

.checkout-summary,
.checkout-form .form-card,
.policy-box {
    background: white;
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 8px 22px rgba(0, 0, 0, 0.08);
}

.checkout-summary h2,
.checkout-form h2 {
    margin-bottom: 24px;
}

.checkout-item {
    display: grid;
    grid-template-columns: 50px 1fr auto;
    gap: 12px;
    padding: 14px 0;
    border-bottom: 1px solid #eee;
}

.checkout-total {
    margin-top: 24px;
    display: flex;
    justify-content: space-between;
    font-size: 22px;
}

.checkout-form {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.form-card h3 {
    margin-bottom: 16px;
}

.form-card input[type="text"],
.form-card input[type="tel"] {
    width: 100%;
    padding: 12px;
    margin: 8px 0 16px;
    border: 1px solid #ddd;
    border-radius: 8px;
}

.radio-group {
    display: flex;
    gap: 20px;
    margin-bottom: 16px;
}

.payment-total {
    margin-top: 20px;
    display: flex;
    justify-content: space-between;
}

.back-btn {
    width: 100%;
    border: 1px solid #ddd;
    background: white;
    color: #40566f;
    padding: 14px;
    border-radius: 12px;
    font-size: 18px;
    cursor: pointer;
}
</style>