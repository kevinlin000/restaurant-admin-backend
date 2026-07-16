<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import api from "@/api/axios";
import { homepageApi } from "@/api/homepage";
import { newsApi } from "@/api/news";

import aburiSalmonImage from "@/assets/images/aburi-salmon-sushi.jpg";
import caramelPuddingImage from "@/assets/images/caramel-pudding.jpg";
import homeImage from "@/assets/images/Home.jpg";
import japaneseSakeImage from "@/assets/images/japanese-sake.jpg";
import matchaDessertImage from "@/assets/images/matcha-dessert.jpg";
import reservationImage from "@/assets/images/reservation.jpg";
import salmonSashimiImage from "@/assets/images/salmon-sashimi.jpg";
import seafoodSaladImage from "@/assets/images/seafood-salad.jpg";
import sukiyakiImage from "@/assets/images/sukiyaki.jpg";
import tempuraImage from "@/assets/images/tempura.jpg";
import tofuImage from "@/assets/images/tofu.jpg";

const stores = ref([]);
const storeLoading = ref(false);
const storeError = ref("");
const newsItems = ref([]);
const homepageSetting = ref(null);
const activeHeroIndex = ref(0);
const heroOffsetX = ref(0);
const heroOffsetY = ref(0);

let heroTimer;
let revealObserver;

const unwrap = (response) => response.data?.data ?? response.data ?? [];

const cityPriority = ["台北市", "新北市", "桃園市", "新竹市", "台中市", "台南市", "高雄市", "花蓮縣"];

const imageMap = {
  home: homeImage,
  salmonSashimi: salmonSashimiImage,
  reservation: reservationImage,
  seafoodSalad: seafoodSaladImage,
  japaneseSake: japaneseSakeImage,
  aburiSalmon: aburiSalmonImage,
  sukiyaki: sukiyakiImage,
  tempura: tempuraImage,
  tofu: tofuImage,
  matchaDessert: matchaDessertImage,
  caramelPudding: caramelPuddingImage,
};

const featuredStores = computed(() => {
  const selectedIds = homepageSetting.value?.featuredStoreIds || [];
  return [...stores.value]
    .sort((a, b) => {
      const aIndex = selectedIds.indexOf(a.storeId);
      const bIndex = selectedIds.indexOf(b.storeId);
      if (aIndex !== -1 || bIndex !== -1) {
        if (aIndex === -1) return 1;
        if (bIndex === -1) return -1;
        return aIndex - bIndex;
      }
      const cityDiff = cityPriority.indexOf(a.city) - cityPriority.indexOf(b.city);
      if (cityDiff !== 0) return cityDiff;
      return `${a.storeName}`.localeCompare(`${b.storeName}`, "zh-Hant");
    })
    .slice(0, 4);
});

const primaryStore = computed(() => featuredStores.value[0] || null);
const supportingStores = computed(() => featuredStores.value.slice(1, 4));
const openStoreCount = computed(() => stores.value.filter((store) => store.openNow).length);
const cityCount = computed(() => new Set(stores.value.map((store) => store.city).filter(Boolean)).size);

const defaultHeroScenes = [
  {
    label: "夜席",
    image: homeImage,
    eyebrow: "Xuri Washoku",
    title: "敘日和食",
    lines: ["「敘」是敘舊，是放下手機後的深度對談", "「日」是時光，是歲月淬鍊出的滋味"],
  },
  {
    label: "旬味",
    image: salmonSashimiImage,
    eyebrow: "Seasonal Sashimi",
    title: "今日旬味",
    lines: ["低溫配送的魚身甜度", "讓聚餐從第一口開始慢下來"],
  },
  {
    label: "吧台",
    image: reservationImage,
    eyebrow: "Chef Counter",
    title: "一席之間",
    lines: ["從訂位到上菜都有清楚節奏", "把用餐體驗交給剛好的時間"],
  },
];

const heroScenes = computed(() => {
  const scenes = homepageSetting.value?.heroScenes;
  if (!Array.isArray(scenes) || !scenes.length) return defaultHeroScenes;

  return scenes.slice(0, 3).map((scene) => ({
    label: scene.label || "夜席",
    image: scene.imageUrl || imageMap[scene.imageKey] || homeImage,
    eyebrow: scene.eyebrow || "Xuri Washoku",
    title: scene.title || "敘日和食",
    lines: Array.isArray(scene.lines) && scene.lines.length
      ? scene.lines
      : ["「敘」是敘舊，是放下手機後的深度對談", "「日」是時光，是歲月淬鍊出的滋味"],
  }));
});

const activeHero = computed(() => heroScenes.value[activeHeroIndex.value] || heroScenes.value[0]);
const heroMotionStyle = computed(() => ({
  "--hero-x": `${heroOffsetX.value}px`,
  "--hero-y": `${heroOffsetY.value}px`,
}));

