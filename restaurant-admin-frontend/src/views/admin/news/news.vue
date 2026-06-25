<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import api from "@/api/axios";
import { newsApi } from "@/api/news";
import seasonalImage from "@/assets/images/salmon-sashimi.jpg";
import noticeImage from "@/assets/images/japanese-tea.jpg";
import openingImage from "@/assets/images/sushi.jpg";
import memberImage from "@/assets/images/matcha-dessert.jpg";

const articles = ref([]);
const stores = ref([]);
const selectedId = ref(null);
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref("");
const message = ref("");
const filterStatus = ref("ALL");
const filterCategory = ref("ALL");

const categoryOptions = [
  { value: "EVENT", label: "活動" },
  { value: "NOTICE", label: "公告" },
  { value: "OPENING", label: "展店" },
  { value: "MEMBER", label: "會員" },
];

const statusOptions = [
  { value: "DRAFT", label: "草稿" },
  { value: "PUBLISHED", label: "已發布" },
  { value: "ARCHIVED", label: "已封存" },
];

const fallbackImages = {
  EVENT: seasonalImage,
  NOTICE: noticeImage,
  OPENING: openingImage,
  MEMBER: memberImage,
};

const emptyForm = () => ({
  newsId: null,
  category: "EVENT",
  status: "DRAFT",
  title: "",
  summary: "",
  content: "",
  coverImageUrl: "",
  publishedAt: new Date().toISOString().slice(0, 10),
  startDate: "",
  endDate: "",
  storeId: "",
  isFeatured: false,
  sortOrder: 10,
});

const form = reactive(emptyForm());

const unwrap = (response) => response.data?.data ?? response.data ?? [];

const selectedArticle = computed(() =>
  articles.value.find((article) => article.newsId === selectedId.value),
);

const filteredArticles = computed(() =>
  articles.value.filter((article) => {
    const statusMatched =
      filterStatus.value === "ALL" || article.status === filterStatus.value;
    const categoryMatched =
      filterCategory.value === "ALL" || article.category === filterCategory.value;
    return statusMatched && categoryMatched;
  }),
);

const publishedCount = computed(
  () => articles.value.filter((article) => article.status === "PUBLISHED").length,
);

const draftCount = computed(
  () => articles.value.filter((article) => article.status === "DRAFT").length,
);

const storeOptions = computed(() => [
  { storeId: "", storeName: "全門市適用" },
  ...stores.value,
]);

const previewImage = computed(
  () => form.coverImageUrl || fallbackImages[form.category] || fallbackImages.EVENT,
);

const periodPreview = computed(() => {
  if (!form.startDate && !form.endDate) return "長期公告";
  if (form.startDate && !form.endDate) return `${form.startDate} 起`;
  if (!form.startDate && form.endDate) return `${form.endDate} 止`;
  return `${form.startDate} - ${form.endDate}`;
});

const storePreview = computed(() => {
  if (!form.storeId) return "全門市適用";
  return stores.value.find((store) => Number(store.storeId) === Number(form.storeId))?.storeName || "指定門市";
});

const resetMessages = () => {
  message.value = "";
  errorMessage.value = "";
};

const showError = (error, fallback) => {
  errorMessage.value = error.response?.data?.message || fallback;
};

const assignForm = (article) => {
  Object.assign(form, emptyForm(), {
    newsId: article.newsId,
    category: article.category,
    status: article.status,
    title: article.title || "",
    summary: article.summary || "",
    content: article.content || "",
    coverImageUrl: article.coverImageUrl || "",
    publishedAt: article.publishedAt || new Date().toISOString().slice(0, 10),
    startDate: article.startDate || "",
    endDate: article.endDate || "",
    storeId: article.storeId || "",
    isFeatured: Boolean(article.isFeatured),
    sortOrder: article.sortOrder ?? 10,
  });
};

const startCreate = () => {
  selectedId.value = null;
  Object.assign(form, emptyForm());
  resetMessages();
};

const selectArticle = (article) => {
  selectedId.value = article.newsId;
  assignForm(article);
  resetMessages();
};

const loadStores = async () => {
  try {
    stores.value = unwrap(await api.get("/api/admin/stores"));
  } catch (error) {
    stores.value = [];
  }
};

