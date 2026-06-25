<script setup>
// =========================
// Vue
import { computed, onMounted, ref, watch } from "vue";
import axios from "axios";
import { useRoute, useRouter } from "vue-router";
import Swal from "sweetalert2";
//Router
const router = useRouter();
const route = useRoute();

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
const firstQueryValue = (value) => (Array.isArray(value) ? value[0] : value);

const parsePositiveId = (value, fallback = null) => {
    const parsed = Number(firstQueryValue(value));
    return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback;
};

const normalizeRouteOrderType = (value) => {
    const normalized = String(firstQueryValue(value) || "TAKEOUT").trim().toUpperCase();
    if (normalized === "TAKE_OUT") return "TAKEOUT";
    return normalized === "DINE_IN" ? "DINE_IN" : "TAKEOUT";
};

const orderForm = ref({
    userId: 1,
    storeId: parsePositiveId(route.query.storeId, 1),
    tableId: parsePositiveId(route.query.tableId),
    reservationId: parsePositiveId(route.query.reservationId),
    orderType: normalizeRouteOrderType(route.query.orderType),
    pointsUsed: 0,
});

const selectedStoreName = computed(() => firstQueryValue(route.query.storeName) || `門市 ${orderForm.value.storeId}`);

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



const touched = ref({
    customerName: false,
    phone: false,
});


// =========================
// Menu Data
const fallbackMenuItems = [
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
        price: 120,
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
        price: 420,
        imageUrl: salmonSashimiImg,
        status: "AVAILABLE",
        allergenInfo: "含生食",
    },
    {
        id: 5,
        categoryId: 3,
        itemName: "握壽司盛合",
        description: "主廚推薦 8 貫握壽司",
        price: 680,
        imageUrl: sushiImg,
        status: "AVAILABLE",
        allergenInfo: "含生食",
    },
    {
        id: 6,
        categoryId: 3,
        itemName: "炙燒鮭魚壽司",
        description: "炙燒表面焦香，入口即化",
        price: 380,
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
];
const menuItems = ref([...fallbackMenuItems]);
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

const isCarrierValid = computed(() => {
    return /^\/(?=.*[A-Z])(?=.*\d)[0-9A-Z.+-]{7}$/.test(
        customerForm.value.carrierNumber
    );
});
const isTaxIdValid = computed(() => {
    return /^\d{8}$/.test(customerForm.value.carrierNumber)
});
const isLoveCodeValid = computed(() => {
    return /^\d{3,7}$/.test(customerForm.value.carrierNumber)
});
const isNameValid = computed(() => {
    const name = customerForm.value.customerName.trim();
    return name.length >= 2 && name.length <= 20;
});

const isPhoneValid = computed(() => {
    return /^09\d{8}$/.test(customerForm.value.phone);
});
function showError(message) {
    Swal.fire({
        icon: "warning",
        title: "提醒",
        text: message,
        confirmButtonText: "知道了",
        confirmButtonColor: "#e8ad78",
    });
}

function showSuccess(message) {
    return Swal.fire({
        icon: "success",
        title: message,
        confirmButtonText: "確認",
        confirmButtonColor: "#e8ad78",
    });
}


const phoneInput = ref(null);
const agreePolicyInput = ref(null);
const carrierInput = ref(null);
const nameInput = ref(null);

const getMenuItemImage = (item) => {
    const imageUrl = item.imageUrl || "";
    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
        return imageUrl;
    }

    const name = item.itemName || "";
    if (name.includes("胡麻")) return tofuImg;
    if (name.includes("海鮮沙拉")) return seafoodSaladImg;
    if (name.includes("綜合生魚片")) return sashimiImg;
    if (name.includes("鮭魚刺身")) return salmonSashimiImg;
    if (name.includes("握壽司")) return sushiImg;
    if (name.includes("炙燒鮭魚")) return aburiSalmonSushiImg;
    if (name.includes("壽喜燒")) return sukiyakiImg;
    if (name.includes("天婦羅")) return tempuraImg;
    if (name.includes("抹茶")) return matchaDessertImg;
    if (name.includes("布丁")) return caramelPuddingImg;
    if (name.includes("可爾必思")) return calpisImg;
    if (name.includes("茶")) return japaneseTeaImg;
    if (name.includes("啤")) return asahiBeerImg;
    if (name.includes("清酒") || name.includes("吟釀")) return japaneseSakeImg;
    return tofuImg;
};

