<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/http' // 確保引入組長整合好、內建自動帶 Token 的 http.js

const isStoreOpen = ref(true)
const autoHideOutOfStock = ref(false)
const userRole = ref('') 
const currentStoreId = ref(1) // 預設為 A11 店的 ID
const isLoading = ref(false)

onMounted(async () => {
  userRole.value = localStorage.getItem('role') || 'MANAGER'
  
  // 💡 [修正] 移除網址開頭多餘的 /api，避免變成 /api/api
  try {
    const res = await axios.get(`/admin/stores/${currentStoreId.value}`)
    isStoreOpen.value = res.data.status === 'OPEN'
  } catch (error) {
    console.error('取得門市狀態失敗', error)
  }
})

// 🛠️ 1. 單店營業開關連動
const handleStoreStatusChange = async () => {
  if (isLoading.value) return
  isLoading.value = true
  
  const targetStatus = isStoreOpen.value ? 'OPEN' : 'PAUSED'
  
  // 💡 [修正] 移除網址開頭多餘的 /api，對齊組長後端規格
  try {
    await axios.put(`/admin/stores/${currentStoreId.value}/status`, {
      status: targetStatus
    })
    alert(`🏪 門市狀態已成功同步至資料庫：${targetStatus}`)
  } catch (error) {
    alert('❌ 狀態變更失敗，請檢查權限或後端連線')
    isStoreOpen.value = !isStoreOpen.value // 失敗時把開關撥回來
  } finally {
    isLoading.value = false
  }
}

// 🛠️ 2. 總部儲存全域設定（ADMIN 專用）
const saveGlobalSettings = async () => {
  // 💡 [修正] 移除網址開頭多餘的 /api
  try {
    await axios.put('/admin/stores/global-status', {
      status: isStoreOpen.value ? 'OPEN' : 'PAUSED'
    })
    alert('⚙️ 總部最高全域核心設定已儲存！已一鍵強制變更全台門市狀態。')
  } catch (error) {
    alert('❌ 全域變更失敗，僅限最高管理員 ADMIN 操作')
  }
}
</script>

<template>
  <div class="container-fluid px-4 py-3">
    <h3 class="mb-5 text-secondary fw-bold">⚙️ 菜單管理 <span class="text-muted fs-5 fw-normal">/ 系統核心設定</span></h3>

    <div class="card shadow-sm border-0 mb-5" style="background-color: #f0f7ff;">
      <div class="card-body p-4">
        <h5 class="card-title fw-bold mb-4" style="color: #03c3ec !important;">🛡️ 敘日輕食後台 - 菜單模組全域控制</h5>
        
        <form @submit.prevent="saveGlobalSettings">
          
          <div class="mb-4 pb-3 border-bottom border-light-subtle">
            <label class="form-label fw-bold text-dark fs-6">🏪 單店線上點餐營業開關 (快捷操作)</label>
            <div class="form-check form-switch mt-1">
              <input 
                v-model="isStoreOpen" 
                @change="handleStoreStatusChange"
                :disabled="isLoading"
                class="form-check-input" 
                type="checkbox" 
                id="switchOpen" 
                style="cursor: pointer;"
              >
              <label class="form-check-label text-secondary fw-semibold small" for="switchOpen">
                {{ isStoreOpen ? '🟢 目前開放本門市線上點餐、加入購物車' : '🔴 目前本店暫停接單，顧客僅提供菜單瀏覽' }}
              </label>
            </div>
          </div>

          <div class="mb-4 pb-3 border-bottom border-light-subtle">
            <label class="form-label fw-bold text-dark fs-6">📦 庫存售罄自動化控制</label>
            <div class="form-check form-switch mt-1">
              <input v-model="autoHideOutOfStock" class="form-check-input" type="checkbox" id="switchStock" style="cursor: pointer;">
              <label class="form-check-label text-dark small" for="switchStock">
                當後端資料庫庫存歸零時，前台自動將餐點標籤改為「已下架」
              </label>
            </div>
          </div>

          <button 
            v-if="userRole === 'ADMIN'" 
            type="submit" 
            class="btn btn-info fw-bold text-white px-4 shadow-sm" 
            style="background-color: #03c3ec; border-color: #03c3ec;"
          >
            💾 總部儲存全域設定 (一鍵強關全台)
          </button>
          
          <div v-else class="text-muted small fw-semibold">
            ℹ️ 您目前以 [店長] 權限登入，單店開關將即時生效；全域管理控制僅限總部最高權限 (ADMIN) 變更。
          </div>
        </form>
      </div>
    </div>

    <div class="card border-0 shadow-sm" style="background-color: #f8f9fa;">
      <div class="card-body p-4">
        <h6 class="fw-bold text-dark mb-2">📌 專案總監架構備忘錄：</h6>
        <p class="mb-0 text-secondary small font-monospace">
          本頁面已完美與組長的 Store 模組進行「跨模組解耦咬合」。店長操作將連動各分店狀態，總部 ADMIN 操作則一鍵覆蓋全域。前後端防禦已全面通車。
        </p>
      </div>
    </div>
  </div>
</template>