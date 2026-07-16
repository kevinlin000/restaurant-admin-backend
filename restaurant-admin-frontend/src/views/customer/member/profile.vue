<template>
  <div class="member-page">
    <div class="member-shell">
      <div v-if="isLoading" class="state-box">載入中...</div>

      <div v-else-if="errorMsg" class="state-box error-msg">
        {{ errorMsg }}
      </div>

      <template v-else>
        <aside class="member-sidebar">
          <div class="sidebar-card level-card compact-level-card">
            <div class="level-badge">{{ memberLevelText }}</div>
            <p class="level-discount">{{ memberDiscountText }}</p>
          </div>

          <div class="sidebar-card point-card combined-point-card">
            <div class="point-section discount-point-section">
              <div class="point-heading-row">
                <p class="sidebar-title point-card-title">折抵點數</p>
                <div class="point-value-inline">
                  <div class="point-main pulse-point" aria-label="折抵點數">
                    <span class="point-number">{{ pointInfo.pointBalance }}</span>
                  </div>
                  <span class="point-unit-label">點</span>
                </div>
              </div>
            </div>

            <div class="point-card-divider"></div>

            <div class="point-section member-point-section">
              <div class="point-heading-row">
                <p class="sidebar-title point-card-title">會員點數</p>
                <div class="point-value-inline">
                  <div class="point-main pulse-point" aria-label="會員點數">
                    <span class="point-number">{{ pointInfo.pointLevel }}</span>
                  </div>
                  <span class="point-unit-label">點</span>
                </div>
              </div>

              <div class="level-progress" aria-label="會員等級進度">
                <div class="level-progress-meta">
                  <span>{{ currentLevelShortText }}</span>
                  <span>{{ nextLevelShortText }}</span>
                </div>
                <div class="level-progress-track">
                  <div
                    class="level-progress-fill"
                    :style="{ width: `${levelProgressPercent}%` }"
                  ></div>
                </div>
                <p class="level-progress-text">{{ levelProgressText }}</p>
              </div>

              <button
                class="point-rule-btn tooltip-trigger tooltip-wide"
                type="button"
                :data-tooltip="pointRuleTooltip"
                @click="showPointRuleModal = true"
              >
                會員集點規則
              </button>
            </div>
          </div>
        </aside>

        <section class="member-content">
          <div class="tab-bar">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              class="tab-btn"
              :class="{ active: activeTab === tab.key }"
              type="button"
              @click="switchTab(tab.key)"
            >
              <i :class="tab.icon"></i>
              {{ tab.label }}
            </button>

            <button
              v-if="activeTab === 'profile' && !isEditingProfile"
              class="edit-profile-btn tab-edit-profile-btn"
              type="button"
              @click="startEditProfile"
            >
              編輯資料
            </button>
          </div>

          <template v-if="activeTab === 'profile'">
            <div class="info-card">
              <div class="info-row">
                <div class="info-label">{{ nameLabel }}</div>
                <div v-if="!isEditingProfile" class="info-value">
                  {{ userInfo.name }}
                </div>
                <div v-else class="edit-field">
                  <input
                    v-model.trim="profileForm.name"
                    type="text"
                    placeholder="請輸入姓名"
                    maxlength="50"
                  />
                </div>
              </div>

              <div class="info-row">
                <div class="info-label">電話</div>
                <div v-if="!isEditingProfile" class="info-value">
                  {{ userInfo.phone }}
                </div>
                <div v-else class="edit-field">
                  <input
                    v-model.trim="profileForm.phone"
                    type="tel"
                    placeholder="請輸入手機號碼，例如 0912345678"
                    maxlength="10"
                  />
                  <p
                    v-if="profileForm.phone && !isPhoneValid"
                    class="field-error"
                  >
                    手機號碼格式必須為 09xxxxxxxx
                  </p>
                </div>
              </div>

              <div class="info-row readonly-row readonly-hint-row">
                <div class="info-label">Email</div>
                <div class="info-value">{{ userInfo.email }}</div>
                <div v-if="isEditingProfile" class="readonly-inline-hint">
                  Email 作為登入帳號使用，不開放修改。
                </div>
              </div>

              <div class="info-row readonly-row readonly-hint-row birthday-hint-row">
                <div class="info-label">生日</div>
                <div class="info-value birthday-value">
                  <span>{{ formatBirthday(userInfo.birthday) }}</span>
                  <span class="birthday-inline-note">
                    <span>生日當月於敘日消費，即可獲得焦糖布丁 1 份。</span>
                    <img
                      :src="puddingImg"
                      alt="焦糖布丁"
                      class="birthday-pudding-inline"
                    />
                  </span>
                </div>
                <div v-if="isEditingProfile" class="readonly-inline-hint">
                  生日作為優惠判斷依據，不開放修改。
                </div>
              </div>

              <div class="info-row password-row">
                <div class="info-label">密碼</div>
                <div class="info-value password-dots">••••••••</div>
                <button
                  class="edit-profile-btn outline"
                  type="button"
                  @click="openPasswordModal"
                >
                  修改密碼
                </button>
              </div>

              <div v-if="isEditingProfile" class="profile-actions">
                <button
                  class="cancel-profile-btn"
                  type="button"
                  :disabled="isSavingProfile"
                  @click="cancelEditProfile"
                >
                  取消
                </button>
                <button
                  class="save-profile-btn"
                  type="button"
                  :disabled="!canSubmitProfile || isSavingProfile"
                  @click="handleUpdateProfile"
                >
                  {{ isSavingProfile ? "儲存中..." : "儲存修改" }}
                </button>
              </div>
            </div>
          </template>

          <template v-else-if="activeTab === 'reservations'">
            <div class="section-header">
              <div>
                <h2>訂位紀錄</h2>
                <p>查看會員登入後建立的訂位紀錄。</p>
              </div>
            </div>

            <div v-if="isReservationLoading" class="state-box inner">
              訂位紀錄載入中...
            </div>


            <div v-else-if="reservations.length" class="record-card">
              <table>
                <thead>
                  <tr>
                    <th>訂位編號</th>
                    <th>訂位日期</th>
                    <th>訂位時間</th>
                    <th>店名</th>
                    <th>人數</th>
                    <th>狀態</th>
                    <th>備註</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="reservation in reservations"
                    :key="reservation.reservationId"
                  >
                    <td>#{{ reservation.reservationId }}</td>
                    <td>{{ formatDate(reservation.reservationDate) }}</td>
                    <td>{{ formatReservationTime(reservation) }}</td>
                    <td>{{ formatStore(reservation) }}</td>
                    <td>{{ reservation.partySize || 0 }} 人</td>
                    <td>
                      <span
                        class="status-badge"
                        :class="getReservationStatusClass(reservation.status)"
                      >
                        {{ getReservationStatusText(reservation.status) }}
                      </span>
                    </td>
                    <td class="note-cell">
                      {{ reservation.specialRequest || "無" }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-else class="empty-card action-empty-card">
              <i class="bx bx-calendar-check"></i>
              <h3>尚無訂位紀錄</h3>
              <p>目前沒有可顯示的訂位資料。</p>
              <button class="empty-action-btn" type="button" @click="goToReservation">
                立即訂位
              </button>
            </div>
          </template>

          <template v-else-if="activeTab === 'orders'">
            <div class="section-header">
              <div>
                <h2>消費紀錄</h2>
                <p>查看會員登入後建立的訂單消費紀錄。</p>
              </div>
            </div>

            <div v-if="isOrderLoading" class="state-box inner">
              消費紀錄載入中...
            </div>


            <div v-else-if="orders.length" class="record-card">
              <table>
                <thead>
                  <tr>
                    <th>訂單編號</th>
                    <th>消費日期</th>
                    <th>訂單類型</th>
                    <th>門市</th>
                    <th class="right">消費金額</th>
                    <th>付款狀態</th>
                    <th>訂單狀態</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="order in orders" :key="order.orderId">
                    <td>#{{ order.orderId }}</td>
                    <td>{{ formatDateTime(order.createdAt) }}</td>
                    <td>{{ getOrderTypeText(order.orderType) }}</td>
                    <td>{{ formatOrderStore(order) }}</td>
                    <td class="right">{{ formatCurrency(getOrderAmount(order)) }}</td>
                    <td>
                      <span
                        class="status-badge"
                        :class="getPaymentStatusClass(order.paymentStatus)"
                      >
                        {{ getPaymentStatusText(order.paymentStatus) }}
                      </span>
                    </td>
                    <td>
                      <span
                        class="status-badge"
                        :class="getOrderStatusClass(order.status)"
                      >
                        {{ getOrderStatusText(order.status) }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-else class="empty-card action-empty-card">
              <i class="bx bx-receipt"></i>
              <h3>尚無消費紀錄</h3>
              <p>目前沒有可顯示的消費資料。</p>
              <button class="empty-action-btn" type="button" @click="goToMenu">
                前往點餐
              </button>
            </div>
          </template>

          <template v-else-if="activeTab === 'points'">
            <div class="section-header">
              <div>
                <h2>點數紀錄</h2>
                <p>查看折抵點數與會員升級點數的異動紀錄。</p>
              </div>
            </div>

            <div class="point-history-summary">
              <div class="history-summary-card earn">
                <span class="summary-label">累積點數</span>
                <strong>+{{ pointHistoryStats.earned }}</strong>
                <small>消費獲得與退回</small>
              </div>
              <div class="history-summary-card use">
                <span class="summary-label">使用點數</span>
                <strong>-{{ pointHistoryStats.used }}</strong>
                <small>訂單折抵使用</small>
              </div>
              <div class="history-summary-card balance">
                <span class="summary-label">目前折抵點數</span>
                <strong>{{ pointInfo.pointBalance }}</strong>
                <small>結帳時可使用</small>
              </div>
            </div>

            <div v-if="isPointHistoryLoading" class="state-box inner">
              點數紀錄載入中...
            </div>

            <div v-else-if="pointHistory.length" class="record-card">
              <table>
                <thead>
                  <tr>
                    <th>異動日期</th>
                    <th>類型</th>
                    <th>門市</th>
                    <th>關聯訂單</th>
                    <th class="right">點數異動</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="tx in pointHistory" :key="tx.txId">
                    <td>{{ formatDateTime(tx.createdAt) }}</td>
                    <td>
                      <span
                        class="status-badge"
                        :class="getPointTypeClass(tx.transactionType)"
                      >
                        {{ getPointTypeText(tx.transactionType) }}
                      </span>
                    </td>
                    <td>{{ tx.storeName || "未提供" }}</td>
                    <td>{{ tx.referenceId ? `#${tx.referenceId}` : "未綁定" }}</td>
                    <td
                      class="right"
                      :class="getPointChangeClass(tx.pointChange)"
                    >
                      {{ formatPointChange(tx.pointChange) }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-else class="empty-card action-empty-card">
              <i class="bx bx-coin-stack"></i>
              <h3>尚無點數紀錄</h3>
              <p>完成會員訂單或使用點數折抵後，這裡會顯示異動紀錄。</p>
              <button class="empty-action-btn" type="button" @click="goToMenu">
                前往點餐累積點數
              </button>
            </div>
          </template>
        </section>
      </template>
    </div>

    <div
      v-if="showPointRuleModal"
      class="rule-modal-mask"
      @click.self="showPointRuleModal = false"
    >
      <div class="rule-modal">
        <button
          class="rule-close-btn"
          type="button"
          aria-label="關閉集點規則"
          @click="showPointRuleModal = false"
        >
          ×
        </button>
        <h3>集點規則</h3>
        <div class="rule-content">
          <div class="rule-section">
            <h4>累積規則</h4>
            <p>每消費 $100 即可累積 1 點。</p>
          </div>
          <div class="rule-section">
            <h4>會員升級</h4>
            <ul>
              <li>銅卡會員：0 ~ 29 點，來店消費享 95 折</li>
              <li>銀卡會員：30 ~ 59 點，來店消費享 9 折</li>
              <li>金卡會員：60 ~ 99 點，來店消費享 85 折</li>
              <li>鑽石會員：達到 100 點，來店消費享 8 折</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="showPasswordModal"
      class="rule-modal-mask"
      @click.self="closePasswordModal"
    >
      <div class="password-modal">
        <button
          class="rule-close-btn"
          type="button"
          aria-label="關閉修改密碼"
          @click="closePasswordModal"
        >
          ×
        </button>
        <h3>修改密碼</h3>
        <form class="password-form" @submit.prevent="handleUpdatePassword">
          <label>
            目前密碼
            <input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="請輸入目前密碼"
            />
          </label>
          <label>
            新密碼
            <input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="8 到 20 個字元"
            />
          </label>
          <label>
            確認新密碼
            <input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="再次輸入新密碼"
            />
          </label>

          <p v-if="passwordError" class="field-error">{{ passwordError }}</p>

          <ul class="password-rules">
            <li :class="{ passed: isLengthValid }">
              密碼長度需為 8 到 20 個字元
            </li>
            <li :class="{ passed: isConfirmMatched }">
              兩次輸入的新密碼需一致
            </li>
            <li :class="{ passed: isDifferentFromOld }">
              新密碼不可與目前密碼相同
            </li>
          </ul>

          <button
            class="save-profile-btn full"
            type="submit"
            :disabled="!canSubmitPassword || isChangingPassword"
          >
            {{ isChangingPassword ? "修改中..." : "確認修改" }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import Swal from "sweetalert2";
import {
  getMyOrders,
  getMyReservations,
  getPointBalance,
  getPointHistory,
  getProfile,
  updatePassword,
  updateProfile,
} from "@/api/member";
import { storeApi } from "@/api/store";
import puddingImg from "@/assets/images/caramel-pudding.jpg";

const router = useRouter();
const route = useRoute();
const isLoading = ref(true);
const errorMsg = ref("");
const activeTab = ref("profile");
const showPointRuleModal = ref(false);
const showPasswordModal = ref(false);
const reservations = ref([]);
const isReservationLoading = ref(false);
const orders = ref([]);
const isOrderLoading = ref(false);
const pointHistory = ref([]);
const isPointHistoryLoading = ref(false);
const stores = ref([]);

// OrderSummaryResponse 只有 storeId 沒有 storeName，
// 額外撈一次公開門市列表自己做對照，不需要動到 order 模組的回應格式。
const storeNameMap = computed(() => {
  return stores.value.reduce((map, store) => {
    map[store.storeId] = store.storeName;
    return map;
  }, {});
});

const getStoredUserInfo = () => {
  try {
    return JSON.parse(localStorage.getItem("userInfo") || "{}");
  } catch (error) {
    localStorage.removeItem("userInfo");
    return {};
  }
};

const roleName = computed(() => getStoredUserInfo()?.roleName || "CUSTOMER");

const profileDescription = computed(
  () => "Email與生日會影響帳號與優惠，故不開放自行修改。",
);
const nameLabel = computed(() => "會員姓名");

const tabs = computed(() => [
  { key: "profile", label: "基本資料", icon: "bx bx-user" },
  { key: "reservations", label: "訂位紀錄", icon: "bx bx-calendar-check" },
  { key: "orders", label: "消費紀錄", icon: "bx bx-receipt" },
  { key: "points", label: "點數紀錄", icon: "bx bx-coin-stack" },
]);

const validTabKeys = computed(() => tabs.value.map((tab) => tab.key));

const syncActiveTabFromRoute = () => {
  const tab = String(route.query.tab || "profile");
  activeTab.value = validTabKeys.value.includes(tab) ? tab : "profile";
};

const userInfo = ref({
  name: "",
  email: "",
  phone: "",
  birthday: "",
  memberLevel: "BRONZE",
  pointBalance: 0,
  pointLevel: 0,
});

const pointInfo = ref({
  pointBalance: 0,
  pointLevel: 0,
  memberLevel: "BRONZE",
  nextLevel: "SILVER",
  pointsToNextLevel: 30,
  earnRuleText: "每消費 $100 即可累積 1 點",
});

const profileForm = ref({
  name: "",
  phone: "",
});

const isEditingProfile = ref(false);
const isSavingProfile = ref(false);

const passwordForm = ref({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});
const passwordError = ref("");
const isChangingPassword = ref(false);

const loadMemberPageData = async () => {
  // 先取得個人資料，再取得點數。
  // 這樣可避免 STAFF / MANAGER 舊測試帳號第一次進會員中心時，
  // /me 與 /points 同時嘗試補建 members 資料造成唯一鍵衝突。
  const profileRes = await getProfile();
  const profileData = profileRes.data.data;

  const pointRes = await getPointBalance();
  const pointData = pointRes.data.data;

  pointInfo.value = {
    pointBalance: pointData.pointBalance ?? 0,
    pointLevel: pointData.pointLevel ?? 0,
    memberLevel: pointData.memberLevel || "BRONZE",
    nextLevel: pointData.nextLevel || null,
    pointsToNextLevel: pointData.pointsToNextLevel ?? 0,
    earnRuleText: pointData.earnRuleText || "每消費 $100 即可累積 1 點",
  };

  userInfo.value = {
    name: profileData.name,
    email: profileData.email,
    phone: profileData.phone,
    birthday: profileData.birthday,
    memberLevel: pointInfo.value.memberLevel || profileData.memberLevel,
    pointBalance: pointInfo.value.pointBalance,
    pointLevel: pointInfo.value.pointLevel,
  };
};

const loadReservationRecords = async () => {
  isReservationLoading.value = true;
  try {
    const res = await getMyReservations();
    reservations.value = Array.isArray(res.data?.data) ? res.data.data : [];
  } catch (err) {
    // 新會員沒有訂位紀錄時，或訂位模組暫時查詢失敗時，
    // 會員中心先以「尚無訂位紀錄」呈現，避免使用者看到系統錯誤。
    console.warn("無法取得訂位紀錄", err);
    reservations.value = [];
  } finally {
    isReservationLoading.value = false;
  }
};

const loadOrderRecords = async () => {
  isOrderLoading.value = true;
  try {
    const [orderRes, storeRes] = await Promise.all([
      getMyOrders(),
      storeApi.getStores().catch(() => ({ data: [] })),
    ]);

    const list = Array.isArray(orderRes.data?.data) ? orderRes.data.data : [];
    // 後端目前沒有排序，前端依建立時間新到舊排列。
    orders.value = [...list].sort(
      (a, b) => new Date(b.createdAt) - new Date(a.createdAt),
    );
    stores.value = Array.isArray(storeRes?.data) ? storeRes.data : [];
  } catch (err) {
    // 新會員沒有消費紀錄時，或訂單模組暫時查詢失敗時，
    // 會員中心先以「尚無消費紀錄」呈現，避免使用者看到系統錯誤。
    console.warn("無法取得消費紀錄", err);
    orders.value = [];
  } finally {
    isOrderLoading.value = false;
  }
};

const loadPointHistory = async () => {
  isPointHistoryLoading.value = true;
  try {
    const res = await getPointHistory();
    const list = Array.isArray(res.data?.data) ? res.data.data : [];
    pointHistory.value = [...list].sort(
      (a, b) => new Date(b.createdAt) - new Date(a.createdAt),
    );
  } catch (err) {
    console.warn("無法取得點數紀錄", err);
    pointHistory.value = [];
  } finally {
    isPointHistoryLoading.value = false;
  }
};

onMounted(async () => {
  syncActiveTabFromRoute();

  const userInfo = JSON.parse(localStorage.getItem("userInfo") || "{}");
  if (!userInfo.userId) {
    router.push("/login");
    return;
  }

  try {
    await loadMemberPageData();
    await loadReservationRecords();
    await loadOrderRecords();
    await loadPointHistory();
  } catch (err) {
    errorMsg.value = "無法取得會員資料，請重新登入";
  } finally {
    isLoading.value = false;
  }
});

watch(
  () => route.query.tab,
  () => {
    syncActiveTabFromRoute();
  },
);

const switchTab = (tabKey) => {
  if (!validTabKeys.value.includes(tabKey)) return;

  activeTab.value = tabKey;
  router.replace({
    path: "/profile",
    query: tabKey === "profile" ? {} : { tab: tabKey },
  });
};

const goToReservation = () => {
  router.push("/reservation");
};

const goToMenu = () => {
  router.push("/menu");
};

const getLevelText = (level) => {
  const levels = {
    BRONZE: "銅卡會員",
    SILVER: "銀卡會員",
    GOLD: "金卡會員",
    DIAMOND: "鑽石卡會員",
  };
  return levels[level] || "一般會員";
};

const getLevelByPoint = (point) => {
  const points = Number(point) || 0;
  if (points >= 100) return "DIAMOND";
  if (points >= 60) return "GOLD";
  if (points >= 30) return "SILVER";
  return "BRONZE";
};

const memberLevelText = computed(() =>
  getLevelText(
    pointInfo.value.memberLevel || getLevelByPoint(pointInfo.value.pointLevel),
  ),
);

const memberDiscountText = computed(() => {
  const level =
    pointInfo.value.memberLevel || getLevelByPoint(pointInfo.value.pointLevel);
  const discountMap = {
    BRONZE: "來店消費享 95 折",
    SILVER: "來店消費享 9 折",
    GOLD: "來店消費享 85 折",
    DIAMOND: "來店消費享 8 折",
  };
  return discountMap[level] || "來店消費享 95 折";
});

const pointRuleTooltip = computed(
  () => "每消費 $100 累積 1 點\n30 點升銀卡\n60 點升金卡\n100 點升鑽石卡",
);

const levelThresholds = {
  BRONZE: { current: 0, next: 30 },
  SILVER: { current: 30, next: 60 },
  GOLD: { current: 60, next: 100 },
  DIAMOND: { current: 100, next: 100 },
};

const shortLevelText = (level) => {
  const levels = {
    BRONZE: "銅卡",
    SILVER: "銀卡",
    GOLD: "金卡",
    DIAMOND: "鑽石",
  };
  return levels[level] || "會員";
};

const currentLevelKey = computed(
  () => pointInfo.value.memberLevel || getLevelByPoint(pointInfo.value.pointLevel),
);

const currentLevelShortText = computed(() => shortLevelText(currentLevelKey.value));
const nextLevelShortText = computed(() =>
  pointInfo.value.nextLevel ? shortLevelText(pointInfo.value.nextLevel) : "最高等級",
);

const levelProgressPercent = computed(() => {
  const level = currentLevelKey.value;
  const points = Number(pointInfo.value.pointLevel) || 0;
  const threshold = levelThresholds[level] || levelThresholds.BRONZE;

  if (level === "DIAMOND") return 100;

  const range = threshold.next - threshold.current;
  const progressed = points - threshold.current;
  return Math.min(100, Math.max(0, Math.round((progressed / range) * 100)));
});

const levelProgressText = computed(() => {
  if (!pointInfo.value.nextLevel) return "已達最高會員等級";
  return `${pointInfo.value.pointLevel} / ${levelThresholds[currentLevelKey.value]?.next || 30} 點`;
});

const pointHistoryStats = computed(() => {
  return pointHistory.value.reduce(
    (stats, tx) => {
      const change = Number(tx.pointChange) || 0;
      if (change >= 0) {
        stats.earned += change;
      } else {
        stats.used += Math.abs(change);
      }
      return stats;
    },
    { earned: 0, used: 0 },
  );
});

const isPhoneValid = computed(() => /^09\d{8}$/.test(profileForm.value.phone));

const canSubmitProfile = computed(() => {
  const name = profileForm.value.name.trim();
  const phone = profileForm.value.phone.trim();
  const hasChanged =
    name !== userInfo.value.name || phone !== userInfo.value.phone;
  return Boolean(name) && isPhoneValid.value && hasChanged;
});

const isBirthdayMonth = computed(() => {
  if (!userInfo.value?.birthday) return false;
  const month = new Date(userInfo.value.birthday).getMonth() + 1;
  const currentMonth = new Date().getMonth() + 1;
  return month === currentMonth;
});

const isLengthValid = computed(() => {
  const length = passwordForm.value.newPassword.length;
  return length >= 8 && length <= 20;
});

const isConfirmMatched = computed(() => {
  if (!passwordForm.value.confirmPassword) return false;
  return passwordForm.value.newPassword === passwordForm.value.confirmPassword;
});

const isDifferentFromOld = computed(() => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword)
    return false;
  return passwordForm.value.oldPassword !== passwordForm.value.newPassword;
});

const canSubmitPassword = computed(() => {
  return (
    Boolean(passwordForm.value.oldPassword) &&
    Boolean(passwordForm.value.newPassword) &&
    Boolean(passwordForm.value.confirmPassword) &&
    isLengthValid.value &&
    isConfirmMatched.value &&
    isDifferentFromOld.value
  );
});

const startEditProfile = () => {
  profileForm.value = {
    name: userInfo.value.name || "",
    phone: userInfo.value.phone || "",
  };
  isEditingProfile.value = true;
};

const cancelEditProfile = () => {
  profileForm.value = {
    name: userInfo.value.name || "",
    phone: userInfo.value.phone || "",
  };
  isEditingProfile.value = false;
};

const handleUpdateProfile = async () => {
  if (!canSubmitProfile.value || isSavingProfile.value) return;
  isSavingProfile.value = true;

  try {
    const res = await updateProfile({
      name: profileForm.value.name.trim(),
      phone: profileForm.value.phone.trim(),
    });
    const data = res.data.data;

    userInfo.value = {
      ...userInfo.value,
      name: data.name,
      phone: data.phone,
    };

    localStorage.setItem(
      "userInfo",
      JSON.stringify({
        ...JSON.parse(localStorage.getItem("userInfo") || "{}"),
        name: data.name,
        phone: data.phone,
      }),
    );
    window.dispatchEvent(new Event("login-state-changed"));
    isEditingProfile.value = false;

    Swal.fire({
      icon: "success",
      title: "資料已更新",
      text: "會員姓名與電話已成功修改。",
      confirmButtonColor: "#d9a372",
    });
  } catch (err) {
    Swal.fire({
      icon: "error",
      title: "資料更新失敗",
      text: err.response?.data?.message || "請稍後再試。",
      confirmButtonColor: "#d9a372",
    });
  } finally {
    isSavingProfile.value = false;
  }
};

const resetPasswordForm = () => {
  passwordForm.value = {
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  };
  passwordError.value = "";
};

const openPasswordModal = () => {
  resetPasswordForm();
  showPasswordModal.value = true;
};

const closePasswordModal = () => {
  resetPasswordForm();
  showPasswordModal.value = false;
};

const validatePasswordForm = () => {
  if (
    !passwordForm.value.oldPassword ||
    !passwordForm.value.newPassword ||
    !passwordForm.value.confirmPassword
  ) {
    passwordError.value = "請完整填寫所有欄位";
    return false;
  }
  if (!isLengthValid.value) {
    passwordError.value = "新密碼長度需為 8 到 20 個字元";
    return false;
  }
  if (!isConfirmMatched.value) {
    passwordError.value = "兩次輸入的新密碼不一致";
    return false;
  }
  if (!isDifferentFromOld.value) {
    passwordError.value = "新密碼不可與目前密碼相同";
    return false;
  }
  passwordError.value = "";
  return true;
};

const handleUpdatePassword = async () => {
  if (!validatePasswordForm() || isChangingPassword.value) return;
  isChangingPassword.value = true;

  try {
    await updatePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
    });

    closePasswordModal();
    Swal.fire({
      icon: "success",
      title: "密碼已更新",
      text: "下次登入請使用新密碼。",
      confirmButtonColor: "#d9a372",
    });
  } catch (err) {
    passwordError.value =
      err.response?.data?.message || "密碼修改失敗，請稍後再試";
  } finally {
    isChangingPassword.value = false;
  }
};

