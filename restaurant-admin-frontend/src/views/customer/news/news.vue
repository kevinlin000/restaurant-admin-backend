<script setup>
import { computed, onMounted, ref } from "vue";

import heroImage from "@/assets/images/sashimi.jpg";
import seasonalImage from "@/assets/images/salmon-sashimi.jpg";
import openingImage from "@/assets/images/sushi.jpg";
import memberImage from "@/assets/images/matcha-dessert.jpg";
import noticeImage from "@/assets/images/japanese-tea.jpg";
import takeoutImage from "@/assets/images/sukiyaki.jpg";
import diningImage from "@/assets/images/asahi-beer.jpg";

const categories = [
  { label: "全部", value: "all" },
  { label: "活動", value: "event" },
  { label: "公告", value: "notice" },
  { label: "展店", value: "opening" },
  { label: "會員", value: "member" },
];

const newsItems = [
  {
    id: 1,
    category: "event",
    categoryLabel: "活動",
    publishedAt: "2026.06.24",
    period: "2026.06.24 - 2026.08.31",
    title: "夏旬和食祭｜海味、炙燒與清酒佐餐同步登場",
    summary:
      "以鮭魚、干貝、季節野菜與吟釀酒香搭出夏季限定菜色，內用套餐可加購指定佐餐飲品。",
    storeScope: "全門市適用",
    image: seasonalImage,
    highlight: true,
    ctaText: "預約席次",
    ctaTo: "/reservation",
  },
  {
    id: 2,
    category: "opening",
    categoryLabel: "展店",
    publishedAt: "2026.06.21",
    period: "2026.07.05 - 2026.07.31",
    title: "台中勤美店試營運公告｜午間席次優先開放",
    summary:
      "新門市試營運期間採分段開放訂位，午餐、下午茶與晚餐席次將依現場準備狀況逐步增加。",
    storeScope: "台中勤美店",
    image: openingImage,
    ctaText: "查看門市",
    ctaTo: "/store",
  },
  {
    id: 3,
    category: "notice",
    categoryLabel: "公告",
    publishedAt: "2026.06.20",
    period: "長期公告",
    title: "重要提醒｜請透過官方網站、APP 或門市電話完成訂位",
    summary:
      "敘日未授權第三方代訂平台收取訂金或轉售席次，所有訂位資訊以官方網站、會員 APP 與門市公告為準。",
    storeScope: "全門市適用",
    image: noticeImage,
    ctaText: "查看門市電話",
    ctaTo: "/store",
    important: true,
  },
  {
    id: 4,
    category: "member",
    categoryLabel: "會員",
    publishedAt: "2026.06.18",
    period: "2026.07.01 - 2026.07.14",
    title: "敘日會員週｜平日午餐點數雙倍累積",
    summary:
      "會員平日 11:30 至 14:00 內用，單筆滿額享點數雙倍累積，可與生日禮擇優使用。",
    storeScope: "全門市適用",
    image: memberImage,
    ctaText: "會員登入",
    ctaTo: "/login",
  },
  {
    id: 5,
    category: "event",
    categoryLabel: "活動",
    publishedAt: "2026.06.14",
    period: "2026.06.27 - 2026.08.28",
    title: "雙人餐酒夜｜週四晚餐限定席",
    summary:
      "精選雙人套餐搭配指定飲品，適合慶生、約會與下班後的小型聚餐。部分門市提供吧檯席。",
    storeScope: "台北信義店、新竹巨城店、台中勤美店",
    image: diningImage,
    ctaText: "預約晚餐",
    ctaTo: "/reservation",
  },
  {
    id: 6,
    category: "notice",
    categoryLabel: "公告",
    publishedAt: "2026.06.10",
    period: "長期公告",
    title: "外帶自取包裝調整｜鍋物與生食餐點分裝升級",
    summary:
      "為維持餐點狀態，外帶自取餐盒將依品項調整為冷熱分裝，部分套餐備餐時間同步延長。",
    storeScope: "供應外帶門市",
    image: takeoutImage,
    ctaText: "前往點餐",
    ctaTo: "/order",
  },
];

const activeCategory = ref("all");

const featuredNews = computed(() => newsItems.find((item) => item.highlight) || newsItems[0]);

const filteredNews = computed(() => {
  if (activeCategory.value === "all") {
    return newsItems;
  }

  return newsItems.filter((item) => item.category === activeCategory.value);
});

const listNews = computed(() =>
  filteredNews.value.filter((item) => item.id !== featuredNews.value.id),
);

const visibleCountLabel = computed(() => `${filteredNews.value.length} 則消息`);

onMounted(() => {
  requestAnimationFrame(() => {
    window.scrollTo({ top: 0, left: 0, behavior: "instant" });
  });
});
</script>