const loadNews = async () => {
  loading.value = true;
  resetMessages();

  try {
    articles.value = await newsApi.getAdminNews();
    if (articles.value.length && !selectedId.value) {
      selectArticle(articles.value[0]);
    }
  } catch (error) {
    articles.value = [];
    showError(error, "無法載入最新消息");
  } finally {
    loading.value = false;
  }
};

const toPayload = () => ({
  category: form.category,
  status: form.status,
  title: form.title,
  summary: form.summary,
  content: form.content,
  coverImageUrl: form.coverImageUrl,
  publishedAt: form.publishedAt,
  startDate: form.startDate || null,
  endDate: form.endDate || null,
  storeId: form.storeId ? Number(form.storeId) : null,
  isFeatured: form.isFeatured,
  sortOrder: Number(form.sortOrder) || 0,
});

const saveNews = async () => {
  saving.value = true;
  resetMessages();

  try {
    const saved = form.newsId
      ? await newsApi.updateNews(form.newsId, toPayload())
      : await newsApi.createNews(toPayload());
    message.value = form.newsId ? "最新消息已更新" : "最新消息已建立";
    selectedId.value = saved.newsId;
    await loadNews();
    selectArticle(saved);
  } catch (error) {
    showError(error, "儲存最新消息失敗");
  } finally {
    saving.value = false;
  }
};

const deleteNews = async () => {
  if (!form.newsId || !window.confirm("確定刪除此最新消息？")) return;
  saving.value = true;
  resetMessages();

  try {
    await newsApi.deleteNews(form.newsId);
    message.value = "最新消息已刪除";
    selectedId.value = null;
    Object.assign(form, emptyForm());
    await loadNews();
  } catch (error) {
    showError(error, "刪除最新消息失敗");
  } finally {
    saving.value = false;
  }
};

const statusLabel = (status) =>
  statusOptions.find((option) => option.value === status)?.label || status;

const categoryLabel = (category) =>
  categoryOptions.find((option) => option.value === category)?.label || category;

onMounted(async () => {
  await Promise.all([loadStores(), loadNews()]);
});
</script>