const signatureDishes = [
  {
    name: "炙燒鮭魚握壽司",
    course: "Nigiri",
    note: "昆布熟成鮭魚、赤醋飯、現炙油脂香",
    image: aburiSalmonImage,
  },
  {
    name: "胡麻豆腐",
    course: "Zensai",
    note: "白芝麻慢磨，佐柴魚高湯與山葵",
    image: tofuImage,
  },
  {
    name: "鮭魚刺身",
    course: "Sashimi",
    note: "每日低溫配送，厚切保留魚身甜度",
    image: salmonSashimiImage,
  },
  {
    name: "壽喜燒",
    course: "Nabe",
    note: "關西風割下醬汁，搭配溫泉蛋與蔬菜",
    image: sukiyakiImage,
  },
  {
    name: "天婦羅",
    course: "Agemono",
    note: "薄衣高溫快炸，保留海味與蔬菜水分",
    image: tempuraImage,
  },
  {
    name: "抹茶甘味",
    course: "Dessert",
    note: "宇治抹茶、黑糖蜜與手作布丁收尾",
    image: matchaDessertImage,
  },
];

const fallbackNews = [
  {
    id: "seasonal-counter",
    category: "季節料理",
    title: "夏季旬味上桌，刺身與冷物菜色同步更新",
    summary: "以清爽醋物、海鮮沙拉與炙燒握壽司組成夏日餐桌，適合聚餐與輕量用餐。",
    date: "2026.06",
    image: seafoodSaladImage,
  },
  {
    id: "reservation-window",
    category: "訂位公告",
    title: "假日熱門時段建議提前完成線上訂位",
    summary: "系統會依門市席位與時段即時回覆，完成後可於會員中心查看訂位狀態。",
    date: "2026.06",
    image: reservationImage,
  },
  {
    id: "sake-pairing",
    category: "品飲企劃",
    title: "晚間限定清酒搭餐組合開放門市預約",
    summary: "由店長依當日料理節奏搭配清酒與無酒精茶飲，提供更完整的和食體驗。",
    date: "2026.06",
    image: japaneseSakeImage,
  },
];

const experienceStats = computed(() => [
  { value: stores.value.length || 7, label: "門市據點" },
  { value: cityCount.value || 6, label: "服務城市" },
  { value: openStoreCount.value || 0, label: "營業中" },
  { value: "18:00", label: "晚餐熱門席次" },
]);

const homepageNews = computed(() => {
  const source = newsItems.value.length ? newsItems.value : fallbackNews;
  const selectedIds = homepageSetting.value?.featuredNewsIds || [];

  return [...source]
    .sort((a, b) => {
      const aIndex = selectedIds.indexOf(a.id);
      const bIndex = selectedIds.indexOf(b.id);
      if (aIndex !== -1 || bIndex !== -1) {
        if (aIndex === -1) return 1;
        if (bIndex === -1) return -1;
        return aIndex - bIndex;
      }
      return 0;
    })
    .slice(0, 3);
});

const homepageCopy = computed(() => ({
  storyKicker: homepageSetting.value?.storyKicker || "Xuri Table",
  storyTitle: homepageSetting.value?.storyTitle || "把忙碌留在門外，讓一餐飯重新有時間感。",
  storyDescription: homepageSetting.value?.storyDescription || "敘日把和食做成日常可以靠近的樣子：保留刺身、握壽司、鍋物與甘味的細節，也把訂位、門市與菜單動線整理成清楚的節奏。從進門、點餐到結帳，每個環節都為聚餐的人服務。",
  signatureKicker: homepageSetting.value?.signatureKicker || "Seasonal Selection",
  signatureTitle: homepageSetting.value?.signatureTitle || "今天想吃的，不只是一份菜單。",
  storeKicker: homepageSetting.value?.storeKicker || "Locations",
  storeTitle: homepageSetting.value?.storeTitle || "今晚坐哪一間敘日。",
  storeDescription: homepageSetting.value?.storeDescription || "依城市、交通與用餐情境挑選門市，讓前往餐桌這件事也輕鬆。",
  newsKicker: homepageSetting.value?.newsKicker || "News",
  newsTitle: homepageSetting.value?.newsTitle || "近期公告與餐期更新。",
  reservationKicker: homepageSetting.value?.reservationKicker || "Reservation",
  reservationTitle: homepageSetting.value?.reservationTitle || "從今天的城市，選一張剛好的桌。",
  reservationDescription: homepageSetting.value?.reservationDescription || "選擇門市、日期、人數與時段後，系統會依席位與營業狀態回覆可訂選項。團體聚餐或特殊需求，也可以在訂位備註中先告訴店長。",
}));