const unwrap = (response) => response.data?.data ?? response.data ?? [];

const normalizeStoreMenuItem = (item) => ({
    id: item.id ?? item.menuItemId,
    categoryId: Number(item.categoryId),
    itemName: item.itemName,
    description: item.description || "",
    price: Number(item.finalPrice ?? item.price ?? 0),
    imageUrl: getMenuItemImage(item),
    status: item.isSelectable === false ? "SOLD_OUT" : "AVAILABLE",
    allergenInfo: item.allergenInfo || "無",
});

const menuLoadError = ref("");

async function loadStoreMenu(storeId) {
    menuLoadError.value = "";

    try {
        const response = await axios.get(`/api/menu-items/store/${storeId}`);
        const items = unwrap(response);
        if (Array.isArray(items) && items.length > 0) {
            menuItems.value = items.map(normalizeStoreMenuItem);
            return;
        }
        menuItems.value = [...fallbackMenuItems];
        menuLoadError.value = "此門市目前沒有可供應菜單，暫時顯示示範菜單";
    } catch (error) {
        menuItems.value = [...fallbackMenuItems];
        menuLoadError.value = "門市菜單暫時無法載入，暫時顯示示範菜單";
    }
}

watch(
    () => route.query.storeId,
    async (storeId) => {
        orderForm.value.storeId = parsePositiveId(storeId, 1);
        cartItems.value = [];
        await loadStoreMenu(orderForm.value.storeId);
    }
);

watch(
    () => route.query.orderType,
    (orderType) => {
        orderForm.value.orderType = normalizeRouteOrderType(orderType);
    }
);

watch(
    () => route.query.tableId,
    (tableId) => {
        orderForm.value.tableId = parsePositiveId(tableId);
    }
);

watch(
    () => route.query.reservationId,
    (reservationId) => {
        orderForm.value.reservationId = parsePositiveId(reservationId);
    }
);

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
        showToast("請先加入餐點", "error");
        return;
    }

    step.value = "CHECKOUT";
}

function backToMenu() {
    step.value = "MENU";
}