const formatDate = (date) => {
  if (!date) return "未提供";
  return String(date).replaceAll("-", "/");
};

const formatTime = (time) => {
  if (!time) return "--:--";
  return String(time).slice(0, 5);
};

const formatReservationTime = (reservation) => {
  const start = formatTime(reservation.startTime);
  const end = reservation.endTime ? formatTime(reservation.endTime) : "";
  return end ? `${start} ~ ${end}` : start;
};

const getPointTypeText = (type) => {
  const typeMap = {
    EARN: "累積",
    USE: "折抵使用",
    REFUND: "折抵退回",
    EXPIRE: "到期",
    ADJUST: "手動調整",
  };
  return typeMap[type] || type || "未提供";
};

const getPointTypeClass = (type) => {
  if (type === "EARN" || type === "REFUND") return "success";
  if (type === "USE" || type === "EXPIRE") return "danger";
  return "pending";
};

const getPointChangeClass = (pointChange) => {
  return Number(pointChange) >= 0 ? "plus" : "minus";
};

const formatPointChange = (pointChange) => {
  const points = Number(pointChange) || 0;
  return `${points > 0 ? "+" : ""}${points} 點`;
};

const formatStore = (reservation) => {
  if (reservation.storeName) return reservation.storeName;
  if (reservation.storeId) return `店名未設定 (#${reservation.storeId})`;
  return "未提供";
};

