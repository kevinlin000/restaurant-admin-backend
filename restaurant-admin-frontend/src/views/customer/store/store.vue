<script setup>
import { computed, onMounted, ref } from "vue";
import api from "@/api/axios";
import logoUrl from "@/assets/images/logo.png";

const stores = ref([]);
const cities = ref([]);
const districts = ref([]);
const selectedCity = ref("");
const selectedDistrict = ref("");
const keyword = ref("");
const selectedStore = ref(null);
const loading = ref(false);
const detailLoading = ref(false);
const nearbyLoading = ref(false);
const errorMessage = ref("");

const hasFilters = computed(
  () => keyword.value.trim() || selectedCity.value || selectedDistrict.value,
);

const unwrap = (response) => response.data?.data ?? response.data ?? [];

const formatDistance = (distanceKm) => {
  if (distanceKm === null || distanceKm === undefined) return "";
  return `${Number(distanceKm).toFixed(1)} km`;
};

const mapsUrl = (store) =>
  `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(
    `${store.storeName} ${store.address}`,
  )}`;

const openStatusText = (store) => (store.openNow ? "營業中" : "非營業時間");

const storeLocation = (store) => [store.city, store.district].filter(Boolean).join(" ");

const loadStores = async () => {
  loading.value = true;
  errorMessage.value = "";

  try {
    const response = await api.get("/api/stores");
    stores.value = unwrap(response);
    if (!selectedStore.value && stores.value.length > 0) {
      await loadStoreDetail(stores.value[0].storeId);
    }
  } catch (error) {
    errorMessage.value = "門市資料暫時無法載入";
    stores.value = [];
  } finally {
    loading.value = false;
  }
};

const loadCities = async () => {
  try {
    const response = await api.get("/api/stores/cities");
    cities.value = unwrap(response);
  } catch (error) {
    cities.value = [];
  }
};

const loadDistricts = async () => {
  selectedDistrict.value = "";
  districts.value = [];

  if (!selectedCity.value) return;

  try {
    const response = await api.get(
      `/api/stores/cities/${encodeURIComponent(selectedCity.value)}/districts`,
    );
    districts.value = unwrap(response);
  } catch (error) {
    districts.value = [];
  }
};

const applyFilters = async () => {
  const q = keyword.value.trim();
  loading.value = true;
  errorMessage.value = "";

  try {
    let response;
    if (q) {
      response = await api.get("/api/stores/search", { params: { keyword: q } });
    } else if (selectedCity.value && selectedDistrict.value) {
      response = await api.get(
        `/api/stores/city/${encodeURIComponent(selectedCity.value)}/district/${encodeURIComponent(
          selectedDistrict.value,
        )}`,
      );
    } else if (selectedCity.value) {
      response = await api.get(`/api/stores/city/${encodeURIComponent(selectedCity.value)}`);
    } else {
      response = await api.get("/api/stores");
    }

    stores.value = unwrap(response);
    if (stores.value.length > 0) {
      await loadStoreDetail(stores.value[0].storeId);
    } else {
      selectedStore.value = null;
    }
  } catch (error) {
    errorMessage.value = "搜尋門市時發生錯誤";
    stores.value = [];
    selectedStore.value = null;
  } finally {
    loading.value = false;
  }
};

const clearFilters = async () => {
  keyword.value = "";
  selectedCity.value = "";
  selectedDistrict.value = "";
  districts.value = [];
  await loadStores();
};

const findNearby = () => {
  if (!navigator.geolocation) {
    errorMessage.value = "此瀏覽器不支援定位功能";
    return;
  }

  nearbyLoading.value = true;
  errorMessage.value = "";

  navigator.geolocation.getCurrentPosition(
    async (position) => {
      try {
        const response = await api.post("/api/stores/nearby", {
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          limit: 8,
        });
        stores.value = unwrap(response);
        if (stores.value.length > 0) {
          await loadStoreDetail(stores.value[0].storeId);
        }
      } catch (error) {
        errorMessage.value = "附近門市查詢失敗";
      } finally {
        nearbyLoading.value = false;
      }
    },
    () => {
      nearbyLoading.value = false;
      errorMessage.value = "無法取得目前位置";
    },
    { enableHighAccuracy: true, timeout: 10000 },
  );
};