function formatCarrier() {
    let value = customerForm.value.carrierNumber.toUpperCase();

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

function formatOnlyNumber(maxLength) {
    customerForm.value.carrierNumber = customerForm.value.carrierNumber
        .replace(/\D/g, "")
        .slice(0, maxLength);
}




async function submitOrder() {

    if (
        customerForm.value.invoiceType === "MOBILE_BARCODE" &&
        !/^\/[0-9A-Z.+-]{7}$/.test(customerForm.value.carrierNumber)
    ) {
        showError("請輸入正確手機條碼載具");
        return;
    }

    // 統編
    if (
        customerForm.value.invoiceType === 'TAX_ID' &&
        !/^\d{8}$/.test(customerForm.value.carrierNumber)
    ) {
        showError('請輸入正確統一編號')
        return
    }

    // 愛心碼
    if (
        customerForm.value.invoiceType === 'DONATION' &&
        !/^\d{3,7}$/.test(customerForm.value.carrierNumber)
    ) {
        showError('請輸入正確愛心碼')
        return
    }

    if (!customerForm.value.customerName) {
        showError("請輸入姓名");
        return;
    }

    if (!customerForm.value.customerName?.trim()) {
        touched.value.customerName = true;
        showError("請輸入姓名");
        return;
    }

    if (!customerForm.value.phone) {
        touched.value.phone = true;
        showError("請輸入電話號碼");
        return;
    }
    const namePattern = /^[A-Za-z\u4e00-\u9fa5\s]{2,20}$/;

    if (!namePattern.test(customerForm.value.customerName.trim())) {
        showToast("姓名格式不正確", "error");
        return;
    }

    if (!/^09\d{8}$/.test(customerForm.value.phone)) {
        touched.value.phone = true;
        showToast("請輸入正確手機號碼", "error");
        return;
    }

    if (!customerForm.value.agreePolicy) {
        showToast("請先勾選同意條款", "error");
        return;
    }

    if (orderForm.value.orderType === "DINE_IN" && !orderForm.value.tableId) {
        alert("內用訂單需要桌位資訊，請從訂位或桌邊 QR Code 進入點餐");
        return;
    }

    const request = {
        userId: orderForm.value.userId,
        storeId: orderForm.value.storeId,
        tableId: orderForm.value.orderType === "DINE_IN" ? orderForm.value.tableId : null,
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
    const orderId = response.data.orderId;
    // if (customerForm.value.paymentMethod === "LINE_PAY") {
    //     pusrouter.h(`/payment/linepay/${orderId}`);
    //     return;
    // }

    // if (customerForm.value.paymentMethod === "CREDIT_CARD") {
    //     router.push(`/payment/card/${orderId}`);
    //     return;
    // }

    if (customerForm.value.paymentMethod === "CREDIT_CARD") {
        await Swal.fire({
            icon: "success",
            title: "訂單建立成功",
            text: "即將前往綠界付款頁面",
            confirmButtonText: "前往付款",
            confirmButtonColor: "#e8ad78",
        });

        window.location.href =
            `http://localhost:8080/api/payments/ecpay/checkout/${orderId}`;
        return;
    }

    if (customerForm.value.paymentMethod === "LINE_PAY") {
        await Swal.fire({
            icon: "success",
            title: "訂單建立成功",
            text: "即將前往 LINE Pay 付款頁面",
            confirmButtonText: "前往付款",
            confirmButtonColor: "#e8ad78",
        });

        window.location.href =
            `http://localhost:8080/api/payments/linepay/request/${orderId}`;
        return;
    }

    // if (customerForm.value.paymentMethod === "LINE_PAY") {
    //     await Swal.fire({
    //         icon: "success",
    //         title: "訂單建立成功",
    //         text: "即將前往 Line Pay 付款頁面",
    //         confirmButtonText: "前往付款",
    //         confirmButtonColor: "#e8ad78",
    //     });

    //     window.location.href =
    //         `http://localhost:8080/api/payments/linepay/request/${orderId}`;
    //     return;
    // }

    // 現場付款
    await Swal.fire({
        icon: "success",
        title: "訂單送出成功",
        text: "請至櫃台完成付款與取餐",
        confirmButtonText: "確認",
        timer: 5000,
        confirmButtonColor: "#e8ad78",
    });

    linePayOrderId.value = null;
    linePayQr.value = "";
    linePayUrl.value = "";
    cartItems.value = [];

    customerForm.value = {
        customerName: "",
        phone: "",
        title: "小姐",
        paymentMethod: "CASH",
        needTableware: false,
        agreePolicy: false,
        invoiceType: "NONE",
        carrierNumber: "",
    };

    touched.value = {
        customerName: false,
        phone: false,
    };

    step.value = "MENU";
    window.scrollTo({
        top: 0,
        behavior: "smooth",
    });
}

onMounted(() => {
    loadStoreMenu(orderForm.value.storeId);
});
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
                    <p class="store-context">
                        目前門市：<strong>{{ selectedStoreName }}</strong>
                        <span>#{{ orderForm.storeId }}</span>
                    </p>
                </div>

                <div class="order-type">
                    <button :class="{ active: orderForm.orderType === 'DINE_IN' }"
                        @click="orderForm.orderType = 'DINE_IN'">
                        內用
                    </button>

                    <button :class="{ active: orderForm.orderType === 'TAKEOUT' }"
                        @click="orderForm.orderType = 'TAKEOUT'">
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

            <p v-if="menuLoadError" class="menu-alert">
                {{ menuLoadError }}
            </p>

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

                    <div class="payment-methods">

                        <button type="button" class="payment-method-card"
                            :class="{ active: customerForm.paymentMethod === 'CASH' }"
                            @click="customerForm.paymentMethod = 'CASH'">
                            <div class="method-icon">💵</div>
                            <div>
                                <strong>現場付款</strong>
                                <p>取餐時付款</p>
                            </div>
                        </button>

                        <button type="button" class="payment-method-card"
                            :class="{ active: customerForm.paymentMethod === 'LINE_PAY' }"
                            @click="customerForm.paymentMethod = 'LINE_PAY'">
                            <div class="method-icon">📱</div>
                            <div>
                                <strong>Line Pay</strong>
                                <p>使用行動支付</p>
                            </div>
                        </button>

                        <button type="button" class="payment-method-card"
                            :class="{ active: customerForm.paymentMethod === 'CREDIT_CARD' }"
                            @click="customerForm.paymentMethod = 'CREDIT_CARD'">
                            <div class="method-icon">💳</div>
                            <div>
                                <strong>信用卡</strong>
                                <p>綠界安全付款</p>
                            </div>
                        </button>

                    </div>

                    <div v-if="customerForm.paymentMethod === 'CASH'" class="payment-info-box">
                        現場付款，取餐時付款。
                    </div>

                    <div v-if="customerForm.paymentMethod === 'LINE_PAY'" class="payment-info-box">
                        <p>LINE Pay 付款</p>
                        <small>
                            送出訂單後，系統將導向 LINE Pay 付款頁面。
                            可使用 Sandbox 帳號登入，或掃描 LINE Pay 頁面提供的 QR Code 完成付款。
                        </small>
                    </div>

                    <div v-if="customerForm.paymentMethod === 'CREDIT_CARD'" class="payment-info-box">
                        <div class="payment-icon">💳</div>

                        <h4>信用卡付款</h4>

                        <p class="payment-desc">
                            送出訂單後，系統將導向綠界科技安全付款頁面，
                            請依照頁面指示完成信用卡付款。
                        </p>

                        <div class="card-brands">
                            <span>VISA</span>
                            <span>MasterCard</span>
                            <span>JCB</span>
                        </div>

                        <div class="payment-notice">
                            信用卡資料將由綠界科技加密處理，
                            本網站不會儲存您的信用卡卡號、有效期限或安全碼。
                        </div>
                    </div>

                    <div class="payment-total">
                        付款金額
                        <strong>NT${{ totalAmount }}</strong>
                    </div>
                </div>

                <div class="form-card">
                    <h3>發票 / 載具</h3>

                    <div class="invoice-options">
                        <label class="invoice-card" :class="{ active: customerForm.invoiceType === 'NONE' }">
                            <input type="radio" value="NONE" v-model="customerForm.invoiceType" />
                            <div>
                                <strong>電子發票</strong>
                                <p>不使用載具</p>
                            </div>
                        </label>

                        <label class="invoice-card" :class="{ active: customerForm.invoiceType === 'MOBILE_BARCODE' }">
                            <input type="radio" value="MOBILE_BARCODE" v-model="customerForm.invoiceType"
                                @change="customerForm.carrierNumber = ''" />
                            <div>
                                <strong>手機條碼載具</strong>
                                <p>發票存入手機條碼</p>
                            </div>
                        </label>

                        <label class="invoice-card" :class="{ active: customerForm.invoiceType === 'TAX_ID' }">
                            <input type="radio" value="TAX_ID" v-model="customerForm.invoiceType"
                                @change="customerForm.carrierNumber = ''" />
                            <div>
                                <strong>公司統編</strong>
                                <p>開立公司用發票</p>
                            </div>
                        </label>

                        <label class="invoice-card" :class="{ active: customerForm.invoiceType === 'DONATION' }">
                            <input type="radio" value="DONATION" v-model="customerForm.invoiceType"
                                @change="customerForm.carrierNumber = ''" />
                            <div>
                                <strong>愛心碼捐贈</strong>
                                <p>捐贈電子發票</p>
                            </div>
                        </label>
                    </div>

                    <div v-if="customerForm.invoiceType === 'MOBILE_BARCODE'" class="invoice-input-box">
                        <label>手機條碼載具</label>
                        <small class="input-hint">
                            格式範例：/ABC1234
                        </small>
                        <input v-model="customerForm.carrierNumber" @input="formatCarrier" type="text"
                            placeholder="/ABC1234" maxlength="8" tabindex="1" :class="{
                                'input-success': customerForm.carrierNumber && isCarrierValid,
                                'input-error': customerForm.carrierNumber && !isCarrierValid
                            }" />
                        <small v-if="customerForm.carrierNumber" :class="isCarrierValid ? 'success-msg' : 'error-msg'">
                            {{ isCarrierValid ? '✓ 手機條碼格式正確' : '✕ 格式需為 /ABC1234，且包含英文與數字' }}
                        </small>
                    </div>

                    <div v-if="customerForm.invoiceType === 'TAX_ID'" class="invoice-input-box">
                        <label>公司統一編號</label>
                        <input @input="formatOnlyNumber(8)" v-model="customerForm.carrierNumber" type="text"
                            placeholder="請輸入 8 碼統一編號" maxlength="8" :class="{
                                'input-success': customerForm.carrierNumber && isTaxIdValid,
                                'input-error': customerForm.carrierNumber && !isTaxIdValid
                            }" />
                        <small v-if="customerForm.carrierNumber" :class="isTaxIdValid ? 'success-msg' : 'error-msg'">
                            {{ isTaxIdValid ? '✓ 統一編號格式正確' : '✕ 請輸入 8 碼數字' }}
                        </small>
                    </div>

                    <div v-if="customerForm.invoiceType === 'DONATION'" class="invoice-input-box">
                        <label>愛心碼</label>
                        <input @input="formatOnlyNumber(7)" v-model="customerForm.carrierNumber" type="text"
                            placeholder="例如：919、8888" maxlength="7" :class="{
                                'input-success': customerForm.carrierNumber && isLoveCodeValid,
                                'input-error': customerForm.carrierNumber && !isLoveCodeValid
                            }" />
                        <small v-if="customerForm.carrierNumber" :class="isLoveCodeValid ? 'success-msg' : 'error-msg'">
                            {{ isLoveCodeValid ? '✓ 愛心碼格式正確' : '✕ 愛心碼需為 3~7 位數字' }}
                        </small>
                    </div>
                </div>

                <div class="form-card">
                    <h3>聯絡資訊</h3>

                    <label>
                        姓名 <span class="required">*</span>
                    </label>
                    <input ref="nameInput" tabindex="2" v-model="customerForm.customerName" type="text"
                        @input="touched.customerName = true" @blur="touched.customerName = true"
                        @keydown.enter.prevent="phoneInput.focus()" :class="{
                            'input-success': touched.customerName && customerForm.customerName && isNameValid,
                            'input-error': touched.customerName && !isNameValid
                        }" />
                    <small v-if="touched.customerName" :class="isNameValid ? 'success-msg' : 'error-msg'">
                        {{ isNameValid ? '✓ 姓名格式正確' : '✕ 姓名至少需 2 個字元' }}
                    </small>

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

                    <label>
                        電話 <span class="required">*</span>
                    </label>
                    <input ref="phoneInput" tabindex="3" class="phone-input" v-model="customerForm.phone"
                        @blur="touched.phone = true" @input="
                            touched.phone = true;
                        customerForm.phone = customerForm.phone.replace(/\D/g, '').slice(0, 10)"
                        @keydown.enter.prevent="agreePolicyInput.focus()" type="tel" placeholder="0912345678"
                        maxlength="10" :class="{
                            'input-success': touched.phone && customerForm.phone && isPhoneValid,
                            'input-error': touched.phone && !isPhoneValid
                        }" />

                    <small v-if="touched.phone" :class="isPhoneValid ? 'success-msg' : 'error-msg'">
                        {{
                            isPhoneValid
                                ? '✓ 電話格式正確'
                                : '✕ 請輸入正確手機號碼，例如 0912345678'
                        }}
                    </small>
                </div>

                <div class="form-card">
                    <label>
                        <input type="checkbox" v-model="customerForm.needTableware" />
                        需要免洗餐具或吸管
                    </label>
                </div>

                <div class="policy-box">
                    <label>
                        <input tabindex="4" ref="agreePolicyInput" type="checkbox" v-model="customerForm.agreePolicy"
                            @keydown.enter.prevent="submitOrder" />
                        我已同意訂單成立後無法任意取消
                    </label>
                </div>

                <button :disabled="!customerForm.agreePolicy" class="submit-btn" @click="submitOrder">
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


