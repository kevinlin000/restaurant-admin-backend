<script setup>
import { computed, onMounted, ref } from "vue";
import api from "@/api/axios";

const stores = ref([]);
const storeLoading = ref(false);
const storeError = ref("");

const unwrap = (response) => response.data?.data ?? response.data ?? [];

const featuredStores = computed(() => {
  const cityPriority = ["台北市", "新竹市", "台中市", "台南市", "高雄市", "花蓮縣"];
  return [...stores.value]
    .sort((a, b) => {
      const cityDiff = cityPriority.indexOf(a.city) - cityPriority.indexOf(b.city);
      if (cityDiff !== 0) return cityDiff;
      return `${a.storeName}`.localeCompare(`${b.storeName}`, "zh-Hant");
    })
    .slice(0, 3);
});

const openStoreCount = computed(() => stores.value.filter((store) => store.openNow).length);

const storeLocation = (store) => [store.city, store.district].filter(Boolean).join(" ");

const openStatusText = (store) => (store.openNow ? "營業中" : "非營業時間");

const loadFeaturedStores = async () => {
  storeLoading.value = true;
  storeError.value = "";

  try {
    const response = await api.get("/api/stores");
    stores.value = unwrap(response);
  } catch (error) {
    storeError.value = "門市資料暫時無法載入";
    stores.value = [];
  } finally {
    storeLoading.value = false;
  }
};

onMounted(loadFeaturedStores);
</script>

<template>

  <!-- Introduction 簡介 -->
  <section class="introduction-section">
    <div class="introduction-overlay">
      <div class="introduction-content">
        <h1 class="introduction-title">標題</h1>
        <p class="introduction-desc">介紹</p>

        <div class="introduction-buttons">
        <RouterLink class="primary-btn" to="/reservation">立即訂位</RouterLink>
        <RouterLink class="secondary-btn" to="/menu">瀏覽菜單</RouterLink>
        </div>
      </div>
    </div>
  </section>

  <!-- About 關於品牌 -->
  <section id="about" class="about-section">
    <div class="section-container">
      <div class="section-title">品牌介紹</div>
      <div class="about-grid">
        <div class="about-text">
          <h2>品牌介紹標題</h2>
          <p>品牌介紹內容</p>
        </div>
        <div class="about-image"></div>
      </div>
    </div>
  </section>

  <!-- Menu 菜單 -->
  <section id="menu" class="menu-section">
    <div class="section-container">
      <div class="section-title">精選菜單</div>

        <div class="menu-grid">
            <div class="menu-card">
                <div class="menu-image example1"></div>
                <h3>菜單種類１</h3>
            </div>
            <div class="menu-card">
                <div class="menu-image example2"></div>
                <h3>菜單種類２</h3>
            </div>
            <div class="menu-card">
                <div class="menu-image example3"></div>
                <h3>菜單種類３</h3>
            </div>
        </div>
    </div>
    <div class="text-center mt-5">
        <RouterLink class="primary-btn" to="/order">點餐</RouterLink>
    </div>
  </section>

  <!-- Reservation 訂位-->
  <section id="reservation" class="reservation-section">
    <div class="section-container text-center">
        <div class="section-title">線上訂位</div>
        <p class="reservation-text">訂位說明</p>
        <br>
        <RouterLink class="primary-btn" to="/reservation">立即訂位</RouterLink>
    </div>
  </section>

  <!-- Store 分店資訊 -->
  <section id="store" class="store-section">
    <div class="section-container">
      <div class="store-heading">
        <span class="section-kicker">Store Locator</span>
        <div class="section-title">分店資訊</div>
        <p>
          從城市旗艦、商圈聚餐到旅途慢食，快速找到最適合今天用餐情境的敘日門市。
        </p>
      </div>

      <div class="store-toolbar">
        <div>
          <span>全台 {{ stores.length || 0 }} 間門市</span>
          <strong>{{ openStoreCount }} 間營業中</strong>
        </div>
        <RouterLink class="store-link" to="/store">
          查看全部分店
          <i class="bi bi-arrow-right"></i>
        </RouterLink>
      </div>

      <div v-if="storeLoading" class="store-state">
        <span class="spinner-border spinner-border-sm"></span>
        載入門市資訊
      </div>

      <div v-else-if="storeError" class="store-state warning">
        {{ storeError }}
      </div>

      <div v-else class="store-grid">
        <RouterLink
          v-for="store in featuredStores"
          :key="store.storeId"
          class="store-card"
          :to="{ name: 'CustomerStore' }"
        >
          <div class="store-image">
            <img :src="store.mainImageUrl" :alt="store.storeName" />
            <span :class="['store-status', store.openNow ? 'open' : 'closed']">
              {{ openStatusText(store) }}
            </span>
          </div>
          <div class="store-card-body">
            <span class="store-location">{{ storeLocation(store) }}</span>
            <h3>{{ store.storeName }}</h3>
            <p>{{ store.address }}</p>
            <div v-if="store.featureTags?.length" class="store-tags">
              <span
                v-for="feature in store.featureTags.slice(0, 3)"
                :key="feature.featureKey"
              >
                {{ feature.featureLabel }}
              </span>
            </div>
            <div class="store-meta">
              <span><i class="bi bi-telephone"></i>{{ store.phone || "電話更新中" }}</span>
              <span><i class="bi bi-train-front"></i>{{ store.mrtInfo || "交通資訊更新中" }}</span>
            </div>
          </div>
        </RouterLink>
      </div>

      <div class="store-actions">
        <RouterLink class="primary-btn" to="/store">依地區找門市</RouterLink>
        <RouterLink class="outline-btn" to="/reservation">選店訂位</RouterLink>
      </div>
    </div>
  </section>
