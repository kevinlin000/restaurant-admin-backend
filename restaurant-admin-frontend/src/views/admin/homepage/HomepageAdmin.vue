<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import api from "@/api/axios";
import { homepageApi } from "@/api/homepage";
import { newsApi } from "@/api/news";

import homeImage from "@/assets/images/Home.jpg";
import salmonSashimiImage from "@/assets/images/salmon-sashimi.jpg";
import reservationImage from "@/assets/images/reservation.jpg";
import seafoodSaladImage from "@/assets/images/seafood-salad.jpg";
import japaneseSakeImage from "@/assets/images/japanese-sake.jpg";
import aburiSalmonImage from "@/assets/images/aburi-salmon-sushi.jpg";
import sukiyakiImage from "@/assets/images/sukiyaki.jpg";
import tempuraImage from "@/assets/images/tempura.jpg";
import tofuImage from "@/assets/images/tofu.jpg";
import matchaDessertImage from "@/assets/images/matcha-dessert.jpg";

const loading = ref(false);
const saving = ref(false);
const message = ref("");
const errorMessage = ref("");
const stores = ref([]);
const newsItems = ref([]);

const imageOptions = [
  { key: "home", label: "首頁黑色和食", image: homeImage },
  { key: "salmonSashimi", label: "鮭魚刺身", image: salmonSashimiImage },
  { key: "reservation", label: "用餐空間", image: reservationImage },
  { key: "seafoodSalad", label: "海鮮沙拉", image: seafoodSaladImage },
  { key: "japaneseSake", label: "清酒搭餐", image: japaneseSakeImage },
  { key: "aburiSalmon", label: "炙燒鮭魚", image: aburiSalmonImage },
  { key: "sukiyaki", label: "壽喜燒", image: sukiyakiImage },
  { key: "tempura", label: "天婦羅", image: tempuraImage },
  { key: "tofu", label: "胡麻豆腐", image: tofuImage },
  { key: "matchaDessert", label: "抹茶甘味", image: matchaDessertImage },
];

const imageMap = Object.fromEntries(imageOptions.map((option) => [option.key, option.image]));

const emptyScene = () => ({
  label: "夜席",
  imageKey: "home",
  imageUrl: "",
  eyebrow: "Xuri Washoku",
  title: "敘日和食",
  lines: ["「敘」是敘舊，是放下手機後的深度對談", "「日」是時光，是歲月淬鍊出的滋味"],
});

const form = reactive({
  heroScenes: [emptyScene()],
  storyKicker: "",
  storyTitle: "",
  storyDescription: "",
  signatureKicker: "",
  signatureTitle: "",
  storeKicker: "",
  storeTitle: "",
  storeDescription: "",
  newsKicker: "",
  newsTitle: "",
  reservationKicker: "",
  reservationTitle: "",
  reservationDescription: "",
  featuredStoreIds: [],
  featuredNewsIds: [],
});

const unwrap = (response) => response.data?.data ?? response.data ?? [];

const selectedStores = computed(() =>
  stores.value.filter((store) => form.featuredStoreIds.includes(store.storeId)),
);

const selectedNews = computed(() =>
  newsItems.value.filter((item) => form.featuredNewsIds.includes(item.newsId)),
);

const previewScene = computed(() => form.heroScenes[0] || emptyScene());
const previewImage = computed(() => previewScene.value.imageUrl || imageMap[previewScene.value.imageKey] || homeImage);

const setMessage = (text) => {
  message.value = text;
  errorMessage.value = "";
};

const setError = (error, fallback) => {
  errorMessage.value = error?.response?.data?.message || fallback;
  message.value = "";
};

const assignForm = (setting) => {
  form.heroScenes = (setting.heroScenes?.length ? setting.heroScenes : [emptyScene()]).slice(0, 3).map((scene) => ({
    label: scene.label || "",
    imageKey: scene.imageKey || "home",
    imageUrl: scene.imageUrl || "",
    eyebrow: scene.eyebrow || "",
    title: scene.title || "",
    lines: scene.lines?.length ? [...scene.lines] : ["", ""],
  }));
  form.storyKicker = setting.storyKicker || "";
  form.storyTitle = setting.storyTitle || "";
  form.storyDescription = setting.storyDescription || "";
  form.signatureKicker = setting.signatureKicker || "";
  form.signatureTitle = setting.signatureTitle || "";
  form.storeKicker = setting.storeKicker || "";
  form.storeTitle = setting.storeTitle || "";
  form.storeDescription = setting.storeDescription || "";
  form.newsKicker = setting.newsKicker || "";
  form.newsTitle = setting.newsTitle || "";
  form.reservationKicker = setting.reservationKicker || "";
  form.reservationTitle = setting.reservationTitle || "";
  form.reservationDescription = setting.reservationDescription || "";
  form.featuredStoreIds = [...(setting.featuredStoreIds || [])];
  form.featuredNewsIds = [...(setting.featuredNewsIds || [])];
};

