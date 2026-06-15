<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import api from "@/api/axios";
import logoUrl from "@/assets/images/logo.png";

const router = useRouter();

const allStores = ref([]);
const nearbyStores = ref([]);
const stores = ref([]);
const cities = ref([]);
const districts = ref([]);
const selectedCity = ref("");
const selectedDistrict = ref("");
const keyword = ref("");
const activeRegion = ref("all");
const openOnly = ref(false);
const sortMode = ref("recommended");
const selectedStore = ref(null);
const selectedImageIndex = ref(0);
const showFullHours = ref(false);
const loading = ref(false);
const detailLoading = ref(false);
const nearbyLoading = ref(false);
const errorMessage = ref("");
const resultMode = ref("all");

const regions = [
  { key: "all", label: "全部", cities: [] },
  { key: "north", label: "北北基", cities: ["台北市", "新北市", "基隆市"] },
  { key: "taoyuan", label: "桃竹苗", cities: ["桃園市", "新竹市", "新竹縣", "苗栗縣"] },
  { key: "central", label: "中彰投", cities: ["台中市", "彰化縣", "南投縣"] },
  { key: "southwest", label: "雲嘉南", cities: ["雲林縣", "嘉義市", "嘉義縣", "台南市"] },
  { key: "east", label: "宜花東", cities: ["宜蘭縣", "花蓮縣", "台東縣"] },
  { key: "south", label: "高屏", cities: ["高雄市", "屏東縣"] },
];

const hasFilters = computed(
  () =>
    keyword.value.trim() ||
    selectedCity.value ||
    selectedDistrict.value ||
    activeRegion.value !== "all" ||
    openOnly.value ||
    resultMode.value === "nearby",
);

const openStoreCount = computed(() => stores.value.filter((store) => store.openNow).length);

const selectedRegion = computed(
  () => regions.find((region) => region.key === activeRegion.value) ?? regions[0],
);

const selectedRegionLabel = computed(() => selectedRegion.value.label);

const closestStore = computed(() =>
  stores.value.find((store) => store.distanceKm !== null && store.distanceKm !== undefined),
);

const todayDayOfWeek = computed(() => {
  const day = new Date().getDay();
  return day === 0 ? 7 : day;
});

const todayHours = computed(() => {
  if (!selectedStore.value?.storeHours?.length) return [];
  return sortStoreHours(
    selectedStore.value.storeHours.filter((hour) => hour.dayOfWeek === todayDayOfWeek.value),
  );
});

const allHours = computed(() => sortStoreHours(selectedStore.value?.storeHours ?? []));

const visibleHours = computed(() => (showFullHours.value ? allHours.value : todayHours.value));

const hasMoreHours = computed(() => allHours.value.length > todayHours.value.length);

const todayHoursText = computed(() => {
  if (!todayHours.value.length) return "";
  return todayHours.value
    .filter((hour) => !isHourClosed(hour))
    .map((hour) => `${formatTime(hour.openTime)}-${formatTime(hour.closeTime)}`)
    .join(" / ");
});

const nextHoliday = computed(() => selectedStore.value?.upcomingHolidays?.[0] ?? null);

const galleryImages = computed(() => {
  if (!selectedStore.value) return [];
  const urls = selectedStore.value.imageUrls?.filter(Boolean) ?? [];
  return [...new Set([selectedStore.value.mainImageUrl, ...urls].filter(Boolean))];
});

const detailHeroImage = computed(
  () => galleryImages.value[selectedImageIndex.value] || selectedStore.value?.mainImageUrl || logoUrl,
);

const tableSummary = computed(() => {
  const tables = selectedStore.value?.tables ?? [];
  const usableTables = tables.filter((table) => table.status !== "UNAVAILABLE");
  const seats = usableTables.reduce((sum, table) => sum + (Number(table.tableSize) || 0), 0);
  const zones = [...new Set(usableTables.map((table) => table.zone).filter(Boolean))];

  return {
    tableCount: usableTables.length,
    seats,
    zones: zones.length ? zones.join("、") : "尚未分區",
  };
});

