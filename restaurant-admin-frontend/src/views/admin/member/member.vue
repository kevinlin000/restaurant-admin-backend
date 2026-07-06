<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute } from "vue-router";
import Swal from "sweetalert2";
import { createStaff, getStaffList, resignStaff } from "@/api/member";
import { storeApi } from "@/api/store";

const route = useRoute();

const isLoading = ref(false);
const isSaving = ref(false);
const loadError = ref("");
const staffList = ref([]);
const stores = ref([]);
const keyword = ref("");
const statusFilter = ref("ALL");
const showPassword = ref(false);
const activeSection = ref("form");

const sectionMeta = computed(() => {
  if (activeSection.value === "list") {
    return {
      title: "員工狀態清單",
      desc: "查看所有員工與店長的在職狀態，可依姓名、Email、員工編號或手機搜尋。",
    };
  }

  return {
    title: "員工帳號設定",
    desc: "建立員工/店長帳號，或將既有會員帳號轉為員工/店長帳號。",
  };
});

const normalizeSection = (section) => {
  return section === "list" ? "list" : "form";
};

const form = reactive({
  email: "",
  password: "",
  name: "",
  phone: "",
  birthday: "",
  storeId: "",
  staffNo: "",
  hireDate: "",
  roleName: "STAFF",
});

const roleOptions = [
  { value: "STAFF", label: "員工" },
  { value: "MANAGER", label: "店長" },
];

const statusText = {
  ACTIVE: "在職",
  RESIGNED: "離職",
};

const roleText = {
  STAFF: "員工",
  MANAGER: "店長",
  ADMIN: "管理員",
  CUSTOMER: "會員",
};

const staffNoPrefixMap = {
  STAFF: "S",
  MANAGER: "M",
};

const getTodayString = () => new Date().toISOString().slice(0, 10);

const getNextStaffNoByRole = (roleName) => {
  const prefix = staffNoPrefixMap[roleName] || "S";
  const pattern = new RegExp(`^${prefix}(\\d+)$`, "i");

  const maxNumber = staffList.value.reduce((max, staff) => {
    const staffNo = String(staff.staffNo || "").trim();
    const matched = staffNo.match(pattern);
    if (!matched) return max;

    const number = Number(matched[1]);
    return Number.isFinite(number) ? Math.max(max, number) : max;
  }, 0);

  return `${prefix}${String(maxNumber + 1).padStart(3, "0")}`;
};

const normalizeApiData = (response) => {
  if (Array.isArray(response)) return response;
  if (Array.isArray(response?.data)) return response.data;
  if (Array.isArray(response?.data?.data)) return response.data.data;
  return [];
};

const storeNameMap = computed(() => {
  return stores.value.reduce((map, store) => {
    map[store.storeId] = store.storeName || `門市 #${store.storeId}`;
    return map;
  }, {});
});

const filteredStaffList = computed(() => {
  const key = keyword.value.trim().toLowerCase();

  return staffList.value.filter((staff) => {
    const matchedKeyword =
      !key ||
      [staff.name, staff.email, staff.phone, staff.staffNo, staff.roleName]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(key));

    const matchedStatus =
      statusFilter.value === "ALL" || staff.status === statusFilter.value;

    return matchedKeyword && matchedStatus;
  });
});

const nextStaffNo = computed(() => getNextStaffNoByRole(form.roleName));

const resetForm = () => {
  form.email = "";
  form.password = "";
  form.name = "";
  form.phone = "";
  form.birthday = "";
  form.storeId = stores.value[0]?.storeId || "";
  form.hireDate = getTodayString();
  form.roleName = "STAFF";
  form.staffNo = nextStaffNo.value;
};

const loadData = async () => {
  isLoading.value = true;
  loadError.value = "";

  try {
    const [staffRes, storeRes] = await Promise.all([
      getStaffList(),
      storeApi.getStores({ admin: true }).catch(() => ({ data: [] })),
    ]);

    staffList.value = normalizeApiData(staffRes);
    stores.value = normalizeApiData(storeRes);

    if (!form.storeId && stores.value.length > 0) {
      form.storeId = stores.value[0].storeId;
    }

    form.staffNo = nextStaffNo.value;
  } catch (err) {
    loadError.value =
      err.response?.data?.message || "員工資料載入失敗，請確認後端服務與權限。";
  } finally {
    isLoading.value = false;
  }
};

