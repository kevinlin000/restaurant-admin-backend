<script setup>
import { ref, computed as vueComputed, onMounted } from 'vue'
import axios from 'axios'

// 🏪 1. 門市控制中心（記住目前顧客選的分店，預設先給空值）
const currentStoreId = ref(null)
// 門市清單改為「空陣列」，準備接收同學本機資料庫的真實分店
const storeList = ref([])

// 🍱 2. 菜單控制中心（假資料全面退役！改為空陣列，等待後端真數據注入）
const menuItems = ref([])
// 記住當前點選的分類 ID
const currentCategory = ref(1)

// 🔄 3. 動態抓取全組每個人本機資料庫的門市清單
const fetchStores = async () => {
  try {
    const response = await axios.get('/api/stores')
    storeList.value = response.data
    
    // 如果資料庫裡有店，就預設選第一家店，並觸發拉取該店菜單
    if (storeList.value.length > 0) {
      currentStoreId.value = storeList.value[0].id
      fetchMenuData(currentStoreId.value)
    }
  } catch (error) {
    console.error('⚠️ 總監報告：撈取門市失敗，請檢查 StoreController 是否啟動！', error)
  }
}

// 🍜 4. 根據目前顧客選擇的分店 ID，向後端 API 動態拉取對應菜單
const fetchMenuData = async (storeId) => {
  if (!storeId) return
  try {
    // 🚀 串接曾總監最完美的後端管線，動態傳入 storeId
    const response = await axios.get(`/api/menu-items/store/${storeId}`)
    // 後端吐出的 DTO 陣列直接餵給 menuItems
    menuItems.value = response.data
  } catch (error) {
    console.error('⚠️ 總監報告：拉取該分店菜單失敗，請確認 MenuItemService 是否通電！', error)
  }
}

// 🔄 5. 當顧客手動切換下拉選單店家時觸發
const handleStoreChange = () => {
  console.log(`🎯 總監控制中心：時空切換！正前往分店 ID [${currentStoreId.value}] 拉取實時菜單！`)
  fetchMenuData(currentStoreId.value)
}

// 📦 6. 靈魂條件篩選（計算屬性）
// 對齊你後端吐出來的 DTO 欄位（categoryId），且這裡「不再隱藏」售罄品項，而是交給按鈕去變灰色！
const filteredMenu = vueComputed(() => {
  return menuItems.value.filter(item => item.categoryId === currentCategory.value)
})

// 🛒 7. 加入購物車動作（預留未來跟 Order 同學對接的管線）
const addToCart = (item) => {
  alert(`🎉 成功將【${item.itemName}】加入購物車！`)
}

// 🚀 8. 網頁一打開，立刻發動總攻，大咬合通電！
onMounted(() => {
  fetchStores()
})
</script>

<template>
  <div class="container overflow-visible" style="min-height: 100vh;">
    
    <div style="height: 120px; width: 100%;"></div>

    <div class="row mb-4">
      <div class="col-md-4 me-auto"> <div class="p-3 bg-light rounded-3 shadow-sm border border-light-subtle">
          <label class="form-label fw-bold text-secondary small mb-1">📍 請選擇您要查看的門市菜單：</label>
          <select 
            v-model="currentStoreId" 
            @change="handleStoreChange" 
            class="form-select border-2 fw-bold" 
            style="border-color: #4a3728; cursor: pointer;"
          >
            <option v-for="store in storeList" :key="store.id" :value="store.id">
              {{ store.name }}
            </option>
          </select>
        </div>
      </div>
    </div>

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
                
                <button 
                  v-if="item.isSelectable !== false" 
                  @click="addToCart(item)" 
                  class="btn btn-outline-dark w-100 fw-bold mt-auto py-2"
                >
                  🛒 加入購物車
                </button>
                
                <button 
                  v-else 
                  class="btn btn-secondary w-100 fw-bold mt-auto py-2" 
                  disabled
                >
                  ❌ 已售罄 / 暫不供應
                </button>
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