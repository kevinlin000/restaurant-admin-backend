<script setup>
import { computed, onMounted, ref } from "vue";
import { faqApi } from "@/api/faq";

const faqs = ref([]);
const loading = ref(false);
const errorMessage = ref("");
const selectedCategory = ref("ALL");
const searchText = ref("");
const openedId = ref(null);

const categories = [
  { value: "ALL", label: "全部", note: "所有規則" },
  { value: "RESERVATION", label: "訂位", note: "時段、保留、修改" },
  { value: "DEPOSIT", label: "訂金", note: "付款、退款、取消" },
  { value: "ORDER", label: "點餐", note: "內用、外帶、菜單" },
  { value: "PAYMENT", label: "付款", note: "信用卡、Line Pay、發票" },
  { value: "STORE", label: "門市", note: "營業、交通、狀態" },
  { value: "MEMBER", label: "會員", note: "登入、紀錄、資料" },
  { value: "SERVICE", label: "服務", note: "過敏、包廂、特殊需求" },
];

const featuredFaqs = computed(() => faqs.value.filter((faq) => faq.isFeatured).slice(0, 4));

const filteredFaqs = computed(() => {
  const keyword = searchText.value.trim().toLowerCase();
  return faqs.value.filter((faq) => {
    const categoryMatched =
      selectedCategory.value === "ALL" || faq.category === selectedCategory.value;
    const keywordMatched =
      !keyword ||
      `${faq.question} ${faq.answer} ${faq.keywords || ""}`.toLowerCase().includes(keyword);
    return categoryMatched && keywordMatched;
  });
});

const categoryCounts = computed(() =>
  categories.map((category) => ({
    ...category,
    count:
      category.value === "ALL"
        ? faqs.value.length
        : faqs.value.filter((faq) => faq.category === category.value).length,
  })),
);

const loadFaqs = async () => {
  loading.value = true;
  errorMessage.value = "";
  try {
    faqs.value = await faqApi.getPublishedFaqs();
    openedId.value = faqs.value[0]?.faqId || null;
  } catch (error) {
    faqs.value = [];
    errorMessage.value = "目前無法載入常見問題，請稍後再試。";
  } finally {
    loading.value = false;
  }
};

const toggleFaq = (faqId) => {
  openedId.value = openedId.value === faqId ? null : faqId;
};

onMounted(loadFaqs);
</script>

<template>
  <main class="faq-page">
    <section class="faq-hero">
      <div class="faq-hero-inner">
        <span class="eyebrow">Guest Support</span>
        <h1>用餐前，把規則查清楚。</h1>
        <p>
          訂位、訂金、取消、點餐與門市資訊整理在同一頁。搜尋問題或選擇分類，快速找到正式規則。
        </p>

        <label class="faq-search" for="faq-search-input">
          <i class="bi bi-search"></i>
          <input
            id="faq-search-input"
            v-model="searchText"
            type="search"
            placeholder="搜尋：訂金、取消、外帶、過敏..."
          />
        </label>
      </div>
    </section>

    <section class="faq-shell">
      <aside class="faq-side">
        <span class="side-label">分類</span>
        <button
          v-for="category in categoryCounts"
          :key="category.value"
          type="button"
          :class="{ active: selectedCategory === category.value }"
          @click="selectedCategory = category.value"
        >
          <strong>{{ category.label }}</strong>
          <small>{{ category.note }}</small>
          <em>{{ category.count }}</em>
        </button>
      </aside>

      <section class="faq-content">
        <div class="faq-summary">
          <div>
            <span>Knowledge Base</span>
            <h2>常見問答</h2>
          </div>
          <p>目前收錄 {{ faqs.length }} 則規則，客服窗也會使用同一份 FAQ 知識庫回答。</p>
        </div>

        <div v-if="featuredFaqs.length" class="featured-strip">
          <article v-for="faq in featuredFaqs" :key="faq.faqId">
            <span>{{ faq.categoryLabel }}</span>
            <strong>{{ faq.question }}</strong>
          </article>
        </div>

        <div v-if="loading" class="faq-state">載入 FAQ 中...</div>
        <div v-else-if="errorMessage" class="faq-state danger">{{ errorMessage }}</div>
        <div v-else-if="!filteredFaqs.length" class="faq-state">
          沒有找到相符問題，請換個關鍵字或改選其他分類。
        </div>

        <div v-else class="faq-list">
          <article
            v-for="faq in filteredFaqs"
            :key="faq.faqId"
            class="faq-item"
            :class="{ open: openedId === faq.faqId }"
          >
            <button type="button" @click="toggleFaq(faq.faqId)">
              <span>{{ faq.categoryLabel }}</span>
              <strong>{{ faq.question }}</strong>
              <i class="bi bi-chevron-down"></i>
            </button>
            <p v-show="openedId === faq.faqId">{{ faq.answer }}</p>
          </article>
        </div>
      </section>

      <aside class="policy-panel">
        <span>Service Policy</span>
        <h2>展示時可以講的亮點</h2>
        <ul>
          <li>前台 FAQ 與客服窗共用同一份後台知識庫。</li>
          <li>只回答已發布內容，避免草稿規則被客人看到。</li>
          <li>搜尋以問題、答案、關鍵字加權排序，比純文字列表更像真實客服。</li>
        </ul>
      </aside>
    </section>
  </main>
</template>