const handlePhoneInput = () => {
  form.phone = String(form.phone || "")
    .replace(/\D/g, "")
    .slice(0, 10);
};

const validateForm = () => {
  if (
    !form.email ||
    !form.name ||
    !form.birthday ||
    !form.storeId ||
    !form.staffNo ||
    !form.hireDate
  ) {
    return "請完整填寫 Email、姓名、生日、門市、員工編號與到職日";
  }

  if (!/^\S+@\S+\.\S+$/.test(form.email)) {
    return "Email 格式不正確";
  }

  if (
    form.password &&
    (form.password.length < 8 || form.password.length > 20)
  ) {
    return "密碼長度需介於 8 到 20 個字元；既有會員轉員工時可不填";
  }

  if (form.phone && !/^09\d{8}$/.test(form.phone)) {
    return "手機號碼格式必須為 09xxxxxxxx";
  }

  if (!roleOptions.some((role) => role.value === form.roleName)) {
    return "員工管理僅能建立員工或店長帳號";
  }

  return "";
};

const handleCreateStaff = async () => {
  const error = validateForm();
  if (error) {
    Swal.fire({
      icon: "warning",
      title: "資料尚未完成",
      text: error,
      confirmButtonColor: "#e3ac7f",
    });
    return;
  }

  isSaving.value = true;

  try {
    await createStaff({
      email: form.email.trim(),
      password: form.password || null,
      name: form.name.trim(),
      phone: form.phone.trim() || null,
      birthday: form.birthday,
      storeId: Number(form.storeId),
      staffNo: form.staffNo.trim(),
      hireDate: form.hireDate,
      roleName: form.roleName,
    });

    await Swal.fire({
      icon: "success",
      title: "設定完成",
      text: "員工或店長帳號已設定完成；若 Email 已是一般會員，已保留原會員資料。",
      confirmButtonColor: "#e3ac7f",
    });

    resetForm();
    await loadData();
  } catch (err) {
    Swal.fire({
      icon: "error",
      title: "新增失敗",
      text: err.response?.data?.message || "請稍後再試。",
      confirmButtonColor: "#e3ac7f",
    });
  } finally {
    isSaving.value = false;
  }
};

const handleResignStaff = async (staff) => {
  const result = await Swal.fire({
    icon: "warning",
    title: "確認設為離職？",
    text: "設為離職後，該帳號將移除後台權限，但仍保留一般會員身分。",
    showCancelButton: true,
    confirmButtonText: "確認離職",
    cancelButtonText: "取消",
    confirmButtonColor: "#d9534f",
    cancelButtonColor: "#8a99a8",
  });

  if (!result.isConfirmed) return;

  try {
    await resignStaff(staff.staffId);
    await Swal.fire({
      icon: "success",
      title: "狀態已更新",
      text: "員工已設為離職。",
      confirmButtonColor: "#e3ac7f",
    });
    await loadData();
  } catch (err) {
    Swal.fire({
      icon: "error",
      title: "更新失敗",
      text: err.response?.data?.message || "請稍後再試。",
      confirmButtonColor: "#e3ac7f",
    });
  }
};

const formatDate = (dateText) => {
  if (!dateText) return "-";
  return String(dateText).replaceAll("-", "/");
};

watch(
  () => route.query.section,
  (section) => {
    activeSection.value = normalizeSection(section);
  },
  { immediate: true },
);

watch(
  () => form.roleName,
  () => {
    form.staffNo = nextStaffNo.value;
  },
);

onMounted(async () => {
  resetForm();
  await loadData();
});
</script>