<template>
  <section class="news-admin-page">
    <header class="page-heading">
      <div>
        <span class="eyebrow">Brand Desk</span>
        <h1>最新消息管理</h1>
        <p>管理官網消息、門市公告與會員活動。發布前先確認日期、適用門市與前台摘要。</p>
      </div>
      <button class="primary-btn" type="button" @click="startCreate">
        <i class="bx bx-plus"></i>
        新增消息
      </button>
    </header>

    <div class="insight-row">
      <div>
        <span>全部消息</span>
        <strong>{{ articles.length }}</strong>
      </div>
      <div>
        <span>已發布</span>
        <strong>{{ publishedCount }}</strong>
      </div>
      <div>
        <span>草稿</span>
        <strong>{{ draftCount }}</strong>
      </div>
    </div>

    <div v-if="message" class="notice success">{{ message }}</div>
    <div v-if="errorMessage" class="notice danger">{{ errorMessage }}</div>

    <div class="workbench">
      <aside class="article-panel">
        <div class="panel-toolbar">
          <div>
            <span class="eyebrow">Archive</span>
            <h2>公告清單</h2>
          </div>
          <span>{{ filteredArticles.length }} 則</span>
        </div>

        <div class="filters">
          <select v-model="filterStatus">
            <option value="ALL">全部狀態</option>
            <option v-for="status in statusOptions" :key="status.value" :value="status.value">
              {{ status.label }}
            </option>
          </select>
          <select v-model="filterCategory">
            <option value="ALL">全部分類</option>
            <option v-for="category in categoryOptions" :key="category.value" :value="category.value">
              {{ category.label }}
            </option>
          </select>
        </div>

        <div v-if="loading" class="empty-state">載入最新消息中</div>
        <div v-else-if="!filteredArticles.length" class="empty-state">尚無符合條件的消息</div>

        <template v-else>
          <button
            v-for="article in filteredArticles"
            :key="article.newsId"
            type="button"
            :class="['article-row', { active: selectedArticle?.newsId === article.newsId }]"
            @click="selectArticle(article)"
          >
            <span class="row-date">{{ article.publishedAt }}</span>
            <span class="row-main">
              <span>
                <b>{{ article.categoryLabel || categoryLabel(article.category) }}</b>
                <em>{{ article.statusLabel || statusLabel(article.status) }}</em>
              </span>
              <strong>{{ article.title }}</strong>
              <small>{{ article.storeScope }}</small>
            </span>
          </button>
        </template>
      </aside>

      <section class="editor-panel">
        <div class="editor-header">
          <div>
            <span class="eyebrow">Editor</span>
            <h2>{{ form.newsId ? "編輯消息" : "新增消息" }}</h2>
          </div>
          <div class="editor-actions">
            <button
              v-if="form.newsId"
              class="ghost-danger"
              type="button"
              :disabled="saving"
              @click="deleteNews"
            >
              刪除
            </button>
            <button class="primary-btn" type="button" :disabled="saving" @click="saveNews">
              {{ saving ? "儲存中" : "儲存" }}
            </button>
          </div>
        </div>

        <div class="form-grid">
          <label>
            分類
            <select v-model="form.category">
              <option v-for="category in categoryOptions" :key="category.value" :value="category.value">
                {{ category.label }}
              </option>
            </select>
          </label>

          <label>
            狀態
            <select v-model="form.status">
              <option v-for="status in statusOptions" :key="status.value" :value="status.value">
                {{ status.label }}
              </option>
            </select>
          </label>

          <label>
            發布日期
            <input v-model="form.publishedAt" type="date" />
          </label>

          <label>
            排序
            <input v-model.number="form.sortOrder" type="number" min="0" />
          </label>

          <label class="wide">
            標題
            <input v-model.trim="form.title" type="text" maxlength="160" placeholder="例如：夏旬和食祭同步登場" />
          </label>

          <label class="wide">
            摘要
            <textarea
              v-model.trim="form.summary"
              maxlength="320"
              rows="3"
              placeholder="顯示於前台列表，請用一句話說清楚活動重點"
            ></textarea>
          </label>

          <label>
            活動開始
            <input v-model="form.startDate" type="date" />
          </label>

          <label>
            活動結束
            <input v-model="form.endDate" type="date" />
          </label>

          <label>
            適用門市
            <select v-model="form.storeId">
              <option v-for="store in storeOptions" :key="store.storeId || 'all'" :value="store.storeId">
                {{ store.storeName }}
              </option>
            </select>
          </label>

          <label class="check-row">
            <input v-model="form.isFeatured" type="checkbox" />
            設為本期焦點
          </label>

          <label class="wide">
            封面圖片 URL
            <input v-model.trim="form.coverImageUrl" type="url" maxlength="500" placeholder="https://..." />
          </label>

          <label class="wide">
            內文
            <textarea v-model.trim="form.content" rows="6" placeholder="可輸入活動細節、注意事項或門市說明"></textarea>
          </label>
        </div>

        <article class="preview-card">
          <img :src="previewImage" alt="消息預覽圖" />
          <div>
            <span class="preview-meta">
              {{ categoryLabel(form.category) }} · {{ statusLabel(form.status) }} · {{ form.publishedAt }}
            </span>
            <h3>{{ form.title || "消息標題預覽" }}</h3>
            <p>{{ form.summary || "摘要會顯示在官網消息列表，建議控制在一到兩行。" }}</p>
            <div class="preview-facts">
              <span><i class="bx bx-calendar"></i>{{ periodPreview }}</span>
              <span><i class="bx bx-store"></i>{{ storePreview }}</span>
            </div>
          </div>
        </article>
      </section>
    </div>
  </section>
</template>

<style scoped>
.news-admin-page {
  color: #25313f;
  padding-bottom: 42px;
}

.page-heading,
.workbench,
.editor-panel,
.article-panel,
.insight-row > div,
.preview-card {
  border: 1px solid #e1d7cb;
  border-radius: 8px;
  background: #fff;
}

.page-heading {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: center;
  padding: 28px;
}

.eyebrow {
  display: inline-block;
  margin-bottom: 8px;
  color: #9a5f32;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

h1,
h2,
h3,
p {
  letter-spacing: 0;
}

h1 {
  margin: 0;
  font-size: 32px;
  font-weight: 900;
}

.page-heading p {
  max-width: 640px;
  margin: 10px 0 0;
  color: #697386;
  line-height: 1.7;
}

.primary-btn,
.ghost-danger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 40px;
  border-radius: 6px;
  padding: 0 16px;
  font-weight: 900;
}

.primary-btn {
  border: 1px solid #25313f;
  background: #25313f;
  color: #fff;
}

.ghost-danger {
  border: 1px solid #d8bbb4;
  background: #fff;
  color: #a13d2d;
}