<template>
  <section class="news-page">
    <header class="news-header" :style="{ backgroundImage: `url(${heroImage})` }">
      <div class="news-header__shade">
        <div class="news-container">
          <nav class="breadcrumb-line" aria-label="目前位置">
            <RouterLink to="/home">首頁</RouterLink>
            <span>/</span>
            <span>最新消息</span>
          </nav>
          <p class="section-label">News</p>
          <h1>最新消息</h1>
          <p class="header-copy">
            活動、門市營運與重要公告都集中在這裡。每則消息標示發布日期、適用期間與門市，方便安排訂位與用餐。
          </p>
        </div>
      </div>
    </header>

    <main class="news-container news-content">
      <section class="featured-block" aria-labelledby="featured-title">
        <div class="section-heading">
          <p class="section-label">Selected</p>
          <h2 id="featured-title">本期焦點</h2>
        </div>

        <article class="featured-article">
          <img :src="featuredNews.image" :alt="featuredNews.title" />
          <div class="featured-copy">
            <div class="news-meta">
              <span>{{ featuredNews.categoryLabel }}</span>
              <time>{{ featuredNews.publishedAt }}</time>
            </div>
            <h3>{{ featuredNews.title }}</h3>
            <p>{{ featuredNews.summary }}</p>
            <dl class="event-facts">
              <div>
                <dt>期間</dt>
                <dd>{{ featuredNews.period }}</dd>
              </div>
              <div>
                <dt>門市</dt>
                <dd>{{ featuredNews.storeScope }}</dd>
              </div>
            </dl>
            <RouterLink class="text-action dark" :to="featuredNews.ctaTo">
              {{ featuredNews.ctaText }}
              <i class="bi bi-arrow-right"></i>
            </RouterLink>
          </div>
        </article>
      </section>

      <section class="news-index" aria-labelledby="news-index-title">
        <div class="index-toolbar">
          <div>
            <p class="section-label">Archive</p>
            <h2 id="news-index-title">消息列表</h2>
          </div>
          <span class="result-count">{{ visibleCountLabel }}</span>
        </div>

        <div class="category-tabs" aria-label="消息分類">
          <button
            v-for="category in categories"
            :key="category.value"
            type="button"
            :class="['category-tab', { active: activeCategory === category.value }]"
            @click="activeCategory = category.value"
          >
            {{ category.label }}
          </button>
        </div>

        <div class="news-list">
          <article
            v-for="item in listNews"
            :key="item.id"
            :class="['news-item', { important: item.important }]"
          >
            <div class="date-block">
              <time>{{ item.publishedAt }}</time>
              <span>{{ item.categoryLabel }}</span>
            </div>
            <img :src="item.image" :alt="item.title" />
            <div class="news-item__body">
              <h3>{{ item.title }}</h3>
              <p>{{ item.summary }}</p>
              <div class="item-facts">
                <span>
                  <i class="bi bi-calendar3"></i>
                  {{ item.period }}
                </span>
                <span>
                  <i class="bi bi-shop"></i>
                  {{ item.storeScope }}
                </span>
              </div>
            </div>
            <RouterLink class="text-action" :to="item.ctaTo">
              {{ item.ctaText }}
              <i class="bi bi-arrow-right"></i>
            </RouterLink>
          </article>
        </div>
      </section>
    </main>
  </section>
</template>

<style scoped>
.news-page {
  min-height: 100vh;
  background: #f7f3ee;
  color: #25313f;
}

.news-container {
  width: min(1120px, calc(100% - 40px));
  margin: 0 auto;
}

.news-header {
  background-position: center;
  background-size: cover;
}

.news-header__shade {
  min-height: 500px;
  background: linear-gradient(180deg, rgba(18, 22, 25, 0.18), rgba(18, 22, 25, 0.76));
  color: #fff;
  padding: 150px 0 72px;
}

.breadcrumb-line {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 48px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 14px;
  font-weight: 800;
}

.breadcrumb-line a {
  color: inherit;
  text-decoration: none;
}

.section-label {
  margin: 0 0 10px;
  color: #a96530;
  font-size: 13px;
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.news-header .section-label {
  color: #f1c8a0;
}

.news-header h1 {
  margin: 0;
  font-size: clamp(46px, 6vw, 78px);
  font-weight: 900;
  line-height: 1.04;
  letter-spacing: 0;
}

.header-copy {
  max-width: 650px;
  margin: 22px 0 0;
  color: rgba(255, 255, 255, 0.86);
  font-size: 18px;
  line-height: 1.8;
}

.news-content {
  padding: 58px 0 96px;
}

.section-heading,
.index-toolbar {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: end;
  margin-bottom: 22px;
}

.section-heading h2,
.index-toolbar h2 {
  margin: 0;
  color: #25313f;
  font-size: 34px;
  font-weight: 900;
  letter-spacing: 0;
}

.featured-article {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(380px, 0.95fr);
  overflow: hidden;
  border: 1px solid #d8cbbd;
  border-radius: 6px;
  background: #fff;
}

.featured-article > img {
  width: 100%;
  height: 100%;
  min-height: 420px;
  object-fit: cover;
}

.featured-copy {
  padding: 42px;
}

.news-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  color: #8c552e;
  font-size: 14px;
  font-weight: 900;
}

