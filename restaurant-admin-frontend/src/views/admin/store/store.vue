<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import api from "@/api/axios";

const stores = ref([]);
const selectedStoreId = ref(null);
const selectedStoreDetail = ref(null);
const tables = ref([]);
const hours = ref([]);
const holidays = ref([]);
const images = ref([]);
const features = ref([]);
const activeTab = ref("overview");
const loadingStores = ref(false);
const loadingDetail = ref(false);
const saving = ref(false);
const message = ref("");
const errorMessage = ref("");

const tabs = [
  { key: "overview", label: "概要", icon: "bx-store" },
  { key: "hours", label: "營業時間", icon: "bx-time-five" },
  { key: "holidays", label: "特殊公休", icon: "bx-calendar-x" },
  { key: "images", label: "門市圖片", icon: "bx-image" },
  { key: "features", label: "特色標籤", icon: "bx-purchase-tag" },
  { key: "tables", label: "桌位", icon: "bx-chair" },
];

const mealPeriodOptions = [
  { value: "ALL_DAY", label: "整日" },
  { value: "LUNCH", label: "午餐" },
  { value: "DINNER", label: "晚餐" },
  { value: "AFTERNOON_TEA", label: "下午茶" },
];

const tableForm = reactive({
  tableNumber: "",
  tableSize: 2,
  tableType: "REGULAR",
  zone: "主用餐區",
  isCombinable: false,
});

const hourForm = reactive({
  dayOfWeek: 1,
  mealPeriod: "LUNCH",
  openTime: "11:30",
  closeTime: "14:30",
  isClosed: false,
});

const holidayForm = reactive({
  holidayDate: "",
  reason: "店休",
});

const imageForm = reactive({
  imageUrl: "",
  caption: "",
  sortOrder: 0,
});

const featureForm = reactive({
  featureKey: "",
  featureLabel: "",
  sortOrder: 0,
});

const commonFeatureOptions = [
  { featureKey: "BUSINESS", featureLabel: "商務聚餐" },
  { featureKey: "FAMILY", featureLabel: "親子友善" },
  { featureKey: "DATE", featureLabel: "約會推薦" },
  { featureKey: "PRIVATE_ROOM", featureLabel: "包廂" },
  { featureKey: "GROUP", featureLabel: "團體聚餐" },
  { featureKey: "PARKING", featureLabel: "停車方便" },
  { featureKey: "STATION", featureLabel: "車站直達" },
  { featureKey: "QUIET", featureLabel: "安靜用餐" },
];

const selectedStore = computed(
  () => selectedStoreDetail.value ?? stores.value.find((store) => store.storeId === selectedStoreId.value),
);

const totalSeats = computed(() =>
  tables.value.reduce((sum, table) => sum + (Number(table.tableSize) || 0), 0),
);

const availableTableCount = computed(
  () => tables.value.filter((table) => table.status === "AVAILABLE").length,
);

const visibleHeroImage = computed(
  () => selectedStore.value?.mainImageUrl || images.value[0]?.imageUrl || "",
);

const unwrap = (response) => response.data?.data ?? response.data ?? [];

const resetMessages = () => {
  message.value = "";
  errorMessage.value = "";
};

const showError = (error, fallback) => {
  errorMessage.value = error.response?.data?.message || fallback;
};

const statusLabel = (status) => {
  const labels = {
    OPEN: "營業中",
    PREPARING: "籌備中",
    PAUSED: "暫停營業",
    CLOSED: "已關閉",
  };
  return labels[status] || status || "未設定";
};

const mealPeriodLabel = (period) =>
  mealPeriodOptions.find((option) => option.value === period)?.label || period || "未設定";

const formatTime = (time) => (time ? `${time}`.slice(0, 5) : "");

const isClosedHour = (hour) => Boolean(hour.closed ?? hour.isClosed);

const loadStores = async () => {
  loadingStores.value = true;
  resetMessages();

  try {
    const response = await api.get("/api/admin/stores");
    stores.value = unwrap(response);

    if (!selectedStoreId.value && stores.value.length > 0) {
      selectedStoreId.value = stores.value[0].storeId;
    }

    if (selectedStoreId.value) {
      await loadSelectedStoreResources();
    }
  } catch (error) {
    showError(error, "無法載入後台門市清單");
    stores.value = [];
  } finally {
    loadingStores.value = false;
  }
};