const getReservationStatusText = (status) => {
  const statusMap = {
    PENDING: "待確認",
    RESERVED: "已保留",
    ASSIGNED: "已配桌",
    CHECKED_IN: "已入座",
    COMPLETED: "已完成",
    CANCELLED: "已取消",
    NO_SHOW: "未到店",
  };
  return statusMap[status] || status || "未提供";
};

const getReservationStatusClass = (status) => {
  if (["COMPLETED", "CHECKED_IN"].includes(status)) return "success";
  if (["RESERVED", "ASSIGNED"].includes(status)) return "active";
  if (["CANCELLED", "NO_SHOW"].includes(status)) return "danger";
  return "pending";
};

const formatDateTime = (dateTime) => {
  if (!dateTime) return "未提供";
  return String(dateTime).replace("T", " ").slice(0, 16).replaceAll("-", "/");
};

const getOrderAmount = (order) => {
  return Number(order?.finalAmount ?? order?.totalAmount ?? 0);
};

const formatCurrency = (amount) => {
  return new Intl.NumberFormat("zh-TW", {
    style: "currency",
    currency: "TWD",
    maximumFractionDigits: 0,
  }).format(Number(amount) || 0);
};

const formatOrderStore = (order) => {
  const name = order?.storeId ? storeNameMap.value[order.storeId] : null;
  if (name) return name;
  if (order?.storeId) return `門市 #${order.storeId}`;
  return "未提供";
};

