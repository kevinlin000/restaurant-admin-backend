<script setup>
import { ref, computed as vueComputed, onMounted, onUnmounted, watch } from 'vue'
import axios from 'axios'

// 🤝 引進 Order 組同學提供的本機圖片
import tofuImg from "@/assets/images/tofu.jpg";
import seafoodSaladImg from "@/assets/images/seafood-salad.jpg";
import sashimiImg from "@/assets/images/sashimi.jpg";
import salmonSashimiImg from "@/assets/images/salmon-sashimi.jpg";
import sushiImg from "@/assets/images/sushi.jpg";
import aburiSalmonSushiImg from "@/assets/images/aburi-salmon-sushi.jpg";
import sukiyakiImg from "@/assets/images/sukiyaki.jpg";
import tempuraImg from "@/assets/images/tempura.jpg";
import matchaDessertImg from "@/assets/images/matcha-dessert.jpg";
import caramelPuddingImg from "@/assets/images/caramel-pudding.jpg";
import calpisImg from "@/assets/images/calpis.jpg";
import japaneseTeaImg from "@/assets/images/japanese-tea.jpg";
import asahiBeerImg from "@/assets/images/asahi-beer.jpg";
import japaneseSakeImg from "@/assets/images/japanese-sake.jpg";

// 🏪 1. 門市控制中心
const currentStoreId = ref(null)
const storeList = ref([])

// 🗂️ 2. 動態分類控制中心 (🎯 已移除所有死資料，完全由資料庫驅動)
const categoryList = ref([])
const currentCategory = ref(null) 

// 🍱 3. 菜單控制中心
const menuItems = ref([])

// 🌟 4. 幻燈片滾動漸隱控制
const heroOpacity = ref(1)

const handleScroll = () => {
  const scrollTop = window.scrollY
  const maxScroll = 1000 
  if (scrollTop <= maxScroll) {
    heroOpacity.value = 1 - (scrollTop / maxScroll)
  } else {
    heroOpacity.value = 0
  }
}

// 🎯 智慧型本機圖片自動對應管線
const getMenuItemImage = (item) => {
  if (item.imageUrl && (item.imageUrl.startsWith('http://') || item.imageUrl.startsWith('https://'))) {
    return item.imageUrl;
  }
  const name = item.itemName || '';
  if (name.includes('胡麻豆腐')) return tofuImg;
  if (name.includes('海鮮沙拉')) return seafoodSaladImg;
  if (name.includes('綜合生魚片')) return sashimiImg;
  if (name.includes('鮭魚刺身')) return salmonSashimiImg;
  if (name.includes('壽司盛合')) return sushiImg;
  if (name.includes('炙燒鮭魚')) return aburiSalmonSushiImg;
  if (name.includes('和牛壽喜燒') || name.includes('壽喜燒')) return sukiyakiImg;
  if (name.includes('天婦羅')) return tempuraImg;
  if (name.includes('抹茶')) return matchaDessertImg;
  if (name.includes('布丁') || name.includes('焦糖')) return caramelPuddingImg;
  if (name.includes('可爾必思')) return calpisImg;
  if (name.includes('日式綠茶') || name.includes('茶')) return japaneseTeaImg;
  if (name.includes('生啤酒') || name.includes('Asahi')) return asahiBeerImg;
  if (name.includes('清酒') || name.includes('吟釀')) return japaneseSakeImg;
  return 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=500&q=80';
}

// 🌟 智慧分類小圖標字典（動態匹配資料庫字眼）
const getCategoryIcon = (categoryName) => {
  if (!categoryName) return '🏮';
  if (categoryName.includes('前菜') || categoryName.includes('沙拉')) return '🥗';
  if (categoryName.includes('刺身') || categoryName.includes('生魚片')) return '🐟';
  if (categoryName.includes('壽司') || categoryName.includes('軍艦')) return '🍣';
  if (categoryName.includes('熟食') || categoryName.includes('熱食')) return '🍳';
  if (categoryName.includes('炸物') || categoryName.includes('揚物')) return '🍤';
  if (categoryName.includes('甜點') || categoryName.includes('甘味')) return '🍰';
  if (categoryName.includes('飲料') || categoryName.includes('水')) return '🥤';
  if (categoryName.includes('酒') || categoryName.includes('微醺')) return '🍺';
  return '🍱';
}