const loadSelectedStoreResources = async () => {
  if (!selectedStoreId.value) {
    selectedStoreDetail.value = null;
    tables.value = [];
    hours.value = [];
    holidays.value = [];
    images.value = [];
    features.value = [];
    return;
  }

  loadingDetail.value = true;
  resetMessages();

  try {
    const [detailRes, hoursRes, holidaysRes, imagesRes, featuresRes, tablesRes] = await Promise.all([
      api.get(`/api/admin/stores/${selectedStoreId.value}`),
      api.get(`/api/admin/stores/${selectedStoreId.value}/hours`),
      api.get(`/api/admin/stores/${selectedStoreId.value}/holidays`),
      api.get(`/api/admin/stores/${selectedStoreId.value}/images`),
      api.get(`/api/admin/stores/${selectedStoreId.value}/features`),
      api.get(`/api/admin/stores/${selectedStoreId.value}/tables`),
    ]);

    selectedStoreDetail.value = unwrap(detailRes);
    hours.value = unwrap(hoursRes);
    holidays.value = unwrap(holidaysRes);
    images.value = unwrap(imagesRes);
    features.value = unwrap(featuresRes);
    tables.value = unwrap(tablesRes);
    if (!featureForm.featureKey && !featureForm.featureLabel) {
      featureForm.sortOrder = features.value.length + 1;
    }
  } catch (error) {
    showError(error, "無法載入門市設定資料");
  } finally {
    loadingDetail.value = false;
  }
};

const selectStore = async (storeId) => {
  selectedStoreId.value = storeId;
  await loadSelectedStoreResources();
};

const resetTableForm = () => {
  tableForm.tableNumber = "";
  tableForm.tableSize = 2;
  tableForm.tableType = "REGULAR";
  tableForm.zone = "主用餐區";
  tableForm.isCombinable = false;
};

const resetHolidayForm = () => {
  holidayForm.holidayDate = "";
  holidayForm.reason = "店休";
};

const resetImageForm = () => {
  imageForm.imageUrl = "";
  imageForm.caption = "";
  imageForm.sortOrder = images.value.length;
};

const resetFeatureForm = () => {
  featureForm.featureKey = "";
  featureForm.featureLabel = "";
  featureForm.sortOrder = features.value.length + 1;
};

const applyFeaturePreset = (feature) => {
  featureForm.featureKey = feature.featureKey;
  featureForm.featureLabel = feature.featureLabel;
  featureForm.sortOrder = features.value.length + 1;
};

const createHour = async () => {
  if (!selectedStoreId.value) return;
  saving.value = true;
  resetMessages();

  try {
    await api.post(`/api/admin/stores/${selectedStoreId.value}/hours`, {
      dayOfWeek: Number(hourForm.dayOfWeek),
      mealPeriod: hourForm.mealPeriod,
      openTime: hourForm.openTime,
      closeTime: hourForm.closeTime,
      isClosed: hourForm.isClosed,
    });
    message.value = "營業時間已新增";
    await loadSelectedStoreResources();
  } catch (error) {
    showError(error, "新增營業時間失敗");
  } finally {
    saving.value = false;
  }
};

const deleteHour = async (hourId) => {
  if (!selectedStoreId.value || !window.confirm("確定刪除此營業時間？")) return;
  resetMessages();

  try {
    await api.delete(`/api/admin/stores/${selectedStoreId.value}/hours/${hourId}`);
    message.value = "營業時間已刪除";
    await loadSelectedStoreResources();
  } catch (error) {
    showError(error, "刪除營業時間失敗");
  }
};

const createHoliday = async () => {
  if (!selectedStoreId.value) return;
  saving.value = true;
  resetMessages();

  try {
    await api.post(`/api/admin/stores/${selectedStoreId.value}/holidays`, holidayForm);
    message.value = "特殊公休日已新增";
    resetHolidayForm();
    await loadSelectedStoreResources();
  } catch (error) {
    showError(error, "新增特殊公休日失敗");
  } finally {
    saving.value = false;
  }
};

const deleteHoliday = async (holidayId) => {
  if (!selectedStoreId.value || !window.confirm("確定刪除此公休日？")) return;
  resetMessages();

  try {
    await api.delete(`/api/admin/stores/${selectedStoreId.value}/holidays/${holidayId}`);
    message.value = "特殊公休日已刪除";
    await loadSelectedStoreResources();
  } catch (error) {
    showError(error, "刪除特殊公休日失敗");
  }
};