<template>
  <section class="staff-page">
    <p v-if="loadError" class="alert-box">
      <i class="bx bx-error-circle"></i>
      {{ loadError }}
    </p>

    <div class="section-summary">
      <div>
        <span class="section-kicker">STAFF OPS</span>
        <h2>{{ sectionMeta.title }}</h2>
        <p>{{ sectionMeta.desc }}</p>
      </div>
    </div>

    <div class="single-section">
      <section
        v-if="activeSection === 'form'"
        class="card form-card section-card"
      >
        <h2>員工帳號設定</h2>
        <div class="form-grid">
          <label>
            Email
            <input v-model.trim="form.email" type="email" />
          </label>

          <label>
            初始密碼（新帳號必填）
            <div class="password-field">
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
              />
              <button
                class="password-toggle"
                type="button"
                :aria-label="showPassword ? '隱藏密碼' : '顯示密碼'"
                @click="showPassword = !showPassword"
              >
                <i :class="showPassword ? 'bx bx-hide' : 'bx bx-show'"></i>
              </button>
            </div>
          </label>

          <label>
            姓名
            <input v-model.trim="form.name" type="text" />
          </label>

          <label>
            手機
            <input
              v-model.trim="form.phone"
              type="tel"
              maxlength="10"
              inputmode="numeric"
              pattern="[0-9]*"
              @input="handlePhoneInput"
            />
          </label>

          <label>
            生日
            <input v-model="form.birthday" type="date" />
          </label>

          <label>
            到職日
            <input v-model="form.hireDate" type="date" />
          </label>

          <label>
            所屬門市
            <select v-model="form.storeId">
              <option value="" disabled>請選擇門市</option>
              <option
                v-for="store in stores"
                :key="store.storeId"
                :value="store.storeId"
              >
                {{ store.storeName }}（#{{ store.storeId }}）
              </option>
            </select>
          </label>

          <label>
            職稱
            <select v-model="form.roleName">
              <option
                v-for="role in roleOptions"
                :key="role.value"
                :value="role.value"
              >
                {{ role.label }}
              </option>
            </select>
          </label>

          <label>
            員工編號
            <select v-model="form.staffNo">
              <option :value="nextStaffNo">
                {{ nextStaffNo }}
              </option>
            </select>
          </label>
        </div>

        <button
          class="submit-btn"
          type="button"
          :disabled="isSaving"
          @click="handleCreateStaff"
        >
          {{ isSaving ? "設定中..." : "建立／設定員工帳號" }}
        </button>
      </section>

      <section v-else class="card list-card section-card">
        <div class="list-header">
          <div>
            <h2>員工狀態清單</h2>
            <p class="card-desc"></p>
          </div>
        </div>

        <div class="toolbar">
          <input
            v-model.trim="keyword"
            type="search"
            placeholder="搜尋姓名、Email、員編、手機"
          />
          <select v-model="statusFilter">
            <option value="ALL">全部狀態</option>
            <option value="ACTIVE">在職</option>
            <option value="RESIGNED">離職</option>
          </select>
        </div>

        <div v-if="isLoading" class="state-box">載入中...</div>
        <div v-else-if="filteredStaffList.length === 0" class="state-box">
          目前沒有符合條件的人員。
        </div>

        <div v-else class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>員工資料</th>
                <th>職稱</th>
                <th>所屬門市</th>
                <th>到職日</th>
                <th>狀態</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="staff in filteredStaffList" :key="staff.staffId">
                <td>
                  <strong>{{ staff.name }}</strong>
                  <span>{{ staff.email }}</span>
                  <small>
                    {{ staff.staffNo || "未設定工號" }}
                    <template v-if="staff.phone">｜{{ staff.phone }}</template>
                  </small>
                </td>
                <td>{{ roleText[staff.roleName] || staff.roleName }}</td>
                <td>
                  {{ storeNameMap[staff.storeId] || `門市 #${staff.storeId}` }}
                </td>
                <td>{{ formatDate(staff.hireDate) }}</td>
                <td>
                  <span
                    class="status-pill"
                    :class="staff.status?.toLowerCase()"
                  >
                    {{ statusText[staff.status] || staff.status || "-" }}
                  </span>
                </td>
                <td>
                  <button
                    class="resign-btn"
                    type="button"
                    :disabled="staff.status !== 'ACTIVE'"
                    @click="handleResignStaff(staff)"
                  >
                    設為離職
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.staff-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.card {
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.05);
}

