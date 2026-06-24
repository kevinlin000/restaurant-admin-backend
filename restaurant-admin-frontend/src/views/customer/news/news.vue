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
    title: "夏旬和食祭｜海味、炙燒與清酒佐餐同步登場",
    summary:
      "以鮭魚、干貝、季節野菜與吟釀酒香搭出夏季限定菜色，內用套餐可加購指定佐餐飲品。",
    body:
      "本季菜單以清爽海味與桌邊炙燒為主軸，保留職人料理的節奏，也讓聚餐更有儀式感。活動期間內用指定套餐，可用優惠價加購日本清酒、朝日啤酒或無酒精可爾必思。",
    from: "2026.06.24",
    to: "2026.08.31",
    storeScope: "全門市適用",
    image: seasonalImage,
    status: "進行中",
    highlight: true,
    ctaText: "立即訂位",
    ctaTo: "/reservation",
  },
  {
    id: 2,
    category: "opening",
    categoryLabel: "展店",
    title: "台中勤美店試營運公告｜午間席次優先開放",
    summary:
      "新門市試營運期間採分段開放訂位，午餐、下午茶與晚餐席次將依現場準備狀況逐步增加。",
    body:
      "台中勤美店以城市聚餐與包廂宴席為主要情境，試營運期間每日席次有限。為維持服務品質，線上訂位會分階段開放，建議提前選擇候補時段或鄰近門市。",
    from: "2026.07.05",
    to: "2026.07.31",
    storeScope: "台中勤美店",
    image: openingImage,
    status: "即將開始",
    ctaText: "查看門市",
    ctaTo: "/store",
  },
  {
    id: 3,
    category: "notice",
    categoryLabel: "公告",
    title: "重要提醒｜請透過官方網站、APP 或門市電話完成訂位",
    summary:
      "敘日未授權第三方代訂平台收取訂金或轉售席次，請勿購買來路不明的訂位。",
    body:
      "近期餐飲訂位詐騙與轉售案件增加。敘日所有訂位資訊皆以官方網站、會員 APP 與門市電話公告為準，不會要求顧客透過私人帳號匯款，也不會以非官方 LINE 帳號確認付款。",
    from: "2026.06.20",
    to: "長期公告",
    storeScope: "全門市適用",
    image: noticeImage,
    status: "重要",
    ctaText: "查看門市電話",
    ctaTo: "/store",
  },
  {
    id: 4,
    category: "member",
    categoryLabel: "會員",
    title: "敘日會員週｜平日午餐點數雙倍累積",
    summary:
      "會員平日 11:30 至 14:00 內用，單筆滿額享點數雙倍累積，可與生日禮擇優使用。",
    body:
      "希望把平日聚餐做得更輕鬆，本月會員週將午餐時段納入點數加倍。活動適用會員本人消費，點數將於結帳後自動入帳，企業包場與外帶訂單不適用。",
    from: "2026.07.01",
    to: "2026.07.14",
    storeScope: "全門市適用",
    image: memberImage,
    status: "即將開始",
    ctaText: "會員登入",
    ctaTo: "/login",
  },
  {
    id: 5,
    category: "event",
    categoryLabel: "活動",
    title: "雙人餐酒夜｜週四晚餐限定席",
    summary:
      "精選雙人套餐搭配指定飲品，適合慶生、約會與下班後的小型聚餐。",
    body:
      "週四晚餐限定推出雙人餐酒夜，餐點包含前菜、刺身、炙燒主菜、鍋物與甜點。部分門市提供吧檯席，適合想安靜用餐或慶祝紀念日的顧客。",
    from: "2026.06.27",
    to: "2026.08.28",
    storeScope: "台北信義店、新竹巨城店、台中勤美店",
    image: diningImage,
    status: "進行中",
    ctaText: "預約晚餐",
    ctaTo: "/reservation",
  },
  {
    id: 6,
    category: "notice",
    categoryLabel: "公告",
    title: "外帶自取包裝調整｜鍋物與生食餐點分裝升級",
    summary:
      "為維持餐點狀態，外帶自取餐盒將依品項調整為冷熱分裝，部分套餐備餐時間同步延長。",
    body:
      "外帶自取將針對鍋物、生食與甜點調整分裝方式，減少溫度互相影響。尖峰時段建議提前 40 分鐘下單，門市會依現場訂單狀況確認最早取餐時間。",
    from: "2026.06.18",
    to: "長期公告",
    storeScope: "供應外帶門市",
    image: takeoutImage,
    status: "公告",
    ctaText: "前往點餐",
    ctaTo: "/order",
  },
];

const activeCategory = ref("all");
const selectedId = ref(newsItems[0].id);

const featuredNews = computed(() => newsItems.find((item) => item.highlight) || newsItems[0]);

const filteredNews = computed(() => {
  if (activeCategory.value === "all") {
    return newsItems;
  }

  return newsItems.filter((item) => item.category === activeCategory.value);
});