const createImage = async () => {
  if (!selectedStoreId.value) return;
  saving.value = true;
  resetMessages();

  try {
    await api.post(`/api/admin/stores/${selectedStoreId.value}/images`, {
      imageUrl: imageForm.imageUrl,
      caption: imageForm.caption,
      sortOrder: Number(imageForm.sortOrder) || 0,
    });
    message.value = "門市圖片已新增";
    resetImageForm();
    await loadSelectedStoreResources();
  } catch (error) {
    showError(error, "新增門市圖片失敗");
  } finally {
    saving.value = false;
  }
};

const deleteImage = async (imageId) => {
  if (!selectedStoreId.value || !window.confirm("確定刪除此圖片？")) return;
  resetMessages();

  try {
    await api.delete(`/api/admin/stores/${selectedStoreId.value}/images/${imageId}`);
    message.value = "門市圖片已刪除";
    await loadSelectedStoreResources();
  } catch (error) {
    showError(error, "刪除門市圖片失敗");
  }
};

const createFeature = async () => {
  if (!selectedStoreId.value) return;
  saving.value = true;
  resetMessages();

  try {
    await api.post(`/api/admin/stores/${selectedStoreId.value}/features`, {
      featureKey: featureForm.featureKey,
      featureLabel: featureForm.featureLabel,
      sortOrder: Number(featureForm.sortOrder) || 0,
    });
    message.value = "特色標籤已新增";
    resetFeatureForm();
    await loadSelectedStoreResources();
  } catch (error) {
    showError(error, "新增特色標籤失敗");
  } finally {
    saving.value = false;
  }
};

const deleteFeature = async (featureId) => {
  if (!selectedStoreId.value || !window.confirm("確定刪除此特色標籤？")) return;
  resetMessages();

  try {
    await api.delete(`/api/admin/stores/${selectedStoreId.value}/features/${featureId}`);
    message.value = "特色標籤已刪除";
    await loadSelectedStoreResources();
  } catch (error) {
    showError(error, "刪除特色標籤失敗");
  }
};

const createTable = async () => {
  if (!selectedStoreId.value) return;
  saving.value = true;
  resetMessages();

  try {
    await api.post(`/api/admin/stores/${selectedStoreId.value}/tables`, {
      tableNumber: tableForm.tableNumber,
      tableSize: Number(tableForm.tableSize),
      tableType: tableForm.tableType,
      zone: tableForm.zone,
      isCombinable: tableForm.isCombinable,
    });
    message.value = "桌位已新增";
    resetTableForm();
    await loadSelectedStoreResources();
  } catch (error) {
    showError(error, "新增桌位失敗");
  } finally {
    saving.value = false;
  }
};

const deleteTable = async (tableId) => {
  if (!selectedStoreId.value || !window.confirm("確定刪除此桌位？")) return;
  resetMessages();

  try {
    await api.delete(`/api/admin/stores/${selectedStoreId.value}/tables/${tableId}`);
    message.value = "桌位已刪除";
    await loadSelectedStoreResources();
  } catch (error) {
    showError(error, "刪除桌位失敗");
  }
};

onMounted(loadStores);
</script>

