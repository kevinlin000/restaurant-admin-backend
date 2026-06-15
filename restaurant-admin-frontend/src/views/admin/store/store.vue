<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import api from "@/api/axios";

const stores = ref([]);
const tables = ref([]);
const selectedStoreId = ref(null);
const loadingStores = ref(false);
const loadingTables = ref(false);
const savingTable = ref(false);
const message = ref("");
const errorMessage = ref("");

const tableForm = reactive({
  tableNumber: "",
  tableSize: 2,
  tableType: "一般桌",
  zone: "主用餐區",
  isCombinable: false,
});

const selectedStore = computed(() =>
  stores.value.find((store) => store.storeId === selectedStoreId.value),
);

const unwrap = (response) => response.data?.data ?? response.data ?? [];

const resetMessages = () => {
  message.value = "";
  errorMessage.value = "";
};

const loadStores = async () => {
  loadingStores.value = true;
  resetMessages();

  try {
    const response = await api.get("/api/admin/stores");
    stores.value = unwrap(response);

    if (!selectedStoreId.value && stores.value.length > 0) {
      selectedStoreId.value = stores.value[0].storeId;
      await loadTables();
    }
  } catch (error) {
    errorMessage.value = "無法載入後台門市清單";
    stores.value = [];
  } finally {
    loadingStores.value = false;
  }
};

const loadTables = async () => {
  if (!selectedStoreId.value) {
    tables.value = [];
    return;
  }

  loadingTables.value = true;
  resetMessages();

  try {
    const response = await api.get(`/api/admin/stores/${selectedStoreId.value}/tables`);
    tables.value = unwrap(response);
  } catch (error) {
    errorMessage.value = "無法載入桌位資料";
    tables.value = [];
  } finally {
    loadingTables.value = false;
  }
};

const selectStore = async (storeId) => {
  selectedStoreId.value = storeId;
  await loadTables();
};

const resetForm = () => {
  tableForm.tableNumber = "";
  tableForm.tableSize = 2;
  tableForm.tableType = "一般桌";
  tableForm.zone = "主用餐區";
  tableForm.isCombinable = false;
};

const createTable = async () => {
  if (!selectedStoreId.value) return;

  savingTable.value = true;
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
    resetForm();
    await loadTables();
  } catch (error) {
    errorMessage.value = error.response?.data?.message || "新增桌位失敗";
  } finally {
    savingTable.value = false;
  }
};

const deleteTable = async (tableId) => {
  if (!selectedStoreId.value) return;

  resetMessages();

  try {
    await api.delete(`/api/admin/stores/${selectedStoreId.value}/tables/${tableId}`);
    message.value = "桌位已刪除";
    await loadTables();
  } catch (error) {
    errorMessage.value = error.response?.data?.message || "刪除桌位失敗";
  }
};

const statusLabel = (status) => {
  const labels = {
    OPEN: "營業中",
    PREPARING: "籌備中",
    PAUSED: "暫停營業",
    CLOSED: "已關閉",
  };
  return labels[status] || status;
};

onMounted(loadStores);
</script>

<template>
  <div class="store-admin-page">
    <header class="page-header">
      <div>
        <h1>分店管理</h1>
        <p>檢視門市狀態與維護桌位資料。</p>
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
        <div v-if="selectedStore" class="selected-store">
          <div>
            <h2>{{ selectedStore.storeName }}</h2>
            <p>{{ selectedStore.address }}</p>
          </div>
          <span :class="['status-chip', selectedStore.status?.toLowerCase()]">
            {{ statusLabel(selectedStore.status) }}
          </span>
        </div>

        <section class="table-tools">
          <h3>新增桌位</h3>
          <form class="table-form" @submit.prevent="createTable">
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
            <button class="submit-btn" :disabled="savingTable || !selectedStoreId" type="submit">
              {{ savingTable ? "新增中" : "新增桌位" }}
            </button>
          </form>
        </section>

        <section class="table-section">
          <div class="panel-title">
            <h3>桌位清單</h3>
            <span>{{ tables.length }} 桌</span>
          </div>

          <div v-if="loadingTables" class="state-box">載入桌位中</div>
          <div v-else-if="tables.length === 0" class="state-box">尚未建立桌位</div>

          <div v-else class="table-grid">
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
          </div>
        </section>
      </main>
    </section>
  </div>
</template>

<style scoped>
.store-admin-page {
  display: grid;
  gap: 20px;
}

.page-header,
.store-panel,
.detail-panel,
.table-tools,
.table-section {
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

.page-header h1 {
  margin: 0 0 6px;
  font-size: 28px;
  font-weight: 800;
}

.page-header p {
  margin: 0;
  color: #6b7280;
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
  background: #e3ac7f;
  color: #ffffff;
  font-weight: 700;
  padding: 0 16px;
}

.notice {
  border-radius: 8px;
  padding: 12px 16px;
  font-weight: 700;
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

.panel-title,
.selected-store {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.panel-title h2,
.panel-title h3,
.selected-store h2,
.table-tools h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
}

.panel-title span {
  color: #8a4f19;
  font-weight: 800;
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
  border-color: #e3ac7f;
  background: #fff8f2;
}

.store-row strong {
  font-size: 16px;
}

.store-row span,
.store-row em,
.selected-store p,
.table-card p {
  color: #6b7280;
  font-style: normal;
}

.status-chip {
  border-radius: 999px;
  background: #f2f0ed;
  color: #736b63;
  padding: 7px 12px;
  font-size: 13px;
  font-weight: 800;
}

.status-chip.open {
  background: #e8f7ee;
  color: #167a3d;
}

.table-tools,
.table-section {
  padding: 18px;
}

.detail-panel {
  display: grid;
  gap: 18px;
}

.table-form {
  display: grid;
  grid-template-columns: repeat(4, minmax(120px, 1fr)) auto auto;
  gap: 12px;
  align-items: end;
}

.table-form label {
  display: grid;
  gap: 6px;
  color: #566a7f;
  font-size: 13px;
  font-weight: 700;
}

.table-form input {
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

.table-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
  gap: 12px;
}

.table-card {
  border: 1px solid #ebe4dc;
  border-radius: 8px;
  padding: 14px;
}

.table-card div,
.table-card footer {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.table-card strong {
  font-size: 18px;
}

.table-card footer {
  align-items: center;
  border-top: 1px solid #f0e9e1;
  padding-top: 10px;
}

.table-card footer button {
  border: 0;
  background: transparent;
  color: #b42318;
  font-weight: 800;
}

.state-box {
  border: 1px dashed #d8c9bc;
  border-radius: 8px;
  padding: 28px;
  color: #6b7280;
  text-align: center;
}

@media (max-width: 1100px) {
  .admin-grid,
  .table-form {
    grid-template-columns: 1fr;
  }
}
</style>