const mapEmbedUrl = computed(() => {
  if (!selectedStore.value) return "";
  const query =
    selectedStore.value.latitude && selectedStore.value.longitude
      ? `${selectedStore.value.latitude},${selectedStore.value.longitude}`
      : `${selectedStore.value.storeName} ${selectedStore.value.address}`;
  return `https://www.google.com/maps?q=${encodeURIComponent(query)}&output=embed`;
});

const unwrap = (response) => response.data?.data ?? response.data ?? [];

const normalize = (value) => `${value ?? ""}`.trim().toLowerCase();

const formatDistance = (distanceKm) => {
  if (distanceKm === null || distanceKm === undefined) return "";
  const distance = Number(distanceKm);
  if (distance < 1) return `${Math.round(distance * 1000)} m`;
  return `${distance.toFixed(1)} km`;
};

const formatDate = (date) => {
  if (!date) return "";
  return new Intl.DateTimeFormat("zh-TW", { month: "2-digit", day: "2-digit" }).format(
    new Date(date),
  );
};

const formatTime = (time) => {
  if (!time) return "";
  return `${time}`.slice(0, 5);
};

const mealPeriodLabel = (period) =>
  ({
    LUNCH: "午餐",
    DINNER: "晚餐",
    AFTERNOON_TEA: "下午茶",
    ALL_DAY: "整日",
  })[period] || period || "";

const sortStoreHours = (hourList) =>
  [...hourList].sort((a, b) => {
    const dayDiff = (a.dayOfWeek ?? 0) - (b.dayOfWeek ?? 0);
    if (dayDiff !== 0) return dayDiff;
    return formatTime(a.openTime).localeCompare(formatTime(b.openTime));
  });

const isHourClosed = (hour) => Boolean(hour.closed ?? hour.isClosed);

const mapsUrl = (store) =>
  `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(
    `${store.storeName} ${store.address}`,
  )}`;

const openStatusText = (store) => (store.openNow ? "營業中" : "非營業時間");

const storeLocation = (store) => [store.city, store.district].filter(Boolean).join(" ");

const matchesKeyword = (store, keywordValue) => {
  if (!keywordValue) return true;
  const text = [
    store.storeName,
    store.city,
    store.district,
    store.address,
    store.phone,
    store.mrtInfo,
  ]
    .map(normalize)
    .join(" ");
  return text.includes(keywordValue);
};

const sortStores = (list) => {
  const sorted = [...list];
  if (sortMode.value === "open") {
    sorted.sort((a, b) => Number(b.openNow) - Number(a.openNow));
  } else if (sortMode.value === "name") {
    sorted.sort((a, b) => `${a.storeName}`.localeCompare(`${b.storeName}`, "zh-Hant"));
  } else if (sortMode.value === "distance") {
    sorted.sort((a, b) => (a.distanceKm ?? Number.MAX_VALUE) - (b.distanceKm ?? Number.MAX_VALUE));
  }
  return sorted;
};

const currentStoreSource = () => (resultMode.value === "nearby" ? nearbyStores.value : allStores.value);

const filterStores = (source = currentStoreSource()) => {
  const keywordValue = normalize(keyword.value);
  const regionCities = selectedRegion.value.cities;

  let nextStores = source.filter((store) => {
    const regionMatched =
      activeRegion.value === "all" || regionCities.includes(store.city);
    const cityMatched = !selectedCity.value || store.city === selectedCity.value;
    const districtMatched =
      !selectedDistrict.value || store.district === selectedDistrict.value;
    const openMatched = !openOnly.value || store.openNow;
    return (
      regionMatched &&
      cityMatched &&
      districtMatched &&
      openMatched &&
      matchesKeyword(store, keywordValue)
    );
  });

  stores.value = sortStores(nextStores);
  syncSelectedStore();
};

