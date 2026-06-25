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
  "訂位最早可以預約多久？",
  "取消訂位可以退款嗎？",
  "可以外帶點餐嗎？",
  "門市可以停車嗎？",
];

const visibleAnswers = computed(() => (results.value.length ? results.value : suggestions.value));
const answerCount = computed(() => visibleAnswers.value.length);

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
  <Teleport to="body">
    <div class="support-widget" aria-live="polite">
      <section v-if="isOpen" class="support-panel" aria-label="敘日服務台">
        <header class="support-header">
          <div>
            <span>Live FAQ Desk</span>
            <h2>敘日服務台</h2>
            <small>訂位、訂金、外帶與門市規則</small>
          </div>
          <button type="button" class="icon-btn" aria-label="關閉客服視窗" @click="closeChat">
            <i class="bi bi-x-lg"></i>
          </button>
        </header>

        <div class="support-body">
          <div class="message-row assistant">
            <div class="assistant-avatar">敘</div>
            <div class="message-bubble">
              <strong>想確認哪一項規則？</strong>
              <p>我會從已發布的 FAQ 裡找最接近的答案，適合快速確認訂位、訂金、外帶與門市資訊。</p>
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
            </div>
          </div>

          <div v-if="hasSearched" class="message-row guest">
            <div class="message-bubble">{{ query }}</div>
          </div>

          <div v-if="loading" class="message-row assistant">
            <div class="assistant-avatar">敘</div>
            <div class="message-bubble muted">正在查詢規則...</div>
          </div>

          <div v-else-if="errorMessage" class="message-row assistant">
            <div class="assistant-avatar">敘</div>
            <div class="message-bubble danger">{{ errorMessage }}</div>
          </div>

          <div v-else-if="hasSearched && !results.length" class="message-row assistant">
            <div class="assistant-avatar">敘</div>
            <div class="message-bubble">
              <strong>目前沒有完全相符的規則</strong>
              <p>可以換個關鍵字，或到完整 FAQ 看所有訂位、付款與門市條款。</p>
            </div>
          </div>

          <div v-else class="message-row assistant">
            <div class="assistant-avatar">敘</div>
            <div class="message-bubble result-bubble">
              <strong>{{ results.length ? `找到 ${answerCount} 則相關規則` : "先給你幾個常用規則" }}</strong>
              <article v-for="item in visibleAnswers" :key="item.faqId" class="answer-card">
                <span>{{ item.categoryLabel }}</span>
                <h3>{{ item.question }}</h3>
                <p>{{ item.answer }}</p>
              </article>
            </div>
          </div>
        </div>

        <footer class="support-footer">
          <form class="support-search" @submit.prevent="submitSearch">
            <label class="visually-hidden" for="support-query">搜尋問題</label>
            <input
              id="support-query"
              v-model="query"
              type="search"
              placeholder="輸入問題，例如：訂金可以退嗎？"
            />
            <button type="submit" :disabled="loading" aria-label="送出查詢">
              <i class="bi bi-send"></i>
            </button>
          </form>
          <button type="button" class="faq-link" @click="goFaq">查看完整 FAQ</button>
        </footer>
      </section>

      <button
        type="button"
        class="support-toggle"
        :aria-expanded="isOpen"
        aria-label="開啟敘日服務台"
        @click="openChat"
      >
        <span class="support-toggle-icon">
          <i class="bi bi-chat-dots"></i>
        </span>
        <span class="support-toggle-copy">
          <strong>問問敘日</strong>
          <small>FAQ 快速查詢</small>
        </span>
      </button>
    </div>
  </Teleport>
</template>

<style scoped>
.support-widget {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 2147483000;
  font-family: "Noto Sans TC", system-ui, -apple-system, sans-serif;
}

.support-toggle {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  min-height: 58px;
  border: 1px solid rgba(220, 168, 116, 0.46);
  border-radius: 999px;
  background: #241914;
  color: #fff;
  padding: 0 20px 0 12px;
  font-weight: 700;
  box-shadow: 0 18px 42px rgba(22, 14, 9, 0.34);
}

.support-toggle-icon {
  position: relative;
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 999px;
  background: #dca874;
  color: #241914;
}