const loadStores = async () => {
  try {
    stores.value = unwrap(await api.get("/api/admin/stores"));
  } catch (error) {
    stores.value = [];
  }
};

const loadNews = async () => {
  try {
    newsItems.value = await newsApi.getAdminNews();
  } catch (error) {
    newsItems.value = [];
  }
};

const loadHomepage = async () => {
  loading.value = true;
  try {
    const setting = await homepageApi.getAdminHomepage();
    assignForm(setting);
  } catch (error) {
    setError(error, "無法載入首頁設定");
  } finally {
    loading.value = false;
  }
};

const addScene = () => {
  if (form.heroScenes.length >= 3) return;
  form.heroScenes.push({
    ...emptyScene(),
    label: form.heroScenes.length === 1 ? "旬味" : "吧台",
    imageKey: form.heroScenes.length === 1 ? "salmonSashimi" : "reservation",
  });
};

const removeScene = (index) => {
  if (form.heroScenes.length <= 1) return;
  form.heroScenes.splice(index, 1);
};

const toggleId = (list, id, limit) => {
  const index = list.indexOf(id);
  if (index >= 0) {
    list.splice(index, 1);
    return;
  }
  if (list.length >= limit) return;
  list.push(id);
};

const toPayload = () => ({
  ...form,
  heroScenes: form.heroScenes.map((scene) => ({
    label: scene.label,
    imageKey: scene.imageKey,
    imageUrl: scene.imageUrl || null,
    eyebrow: scene.eyebrow,
    title: scene.title,
    lines: scene.lines.filter((line) => line && line.trim()),
  })),
  featuredStoreIds: form.featuredStoreIds,
  featuredNewsIds: form.featuredNewsIds,
});

const saveHomepage = async () => {
  saving.value = true;
  try {
    const saved = await homepageApi.updateAdminHomepage(toPayload());
    assignForm(saved);
    setMessage("首頁設定已更新，前台會立即套用");
  } catch (error) {
    setError(error, "儲存首頁設定失敗");
  } finally {
    saving.value = false;
  }
};

onMounted(async () => {
  await Promise.all([loadStores(), loadNews(), loadHomepage()]);
});
</script>