const getOrderTypeText = (type) => {
  const typeMap = {
    DINE_IN: "內用",
    TAKEOUT: "外帶",
    TAKE_OUT: "外帶",
    DELIVERY: "外送",
  };
  return typeMap[type] || type || "未提供";
};

const getPaymentStatusText = (status) => {
  const statusMap = {
    UNPAID: "未付款",
    PENDING: "待付款",
    PAID: "已付款",
    FAILED: "付款失敗",
    REFUNDED: "已退款",
  };
  return statusMap[status] || status || "未提供";
};

const getPaymentStatusClass = (status) => {
  if (status === "PAID") return "success";
  if (["FAILED", "REFUNDED"].includes(status)) return "danger";
  return "pending";
};

const getOrderStatusText = (status) => {
  const statusMap = {
    PENDING: "待處理",
    CONFIRMED: "已確認",
    PREPARING: "製作中",
    READY: "可取餐",
    COMPLETED: "已完成",
    CANCELLED: "已取消",
  };
  return statusMap[status] || status || "未提供";
};

const getOrderStatusClass = (status) => {
  if (status === "COMPLETED") return "success";
  if (["CONFIRMED", "PREPARING", "READY"].includes(status)) return "active";
  if (status === "CANCELLED") return "danger";
  return "pending";
};

