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

const primaryStore = computed(() => featuredStores.value[0] || null);

const supportingStores = computed(() => featuredStores.value.slice(1));

const openStoreCount = computed(() => stores.value.filter((store) => store.openNow).length);

const cityCount = computed(() => new Set(stores.value.map((store) => store.city).filter(Boolean)).size);

const storeLocation = (store) => [store.city, store.district].filter(Boolean).join(" ");

const openStatusText = (store) => (store.openNow ? "營業中" : "非營業時間");

const storeImage = (store) => store?.mainImageUrl || "/store-images/xuri-dining-room.jpg";

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
        <h1 class="introduction-title yuji-boku-regular">敘日和食</h1>
        <small class="introduction-desc new-tegomin-regular">
          「敘」是敘舊，是放下手機後的深度對談
          <br>
          「日」是時光，是歲月淬鍊出的滋味
        </small>
        <div class="introduction-buttons py-5">
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
      <div class="store-heading-row">
        <div class="store-heading">
          <span class="section-kicker">Locations</span>
          <div class="section-title">今日想在哪裡用餐</div>
          <p>從信義夜景、車站共構到旅途餐桌，挑一間離今天最近的敘日。</p>
        </div>

        <RouterLink class="store-link" to="/store">
          全部門市
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

      <div v-else class="location-panel">
        <RouterLink
          v-if="primaryStore"
          class="location-feature"
          :to="{ name: 'CustomerStore' }"
        >
          <img :src="storeImage(primaryStore)" :alt="primaryStore.storeName" />
          <div class="location-feature-copy">
            <span :class="['store-status', primaryStore.openNow ? 'open' : 'closed']">
              {{ openStatusText(primaryStore) }}
            </span>
            <p>{{ storeLocation(primaryStore) }}</p>
            <h3>{{ primaryStore.storeName }}</h3>
            <span>{{ primaryStore.address }}</span>
          </div>
        </RouterLink>

        <div class="location-side">
          <div class="location-counts" aria-label="門市統計">
            <div>
              <strong>{{ stores.length || 0 }}</strong>
              <span>門市</span>
            </div>
            <div>
              <strong>{{ cityCount || 0 }}</strong>
              <span>城市</span>
            </div>
            <div>
              <strong>{{ openStoreCount }}</strong>
              <span>營業中</span>
            </div>
          </div>

          <RouterLink
            v-for="store in supportingStores"
            :key="store.storeId"
            class="location-row"
            :to="{ name: 'CustomerStore' }"
          >
            <img :src="storeImage(store)" :alt="store.storeName" />
            <span>{{ storeLocation(store) }}</span>
            <strong>{{ store.storeName }}</strong>
            <p>{{ store.mrtInfo || store.address }}</p>
          </RouterLink>

          <div class="location-note">
            <span>依地區、情境標籤與交通方式瀏覽完整門市。</span>
            <RouterLink to="/store">查看門市頁</RouterLink>
          </div>
        </div>
      </div>

      <div class="store-actions">
        <RouterLink class="primary-btn" to="/store">依地區找門市</RouterLink>
        <RouterLink class="outline-btn" to="/reservation">選店訂位</RouterLink>
      </div>
    </div>
  </section>
</template>

<style scoped>

/* 字型 */
@import url('https://fonts.googleapis.com/css2?family=Yuji+Boku&display=swap');
@import url('https://fonts.googleapis.com/css2?family=New+Tegomin&display=swap');

.yuji-boku-regular {
  font-family: "Yuji Boku", serif;
  font-weight: 400;
  font-style: normal;
}
.new-tegomin-regular {
  font-family: "New Tegomin", serif;
  font-weight: 400 bold;
  font-style: normal;
}

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
    url('../../assets/images/Home.jpg');
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
  background: #f5f0ea;
}

.store-heading-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 32px;
  align-items: end;
  margin-bottom: 34px;
}

.store-heading {
  max-width: 680px;
  text-align: left;
}

.store-heading .section-title {
  text-align: left;
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
  color: #5f5750;
  font-size: 18px;
  line-height: 1.8;
}

.store-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid currentColor;
  color: #7b3f2a;
  font-weight: 900;
  text-decoration: none;
  padding-bottom: 4px;
}

.location-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(320px, 0.75fr);
  min-height: 540px;
  border: 1px solid #2f2924;
  background: #fff;
}

.location-feature,
.location-row {
  color: inherit;
  text-decoration: none;
}

.location-feature {
  position: relative;
  display: grid;
  align-items: end;
  min-height: 540px;
  overflow: hidden;
  background: #2f2924;
}

.location-feature::after {
  position: absolute;
  inset: 0;
  content: "";
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.04), rgba(0, 0, 0, 0.72));
}

.location-feature > img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.location-feature-copy {
  position: relative;
  z-index: 1;
  max-width: 620px;
  color: #fff;
  padding: 38px;
}

.location-feature-copy p {
  margin: 18px 0 8px;
  color: #f0d8bd;
  font-weight: 900;
}

.location-feature-copy h3 {
  margin: 0 0 10px;
  font-size: 36px;
  font-weight: 900;
  line-height: 1.2;
}

.location-feature-copy > span:last-child {
  color: rgba(255, 255, 255, 0.84);
  line-height: 1.7;
}

.store-status {
  display: inline-flex;
  width: fit-content;
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

.location-side {
  display: grid;
  align-content: start;
  border-left: 1px solid #2f2924;
}

.location-counts {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  border-bottom: 1px solid #d9cec3;
}

.location-counts div {
  display: grid;
  gap: 2px;
  padding: 18px;
  border-right: 1px solid #d9cec3;
}

.location-counts div:last-child {
  border-right: 0;
}

.location-counts strong {
  color: #7b3f2a;
  font-size: 28px;
  font-weight: 900;
}

.location-counts span {
  color: #6c625a;
  font-size: 13px;
  font-weight: 900;
}

.location-row {
  display: grid;
  grid-template-columns: 108px minmax(0, 1fr);
  column-gap: 16px;
  row-gap: 4px;
  align-items: start;
  border-bottom: 1px solid #d9cec3;
  padding: 18px;
}

.location-row:hover {
  background: #fbf6f0;
}

.location-row img {
  grid-row: span 3;
  width: 108px;
  height: 88px;
  object-fit: cover;
  background: #eee8df;
}

.location-row span {
  color: #7b3f2a;
  font-size: 13px;
  font-weight: 900;
}

.location-row strong {
  color: #2f2924;
  font-size: 20px;
  font-weight: 900;
}

.location-row p {
  margin: 0;
  color: #625951;
  line-height: 1.6;
}

.location-note {
  display: grid;
  gap: 10px;
  padding: 20px;
  color: #625951;
  line-height: 1.7;
}

.location-note a {
  width: fit-content;
  color: #7b3f2a;
  font-weight: 900;
  text-decoration: none;
  border-bottom: 1px solid currentColor;
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
  .location-panel,
  .store-heading-row {
    grid-template-columns: 1fr;
  }

  .location-side {
    border-left: 0;
    border-top: 1px solid #2f2924;
  }

  .store-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .location-feature,
  .location-panel {
    min-height: auto;
  }

  .location-feature {
    min-height: 420px;
  }
}

@media (max-width: 640px) {
  .location-feature-copy {
    padding: 26px;
  }

  .location-feature-copy h3 {
    font-size: 28px;
  }

  .location-counts,
  .location-row {
    grid-template-columns: 1fr;
  }

  .location-row img {
    grid-row: auto;
    width: 100%;
    height: 170px;
  }
}
</style>