const currentMealPeriod = computed(() => {
  const hour = new Date().getHours();
  if (hour < 11) {
    return { label: "午餐準備", time: "11:30", note: "訂位開放中" };
  }
  if (hour < 15) {
    return { label: "午餐餐期", time: "12:00", note: "建議先訂位" };
  }
  if (hour < 17) {
    return { label: "晚餐準備", time: "18:00", note: "熱門席次" };
  }
  return { label: "晚餐餐期", time: "18:00", note: "現正服務" };
});

const heroSignals = computed(() => {
  const totalStores = stores.value.length || 7;
  const store = primaryStore.value;
  const news = homepageNews.value[0];

  return [
    {
      label: "今日營業",
      value: `${openStoreCount.value || 0}/${totalStores}`,
      note: openStoreCount.value > 0 ? "門市開放中" : "準備下一餐期",
    },
    {
      label: "推薦門市",
      value: store?.city || "台北市",
      note: store?.storeName || "敘日信義 A11 店",
    },
    {
      label: "近期公告",
      value: news?.category || "季節料理",
      note: news?.title || "夏季旬味同步更新",
    },
  ];
});

const storeLocation = (store) => [store.city, store.district].filter(Boolean).join(" ");
const openStatusText = (store) => (store.openNow ? "營業中" : "非營業時間");
const storeImage = (store) => store?.mainImageUrl || "/store-images/xuri-dining-room.jpg";

const formatNewsDate = (value) => {
  if (!value) return "最新";
  return `${value}`.slice(0, 10).replaceAll("-", ".");
};

const normalizeNews = (article, index) => ({
  id: article.newsId ?? article.id ?? `news-${index}`,
  category: article.categoryLabel || article.category || "最新消息",
  title: article.title || "敘日最新消息",
  summary: article.summary || article.content || "更多門市活動與餐期更新，請至最新消息頁查看。",
  date: formatNewsDate(article.publishedAt || article.startDate || article.createdAt),
  image: article.coverImageUrl || fallbackNews[index % fallbackNews.length].image,
});

const startHeroTimer = () => {
  if (heroTimer) {
    window.clearInterval(heroTimer);
  }
  if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;

  heroTimer = window.setInterval(() => {
    activeHeroIndex.value = (activeHeroIndex.value + 1) % heroScenes.value.length;
  }, 7600);
};

const selectHeroScene = (index) => {
  activeHeroIndex.value = index;
  startHeroTimer();
};

const handleHeroPointer = (event) => {
  if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
  if (!event.currentTarget) return;

  const rect = event.currentTarget.getBoundingClientRect();
  const x = (event.clientX - rect.left) / rect.width - 0.5;
  const y = (event.clientY - rect.top) / rect.height - 0.5;
  heroOffsetX.value = Number((-x * 18).toFixed(2));
  heroOffsetY.value = Number((-y * 12).toFixed(2));
};

const resetHeroPointer = () => {
  heroOffsetX.value = 0;
  heroOffsetY.value = 0;
};

const loadHomepageSetting = async () => {
  try {
    homepageSetting.value = await homepageApi.getHomepage();
    activeHeroIndex.value = 0;
  } catch (error) {
    homepageSetting.value = null;
  }
};

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

const loadNews = async () => {
  try {
    const articles = await newsApi.getPublishedNews();
    newsItems.value = Array.isArray(articles) ? articles.map(normalizeNews) : [];
  } catch (error) {
    newsItems.value = [];
  }
};

onMounted(() => {
  loadHomepageSetting();
  loadFeaturedStores();
  loadNews();

  startHeroTimer();

  nextTick(() => {
    revealObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("is-visible");
            revealObserver.unobserve(entry.target);
          }
        });
      },
      { rootMargin: "0px 0px -12% 0px", threshold: 0.18 },
    );

    document.querySelectorAll(".home-reveal").forEach((element) => revealObserver.observe(element));
  });
});

onBeforeUnmount(() => {
  if (heroTimer) {
    window.clearInterval(heroTimer);
  }
  if (revealObserver) {
    revealObserver.disconnect();
  }
});
</script>

