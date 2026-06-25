<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { faqApi } from "@/api/faq";

const router = useRouter();

const loading = ref(false);
const errorMessage = ref("");
const analytics = ref({
  summary: {
    windowLabel: "近 30 天",
    totalSearches: 0,
    missedSearches: 0,
    uniqueQueries: 0,
    hitRate: 0,
  },
  popularQueries: [],
  missedQueries: [],
  recentLogs: [],
});

const summary = computed(() => analytics.value.summary || {});
const missedQueries = computed(() => analytics.value.missedQueries || []);
const popularQueries = computed(() => analytics.value.popularQueries || []);
const recentLogs = computed(() => analytics.value.recentLogs || []);

const loadAnalytics = async () => {
  loading.value = true;
  errorMessage.value = "";
  try {
    analytics.value = await faqApi.getSearchAnalytics();
  } catch (error) {
    errorMessage.value = error.response?.data?.message || "無法載入客服查詢紀錄";
  } finally {
    loading.value = false;
  }
};

const createFaqFromQuery = (query) => {
  router.push({
    path: "/admin/faqs",
    query: { draftQuestion: query },
  });
};

const formatDateTime = (value) => {
  if (!value) return "-";
  return new Intl.DateTimeFormat("zh-TW", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value));
};

onMounted(loadAnalytics);
</script>

<template>
  <section class="faq-analytics-page">
    <header class="page-heading">
      <div>
        <span class="eyebrow">Support Operations</span>
        <h1>客服查詢紀錄</h1>
        <p>
          追蹤客人在客服窗輸入的問題，找出 FAQ 沒有回答到的規則，讓知識庫能依真實需求補強。
        </p>
      </div>
      <button class="secondary-btn" type="button" :disabled="loading" @click="loadAnalytics">
        <i class="bx bx-refresh"></i>
        重新整理
      </button>
    </header>

    <div v-if="errorMessage" class="notice danger">{{ errorMessage }}</div>

    <div class="metric-grid">
      <article>
        <span>{{ summary.windowLabel }}</span>
        <strong>{{ summary.totalSearches || 0 }}</strong>
        <small>客服窗搜尋</small>
      </article>
      <article>
        <span>命中率</span>
        <strong>{{ summary.hitRate || 0 }}%</strong>
        <small>有找到 FAQ 答案</small>
      </article>
      <article>
        <span>未命中</span>
        <strong>{{ summary.missedSearches || 0 }}</strong>
        <small>需要補內容的問題</small>
      </article>
      <article>
        <span>不同問題</span>
        <strong>{{ summary.uniqueQueries || 0 }}</strong>
        <small>去重後查詢字串</small>
      </article>
    </div>

    <div class="workspace-grid">
      <main class="missed-panel">
        <div class="panel-head">
          <div>
            <span class="eyebrow">Content Gaps</span>
            <h2>未命中問題</h2>
          </div>
          <p>優先把反覆出現、又找不到答案的問題補成 FAQ。</p>
        </div>

        <div v-if="loading" class="empty-state">載入中...</div>
        <div v-else-if="!missedQueries.length" class="empty-state">
          目前沒有未命中問題。客服 FAQ 覆蓋狀況良好。
        </div>
        <div v-else class="query-list">
          <article v-for="item in missedQueries" :key="item.query">
            <div>
              <span>{{ item.searchCount }} 次詢問</span>
              <h3>{{ item.query }}</h3>
              <small>最後查詢 {{ formatDateTime(item.lastSearchedAt) }}</small>
            </div>
            <button type="button" @click="createFaqFromQuery(item.query)">
              <i class="bx bx-plus"></i>
              建立 FAQ
            </button>
          </article>
        </div>
      </main>

      <aside class="trend-panel">
        <section>
          <div class="panel-head compact">
            <div>
              <span class="eyebrow">Demand</span>
              <h2>熱門查詢</h2>
            </div>
          </div>
          <ol v-if="popularQueries.length" class="rank-list">
            <li v-for="item in popularQueries" :key="item.query">
              <span>{{ item.query }}</span>
              <strong>{{ item.searchCount }}</strong>
            </li>
          </ol>
          <div v-else class="empty-state compact-empty">尚無查詢資料</div>
        </section>

        <section>
          <div class="panel-head compact">
            <div>
              <span class="eyebrow">Recent</span>
              <h2>最近查詢</h2>
            </div>
          </div>
          <div v-if="recentLogs.length" class="recent-list">
            <article v-for="log in recentLogs.slice(0, 10)" :key="log.logId">
              <div>
                <strong>{{ log.queryText }}</strong>
                <small>{{ formatDateTime(log.createdAt) }}</small>
              </div>
              <span :class="{ matched: log.matched }">
                {{ log.matched ? "已命中" : "未命中" }}
              </span>
            </article>
          </div>
          <div v-else class="empty-state compact-empty">尚無最近查詢</div>
        </section>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.faq-analytics-page {
  display: grid;
  gap: 22px;
}