const syncSelectedStore = async () => {
  if (!stores.value.length) {
    selectedStore.value = null;
    selectedImageIndex.value = 0;
    showFullHours.value = false;
    return;
  }

  const stillVisible = stores.value.some((store) => store.storeId === selectedStore.value?.storeId);
  if (!stillVisible) {
    await loadStoreDetail(stores.value[0].storeId);
  }
};

const loadStores = async () => {
  loading.value = true;
  errorMessage.value = "";

  try {
    const response = await api.get("/api/stores");
    allStores.value = unwrap(response);
    resultMode.value = "all";
    filterStores();
  } catch (error) {
    errorMessage.value = "門市資料暫時無法載入";
    allStores.value = [];
    stores.value = [];
    selectedStore.value = null;
    selectedImageIndex.value = 0;
    showFullHours.value = false;
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

  if (!selectedCity.value) {
    filterStores();
    return;
  }

  try {
    const response = await api.get(
      `/api/stores/cities/${encodeURIComponent(selectedCity.value)}/districts`,
    );
    districts.value = unwrap(response);
  } catch (error) {
    districts.value = [];
  } finally {
    activeRegion.value = "all";
    resultMode.value = "all";
    filterStores();
  }
};

const applyFilters = () => {
  resultMode.value = "all";
  filterStores();
};

const selectRegion = (regionKey) => {
  activeRegion.value = regionKey;
  selectedCity.value = "";
  selectedDistrict.value = "";
  districts.value = [];
  resultMode.value = "all";
  filterStores();
};

const toggleOpenOnly = () => {
  openOnly.value = !openOnly.value;
  filterStores();
};

const updateSort = () => {
  stores.value = sortStores(stores.value);
};

const clearFilters = async () => {
  keyword.value = "";
  selectedCity.value = "";
  selectedDistrict.value = "";
  activeRegion.value = "all";
  openOnly.value = false;
  sortMode.value = "recommended";
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
          limit: 12,
        });
        resultMode.value = "nearby";
        sortMode.value = "distance";
        nearbyStores.value = unwrap(response);
        filterStores();
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
  selectedImageIndex.value = 0;
  showFullHours.value = false;

  try {
    const response = await api.get(`/api/stores/${storeId}`);
    selectedStore.value = unwrap(response);
  } catch (error) {
    selectedStore.value = stores.value.find((store) => store.storeId === storeId) ?? null;
  } finally {
    detailLoading.value = false;
  }
};

const selectGalleryImage = (index) => {
  selectedImageIndex.value = index;
};

const toggleHours = () => {
  showFullHours.value = !showFullHours.value;
};

const goReservation = (store) => {
  router.push({
    name: "CustomerReservation",
    query: { storeId: store.storeId, storeName: store.storeName },
  });
};

const goOrder = (store) => {
  router.push({
    name: "CustomerOrder",
    query: { storeId: store.storeId, storeName: store.storeName },
  });
};

onMounted(async () => {
  await Promise.all([loadStores(), loadCities()]);
});
</script>