const selectedNews = computed(() => {
  const current = filteredNews.value.find((item) => item.id === selectedId.value);
  return current || filteredNews.value[0] || featuredNews.value;
});

const setCategory = (category) => {
  activeCategory.value = category;
  selectedId.value = filteredNews.value[0]?.id || featuredNews.value.id;
};

const selectNews = (id) => {
  selectedId.value = id;
};

onMounted(() => {
  requestAnimationFrame(() => {
    window.scrollTo({ top: 0, left: 0, behavior: "instant" });
  });
});
</script>

<template>
  <section class="news-page">
    <section class="news-hero" :style="{ backgroundImage: `url(${heroImage})` }">
      <div class="news-hero__overlay">
        <div class="news-container news-hero__content">
          <span class="eyebrow">News & Notice</span>
          <h1>最新消息</h1>
          <p>
            掌握敘日最新菜單、門市營運、會員禮遇與重要公告。每一則消息都標示適用期間與門市，方便你安排下一次聚餐。
          </p>
          <div class="hero-actions">
            <RouterLink class="primary-action" to="/reservation">預約席次</RouterLink>
            <RouterLink class="secondary-action" to="/store">查看門市</RouterLink>
          </div>
        </div>
      </div>
    </section>

    <section class="news-container news-feature">
      <div class="feature-copy">
        <span class="eyebrow">Featured</span>
        <h2>{{ featuredNews.title }}</h2>
        <p>{{ featuredNews.summary }}</p>
      </div>
      <div class="feature-meta">
        <span>{{ featuredNews.from }} - {{ featuredNews.to }}</span>
        <strong>{{ featuredNews.storeScope }}</strong>
      </div>
    </section>

    <section class="news-container news-board" aria-label="最新消息列表">
      <div class="news-board__header">
        <div>
          <span class="eyebrow">Browse</span>
          <h2>消息一覽</h2>
        </div>
        <div class="category-tabs" aria-label="消息分類">
          <button
            v-for="category in categories"
            :key="category.value"
            type="button"
            :class="['category-tab', { active: activeCategory === category.value }]"
            @click="setCategory(category.value)"
          >
            {{ category.label }}
          </button>
        </div>
      </div>

      <div class="news-layout">
        <div class="news-list">
          <button
            v-for="item in filteredNews"
            :key="item.id"
            type="button"
            :class="['news-row', { active: selectedNews.id === item.id }]"
            @click="selectNews(item.id)"
          >
            <span class="news-date">{{ item.from }}</span>
            <span class="news-row__main">
              <span class="news-row__topline">
                <span class="news-category">{{ item.categoryLabel }}</span>
                <span class="news-status">{{ item.status }}</span>
              </span>
              <strong>{{ item.title }}</strong>
              <span>{{ item.summary }}</span>
            </span>
            <i class="bi bi-arrow-right-short"></i>
          </button>
        </div>

        <article class="news-detail">
          <img :src="selectedNews.image" :alt="selectedNews.title" />
          <div class="news-detail__body">
            <div class="detail-meta">
              <span>{{ selectedNews.categoryLabel }}</span>
              <span>{{ selectedNews.from }} - {{ selectedNews.to }}</span>
            </div>
            <h3>{{ selectedNews.title }}</h3>
            <p>{{ selectedNews.body }}</p>
            <dl class="detail-list">
              <div>
                <dt>適用門市</dt>
                <dd>{{ selectedNews.storeScope }}</dd>
              </div>
              <div>
                <dt>消息狀態</dt>
                <dd>{{ selectedNews.status }}</dd>
              </div>
            </dl>
            <RouterLink class="detail-action" :to="selectedNews.ctaTo">
              {{ selectedNews.ctaText }}
              <i class="bi bi-arrow-right"></i>
            </RouterLink>
          </div>
        </article>
      </div>
    </section>
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

.news-hero {
  min-height: 560px;
  background-position: center;
  background-size: cover;
}

.news-hero__overlay {
  display: flex;
  min-height: 560px;
  align-items: end;
  background: linear-gradient(180deg, rgba(20, 24, 28, 0.26), rgba(20, 24, 28, 0.72));
  padding: 160px 0 72px;
}

.news-hero__content {
  color: #fff;
}

.eyebrow {
  display: inline-block;
  margin-bottom: 12px;
  color: #a96530;
  font-size: 13px;
  font-weight: 900;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.news-hero .eyebrow {
  color: #f3cba6;
}

.news-hero h1 {
  margin: 0;
  font-size: clamp(44px, 7vw, 82px);
  font-weight: 900;
  line-height: 1.05;
  letter-spacing: 0;
}

.news-hero p {
  max-width: 620px;
  margin: 24px 0 0;
  color: rgba(255, 255, 255, 0.86);
  font-size: 18px;
  line-height: 1.85;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 32px;
}

.primary-action,
.secondary-action,
.detail-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 46px;
  border-radius: 8px;
  padding: 0 18px;
  font-weight: 900;
  text-decoration: none;
}

