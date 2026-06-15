<script setup>
import { ref } from 'vue'

const isStoreOpen = ref(true)
const autoHideOutOfStock = ref(false)

const saveSettings = () => {
  alert('⚙️ 菜單系統進階設定已成功儲存！')
}
</script>

<template>
  <div class="container-fluid px-4 py-3">
    <h3 class="mb-5 text-secondary fw-bold">⚙️ 菜單管理 <span class="text-muted fs-5 fw-normal">/ 系統核心設定</span></h3>

    <div class="card shadow-sm border-0 mb-5" style="background-color: #f0f7ff;">
      <div class="card-body p-4">
        <h5 class="card-title fw-bold mb-4" style="color: #03c3ec !important;">🛡️ 敘日輕食後台 - 菜單模組全域控制</h5>
        <form @submit.prevent="saveSettings">
          
          <div class="mb-4 pb-3 border-bottom border-light-subtle">
            <label class="form-label fw-bold text-dark fs-6">🏪 線上點餐營業開關</label>
            <div class="form-check form-switch mt-1">
              <input v-model="isStoreOpen" class="form-check-input" type="checkbox" id="switchOpen" style="cursor: pointer;">
              <label class="form-check-label text-secondary fw-semibold small" for="switchOpen">
                {{ isStoreOpen ? '🟢 目前開放前台顧客線上點餐、加入購物車' : '🔴 目前店休，前台僅提供菜單瀏覽、無法點餐' }}
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

          <button type="submit" class="btn btn-info fw-bold text-white px-4 shadow-sm" style="background-color: #03c3ec; border-color: #03c3ec;">💾 儲存全域設定</button>
        </form>
      </div>
    </div>

    <div class="card border-0 shadow-sm" style="background-color: #f8f9fa;">
      <div class="card-body p-4">
        <h6 class="fw-bold text-dark mb-2">📌 專案總監架構備忘錄：</h6>
        <p class="mb-0 text-secondary small font-monospace">
          本頁面已預留給未來跨模組（如：店鋪狀態控制、通知中心）的串接。下午開會可向團隊提報此頁面為「菜單控制台的外殼預留」，展現系統設計的完整度。
        </p>
      </div>
    </div>
  </div>
</template>