.news-meta span {
  border: 1px solid #d8cbbd;
  border-radius: 999px;
  padding: 5px 11px;
}

.featured-copy h3 {
  margin: 18px 0 14px;
  color: #25313f;
  font-size: 32px;
  font-weight: 900;
  line-height: 1.35;
  letter-spacing: 0;
}

.featured-copy p,
.news-item__body p {
  color: #657284;
  line-height: 1.8;
}

.event-facts {
  display: grid;
  gap: 12px;
  margin: 28px 0;
}

.event-facts div {
  display: grid;
  grid-template-columns: 62px minmax(0, 1fr);
  gap: 14px;
  border-top: 1px solid #ece3da;
  padding-top: 12px;
}

.event-facts dt {
  color: #8a7d70;
  font-weight: 900;
}

.event-facts dd {
  margin: 0;
  color: #25313f;
  font-weight: 800;
}

.text-action {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  color: #8c552e;
  font-weight: 900;
  text-decoration: none;
  white-space: nowrap;
}

.text-action.dark {
  min-height: 46px;
  border-radius: 6px;
  background: #25313f;
  color: #fff;
  padding: 0 18px;
}

.news-index {
  margin-top: 64px;
}

.result-count {
  color: #657284;
  font-weight: 900;
}

.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  border-bottom: 1px solid #d8cbbd;
  padding-bottom: 16px;
}

.category-tab {
  border: 1px solid #d8cbbd;
  border-radius: 999px;
  background: transparent;
  color: #685c51;
  padding: 8px 16px;
  font-weight: 900;
}

.category-tab.active {
  border-color: #8c552e;
  background: #8c552e;
  color: #fff;
}

.news-list {
  display: grid;
  margin-top: 10px;
}

.news-item {
  display: grid;
  grid-template-columns: 120px 188px minmax(0, 1fr) auto;
  gap: 24px;
  align-items: center;
  border-bottom: 1px solid #d8cbbd;
  padding: 24px 0;
}

.news-item.important {
  background: linear-gradient(90deg, rgba(169, 101, 48, 0.08), rgba(169, 101, 48, 0));
}

.date-block {
  display: grid;
  gap: 7px;
}

.date-block time {
  color: #25313f;
  font-size: 18px;
  font-weight: 900;
}

.date-block span {
  width: fit-content;
  border: 1px solid #d8cbbd;
  border-radius: 999px;
  color: #8c552e;
  padding: 4px 10px;
  font-size: 13px;
  font-weight: 900;
}

.news-item img {
  width: 188px;
  aspect-ratio: 4 / 3;
  border-radius: 4px;
  object-fit: cover;
}

.news-item h3 {
  margin: 0 0 8px;
  color: #25313f;
  font-size: 22px;
  font-weight: 900;
  line-height: 1.4;
  letter-spacing: 0;
}

.news-item__body p {
  margin: 0;
}

.item-facts {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  margin-top: 12px;
  color: #70665d;
  font-size: 14px;
  font-weight: 800;
}

.item-facts span {
  display: inline-flex;
  gap: 6px;
  align-items: center;
}

@media (max-width: 991px) {
  .news-container {
    width: min(100% - 28px, 720px);
  }

  .news-header__shade {
    min-height: 470px;
    padding: 128px 0 56px;
  }

  .breadcrumb-line {
    margin-bottom: 34px;
  }

  .section-heading,
  .index-toolbar {
    display: grid;
    align-items: start;
  }

  .featured-article,
  .news-item {
    grid-template-columns: 1fr;
  }

  .featured-article > img {
    min-height: 260px;
  }

  .featured-copy {
    padding: 28px;
  }

  .news-item {
    gap: 14px;
  }

  .news-item img {
    width: 100%;
    max-height: 260px;
  }
}

@media (max-width: 640px) {
  .news-header__shade {
    min-height: 450px;
  }

  .header-copy {
    font-size: 16px;
  }

  .section-heading h2,
  .index-toolbar h2 {
    font-size: 28px;
  }

  .featured-copy h3 {
    font-size: 26px;
  }

  .event-facts div {
    grid-template-columns: 1fr;
    gap: 4px;
  }
}
</style>