<template>
  <div class="store-admin-page">
    <header class="page-header">
      <div>
        <span>STORE OPERATIONS</span>
        <h1>分店營運設定</h1>
        <p>維護前台找門市會顯示的營業時間、公休日、門市圖片與桌位容量。</p>
      </div>
      <button class="refresh-btn" type="button" @click="loadStores">
        <i class="bx bx-refresh"></i>
        重新整理
      </button>
    </header>

    <div v-if="message" class="notice success">{{ message }}</div>
    <div v-if="errorMessage" class="notice error">{{ errorMessage }}</div>

    <section class="admin-grid">
      <aside class="store-panel">
        <div class="panel-title">
          <h2>門市清單</h2>
          <span>{{ stores.length }} 間</span>
        </div>

        <div v-if="loadingStores" class="state-box">載入門市中</div>
        <div v-else-if="stores.length === 0" class="state-box">目前沒有門市資料</div>

        <button
          v-for="store in stores"
          v-else
          :key="store.storeId"
          type="button"
          :class="['store-row', selectedStoreId === store.storeId ? 'active' : '']"
          @click="selectStore(store.storeId)"
        >
          <strong>{{ store.storeName }}</strong>
          <span>{{ store.city }} {{ store.district }}</span>
          <em>{{ statusLabel(store.status) }}</em>
        </button>
      </aside>

      <main class="detail-panel">
        <div v-if="!selectedStore" class="state-box large">請先選擇門市</div>

        <template v-else>
          <section class="store-summary">
            <div class="summary-image">
              <img v-if="visibleHeroImage" :src="visibleHeroImage" :alt="selectedStore.storeName" />
              <i v-else class="bx bx-store"></i>
            </div>
            <div class="summary-copy">
              <span :class="['status-chip', selectedStore.status?.toLowerCase()]">
                {{ statusLabel(selectedStore.status) }}
              </span>
              <h2>{{ selectedStore.storeName }}</h2>
              <p>{{ selectedStore.address }}</p>
            </div>
            <div class="summary-metrics">
              <div>
                <strong>{{ hours.length }}</strong>
                <span>營業時段</span>
              </div>
              <div>
                <strong>{{ holidays.length }}</strong>
                <span>公休日</span>
              </div>
              <div>
                <strong>{{ features.length }}</strong>
                <span>特色標籤</span>
              </div>
              <div>
                <strong>{{ availableTableCount }}/{{ tables.length }}</strong>
                <span>可用桌位</span>
              </div>
              <div>
                <strong>{{ totalSeats }}</strong>
                <span>座位數</span>
              </div>
            </div>
          </section>

          <nav class="tab-row" aria-label="門市設定分頁">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              type="button"
              :class="['tab-button', activeTab === tab.key ? 'active' : '']"
              @click="activeTab = tab.key"
            >
              <i :class="['bx', tab.icon]"></i>
              {{ tab.label }}
            </button>
          </nav>

          <div v-if="loadingDetail" class="state-box">載入門市設定中</div>

          <section v-else-if="activeTab === 'overview'" class="content-section">
            <div class="info-grid">
              <div>
                <span>電話</span>
                <strong>{{ selectedStore.phone || "未提供" }}</strong>
              </div>
              <div>
                <span>縣市區域</span>
                <strong>{{ selectedStore.city }} {{ selectedStore.district }}</strong>
              </div>
              <div>
                <span>捷運資訊</span>
                <strong>{{ selectedStore.mrtInfo || "尚未設定" }}</strong>
              </div>
              <div>
                <span>停車資訊</span>
                <strong>{{ selectedStore.parkingInfo || "尚未設定" }}</strong>
              </div>
            </div>
            <article class="description-card">
              <h3>門市特色</h3>
              <p>{{ selectedStore.description || "尚未填寫門市特色。" }}</p>
            </article>
            <article class="description-card">
              <h3>前台情境標籤</h3>
              <div v-if="features.length" class="feature-list compact">
                <span v-for="feature in features" :key="feature.featureId">
                  {{ feature.featureLabel }}
                </span>
              </div>
              <p v-else>尚未設定情境標籤，前台無法依用餐情境推薦此門市。</p>
            </article>
          </section>

          <section v-else-if="activeTab === 'hours'" class="content-section">
            <form class="setting-form" @submit.prevent="createHour">
              <label>
                星期
                <select v-model.number="hourForm.dayOfWeek">
                  <option v-for="day in 7" :key="day" :value="day">週{{ "一二三四五六日"[day - 1] }}</option>
                </select>
              </label>
              <label>
                時段
                <select v-model="hourForm.mealPeriod">
                  <option v-for="option in mealPeriodOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </label>
              <label>
                開始
                <input v-model="hourForm.openTime" type="time" required />
              </label>
              <label>
                結束
                <input v-model="hourForm.closeTime" type="time" required />
              </label>
              <label class="checkbox-field">
                <input v-model="hourForm.isClosed" type="checkbox" />
                公休
              </label>
              <button class="submit-btn" :disabled="saving" type="submit">新增時段</button>
            </form>

            <div class="data-list">
              <article v-for="hour in hours" :key="hour.hourId" class="data-card">
                <div>
                  <strong>{{ hour.dayName }} {{ mealPeriodLabel(hour.mealPeriod) }}</strong>
                  <span v-if="isClosedHour(hour)">公休</span>
                  <span v-else>{{ formatTime(hour.openTime) }} - {{ formatTime(hour.closeTime) }}</span>
                </div>
                <button type="button" @click="deleteHour(hour.hourId)">刪除</button>
              </article>
              <div v-if="hours.length === 0" class="state-box">尚未建立營業時間</div>
            </div>
          </section>

          <section v-else-if="activeTab === 'holidays'" class="content-section">
            <form class="setting-form three" @submit.prevent="createHoliday">
              <label>
                公休日期
                <input v-model="holidayForm.holidayDate" type="date" required />
              </label>
              <label>
                原因
                <input v-model.trim="holidayForm.reason" maxlength="100" type="text" />
              </label>
              <button class="submit-btn" :disabled="saving" type="submit">新增公休日</button>
            </form>

            <div class="data-list">
              <article v-for="holiday in holidays" :key="holiday.holidayId" class="data-card">
                <div>
                  <strong>{{ holiday.holidayDate }}</strong>
                  <span>{{ holiday.reason || "門市公休" }}</span>
                </div>
                <button type="button" @click="deleteHoliday(holiday.holidayId)">刪除</button>
              </article>
              <div v-if="holidays.length === 0" class="state-box">尚未建立特殊公休日</div>
            </div>
          </section>

          <section v-else-if="activeTab === 'images'" class="content-section">
            <form class="setting-form image-form" @submit.prevent="createImage">
              <label>
                圖片 URL
                <input v-model.trim="imageForm.imageUrl" maxlength="500" required type="url" />
              </label>
              <label>
                圖說
                <input v-model.trim="imageForm.caption" maxlength="100" type="text" />
              </label>
              <label>
                排序
                <input v-model.number="imageForm.sortOrder" min="0" type="number" />
              </label>
              <button class="submit-btn" :disabled="saving" type="submit">新增圖片</button>
            </form>

            <div class="image-grid">
              <article v-for="image in images" :key="image.imageId" class="image-card">
                <img :src="image.imageUrl" :alt="image.caption || selectedStore.storeName" />
                <div>
                  <strong>{{ image.caption || "未命名圖片" }}</strong>
                  <span>排序 {{ image.sortOrder ?? 0 }}</span>
                </div>
                <button type="button" @click="deleteImage(image.imageId)">刪除</button>
              </article>
              <div v-if="images.length === 0" class="state-box">尚未建立門市圖片</div>
            </div>
          </section>

          <section v-else-if="activeTab === 'features'" class="content-section">
            <div class="preset-row">
              <span>常用標籤</span>
              <button
                v-for="feature in commonFeatureOptions"
                :key="feature.featureKey"
                type="button"
                @click="applyFeaturePreset(feature)"
              >
                {{ feature.featureLabel }}
              </button>
            </div>

            <form class="setting-form feature-form" @submit.prevent="createFeature">
              <label>
                標籤代碼
                <input
                  v-model.trim="featureForm.featureKey"
                  maxlength="40"
                  placeholder="PRIVATE_ROOM"
                  required
                  type="text"
                />
              </label>
              <label>
                顯示名稱
                <input
                  v-model.trim="featureForm.featureLabel"
                  maxlength="30"
                  placeholder="包廂"
                  required
                  type="text"
                />
              </label>
              <label>
                排序
                <input v-model.number="featureForm.sortOrder" min="0" type="number" />
              </label>
              <button class="submit-btn" :disabled="saving" type="submit">新增標籤</button>
            </form>

            <div class="feature-list">
              <article v-for="feature in features" :key="feature.featureId" class="feature-card">
                <div>
                  <strong>{{ feature.featureLabel }}</strong>
                  <span>{{ feature.featureKey }} · 排序 {{ feature.sortOrder ?? 0 }}</span>
                </div>
                <button type="button" @click="deleteFeature(feature.featureId)">刪除</button>
              </article>
              <div v-if="features.length === 0" class="state-box">
                尚未建立特色標籤，前台情境篩選不會顯示此店特色。
              </div>
            </div>
          </section>

          <section v-else-if="activeTab === 'tables'" class="content-section">
            <form class="setting-form table-form" @submit.prevent="createTable">
              <label>
                桌號
                <input v-model.trim="tableForm.tableNumber" required maxlength="10" type="text" />
              </label>
              <label>
                座位數
                <input v-model.number="tableForm.tableSize" required min="1" type="number" />
              </label>
              <label>
                類型
                <input v-model.trim="tableForm.tableType" maxlength="20" type="text" />
              </label>
              <label>
                區域
                <input v-model.trim="tableForm.zone" maxlength="20" type="text" />
              </label>
              <label class="checkbox-field">
                <input v-model="tableForm.isCombinable" type="checkbox" />
                可併桌
              </label>
              <button class="submit-btn" :disabled="saving" type="submit">新增桌位</button>
            </form>

            <div class="table-grid">
              <article v-for="table in tables" :key="table.tableId" class="table-card">
                <div>
                  <strong>{{ table.tableNumber }}</strong>
                  <span>{{ table.tableSize }} 人桌</span>
                </div>
                <p>{{ table.zone || "未分區" }} · {{ table.tableType || "一般桌" }}</p>
                <footer>
                  <span>{{ table.status }}</span>
                  <button type="button" @click="deleteTable(table.tableId)">刪除</button>
                </footer>
              </article>
              <div v-if="tables.length === 0" class="state-box">尚未建立桌位</div>
            </div>
          </section>
        </template>
      </main>
    </section>
  </div>