<template>
  <section class="homepage-admin-page">
    <header class="page-heading">
      <div>
        <span class="eyebrow">Homepage Studio</span>
        <h1>首頁管理</h1>
        <p>控制官網第一屏、品牌文案、精選門市與最新消息。儲存後前台首頁會即時套用。</p>
      </div>
      <button class="primary-btn" type="button" :disabled="saving || loading" @click="saveHomepage">
        <i class="bx bx-save"></i>
        {{ saving ? "儲存中" : "儲存首頁" }}
      </button>
    </header>

    <div v-if="message" class="notice success">{{ message }}</div>
    <div v-if="errorMessage" class="notice danger">{{ errorMessage }}</div>

    <div class="workspace">
      <main class="editor-stack">
        <section class="panel">
          <div class="panel-head">
            <div>
              <span>Hero Scenes</span>
              <h2>第一屏情境</h2>
            </div>
            <button type="button" class="ghost-btn" :disabled="form.heroScenes.length >= 3" @click="addScene">
              <i class="bx bx-plus"></i>
              新增情境
            </button>
          </div>

          <div class="scene-list">
            <article v-for="(scene, index) in form.heroScenes" :key="index" class="scene-editor">
              <div class="scene-editor-head">
                <strong>情境 {{ index + 1 }}</strong>
                <button type="button" :disabled="form.heroScenes.length <= 1" @click="removeScene(index)">
                  <i class="bx bx-trash"></i>
                </button>
              </div>

              <div class="form-grid">
                <label>
                  <span>頁籤名稱</span>
                  <input v-model.trim="scene.label" type="text" maxlength="24" />
                </label>
                <label>
                  <span>英文小標</span>
                  <input v-model.trim="scene.eyebrow" type="text" maxlength="80" />
                </label>
                <label>
                  <span>主標</span>
                  <input v-model.trim="scene.title" type="text" maxlength="80" />
                </label>
                <label>
                  <span>內建圖片</span>
                  <select v-model="scene.imageKey">
                    <option v-for="option in imageOptions" :key="option.key" :value="option.key">
                      {{ option.label }}
                    </option>
                  </select>
                </label>
              </div>

              <label class="full-field">
                <span>外部圖片 URL（可空白，空白時使用內建圖片）</span>
                <input v-model.trim="scene.imageUrl" type="url" maxlength="500" placeholder="https://..." />
              </label>

              <div class="line-grid">
                <label v-for="lineIndex in 3" :key="lineIndex">
                  <span>文案第 {{ lineIndex }} 行</span>
                  <input v-model.trim="scene.lines[lineIndex - 1]" type="text" maxlength="80" />
                </label>
              </div>
            </article>
          </div>
        </section>

        <section class="panel">
          <div class="panel-head">
            <div>
              <span>Content Blocks</span>
              <h2>首頁文案</h2>
            </div>
          </div>

          <div class="content-grid">
            <label>
              <span>品牌故事小標</span>
              <input v-model.trim="form.storyKicker" type="text" maxlength="80" />
            </label>
            <label>
              <span>品牌故事標題</span>
              <input v-model.trim="form.storyTitle" type="text" maxlength="160" />
            </label>
            <label class="wide">
              <span>品牌故事內文</span>
              <textarea v-model.trim="form.storyDescription" rows="4" maxlength="700"></textarea>
            </label>

            <label>
              <span>菜單區小標</span>
              <input v-model.trim="form.signatureKicker" type="text" maxlength="80" />
            </label>
            <label>
              <span>菜單區標題</span>
              <input v-model.trim="form.signatureTitle" type="text" maxlength="160" />
            </label>
            <label>
              <span>門市區小標</span>
              <input v-model.trim="form.storeKicker" type="text" maxlength="80" />
            </label>
            <label>
              <span>門市區標題</span>
              <input v-model.trim="form.storeTitle" type="text" maxlength="160" />
            </label>
            <label class="wide">
              <span>門市區內文</span>
              <textarea v-model.trim="form.storeDescription" rows="3" maxlength="360"></textarea>
            </label>
            <label>
              <span>消息區小標</span>
              <input v-model.trim="form.newsKicker" type="text" maxlength="80" />
            </label>
            <label>
              <span>消息區標題</span>
              <input v-model.trim="form.newsTitle" type="text" maxlength="160" />
            </label>
            <label>
              <span>訂位區小標</span>
              <input v-model.trim="form.reservationKicker" type="text" maxlength="80" />
            </label>
            <label>
              <span>訂位區標題</span>
              <input v-model.trim="form.reservationTitle" type="text" maxlength="160" />
            </label>
            <label class="wide">
              <span>訂位區內文</span>
              <textarea v-model.trim="form.reservationDescription" rows="4" maxlength="700"></textarea>
            </label>
          </div>
        </section>

        <section class="panel">
          <div class="panel-head">
            <div>
              <span>Featured Content</span>
              <h2>精選內容</h2>
            </div>
          </div>

          <div class="pick-grid">
            <div>
              <h3>精選門市 <small>{{ form.featuredStoreIds.length }}/6</small></h3>
              <button
                v-for="store in stores"
                :key="store.storeId"
                type="button"
                :class="['pick-row', { selected: form.featuredStoreIds.includes(store.storeId) }]"
                @click="toggleId(form.featuredStoreIds, store.storeId, 6)"
              >
                <span>{{ store.city }} {{ store.district }}</span>
                <strong>{{ store.storeName }}</strong>
              </button>
            </div>

            <div>
              <h3>精選消息 <small>{{ form.featuredNewsIds.length }}/6</small></h3>
              <button
                v-for="item in newsItems"
                :key="item.newsId"
                type="button"
                :class="['pick-row', { selected: form.featuredNewsIds.includes(item.newsId) }]"
                @click="toggleId(form.featuredNewsIds, item.newsId, 6)"
              >
                <span>{{ item.categoryLabel || item.category }} / {{ item.statusLabel || item.status }}</span>
                <strong>{{ item.title }}</strong>
              </button>
            </div>
          </div>
        </section>
      </main>

      <aside class="preview-panel">
        <div class="preview-hero" :style="{ backgroundImage: `url(${previewImage})` }">
          <div>
            <span>{{ previewScene.eyebrow || "Xuri Washoku" }}</span>
            <h2>{{ previewScene.title || "敘日和食" }}</h2>
            <p>{{ previewScene.lines.filter(Boolean).join(" / ") }}</p>
          </div>
        </div>

        <div class="preview-summary">
          <span>目前精選</span>
          <strong>{{ selectedStores.length }} 間門市 / {{ selectedNews.length }} 則消息</strong>
          <p>精選排序會影響前台首頁的門市與消息優先顯示。</p>
        </div>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.homepage-admin-page {
  display: grid;
  gap: 22px;
}

