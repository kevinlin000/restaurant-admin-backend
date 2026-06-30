<script setup>
import { ref, onMounted, onUnmounted, watch, computed as vueComputed } from 'vue'
import axios from 'axios'

// 🤝 1. 引進餐點列表本機圖片
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

// 🌟 EDM 圖片與內容配置 (同學未來只需修改這裡)
const edmConfig = ref({
  sashimi: {
    title: "極上生魚片盛合",
    desc: "嚴選每日直送頂級鮮味與極上鮭魚肚，主廚以精湛刀工完美留住海洋鮮甜。",
    price: "NT$ 480",
    imageUrl: "https://i.ibb.co/NdZthW9t/sashimi.png" 
  },
  pork: {
    title: "生薑燒肉定食",
    desc: "經典日式老薑風味醬汁，爆炒鮮嫩豬五花。肉質Q彈帶點微甜，是本舖最具人氣的靈魂定食。",
    price: "NT$ 290",
    imageUrl: "https://i.ibb.co/SXmQF02Z/Ginger-Braised-Pork-Set-Meal.jpg"
  }
});

// 🎛️ 2. 狀態控制中心
const showEdmModal = ref(true)      
const activeCategoryId = ref(null)  
const currentStoreId = ref(null)    
const storeList = ref([])          
const categoryList = ref([])        
const menuItems = ref([]) 
const selectedItem = ref(null)  // 記錄被點擊的餐點

const openItemModal = (item) => {
  selectedItem.value = item
}

const closeItemModal = () => {
  selectedItem.value = null
}

const heroOpacity = ref(1)          

// 🌟 3. 紀錄客人在畫面上點擊了哪一個行銷特色標籤
const selectedFeatureTag = ref(null)

