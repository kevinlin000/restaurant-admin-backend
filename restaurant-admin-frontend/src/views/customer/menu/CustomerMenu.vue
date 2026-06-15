<script setup>
import { ref, computed as vueComputed } from 'vue'

const menuItems = ref([
  { id: 1, categoryId: 1, itemName: "🍱 蜜汁醬烤雞肉定食", price: 340, description: "鮮嫩雞腿肉以特製照燒醬慢火烘烤，搭配現碾金芽米飯、溫潤味噌湯與手作季節小菜。", imageUrl: "https://images.unsplash.com/photo-1546069901-ba9599a7e63c", status: "AVAILABLE", allergenInfo: "本產品含有大豆、麩質小麥" },
  { id: 2, categoryId: 2, itemName: "🍤 熟成炸豬排滑蛋丼", price: 290, description: "嚴選在地熟成豬里肌，現炸金黃酥脆，淋上特製高湯與滑嫩嚴選鮮蛋液，香氣濃郁。", imageUrl: "https://images.unsplash.com/photo-1583623025817-d180a2221d0a", status: "AVAILABLE", allergenInfo: "本產品含有雞蛋、麩質" },
  { id: 3, categoryId: 3, itemName: "🍜 鮮蝦蔬菜天婦羅烏龍麵", price: 320, description: "大尾鮮蝦與手切旬蔬炸至金黃輕薄外衣，搭配昆布柴魚高湯與Q彈讚岐烏龍麵。", imageUrl: "https://images.unsplash.com/photo-1569718212165-3a8278d5f624", status: "AVAILABLE", allergenInfo: "本產品含有甲殼類蝦、小麥麩質" },
  { id: 4, categoryId: 1, itemName: "🐟 炭烤鯖魚定食", price: 310, description: "嚴選肥美挪威鯖魚，慢火炭烤至外皮金黃酥脆，肉質鮮嫩多汁飽含油脂。", imageUrl: "https://images.unsplash.com/photo-1534604973900-c43ab4c2e0ab", status: "UNAVAILABLE", allergenInfo: "本產品含有魚類" },
  { id: 5, categoryId: 4, itemName: "🍣 鮭魚贅澤握壽司組合", price: 280, description: "主廚特選肥美生鮮鮭魚、炙燒鮭魚與鮭魚卵握壽司共四貫，精緻美味。", imageUrl: "https://images.unsplash.com/photo-1611143669185-af224c5e3252", status: "AVAILABLE", allergenInfo: "本產品含有魚類、生鮮食材" }
])

const currentCategory = ref(1)

const filteredMenu = vueComputed(() => {
  return menuItems.value.filter(item => item.categoryId === currentCategory.value && item.status === 'AVAILABLE')
})
</script>

<template>
  <div class="container overflow-visible" style="min-height: 100vh;">
    
    <div style="height: 120px; width: 100%;"></div>

    <div class="row g-4">
      
      <div class="col-md-3 mb-4">
        <div class="list-group shadow-sm sticky-top" style="top: 120px; z-index: 100;">
          <button @click="currentCategory = 1" :class="['list-group-item list-group-item-action py-3 fw-bold border-light-subtle', currentCategory === 1 ? 'active bg-dark border-dark text-white' : '']">🍱 精緻定食系列</button>
          <button @click="currentCategory = 2" :class="['list-group-item list-group-item-action py-3 fw-bold border-light-subtle', currentCategory === 2 ? 'active bg-dark border-dark text-white' : '']">🍤 經典和風丼飯</button>
          <button @click="currentCategory = 3" :class="['list-group-item list-group-item-action py-3 fw-bold border-light-subtle', currentCategory === 3 ? 'active bg-dark border-dark text-white' : '']">🍜 讚岐烏龍/麵類</button>
          <button @click="currentCategory = 4" :class="['list-group-item list-group-item-action py-3 fw-bold border-light-subtle', currentCategory === 4 ? 'active bg-dark border-dark text-white' : '']">🏮 職人單品/壽司</button>
        </div>
      </div>

      <div class="col-md-9">
        <div class="row row-cols-1 row-cols-md-2 g-4">
          <div class="col" v-for="item in filteredMenu" :key="item.id">
            <div class="card h-100 border-0 shadow-sm overflow-hidden hover-shadow bg-white">
              <img :src="item.imageUrl" class="card-img-top" style="height: 220px; object-fit: cover;" :alt="item.itemName">
              <div class="card-body d-flex flex-column p-4">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <h5 class="card-title fw-bold text-dark mb-0">{{ item.itemName }}</h5>
                  <span class="fs-5 fw-bold text-danger">${{ item.price }}</span>
                </div>
                <p class="card-text text-muted small flex-grow-1" style="line-height: 1.6;">{{ item.description }}</p>
                
                <div v-if="item.allergenInfo" class="alert alert-warning py-1 px-2 mb-3 border-0 text-amber-800 bg-warning bg-opacity-10" style="font-size: 0.75rem;">
                  ⚠️ 過敏原提示：{{ item.allergenInfo }}
                </div>
                
                <button class="btn btn-outline-dark w-100 fw-bold mt-auto py-2">🛒 加入購物車</button>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="filteredMenu.length === 0" class="text-center py-5 text-muted">
          📭 該系列品項正由主廚精緻籌備中，敬請期待！
        </div>
      </div>
      
    </div>
  </div>
</template>

<style scoped>
.hover-shadow { transition: transform 0.2s, box-shadow 0.2s; }
.hover-shadow:hover { transform: translateY(-4px); box-shadow: 0 .5rem 1rem rgba(0,0,0,.08)!important; }
.list-group-item.active { background-color: #4a3728 !important; border-color: #4a3728 !important; }
</style>