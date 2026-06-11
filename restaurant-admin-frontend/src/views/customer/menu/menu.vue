<script setup>
import { ref, computed as vueComputed, onMounted } from 'vue'

// 1. 先開一個乾淨的空盒子，準備裝等一下從 Java 運過來的 10 道菜
const menuItems = ref([])

// 2. 當網頁一整頁載入完成（onMounted），立刻發動接線！
onMounted(() => {
  fetch('http://localhost:8080/api/menu-items')
    .then(response => response.json())
    .then(res => {
      // 💡 還記得我們在瀏覽器驗收看到的 {"success": true, "data": [...]} 嗎？
      // 真實的 10 道菜陣列藏在 res.data 裡，我們把它牢牢裝進盒子！
      if (res.success && res.data) {
        menuItems.value = res.data
      }
    })
    .catch(error => console.error('敲門連線失敗：', error))
})

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