// 🌟 4. 全端大圖動態管線：採用免 import 的超高畫質網路圖片防線，確保長度永遠固定為 3
const localFallbackBanners = [
  { id: 1, url: 'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=1200&q=80', title: '日式職人．感動嚴選', desc: '源自經典的精緻美味，現點現做，為您奉上最溫慢的道地定食饗宴' },
  { id: 2, url: 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=1200&q=80', title: '極致傳承．究極美味', desc: '嚴選新鮮食材與獨門研磨湯頭，帶給您純粹且多層次的和食體驗' },
  { id: 3, url: 'https://images.unsplash.com/photo-1611143669185-af224c5e3252?auto=format&fit=crop&w=1200&q=80', title: '旬味珍饌．手作壽司', desc: '每日海洋鮮甜直送，經職人掌心溫潤握製，完美綻放極致鮮味' }
]

const carouselImages = ref([...localFallbackBanners])

// 🎯 Vue 輪播核心控制器
const currentSlideIndex = ref(0)
let heroTimer = null

const startHeroAutoPlay = () => {
  stopHeroAutoPlay() 
  heroTimer = setInterval(() => {
    if (carouselImages.value.length > 0) {
      currentSlideIndex.value = (currentSlideIndex.value + 1) % carouselImages.value.length
    }
  }, 4500) 
}

const stopHeroAutoPlay = () => {
  if (heroTimer) { clearInterval(heroTimer); heroTimer = null; }
}

const handleIndicatorClick = (index) => {
  currentSlideIndex.value = index
  startHeroAutoPlay() 
}

// 🌟 5. 精準 Scrollspy 偵測與大圖透明度線性漸變
const handleWindowScroll = () => {
  const scrollTop = window.scrollY
  const fadeStart = 0
  const fadeEnd = 350 
  
  if (scrollTop <= fadeStart) { heroOpacity.value = 1 } 
  else if (scrollTop >= fadeEnd) { heroOpacity.value = 0 } 
  else { heroOpacity.value = 1 - (scrollTop - fadeStart) / (fadeEnd - fadeStart) }
  
  if (selectedFeatureTag.value) return; 
  
  const scrollPosition = window.scrollY + 280 
  for (const cat of categoryList.value) {
    const el = document.getElementById(`category-section-${cat.id}`)
    if (el) {
      const top = el.offsetTop
      const bottom = top + el.offsetHeight
      if (scrollPosition >= top && scrollPosition < bottom) {
        activeCategoryId.value = cat.id
        break
      }
    }
  }
}

// 🌟 6. 絲滑平滑滾動至指定分類區 (修正版)
const scrollToCategory = (categoryId) => {
  if (selectedFeatureTag.value) { selectedFeatureTag.value = null; }
  
  setTimeout(() => {
    const el = document.getElementById(`category-section-${categoryId}`)
    
    if (el) {
      const rect = el.getBoundingClientRect()
      const scrollTop = window.scrollY || document.documentElement.scrollTop
      const targetOffset = scrollTop + rect.top - 160
      
      window.scrollTo({ 
        top: targetOffset, 
        behavior: 'smooth' 
      })
      
      activeCategoryId.value = categoryId
    }
  }, 60)
}

// 🌟 7. 點擊特色膠囊滑動到動態大標題
const handleFeatureTagClick = (tag) => {
  if (selectedFeatureTag.value === tag) {
    selectedFeatureTag.value = null;
    return;
  }
  
  selectedFeatureTag.value = tag;
  activeCategoryId.value = null; 

  setTimeout(() => {
    const el = document.getElementById('dynamic-feature-title');
    if (el) {
      const targetOffset = el.getBoundingClientRect().top + window.scrollY;
      window.scrollTo({ 
        top: targetOffset - 160, 
        behavior: 'smooth' 
      });
    }
  }, 80);
}

// 🌟 8. 智慧型本機圖片自動對應管線
const getMenuItemImage = (item) => {
  if (item.imageUrl && (item.imageUrl.startsWith('http'))) return item.imageUrl;
  const name = (item.itemName || '').toLowerCase();
  
  if (name.includes('胡麻豆腐') || name.includes('豆腐') || name.includes('胡麻菠菜')) return tofuImg;
  if (name.includes('海鮮沙拉') || name.includes('沙拉') || name.includes('拌番茄') || name.includes('小黃瓜')) return seafoodSaladImg;
  if (name.includes('綜合生魚片') || name.includes('刺身盛合') || name.includes('生魚片')) return sashimiImg;
  if (name.includes('鮭魚刺身') || name.includes('鮭魚肚')) return salmonSashimiImg;
  if (name.includes('壽司盛合') || name.includes('壽司') || name.includes('加州卷')) return sushiImg;
  if (name.includes('炙燒鮭魚') || name.includes('厚蛋燒')) return aburiSalmonSushiImg;
  if (name.includes('壽喜燒') || name.includes('肉') || name.includes('生薑燒肉') || name.includes('雞肉')) return sukiyakiImg;
  if (name.includes('天婦羅') || name.includes('炸') || name.includes('豬排') || name.includes('可樂餅') || name.includes('炸蝦')) return tempuraImg;
  if (name.includes('抹茶') || name.includes('蕨餅')) return matchaDessertImg;
  if (name.includes('布丁') || name.includes('大福') || name.includes('紅豆')) return caramelPuddingImg;
  if (name.includes('可爾必思') || name.includes('氣氣飲')) return calpisImg;
  if (name.includes('茶')) return japaneseTeaImg;
  if (name.includes('生啤酒') || name.includes('啤酒')) return asahiBeerImg;
  if (name.includes('清酒') || name.includes('梅酒') || name.includes('大吟釀')) return japaneseSakeImg;
  return 'https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?auto=format&fit=crop&w=600&q=80';
}

// 🌟 9. 智慧分類小圖字典
const getCategoryIcon = (categoryName) => {
  if (!categoryName) return '🏮';
  if (categoryName.includes('前菜')) return '🥗';
  if (categoryName.includes('生魚片') || categoryName.includes('刺身')) return '🐟';
  if (categoryName.includes('壽司')) return '🍣';
  if (categoryName.includes('熟食') || categoryName.includes('定食')) return '🍳';
  if (categoryName.includes('炸物')) return '🍤';
  if (categoryName.includes('甜點') || categoryName.includes('甘味')) return '🍰';
  if (categoryName.includes('飲料')) return '🥤';
  if (categoryName.includes('酒')) return '🍺';
  return '🍱';
}

// 🔄 10. API 資料傳輸管線
const fetchHeroBanners = async () => {
  try {
    const response = await axios.get('/api/menu-banners')
    const backendData = response.data.data || response.data
    if (Array.isArray(backendData) && backendData.length > 0) {
      carouselImages.value = backendData.map((b, i) => ({
        id: b.id || i, url: b.imageUrl || b.url, title: b.title || '敘日嚴選', desc: b.description || b.desc || ''
      }))
    }
  } catch (error) { carouselImages.value = [...localFallbackBanners] }
}

const fetchStores = async () => {
  try {
    const response = await axios.get('/api/menu-component/stores')
    storeList.value = response.data.data || response.data
    if (storeList.value.length > 0 && !currentStoreId.value) currentStoreId.value = storeList.value[0].id
  } catch (error) { console.error('門市載入失敗', error) }
}

const fetchCategories = async () => {
  try {
    const response = await axios.get('/api/menu-categories')
    const raw = response.data.data || response.data
    categoryList.value = raw.map(cat => ({ id: cat.id || cat.categoryId, name: cat.categoryName || cat.name }))
    if (categoryList.value.length > 0) activeCategoryId.value = categoryList.value[0].id
  } catch (error) { console.error('分類載入失敗', error) }
}

const fetchMenuData = async (storeId) => {
  if (!storeId) return
  try {
    const response = await axios.get(`/api/menu-items/store/${storeId}`)
    menuItems.value = (response.data.data || response.data).map(item => {
      if (item.categoryId === undefined) item.categoryId = item.category_id;
      return item;
    })
  } catch (error) { console.error('菜單載入失敗', error); menuItems.value = [] }
}

// ✅ 統一的 featureTags 解析函式
const parseFeatureTags = (tags) => {
  if (!tags) return []
  if (Array.isArray(tags)) return tags.map(t => cleanTagText(t)).filter(Boolean)
  if (typeof tags === 'string') {
    try {
      const parsed = JSON.parse(tags)
      return Array.isArray(parsed) 
        ? parsed.map(t => cleanTagText(t)).filter(Boolean) 
        : []
    } catch {
      return tags.split(',').map(t => cleanTagText(t)).filter(Boolean)
    }
  }
  return []
}

const cleanTagText = (tag) => { if (!tag) return ''; return String(tag).replace(/[\[\]"']/g, '').trim(); }

// 🌟 11. 特色膠囊智慧清洗：精準對齊官方定義的 10 個黃金特色標籤
const activeAvailableTags = vueComputed(() => {
  const allowedTags = ["主廚推薦", "手作工法", "人氣爆棚", "鮮味極致", "經典必點", "職人精神", "嚴選食材", "季節限定", "極致奢華", "限量供應"]
  if (!Array.isArray(menuItems.value)) return []
  const tagCounts = {}
  
  menuItems.value.forEach(item => {
  const tags = parseFeatureTags(item.featureTags)
  tags.forEach(tag => {
  if (tag && allowedTags.includes(tag)) {
    tagCounts[tag] = (tagCounts[tag] || 0) + 1
    }
  })
})
  return allowedTags.filter(tag => tagCounts[tag] > 0 || selectedFeatureTag.value === tag)
})

// 🌟 12. 雙重過濾與總品項計算
const getFilteredItemsByCategory = (catId) => {
  let items = menuItems.value.filter(item => Number(item.categoryId || item.category_id) === Number(catId))
  if (selectedFeatureTag.value) {
    items = items.filter(item => {
      return parseFeatureTags(item.featureTags).includes(selectedFeatureTag.value)
    });
  }
  return items
}

const totalFilteredItemsCount = vueComputed(() => {
  if (!selectedFeatureTag.value) return 0;
  let count = 0;
  categoryList.value.forEach(cat => { count += getFilteredItemsByCategory(cat.id).length; });
  return count;
})

const addToCart = (item) => { alert(`🎉 成功將【${item.itemName}】加入購物車！`) }

watch(currentStoreId, (newStoreId) => { if (newStoreId) fetchMenuData(newStoreId) })

onMounted(async () => {
  await fetchHeroBanners(); 
  await fetchStores(); 
  await fetchCategories();
  if (currentStoreId.value) await fetchMenuData(currentStoreId.value)
  window.addEventListener('scroll', handleWindowScroll)
  startHeroAutoPlay() 
})

onUnmounted(() => { 
  window.removeEventListener('scroll', handleWindowScroll);
  stopHeroAutoPlay() 
})
</script>

<template>
  <div class="menu-root">
    
    <div v-if="showEdmModal" class="edm-overlay">
      <div class="edm-container shadow-lg">
        <button class="edm-close-btn" @click="showEdmModal = false">✖</button>
        <div id="edmCarousel" class="carousel slide h-100" data-bs-ride="carousel">
          <div class="carousel-inner h-100">
            <div class="carousel-item active h-100">
              <div class="row g-0 h-100">
                <div class="col-md-5 edm-text-section p-5 d-flex flex-column justify-content-center">
                  <span class="edm-tag">SUMMER SPECIAL</span>
                  <h2 class="edm-title">{{ edmConfig.sashimi.title }}</h2>
                  <div class="edm-divider"></div>
                  <p class="edm-desc">{{ edmConfig.sashimi.desc }}</p>
                  <div class="edm-price-container">
                    <span class="edm-price">{{ edmConfig.sashimi.price }}</span>
                  </div>
                  <div class="d-flex align-items-center mt-auto pt-4">
                    <img src="@/assets/images/logo.png" alt="敘日 logo" style="width: 70px; height: 70px; object-fit: contain; margin-right: 14px;">
                    <div>
                      <div style="font-size: 1.4rem; font-weight: 700; color: #2d2a2a; margin-bottom: 2px;">敘日</div>
                      <div style="font-size: 0.9rem; font-weight: 500; color: #6b7280; letter-spacing: 0.15rem;">CHEF COUNTER</div>
                    </div>
                  </div>
                </div>
                <div class="col-md-7 h-100">
                  <img :src="edmConfig.sashimi.imageUrl" class="edm-img" alt="生魚片">
                </div>
              </div>
            </div>
            <div class="carousel-item h-100">
              <div class="row g-0 h-100">
                <div class="col-md-7 h-100">
                  <img :src="edmConfig.pork.imageUrl" class="edm-img" alt="生薑燒肉">
                </div>
                <div class="col-md-5 edm-text-section p-5 d-flex flex-column justify-content-center">
                  <span class="edm-tag">POPULAR NO.1</span>
                  <h2 class="edm-title">{{ edmConfig.pork.title }}</h2>
                  <div class="edm-divider"></div>
                  <p class="edm-desc">{{ edmConfig.pork.desc }}</p>
                  <div class="edm-price-container">
                    <span class="edm-price">{{ edmConfig.pork.price }}</span>
                  </div>
                  <div class="d-flex align-items-center mt-auto pt-4">
                    <img src="@/assets/images/logo.png" alt="敘日 logo" style="width: 70px; height: 70px; object-fit: contain; margin-right: 14px;">
                    <div>
                      <div style="font-size: 1.4rem; font-weight: 700; color: #2d2a2a; margin-bottom: 2px;">敘日</div>
                      <div style="font-size: 0.9rem; font-weight: 500; color: #6b7280; letter-spacing: 0.15rem;">CHEF COUNTER</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <button class="carousel-control-prev" type="button" data-bs-target="#edmCarousel" data-bs-slide="prev">
            <span class="carousel-control-prev-icon" aria-hidden="true"></span>
          </button>
          <button class="carousel-control-next" type="button" data-bs-target="#edmCarousel" data-bs-slide="next">
            <span class="carousel-control-next-icon" aria-hidden="true"></span>
          </button>
        </div>
      </div>
    </div>

    <!-- 餐點詳情 Modal -->
    <div v-if="selectedItem" class="item-modal-overlay" @click.self="closeItemModal">
      <div class="item-modal-container">
        <button class="item-modal-close" @click="closeItemModal">✖</button>
        <div class="item-modal-img-wrapper">
          <img :src="getMenuItemImage(selectedItem)" alt="餐點圖片">
        </div>
        <div class="item-modal-body">
          <div class="d-flex justify-content-between align-items-start mb-3">
            <h3 class="fw-bold text-dark mb-0">{{ selectedItem.itemName }}</h3>
            <span class="price-text fs-4">${{ selectedItem.price }}</span>
          </div>
          <p class="text-muted mb-3" style="line-height: 1.8;">{{ selectedItem.description }}</p>
          <div class="d-flex flex-wrap gap-2 mb-3">
            <template v-for="tag in parseFeatureTags(selectedItem.featureTags)">
              <span v-if="cleanTagText(tag) && ['主廚推薦', '手作工法', '人氣爆棚', '鮮味極致', '經典必點', '職人精神', '嚴選食材', '季節限定', '極致奢華', '限量供應'].includes(cleanTagText(tag))" :key="tag" class="badge-feature">{{ cleanTagText(tag) }}</span>
            </template>
          </div>
          <div v-if="selectedItem.allergenInfo" class="mb-4" style="font-size: 13px; color: #78350f;">
            🔸 本產品含{{ selectedItem.allergenInfo }}
          </div>
          <button @click="addToCart(selectedItem); closeItemModal()" class="btn yayoi-btn-primary w-100 fw-bold py-2" :disabled="selectedItem.isSelectable === false">
            {{ selectedItem.isSelectable === false ? '暫不供應' : '加入購物車' }}
          </button>
        </div>
      </div>
    </div>

    <div id="mainHeroCarousel" class="custom-vue-hero-container" :style="{ opacity: heroOpacity }">
      <div class="carousel-indicators custom-hero-indicators">
        <button v-for="(slide, index) in carouselImages" :key="'indicator-' + slide.id" type="button" :class="{ active: index === currentSlideIndex }" @click="handleIndicatorClick(index)"></button>
      </div>
      <div class="carousel-inner h-100">
        <div v-for="(slide, index) in carouselImages" :key="slide.id" :class="['carousel-item h-100 carousel-fade-item', index === currentSlideIndex ? 'active' : '']">
          <div class="carousel-fade-wrapper">
            <div class="hero-overlay"></div>
            <img :src="slide.url" class="d-block w-100 h-100 object-fit-cover" alt="形象圖">
            <div class="carousel-caption hero-text-box animate__animated animate__fadeInUp">
              <h2 class="fw-bold text-white">{{ slide.title }}</h2>
              <div class="hero-divider"></div>
              <p class="text-white fw-light">{{ slide.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="main-content-wrapper">
      <div class="container">
        <div class="row">

          <!-- 左側導覽 -->
          <div class="col-md-3">
            <div class="sticky-top" style="top: 110px; z-index: 20;">

              <div class="card shadow-sm mb-4 border-0 rounded-3" style="border-radius: 12px !important; overflow: hidden;">
                <div class="card-body p-3 bg-white">
                  <label class="form-label fw-bold text-secondary small">📍 當前查看門市：</label>
                  <select v-model="currentStoreId" class="form-select border-2 fw-bold store-select">
                    <option v-for="store in storeList" :key="store.id" :value="store.id">{{ store.name }}</option>
                  </select>
                </div>
              </div>

              <div style="max-height: calc(100vh - 145px); overflow-y: auto; scrollbar-width: none; -webkit-overflow-scrolling: touch;">
                <div class="list-group shadow-sm border-0 bg-white rounded-3 overflow-hidden">
                  <button
                    v-for="cat in categoryList" :key="cat.id" @click="scrollToCategory(cat.id)"
                    :class="['list-group-item list-group-item-action py-3 px-4 fw-bold border-0 border-bottom d-flex align-items-center sidebar-item', activeCategoryId === cat.id ? 'yayoi-active' : 'text-secondary bg-white']"
                  >
                    <span class="fs-5 me-3">{{ getCategoryIcon(cat.name) }}</span>
                    <span>{{ cat.name }}</span>
                  </button>
                </div>
              </div>

            </div>
          </div>
          <!-- 左側導覽結束 -->

          <!-- 右側內容 -->
          <div class="col-md-9">
            <div v-if="activeAvailableTags.length > 0" class="mb-4 d-flex flex-wrap gap-2 align-items-center bg-white p-3 rounded-3 shadow-sm border-0">
              <span class="fw-bold text-secondary small me-2">🔍 快速過濾：</span>
              <button @click="selectedFeatureTag = null" :class="['btn tag-pill', !selectedFeatureTag ? 'active' : '']">全部特色</button>
              <button v-for="tag in activeAvailableTags" :key="tag" @click="handleFeatureTagClick(tag)" :class="['btn tag-pill', selectedFeatureTag === tag ? 'active' : '']">{{ tag }}</button>
            </div>

            <div id="dynamic-feature-title" v-if="selectedFeatureTag" class="alert alert-warning border-0 shadow-sm p-4 rounded-3 mb-4 d-flex justify-content-between align-items-center bg-white border-start border-4 border-warning">
              <div>
                <span class="badge bg-warning text-dark px-2 py-1 small mb-1 fw-bold">FEATURE SHOWCASE</span>
                <h4 class="fw-bold mb-0 text-dark">【{{ selectedFeatureTag }}】</h4>
              </div>
              <span class="fs-5 fw-bold text-secondary">共 <span class="text-warning fs-3">{{ totalFilteredItemsCount }}</span> 品</span>
            </div>

            <div class="menu-sections-container">
              <template v-for="cat in categoryList" :key="cat.id">
                <div v-if="getFilteredItemsByCategory(cat.id).length > 0" :id="'category-section-' + cat.id" class="category-section-block mb-5 animate__animated animate__fadeIn">
                  <h3 v-if="!selectedFeatureTag" class="fw-bold mb-4 border-start border-4 border-warning ps-3 text-dark">{{ cat.name }}</h3>
                  <div class="row row-cols-1 row-cols-md-2 g-4">
                    <div class="col" v-for="item in getFilteredItemsByCategory(cat.id)" :key="item.id">
                      <div class="card h-100 border-0 shadow-sm item-card" :class="{ 'sold-out': item.isSelectable === false || item.is_active === 0 }" @click="openItemModal(item)" style="cursor: pointer;">
                        <div class="card-img-wrapper">
                          <img :src="getMenuItemImage(item)" class="card-img-top" alt="餐點圖片">
                          <div v-if="item.isSelectable === false || item.is_active === 0" class="sold-out-overlay"><span>已售罄</span></div>
                        </div>
                        <div class="card-body p-4 d-flex flex-column">
                          <div v-if="selectedFeatureTag" class="mb-2"><span class="badge bg-light text-secondary border px-2 py-1 small fw-normal">{{ cat.name }}系列</span></div>
                          <div class="d-flex justify-content-between mb-2">
                            <h5 class="fw-bold mb-0 text-dark">{{ item.itemName }}</h5>
                            <span class="price-text">${{ item.price }}</span>
                          </div>
                          <p class="text-muted small mb-3 flex-grow-1" style="line-height: 1.6;">{{ item.description }}</p>
                          <div class="mb-2 d-flex flex-wrap gap-1 align-items-center" style="min-height: 26px;">
                            <template v-for="tag in parseFeatureTags(item.featureTags)">
                              <span v-if="cleanTagText(tag) && ['主廚推薦', '手作工法', '人氣爆棚', '鮮味極致', '經典必點', '職人精神', '嚴選食材', '季節限定', '極致奢華', '限量供應'].includes(cleanTagText(tag))" :key="tag" class="badge-feature">{{ cleanTagText(tag) }}</span>
                            </template>
                          </div>
                          <div v-if="item.allergenInfo || item.allergen_info" class="mb-3 d-flex align-items-center gap-1 mt-2" style="font-size: 11px; font-weight: 400; letter-spacing: 0.1px;">
                            <span style="color: #78350f; opacity: 0.8;">🔸 本產品含{{ item.allergenInfo || item.allergen_info }}</span>
                          </div>
                          <button @click="addToCart(item)" class="btn yayoi-btn-primary w-100 fw-bold py-2 mt-auto" :disabled="item.isSelectable === false || item.is_active === 0">
                            {{ (item.isSelectable === false || item.is_active === 0) ? '暫不供應' : '加入購物車' }}
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </div>

            <div v-if="menuItems.length > 0 && selectedFeatureTag && totalFilteredItemsCount === 0" class="text-center py-5 bg-white rounded shadow-sm border border-dashed mt-4">
              <div class="fs-2 mb-2">🔍</div>
              <p class="fw-bold text-secondary mb-1">區域內找不到符合「{{ selectedFeatureTag }}」的菜色</p>
              <button @click="selectedFeatureTag = null" class="btn btn-sm btn-outline-warning mt-2 fw-bold">重設篩選</button>
            </div>
          </div>
          <!-- 右側內容結束 -->

        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* ================= 原有的 EDM 樣式 (保留) ================= */
.edm-overlay {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background-color: rgba(0, 0, 0, 0.85); z-index: 99999;
  display: flex; justify-content: center; align-items: center; backdrop-filter: blur(8px);
}
.edm-container { position: relative; width: 85%; height: 75%; max-width: 1100px; background: #f7f5f0; border-radius: 16px; overflow: hidden; }
.edm-close-btn { position: absolute; top: 15px; right: 15px; z-index: 100; background: rgba(0,0,0,0.5); color: white; border: 1px solid white; border-radius: 50%; width: 40px; height: 40px; cursor: pointer; transition: 0.3s; }
.edm-close-btn:hover { background: #b22222; transform: rotate(90deg); }
.edm-tag { font-size: 12px; color: #8b7355; font-weight: bold; letter-spacing: 2px; }
.edm-title { font-size: 36px !important; font-weight: 800 !important; color: #1f2937 !important; }
.edm-divider { width: 50px; height: 3px; background: #8b7355; margin: 15px 0; }
.edm-desc { font-size: 15px; line-height: 1.8; color: #555; }
.edm-price { font-size: 28px; font-weight: 700; color: #b22222; display: block; }
.edm-img { width: 100%; height: 100%; object-fit: cover; }

/* ================= Hero Section (職人風格優化) ================= */
.custom-vue-hero-container {
  position: fixed; top: 90px; left: 0; width: 100%; height: 460px; z-index: 1; 
}

/* 🌟 漸層遮罩優化：解決圖片過亮、大幅增加文字的可讀性與高級感 */
.hero-overlay {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.2) 0%, rgba(0, 0, 0, 0.65) 100%);
  z-index: 2;
}

/* 修改這個既有的 class，加入 transition */
.carousel-fade-item {
  position: absolute !important; 
  top: 0; left: 0; width: 100%; height: 100%; 
  opacity: 0 !important; 
  transition: opacity 1.2s ease-in-out !important; /* 這是淡入淡出的關鍵 */
  pointer-events: none; /* 讓未選中的圖層不影響點擊 */
}

/* 當有 active 時，顯示該圖片 */
.carousel-fade-item.active { 
  opacity: 1 !important; 
  z-index: 1 !important; 
}

/* 🌟 文字佈局優化：調整至中下方 (top: 65%)，改用 translate 精準置中 */
.carousel-caption { 
  z-index: 10 !important; 
  text-align: center !important;
  left: 50% !important; 
  top: 65% !important;
  transform: translate(-50%, -50%) !important;    
  width: 100%;
  padding: 0 20px;
}

/* 🌟 主標題優化：加粗、加大、並拉開字距展現職人內斂感 */
.carousel-caption h2 { 
  font-size: 2.6rem !important; 
  font-weight: 800 !important; 
  color: #ffffff !important; 
  letter-spacing: 0.18rem !important; 
  margin-bottom: 18px !important;
  text-shadow: none !important; /* 捨棄厚重陰影，改靠遮罩襯托 */
}

/* 🌟 置中極細線裝飾點綴 */
.hero-divider { 
  width: 50px; 
  height: 1.5px; 
  background-color: rgba(255, 255, 255, 0.85); 
  margin: 0 auto 18px auto; 
}

/* 🌟 副標題優化：輕量字體與微調字距，呈現呼吸空氣感 */
.carousel-caption p { 
  font-size: 1.15rem !important; 
  color: #ffffff !important;
  font-weight: 300 !important;
  letter-spacing: 0.05rem !important;
  opacity: 0.95;
  text-shadow: none !important;
}

/* 確保漸變層覆蓋整個區塊 */
.carousel-fade-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}

/* 核心：強行覆蓋 Bootstrap 的 display 設定 */
#mainHeroCarousel .carousel-fade-item {
  display: block !important; 
  opacity: 0;
  transition: opacity 1.2s ease-in-out !important;
  position: absolute;
  top: 0;
  left: 0;
}

/* 核心：啟用淡入效果 */
#mainHeroCarousel .carousel-fade-item.active {
  opacity: 1 !important;
}

/* 優化後的卡片樣式：增加呼吸感與質感 */
.item-card {
  border-radius: 16px !important;       /* 加大圓角，讓視覺更柔和 */
  background-color: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.05); /* 加入極淡邊框，增加立體層次 */
  overflow: hidden;                     /* 確保內容不會溢出圓角 */
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1); /* 改用更平滑的動畫曲線 */
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05); /* 靜止時給予極淡的基礎陰影 */
}

/* 滑鼠懸浮時的效果：模擬浮起感 */
.item-card:hover {
  transform: translateY(-8px);          /* 調整移動距離，更明顯的懸浮感 */
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04) !important; 
}

/* --- 新增：高質感分類標題 (呼吸感與層次提升) --- */
/* 修改為適應文字寬度 */
/* --- 優化後的分類標題樣式 --- */
.category-section-block h3 {
  font-size: 1.75rem !important;
  color: #2d2a2a !important; 
  font-weight: 800 !important;
  
  /* 改用 block + margin 來撐開空間，不再使用 ::after */
  display: block !important; 
  width: fit-content !important; /* 寬度自動貼合文字 */
  
  /* 透過 border-bottom 畫線，這最穩定且不會消失 */
  padding-bottom: 8px !important;
  margin-bottom: 24px !important;
  border-bottom: 3px solid #b45309 !important; /* 直接給予顏色與厚度 */
}

/* 滑鼠滑過時延伸到 100% */
.category-section-block:hover h3::after {
  transform: scaleX(1); 
}

/* 懸浮效果：讓分類標題在視覺上更靈動 */
.category-section-block:hover h3::after {
  width: 80px;
}

/* 圖片區塊優化 */
.card-img-wrapper {
  height: 220px;
  overflow: hidden;
  position: relative;
}

.card-img-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover !important;
  transition: transform 0.6s ease;     /* 減慢縮放速度，更有質感 */
}

/* 當卡片 hover 時，圖片稍微放大，增加視覺互動性 */
.item-card:hover .card-img-wrapper img {
  transform: scale(1.05);
}

/* ================= 其餘元件設定 (全部保留) ================= */
.custom-hero-indicators {
  position: absolute !important; z-index: 50 !important; bottom: 85px !important; 
  left: 0 !important; right: 0 !important; display: flex !important; justify-content: center !important;
  margin: 0 !important; list-style: none !important; pointer-events: auto !important; 
}
.custom-hero-indicators button {
  pointer-events: auto !important; width: 24px !important; height: 3px !important; 
  border-radius: 20px !important; margin: 0 4px !important; 
  background-color: rgba(255, 255, 255, 0.25) !important; border: none !important;
  border-top: 15px solid transparent !important; border-bottom: 15px solid transparent !important; 
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1) !important; cursor: pointer !important;
}
.custom-hero-indicators button.active { background-color: rgba(92, 64, 51, 0.7) !important; width: 36px !important; }

.main-content-wrapper { position: relative; z-index: 10; background-color: #fafafa !important; margin-top: 460px; padding-top: 25px; padding-bottom: 100px; }
.menu-sections-container { background-color: #fafafa; border-radius: 12px; padding: 20px; }
.sidebar-item { 
  transition: all 0.2s ease; 
  font-size: 15px;
  letter-spacing: 0.05rem;
  font-weight: 500;
  color: #3d2b1f;
}
.sidebar-item:hover:not(.yayoi-active) { 
  background-color: #fff7ed !important; 
  color: #b45309 !important; 
  padding-left: 1.5rem !important; 
}
.yayoi-active { background-color: #b45309 !important; color: white !important; border-left: 5px solid #ffc107 !important; padding-left: 1.5rem !important; box-shadow: 0 4px 10px rgba(180, 83, 9, 0.3); }
.store-select:focus { border-color: #b45309; box-shadow: 0 0 0 0.25rem rgba(180, 83, 9, 0.2); }
.price-text { color: #b45309; font-size: 1.4rem; font-weight: 800; }
.badge-feature { background: #fdf2e9; color: #ca8a04; border: 1px solid #fef08a; padding: 4px 12px; border-radius: 6px; font-size: 12px; font-weight: 600; }
.yayoi-btn-primary { background: linear-gradient(135deg, #f97316, #ea580c); color: white; border: none; border-radius: 6px; }

.tag-pill { 
  border: 1px solid #e5e7eb; 
  border-radius: 6px !important; 
  padding: 6px 14px; 
  font-size: 13px; 
  font-weight: 500; 
  color: #5c4033; 
  background-color: #ffffff; 
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.tag-pill::after {
  content: '';
  position: absolute;
  bottom: 4px;
  left: 14px;
  right: 14px;
  height: 2px;
  background: linear-gradient(90deg, #fbbf24, #fed7aa);
  border-radius: 2px;
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0.8;
}

.tag-pill:hover { 
  border-color: #fdba74; 
  color: #c2410c; 
  background-color: #fff7ed; 
  transform: translateY(-1px); 
}

.tag-pill:hover::after {
  transform: scaleX(1);
}

.tag-pill.active { 
  background: linear-gradient(135deg, #fff7ed, #ffedd5) !important; 
  border-color: #b45309 !important; 
  color: #78350f !important; 
  font-weight: 600; 
  box-shadow: 0 4px 12px rgba(180, 83, 9, 0.15) !important; 
}

.tag-pill.active::after {
  transform: scaleX(1);
}

.list-group::-webkit-scrollbar {
  display: none;
}

/*加上資訊卡*/
.item-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.item-modal-container {
  background: #ffffff;
  border-radius: 16px;
  width: 90%;
  max-width: 480px;
  max-height: 85vh;
  overflow-y: auto;
  position: relative;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: modalFadeIn 0.25s ease;
}

.item-modal-close {
  position: absolute;
  top: 12px;
  right: 16px;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  font-size: 14px;
  cursor: pointer;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.item-modal-close:hover {
  background: #fff7ed;
  color: #b45309;
}

.item-modal-img-wrapper {
  width: 100%;
  height: 260px;
  overflow: hidden;
  border-radius: 16px 16px 0 0;
}

.item-modal-img-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-modal-body {
  padding: 24px;
}

@keyframes modalFadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.sold-out { opacity: 0.65; }
.sold-out-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); display: flex; justify-content: center; align-items: center; }
.sold-out-overlay span { background: rgba(0,0,0,0.8); color: #fff; padding: 5px 15px; border-radius: 20px; font-size: 13px; font-weight: bold; }
.object-fit-cover { object-fit: cover; }
.brightness-50 { filter: brightness(0.5); }
.shadow-text { text-shadow: 2px 2px 8px rgba(0,0,0,0.8); }
</style>