const formatBirthday = (birthday) => {
  if (!birthday) return "未提供";
  return formatDate(birthday);
};
</script>

<style scoped>
.member-page {
  position: relative;
  isolation: isolate;
  overflow: hidden;
  min-height: 100vh;
  padding: clamp(120px, 8vw, 150px) clamp(18px, 3vw, 32px) clamp(50px, 5vw, 70px);
  background:
    linear-gradient(115deg, rgba(10, 7, 5, 0.72) 0%, rgba(46, 31, 20, 0.42) 52%, rgba(10, 7, 5, 0.72) 100%),
    url("@/assets/images/caramel-pudding.jpg") center / cover fixed no-repeat;
}

.member-page::before {
  content: "";
  position: absolute;
  inset: 0;
  z-index: -1;
  background:
    linear-gradient(180deg, rgba(248, 243, 237, 0.1) 0%, rgba(248, 243, 237, 0.42) 55%, rgba(248, 243, 237, 0.7) 100%),
    radial-gradient(circle at 20% 16%, rgba(227, 172, 127, 0.22), transparent 32%),
    linear-gradient(rgba(255, 255, 255, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.035) 1px, transparent 1px);
  background-size: auto, auto, 110px 110px, 110px 110px;
}

.member-shell {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: clamp(280px, 24vw, 300px) minmax(0, 1fr);
  gap: clamp(26px, 2.4vw, 34px);
  width: min(1220px, calc(100vw - 48px));
  margin: 0 auto;
}