.required {
    color: #d32f2f;
    font-weight: 700;
}

.input-hint {
    display: block;
    margin-top: 4px;
    margin-bottom: 8px;
    color: #888;
    font-size: 12px;
}

.input-success {
    border: 2px solid #4caf50 !important;
}

.input-error {
    border: 2px solid #f44336 !important;
}

.success-msg,
.error-msg {
    display: block;
    margin-top: 4px;
    margin-bottom: 12px;
    font-size: 13px;
}

.success-msg {
    color: #2e7d32;
    font-weight: 500;
}

.error-msg {
    color: #d32f2f;
    font-weight: 500;
}

.invoice-options {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    margin-top: 16px;
}

.invoice-card {
    border: 1px solid #ddd;
    border-radius: 14px;
    padding: 14px;
    display: flex;
    gap: 10px;
    cursor: pointer;
    background: #fff;
}

.invoice-card.active {
    border-color: #efb071;
    background: #fff3e4;
}

.invoice-card strong {
    color: #23466b;
}

.invoice-card p {
    margin: 4px 0 0;
    color: #777;
    font-size: 13px;
}

.invoice-input-box {
    margin-top: 18px;
}

.invoice-input-box label {
    display: block;
    margin-bottom: 8px;
    color: #23466b;
}

.invoice-input-box input {
    width: 100%;
    height: 42px;
    border: 1px solid #ddd;
    border-radius: 10px;
    padding: 0 12px;
}