const loadStoreDetail = async (storeId) => {
  detailLoading.value = true;

  try {
    const response = await api.get(`/api/stores/${storeId}`);
    selectedStore.value = unwrap(response);
  } catch (error) {
    selectedStore.value = stores.value.find((store) => store.storeId === storeId) ?? null;
  } finally {
    detailLoading.value = false;
  }
};

onMounted(async () => {
  await Promise.all([loadStores(), loadCities()]);
});
</script>

<template>
  <main class="store-page">
    <section class="store-hero">
      <div class="container">
        <div class="hero-content">
          <span class="eyebrow">Store Locator</span>
          <h1>分店資訊</h1>
          <p>查詢鄰近門市、營業狀態與交通資訊，快速找到最方便的用餐地點。</p>
        </div>
      </div>
    </section>

    <section class="store-content">
      <div class="container">
        <div class="filter-bar">
          <div class="search-box">
            <i class="bi bi-search"></i>
            <input
              v-model="keyword"
              type="search"
              placeholder="搜尋門市、區域或地址"
              @keyup.enter="applyFilters"
            />
          </div>

          <select v-model="selectedCity" class="filter-select" @change="loadDistricts">
            <option value="">全部縣市</option>
            <option v-for="city in cities" :key="city" :value="city">{{ city }}</option>
          </select>

          <select
            v-model="selectedDistrict"
            class="filter-select"
            :disabled="!selectedCity"
            @change="applyFilters"
          >
            <option value="">全部區域</option>
            <option v-for="district in districts" :key="district" :value="district">
              {{ district }}
            </option>
          </select>

          <button class="primary-action" type="button" @click="applyFilters">
            <i class="bi bi-funnel"></i>
            篩選
          </button>

          <button class="secondary-action" type="button" @click="findNearby">
            <span v-if="nearbyLoading" class="spinner-border spinner-border-sm"></span>
            <i v-else class="bi bi-geo-alt"></i>
            附近門市
          </button>

          <button v-if="hasFilters" class="ghost-action" type="button" @click="clearFilters">
            清除
          </button>
        </div>

        <div v-if="errorMessage" class="alert alert-warning border-0 rounded-2">
          {{ errorMessage }}
        </div>

        <div class="store-layout">
          <section class="store-list" aria-label="門市列表">
            <div v-if="loading" class="loading-state">
              <span class="spinner-border"></span>
              <span>載入門市中</span>
            </div>

            <div v-else-if="stores.length === 0" class="empty-state">
              <i class="bi bi-shop"></i>
              <strong>目前沒有符合條件的門市</strong>
            </div>

            <button
              v-for="store in stores"
              v-else
              :key="store.storeId"
              type="button"
              :class="[
                'store-card',
                selectedStore?.storeId === store.storeId ? 'store-card-active' : '',
              ]"
              @click="loadStoreDetail(store.storeId)"
            >
              <div class="card-main">
                <div>
                  <div class="store-title-row">
                    <h2>{{ store.storeName }}</h2>
                    <span :class="['status-pill', store.openNow ? 'open' : 'closed']">
                      {{ openStatusText(store) }}
                    </span>
                  </div>
                  <p class="location">{{ storeLocation(store) }}</p>
                  <p class="address">{{ store.address }}</p>
                </div>
                <span v-if="store.distanceKm !== null && store.distanceKm !== undefined" class="distance">
                  {{ formatDistance(store.distanceKm) }}
                </span>
              </div>
              <div class="meta-row">
                <span><i class="bi bi-telephone"></i>{{ store.phone || "未提供電話" }}</span>
                <span><i class="bi bi-train-front"></i>{{ store.mrtInfo || "交通資訊更新中" }}</span>
              </div>
            </button>
          </section>

          <aside class="store-detail" aria-label="門市詳細資料">
            <div v-if="detailLoading" class="loading-state compact">
              <span class="spinner-border spinner-border-sm"></span>
              <span>載入詳細資訊</span>
            </div>

            <template v-else-if="selectedStore">
              <div class="detail-image">
                <img
                  :src="selectedStore.mainImageUrl || logoUrl"
                  :alt="selectedStore.storeName"
                />
              </div>

              <div class="detail-body">
                <span :class="['status-pill', selectedStore.openNow ? 'open' : 'closed']">
                  {{ openStatusText(selectedStore) }}
                </span>
                <h2>{{ selectedStore.storeName }}</h2>
                <p class="detail-address">{{ selectedStore.address }}</p>

                <div class="detail-actions">
                  <a class="primary-action link-action" :href="mapsUrl(selectedStore)" target="_blank">
                    <i class="bi bi-map"></i>
                    地圖
                  </a>
                  <a v-if="selectedStore.phone" class="secondary-action link-action" :href="`tel:${selectedStore.phone}`">
                    <i class="bi bi-telephone"></i>
                    撥號
                  </a>
                </div>

                <dl class="info-list">
                  <div>
                    <dt>鄰近交通</dt>
                    <dd>{{ selectedStore.mrtInfo || "尚未提供" }}</dd>
                  </div>
                  <div>
                    <dt>停車資訊</dt>
                    <dd>{{ selectedStore.parkingInfo || "尚未提供" }}</dd>
                  </div>
                  <div>
                    <dt>門市特色</dt>
                    <dd>{{ selectedStore.description || "歡迎到店享用精緻餐點。" }}</dd>
                  </div>
                </dl>

                <div class="hours-block">
                  <h3>營業時間</h3>
                  <div v-if="selectedStore.storeHours?.length" class="hours-list">
                    <div v-for="hour in selectedStore.storeHours" :key="`${hour.dayOfWeek}-${hour.mealPeriod}`">
                      <span>{{ hour.dayName }} {{ hour.mealPeriod }}</span>
                      <strong v-if="hour.closed">公休</strong>
                      <strong v-else>{{ hour.openTime }} - {{ hour.closeTime }}</strong>
                    </div>
                  </div>
                  <p v-else class="muted-text">營業時間尚未設定</p>
                </div>
              </div>
            </template>

            <div v-else class="empty-state detail-empty">
              <i class="bi bi-shop-window"></i>
              <strong>選擇門市查看詳細資訊</strong>
            </div>
          </aside>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
