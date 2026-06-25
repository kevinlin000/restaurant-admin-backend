<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import { faqApi } from "@/api/faq";

const route = useRoute();

const faqs = ref([]);
const selectedId = ref(null);
const loading = ref(false);
const saving = ref(false);
const message = ref("");
const errorMessage = ref("");
const filterStatus = ref("ALL");
const filterCategory = ref("ALL");

const categoryOptions = [
  { value: "RESERVATION", label: "訂位規則" },
  { value: "DEPOSIT", label: "訂金與取消" },
  { value: "ORDER", label: "點餐與外帶" },
  { value: "PAYMENT", label: "付款與發票" },
  { value: "STORE", label: "門市資訊" },
  { value: "MEMBER", label: "會員服務" },
  { value: "SERVICE", label: "用餐服務" },
];

const statusOptions = [
  { value: "DRAFT", label: "草稿" },
  { value: "PUBLISHED", label: "已發布" },
  { value: "ARCHIVED", label: "已封存" },
];

const emptyForm = () => ({
  faqId: null,
  category: "RESERVATION",
  status: "DRAFT",
  question: "",
  answer: "",
  keywords: "",
  isFeatured: false,
  sortOrder: 10,
});

const form = reactive(emptyForm());

const selectedFaq = computed(() => faqs.value.find((faq) => faq.faqId === selectedId.value));

const filteredFaqs = computed(() =>
  faqs.value.filter((faq) => {
    const statusMatched = filterStatus.value === "ALL" || faq.status === filterStatus.value;
    const categoryMatched = filterCategory.value === "ALL" || faq.category === filterCategory.value;
    return statusMatched && categoryMatched;
  }),
);

const publishedCount = computed(() => faqs.value.filter((faq) => faq.status === "PUBLISHED").length);
const featuredCount = computed(() => faqs.value.filter((faq) => faq.isFeatured).length);
const draftCount = computed(() => faqs.value.filter((faq) => faq.status === "DRAFT").length);

const categoryLabel = (category) =>
  categoryOptions.find((option) => option.value === category)?.label || category;

const statusLabel = (status) =>
  statusOptions.find((option) => option.value === status)?.label || status;

const resetMessages = () => {
  message.value = "";
  errorMessage.value = "";
};

const showError = (error, fallback) => {
  errorMessage.value = error.response?.data?.message || fallback;
};

const assignForm = (faq) => {
  Object.assign(form, emptyForm(), {
    faqId: faq.faqId,
    category: faq.category,
    status: faq.status,
    question: faq.question || "",
    answer: faq.answer || "",
    keywords: faq.keywords || "",
    isFeatured: Boolean(faq.isFeatured),
    sortOrder: faq.sortOrder ?? 10,
  });
};

const startCreate = () => {
  selectedId.value = null;
  Object.assign(form, emptyForm());
  resetMessages();
};

const applyDraftQuestionFromRoute = () => {
  const draftQuestion =
    typeof route.query.draftQuestion === "string" ? route.query.draftQuestion.trim() : "";
  if (!draftQuestion) return;

  startCreate();
  form.question = draftQuestion;
  form.keywords = draftQuestion;
  message.value = "已帶入客服未命中問題，請補上回答後儲存。";
};

const selectFaq = (faq) => {
  selectedId.value = faq.faqId;
  assignForm(faq);
  resetMessages();
};

const loadFaqs = async () => {
  loading.value = true;
  resetMessages();
  try {
    faqs.value = await faqApi.getAdminFaqs();
    if (faqs.value.length && !selectedId.value) {
      selectFaq(faqs.value[0]);
    }
  } catch (error) {
    faqs.value = [];
    showError(error, "無法載入 FAQ");
  } finally {
    loading.value = false;
  }
};

const toPayload = () => ({
  category: form.category,
  status: form.status,
  question: form.question,
  answer: form.answer,
  keywords: form.keywords,
  isFeatured: form.isFeatured,
  sortOrder: Number(form.sortOrder) || 0,
});

const saveFaq = async () => {
  saving.value = true;
  resetMessages();
  try {
    const saved = form.faqId
      ? await faqApi.updateFaq(form.faqId, toPayload())
      : await faqApi.createFaq(toPayload());
    message.value = form.faqId ? "FAQ 已更新" : "FAQ 已建立";
    selectedId.value = saved.faqId;
    await loadFaqs();
    selectFaq(saved);
  } catch (error) {
    showError(error, "儲存 FAQ 失敗");
  } finally {
    saving.value = false;
  }
};