</template>

<style scoped>

/* sections */

.section-container {
  width: 80%;
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  font-size: 42px;
  text-align: center;
  margin-bottom: 80px;
}

/* buttons */

.primary-btn {
  background: #d6a679;
  color: white;
  padding: 14px 30px;
  border-radius: 12px;
  text-decoration: none;
}

.secondary-btn {
  border: 1px solid white;
  color: white;
  padding: 14px 30px;
  border-radius: 12px;
  text-decoration: none;
}

/* Introduction 簡介 */

.introduction-section {
  height: 100vh;
  background:
    linear-gradient(rgba(0,0,0,0.35), rgba(0,0,0,0.35)),
    url('../assets/images/bg-introduction.jpg');
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
}

.introduction-overlay {
  width: 100%;
}

.introduction-content {
  width: 80%;
  max-width: 1200px;
  margin: 0 auto;
  color: white;
}

.introduction-title {
  font-size: 72px;
  line-height: 1.3;
  margin-bottom: 30px;
}

.introduction-desc {
  font-size: 22px;
  margin-bottom: 40px;
}

.introduction-buttons {
  display: flex;
  gap: 20px;
}

/* about 關於品牌 */

.about-section {
  padding: 140px 0;
  background: url('../assets/images/bg-about.jpg');
  background-size: cover;
  background-color: #ffffff;
}

.about-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
}

.about-image {
  height: 500px;
  border-radius: 20px;
  background:url('../assets/images/about-food.jpg');
  background-size: cover;
}

/* reservation 訂位 */

.reservation-section {
  padding: 140px 0;
  background:
    linear-gradient(rgba(0,0,0,0.45), rgba(0,0,0,0.45)),
    url('../assets/images/bg-reservation.jpg');
  background-size: cover;
  color: white;
}

/* menu 菜單 */

.menu-section {
  padding: 140px 0;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(3,1fr);
  gap: 30px;
}

.menu-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  padding: 20px;
  text-align: center;
}

.menu-image {
  height: 240px;
  background-size: cover;
  background-position: center;
}

