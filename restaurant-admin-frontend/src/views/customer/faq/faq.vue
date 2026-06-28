<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { faqApi } from "@/api/faq";

const INITIAL_VISIBLE_COUNT = 8;

const faqs = ref([]);
const loading = ref(false);
const errorMessage = ref("");
const selectedCategory = ref("ALL");
const searchText = ref("");
const openedId = ref(null);
const visibleCount = ref(INITIAL_VISIBLE_COUNT);

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

const policyNotes = [
  { title: "訂位開放", text: "線上可預約未來 30 天內餐期，熱門時段建議提前安排。" },
  { title: "座位保留", text: "請依訂位時間抵達；若可能晚到，請先聯繫門市確認座位保留。" },
  { title: "訂金確認", text: "特殊餐期、包廂或大人數訂位可能需於期限內完成付款。" },
  { title: "現場為準", text: "營業異動、候位與特殊需求，仍以當日門市回覆為準。" },
];

const activeCategory = computed(() =>
  categoryCounts.value.find((category) => category.value === selectedCategory.value),
);

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

const visibleFaqs = computed(() => filteredFaqs.value.slice(0, visibleCount.value));

const remainingFaqCount = computed(() =>
  Math.max(filteredFaqs.value.length - visibleFaqs.value.length, 0),
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

const selectCategory = (category) => {
  selectedCategory.value = category;
};

const toggleFaq = (faqId) => {
  openedId.value = openedId.value === faqId ? null : faqId;
};

const showMoreFaqs = () => {
  visibleCount.value += 8;
};

watch([selectedCategory, searchText], () => {
  visibleCount.value = INITIAL_VISIBLE_COUNT;
});

onMounted(loadFaqs);
</script>

<template>
  <main class="faq-page">
    <section class="faq-hero">
      <div class="faq-hero-inner">
        <span class="eyebrow">Service Guide</span>
        <h1>訂位與用餐規則。</h1>
        <p>
          把用餐前會遇到的時段、訂金、取消、付款與門市資訊整理成正式規則。查詢前先確認細節，抵達後把時間留給餐桌。
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
        <span class="side-label">規則索引</span>
        <button
          v-for="category in categoryCounts"
          :key="category.value"
          type="button"
          :class="{ active: selectedCategory === category.value }"
          @click="selectCategory(category.value)"
        >
          <strong>{{ category.label }}</strong>
          <small>{{ category.note }}</small>
          <em>{{ category.count }}</em>
        </button>
      </aside>

      <section class="faq-content">
        <div class="faq-summary">
          <div>
            <span>Guest Policy</span>
            <h2>{{ activeCategory?.label || "全部" }}</h2>
          </div>
          <p>
            目前收錄 {{ faqs.length }} 則已發布規則。依分類瀏覽，或直接搜尋訂金、外帶、停車、過敏等關鍵字。
          </p>
        </div>

        <div class="service-notes" aria-label="用餐須知摘要">
          <article v-for="note in policyNotes" :key="note.title">
            <span>{{ note.title }}</span>
            <p>{{ note.text }}</p>
          </article>
        </div>

        <div v-if="loading" class="faq-state">載入 FAQ 中...</div>
        <div v-else-if="errorMessage" class="faq-state danger">{{ errorMessage }}</div>
        <div v-else-if="!filteredFaqs.length" class="faq-state">
          沒有找到相符問題，請換個關鍵字或改選其他分類。
        </div>

        <div v-else class="faq-list">
          <div class="faq-toolbar">
            <span>顯示 {{ visibleFaqs.length }} / {{ filteredFaqs.length }} 則</span>
            <small v-if="searchText.trim()">搜尋「{{ searchText.trim() }}」</small>
          </div>

          <article
            v-for="faq in visibleFaqs"
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

          <button
            v-if="remainingFaqCount"
            type="button"
            class="faq-more"
            @click="showMoreFaqs"
          >
            再顯示 {{ Math.min(remainingFaqCount, 8) }} 則
          </button>
        </div>
      </section>
    </section>
  </main>
</template>

<style scoped>
.faq-page {
  min-height: 100vh;
  background: #f8f2ea;
  color: #2f2924;
}

.faq-hero {
  background:
    linear-gradient(90deg, rgba(20, 13, 9, 0.9), rgba(20, 13, 9, 0.66)),
    url("@/assets/images/japanese-tea.jpg") center/cover;
  color: #fff;
  padding: 184px 0 86px;
}

.faq-hero-inner {
  width: min(1120px, calc(100% - 48px));
  margin: 0 auto;
}

.eyebrow,
.side-label,
.faq-summary span {
  color: #b7774d;
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
  border: 1px solid rgba(255, 255, 255, 0.28);
  background: rgba(255, 253, 248, 0.98);
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
  grid-template-columns: 252px minmax(0, 1fr);
  gap: 24px;
  width: min(1120px, calc(100% - 48px));
  margin: -36px auto 96px;
  align-items: start;
}

.faq-side,
.faq-content {
  border: 1px solid #dfd3c7;
  background: #fffdf9;
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
  background: #fbf6ef;
  padding: 14px 42px 14px 14px;
  text-align: left;
}

.faq-side button.active,
.faq-side button:hover {
  border-color: #c98e63;
  background: #fff3e5;
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
  padding: 30px 30px 34px;
}

.faq-summary {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  border-bottom: 1px solid #e1d6cb;
  padding-bottom: 26px;
}

.faq-summary h2 {
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

.service-notes {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  margin-top: 24px;
  border-top: 1px solid #e1d6cb;
  border-bottom: 1px solid #e1d6cb;
}

.service-notes article {
  min-width: 0;
  padding: 16px 18px;
}

.service-notes article + article {
  border-left: 1px solid #e8ded4;
}

.service-notes span {
  display: block;
  color: #9a5d35;
  font-size: 12px;
  font-weight: 800;
}

.service-notes p {
  margin: 8px 0 0;
  color: #615850;
  font-size: 14px;
  line-height: 1.7;
}

.faq-list {
  display: grid;
  gap: 12px;
  margin-top: 26px;
}

.faq-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  color: #8a7768;
  font-size: 13px;
}

.faq-toolbar span {
  font-weight: 800;
}

.faq-toolbar small {
  min-width: 0;
  color: #9a5d35;
}

.faq-item {
  border: 1px solid #dfd4c9;
  background: #fff;
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
  background: #fbf6ef;
  color: #615850;
  padding: 20px;
  line-height: 1.9;
}

.faq-more {
  min-height: 48px;
  border: 1px solid #c98e63;
  background: #fff;
  color: #8a512d;
  font-weight: 800;
}

.faq-more:hover {
  background: #fff3e5;
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

@media (max-width: 1080px) {
  .faq-shell {
    grid-template-columns: 1fr;
  }

  .faq-side {
    position: static;
  }

  .faq-side {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .service-notes {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .service-notes article:nth-child(odd) {
    border-left: 0;
  }

  .service-notes article:nth-child(n + 3) {
    border-top: 1px solid #e8ded4;
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

  .faq-shell {
    gap: 14px;
    margin-bottom: 72px;
  }

  .faq-side {
    display: flex;
    gap: 8px;
    overflow-x: auto;
    width: calc(100% + 32px);
    margin-inline: -16px;
    padding: 10px 16px;
    scroll-snap-type: x proximity;
    scrollbar-width: none;
  }

  .faq-side::-webkit-scrollbar {
    display: none;
  }

  .faq-side .side-label {
    flex: 0 0 auto;
    align-self: center;
    margin: 0 2px 0 0;
    white-space: nowrap;
  }

  .faq-side button {
    flex: 0 0 142px;
    min-height: 58px;
    padding: 10px 34px 10px 12px;
    scroll-snap-align: start;
  }

  .faq-side small {
    display: none;
  }

  .faq-side em {
    top: 12px;
    right: 12px;
  }

  .faq-content {
    padding: 24px 18px 28px;
  }

  .faq-summary {
    display: grid;
  }

  .service-notes {
    grid-template-columns: 1fr;
  }

  .service-notes article + article,
  .service-notes article:nth-child(n + 3) {
    border-top: 1px solid #e8ded4;
    border-left: 0;
  }

  .faq-item button {
    grid-template-columns: 1fr 24px;
  }

  .faq-item button span {
    grid-column: 1 / -1;
  }

  .faq-toolbar {
    display: grid;
  }
}
</style>
