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
const selectedFeatures = ref([]);
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
    selectedFeatures.value.length > 0 ||
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

const featureOptions = computed(() => {
  const featureMap = new Map();
  allStores.value.forEach((store) => {
    (store.featureTags ?? []).forEach((feature) => {
      if (!featureMap.has(feature.featureKey)) {
        featureMap.set(feature.featureKey, feature);
      }
    });
  });
  return [...featureMap.values()].sort(
    (a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0) || a.featureLabel.localeCompare(b.featureLabel, "zh-Hant"),
  );
});

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

const storeFeatureKeys = (store) => (store.featureTags ?? []).map((feature) => feature.featureKey);

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

const matchesFeatures = (store) => {
  if (!selectedFeatures.value.length) return true;
  const keys = storeFeatureKeys(store);
  return selectedFeatures.value.every((featureKey) => keys.includes(featureKey));
};

const recommendationScore = (store) => {
  const keys = storeFeatureKeys(store);
  const selectedFeatureHits = selectedFeatures.value.filter((featureKey) => keys.includes(featureKey)).length;
  const hasDistance = store.distanceKm !== null && store.distanceKm !== undefined;
  const distanceScore = hasDistance ? Math.max(0, 30 - Number(store.distanceKm)) : 0;
  return (
    Number(store.openNow) * 100 +
    selectedFeatureHits * 32 +
    Math.min(keys.length, 4) * 4 +
    distanceScore
  );
};