</template>

<style scoped>
.store-admin-page {
  display: grid;
  gap: 20px;
  color: #344051;
}

.page-header,
.store-panel,
.detail-panel,
.store-summary,
.content-section {
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding: 24px;
}

.page-header span {
  color: #b1642f;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.1em;
}

.page-header h1 {
  margin: 4px 0 6px;
  font-size: 28px;
  font-weight: 900;
}

.page-header p,
.store-row span,
.store-row em,
.summary-copy p,
.description-card p,
.data-card span,
.image-card span,
.table-card p {
  margin: 0;
  color: #697386;
  font-style: normal;
}

.refresh-btn,
.submit-btn {
  height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 0;
  border-radius: 8px;
  background: #b1642f;
  color: #ffffff;
  font-weight: 800;
  padding: 0 16px;
}

.submit-btn:disabled {
  opacity: 0.65;
}

.notice {
  border-radius: 8px;
  padding: 12px 16px;
  font-weight: 800;
}

.notice.success {
  background: #e8f7ee;
  color: #167a3d;
}

.notice.error {
  background: #fdecec;
  color: #b42318;
}

.admin-grid {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.store-panel,
.detail-panel {
  padding: 20px;
}

.panel-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.panel-title h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 900;
}

.panel-title span {
  color: #b1642f;
  font-weight: 900;
}