.page-heading {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: end;
  border: 1px solid #eee0d4;
  border-radius: 18px;
  background: linear-gradient(135deg, #fff 0%, #fff7ef 100%);
  padding: 30px;
}

.eyebrow,
.panel-head span,
.preview-summary span {
  color: #b4774f;
  font-size: 12px;
  font-weight: 800;
}

.page-heading h1,
.panel-head h2 {
  margin: 6px 0 0;
  color: #2f2924;
  font-weight: 900;
}

.page-heading p {
  margin: 10px 0 0;
  color: #6d6259;
  line-height: 1.7;
}

.primary-btn,
.ghost-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 42px;
  border-radius: 8px;
  font-weight: 800;
}

.primary-btn {
  border: 1px solid #dca874;
  background: #dca874;
  color: #fff;
  padding: 0 18px;
}

.ghost-btn {
  border: 1px solid #e5d8cb;
  background: #fff;
  color: #8b5a38;
  padding: 0 14px;
}

.primary-btn:disabled,
.ghost-btn:disabled,
.scene-editor-head button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.notice {
  border-radius: 12px;
  padding: 12px 16px;
  font-weight: 800;
}

.notice.success {
  background: #ecfdf3;
  color: #137a3f;
}

.notice.danger {
  background: #fff2f0;
  color: #b42318;
}

.workspace {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 22px;
  align-items: start;
}

.editor-stack {
  display: grid;
  gap: 18px;
}

.panel,
.preview-panel {
  border: 1px solid #eee0d4;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(47, 41, 36, 0.04);
}

.panel {
  padding: 24px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.scene-list {
  display: grid;
  gap: 16px;
}

.scene-editor {
  border: 1px solid #efe3d8;
  border-radius: 14px;
  background: #fffaf5;
  padding: 18px;
}

.scene-editor-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.scene-editor-head strong {
  color: #2f2924;
  font-size: 16px;
}

.scene-editor-head button {
  border: 0;
  background: transparent;
  color: #b42318;
}

.form-grid,
.content-grid,
.line-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.line-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin-top: 14px;
}

.content-grid .wide,
.full-field {
  grid-column: 1 / -1;
}

label {
  display: grid;
  gap: 7px;
  color: #5f554d;
  font-weight: 800;
}

label span {
  font-size: 13px;
}

input,
select,
textarea {
  width: 100%;
  border: 1px solid #e1d4c8;
  border-radius: 8px;
  background: #fff;
  color: #2f2924;
  padding: 11px 12px;
  font: inherit;
}

textarea {
  resize: vertical;
}

.pick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.pick-grid h3 {
  display: flex;
  justify-content: space-between;
  margin: 0 0 10px;
  color: #2f2924;
  font-size: 18px;
}

.pick-grid small {
  color: #9b8f86;
}

.pick-row {
  display: grid;
  width: 100%;
  gap: 3px;
  margin-bottom: 8px;
  border: 1px solid #eaded2;
  border-radius: 10px;
  background: #fff;
  color: #2f2924;
  padding: 12px;
  text-align: left;
}

.pick-row.selected {
  border-color: #dca874;
  background: #fff6ec;
}

.pick-row span {
  color: #9a6a48;
  font-size: 12px;
  font-weight: 800;
}

.pick-row strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-panel {
  position: sticky;
  top: 24px;
  overflow: hidden;
}

.preview-hero {
  min-height: 360px;
  display: grid;
  align-items: end;
  background-position: center;
  background-size: cover;
  color: #fff;
}

.preview-hero > div {
  min-height: 360px;
  display: grid;
  align-content: end;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.04), rgba(0, 0, 0, 0.78));
  padding: 24px;
}

.preview-hero span {
  color: #dca874;
  font-size: 12px;
  font-weight: 800;
}

.preview-hero h2 {
  margin: 8px 0;
  font-size: 34px;
  font-weight: 900;
}

.preview-hero p {
  margin: 0;
  color: rgba(255, 255, 255, 0.82);
  line-height: 1.7;
}

.preview-summary {
  padding: 20px;
}

.preview-summary strong {
  display: block;
  margin: 8px 0;
  color: #2f2924;
}

.preview-summary p {
  margin: 0;
  color: #6d6259;
  line-height: 1.7;
}

@media (max-width: 1180px) {
  .workspace {
    grid-template-columns: 1fr;
  }

  .preview-panel {
    position: static;
  }
}

@media (max-width: 760px) {
  .page-heading,
  .panel-head {
    align-items: stretch;
    flex-direction: column;
  }

  .form-grid,
  .content-grid,
  .line-grid,
  .pick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