const sortStores = (list) => {
  const sorted = [...list];
  if (sortMode.value === "recommended") {
    sorted.sort((a, b) => recommendationScore(b) - recommendationScore(a));
  } else if (sortMode.value === "open") {
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
    const featureMatched = matchesFeatures(store);
    return (
      regionMatched &&
      cityMatched &&
      districtMatched &&
      openMatched &&
      featureMatched &&
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

const toggleFeature = (featureKey) => {
  selectedFeatures.value = selectedFeatures.value.includes(featureKey)
    ? selectedFeatures.value.filter((selected) => selected !== featureKey)
    : [...selectedFeatures.value, featureKey];
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
  selectedFeatures.value = [];
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
    query: { storeId: store.storeId, storeName: store.storeName, orderType: "TAKEOUT" },
  });
};

onMounted(async () => {
  await Promise.all([loadStores(), loadCities()]);
});
</script>

<template>
  <main class="store-page">
    <section class="store-hero">
      <div class="store-hero__image" aria-hidden="true"></div>
      <div class="container store-hero__content">
        <p class="eyebrow">Location</p>
        <h1>門市據點</h1>
        <p class="hero-lead">選擇今晚的餐桌，確認營業時段、交通與訂位資訊。</p>
        <div class="hero-actions">
          <button class="hero-action primary-action" type="button" @click="findNearby">
            <span v-if="nearbyLoading" class="spinner-border spinner-border-sm"></span>
            <i v-else class="bi bi-crosshair"></i>
            附近門市
          </button>
          <a class="hero-action secondary-action" href="#store-results">
            <i class="bi bi-list-ul"></i>
            瀏覽全部
          </a>
        </div>
      </div>
    </section>

    <section id="store-results" class="store-content">
      <div class="container store-shell">
        <aside class="store-controls" aria-label="門市篩選">
          <div class="control-heading">
            <span>Find a Table</span>
            <strong>{{ stores.length }} 間門市</strong>
          </div>

          <label class="search-field">
            <span>關鍵字</span>
            <div>
              <i class="bi bi-search"></i>
              <input
                v-model="keyword"
                type="search"
                placeholder="店名、地址、捷運站"
                @keyup.enter="applyFilters"
              />
            </div>
          </label>

          <div class="select-grid">
            <label>
              <span>縣市</span>
              <select v-model="selectedCity" class="filter-select" @change="loadDistricts">
                <option value="">全部縣市</option>
                <option v-for="city in cities" :key="city" :value="city">{{ city }}</option>
              </select>
            </label>

            <label>
              <span>區域</span>
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
            </label>
          </div>

          <div class="control-actions">
            <button class="primary-action" type="button" @click="applyFilters">
              <i class="bi bi-funnel"></i>
              套用篩選
            </button>
            <button v-if="hasFilters" class="ghost-action" type="button" @click="clearFilters">
              清除條件
            </button>
          </div>

          <div class="region-group">
            <p>區域</p>
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
          </div>

          <div v-if="featureOptions.length" class="feature-row" aria-label="用餐情境篩選">
            <p>用餐情境</p>
            <button
              v-for="feature in featureOptions"
              :key="feature.featureKey"
              type="button"
              :class="[
                'feature-chip',
                selectedFeatures.includes(feature.featureKey) ? 'active' : '',
              ]"
              @click="toggleFeature(feature.featureKey)"
            >
              {{ feature.featureLabel }}
            </button>
          </div>

          <div class="side-note">
            <span>{{ selectedRegionLabel }}</span>
            <strong>{{ openStoreCount }}</strong>
            <span>間目前營業中</span>
          </div>
        </aside>

        <section class="store-results" aria-label="門市列表">
          <div class="result-toolbar">
            <div>
              <p>{{ resultMode === "nearby" ? "Nearby" : "Shops" }}</p>
              <h2>{{ resultMode === "nearby" ? "附近門市" : "全部門市" }}</h2>
            </div>

            <div class="tool-actions">
              <button
                type="button"
                :class="['toggle-action', openOnly ? 'active' : '']"
                @click="toggleOpenOnly"
              >
                <i class="bi bi-clock"></i>
                營業中
              </button>

              <select v-model="sortMode" class="sort-select" @change="updateSort">
                <option value="recommended">推薦排序</option>
                <option value="open">營業優先</option>
                <option value="distance">距離優先</option>
                <option value="name">名稱排序</option>
              </select>
            </div>
          </div>

          <div v-if="closestStore" class="nearby-note">
            <i class="bi bi-geo-alt"></i>
            最近門市：{{ closestStore.storeName }} {{ formatDistance(closestStore.distanceKm) }}
          </div>

          <div v-if="errorMessage" class="notice-banner">
            <i class="bi bi-exclamation-triangle"></i>
            {{ errorMessage }}
          </div>

          <div class="store-layout">
            <div class="store-list">
              <div v-if="loading" class="loading-state">
                <span class="spinner-border"></span>
                <span>載入門市中</span>
              </div>

              <div v-else-if="stores.length === 0" class="empty-state">
                <i class="bi bi-shop"></i>
                <strong>目前沒有符合條件的門市</strong>
                <span>調整地區或關鍵字後再查詢。</span>
              </div>

              <article
                v-for="store in stores"
                v-else
                :key="store.storeId"
                :class="[
                  'store-card',
                  selectedStore?.storeId === store.storeId ? 'store-card-active' : '',
                ]"
              >
                <button class="store-card-main" type="button" @click="loadStoreDetail(store.storeId)">
                  <span class="store-area">{{ storeLocation(store) || "分店" }}</span>
                  <span :class="['status-pill', store.openNow ? 'open' : 'closed']">
                    {{ openStatusText(store) }}
                  </span>

                  <strong>{{ store.storeName }}</strong>
                  <span class="store-address">{{ store.address }}</span>

                  <span class="store-meta-line">
                    <i class="bi bi-telephone"></i>
                    {{ store.phone || "未提供電話" }}
                  </span>
                  <span class="store-meta-line">
                    <i class="bi bi-train-front"></i>
                    {{ store.mrtInfo || "交通資訊更新中" }}
                  </span>
                </button>

                <div v-if="store.featureTags?.length" class="store-tags">
                  <span
                    v-for="feature in store.featureTags.slice(0, 3)"
                    :key="feature.featureKey"
                  >
                    {{ feature.featureLabel }}
                  </span>
                </div>

                <div class="store-card-actions">
                  <button type="button" @click="goReservation(store)">
                    <i class="bi bi-calendar-check"></i>
                    訂位
                  </button>
                  <button type="button" @click="goOrder(store)">
                    <i class="bi bi-bag-check"></i>
                    外帶
                  </button>
                  <a :href="mapsUrl(store)" target="_blank" rel="noreferrer">
                    <i class="bi bi-map"></i>
                    地圖
                  </a>
                  <span v-if="store.distanceKm !== null && store.distanceKm !== undefined" class="distance">
                    {{ formatDistance(store.distanceKm) }}
                  </span>
                </div>
              </article>
            </div>

            <aside class="store-detail" aria-label="門市詳細資料">
              <div v-if="detailLoading" class="loading-state compact">
                <span class="spinner-border spinner-border-sm"></span>
                <span>載入詳細資訊</span>
              </div>

              <template v-else-if="selectedStore">
                <div class="detail-image">
                  <img :src="detailHeroImage" :alt="selectedStore.storeName" />
                </div>

                <div class="detail-body">
                  <div class="detail-heading">
                    <p>{{ storeLocation(selectedStore) }}</p>
                    <h2>{{ selectedStore.storeName }}</h2>
                    <span :class="['status-pill', selectedStore.openNow ? 'open' : 'closed']">
                      {{ openStatusText(selectedStore) }}
                    </span>
                  </div>

                  <p class="detail-address">{{ selectedStore.address }}</p>

                  <div class="detail-actions">
                    <button class="primary-action" type="button" @click="goReservation(selectedStore)">
                      <i class="bi bi-calendar-check"></i>
                      線上訂位
                    </button>
                    <button class="secondary-action" type="button" @click="goOrder(selectedStore)">
                      <i class="bi bi-bag-check"></i>
                      外帶自取
                    </button>
                    <a v-if="selectedStore.phone" class="icon-action" :href="`tel:${selectedStore.phone}`">
                      <i class="bi bi-telephone"></i>
                    </a>
                  </div>

                  <div class="detail-facts">
                    <div>
                      <span>今日營業</span>
                      <strong v-if="todayHoursText">{{ todayHoursText }}</strong>
                      <strong v-else>尚未設定</strong>
                    </div>
                    <div>
                      <span>座位資訊</span>
                      <strong>{{ tableSummary.tableCount }} 桌・{{ tableSummary.seats || "待設定" }} 席</strong>
                    </div>
                    <div>
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
                      <dt>電話</dt>
                      <dd>{{ selectedStore.phone || "尚未提供" }}</dd>
                    </div>
                    <div>
                      <dt>交通</dt>
                      <dd>{{ selectedStore.mrtInfo || "尚未提供" }}</dd>
                    </div>
                    <div>
                      <dt>停車</dt>
                      <dd>{{ selectedStore.parkingInfo || "尚未提供" }}</dd>
                    </div>
                  </dl>

                  <p v-if="selectedStore.description" class="detail-description">
                    {{ selectedStore.description }}
                  </p>

                  <div v-if="selectedStore.featureTags?.length" class="detail-tags">
                    <span
                      v-for="feature in selectedStore.featureTags"
                      :key="feature.featureKey"
                    >
                      {{ feature.featureLabel }}
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

                  <div class="map-panel">
                    <iframe
                      v-if="mapEmbedUrl"
                      :src="mapEmbedUrl"
                      loading="lazy"
                      referrerpolicy="no-referrer-when-downgrade"
                      title="門市地圖"
                    ></iframe>
                  </div>
                </div>
              </template>

              <div v-else class="empty-state detail-empty">
                <i class="bi bi-shop-window"></i>
                <strong>選擇門市查看詳細資訊</strong>
              </div>
            </aside>
          </div>
        </section>
      </div>
    </section>
  </main>