.member-sidebar,
.member-content {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.sidebar-card,
.info-card,
.empty-card,
.record-card,
.state-box {
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: clamp(18px, 1.5vw, 22px);
  box-shadow: 0 20px 55px rgba(0, 0, 0, 0.16);
  backdrop-filter: blur(10px);
}

.sidebar-card {
  padding: clamp(20px, 1.8vw, 24px);
}

.level-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.compact-level-card {
  min-height: auto;
}

.sidebar-title {
  margin: 0 0 12px;
  color: #8a99a8;
  font-weight: 900;
}

.point-card-title {
  margin: 0;
  color: #66798d;
  font-size: clamp(20px, 1.65vw, 22px);
  line-height: 1.2;
  letter-spacing: 0.04em;
}

.point-heading-row .point-card-title {
  margin-right: auto;
}

.combined-point-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.point-section {
  position: relative;
}

.point-heading-row {
  display: flex;
  align-items: center;
  gap: 14px;
}

.point-value-inline {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
}

.point-unit-label {
  flex: 0 0 auto;
  color: #d88750;
  font-size: 18px;
  font-weight: 900;
  line-height: 1;
  letter-spacing: 0.08em;
}

.point-card-divider {
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, rgba(227, 172, 127, 0.08), rgba(227, 172, 127, 0.42), rgba(227, 172, 127, 0.08));
}

.level-badge {
  display: inline-flex;
  padding: 10px 16px;
  border-radius: 999px;
  background: #e3ac7f;
  color: #fff;
  font-size: 20px;
  font-weight: 900;
}

.tooltip-trigger {
  position: relative;
}

.tooltip-trigger::after {
  content: attr(data-tooltip);
  position: absolute;
  left: 50%;
  bottom: calc(100% + 12px);
  transform: translateX(-50%) translateY(4px);
  width: max-content;
  max-width: 260px;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(52, 34, 23, 0.94);
  color: #fff;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.6;
  text-align: center;
  white-space: pre-line;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.18s ease, transform 0.18s ease;
  z-index: 20;
}

.tooltip-trigger::before {
  content: "";
  position: absolute;
  left: 50%;
  bottom: calc(100% + 5px);
  transform: translateX(-50%) translateY(4px);
  border: 7px solid transparent;
  border-top-color: rgba(52, 34, 23, 0.94);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.18s ease, transform 0.18s ease;
  z-index: 20;
}