.primary-action {
  background: #a96530;
  color: #fff;
}

.secondary-action {
  border: 1px solid rgba(255, 255, 255, 0.52);
  color: #fff;
}

.news-feature {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 32px;
  border-bottom: 1px solid #dfd3c7;
  padding: 42px 0;
}

.feature-copy h2,
.news-board__header h2 {
  margin: 0;
  color: #263445;
  font-size: 34px;
  font-weight: 900;
  letter-spacing: 0;
}

.feature-copy p {
  max-width: 720px;
  margin: 14px 0 0;
  color: #697386;
  font-size: 17px;
  line-height: 1.8;
}

.feature-meta {
  align-self: end;
  border-left: 3px solid #a96530;
  padding-left: 18px;
}

.feature-meta span,
.feature-meta strong {
  display: block;
}

.feature-meta span {
  color: #697386;
  font-size: 14px;
  font-weight: 800;
}

.feature-meta strong {
  margin-top: 8px;
  color: #263445;
  font-size: 18px;
}

.news-board {
  padding: 54px 0 96px;
}

.news-board__header {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
}

.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-tab {
  border: 1px solid #d6c7b9;
  border-radius: 999px;
  background: #fff;
  color: #685c51;
  padding: 9px 16px;
  font-weight: 900;
}

.category-tab.active {
  border-color: #a96530;
  background: #a96530;
  color: #fff;
}

.news-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 28px;
  align-items: start;
}

.news-list {
  display: grid;
  gap: 10px;
}

.news-row {
  display: grid;
  grid-template-columns: 104px minmax(0, 1fr) 24px;
  gap: 18px;
  align-items: center;
  width: 100%;
  border: 1px solid #e1d6ca;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.78);
  padding: 18px;
  color: inherit;
  text-align: left;
  transition: border-color 0.2s, background 0.2s, transform 0.2s;
}

.news-row:hover,
.news-row.active {
  border-color: #a96530;
  background: #fff;
  transform: translateY(-1px);
}

.news-date {
  color: #a96530;
  font-weight: 900;
}

.news-row__main {
  display: grid;
  gap: 7px;
  min-width: 0;
}

.news-row__topline {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.news-category,
.news-status {
  border-radius: 999px;
  padding: 4px 9px;
  font-size: 12px;
  font-weight: 900;
}

.news-category {
  background: #f4e6d8;
  color: #8c552e;
}

.news-status {
  background: #eef2f5;
  color: #536271;
}

.news-row strong {
  color: #263445;
  font-size: 18px;
  line-height: 1.35;
}

.news-row__main > span:last-child {
  color: #697386;
  line-height: 1.6;
}

.news-row i {
  color: #a96530;
  font-size: 24px;
}

.news-detail {
  position: sticky;
  top: 112px;
  overflow: hidden;
  border: 1px solid #d8ccbf;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 18px 42px rgba(52, 64, 81, 0.1);
}

.news-detail img {
  width: 100%;
  height: 240px;
  object-fit: cover;
}

.news-detail__body {
  padding: 26px;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: #8c552e;
  font-size: 13px;
  font-weight: 900;
}

.news-detail h3 {
  margin: 14px 0 12px;
  color: #263445;
  font-size: 26px;
  font-weight: 900;
  line-height: 1.3;
}

.news-detail p {
  margin: 0;
  color: #697386;
  line-height: 1.85;
}

.detail-list {
  display: grid;
  gap: 12px;
  margin: 22px 0;
}

.detail-list div {
  display: grid;
  grid-template-columns: 86px minmax(0, 1fr);
  gap: 12px;
  border-top: 1px solid #ede4da;
  padding-top: 12px;
}

.detail-list dt {
  color: #7a6f65;
  font-weight: 900;
}

.detail-list dd {
  margin: 0;
  color: #25313f;
  font-weight: 800;
}

.detail-action {
  width: 100%;
  background: #263445;
  color: #fff;
}

@media (max-width: 991px) {
  .news-container {
    width: min(100% - 28px, 720px);
  }

  .news-hero,
  .news-hero__overlay {
    min-height: 520px;
  }

  .news-feature,
  .news-layout,
  .news-board__header {
    grid-template-columns: 1fr;
  }

  .news-board__header {
    display: grid;
    align-items: start;
  }

  .news-detail {
    position: static;
  }
}

@media (max-width: 640px) {
  .news-hero__overlay {
    align-items: center;
    padding: 128px 0 56px;
  }

  .news-hero p {
    font-size: 16px;
  }

  .news-feature {
    padding: 30px 0;
  }

  .feature-copy h2,
  .news-board__header h2 {
    font-size: 28px;
  }

  .news-row {
    grid-template-columns: 1fr 24px;
  }

  .news-date {
    grid-column: 1 / -1;
  }

  .detail-list div {
    grid-template-columns: 1fr;
    gap: 4px;
  }
}
</style>