</template>

<style scoped>
.store-page {
  min-height: 100vh;
  background: #f8f6f2;
  color: #211f1c;
}

.store-hero {
  position: relative;
  min-height: 520px;
  display: flex;
  align-items: flex-end;
  overflow: hidden;
  background: #11100e;
}

.store-hero__image {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(10, 10, 9, 0.82) 0%, rgba(10, 10, 9, 0.45) 48%, rgba(10, 10, 9, 0.1) 100%),
    url("/store-images/xuri-dining-room.jpg");
  background-position: center;
  background-size: cover;
  transform: scale(1.01);
}

.store-hero__content {
  position: relative;
  z-index: 1;
  padding: 150px 12px 78px;
  color: #fffaf0;
}

.eyebrow,
.result-toolbar p,
.control-heading span,
.region-group p,
.feature-row p {
  margin: 0;
  color: #b98a52;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.store-hero h1 {
  max-width: 680px;
  margin: 12px 0 18px;
  font-size: 64px;
  font-weight: 800;
  line-height: 1.05;
  letter-spacing: 0;
}

.hero-lead {
  max-width: 560px;
  margin: 0;
  color: rgba(255, 250, 240, 0.86);
  font-size: 18px;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 30px;
}

.primary-action,
.secondary-action,
.ghost-action,
.toggle-action,
.icon-action,
.hero-action,
.store-card-actions button,
.store-card-actions a {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 4px;
  font-weight: 800;
  line-height: 1;
  text-decoration: none;
  white-space: nowrap;
  transition: background 0.2s, border-color 0.2s, color 0.2s, transform 0.2s;
}

