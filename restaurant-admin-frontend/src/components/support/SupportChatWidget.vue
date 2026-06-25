<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { faqApi } from "@/api/faq";

const router = useRouter();
const isOpen = ref(false);
const query = ref("");
const loading = ref(false);
const errorMessage = ref("");
const results = ref([]);
const suggestions = ref([]);
const hasSearched = ref(false);

const quickPrompts = [
  "訂位需要訂金嗎？",
  "取消訂位可以退款嗎？",
  "可以外帶點餐嗎？",
];

const visibleAnswers = computed(() => (results.value.length ? results.value : suggestions.value));

const openChat = () => {
  isOpen.value = true;
};

const closeChat = () => {
  isOpen.value = false;
};

const search = async (text = query.value) => {
  query.value = text;
  loading.value = true;
  errorMessage.value = "";
  hasSearched.value = Boolean(text.trim());

  try {
    const response = await faqApi.searchFaqs(text.trim());
    results.value = response.results || [];
    suggestions.value = response.suggestions || [];
  } catch (error) {
    results.value = [];
    errorMessage.value = "目前無法連線客服知識庫，請稍後再試。";
  } finally {
    loading.value = false;
  }
};

const submitSearch = () => {
  search(query.value);
};

const goFaq = () => {
  isOpen.value = false;
  router.push("/faq");
};

onMounted(() => {
  search("");
});
</script>

<template>
  <div class="support-widget" aria-live="polite">
    <section v-if="isOpen" class="support-panel" aria-label="敘日客服助手">
      <header class="support-header">
        <div>
          <span>Support Desk</span>
          <h2>敘日客服</h2>
        </div>
        <button type="button" class="icon-btn" aria-label="關閉客服視窗" @click="closeChat">
          <i class="bi bi-x-lg"></i>
        </button>
      </header>

      <div class="support-intro">
        <p>輸入訂位、訂金、點餐或門市問題，我會從官方 FAQ 裡找最接近的規則。</p>
      </div>

      <form class="support-search" @submit.prevent="submitSearch">
        <label class="visually-hidden" for="support-query">搜尋問題</label>
        <input
          id="support-query"
          v-model="query"
          type="search"
          placeholder="例如：訂金可以退嗎？"
        />
        <button type="submit" :disabled="loading">
          <i class="bi bi-search"></i>
        </button>
      </form>

      <div class="quick-prompts">
        <button
          v-for="prompt in quickPrompts"
          :key="prompt"
          type="button"
          @click="search(prompt)"
        >
          {{ prompt }}
        </button>
      </div>

      <div class="support-body">
        <div v-if="loading" class="support-state">查詢中...</div>
        <div v-else-if="errorMessage" class="support-state danger">{{ errorMessage }}</div>
        <div v-else-if="hasSearched && !results.length" class="support-state">
          沒有找到完全相符的規則。可以換個關鍵字，或查看完整 FAQ。
        </div>

        <article v-for="item in visibleAnswers" :key="item.faqId" class="answer-card">
          <span>{{ item.categoryLabel }}</span>
          <h3>{{ item.question }}</h3>
          <p>{{ item.answer }}</p>
        </article>
      </div>

      <footer class="support-footer">
        <button type="button" @click="goFaq">查看完整 FAQ</button>
      </footer>
    </section>

    <button
      type="button"
      class="support-toggle"
      :aria-expanded="isOpen"
      aria-label="開啟客服助手"
      @click="openChat"
    >
      <i class="bi bi-chat-dots"></i>
      <span>客服</span>
    </button>
  </div>
</template>

<style scoped>
.support-widget {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1200;
  font-family: "Noto Sans TC", system-ui, -apple-system, sans-serif;
}

.support-toggle {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 48px;
  border: 1px solid rgba(46, 33, 24, 0.12);
  border-radius: 999px;
  background: #211814;
  color: #fff;
  padding: 0 18px;
  font-weight: 700;
  box-shadow: 0 18px 42px rgba(22, 14, 9, 0.24);
}

.support-panel {
  position: absolute;
  right: 0;
  bottom: 64px;
  display: grid;
  width: min(380px, calc(100vw - 32px));
  max-height: min(680px, calc(100vh - 120px));
  overflow: hidden;
  border: 1px solid #ded4ca;
  background: #fffaf4;
  box-shadow: 0 30px 80px rgba(20, 14, 9, 0.28);
}

.support-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #211814;
  color: #fff;
  padding: 20px;
}

.support-header span {
  color: #dca874;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
}

.support-header h2 {
  margin: 4px 0 0;
  color: #fff;
  font-family: "Noto Serif TC", serif;
  font-size: 24px;
  font-weight: 700;
}

.icon-btn {
  width: 36px;
  height: 36px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.support-intro {
  padding: 16px 20px 0;
}

.support-intro p {
  margin: 0;
  color: #675c54;
  font-size: 14px;
  line-height: 1.7;
}

.support-search {
  display: grid;
  grid-template-columns: 1fr 46px;
  margin: 16px 20px 12px;
  border: 1px solid #d8cbbf;
  background: #fff;
}

.support-search input,
.support-search button {
  min-height: 46px;
  border: 0;
  background: transparent;
}

.support-search input {
  min-width: 0;
  padding: 0 14px;
  color: #2f2924;
  outline: none;
}

.support-search button {
  border-left: 1px solid #d8cbbf;
  color: #9a5d35;
}

.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 20px 14px;
}

.quick-prompts button {
  border: 1px solid #dccdbf;
  background: #fff;
  color: #7c6250;
  padding: 7px 10px;
  font-size: 13px;
}

.support-body {
  display: grid;
  gap: 10px;
  overflow: auto;
  padding: 0 20px 18px;
}

.support-state {
  border: 1px dashed #d8cbbf;
  background: #fff;
  color: #6f6259;
  padding: 14px;
  font-size: 14px;
  line-height: 1.6;
}

.support-state.danger {
  color: #9b2f1f;
}

.answer-card {
  border: 1px solid #e2d8ce;
  background: #fff;
  padding: 16px;
}

.answer-card span {
  color: #9a5d35;
  font-size: 12px;
  font-weight: 700;
}

.answer-card h3 {
  margin: 6px 0 8px;
  color: #2f2924;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.5;
}

.answer-card p {
  margin: 0;
  color: #615850;
  font-size: 14px;
  line-height: 1.75;
}

.support-footer {
  border-top: 1px solid #ded4ca;
  padding: 14px 20px;
}

.support-footer button {
  width: 100%;
  min-height: 42px;
  border: 0;
  background: #dca874;
  color: #211814;
  font-weight: 800;
}

.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
}

@media (max-width: 640px) {
  .support-widget {
    right: 16px;
    bottom: 16px;
  }
}
</style>