.support-toggle-icon::after {
  position: absolute;
  top: 1px;
  right: 1px;
  width: 9px;
  height: 9px;
  border: 2px solid #241914;
  border-radius: 999px;
  background: #5fd18a;
  content: "";
}

.support-toggle-copy {
  display: grid;
  gap: 1px;
  text-align: left;
}

.support-toggle-copy strong {
  font-size: 15px;
  line-height: 1.2;
}

.support-toggle-copy small {
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
  font-weight: 600;
  line-height: 1.2;
}

.support-panel {
  position: absolute;
  right: 0;
  bottom: 76px;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  width: min(420px, calc(100vw - 32px));
  max-height: min(720px, calc(100vh - 120px));
  overflow: hidden;
  border: 1px solid #ded4ca;
  border-radius: 18px;
  background: #f8f2ea;
  box-shadow: 0 30px 80px rgba(20, 14, 9, 0.34);
}

.support-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #2a1d18;
  color: #fff;
  padding: 20px 22px;
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
  font-size: 26px;
  font-weight: 700;
}

.support-header small {
  display: block;
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
}

.icon-btn {
  width: 42px;
  height: 42px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: transparent;
  color: #fff;
}

.support-search {
  display: grid;
  grid-template-columns: 1fr 46px;
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
  margin-top: 14px;
}

.quick-prompts button {
  border: 1px solid #dccdbf;
  border-radius: 999px;
  background: #fffdf9;
  color: #7c6250;
  padding: 8px 12px;
  font-size: 13px;
  text-align: left;
}

.support-body {
  display: grid;
  align-content: start;
  gap: 14px;
  overflow: auto;
  min-height: 0;
  padding: 18px 20px 20px;
}

.message-row {
  display: flex;
  gap: 10px;
}

.message-row.guest {
  justify-content: flex-end;
}

.assistant-avatar {
  display: grid;
  flex: 0 0 34px;
  place-items: center;
  width: 34px;
  height: 34px;
  border: 1px solid #dfc5ae;
  border-radius: 999px;
  background: #fffdf9;
  color: #9a5d35;
  font-family: "Noto Serif TC", serif;
  font-weight: 800;
}

.message-bubble {
  max-width: min(320px, 100%);
  border: 1px solid #e0d2c5;
  border-radius: 16px;
  border-top-left-radius: 4px;
  background: #fffdf9;
  color: #6f6259;
  padding: 14px 15px;
  font-size: 14px;
  line-height: 1.6;
}

.message-row.guest .message-bubble {
  border-color: #c98e63;
  border-top-left-radius: 16px;
  border-top-right-radius: 4px;
  background: #fff3e5;
  color: #2f2924;
  font-weight: 700;
}

.message-bubble strong {
  display: block;
  color: #2f2924;
  font-size: 15px;
  line-height: 1.45;
}

.message-bubble p {
  margin: 8px 0 0;
}

.message-bubble.muted {
  color: #6f6259;
}

.message-bubble.danger {
  color: #9b2f1f;
}

.result-bubble {
  display: grid;
  gap: 10px;
}

.answer-card {
  border: 1px solid #e2d8ce;
  background: #fff;
  padding: 13px 14px;
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
  background: #fffdf9;
  padding: 14px 20px 16px;
}

.faq-link {
  width: 100%;
  min-height: 38px;
  margin-top: 10px;
  border: 0;
  background: transparent;
  color: #8a512d;
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
    right: 14px;
    bottom: 14px;
  }

  .support-panel {
    position: fixed;
    right: 12px;
    bottom: 82px;
    left: 12px;
    width: auto;
    max-height: min(74vh, 620px);
    border-radius: 18px;
  }

  .support-header {
    padding: 18px 18px 16px;
  }

  .support-body {
    gap: 12px;
    padding: 14px 14px 16px;
  }

  .message-bubble {
    max-width: 100%;
  }

  .support-footer {
    padding: 12px 14px 14px;
  }

  .support-toggle {
    min-height: 52px;
    padding-right: 16px;
  }

  .support-toggle-copy small {
    display: none;
  }
}
</style>