<template>
  <main class="store-page">
    <section class="store-hero">
      <div class="container hero-shell">
        <div class="hero-content">
          <span class="eyebrow">門市查詢</span>
          <h1>分店資訊</h1>
          <p>查詢鄰近門市、營業狀態與交通資訊，選好地點後直接前往訂位或點餐。</p>
        </div>
      </div>
    </section>

    <section class="store-content">
      <div class="container">
        <div class="filter-panel">
          <div class="search-box">
            <i class="bi bi-search"></i>
            <input
              v-model="keyword"
              type="search"
              placeholder="搜尋門市、區域、捷運或地址"
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
        </div>

        <div class="region-row" aria-label="快速區域篩選">
          <button
            v-for="region in regions"
            :key="region.key"
            type="button"
            :class="['region-tab', activeRegion === region.key ? 'active' : '']"
            @click="selectRegion(region.key)"
          >
            {{ region.label }}
          </button>
        </div>

        <div class="tools-row">
          <div class="result-summary">
            <span>{{ selectedRegionLabel }}</span>
            <strong>{{ stores.length }}</strong>
            <span>間門市</span>
            <span class="divider"></span>
            <span>{{ openStoreCount }} 間營業中</span>
            <span v-if="closestStore" class="nearby-note">
              最近 {{ closestStore.storeName }} {{ formatDistance(closestStore.distanceKm) }}
            </span>
          </div>

          <div class="tool-actions">
            <button
              type="button"
              :class="['toggle-action', openOnly ? 'active' : '']"
              @click="toggleOpenOnly"
            >
              <i class="bi bi-clock"></i>
              只看營業中
            </button>

            <select v-model="sortMode" class="sort-select" @change="updateSort">
              <option value="recommended">推薦排序</option>
              <option value="open">營業優先</option>
              <option value="distance">距離優先</option>
              <option value="name">名稱排序</option>
            </select>

            <button v-if="hasFilters" class="ghost-action" type="button" @click="clearFilters">
              清除
            </button>
          </div>
        </div>

        <div v-if="errorMessage" class="notice-banner">
          <i class="bi bi-exclamation-triangle"></i>
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
              <span>調整地區或關鍵字後再查詢。</span>
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
              <div class="card-topline">
                <span>{{ storeLocation(store) || "分店" }}</span>
                <span v-if="store.distanceKm !== null && store.distanceKm !== undefined" class="distance">
                  {{ formatDistance(store.distanceKm) }}
                </span>
              </div>

              <div class="store-title-row">
                <h2>{{ store.storeName }}</h2>
                <span :class="['status-pill', store.openNow ? 'open' : 'closed']">
                  {{ openStatusText(store) }}
                </span>
              </div>

              <p class="address">{{ store.address }}</p>

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
                  :src="detailHeroImage"
                  :alt="selectedStore.storeName"
                />
                <span :class="['status-pill image-status', selectedStore.openNow ? 'open' : 'closed']">
                  {{ openStatusText(selectedStore) }}
                </span>
              </div>

              <div v-if="galleryImages.length > 1" class="image-strip" aria-label="門市圖片">
                <button
                  v-for="(imageUrl, index) in galleryImages"
                  :key="imageUrl"
                  type="button"
                  :class="['image-thumb', selectedImageIndex === index ? 'active' : '']"
                  @click="selectGalleryImage(index)"
                >
                  <img :src="imageUrl" :alt="`${selectedStore.storeName} 圖片 ${index + 1}`" />
                </button>
              </div>

              <div class="detail-body">
                <div class="detail-heading">
                  <div>
                    <p>{{ storeLocation(selectedStore) }}</p>
                    <h2>{{ selectedStore.storeName }}</h2>
                  </div>
                  <a class="map-link" :href="mapsUrl(selectedStore)" target="_blank" rel="noreferrer">
                    <i class="bi bi-map"></i>
                  </a>
                </div>

                <p class="detail-address">{{ selectedStore.address }}</p>

                <div class="detail-actions">
                  <button class="primary-action link-action" type="button" @click="goReservation(selectedStore)">
                    <i class="bi bi-calendar-check"></i>
                    線上訂位
                  </button>
                  <button class="secondary-action link-action" type="button" @click="goOrder(selectedStore)">
                    <i class="bi bi-bag-check"></i>
                    外帶點餐
                  </button>
                  <a v-if="selectedStore.phone" class="icon-action" :href="`tel:${selectedStore.phone}`">
                    <i class="bi bi-telephone"></i>
                  </a>
                </div>

                <div class="insight-grid">
                  <div class="insight-item">
                    <span>今日時段</span>
                    <strong v-if="todayHoursText">{{ todayHoursText }}</strong>
                    <strong v-else>尚未設定</strong>
                  </div>
                  <div class="insight-item">
                    <span>可用桌數</span>
                    <strong>{{ tableSummary.tableCount }} 桌</strong>
                  </div>
                  <div class="insight-item">
                    <span>座位容量</span>
                    <strong>{{ tableSummary.seats || "待設定" }}</strong>
                  </div>
                  <div class="insight-item">
                    <span>用餐區域</span>
                    <strong>{{ tableSummary.zones }}</strong>
                  </div>
                </div>

                <div v-if="nextHoliday" class="holiday-note">
                  <i class="bi bi-calendar-x"></i>
                  {{ formatDate(nextHoliday.holidayDate) }} {{ nextHoliday.reason || "門市公休" }}
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

                <div class="map-panel">
                  <iframe
                    v-if="mapEmbedUrl"
                    :src="mapEmbedUrl"
                    loading="lazy"
                    referrerpolicy="no-referrer-when-downgrade"
                    title="門市地圖"
                  ></iframe>
                </div>

                <div class="hours-block">
                  <div class="section-heading">
                    <h3>營業時間</h3>
                    <button
                      v-if="hasMoreHours"
                      class="text-action"
                      type="button"
                      @click="toggleHours"
                    >
                      {{ showFullHours ? "收起" : "完整時段" }}
                    </button>
                  </div>
                  <div v-if="visibleHours.length" class="hours-list">
                    <div
                      v-for="hour in visibleHours"
                      :key="hour.hourId || `${hour.dayOfWeek}-${hour.mealPeriod}`"
                    >
                      <span>
                        {{ showFullHours ? hour.dayName : "今日" }}
                        {{ mealPeriodLabel(hour.mealPeriod) }}
                      </span>
                      <strong v-if="isHourClosed(hour)">公休</strong>
                      <strong v-else>{{ formatTime(hour.openTime) }} - {{ formatTime(hour.closeTime) }}</strong>
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
  background: #f7f3ee;
  color: #344051;
}

