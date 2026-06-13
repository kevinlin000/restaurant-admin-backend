<script setup>
import { ref, computed as vueComputed, onMounted } from 'vue'

// 1. 準備裝從 Java 運過來的美味菜色盒子
const menuItems = ref([])
const currentCategory = ref(1)

// 🌟 2. 幻燈片大圖形象數據（模擬彌生軒的高級宣傳大圖）
const carouselImages = ref([
  { id: 1, url: 'https://images.unsplash.com/photo-1580822184713-fc5400e7fe10?auto=format&fit=crop&w=1200&q=80', title: '日式職人．感動嚴選', desc: '源自日本的美味，現點現做，為您奉上最溫暖的精緻定食' },
  { id: 3, url: 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=1200&q=80', title: '讚岐傳承．彈牙美味', desc: '純手工研磨湯頭與究極麵體，體驗醇厚純粹的日式風味' },
  { id: 2, url: 'https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?auto=format&fit=crop&w=1200&q=80', title: '經典和風．極致饗宴', desc: '嚴選頂級食材，搭配主廚特調醬汁，每一口都是道地和風魂' }
])

// 3. 網頁開機立刻發動接線
onMounted(() => {
  // 📥 A. 去 Java 後端撈菜單數據
  fetch('http://localhost:8080/api/menu-items')
    .then(response => response.json())
    .then(res => {
      const rawData = res.data || res
      if (Array.isArray(rawData)) {
        menuItems.value = rawData
        console.log('前台成功連線！已裝載菜單數量：', menuItems.value.length)
      }
    })
    .catch(error => console.error('前台敲門連線失敗：', error))

  // 🎯 B.大圖自動輪播點火核心！
  // 用原生 JS 抓住我們 template 裡面的 carousel 盒子，強制初始化並啟動自動輪播
  setTimeout(() => {
    const carouselEl = document.getElementById('yayoiHeroCarousel')
    if (carouselEl && window.bootstrap) {
      // 強制設定：每 4000 毫秒（4秒）換一張圖，並且滑鼠移過去時不要暫停（ride: 'carousel'）
      new window.bootstrap.Carousel(carouselEl, {
        interval: 4000,
        ride: 'carousel',
        wrap: true
      })
      console.log('🚀 彌生軒職人大圖自動輪播系統：成功點火！')
    }
  }, 500) // 延遲半秒等 DOM 完全長好，最安全穩健
})

// 🚀 精準對齊後台修改好的純布林值 isActive 規格！
const filteredMenu = vueComputed(() => {
  return menuItems.value.filter(item => {
    return item.categoryId === currentCategory.value && 
           (item.isActive === true || item.isActive === 'true' || item.isActive == 1)
  })
})
</script>

<template>
  <div class="overflow-visible bg-neutral-50" style="min-height: 100vh; background-color: #fafafa;">
    
    <div style="height: 90px; width: 100%;"></div>

    <div id="yayoiHeroCarousel" class="carousel slide carousel-fade shadow-sm mb-5" data-bs-ride="carousel" style="border-radius: 12px; overflow: hidden;">
      <div class="carousel-indicators">
        <button type="button" data-bs-target="#yayoiHeroCarousel" data-bs-slide-to="0" class="active"></button>
        <button type="button" data-bs-target="#yayoiHeroCarousel" data-bs-slide-to="1"></button>
        <button type="button" data-bs-target="#yayoiHeroCarousel" data-bs-slide-to="2"></button>
      </div>
      
      <div class="carousel-inner">
        <div v-for="(slide, index) in carouselImages" :key="slide.id" :class="['carousel-item', index === 0 ? 'active' : '']" data-bs-interval="4000">
          <div class="position-relative" style="height: 400px;">
            <img :src="slide.url" class="d-block w-100 h-100" style="object-fit: cover; filter: brightness(0.75);" :alt="slide.title">
            <div class="carousel-caption d-none d-md-block text-start" style="left: 8%; bottom: 15%; z-index: 10;">
              <h2 class="display-6 fw-bold text-white mb-2" style="letter-spacing: 2px; text-shadow: 1px 1px 8px rgba(0,0,0,0.6);">{{ slide.title }}</h2>
              <p class="fs-5 text-white-50 fw-light mb-0" style="text-shadow: 1px 1px 5px rgba(0,0,0,0.5);">{{ slide.desc }}</p>
            </div>
          </div>
        </div>
      </div>
      
      <button class="carousel-control-prev" type="button" data-bs-target="#yayoiHeroCarousel" data-bs-slide="prev">
        <span class="carousel-control-prev-icon"></span>
      </button>
      <button class="carousel-control-next" type="button" data-bs-target="#yayoiHeroCarousel" data-bs-slide="next">
        <span class="carousel-control-next-icon"></span>
      </button>
    </div>

    <div class="container px-2 py-2">
      <div class="row g-4">
        
        <div class="col-md-3 mb-4">
          <div class="list-group shadow-sm sticky-top" style="top: 110px; z-index: 90; border-radius: 8px; overflow: hidden;">
            <button @click="currentCategory = 1" :class="['list-group-item list-group-item-action py-3 fw-bold border-light-subtle transition-all', currentCategory === 1 ? 'yayoi-active' : 'text-secondary']">🥗 精選日式前菜</button>
            <button @click="currentCategory = 2" :class="['list-group-item list-group-item-action py-3 fw-bold border-light-subtle transition-all', currentCategory === 2 ? 'yayoi-active' : 'text-secondary']">🐟 旬味生魚片系列</button>
            <button @click="currentCategory = 3" :class="['list-group-item list-group-item-action py-3 fw-bold border-light-subtle transition-all', currentCategory === 3 ? 'yayoi-active' : 'text-secondary']">🍣 職人握壽司盛合</button>
            <button @click="currentCategory = 4" :class="['list-group-item list-group-item-action py-3 fw-bold border-light-subtle transition-all', currentCategory === 4 ? 'yayoi-active' : 'text-secondary']">🍳 主廚熱騰騰熟食</button>
            <button @click="currentCategory = 6" :class="['list-group-item list-group-item-action py-3 fw-bold border-light-subtle transition-all', currentCategory === 6 ? 'yayoi-active' : 'text-secondary']">🍰 職人手作甜點</button>
            <button @click="currentCategory = 7" :class="['list-group-item list-group-item-action py-3 fw-bold border-light-subtle transition-all', currentCategory === 7 ? 'yayoi-active' : 'text-secondary']">🥤 特調清爽飲料</button>
            <button @click="currentCategory = 8" :class="['list-group-item list-group-item-action py-3 fw-bold border-light-subtle transition-all', currentCategory === 8 ? 'yayoi-active' : 'text-secondary']">🍺 微醺日式酒水</button>
          </div>
        </div>

        <div class="col-md-9">
          <div class="row row-cols-1 row-cols-md-2 g-4">
            <div class="col" v-for="item in filteredMenu" :key="item.id">
              <div class="card h-100 border-0 shadow-sm overflow-hidden hover-shadow bg-white transition-all" style="border-radius: 12px;">
                <div class="position-relative overflow-hidden" style="height: 240px;">
                  <img :src="item.imageUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=500&q=80'" class="card-img-top h-100 w-100 transition-scale" style="object-fit: cover;" :alt="item.itemName">
                </div>
                
                <div class="card-body d-flex flex-column p-4">
                  <div class="d-flex justify-content-between align-items-center mb-2">
                    <h5 class="card-title fw-bold mb-0" style="color: #374151;">{{ item.itemName }}</h5>
                    <span class="fs-4 fw-bold" style="color: #b45309;">${{ item.price }}</span>
                  </div>
                  
                  <p class="card-text small flex-grow-1 mb-3" style="line-height: 1.6; color: #6b7280;">{{ item.description || '精選道地食材，主廚極致匠心巨作，為您帶來最純粹的日式味蕾饗宴。' }}</p>
                  
                  <div v-if="item.allergenInfo" class="alert alert-warning py-1 px-2 mb-3 border-0 rounded-2 d-flex align-items-center bg-opacity-10" style="font-size: 0.75rem; color: #9a3412; background-color: #ffedd5;">
                    <i class="fa-solid fa-triangle-exclamation me-1"></i> 過敏原提示：{{ item.allergenInfo }}
                  </div>
                  
                  <button class="btn w-100 fw-bold mt-auto py-2 text-white border-0 shadow-sm yayoi-btn">
                    <i class="fa-solid fa-cart-plus me-1"></i>加入購物車
                  </button>
                </div>
              </div>
            </div>
          </div>
          
          <div v-if="filteredMenu.length === 0" class="text-center py-5 text-muted border border-dashed rounded-3 bg-white shadow-sm mt-2">
            <div class="fs-1 mb-2">👨‍🍳</div>
            <div class="fw-bold" style="color: #4b5563;">該系列品項正由主廚精製籌備中</div>
            <div class="small text-muted mt-1">敬請期待！</div>
          </div>
        </div>
        
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 🌟 日式職人美學樣式 */
.transition-all { transition: all 0.3s ease; }
.transition-scale { transition: transform 0.5s ease; }

/* 卡片懸浮滑順放大與長陰影效果 */
.hover-shadow:hover { 
  transform: translateY(-6px); 
  box-shadow: 0 12px 20px rgba(0,0,0,0.06) !important; 
}
.hover-shadow:hover .transition-scale {
  transform: scale(1.04);
}

/* 導覽列激活：竹木暖棕與莫蘭迪暮紫的完美合體 */
.yayoi-active {
  background-color: #b45309 !important;
  background-image: linear-gradient(135deg, #cc7d24 0%, #b45309 100%) !important;
  color: #ffffff !important;
  border-color: #b45309 !important;
}

/* 購物車按鈕職人橘 */
.yayoi-btn {
  background-color: #ea580c;
  background-image: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  border-radius: 6px;
  transition: opacity 0.2s;
}
.yayoi-btn:hover {
  opacity: 0.9;
  color: #ffffff;
}
</style>