<template>
  <section
    class="introduction-section"
    :style="heroMotionStyle"
    aria-label="敘日和食首頁主視覺"
    @pointermove="handleHeroPointer"
    @pointerleave="resetHeroPointer"
  >
    <div class="hero-scenes" aria-hidden="true">
      <div
        v-for="(scene, index) in heroScenes"
        :key="scene.label"
        :class="['hero-scene', { active: index === activeHeroIndex }]"
        :style="{ backgroundImage: `url(${scene.image})` }"
      ></div>
    </div>
    <div class="hero-gradient" aria-hidden="true"></div>

    <div class="introduction-overlay">
      <div class="introduction-content">
        <span class="hero-eyebrow">{{ activeHero.eyebrow }}</span>
        <h1 class="introduction-title yuji-boku-regular">敘日和食</h1>
        <p class="introduction-desc">
          <template v-for="(line, index) in activeHero.lines" :key="line">
            {{ line }}
            <br v-if="index < activeHero.lines.length - 1" />
          </template>
        </p>
        <div class="introduction-buttons">
          <RouterLink class="primary-btn hero-btn" to="/reservation">立即訂位</RouterLink>
          <RouterLink class="secondary-btn hero-btn" to="/menu">瀏覽菜單</RouterLink>
        </div>

        <div class="hero-console" aria-label="今日餐期資訊">
          <div class="meal-status">
            <span>{{ currentMealPeriod.label }}</span>
            <strong>{{ currentMealPeriod.time }}</strong>
            <p>{{ currentMealPeriod.note }}</p>
          </div>

          <div class="hero-signal-grid">
            <div v-for="signal in heroSignals" :key="signal.label" class="hero-signal">
              <span>{{ signal.label }}</span>
              <strong>{{ signal.value }}</strong>
              <p>{{ signal.note }}</p>
            </div>
          </div>
        </div>

        <div class="hero-scene-tabs" aria-label="切換首頁情境">
          <button
            v-for="(scene, index) in heroScenes"
            :key="scene.label"
            type="button"
            :class="{ active: index === activeHeroIndex }"
            :aria-label="`切換至${scene.label}情境`"
            :aria-pressed="index === activeHeroIndex"
            @click="selectHeroScene(index)"
          >
            <span>{{ scene.label }}</span>
          </button>
        </div>
      </div>
    </div>
  </section>

  <section class="experience-bar" aria-label="品牌營運摘要">
    <div class="experience-inner">
      <div v-for="item in experienceStats" :key="item.label" class="experience-item">
        <strong>{{ item.value }}</strong>
        <span>{{ item.label }}</span>
      </div>
      <RouterLink class="experience-link" to="/reservation">
        選擇今日席位
        <i class="bi bi-arrow-right"></i>
      </RouterLink>
    </div>
  </section>

  <section id="about" class="story-section home-reveal">
    <div class="section-container story-grid">
      <div class="section-copy">
        <span class="section-kicker">{{ homepageCopy.storyKicker }}</span>
        <h2>{{ homepageCopy.storyTitle }}</h2>
        <p>{{ homepageCopy.storyDescription }}</p>
      </div>

      <div class="story-visual">
        <img class="story-main" :src="reservationImage" alt="敘日用餐空間" />
        <div class="story-note">
          <span>Chef Counter</span>
          <strong>近距離看見料理完成的節奏</strong>
        </div>
        <img class="story-accent" :src="caramelPuddingImage" alt="焦糖布丁" />
      </div>
    </div>
  </section>

  <section id="menu" class="signature-section home-reveal">
    <div class="section-container">
      <div class="section-head">
        <div>
          <span class="section-kicker">{{ homepageCopy.signatureKicker }}</span>
          <h2>{{ homepageCopy.signatureTitle }}</h2>
        </div>
        <RouterLink class="text-link" to="/menu">完整菜單</RouterLink>
      </div>

      <div class="signature-grid">
        <RouterLink
          v-for="dish in signatureDishes"
          :key="dish.name"
          class="dish-card"
          to="/menu"
        >
          <img :src="dish.image" :alt="dish.name" />
          <div class="dish-copy">
            <span>{{ dish.course }}</span>
            <h3>{{ dish.name }}</h3>
            <p>{{ dish.note }}</p>
          </div>
        </RouterLink>
      </div>
    </div>
  </section>

  <section id="store" class="store-section home-reveal">
    <div class="section-container">
      <div class="section-head">
        <div>
          <span class="section-kicker">{{ homepageCopy.storeKicker }}</span>
          <h2>{{ homepageCopy.storeTitle }}</h2>
          <p>{{ homepageCopy.storeDescription }}</p>
        </div>
        <RouterLink class="text-link" to="/store">全部門市</RouterLink>
      </div>

      <div v-if="storeLoading" class="store-state">
        <span class="spinner-border spinner-border-sm"></span>
        載入門市資訊
      </div>

      <div v-else-if="storeError" class="store-state warning">
        {{ storeError }}
      </div>

      <div v-else class="location-panel">
        <RouterLink v-if="primaryStore" class="location-feature" :to="{ name: 'CustomerStore' }">
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

          <div class="location-reserve">
            <span>需要快速決定？</span>
            <strong>先選時段，再讓系統帶你到適合門市。</strong>
            <RouterLink class="primary-btn compact-btn" to="/reservation">立即訂位</RouterLink>
          </div>
        </div>
      </div>
    </div>
  </section>

  <section id="news" class="news-section home-reveal">
    <div class="section-container">
      <div class="section-head">
        <div>
          <span class="section-kicker">{{ homepageCopy.newsKicker }}</span>
          <h2>{{ homepageCopy.newsTitle }}</h2>
        </div>
        <RouterLink class="text-link" to="/news">查看全部消息</RouterLink>
      </div>

      <div class="news-grid">
        <RouterLink v-for="item in homepageNews" :key="item.id" class="news-card" to="/news">
          <img :src="item.image" :alt="item.title" />
          <div>
            <span>{{ item.category }} / {{ item.date }}</span>
            <h3>{{ item.title }}</h3>
            <p>{{ item.summary }}</p>
          </div>
        </RouterLink>
      </div>
    </div>
  </section>

  <section class="reservation-section home-reveal">
    <div class="reservation-media">
      <img :src="reservationImage" alt="敘日訂位用餐空間" />
    </div>
    <div class="reservation-copy">
      <span class="section-kicker">{{ homepageCopy.reservationKicker }}</span>
      <h2>{{ homepageCopy.reservationTitle }}</h2>
      <p>{{ homepageCopy.reservationDescription }}</p>
      <div class="reservation-steps">
        <span>01 選門市</span>
        <span>02 選時段</span>
        <span>03 收確認</span>
      </div>
      <RouterLink class="primary-btn reserve-btn" to="/reservation">開始訂位</RouterLink>
    </div>
  </section>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Sans+TC:wght@400;500;700;900&family=Noto+Serif+TC:wght@500;600;700;900&family=Yuji+Boku&display=swap");