// 🌟 幻燈片大圖形象數據
const carouselImages = ref([
  { id: 1, url: 'https://images.unsplash.com/photo-1580822184713-fc5400e7fe10?auto=format&fit=crop&w=1200&q=80', title: '日式職人．感動嚴選', desc: '源自日本的美味，現點現做，為您奉上最溫慢的精緻定食' },
  { id: 3, url: 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=1200&q=80', title: '讚岐傳承．彈牙美味', desc: '純手工研磨湯頭與究極麵體，體驗醇厚純粹的日式風味' },
  { id: 2, url: 'https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?auto=format&fit=crop&w=1200&q=80', title: '經典和風．極致饗宴', desc: '嚴選頂級食材，搭配主廚特調醬汁，每一口都是道地和風魂' }
])

// 🔄 抓取後端真實門市清單
const fetchStores = async () => {
  try {
    const response = await axios.get('/api/menu-component/stores')
    const rawStores = response.data.data || response.data
    if (Array.isArray(rawStores) && rawStores.length > 0) {
      storeList.value = rawStores
      if (!currentStoreId.value) {
        currentStoreId.value = storeList.value[0].id
      }
    }
  } catch (error) {
    console.error('⚠️ 撈取門市失敗！', error)
  }
}

// 🗂️ 抓取所有真實分類 —— ⚡ 100% 自動對應並抓取資料庫 8 大分類
const fetchCategories = async () => {
  try {
    // 🎯 呼叫我們在後端蓋好的統一分類 API 管線
    const response = await axios.get('/api/menu-categories')
    
    // 🔍 自動解析組長規定封裝的 ApiResponse { success, message, data } 格式
    const rawCategories = response.data.data || response.data
    
    if (Array.isArray(rawCategories) && rawCategories.length > 0) {
      categoryList.value = rawCategories.map(cat => ({
        id: cat.id || cat.categoryId,
        name: cat.categoryName || cat.name  // 🎯 精準對齊資料庫對應的實體屬性名稱
      }))
      
      // 預設選中資料庫跑出來的第一個分類 ID
      if (categoryList.value.length > 0 && !currentCategory.value) {
        currentCategory.value = categoryList.value[0].id
      }
    }
  } catch (error) {
    console.error('⚠️ 撈取資料庫動態分類失敗，請確認 Java 後端程式是否啟動！', error)
  }
}

// 🍜 根據選擇的分店 ID，拉取專屬動態菜單
const fetchMenuData = async (storeId) => {
  if (!storeId) return
  try {
    const response = await axios.get(`/api/menu-items/store/${storeId}`)
    menuItems.value = response.data.data || response.data
  } catch (error) {
    console.error(`⚠️ 拉取分店菜單失敗！`, error)
    menuItems.value = [] 
  }
}

const handleStoreChange = () => { fetchMenuData(currentStoreId.value) }

watch(currentStoreId, (newStoreId) => { if (newStoreId) { fetchMenuData(newStoreId) } })

// 🔍 演算核心：現在完全與資料庫 ID 咬合，過濾絕對精準
const filteredMenu = vueComputed(() => {
  if (!Array.isArray(menuItems.value)) return []
  return menuItems.value.filter(item => Number(item.categoryId) === Number(currentCategory.value))
})

const addToCart = (item) => { alert(`🎉 成功將【${item.itemName}】加入購物車！`) }

onMounted(async () => {
  await fetchStores()
  await fetchCategories() // ⚡ 啟動全自動分類追蹤
  if (currentStoreId.value) { await fetchMenuData(currentStoreId.value) }

  window.addEventListener('scroll', handleScroll)

  setTimeout(() => {
    const carouselEl = document.getElementById('yayoiHeroCarousel')
    if (carouselEl && window.bootstrap) {
      new window.bootstrap.Carousel(carouselEl, { 
        interval: 3500, 
        ride: 'carousel', 
        pause: 'hover',
        wrap: true 
      })
    }
  }, 600)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="overflow-visible" style="min-height: 100vh; background-color: #fafafa;">
    <div style="height: 90px; width: 100%;"></div>
    
    <div id="yayoiHeroCarousel" class="carousel slide carousel-fade shadow-sm" data-bs-ride="carousel" style="position: fixed; top: 90px; left: 0; width: 100%; height: 420px; z-index: 1; overflow: hidden; will-change: opacity; transition: opacity 0.05s linear;" :style="{ opacity: heroOpacity }">
      <div class="carousel-indicators" style="z-index: 15;">
        <button type="button" data-bs-target="#yayoiHeroCarousel" data-bs-slide-to="0" class="active"></button>
        <button type="button" data-bs-target="#yayoiHeroCarousel" data-bs-slide-to="1"></button>
        <button type="button" data-bs-target="#yayoiHeroCarousel" data-bs-slide-to="2"></button>
      </div>
      <div class="carousel-inner h-100">
        <div v-for="(slide, index) in carouselImages" :key="slide.id" :class="['carousel-item h-100', index === 0 ? 'active' : '']">
          <div class="position-relative h-100 w-100">
            <img :src="slide.url" class="d-block w-100 h-100" style="object-fit: cover; filter: brightness(0.65);" :alt="slide.title">
            <div class="carousel-caption text-start" style="left: 8%; bottom: 25%; z-index: 20; max-width: 65%;">
              <h2 class="fw-bold text-white mb-2" style="font-size: 2.2rem; letter-spacing: 3px; text-shadow: 2px 4px 10px rgba(0,0,0,0.85), 0 0 6px rgba(0,0,0,0.6);">{{ slide.title }}</h2>
              <p class="fs-5 text-white-50 fw-light mb-0" style="font-size: 1.2rem; letter-spacing: 1.5px; color: rgba(255, 255, 255, 0.75) !important; text-shadow: 1px 2px 8px rgba(0,0,0,0.85);">{{ slide.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="main-content-wrapper position-relative" style="z-index: 10; background-color: #fafafa; margin-top: 420px; padding-top: 40px; padding-bottom: 100px;">
      <div class="container px-2">
        <div class="row g-4">
          
          <div class="col-md-3 mb-4">
            <div class="sticky-top" style="top: 110px; z-index: 90;">
              <div class="card shadow-sm mb-4 border-0" style="border-radius: 8px;">
                <div class="card-body p-3 bg-white" style="border-radius: 8px;">
                  <label class="form-label fw-bold text-secondary small mb-2">📍 請選擇您要查看的門市：</label>
                  <select v-model="currentStoreId" @change="handleStoreChange" class="form-select border-2 fw-bold select-store-style">
                    <option v-for="store in storeList" :key="store.id" :value="store.id">{{ store.name }}</option>
                  </select>
                </div>
              </div>

              <div class="list-group shadow-sm border-0 bg-white" style="border-radius: 12px; overflow: hidden;">
                <button 
                  v-for="cat in categoryList" 
                  :key="cat.id"
                  @click="currentCategory = cat.id" 
                  :class="['list-group-item list-group-item-action py-3 px-4 fw-bold border-0 border-bottom border-light-subtle d-flex align-items-center transition-all category-btn', currentCategory === cat.id ? 'yayoi-active' : 'text-secondary bg-white']"
                >
                  <span class="fs-5 me-3 icon-wrapper">{{ getCategoryIcon(cat.name) }}</span>
                  <span class="category-text-label">{{ cat.name }}</span>
                </button>
              </div>
            </div>
          </div>

          <div class="col-md-9">
            <div class="row row-cols-1 row-cols-md-2 g-4">
              <div class="col" v-for="item in filteredMenu" :key="item.id">
                <div class="card h-100 border-0 shadow-sm overflow-hidden hover-shadow bg-white transition-all" style="border-radius: 12px;">
                  <div class="position-relative overflow-hidden" style="height: 240px;">
                    <img :src="getMenuItemImage(item)" class="card-img-top h-100 w-100 transition-scale" style="object-fit: cover;" :alt="item.itemName">
                  </div>
                  <div class="card-body d-flex flex-column p-4">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                      <h5 class="card-title fw-bold mb-0" style="color: #374151;">{{ item.itemName }}</h5>
                      <span class="fs-4 fw-bold" style="color: #b45309;">
                        ${{ item.finalPrice || item.price || 0 }}
                      </span>
                    </div>
                    <p class="card-text small flex-grow-1 mb-3" style="line-height: 1.6; color: #6b7280;">{{ item.description }}</p>
                    <div v-if="item.allergenInfo" class="alert alert-warning py-1 px-2 mb-3 border-0 rounded-2 d-flex align-items-center bg-opacity-10" style="font-size: 0.75rem; color: #9a3412; background-color: #ffedd5;">
                      ⚠️ 過敏原提示：{{ item.allergenInfo }}
                    </div>
                    <button v-if="item.isSelectable !== false" @click="addToCart(item)" class="btn w-100 fw-bold mt-auto py-2 text-white border-0 shadow-sm yayoi-btn">
                      <i class="fa-solid fa-cart-plus me-1"></i>加入購物車
                    </button>
                    <button v-else class="btn btn-secondary w-100 fw-bold mt-auto py-2 border-0" style="border-radius: 6px; cursor: not-allowed;" disabled>
                      ❌ 已售罄 / 暫不供應
                    </button>
                  </div>
                </div>
              </div>
            </div>
            
            <div v-if="filteredMenu.length === 0" class="text-center py-5 text-muted border border-dashed rounded-3 bg-white shadow-sm mt-2">
              <div class="fs-1 mb-2">👨‍🍳</div>
              <div class="fw-bold" style="color: #4b5563;">該門市此系列品項正由主廚精製籌備中</div>
              <div class="small text-muted mt-1">敬請期待或切換其他分店！</div>
            </div>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.transition-all { transition: all 0.3s ease; }
.transition-scale { transition: transform 0.5s ease; }
.hover-shadow:hover { transform: translateY(-6px); box-shadow: 0 12px 20px rgba(0,0,0,0.06) !important; }
.hover-shadow:hover .transition-scale { transform: scale(1.04); }
.category-btn { letter-spacing: 0.8px; color: #4b5563 !important; font-weight: 650 !important; transition: all 0.25s ease-in-out; }
.category-btn:hover:not(.yayoi-active) { background-color: #fff7ed !important; color: #b45309 !important; }
.yayoi-active { background-color: #b45309 !important; background-image: linear-gradient(135deg, #cc7d24 0%, #b45309 100%) !important; color: #ffffff !important; font-weight: 700 !important; }
.category-text-label { color: inherit; }
.icon-wrapper { display: inline-block; transition: transform 0.2s ease; }
.category-btn:hover .icon-wrapper { transform: scale(1.15) rotate(5deg); }
.yayoi-btn { background-color: #ea580c; background-image: linear-gradient(135deg, #f97316 0%, #ea580c 100%); border-radius: 6px; }
.yayoi-btn:hover { opacity: 0.9; color: #ffffff; }
.select-store-style { border-color: #4a3728; cursor: pointer; }
.select-store-style:focus { border-color: #b45309; box-shadow: 0 0 0 0.25rem rgba(180, 83, 9, 0.25); }
</style>