.card-brands {
    margin: 18px 0;
    display: flex;
    justify-content: flex-start;
    gap: 10px;
}

.card-brands span {
    padding: 8px 14px;
    border: 1px solid #d9e2ec;
    border-radius: 999px;
    background: #ffffff;
    color: #23466b;
    font-size: 13px;
    font-weight: 600;
}

.payment-methods {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    margin-top: 18px;
}

.payment-method-card {
    border: 1px solid #ddd;
    background: #fff;
    border-radius: 14px;
    padding: 16px;
    cursor: pointer;
    text-align: left;
    display: flex;
    align-items: center;
    gap: 12px;
    transition: 0.2s;
}

.payment-method-card:hover {
    border-color: #efb071;
    background: #fffaf5;
}

.payment-method-card.active {
    border-color: #efb071;
    background: #fff3e4;
    box-shadow: 0 8px 20px rgba(239, 176, 113, 0.22);
}

.method-icon {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    background: #f6eadf;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
}

.payment-method-card strong {
    color: #23466b;
    font-size: 15px;
}

.payment-method-card p {
    margin: 4px 0 0;
    color: #777;
    font-size: 13px;
}

.payment-info-box {
    margin-top: 18px;
    border: 1px solid #eee;
    background: #fafafa;
    border-radius: 14px;
    padding: 18px;
    color: #23466b;
}

.payment-info-box p {
    margin: 8px 0 0;
    color: #666;
    line-height: 1.7;
}

.payment-info-box small {
    display: block;
    margin-top: 10px;
    color: #8c6335;
}

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

.store-context {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    margin-top: 10px;
    padding: 8px 12px;
    border: 1px solid #ead7c5;
    border-radius: 999px;
    background: #fff8f1;
    color: #8f623e;
    font-size: 14px;
    font-weight: 700;
}

.store-context span {
    color: #9ca8b5;
    font-weight: 600;
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

.menu-alert {
    margin: -12px 0 24px;
    padding: 12px 16px;
    border: 1px solid #ead7c5;
    border-radius: 12px;
    background: #fff8f1;
    color: #8f623e;
    font-weight: 700;
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