.yuji-boku-regular {
  font-family: "Yuji Boku", "Noto Serif TC", serif;
  font-style: normal;
  font-weight: 400;
}

.introduction-section,
.experience-bar,
.story-section,
.signature-section,
.store-section,
.news-section,
.reservation-section {
  font-family: "Noto Sans TC", system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  letter-spacing: 0;
}

.section-container {
  width: min(1180px, calc(100% - 48px));
  margin: 0 auto;
}

.section-kicker {
  display: inline-flex;
  margin-bottom: 14px;
  color: #a86537;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 32px;
  align-items: end;
  margin-bottom: 34px;
}

.section-head h2,
.section-copy h2,
.reservation-copy h2 {
  margin: 0;
  color: #221a15;
  font-family: "Noto Serif TC", "Yuji Boku", serif;
  font-size: 42px;
  font-weight: 700;
  letter-spacing: 0;
  line-height: 1.45;
}

.section-head p,
.section-copy p,
.reservation-copy p {
  max-width: 650px;
  margin: 16px 0 0;
  color: #6d6259;
  font-size: 17px;
  font-weight: 400;
  letter-spacing: 0;
  line-height: 1.9;
}

.primary-btn,
.secondary-btn,
.text-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  text-decoration: none;
  transition: transform 180ms ease, border-color 180ms ease, background 180ms ease, color 180ms ease;
}

.primary-btn {
  min-height: 48px;
  border: 1px solid #dca874;
  border-radius: 8px;
  background: #dca874;
  color: #fff;
  padding: 12px 26px;
  font-weight: 700;
}

.secondary-btn {
  min-height: 48px;
  border: 1px solid rgba(255, 255, 255, 0.64);
  border-radius: 8px;
  color: #fff;
  padding: 12px 26px;
  font-weight: 700;
}

.primary-btn:hover,
.secondary-btn:hover,
.text-link:hover {
  transform: translateY(-2px);
}

.text-link {
  width: fit-content;
  border-bottom: 1px solid currentColor;
  color: #8d5431;
  font-weight: 700;
  padding-bottom: 4px;
}

.introduction-section {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 100vh;
  overflow: hidden;
  background: #050403;
  isolation: isolate;
}

.hero-scenes,
.hero-scene,
.hero-gradient {
  position: absolute;
  inset: 0;
}

.hero-scenes {
  z-index: -3;
  overflow: hidden;
}

.hero-scene {
  background-position: center;
  background-size: cover;
  opacity: 0;
  transform: translate3d(var(--hero-x, 0), var(--hero-y, 0), 0) scale(1.06);
  transition: opacity 1400ms ease, transform 900ms ease;
  will-change: opacity, transform;
}

.hero-scene.active {
  opacity: 1;
}

.hero-gradient {
  z-index: -2;
  background:
    radial-gradient(circle at 74% 38%, rgba(166, 82, 34, 0.12), transparent 30%),
    linear-gradient(90deg, rgba(0, 0, 0, 0.84) 0%, rgba(0, 0, 0, 0.64) 36%, rgba(0, 0, 0, 0.2) 78%),
    linear-gradient(180deg, rgba(0, 0, 0, 0.16), rgba(0, 0, 0, 0.68));
}