.tooltip-trigger:hover::after,
.tooltip-trigger:focus-visible::after,
.tooltip-trigger:hover::before,
.tooltip-trigger:focus-visible::before {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

.tooltip-wide::after {
  width: 220px;
  text-align: left;
}

.level-discount {
  margin: 10px 0 0;
  color: #d88750;
  font-size: 14px;
  font-weight: 900;
  line-height: 1.6;
}

.point-main {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: clamp(52px, 4vw, 58px);
  height: clamp(52px, 4vw, 58px);
  margin-top: 0;
  margin-right: 0;
  top: -6px;
  border-radius: 50%;
  background:
    radial-gradient(circle at 34% 28%, rgba(255, 246, 236, 0.96), rgba(248, 225, 204, 0.68));
  border: 2px solid rgba(227, 172, 127, 0.34);
  box-shadow:
    inset 0 0 0 5px rgba(255, 255, 255, 0.52),
    0 8px 20px rgba(227, 172, 127, 0.14);
  isolation: isolate;
  animation: pointBadgePulse 0.95s ease-in-out infinite;
}

.point-main::before {
  content: "";
  position: absolute;
  inset: -5px;
  border-radius: inherit;
  border: 1px solid rgba(227, 172, 127, 0.24);
  box-shadow: 0 0 0 0 rgba(227, 172, 127, 0.2);
  opacity: 0.75;
  animation: pointRingPulse 0.95s ease-in-out infinite;
  z-index: -1;
}

.point-number {
  color: #566a7f;
  font-family: Georgia, "Times New Roman", "Noto Serif TC", serif;
  font-size: clamp(29px, 2.8vw, 36px);
  line-height: 1;
  font-weight: 900;
  letter-spacing: -0.07em;
  transform-origin: center;
  animation: pointNumberPop 0.95s ease-in-out infinite;
}

.discount-help-note {
  color: #8a99a8;
  margin: 10px 0 0;
  line-height: 1.7;
}


.level-progress {
  margin: 14px 0 18px;
}

.level-progress-meta {
  display: flex;
  justify-content: space-between;
  color: #8a99a8;
  font-size: 12px;
  font-weight: 900;
  margin-bottom: 8px;
}

.level-progress-track {
  position: relative;
  height: 10px;
  overflow: hidden;
  border-radius: 999px;
  background: #f2e4d7;
}

.level-progress-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #d89762, #e8b98d);
  box-shadow: 0 0 16px rgba(227, 172, 127, 0.45);
  transition: width 0.8s ease;
}

.level-progress-text {
  margin: 8px 0 0;
  color: #a17353;
  font-size: 12px;
  font-weight: 900;
}

.highlight-point {
  color: #e3ac7f;
  font-weight: 900;
}

@keyframes pointBadgePulse {
  0%,
  100% {
    transform: scale(0.98);
    box-shadow:
      inset 0 0 0 5px rgba(255, 255, 255, 0.52),
      0 8px 20px rgba(227, 172, 127, 0.14);
  }
  44% {
    transform: scale(1.06);
    box-shadow:
      inset 0 0 0 5px rgba(255, 255, 255, 0.72),
      0 10px 26px rgba(227, 172, 127, 0.24);
  }
  68% {
    transform: scale(0.99);
  }
}

@keyframes pointRingPulse {
  0%,
  100% {
    opacity: 0.52;
    transform: scale(0.94);
    box-shadow: 0 0 0 0 rgba(227, 172, 127, 0.2);
  }
  44% {
    opacity: 1;
    transform: scale(1.08);
    box-shadow: 0 0 0 8px rgba(227, 172, 127, 0.08);
  }
  68% {
    opacity: 0.72;
    transform: scale(0.99);
  }
}

@keyframes pointNumberPop {
  0%,
  100% {
    transform: scale(1);
  }
  44% {
    transform: scale(1.09);
  }
  68% {
    transform: scale(0.98);
  }
}


.point-rule-btn,
.edit-profile-btn,
.save-profile-btn,
.cancel-profile-btn,
.tab-btn {
  border: none;
  border-radius: 12px;
  font-weight: 900;
  cursor: pointer;
}

.point-rule-btn,
.edit-profile-btn,
.save-profile-btn {
  background: #e3ac7f;
  color: #fff;
}

.point-rule-btn,
.edit-profile-btn {
  padding: 10px 14px;
}

.edit-profile-btn.outline,
.cancel-profile-btn {
  background: #fff7ef;
  color: #d18f5e;
}


.birthday-value {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.birthday-inline-note {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  color: #d18f5e;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.5;
}

.birthday-pudding-inline {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.88);
  box-shadow: 0 8px 18px rgba(209, 143, 94, 0.26);
  animation: puddingGiftFloat 2.2s ease-in-out infinite;
}

@keyframes puddingGiftFloat {
  0%,
  100% {
    transform: translateY(0) scale(1) rotate(-3deg);
  }
  50% {
    transform: translateY(-4px) scale(1.06) rotate(3deg);
  }
}

.tab-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 20px 55px rgba(0, 0, 0, 0.14);
  backdrop-filter: blur(10px);
}

.tab-edit-profile-btn {
  margin-left: auto;
  min-height: 44px;
  padding-inline: 18px;
  box-shadow: 0 12px 24px rgba(227, 172, 127, 0.22);
}

.tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 11px 15px;
  background: #fbfaf8;
  color: #7d8b9a;
}

.tab-btn.active {
  background: #e3ac7f;
  color: #fff;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
}

.section-header h2 {
  margin: 0;
  color: #fff;
  font-size: 26px;
  font-weight: 900;
}

.section-header p {
  margin: 8px 0 0;
  color: rgba(255, 255, 255, 0.82);
  line-height: 1.7;
}

.info-card {
  margin-top: 0;
  padding: clamp(22px, 2vw, 26px);
}

.info-row {
  display: grid;
  grid-template-columns: clamp(110px, 11vw, 140px) minmax(0, 1fr) auto;
  gap: clamp(14px, 1.5vw, 18px);
  align-items: center;
  min-height: 58px;
  padding: 14px 0;
  border-bottom: 1px solid #f0e2d5;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  color: #8a99a8;
  font-weight: 900;
}

.info-value {
  color: #566a7f;
  font-weight: 800;
}