.store-page {
  min-height: 100vh;
  background: #f7f4ef;
  color: #2f3640;
}

.store-hero {
  min-height: 360px;
  padding: 150px 0 70px;
  background:
    linear-gradient(90deg, rgba(24, 29, 34, 0.74), rgba(24, 29, 34, 0.34)),
    url("https://images.unsplash.com/photo-1552566626-52f8b828add9?auto=format&fit=crop&w=1600&q=80");
  background-position: center;
  background-size: cover;
}

.hero-content {
  max-width: 720px;
  color: #ffffff;
}

.eyebrow {
  display: inline-block;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #f4c7a1;
}

.hero-content h1 {
  margin: 0 0 16px;
  font-size: 56px;
  font-weight: 800;
  letter-spacing: 0;
}

.hero-content p {
  max-width: 620px;
  margin: 0;
  font-size: 18px;
  line-height: 1.8;
}

.store-content {
  padding: 36px 0 72px;
}

.filter-bar {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) 160px 160px auto auto auto;
  gap: 12px;
  align-items: center;
  margin-bottom: 24px;
}

.search-box,
.filter-select {
  height: 48px;
  border: 1px solid #e2ddd6;
  border-radius: 8px;
  background: #ffffff;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
}

.search-box input {
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
}

.filter-select {
  padding: 0 12px;
}

.primary-action,
.secondary-action,
.ghost-action {
  height: 48px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 8px;
  padding: 0 18px;
  font-weight: 700;
  text-decoration: none;
  white-space: nowrap;
}

.primary-action {
  border: 1px solid #b45309;
  background: #b45309;
  color: #ffffff;
}