.hero-gradient::after {
  position: absolute;
  inset: 0;
  content: "";
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.028) 1px, transparent 1px);
  background-size: 120px 120px;
  mask-image: linear-gradient(90deg, rgba(0, 0, 0, 0.68), transparent 68%);
  pointer-events: none;
}

.introduction-overlay {
  width: 100%;
}

.introduction-content {
  position: relative;
  width: min(1180px, calc(100% - 48px));
  margin: 0 auto;
  padding-top: 50px;
  color: #fff;
}

.hero-eyebrow {
  display: inline-flex;
  margin-bottom: 18px;
  color: #dca874;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
}

.introduction-title {
  margin: 0 0 28px;
  color: #fff;
  font-size: 76px;
  letter-spacing: 0;
  line-height: 1.22;
  text-shadow: 0 10px 30px rgba(0, 0, 0, 0.28);
}

.introduction-desc {
  display: block;
  width: fit-content;
  margin: 0;
  border-left: 1px solid rgba(220, 168, 116, 0.72);
  padding: 2px 0 2px 20px;
  color: rgba(255, 255, 255, 0.9);
  font-family: "Noto Serif TC", serif;
  font-size: 21px;
  font-weight: 600;
  letter-spacing: 0;
  line-height: 2;
}

.introduction-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  margin-top: 48px;
}

.hero-btn {
  min-width: 156px;
}

.hero-console {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr);
  width: min(720px, 100%);
  margin-top: 52px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: rgba(10, 7, 5, 0.5);
  backdrop-filter: blur(18px);
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.24);
}

.meal-status {
  display: grid;
  align-content: center;
  gap: 5px;
  border-right: 1px solid rgba(255, 255, 255, 0.18);
  padding: 20px;
}

.meal-status span,
.hero-signal span {
  color: rgba(255, 255, 255, 0.62);
  font-size: 12px;
  font-weight: 700;
}

.meal-status strong {
  color: #fff;
  font-family: "Noto Serif TC", serif;
  font-size: 34px;
  font-variant-numeric: tabular-nums;
  font-weight: 700;
  line-height: 1.1;
}

.meal-status p,
.hero-signal p {
  margin: 0;
  color: rgba(255, 255, 255, 0.76);
  font-size: 13px;
  line-height: 1.5;
}

.hero-signal-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.hero-signal {
  display: grid;
  align-content: center;
  gap: 6px;
  min-height: 112px;
  border-right: 1px solid rgba(255, 255, 255, 0.14);
  padding: 18px;
}

.hero-signal:last-child {
  border-right: 0;
}

.hero-signal strong {
  display: block;
  min-width: 0;
  overflow: hidden;
  color: #fff;
  font-family: "Noto Serif TC", serif;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hero-signal p {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.hero-scene-tabs {
  display: flex;
  gap: 10px;
  margin-top: 24px;
}

.hero-scene-tabs button {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 54px;
  min-height: 34px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.72);
  font: inherit;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: border-color 180ms ease, background 180ms ease, color 180ms ease;
}

.hero-scene-tabs button.active,
.hero-scene-tabs button:hover {
  border-color: rgba(220, 168, 116, 0.82);
  background: rgba(220, 168, 116, 0.14);
  color: #fff;
}

.home-reveal {
  opacity: 0;
  transform: translateY(32px);
  transition: opacity 680ms ease, transform 680ms ease;
}

.home-reveal.is-visible {
  opacity: 1;
  transform: translateY(0);
}

.experience-bar {
  position: relative;
  z-index: 2;
  margin-top: -1px;
  border-top: 1px solid rgba(255, 255, 255, 0.14);
  border-bottom: 1px solid #ded4ca;
  background: #fffaf4;
}

.experience-inner {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr)) auto;
  width: min(1180px, calc(100% - 48px));
  margin: 0 auto;
}

.experience-item {
  display: grid;
  gap: 4px;
  border-left: 1px solid #ded4ca;
  padding: 24px 22px;
}

.experience-item:nth-child(4) {
  border-right: 1px solid #ded4ca;
}

.experience-item strong {
  color: #221a15;
  font-family: "Noto Serif TC", Georgia, serif;
  font-size: 34px;
  font-variant-numeric: tabular-nums;
  font-weight: 700;
  line-height: 1;
}

.experience-item span {
  color: #76695f;
  font-size: 13px;
  font-weight: 700;
}

.experience-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-width: 180px;
  background: #221a15;
  color: #fff;
  font-weight: 700;
  text-decoration: none;
}

.story-section,
.signature-section,
.news-section {
  padding: 118px 0;
  background: #fffaf4;
}