.primary-action,
.secondary-action,
.ghost-action,
.toggle-action,
.hero-action {
  min-height: 46px;
  padding: 0 18px;
}

.primary-action {
  border: 1px solid #8f1f1d;
  background: #8f1f1d;
  color: #ffffff;
}

.secondary-action {
  border: 1px solid rgba(143, 31, 29, 0.28);
  background: #ffffff;
  color: #6e1b19;
}

.store-hero .secondary-action {
  border-color: rgba(255, 250, 240, 0.58);
  background: rgba(255, 250, 240, 0.08);
  color: #fffaf0;
}

.ghost-action {
  border: 1px solid transparent;
  background: transparent;
  color: #6f665d;
}

.primary-action:hover,
.secondary-action:hover,
.toggle-action:hover,
.store-card-actions button:hover,
.store-card-actions a:hover {
  transform: translateY(-1px);
}

.store-content {
  padding: 42px 0 86px;
}

.store-shell {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 28px;
  align-items: start;
}

.store-controls {
  position: sticky;
  top: 92px;
  border-top: 3px solid #211f1c;
  background: #fffdf8;
  padding: 22px 0 0;
}

.control-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 22px;
}

.control-heading strong {
  color: #211f1c;
  font-size: 22px;
}

.search-field,
.select-grid label {
  display: grid;
  gap: 8px;
}

.search-field > span,
.select-grid span {
  color: #6f665d;
  font-size: 13px;
  font-weight: 800;
}

.search-field div {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 48px;
  border-bottom: 1px solid #d8d0c6;
}

.search-field i {
  color: #8f1f1d;
}

.search-field input {
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
  color: #211f1c;
}

.select-grid {
  display: grid;
  gap: 16px;
  margin-top: 20px;
}

.filter-select,
.sort-select {
  height: 44px;
  width: 100%;
  border: 1px solid #d8d0c6;
  border-radius: 4px;
  background: #ffffff;
  color: #211f1c;
  padding: 0 12px;
}

.control-actions {
  display: grid;
  gap: 8px;
  margin-top: 20px;
}

.region-group,
.feature-row,
.side-note {
  margin-top: 28px;
  border-top: 1px solid #e7e0d7;
  padding-top: 22px;
}

.region-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.region-tab,
.feature-chip {
  min-height: 34px;
  border: 1px solid #d8d0c6;
  border-radius: 999px;
  background: #fffdf8;
  color: #3b342d;
  padding: 0 13px;
  font-size: 13px;
  font-weight: 800;
}

.region-tab.active,
.feature-chip.active {
  border-color: #211f1c;
  background: #211f1c;
  color: #fffaf0;
}

.feature-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.feature-row p {
  flex: 0 0 100%;
}