.card h2 {
  margin: 0;
  color: #566a7f;
  font-weight: 900;
}

.card-desc {
  margin: 9px 0 0;
  color: #7d8b9a;
  line-height: 1.7;
}

.compact-desc {
  font-size: 14px;
}

.submit-btn,
.resign-btn {
  border: none;
  border-radius: 12px;
  font-weight: 800;
  cursor: pointer;
}

.alert-box {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  padding: 14px 18px;
  border-radius: 12px;
  background: #fff2ef;
  color: #c0392b;
  font-weight: 700;
}

.section-summary {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 18px;
  padding: 22px 26px;
  border-radius: 18px;
  background: #fffaf6;
  border: 1px solid #f1dfcf;
}

.section-kicker {
  display: inline-block;
  margin-bottom: 6px;
  color: #b67848;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.12em;
}

.section-summary h2 {
  margin: 0;
  color: #566a7f;
  font-weight: 900;
}

.section-summary p {
  margin: 8px 0 0;
  color: #7d8b9a;
  font-weight: 700;
  line-height: 1.6;
}

.single-section {
  width: 100%;
}

.section-card {
  width: 100%;
}

.form-card.section-card {
  max-width: 1120px;
}

.card {
  padding: 26px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 22px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #566a7f;
  font-weight: 800;
}

input,
select {
  width: 100%;
  border: 1px solid #ead8c8;
  border-radius: 10px;
  padding: 11px 12px;
  color: #566a7f;
  background: #fff;
}

.password-field {
  position: relative;
  width: 100%;
}

.password-field input {
  padding-right: 44px;
}

.password-toggle {
  position: absolute;
  top: 50%;
  right: 10px;
  transform: translateY(-50%);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  border: none;
  background: transparent;
  color: #8a99a8;
  cursor: pointer;
  line-height: 1;
}

.password-toggle i {
  font-size: 19px;
  line-height: 1;
}

.password-toggle:hover {
  color: #e3ac7f;
}

input:disabled,
select:disabled {
  background: #f7f2ed;
  color: #7d8b9a;
  cursor: not-allowed;
}

input:focus,
select:focus {
  outline: none;
  border-color: #e3ac7f;
  box-shadow: 0 0 0 3px rgba(227, 172, 127, 0.18);
}

.submit-btn {
  width: 100%;
  margin-top: 22px;
  padding: 13px 18px;
  background: #e3ac7f;
  color: #fff;
}

button:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.list-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 150px;
  gap: 12px;
  margin: 20px 0;
}

.state-box {
  padding: 28px;
  border-radius: 14px;
  background: #fbfaf8;
  color: #8a99a8;
  text-align: center;
  font-weight: 800;
}

.table-wrap {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 14px 12px;
  border-bottom: 1px solid #f0e2d5;
  text-align: left;
  vertical-align: middle;
  color: #566a7f;
}

th {
  color: #8a99a8;
  font-size: 13px;
  white-space: nowrap;
}

td strong,
td span,
td small {
  display: block;
}

td strong {
  font-weight: 900;
}

td span,
td small {
  margin-top: 4px;
  color: #8a99a8;
}

.status-pill {
  display: inline-flex;
  width: fit-content;
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 900;
}

.status-pill.active {
  background: #eaf7ef;
  color: #2e9f5e;
}

.status-pill.resigned {
  background: #fff2ef;
  color: #c0392b;
}

.resign-btn {
  padding: 9px 12px;
  background: #fff2ef;
  color: #c0392b;
  white-space: nowrap;
}

@media (max-width: 1100px) {
  .form-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .section-summary {
    align-items: flex-start;
    flex-direction: column;
  }

  .form-grid,
  .toolbar {
    grid-template-columns: 1fr;
  }
}
</style>