.page-heading,
.metric-grid article,
.missed-panel,
.trend-panel section {
  border: 1px solid #e6dfd7;
  background: #fff;
}

.page-heading {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 30px;
}

.eyebrow {
  color: #a66a3e;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0;
  text-transform: uppercase;
}

.page-heading h1,
.panel-head h2 {
  margin: 6px 0;
  color: #2f2924;
  font-size: 34px;
  font-weight: 900;
}

.panel-head h2 {
  font-size: 26px;
}

.page-heading p,
.panel-head p,
.empty-state,
.recent-list small,
.query-list small,
.metric-grid small {
  color: #68727e;
  line-height: 1.7;
}

.page-heading p {
  max-width: 760px;
  margin: 0;
}

.secondary-btn,
.query-list button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 42px;
  border: 1px solid #d8cbbf;
  background: #fff;
  color: #6e4d35;
  padding: 0 18px;
  font-weight: 900;
}

.secondary-btn:hover,
.query-list button:hover {
  border-color: #dca874;
  background: #fff7ee;
}

.notice {
  padding: 14px 18px;
  font-weight: 900;
}

.notice.danger {
  background: #fff1ef;
  color: #a33a2c;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.metric-grid article {
  display: grid;
  gap: 4px;
  padding: 20px;
}

.metric-grid span {
  color: #6d7682;
  font-weight: 900;
}

.metric-grid strong {
  color: #2f2924;
  font-size: 32px;
}

.workspace-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 430px;
  gap: 18px;
  align-items: start;
}

.missed-panel,
.trend-panel section {
  padding: 22px;
}

.trend-panel {
  display: grid;
  gap: 18px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: end;
  margin-bottom: 20px;
  border-bottom: 1px solid #eaded2;
  padding-bottom: 16px;
}

.panel-head.compact {
  align-items: start;
  margin-bottom: 12px;
}

.query-list {
  display: grid;
  gap: 12px;
}

.query-list article {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
  border: 1px solid #eaded2;
  background: #fffaf4;
  padding: 18px;
}

.query-list span {
  color: #a66a3e;
  font-size: 12px;
  font-weight: 900;
}

.query-list h3 {
  margin: 6px 0;
  color: #2f2924;
  font-size: 20px;
  line-height: 1.45;
}

.rank-list {
  display: grid;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.rank-list li {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  align-items: center;
  border-bottom: 1px solid #f0e6dc;
  padding: 11px 0;
}

.rank-list span,
.recent-list strong {
  color: #2f2924;
  font-weight: 900;
  line-height: 1.5;
}

.rank-list strong {
  min-width: 34px;
  border: 1px solid #eaded2;
  background: #fffaf4;
  color: #a66a3e;
  padding: 5px 8px;
  text-align: center;
}

.recent-list {
  display: grid;
  gap: 8px;
}

.recent-list article {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  align-items: start;
  border-bottom: 1px solid #f0e6dc;
  padding: 12px 0;
}

.recent-list article > div {
  display: grid;
  gap: 4px;
}

.recent-list article > span {
  border: 1px solid #d9b49a;
  color: #a33a2c;
  padding: 5px 8px;
  font-size: 12px;
  font-weight: 900;
}

.recent-list article > span.matched {
  border-color: #bbd7c4;
  color: #2e7d4f;
}

.empty-state {
  border: 1px dashed #d8cbbf;
  background: #fffaf4;
  padding: 20px;
  font-weight: 800;
}

.compact-empty {
  padding: 14px;
}

@media (max-width: 1180px) {
  .workspace-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 780px) {
  .page-heading,
  .panel-head,
  .query-list article {
    display: grid;
  }

  .metric-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 560px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