.side-note {
  display: grid;
  grid-template-columns: auto auto 1fr;
  align-items: baseline;
  gap: 8px;
  color: #6f665d;
  font-weight: 700;
}

.side-note strong {
  color: #8f1f1d;
  font-size: 28px;
}

.result-toolbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
}

.result-toolbar h2 {
  margin: 4px 0 0;
  color: #211f1c;
  font-size: 32px;
  font-weight: 800;
}

.tool-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.toggle-action {
  border: 1px solid #d8d0c6;
  background: #fffdf8;
  color: #3b342d;
}

.toggle-action.active {
  border-color: #1f6f44;
  color: #1f6f44;
}

.nearby-note,
.notice-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
  border-left: 3px solid #b98a52;
  background: #fffdf8;
  padding: 12px 14px;
  color: #5b5148;
  font-weight: 700;
}

.notice-banner {
  border-left-color: #8f1f1d;
}

.store-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 22px;
  align-items: start;
}

.store-list {
  display: grid;
  gap: 12px;
}

.store-card {
  border: 1px solid #e1d9cf;
  background: #fffdf8;
}

.store-card-active {
  border-color: #8f1f1d;
  box-shadow: 0 12px 34px rgba(36, 28, 21, 0.08);
}

.store-card-main {
  width: 100%;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px 16px;
  border: 0;
  background: transparent;
  padding: 18px 18px 12px;
  color: inherit;
  text-align: left;
}

.store-area {
  color: #8f1f1d;
  font-size: 13px;
  font-weight: 900;
}

.store-card-main strong {
  grid-column: 1 / -1;
  color: #211f1c;
  font-size: 23px;
  font-weight: 800;
}

.store-address {
  grid-column: 1 / -1;
  color: #5b5148;
  line-height: 1.7;
}

.store-meta-line {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: #6f665d;
  font-size: 14px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-self: start;
  min-height: 28px;
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 12px;
  font-weight: 900;
}

.store-card-main .status-pill {
  justify-self: end;
}

.status-pill.open {
  background: #e8f3eb;
  color: #1f6f44;
}

.status-pill.closed {
  background: #eee9e2;
  color: #6f665d;
}

.store-tags,
.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.store-tags {
  padding: 0 18px 14px;
}

.store-tags span,
.detail-tags span {
  border: 1px solid #e1d9cf;
  border-radius: 999px;
  color: #5b5148;
  padding: 5px 9px;
  font-size: 12px;
  font-weight: 800;
}

.store-card-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  border-top: 1px solid #eee8df;
  padding: 12px 18px;
}

.store-card-actions button,
.store-card-actions a {
  min-height: 36px;
  border: 1px solid #d8d0c6;
  background: #ffffff;
  color: #3b342d;
  padding: 0 12px;
  font-size: 13px;
}

.distance {
  margin-left: auto;
  color: #8f1f1d;
  font-weight: 900;
}

.store-detail {
  position: sticky;
  top: 92px;
  max-height: calc(100vh - 112px);
  overflow: auto;
  border: 1px solid #211f1c;
  background: #fffdf8;
  scrollbar-gutter: stable;
}

.store-detail::-webkit-scrollbar {
  width: 8px;
}

.store-detail::-webkit-scrollbar-thumb {
  border: 2px solid #fffdf8;
  border-radius: 999px;
  background: #d8d0c6;
}

.detail-image {
  height: 230px;
  background: #eee9e2;
}

.detail-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-body {
  padding: 22px;
}

.detail-heading {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px 14px;
  align-items: start;
}

.detail-heading p {
  grid-column: 1 / -1;
  margin: 0;
  color: #8f1f1d;
  font-weight: 900;
}

.detail-heading h2 {
  margin: 0;
  color: #211f1c;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.2;
}

.detail-address,
.detail-description,
.muted-text,
.empty-state span {
  color: #5b5148;
}

.detail-address {
  margin: 12px 0 0;
  line-height: 1.7;
}