.store-hero {
  min-height: 340px;
  padding: 138px 0 62px;
  background:
    linear-gradient(90deg, rgba(22, 28, 34, 0.76), rgba(22, 28, 34, 0.2)),
    url("https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=1800&q=80");
  background-position: center;
  background-size: cover;
}

.hero-shell {
  display: flex;
  align-items: end;
  min-height: 180px;
}

.hero-content {
  max-width: 760px;
  color: #ffffff;
}

.eyebrow {
  display: inline-block;
  margin-bottom: 12px;
  color: #e5b582;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.12em;
}

.hero-content h1 {
  margin: 0 0 14px;
  font-size: 56px;
  font-weight: 900;
  letter-spacing: 0;
}

.hero-content p {
  max-width: 660px;
  margin: 0;
  font-size: 18px;
  line-height: 1.8;
}

.store-content {
  padding: 36px 0 78px;
}

.filter-panel {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) 160px 160px auto auto;
  gap: 12px;
  align-items: center;
  margin-bottom: 18px;
}

.search-box,
.filter-select,
.sort-select {
  height: 50px;
  border: 1px solid #e2d8cf;
  border-radius: 8px;
  background: #ffffff;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
}

.search-box i {
  color: #9a6b42;
}

.search-box input {
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
  color: #344051;
}

.filter-select,
.sort-select {
  padding: 0 12px;
  color: #344051;
}

.primary-action,
.secondary-action,
.ghost-action,
.toggle-action,
.icon-action,
.map-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 8px;
  font-weight: 800;
  text-decoration: none;
  white-space: nowrap;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s, background 0.2s;
}

.primary-action,
.secondary-action,
.ghost-action,
.toggle-action {
  height: 50px;
  padding: 0 18px;
}

.primary-action {
  border: 1px solid #b1642f;
  background: #b1642f;
  color: #ffffff;
}

.secondary-action {
  border: 1px solid #d7c6b7;
  background: #ffffff;
  color: #8c552e;
}

.ghost-action {
  border: 1px solid transparent;
  background: transparent;
  color: #697386;
}