.store-row {
  width: 100%;
  display: grid;
  gap: 4px;
  border: 1px solid #ebe4dc;
  border-radius: 8px;
  background: #ffffff;
  padding: 14px;
  text-align: left;
  margin-bottom: 10px;
}

.store-row.active {
  border-color: #b1642f;
  background: #fff8f2;
}

.store-row strong {
  color: #263445;
  font-size: 16px;
}

.detail-panel {
  display: grid;
  gap: 18px;
  min-width: 0;
}

.store-summary {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr) minmax(280px, 0.8fr);
  gap: 20px;
  align-items: center;
  padding: 18px;
}

.summary-image {
  height: 120px;
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: 8px;
  background: #f7f3ee;
}

.summary-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.summary-image i {
  color: #b1642f;
  font-size: 36px;
}

.summary-copy h2 {
  margin: 10px 0 8px;
  color: #263445;
  font-size: 26px;
  font-weight: 900;
}

.summary-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.summary-metrics div,
.info-grid div,
.description-card {
  border: 1px solid #eee5dd;
  border-radius: 8px;
  background: #fbf8f5;
  padding: 14px;
}

.summary-metrics strong {
  display: block;
  color: #a2322f;
  font-size: 24px;
  font-weight: 900;
}

.summary-metrics span,
.info-grid span {
  color: #697386;
  font-size: 13px;
  font-weight: 800;
}

.status-chip {
  display: inline-flex;
  border-radius: 999px;
  background: #f2f0ed;
  color: #736b63;
  padding: 7px 12px;
  font-size: 13px;
  font-weight: 900;
}

.status-chip.open {
  background: #e8f7ee;
  color: #167a3d;
}