<style scoped>
.faq-page {
  min-height: 100vh;
  background: #fffaf4;
  color: #2f2924;
}

.faq-hero {
  background:
    linear-gradient(90deg, rgba(22, 15, 11, 0.88), rgba(22, 15, 11, 0.58)),
    url("@/assets/images/japanese-tea.jpg") center/cover;
  color: #fff;
  padding: 190px 0 92px;
}

.faq-hero-inner {
  width: min(1120px, calc(100% - 48px));
  margin: 0 auto;
}

.eyebrow,
.side-label,
.faq-summary span,
.featured-strip span,
.policy-panel span {
  color: #dca874;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.faq-hero h1 {
  max-width: 780px;
  margin: 16px 0;
  color: #fff;
  font-family: "Noto Serif TC", serif;
  font-size: clamp(42px, 6vw, 76px);
  font-weight: 700;
  line-height: 1.18;
}

.faq-hero p {
  max-width: 620px;
  margin: 0 0 30px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 18px;
  line-height: 1.8;
}

.faq-search {
  display: grid;
  grid-template-columns: 54px 1fr;
  width: min(720px, 100%);
  min-height: 58px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 250, 244, 0.98);
  color: #2f2924;
}

.faq-search i {
  display: grid;
  place-items: center;
  color: #9a5d35;
}

.faq-search input {
  min-width: 0;
  border: 0;
  background: transparent;
  color: #2f2924;
  outline: none;
}

.faq-shell {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr) 300px;
  gap: 28px;
  width: min(1180px, calc(100% - 48px));
  margin: -36px auto 96px;
  align-items: start;
}

.faq-side,
.faq-content,
.policy-panel {
  border: 1px solid #ded4ca;
  background: #fff;
}

.faq-side {
  position: sticky;
  top: 126px;
  display: grid;
  gap: 8px;
  padding: 18px;
}

.faq-side button {
  position: relative;
  display: grid;
  gap: 2px;
  border: 1px solid transparent;
  background: #fffaf4;
  padding: 14px 42px 14px 14px;
  text-align: left;
}

.faq-side button.active,
.faq-side button:hover {
  border-color: #dca874;
  background: #fff4e7;
}

.faq-side strong {
  color: #2f2924;
}

.faq-side small {
  color: #74685f;
}

.faq-side em {
  position: absolute;
  right: 14px;
  top: 16px;
  color: #9a5d35;
  font-style: normal;
  font-weight: 800;
}

.faq-content {
  padding: 28px;
}

.faq-summary {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  border-bottom: 1px solid #e4d9ce;
  padding-bottom: 24px;
}

.faq-summary h2,
.policy-panel h2 {
  margin: 6px 0 0;
  color: #2f2924;
  font-family: "Noto Serif TC", serif;
  font-size: 32px;
  font-weight: 700;
}

.faq-summary p {
  max-width: 360px;
  margin: 0;
  color: #675d54;
  line-height: 1.7;
}

.featured-strip {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 24px 0;
}

.featured-strip article {
  border: 1px solid #eaded2;
  background: #fffaf4;
  padding: 16px;
}

.featured-strip strong {
  display: block;
  margin-top: 6px;
  color: #2f2924;
  line-height: 1.55;
}

.faq-list {
  display: grid;
  gap: 12px;
}

.faq-item {
  border: 1px solid #e1d6cb;
}

.faq-item button {
  display: grid;
  grid-template-columns: 130px 1fr 24px;
  gap: 16px;
  align-items: center;
  width: 100%;
  border: 0;
  background: #fff;
  padding: 20px;
  text-align: left;
}

.faq-item button span {
  color: #9a5d35;
  font-size: 13px;
  font-weight: 800;
}

.faq-item button strong {
  color: #2f2924;
  font-size: 18px;
  line-height: 1.5;
}

.faq-item button i {
  color: #9a5d35;
  transition: transform 180ms ease;
}

.faq-item.open button i {
  transform: rotate(180deg);
}

.faq-item p {
  margin: 0;
  border-top: 1px solid #eaded2;
  background: #fffaf4;
  color: #615850;
  padding: 20px;
  line-height: 1.9;
}

.faq-state {
  border: 1px dashed #d8cbbf;
  background: #fffaf4;
  color: #675d54;
  padding: 22px;
}

.faq-state.danger {
  color: #9b2f1f;
}

.policy-panel {
  position: sticky;
  top: 126px;
  padding: 24px;
}

.policy-panel ul {
  display: grid;
  gap: 14px;
  margin: 22px 0 0;
  padding: 0;
  list-style: none;
}

.policy-panel li {
  border-top: 1px solid #eaded2;
  color: #615850;
  padding-top: 14px;
  line-height: 1.7;
}

@media (max-width: 1080px) {
  .faq-shell {
    grid-template-columns: 1fr;
  }

  .faq-side,
  .policy-panel {
    position: static;
  }

  .faq-side {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .faq-hero {
    padding: 150px 0 72px;
  }

  .faq-shell,
  .faq-hero-inner {
    width: min(100% - 32px, 1180px);
  }

  .faq-side,
  .featured-strip {
    grid-template-columns: 1fr;
  }

  .faq-summary {
    display: grid;
  }

  .faq-item button {
    grid-template-columns: 1fr 24px;
  }

  .faq-item button span {
    grid-column: 1 / -1;
  }
}
</style>