.primary-action:hover,
.secondary-action:hover,
.toggle-action:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(52, 64, 81, 0.12);
}

.region-row {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 18px;
}

.region-tab {
  height: 42px;
  border: 1px solid #1c2c3d;
  border-radius: 8px;
  background: #1c2c3d;
  color: #ffffff;
  font-weight: 800;
}

.region-tab.active {
  border-color: #a2322f;
  background: #a2322f;
}

.tools-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.result-summary {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  color: #697386;
  font-weight: 700;
}

.result-summary strong {
  color: #a2322f;
  font-size: 24px;
}

.divider {
  width: 1px;
  height: 18px;
  background: #d8cabf;
}

.nearby-note {
  color: #8c552e;
}

.tool-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.toggle-action {
  border: 1px solid #d7c6b7;
  background: #ffffff;
  color: #8c552e;
}

.toggle-action.active {
  border-color: #167a3d;
  background: #e8f7ee;
  color: #167a3d;
}

.notice-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  border-radius: 8px;
  background: #fff2d5;
  padding: 16px;
  color: #a16012;
  font-weight: 700;
}

.store-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 400px;
  gap: 24px;
  align-items: start;
}

.store-list {
  display: grid;
  gap: 14px;
}

.store-card {
  width: 100%;
  border: 1px solid #e4d9ce;
  border-radius: 8px;
  background: #ffffff;
  padding: 18px 20px;
  text-align: left;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}

.store-card:hover,
.store-card-active {
  border-color: #b1642f;
  box-shadow: 0 10px 24px rgba(52, 64, 81, 0.1);
  transform: translateY(-2px);
}

.card-topline,
.store-title-row,
.meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-topline {
  justify-content: space-between;
  margin-bottom: 10px;
  color: #8c552e;
  font-size: 14px;
  font-weight: 800;
}

.store-title-row {
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.store-title-row h2 {
  margin: 0;
  color: #263445;
  font-size: 22px;
  font-weight: 900;
}

.address,
.detail-address,
.muted-text,
.empty-state span {
  color: #697386;
}

.address {
  margin: 0;
  line-height: 1.7;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 13px;
  font-weight: 900;
}

.status-pill.open {
  background: #e8f7ee;
  color: #167a3d;
}

.status-pill.closed {
  background: #f1eeeb;
  color: #74685f;
}

.distance {
  min-width: 72px;
  text-align: right;
  color: #b1642f;
  font-weight: 900;
}

.meta-row {
  flex-wrap: wrap;
  margin-top: 14px;
  color: #566a7f;
}

.meta-row span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.store-detail {
  position: sticky;
  top: 92px;
  max-height: calc(100vh - 112px);
  border: 1px solid #e4d9ce;
  border-radius: 8px;
  overflow: auto;
  background: #ffffff;
  box-shadow: 0 14px 36px rgba(52, 64, 81, 0.1);
  scrollbar-gutter: stable;
}

.store-detail::-webkit-scrollbar {
  width: 8px;
}

.store-detail::-webkit-scrollbar-thumb {
  border: 2px solid #ffffff;
  border-radius: 999px;
  background: #d7c6b7;
}

.detail-image {
  position: relative;
  height: 178px;
  background: #f2eee9;
}

.detail-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-status {
  position: absolute;
  right: 16px;
  bottom: 16px;
}

.image-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  border-top: 1px solid #eee5dd;
  background: #fbf8f5;
  padding: 8px 10px;
}

.image-thumb {
  height: 50px;
  overflow: hidden;
  border: 2px solid transparent;
  border-radius: 8px;
  background: #ffffff;
  padding: 0;
}

.image-thumb.active {
  border-color: #b1642f;
}

.image-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-body {
  padding: 20px;
}