.detail-actions {
  display: grid;
  grid-template-columns: 1fr 1fr 46px;
  gap: 8px;
  margin: 18px 0;
}

.icon-action {
  width: 46px;
  min-height: 46px;
  border: 1px solid #d8d0c6;
  background: #ffffff;
  color: #6e1b19;
}

.detail-facts {
  display: grid;
  gap: 0;
  border-top: 1px solid #211f1c;
  border-bottom: 1px solid #211f1c;
}

.detail-facts div {
  display: grid;
  grid-template-columns: 86px 1fr;
  gap: 12px;
  padding: 12px 0;
}

.detail-facts div + div {
  border-top: 1px solid #e7e0d7;
}

.detail-facts span,
.info-list dt {
  color: #8f1f1d;
  font-size: 13px;
  font-weight: 900;
}

.detail-facts strong {
  color: #211f1c;
  font-size: 15px;
  line-height: 1.45;
}

.holiday-note {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 14px 0;
  border-left: 3px solid #b98a52;
  background: #faf2e5;
  padding: 10px 12px;
  color: #74542e;
  font-weight: 800;
}

.info-list {
  display: grid;
  gap: 10px;
  margin: 18px 0 0;
}

.info-list div {
  display: grid;
  grid-template-columns: 54px 1fr;
  gap: 12px;
}

.info-list dd {
  margin: 0;
  color: #3b342d;
  line-height: 1.7;
}

.detail-description {
  margin: 18px 0 0;
  line-height: 1.8;
}

.detail-tags {
  margin-top: 16px;
}

.image-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  margin-top: 18px;
}

.image-thumb {
  height: 58px;
  overflow: hidden;
  border: 2px solid transparent;
  border-radius: 0;
  background: #eee9e2;
  padding: 0;
}

.image-thumb.active {
  border-color: #8f1f1d;
}

.image-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hours-block,
.map-panel {
  margin-top: 20px;
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
  color: #211f1c;
  font-size: 17px;
  font-weight: 900;
}

.text-action {
  border: 0;
  background: transparent;
  color: #8f1f1d;
  font-size: 13px;
  font-weight: 900;
}

.hours-list {
  display: grid;
  border-top: 1px solid #e7e0d7;
}

.hours-list div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid #e7e0d7;
  padding: 10px 0;
  color: #5b5148;
}

.hours-list strong {
  color: #211f1c;
}

.map-panel {
  height: 180px;
  overflow: hidden;
  border: 1px solid #d8d0c6;
  background: #eee9e2;
}

.map-panel iframe {
  width: 100%;
  height: 100%;
  border: 0;
}

.loading-state,
.empty-state {
  min-height: 190px;
  display: grid;
  place-items: center;
  gap: 10px;
  border: 1px dashed #d8d0c6;
  background: #fffdf8;
  color: #6f665d;
  text-align: center;
}

.loading-state.compact {
  min-height: 360px;
}

.empty-state i {
  font-size: 34px;
  color: #8f1f1d;
}

.detail-empty {
  min-height: 520px;
  border: 0;
}

@media (max-width: 1080px) {
  .store-shell,
  .store-layout {
    grid-template-columns: 1fr;
  }

  .store-controls,
  .store-detail {
    position: static;
    max-height: none;
  }

  .select-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .store-hero {
    min-height: 430px;
  }

  .store-hero__content {
    padding-top: 130px;
    padding-bottom: 54px;
  }

  .store-hero h1 {
    font-size: 46px;
  }

  .result-toolbar,
  .tool-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .select-grid,
  .detail-actions,
  .image-strip {
    grid-template-columns: 1fr;
  }

  .icon-action {
    width: 100%;
  }

  .store-card-main,
  .detail-heading,
  .detail-facts div,
  .info-list div {
    grid-template-columns: 1fr;
  }

  .store-card-actions {
    align-items: stretch;
  }

  .store-card-actions button,
  .store-card-actions a {
    flex: 1 1 30%;
  }

  .distance {
    flex: 0 0 100%;
    margin-left: 0;
  }
}
</style>