.primary-btn:disabled,
.ghost-danger:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.insight-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin: 18px 0;
}

.insight-row > div {
  padding: 18px;
}

.insight-row span {
  color: #697386;
  font-weight: 800;
}

.insight-row strong {
  display: block;
  margin-top: 8px;
  font-size: 30px;
  line-height: 1;
}

.notice {
  border-radius: 6px;
  margin-bottom: 14px;
  padding: 12px 14px;
  font-weight: 900;
}

.notice.success {
  background: #e9f7ef;
  color: #176d3b;
}

.notice.danger {
  background: #fff0ee;
  color: #a13d2d;
}

.workbench {
  display: grid;
  grid-template-columns: 390px minmax(0, 1fr);
  gap: 0;
  overflow: hidden;
}

.article-panel {
  border: none;
  border-right: 1px solid #e1d7cb;
  border-radius: 0;
  padding: 24px;
}

.panel-toolbar,
.editor-header {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.panel-toolbar h2,
.editor-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 900;
}

.panel-toolbar > span {
  color: #697386;
  font-weight: 900;
}

.filters {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 14px;
}

select,
input,
textarea {
  width: 100%;
  border: 1px solid #d8cbbd;
  border-radius: 6px;
  background: #fff;
  color: #25313f;
  padding: 10px 12px;
  outline: none;
}

select:focus,
input:focus,
textarea:focus {
  border-color: #9a5f32;
  box-shadow: 0 0 0 3px rgba(154, 95, 50, 0.12);
}

.empty-state {
  border: 1px dashed #d8cbbd;
  border-radius: 6px;
  color: #697386;
  padding: 26px;
  text-align: center;
}

.article-row {
  display: grid;
  grid-template-columns: 86px minmax(0, 1fr);
  gap: 14px;
  width: 100%;
  border: 1px solid transparent;
  border-bottom-color: #eee5dc;
  background: transparent;
  padding: 16px 10px;
  text-align: left;
}

.article-row:hover,
.article-row.active {
  border-color: #d8cbbd;
  border-radius: 6px;
  background: #fbf7f1;
}

.row-date {
  color: #9a5f32;
  font-weight: 900;
}

.row-main {
  display: grid;
  gap: 6px;
}

.row-main span {
  display: flex;
  gap: 8px;
}

.row-main b,
.row-main em {
  border-radius: 999px;
  padding: 3px 8px;
  font-size: 12px;
  font-style: normal;
  font-weight: 900;
}

.row-main b {
  background: #f4e6d8;
  color: #8c552e;
}

.row-main em {
  background: #eef2f5;
  color: #566a7f;
}

.row-main strong {
  color: #25313f;
  line-height: 1.4;
}

.row-main small {
  color: #697386;
}

.editor-panel {
  border: none;
  border-radius: 0;
  padding: 24px;
}

.editor-actions {
  display: flex;
  gap: 10px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

label {
  display: grid;
  gap: 7px;
  color: #596779;
  font-weight: 900;
}

label.wide {
  grid-column: 1 / -1;
}

.check-row {
  display: flex;
  align-items: center;
  gap: 8px;
  align-self: end;
  min-height: 42px;
}

.check-row input {
  width: auto;
}

.preview-card {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 22px;
  margin-top: 22px;
  padding: 16px;
}

.preview-card img {
  width: 220px;
  height: 160px;
  border-radius: 4px;
  object-fit: cover;
}

.preview-meta {
  color: #9a5f32;
  font-size: 13px;
  font-weight: 900;
}

.preview-card h3 {
  margin: 10px 0 8px;
  font-size: 22px;
  font-weight: 900;
}

.preview-card p {
  margin: 0;
  color: #697386;
  line-height: 1.7;
}

.preview-facts {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  margin-top: 12px;
  color: #5f6e80;
  font-weight: 800;
}

.preview-facts span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

@media (max-width: 1180px) {
  .workbench,
  .preview-card {
    grid-template-columns: 1fr;
  }

  .article-panel {
    border-right: none;
    border-bottom: 1px solid #e1d7cb;
  }

  .form-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 720px) {
  .page-heading,
  .panel-toolbar,
  .editor-header {
    align-items: stretch;
    flex-direction: column;
  }

  .insight-row,
  .filters,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .preview-card img {
    width: 100%;
  }
}
</style>