.readonly-row .info-value {
  color: #8a99a8;
}

.readonly-hint-row {
  display: grid;
  grid-template-columns: 130px minmax(220px, 1fr) auto;
  align-items: center;
  gap: 16px;
}

.readonly-inline-hint {
  justify-self: end;
  text-align: right;
  color: #d64545;
  font-size: 13px;
  font-weight: 900;
  line-height: 1.45;
  white-space: nowrap;
}

.birthday-hint-row {
  align-items: start;
}

.birthday-hint-row .readonly-inline-hint {
  margin-top: 2px;
}

.password-dots {
  letter-spacing: 3px;
}

.edit-field input,
.password-form input {
  width: 100%;
  border: 1px solid #ead8c8;
  border-radius: 10px;
  padding: 11px 12px;
  color: #566a7f;
}

.edit-field input:focus,
.password-form input:focus {
  outline: none;
  border-color: #e3ac7f;
  box-shadow: 0 0 0 3px rgba(227, 172, 127, 0.18);
}

.field-error {
  margin: 8px 0 0;
  color: #c0392b;
  font-size: 14px;
  font-weight: 700;
}

.profile-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}

.cancel-profile-btn,
.save-profile-btn {
  padding: 12px 22px;
}

.save-profile-btn.full {
  width: 100%;
  margin-top: 14px;
}

button:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.state-box {
  grid-column: 1 / -1;
  padding: 36px;
  color: #8a99a8;
  text-align: center;
  font-weight: 900;
}

.state-box.inner {
  grid-column: auto;
}

.error-msg {
  color: #c0392b;
}

.empty-card {
  padding: 44px 34px;
  text-align: center;
}

.empty-card i {
  color: #e3ac7f;
  font-size: 48px;
}

.empty-card h3 {
  margin: 14px 0 8px;
  color: #566a7f;
  font-weight: 900;
}

.empty-card p,
.empty-card li {
  color: #7d8b9a;
  line-height: 1.8;
}

.action-empty-card .empty-action-btn {
  margin-top: 18px;
}

.empty-action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 14px;
  padding: 12px 20px;
  background: #e3ac7f;
  color: #fff;
  font-weight: 900;
  cursor: pointer;
  box-shadow: 0 10px 22px rgba(227, 172, 127, 0.28);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.empty-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 26px rgba(227, 172, 127, 0.34);
}

.pending-card {
  text-align: left;
}

.pending-card i,
.pending-card h3 {
  text-align: center;
  display: block;
}

.pending-card code {
  padding: 2px 6px;
  border-radius: 6px;
  background: #fff1e5;
  color: #c47d4e;
}

.point-history-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.history-summary-card {
  padding: 18px 18px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 14px 36px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.history-summary-card .summary-label {
  display: block;
  color: #8a99a8;
  font-size: 13px;
  font-weight: 900;
  margin-bottom: 8px;
}

.history-summary-card strong {
  display: block;
  color: #566a7f;
  font-size: 30px;
  line-height: 1;
  font-weight: 900;
}

.history-summary-card small {
  display: block;
  margin-top: 8px;
  color: #9aa6b2;
  font-weight: 700;
}

.history-summary-card.earn strong {
  color: #2e9f5e;
}

.history-summary-card.use strong {
  color: #c47d4e;
}

.record-card {
  overflow-x: auto;
}

.record-card table {
  width: 100%;
  border-collapse: collapse;
}

.record-card th,
.record-card td {
  padding: 16px 18px;
  border-bottom: 1px solid #f0e2d5;
  color: #566a7f;
  text-align: left;
}

.record-card th {
  color: #8a99a8;
  font-size: 13px;
}

.record-card .right {
  text-align: right;
  font-weight: 900;
}

.note-cell {
  min-width: 160px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 68px;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 900;
  white-space: nowrap;
}

.status-badge.pending {
  background: #fff7ef;
  color: #c47d4e;
}

.status-badge.active {
  background: #edf4ff;
  color: #3f6fb5;
}

.status-badge.success {
  background: #eef9f2;
  color: #2e9f5e;
}

.status-badge.danger {
  background: #fff0ee;
  color: #c0392b;
}

.plus {
  color: #2e9f5e !important;
}

.minus {
  color: #c0392b !important;
}

.rule-modal-mask {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(0, 0, 0, 0.42);
}

.rule-modal,
.password-modal {
  position: relative;
  width: min(520px, 100%);
  padding: 30px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.18);
}

.rule-close-btn {
  position: absolute;
  top: 14px;
  right: 16px;
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 50%;
  background: #fff7ef;
  color: #d18f5e;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
}

.rule-modal h3,
.password-modal h3 {
  margin: 0 0 18px;
  color: #566a7f;
  font-weight: 900;
}

.rule-section h4 {
  margin: 18px 0 8px;
  color: #e3ac7f;
  font-weight: 900;
}

.rule-section p,
.rule-section li {
  color: #7d8b9a;
  line-height: 1.8;
}

.password-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.password-form label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #566a7f;
  font-weight: 900;
}

.password-rules {
  margin: 0;
  padding-left: 20px;
  color: #9aa6b2;
  line-height: 1.9;
}

.password-rules li.passed {
  color: #2e9f5e;
}

@media (min-width: 1500px) {
  .member-page {
    background-size: cover;
  }
}

@media (max-width: 1180px) {
  .member-shell {
    grid-template-columns: clamp(260px, 28vw, 285px) minmax(0, 1fr);
    width: min(1060px, calc(100vw - 36px));
    gap: 24px;
  }
}

@media (max-width: 900px) {
  .member-shell {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .member-page {
    padding: 120px 14px 50px;
    background-attachment: scroll;
    background-position: 58% center;
  }

  .section-header,
  .info-row,
  .password-row {
    grid-template-columns: 1fr;
    align-items: flex-start;
  }

  .section-header {
    flex-direction: column;
  }

  .point-history-summary {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .point-number,
  .point-main,
  .point-main::before {
    animation: none;
  }

  .point-main::before {
    transform: none;
  }
}
</style>
