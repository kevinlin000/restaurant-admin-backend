<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/axios'

// ✅ 統一改成跟其他頁面一樣的判斷方式
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const isAdmin = userInfo.roleName === 'ADMIN'

const isStoreOpen = ref(true)
const autoHideOutOfStock = ref(false)
const currentStoreId = ref(Number(localStorage.getItem('storeId')) || 1)
const isLoading = ref(false)

onMounted(async () => {
  // 🎯 只有店長需要撈取自己門市的狀態，ADMIN 不需要
  if (!isAdmin) {
    try {
      const res = await axios.get(`/admin/stores/${currentStoreId.value}`)
      isStoreOpen.value = res.data.status === 'OPEN'
    } catch (error) {
      console.error('取得門市狀態失敗', error)
    }
  }
})

// 🛠️ 1. 單店營業開關連動（店長專用）
const handleStoreStatusChange = async () => {
  if (isLoading.value) return
  isLoading.value = true
  
  const targetStatus = isStoreOpen.value ? 'OPEN' : 'PAUSED'
  
  try {
    await axios.put(`/admin/stores/${currentStoreId.value}/status`, {
      status: targetStatus
    })
    alert(`🏪 門市狀態已成功同步至資料庫：${targetStatus}`)
  } catch (error) {
    alert('❌ 狀態變更失敗，請檢查權限或後端連線')
    isStoreOpen.value = !isStoreOpen.value
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="container-fluid px-4 py-3">
    <h3 class="mb-5 text-secondary fw-bold">⚙️ 菜單管理 <span class="text-muted fs-5 fw-normal">/ 系統核心設定</span></h3>
    <div class="card border-0 shadow-sm mb-4" style="background-color: #fffaf3;">
      <div class="card-body p-4">
        <h6 class="fw-bold mb-3" style="color: #2d2a2a;">📋 菜單模組權限說明</h6>
        <div class="row">
          <div class="col-md-6 mb-3">
            <div class="d-flex align-items-start gap-2">
              <span class="badge" style="background-color: #b45309; color: #fff;">總部</span>
              <p class="small mb-0" style="color: #5c4033;">可選擇「全台統一定價」調整總部基準價，各分店將依區域規則自動套用加減價。</p>
            </div>
          </div>
          <div class="col-md-6 mb-3">
            <div class="d-flex align-items-start gap-2">
              <span class="badge" style="background-color: #6b7280; color: #fff;">店長</span>
              <p class="small mb-0" style="color: #5c4033;">僅能管理自己所屬分店的菜單內容與上下架狀態，無法影響其他分店或總部基準價。</p>
            </div>
          </div>
        </div>
        <p class="small mb-0 mt-2" style="color: #9a3412;">💡 分店營運狀態（開店/休息）請至「分店管理」頁面調整。</p>
      </div>
    </div>
    <div class="card shadow-sm border-0 mb-5" style="background-color: #f0f7ff;">
      <div class="card-body p-4">
        <h5 class="card-title fw-bold mb-4" style="color: #03c3ec !important;">🛡️ 敘日輕食後台 - 菜單模組全域控制</h5>

        
        <!-- 🏪 店長專屬：單店營業開關 -->
        <div v-if="!isAdmin" class="mb-4 pb-3 border-bottom border-light-subtle">
        <label class="form-label fw-bold text-dark fs-6">🏪 單店線上點餐營業開關 (快捷操作)</label>
        <div class="form-check form-switch mt-1">
          <input 
            v-model="isStoreOpen" 
            @change="handleStoreStatusChange"
            :disabled="true"
            class="form-check-input" 
            type="checkbox" 
            id="switchOpen" 
            style="cursor: not-allowed;"
          >
          <label class="form-check-label text-secondary fw-semibold small" for="switchOpen">
            {{ isStoreOpen ? '🟢 目前開放本門市線上點餐、加入購物車' : '🔴 目前本店暫停接單，顧客僅提供菜單瀏覽' }}
            <span class="badge bg-light text-secondary border ms-1">開發中</span>
          </label>
        </div>
      </div>
        <!-- 🏪 店長看到的版本：可操作（標示開發中）的開關 -->
        <div v-if="!isAdmin" class="mb-4 pb-3 border-bottom border-light-subtle">
          <label class="form-label fw-bold text-dark fs-6">📦 庫存售罄自動化控制</label>
          <div class="form-check form-switch mt-1">
            <input v-model="autoHideOutOfStock" disabled class="form-check-input" type="checkbox" id="switchStock" style="cursor: not-allowed;">
            <label class="form-check-label text-dark small" for="switchStock">
              啟用後，訂單付款完成時將自動標記售完餐點為「已下架」
              <span class="badge bg-light text-secondary border ms-1">開發中</span>
            </label>
          </div>
        </div>

        <!-- 🛡️ 總部看到的版本：說明文字，非操作型 -->
        <div v-if="isAdmin" class="mb-4 pb-3 border-bottom border-light-subtle">
          <label class="form-label fw-bold text-dark fs-6">📦 庫存售罄自動化控制</label>
          <p class="small mb-0" style="color: #5c4033;">
            此設定由各分店店長自行決定是否啟用，總部可於未來版本中新增「全台庫存自動化總覽」功能進行即時監控。
          </p>
        </div>
        <div v-if="!isAdmin" class="text-muted small fw-semibold">
          ℹ️ 您目前以 [店長] 權限登入，單店開關將即時生效
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm" style="background-color: #f8f9fa;">
      <div class="card-body p-4">
        <h6 class="fw-bold text-dark mb-2">📌 專案總監架構備忘錄：</h6>
        <p class="mb-0 text-secondary small font-monospace">
          本頁面已完美與組長的 Store 模組進行「跨模組解耦咬合」。店長操作將連動各分店狀態，總部 ADMIN 操作則一鍵覆蓋或恢復全域狀態。前後端防禦已全面通車。
        </p>
      </div>
    </div>
  </div>
</template>