.detail-heading {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.detail-heading p {
  margin: 0 0 6px;
  color: #8c552e;
  font-weight: 800;
}

.detail-heading h2 {
  margin: 0;
  color: #263445;
  font-size: 25px;
  font-weight: 900;
}

.map-link,
.icon-action {
  width: 42px;
  height: 42px;
  flex: 0 0 42px;
  border: 1px solid #d7c6b7;
  background: #ffffff;
  color: #8c552e;
}

.detail-address {
  margin: 10px 0 0;
  line-height: 1.7;
}

.detail-actions {
  display: grid;
  grid-template-columns: 1fr 1fr 42px;
  gap: 8px;
  margin: 16px 0;
}

.link-action {
  height: 42px;
  border-radius: 8px;
  font-size: 14px;
}

.insight-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 12px;
}

.insight-item {
  border: 1px solid #eee5dd;
  border-radius: 8px;
  background: #fbf8f5;
  padding: 10px;
}

.insight-item span {
  display: block;
  margin-bottom: 5px;
  color: #8c552e;
  font-size: 13px;
  font-weight: 800;
}

.insight-item strong {
  display: block;
  color: #263445;
  font-size: 15px;
  line-height: 1.4;
}

.holiday-note {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  border-radius: 8px;
  background: #fff2d5;
  padding: 10px 12px;
  color: #a16012;
  font-weight: 800;
}

.info-list {
  display: grid;
  gap: 10px;
  margin: 0;
}

.info-list div {
  border-top: 1px solid #eee8e1;
  padding-top: 10px;
}

.info-list dt {
  margin-bottom: 4px;
  color: #8c552e;
  font-size: 13px;
  font-weight: 900;
}

.info-list dd {
  margin: 0;
  color: #3f4650;
  line-height: 1.7;
}

.map-panel {
  height: 150px;
  margin-top: 16px;
  overflow: hidden;
  border: 1px solid #eee5dd;
  border-radius: 8px;
  background: #f2eee9;
}

.map-panel iframe {
  width: 100%;
  height: 100%;
  border: 0;
}

.hours-block {
  margin-top: 16px;
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.section-heading h3 {
  margin: 0;
  color: #263445;
  font-size: 17px;
  font-weight: 900;
}

.text-action {
  border: 0;
  background: transparent;
  color: #8c552e;
  font-size: 13px;
  font-weight: 900;
}

.hours-list {
  display: grid;
  gap: 6px;
}

.hours-list div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  border-radius: 8px;
  background: #faf7f2;
  padding: 9px 10px;
  color: #566a7f;
}

.hours-list strong {
  color: #263445;
}

.loading-state,
.empty-state {
  min-height: 190px;
  display: grid;
  place-items: center;
  gap: 10px;
  border: 1px dashed #d7c6b7;
  border-radius: 8px;
  background: #ffffff;
  color: #697386;
  text-align: center;
}

.loading-state.compact {
  min-height: 260px;
}

.empty-state i {
  font-size: 34px;
  color: #b1642f;
}

.detail-empty {
  min-height: 520px;
  border: 0;
}

@media (max-width: 1200px) {
  .filter-panel {
    grid-template-columns: 1fr 1fr;
  }

  .region-row {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .store-layout {
    grid-template-columns: 1fr;
  }

  .store-detail {
    position: static;
    max-height: none;
  }

  .tools-row {
    align-items: stretch;
    flex-direction: column;
  }

  .tool-actions {
    flex-wrap: wrap;
  }
}

@media (max-width: 576px) {
  .store-hero {
    min-height: 300px;
    padding: 122px 0 48px;
  }

  .hero-content h1 {
    font-size: 40px;
  }

  .hero-content p {
    font-size: 16px;
  }

  .filter-panel,
  .region-row,
  .image-strip,
  .insight-grid,
  .detail-actions {
    grid-template-columns: 1fr;
  }

  .image-thumb {
    height: 72px;
  }

  .icon-action {
    width: 100%;
  }

  .card-topline,
  .hours-list div {
    align-items: flex-start;
    flex-direction: column;
  }

  .distance {
    text-align: left;
  }
}
</style>