.status-chip.paused,
.status-chip.preparing {
  background: #fff2d5;
  color: #a16012;
}

.tab-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tab-button {
  height: 42px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1px solid #d7c6b7;
  border-radius: 8px;
  background: #ffffff;
  color: #8c552e;
  font-weight: 900;
  padding: 0 14px;
}

.tab-button.active {
  border-color: #1c2c3d;
  background: #1c2c3d;
  color: #ffffff;
}

.content-section {
  display: grid;
  gap: 18px;
  padding: 18px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.info-grid strong {
  display: block;
  margin-top: 6px;
  color: #263445;
  line-height: 1.5;
}

.description-card h3 {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 900;
}

.setting-form {
  display: grid;
  grid-template-columns: repeat(4, minmax(120px, 1fr)) auto auto;
  gap: 12px;
  align-items: end;
}

.setting-form.three {
  grid-template-columns: 180px minmax(220px, 1fr) auto;
}

.image-form {
  grid-template-columns: minmax(280px, 1fr) minmax(180px, 0.5fr) 100px auto;
}

.feature-form {
  grid-template-columns: minmax(180px, 0.8fr) minmax(180px, 0.8fr) 100px auto;
}

.setting-form label {
  display: grid;
  gap: 6px;
  color: #566a7f;
  font-size: 13px;
  font-weight: 800;
}

.setting-form input,
.setting-form select {
  height: 40px;
  border: 1px solid #e4ddd3;
  border-radius: 8px;
  padding: 0 10px;
}

.checkbox-field {
  min-height: 40px;
  display: flex !important;
  grid-template-columns: none !important;
  align-items: center;
  gap: 8px !important;
}

.checkbox-field input {
  width: 16px;
  height: 16px;
}

.data-list {
  display: grid;
  gap: 10px;
}

.data-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  border: 1px solid #ebe4dc;
  border-radius: 8px;
  padding: 14px;
}

.data-card strong {
  display: block;
  color: #263445;
}

.data-card button,
.table-card button,
.image-card button {
  border: 0;
  background: transparent;
  color: #b42318;
  font-weight: 900;
}

.image-grid,
.table-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 12px;
}

.preset-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.preset-row span {
  margin-right: 4px;
  color: #8c552e;
  font-size: 13px;
  font-weight: 900;
}

.preset-row button,
.feature-list.compact span {
  border-radius: 999px;
  background: #faf3ea;
  color: #8c552e;
  font-size: 13px;
  font-weight: 900;
}

.preset-row button {
  border: 1px solid #ead8c6;
  padding: 8px 11px;
}

.feature-list {
  display: grid;
  gap: 10px;
}

.feature-list.compact {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.feature-list.compact span {
  display: inline-flex;
  padding: 6px 10px;
}

.feature-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  border: 1px solid #ebe4dc;
  border-radius: 8px;
  padding: 14px;
}

.feature-card strong {
  display: block;
  color: #263445;
}

.feature-card span {
  color: #697386;
  font-size: 13px;
}

.feature-card button {
  border: 0;
  background: transparent;
  color: #b42318;
  font-weight: 900;
}

.image-card,
.table-card {
  border: 1px solid #ebe4dc;
  border-radius: 8px;
  overflow: hidden;
  background: #ffffff;
}

.image-card img {
  width: 100%;
  height: 130px;
  object-fit: cover;
  background: #f7f3ee;
}

.image-card div,
.table-card {
  padding: 14px;
}

.image-card strong,
.table-card strong {
  display: block;
  color: #263445;
  font-size: 17px;
}

.image-card button {
  margin: 0 14px 14px;
}

.table-card div,
.table-card footer {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.table-card p {
  margin: 10px 0 0;
}

.table-card footer {
  align-items: center;
  border-top: 1px solid #f0e9e1;
  margin-top: 12px;
  padding-top: 10px;
}

.state-box {
  border: 1px dashed #d8c9bc;
  border-radius: 8px;
  padding: 28px;
  color: #697386;
  text-align: center;
}

.state-box.large {
  min-height: 360px;
  display: grid;
  place-items: center;
}

@media (max-width: 1200px) {
  .admin-grid,
  .store-summary,
  .setting-form,
  .setting-form.three,
  .image-form,
  .feature-form {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-header {
    align-items: stretch;
    flex-direction: column;
  }

  .info-grid,
  .summary-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