.story-grid {
  display: grid;
  grid-template-columns: minmax(0, 0.82fr) minmax(420px, 1.18fr);
  gap: 64px;
  align-items: center;
}

.story-visual {
  position: relative;
  min-height: 560px;
}

.story-main {
  width: 82%;
  height: 520px;
  object-fit: cover;
  box-shadow: 0 28px 72px rgba(30, 18, 10, 0.18);
}

.story-accent {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 36%;
  min-width: 210px;
  height: 270px;
  border: 12px solid #fffaf4;
  object-fit: cover;
}

.story-note {
  position: absolute;
  right: 32px;
  top: 42px;
  display: grid;
  gap: 6px;
  max-width: 230px;
  background: rgba(34, 26, 21, 0.92);
  color: #fff;
  padding: 22px;
}

.story-note span {
  color: #dca874;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
}

.story-note strong {
  font-family: "Noto Serif TC", serif;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.6;
}

.signature-section {
  background: #17120f;
  color: #fff;
}

.signature-section .section-head h2,
.signature-section .text-link {
  color: #fff;
}

.signature-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1px;
  background: rgba(255, 255, 255, 0.16);
}

.dish-card {
  position: relative;
  display: grid;
  min-height: 430px;
  overflow: hidden;
  color: #fff;
  text-decoration: none;
  isolation: isolate;
}

.dish-card::after {
  position: absolute;
  inset: 0;
  z-index: -1;
  content: "";
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.02), rgba(0, 0, 0, 0.76));
}

.dish-card img {
  position: absolute;
  inset: 0;
  z-index: -2;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 460ms ease;
}

.dish-card:hover img {
  transform: scale(1.06);
}

.dish-copy {
  align-self: end;
  padding: 28px;
}

.dish-copy span {
  color: #f1bd85;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
}

.dish-copy h3 {
  margin: 10px 0 8px;
  font-family: "Noto Serif TC", serif;
  font-size: 26px;
  font-weight: 700;
  line-height: 1.42;
}

.dish-copy p {
  margin: 0;
  color: rgba(255, 255, 255, 0.82);
  font-weight: 400;
  letter-spacing: 0;
  line-height: 1.7;
}

.store-section {
  padding: 118px 0;
  background: #f2ece5;
}

.location-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.16fr) minmax(360px, 0.84fr);
  min-height: 560px;
  border: 1px solid #2f2924;
  background: #fffaf4;
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
  min-height: 560px;
  overflow: hidden;
  background: #2f2924;
}

.location-feature::after {
  position: absolute;
  inset: 0;
  content: "";
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.04), rgba(0, 0, 0, 0.78));
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
  max-width: 640px;
  color: #fff;
  padding: 42px;
}

.location-feature-copy p {
  margin: 18px 0 8px;
  color: #f0d8bd;
  font-weight: 700;
}

.location-feature-copy h3 {
  margin: 0 0 10px;
  font-family: "Noto Serif TC", serif;
  font-size: 40px;
  font-weight: 700;
  line-height: 1.32;
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
  font-weight: 700;
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

.location-row {
  display: grid;
  grid-template-columns: 116px minmax(0, 1fr);
  column-gap: 18px;
  row-gap: 4px;
  align-items: start;
  border-bottom: 1px solid #d9cec3;
  padding: 20px;
}

.location-row:hover {
  background: #fff6ec;
}

.location-row img {
  grid-row: span 3;
  width: 116px;
  height: 92px;
  object-fit: cover;
  background: #eee8df;
}

.location-row span {
  color: #9a5d35;
  font-size: 13px;
  font-weight: 700;
}

.location-row strong {
  color: #2f2924;
  font-family: "Noto Serif TC", serif;
  font-size: 21px;
  font-weight: 700;
}

.location-row p {
  margin: 0;
  color: #625951;
  line-height: 1.6;
}

.location-reserve {
  display: grid;
  gap: 10px;
  padding: 26px;
}

.location-reserve span {
  color: #9a5d35;
  font-size: 13px;
  font-weight: 700;
}

.location-reserve strong {
  color: #2f2924;
  font-family: "Noto Serif TC", serif;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.5;
}

.compact-btn {
  width: fit-content;
  margin-top: 8px;
}

.store-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 180px;
  border: 1px dashed #d7c6b7;
  background: rgba(255, 255, 255, 0.76);
  color: #697386;
  font-weight: 800;
}

.store-state.warning {
  color: #a16012;
}

.news-section {
  background: #fffaf4;
}

.news-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.9fr 0.9fr;
  gap: 20px;
}

.news-card {
  display: grid;
  grid-template-rows: 260px 1fr;
  min-height: 480px;
  border: 1px solid #dfd4c9;
  background: #fff;
  color: inherit;
  text-decoration: none;
}

.news-card:first-child {
  grid-template-rows: 330px 1fr;
}