const deleteFaq = async () => {
  if (!form.faqId || !window.confirm("確定刪除此 FAQ？")) return;
  saving.value = true;
  resetMessages();
  try {
    await faqApi.deleteFaq(form.faqId);
    message.value = "FAQ 已刪除";
    selectedId.value = null;
    Object.assign(form, emptyForm());
    await loadFaqs();
  } catch (error) {
    showError(error, "刪除 FAQ 失敗");
  } finally {
    saving.value = false;
  }
};

onMounted(async () => {
  await loadFaqs();
  applyDraftQuestionFromRoute();
});
</script>

<template>
  <section class="faq-admin-page">
    <header class="page-heading">
      <div>
        <span class="eyebrow">Support Knowledge</span>
        <h1>常見問答管理</h1>
        <p>維護前台 FAQ 與右下角客服窗共用的知識庫。發布後，客人就能直接搜尋餐廳規則。</p>
      </div>
      <button class="primary-btn" type="button" @click="startCreate">
        <i class="bx bx-plus"></i>
        新增 FAQ
      </button>
    </header>

    <div class="insight-row">
      <div>
        <span>全部 FAQ</span>
        <strong>{{ faqs.length }}</strong>
      </div>
      <div>
        <span>已發布</span>
        <strong>{{ publishedCount }}</strong>
      </div>
      <div>
        <span>熱門問題</span>
        <strong>{{ featuredCount }}</strong>
      </div>
      <div>
        <span>草稿</span>
        <strong>{{ draftCount }}</strong>
      </div>
    </div>

    <div v-if="message" class="notice success">{{ message }}</div>
    <div v-if="errorMessage" class="notice danger">{{ errorMessage }}</div>

    <div class="workspace-grid">
      <aside class="faq-list-panel">
        <div class="filters">
          <select v-model="filterStatus">
            <option value="ALL">全部狀態</option>
            <option v-for="option in statusOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
          <select v-model="filterCategory">
            <option value="ALL">全部分類</option>
            <option v-for="option in categoryOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </div>

        <div v-if="loading" class="empty-state">載入中...</div>
        <div v-else-if="!filteredFaqs.length" class="empty-state">目前沒有符合條件的 FAQ</div>

        <template v-else>
          <button
            v-for="faq in filteredFaqs"
            :key="faq.faqId"
            type="button"
            class="faq-list-item"
            :class="{ active: selectedId === faq.faqId }"
            @click="selectFaq(faq)"
          >
            <span>{{ faq.categoryLabel }}</span>
            <strong>{{ faq.question }}</strong>
            <small>{{ faq.statusLabel }} · 排序 {{ faq.sortOrder }}</small>
          </button>
        </template>
      </aside>

      <main class="editor-panel">
        <div class="editor-head">
          <div>
            <span>{{ form.faqId ? `FAQ #${form.faqId}` : "New FAQ" }}</span>
            <h2>{{ form.faqId ? "編輯常見問題" : "新增常見問題" }}</h2>
          </div>
          <button
            v-if="form.faqId"
            class="ghost-danger"
            type="button"
            :disabled="saving"
            @click="deleteFaq"
          >
            刪除
          </button>
        </div>

        <div class="form-grid">
          <label>
            分類
            <select v-model="form.category">
              <option v-for="option in categoryOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>

          <label>
            狀態
            <select v-model="form.status">
              <option v-for="option in statusOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>

          <label>
            排序
            <input v-model.number="form.sortOrder" type="number" min="0" step="1" />
          </label>

          <label class="toggle-row">
            <input v-model="form.isFeatured" type="checkbox" />
            <span>設為客服窗推薦問題</span>
          </label>
        </div>

        <label class="full-field">
          問題
          <input v-model="form.question" type="text" maxlength="180" placeholder="例如：取消訂位後訂金可以退嗎？" />
        </label>

        <label class="full-field">
          回答
          <textarea v-model="form.answer" rows="8" maxlength="1200" placeholder="用客人能理解的語氣寫清楚規則、限制與下一步。"></textarea>
        </label>

        <label class="full-field">
          搜尋關鍵字
          <input v-model="form.keywords" type="text" maxlength="320" placeholder="訂金,退款,取消,未到,no show" />
        </label>

        <div class="actions">
          <button class="secondary-btn" type="button" @click="startCreate">清空</button>
          <button class="primary-btn" type="button" :disabled="saving" @click="saveFaq">
            <i class="bx bx-save"></i>
            {{ saving ? "儲存中..." : "儲存 FAQ" }}
          </button>
        </div>
      </main>

      <aside class="preview-panel">
        <span>Customer Preview</span>
        <article>
          <small>{{ categoryLabel(form.category) }}</small>
          <h3>{{ form.question || "問題會顯示在這裡" }}</h3>
          <p>{{ form.answer || "回答會同步出現在 FAQ 頁與客服窗搜尋結果。" }}</p>
        </article>
        <div class="preview-meta">
          <strong>{{ statusLabel(form.status) }}</strong>
          <span>{{ form.isFeatured ? "客服推薦" : "一般問題" }}</span>
        </div>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.faq-admin-page {
  display: grid;
  gap: 22px;
}