.example1 {
  background-image: url('../assets/images/menu-example1.jpg');
}

.example2 {
  background-image: url('../assets/images/menu-example2.jpg');
}

.example3 {
  background-image: url('../assets/images/menu-example3.jpg');
}

/* store 分店資訊 */

.store-section {
  padding: 140px 0;
  background:
    linear-gradient(rgba(247, 243, 238, 0.9), rgba(247, 243, 238, 0.94)),
    url('../assets/images/bg-store.jpg');
  background-size: cover;
  background-position: center;
}

.store-heading {
  max-width: 760px;
  margin: 0 auto 42px;
  text-align: center;
}

.store-heading .section-title {
  margin-bottom: 18px;
}

.section-kicker {
  display: inline-block;
  margin-bottom: 12px;
  color: #b1642f;
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.store-heading p {
  margin: 0;
  color: #697386;
  font-size: 18px;
  line-height: 1.8;
}

.store-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
  color: #697386;
  font-weight: 800;
}

.store-toolbar div {
  display: flex;
  align-items: center;
  gap: 12px;
}

.store-toolbar strong {
  color: #167a3d;
}

.store-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #8c552e;
  font-weight: 900;
  text-decoration: none;
}

.store-grid {
  display: grid;
  grid-template-columns: repeat(3,1fr);
  gap: 24px;
}

.store-card {
  overflow: hidden;
  border: 1px solid #e4d9ce;
  border-radius: 8px;
  background: #ffffff;
  color: inherit;
  text-decoration: none;
  box-shadow: 0 12px 28px rgba(52, 64, 81, 0.09);
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
}

.store-card:hover {
  border-color: #b1642f;
  box-shadow: 0 18px 36px rgba(52, 64, 81, 0.14);
  transform: translateY(-4px);
}

.store-image {
  position: relative;
  height: 210px;
  background: #f2eee9;
}

.store-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.store-status {
  position: absolute;
  right: 14px;
  bottom: 14px;
  border-radius: 999px;
  padding: 7px 12px;
  font-size: 13px;
  font-weight: 900;
}

.store-status.open {
  background: #e8f7ee;
  color: #167a3d;
}

.store-status.closed {
  background: #f1eeeb;
  color: #74685f;
}

.store-card-body {
  padding: 24px;
}

.store-location {
  color: #8c552e;
  font-size: 14px;
  font-weight: 900;
}

.store-card h3 {
  margin: 8px 0 10px;
  color: #263445;
  font-size: 24px;
  font-weight: 900;
}

.store-card p {
  min-height: 52px;
  margin: 0;
  color: #697386;
  line-height: 1.7;
}

.store-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 12px;
}

.store-tags span {
  border-radius: 999px;
  background: #faf3ea;
  color: #8c552e;
  padding: 5px 9px;
  font-size: 13px;
  font-weight: 900;
}

.store-meta {
  display: grid;
  gap: 8px;
  margin-top: 18px;
  color: #566a7f;
  font-size: 14px;
}

.store-meta span {
  display: flex;
  align-items: center;
  gap: 8px;
}

.store-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 180px;
  border: 1px dashed #d7c6b7;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.76);
  color: #697386;
  font-weight: 800;
}

.store-state.warning {
  color: #a16012;
}

.store-actions {
  display: flex;
  justify-content: center;
  gap: 14px;
  margin-top: 34px;
}

.outline-btn {
  border: 1px solid #d7c6b7;
  background: #ffffff;
  color: #8c552e;
  padding: 14px 30px;
  border-radius: 12px;
  text-decoration: none;
  font-weight: 800;
}

/* mobile */

@media (max-width: 991px) {

  .introduction-title {
    font-size: 42px;
  }

  .about-grid,
  .menu-grid,
  .store-grid {
    grid-template-columns: 1fr;
  }

  .store-toolbar,
  .store-toolbar div,
  .store-actions {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