.news-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.news-card div {
  padding: 24px;
}

.news-card span {
  color: #9a5d35;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
}

.news-card h3 {
  margin: 12px 0 10px;
  color: #2f2924;
  font-family: "Noto Serif TC", serif;
  font-size: 25px;
  font-weight: 700;
  line-height: 1.55;
}

.news-card p {
  margin: 0;
  color: #625951;
  line-height: 1.75;
}

.reservation-section {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(420px, 0.95fr);
  min-height: 620px;
  background: #201712;
  color: #fff;
}

.reservation-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.reservation-copy {
  display: grid;
  align-content: center;
  padding: 72px min(8vw, 96px);
}

.reservation-copy h2 {
  color: #fff;
}

.reservation-copy p {
  color: rgba(255, 255, 255, 0.78);
}

.reservation-steps {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1px;
  margin: 34px 0;
  background: rgba(255, 255, 255, 0.18);
}

.reservation-steps span {
  min-height: 72px;
  background: #201712;
  color: rgba(255, 255, 255, 0.86);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.reserve-btn {
  width: fit-content;
}

@media (max-width: 1080px) {
  .experience-inner,
  .story-grid,
  .location-panel,
  .reservation-section {
    grid-template-columns: 1fr;
  }

  .experience-inner {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .experience-link {
    grid-column: 1 / -1;
    min-height: 64px;
  }

  .story-visual {
    min-height: 480px;
  }

  .location-side {
    border-top: 1px solid #2f2924;
    border-left: 0;
  }

  .news-grid,
  .signature-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .section-container,
  .introduction-content,
  .experience-inner {
    width: min(100% - 32px, 1180px);
  }

  .section-head {
    display: grid;
    gap: 18px;
  }

  .section-head h2,
  .section-copy h2,
  .reservation-copy h2 {
    font-size: 30px;
  }

  .introduction-section {
    align-items: flex-start;
    min-height: auto;
    padding: 148px 0 48px;
  }

  .hero-scene {
    background-position: 66% center;
  }

  .introduction-title {
    font-size: 48px;
  }

  .introduction-desc {
    font-size: 18px;
  }

  .introduction-buttons {
    align-items: stretch;
    flex-direction: column;
    max-width: 230px;
  }

  .hero-console {
    grid-template-columns: 1fr;
    margin-top: 36px;
  }

  .meal-status {
    grid-template-columns: 1fr auto;
    border-right: 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.18);
  }

  .meal-status strong {
    grid-column: 2;
    grid-row: 1 / span 2;
    align-self: center;
    font-size: 30px;
  }

  .meal-status p {
    grid-column: 1;
  }

  .hero-signal-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .hero-signal {
    min-height: 104px;
    border-right: 1px solid rgba(255, 255, 255, 0.14);
    border-bottom: 0;
    padding: 14px 12px;
  }

  .hero-signal:last-child {
    border-right: 0;
  }

  .hero-signal strong {
    font-size: 17px;
  }

  .hero-signal p {
    -webkit-line-clamp: 1;
  }

  .hero-scene-tabs {
    flex-wrap: wrap;
  }

  .experience-inner,
  .signature-grid,
  .news-grid {
    grid-template-columns: 1fr;
  }

  .experience-inner {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .experience-link {
    grid-column: 1 / -1;
    min-height: 58px;
  }

  .experience-item,
  .experience-item:nth-child(4) {
    border-right: 1px solid #ded4ca;
  }

  .story-section,
  .signature-section,
  .store-section,
  .news-section {
    padding: 78px 0;
  }

  .story-visual {
    min-height: auto;
  }

  .story-main {
    width: 100%;
    height: 360px;
  }

  .story-accent,
  .story-note {
    position: static;
    width: 100%;
    min-width: 0;
    height: auto;
    border: 0;
  }

  .story-accent {
    margin-top: 12px;
    aspect-ratio: 4 / 3;
  }

  .dish-card {
    min-height: 340px;
  }

  .location-feature {
    min-height: 390px;
  }

  .location-feature-copy {
    padding: 28px;
  }

  .location-feature-copy h3 {
    font-size: 30px;
  }

  .location-row {
    grid-template-columns: 1fr;
  }

  .location-row img {
    grid-row: auto;
    width: 100%;
    height: 190px;
  }

  .news-card,
  .news-card:first-child {
    grid-template-rows: 240px 1fr;
    min-height: auto;
  }

  .reservation-copy {
    padding: 56px 24px;
  }

  .reservation-steps {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .primary-btn,
  .secondary-btn,
  .text-link,
  .dish-card img,
  .hero-scene,
  .home-reveal {
    transition: none;
  }

  .hero-scene {
    transform: scale(1.02);
  }
}
</style>