.page-heading,
.editor-panel,
.faq-list-panel,
.preview-panel,
.insight-row > div {
  border: 1px solid #e6dfd7;
  background: #fff;
}

.page-heading {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 30px;
}

.eyebrow,
.editor-head span,
.preview-panel > span,
.preview-panel small {
  color: #a66a3e;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.page-heading h1,
.editor-head h2 {
  margin: 6px 0;
  color: #2f2924;
  font-size: 34px;
  font-weight: 900;
}

.page-heading p {
  max-width: 720px;
  margin: 0;
  color: #67717f;
  line-height: 1.7;
}

.primary-btn,
.secondary-btn,
.ghost-danger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 42px;
  border: 0;
  padding: 0 18px;
  font-weight: 800;
}

.primary-btn {
  background: #dca874;
  color: #211814;
}

.secondary-btn {
  border: 1px solid #d8cbbf;
  background: #fff;
  color: #6e5f55;
}

.ghost-danger {
  background: #fff2ef;
  color: #9b2f1f;
}

.insight-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.insight-row > div {
  display: grid;
  gap: 4px;
  padding: 20px;
}

.insight-row span {
  color: #6d7682;
  font-weight: 800;
}

.insight-row strong {
  color: #2f2924;
  font-size: 30px;
}

.notice {
  padding: 14px 18px;
  font-weight: 800;
}

.notice.success {
  background: #effaf4;
  color: #2e7d4f;
}

.notice.danger {
  background: #fff1ef;
  color: #a33a2c;
}

.workspace-grid {
  display: grid;
  grid-template-columns: 330px minmax(0, 1fr) 300px;
  gap: 18px;
  align-items: start;
}

.faq-list-panel,
.editor-panel,
.preview-panel {
  padding: 20px;
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
  border: 1px solid #d8cbbf;
  background: #fffaf4;
  color: #2f2924;
  padding: 11px 12px;
  outline: none;
}

textarea {
  resize: vertical;
}

.faq-list-item {
  display: grid;
  gap: 5px;
  width: 100%;
  border: 1px solid #eaded2;
  background: #fff;
  padding: 14px;
  text-align: left;
}

.faq-list-item + .faq-list-item {
  margin-top: 10px;
}

.faq-list-item.active,
.faq-list-item:hover {
  border-color: #dca874;
  background: #fff7ee;
}

.faq-list-item span {
  color: #a66a3e;
  font-size: 12px;
  font-weight: 800;
}

.faq-list-item strong {
  color: #2f2924;
  line-height: 1.5;
}

.faq-list-item small,
.empty-state {
  color: #78818c;
}

.editor-head {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 22px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 120px 1fr;
  gap: 14px;
}

label {
  display: grid;
  gap: 8px;
  color: #4f5965;
  font-weight: 800;
}

.toggle-row {
  align-content: end;
  grid-template-columns: 18px 1fr;
  gap: 10px;
  padding-bottom: 10px;
}

.toggle-row input {
  width: 18px;
}

.full-field {
  margin-top: 18px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.preview-panel {
  position: sticky;
  top: 116px;
}

.preview-panel article {
  margin-top: 16px;
  border: 1px solid #eaded2;
  background: #fffaf4;
  padding: 18px;
}

.preview-panel h3 {
  margin: 8px 0 10px;
  color: #2f2924;
  font-family: "Noto Serif TC", serif;
  font-size: 22px;
  line-height: 1.45;
}

.preview-panel p {
  margin: 0;
  color: #615850;
  line-height: 1.8;
}

.preview-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 14px;
}

.preview-meta strong,
.preview-meta span {
  background: #211814;
  color: #fff;
  padding: 7px 10px;
  font-size: 12px;
}

.preview-meta span {
  background: #dca874;
  color: #211814;
}

@media (max-width: 1180px) {
  .workspace-grid {
    grid-template-columns: 1fr;
  }

  .preview-panel {
    position: static;
  }
}

@media (max-width: 780px) {
  .page-heading,
  .editor-head {
    display: grid;
  }

  .insight-row,
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