.secondary-action {
  border: 1px solid #d8c9bc;
  background: #ffffff;
  color: #8a4f19;
}

.ghost-action {
  border: 1px solid transparent;
  background: transparent;
  color: #6b7280;
}

.store-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 390px;
  gap: 24px;
  align-items: start;
}

.store-list {
  display: grid;
  gap: 14px;
}

.store-card {
  width: 100%;
  border: 1px solid #e4ddd3;
  border-radius: 8px;
  background: #ffffff;
  padding: 20px;
  text-align: left;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}

.store-card:hover,
.store-card-active {
  border-color: #b45309;
  box-shadow: 0 12px 28px rgba(47, 54, 64, 0.12);
  transform: translateY(-2px);
}

.card-main,
.store-title-row,
.meta-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.card-main {
  justify-content: space-between;
}

.store-title-row {
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.store-title-row h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
}

.location,
.address,
.detail-address,
.muted-text {
  color: #6b7280;
}

.location {
  margin: 0 0 4px;
  font-weight: 700;
}

.address {
  margin: 0;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  height: 28px;
  border-radius: 999px;
  padding: 0 10px;
  font-size: 13px;
  font-weight: 800;
}

.status-pill.open {
  background: #e8f7ee;
  color: #167a3d;
}

.status-pill.closed {
  background: #f2f0ed;
  color: #736b63;
}

.distance {
  min-width: 72px;
  text-align: right;
  color: #b45309;
  font-weight: 800;
}

.meta-row {
  flex-wrap: wrap;
  margin-top: 16px;
  color: #566a7f;
}

.meta-row span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.store-detail {
  position: sticky;
  top: 112px;
  border: 1px solid #e4ddd3;
  border-radius: 8px;
  overflow: hidden;
  background: #ffffff;
  box-shadow: 0 16px 40px rgba(47, 54, 64, 0.1);
}

.detail-image {
  height: 220px;
  background: #f2eee9;
}

.detail-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-body {
  padding: 24px;
}

.detail-body h2 {
  margin: 14px 0 8px;
  font-size: 28px;
  font-weight: 800;
}

.detail-actions {
  display: flex;
  gap: 10px;
  margin: 20px 0;
}

.link-action {
  height: 42px;
  flex: 1;
}

.info-list {
  display: grid;
  gap: 14px;
  margin: 0;
}

.info-list div {
  border-top: 1px solid #eee8e1;
  padding-top: 14px;
}

.info-list dt {
  margin-bottom: 4px;
  color: #8a4f19;
  font-size: 13px;
  font-weight: 800;
}

.info-list dd {
  margin: 0;
  color: #3f4650;
  line-height: 1.7;
}

.hours-block {
  margin-top: 22px;
}

.hours-block h3 {
  margin-bottom: 12px;
  font-size: 18px;
  font-weight: 800;
}

.hours-list {
  display: grid;
  gap: 8px;
}

.hours-list div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  border-radius: 8px;
  background: #faf7f2;
  padding: 10px 12px;
  color: #566a7f;
}

.hours-list strong {
  color: #2f3640;
}

.loading-state,
.empty-state {
  min-height: 180px;
  display: grid;
  place-items: center;
  gap: 10px;
  border: 1px dashed #d8c9bc;
  border-radius: 8px;
  background: #ffffff;
  color: #6b7280;
  text-align: center;
}

.loading-state.compact {
  min-height: 220px;
}

.empty-state i {
  font-size: 34px;
  color: #b45309;
}

.detail-empty {
  min-height: 420px;
  border: 0;
}

@media (max-width: 992px) {
  .filter-bar,
  .store-layout {
    grid-template-columns: 1fr;
  }

  .store-detail {
    position: static;
  }
}

@media (max-width: 576px) {
  .store-hero {
    min-height: 300px;
    padding: 128px 0 48px;
  }

  .hero-content h1 {
    font-size: 40px;
  }

  .card-main,
  .detail-actions {
    flex-direction: column;
  }

  .distance {
    text-align: left;
  }
}
</style>
