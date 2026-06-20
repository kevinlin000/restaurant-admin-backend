<script setup>
import { ref, onMounted, computed } from 'vue' // 💡 完美引入動態計算屬性
import { useRouter } from 'vue-router' // 🚀 1. 引入 Vue Router 的導航推手
import axios from '@/api/axios';

// 🚀 2. 啟動導航推手
const router = useRouter()

// 🏪 多租戶防禦點火線：獲取當前店長專屬的 storeId (可根據你們組內的 localStorage 鍵名調整，如 'storeId' 或 'user')
const currentStoreId = ref(localStorage.getItem('storeId') || 1) 

// 🚀 3. 後端大寫 MenuCreateDTO 規格變數
const newItem = ref({
  itemName: '',
  price: '',
  description: '',
  imageUrl: '',
  categoryId: 1,
  status: 'AVAILABLE',
  allergenInfo: ''
})

// 🚀 4. 初始化為空籃子，準備裝真實數據
const menuItems = ref([])
// 🚀 5. 搜尋欄位的響應式變數
const searchQuery = ref('')

// 🚀 6. 核心演算法：根據你打的字，即時從下面動態篩選出結果！
const filteredMenuItems = computed(() => {
  if (!searchQuery.value.trim()) {
    return menuItems.value
  }
  return menuItems.value.filter(item => {
    return item.itemName && item.itemName.toLowerCase().includes(searchQuery.value.toLowerCase().trim())
  })
})

// 🚀 7. 去資料庫撈真數據的方法 —— ⚡ 史詩級升級：只撈取該店長所屬分店的菜單，達成多租戶隔離！
const fetchMenuItems = async () => {
  try {
    // 🎯 完美咬合後端 MenuItemController.java 第 68 行的專屬菜單 displayList 路由！
    const response = await axios.get(`/api/menu-items/store/${currentStoreId.value}`)
    // 由於後端 getStoreMenu 回傳的是 StoreMenuDisplayResponse 自訂結構，資料在外層的 .data 內
    menuItems.value = response.data.data || response.data
  } catch (error) {
    console.error('後端發電廠傳輸大塞車：', error)
  }
}

// 🚀 8. 網頁一打開，立馬派人去撈資料庫
onMounted(() => {
  fetchMenuItems()
})

// 🚀 9. 點擊「編輯此項」跨頁面轉跳方法（對齊 /admin/menu-edit/:id 路由）
const selectItem = (item) => {
  if (!item.id) {
    alert(' ⚠️ 這道餐點沒有 ID 數據，無法進行編輯！')
    return
  }
  // 帶著 ID 絲滑轉跳到修改頁面
  router.push(`/admin/menu-edit/${item.id}`)
}

// 🚀 10. 新增菜單連通點火方法 —— ⚡ 史詩級升級：對齊後端獨立分店寫入管線
const handleAddItemMenu = async () => {
  if (!newItem.value.itemName || !newItem.value.price) {
    alert('請填寫完整餐點名稱與價格！')
    return
  }
  try {
    // 🎯 完美咬合後端最新 POST /api/menu-items/store/{storeId} 隔離管線，精準寫入 store_menu！
    const response = await axios.post(`/api/menu-items/store/${currentStoreId.value}`, {
      categoryId: Number(newItem.value.categoryId),
      itemName: newItem.value.itemName,
      price: Number(newItem.value.price),
      description: newItem.value.description || "日式職人手作美味。",
      imageUrl: newItem.value.imageUrl || "https://images.unsplash.com/photo-1546069901-ba9599a7e63c",
      allergenInfo: newItem.value.allergenInfo || "無特殊過敏原提示。",
      isActive: true
    })
    
    if (response.data.success) {
      alert(` 🎉 成功新增一筆定食餐點，並精準綁定至第 ${currentStoreId.value} 號分店資料庫！`)
      fetchMenuItems() // 重新刷新該店專屬資料列表
      newItem.value = { itemName: '', price: '', description: '', imageUrl: '', categoryId: 1, status: 'AVAILABLE', allergenInfo: '' }
    } else {
      alert('上架失敗：' + response.data.message)
    }
  } catch (error) {
    console.error('後端發電廠拒收包裹：', error)
    alert(' ⚠️ 請檢查後端控制台是否已完全啟動！')
  }
}
</script>

<template>
  <div class="container-fluid px-4 py-3">
    <h3 class="mb-5 text-secondary fw-bold" style="color: #374151 !important;">
      <span style="color: #374151;">➕ 菜單管理</span>
      <span class="text-muted fs-5 fw-normal"> / 新增全新項目</span>
    </h3>

    <div class="card mb-5 border-0 shadow-sm" style="background-color: #fbfbfd;">
      <div class="card-body p-4">
        <h5 class="mb-4 fw-bold" style="color: #374151;">
          <i class="fa-solid fa-wand-magic-sparkles me-2" style="color: #8162c9;"></i>填寫日式定食餐點資訊
        </h5>

        <div class="row g-3">
          <div class="col-md-4">
            <label class="form-label fw-bold small" style="color: #4b5563;">餐點名稱</label>
            <input v-model="newItem.itemName" type="text" class="form-control form-control-solid" placeholder="例如：炭烤鯖魚定食" style="color: #374151; border-color: #cbd5e1;">
          </div>

          <div class="col-md-2">
            <label class="form-label fw-bold small" style="color: #4b5563;">價格 (NTD)</label>
            <input v-model="newItem.price" type="number" class="form-control form-control-solid" placeholder="價格" style="color: #374151; border-color: #cbd5e1;">
          </div>

          <div class="col-md-3">
            <label class="form-label fw-bold small" style="color: #4b5563;">餐點分類</label>
            <select v-model="newItem.categoryId" class="form-select form-control-solid" style="color: #374151; border-color: #cbd5e1; font-weight: 500;">
              <option :value="1" style="color: #374151;">精選日式前菜</option>
              <option :value="2" style="color: #374151;">旬味生魚片系列</option>
              <option :value="3" style="color: #374151;">職人握壽司盛合</option>
              <option :value="4" style="color: #374151;">主廚熱騰騰熟食</option>
              <option :value="6" style="color: #374151;">職人手作甜點</option>
              <option :value="7" style="color: #374151;">特調清爽飲料</option>
              <option :value="8" style="color: #374151;">微醺日式酒水</option>
            </select>
          </div>

          <div class="col-md-3">
            <label class="form-label fw-bold small" style="color: #4b5563;">上架狀態</label>
            <select v-model="newItem.isActive" class="form-select form-control-solid" style="color: #374151; border-color: #cbd5e1; font-weight: 500;">
              <option :value="true" style="color: #374151;">🟢 上架供應</option>
              <option :value="false" style="color: #374151;">🔴 暫時下架</option>
            </select>
          </div>

          <div class="col-md-6">
            <label class="form-label fw-bold small" style="color: #4b5563;">餐點詳細描述</label>
            <input v-model="newItem.description" type="text" class="form-control form-control-solid" placeholder="餐點描述..." style="color: #374151; border-color: #cbd5e1;">
          </div>

          <div class="col-md-3">
            <label class="form-label fw-bold small" style="color: #4b5563;">過敏原資訊</label>
            <div class="input-group">
              <span class="input-group-text bg-white" style="border-color: #fed7aa; color: #9a3412;">⚠️</span>
              <input
                v-model="newItem.allergenInfo"
                type="text"
                class="form-control"
                placeholder="例如：含堅果、大豆（無則留空）"
                style="color: #374151; border-color: #fed7aa; background-color: #ffffff; font-weight: 500;"
              >
            </div>
          </div>

          <div class="col-md-3">
            <label class="form-label fw-bold small" style="color: #4b5563;">圖片網址</label>
            <input v-model="newItem.imageUrl" type="text" class="form-control form-control-solid" placeholder="圖片 URL..." style="color: #374151; border-color: #cbd5e1;">
          </div>
        </div>

        <div class="text-end mt-4">
          <button @click="handleAddItemMenu" class="btn px-4 py-2 text-white fw-bold shadow-sm" style="background-color: #7c3aed; background-image: linear-gradient(135deg, #9365ec 0%, #7c46e3 100%); border: none; border-radius: 8px;">
            <i class="fa-solid fa-floppy-disk me-2"></i>儲存並上架項目
          </button>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div class="p-3 border-bottom d-flex align-items-center justify-content-between bg-light">
          <h6 class="mb-0 fw-bold" style="color: #374151;"><i class="fa-solid fa-utensils me-2" style="color: #6d28d9;"></i>目前定食餐點列表</h6>

          <div style="width: 280px;">
            <input v-model="searchQuery" type="text" class="form-control form-control-sm" placeholder="🔍 輸入品項名稱快速查詢..." style="border-radius: 6px; border: 1px solid #cbd5e1; color: #374151; font-weight: 500; background-color: #ffffff;">
          </div>
        </div>

        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light small uppercase fw-bold" style="color: #374151;">
              <tr>
                <th class="px-4" style="width: 80px; color: #374151;">ID</th>
                <th style="width: 120px; color: #374151;">分類</th>
                <th style="color: #374151;">餐點名稱</th>
                <th style="width: 100px; color: #374151;">價格</th>
                <th style="color: #374151;">餐點描述</th>
                <th style="color: #374151;">過敏原</th>
                <th style="width: 120px; color: #374151;">狀態</th>
                <th class="px-4" style="width: 100px; color: #374151;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in filteredMenuItems" :key="item.id" style="color: #4b5563; font-weight: 500;">
                <td class="px-4 fw-bold" style="color: #374151;">#{{ item.id }}</td>
                <td>
                  <span v-if="(item.categoryId || item.category_id) === 1" class="badge px-3 py-1.5 rounded-pill border" style="background-color: #eff6ff !important; color: #1d4ed8 !important; border-color: #bfdbfe !important; font-weight: 600;">前菜</span>
                  <span v-else-if="(item.categoryId || item.category_id) === 2" class="badge px-3 py-1.5 rounded-pill border" style="background-color: #fef2f2 !important; color: #b91c1c !important; border-color: #fecaca !important; font-weight: 600;">生魚片</span>
                  <span v-else-if="(item.categoryId || item.category_id) === 3" class="badge px-3 py-1.5 rounded-pill border" style="background-color: #fdf2f8 !important; color: #be185d !important; border-color: #fbcfe8 !important; font-weight: 600;">壽司</span>
                  <span v-else-if="(item.categoryId || item.category_id) === 4" class="badge px-3 py-1.5 rounded-pill border" style="background-color: #fff7ed !important; color: #c2410c !important; border-color: #ffedd5 !important; font-weight: 600;">熟食</span>
                  <span v-else-if="(item.categoryId || item.category_id) === 5" class="badge px-3 py-1.5 rounded-pill border" style="background-color: #f0fdf4 !important; color: #15803d !important; border-color: #bbf7d0 !important; font-weight: 600;">炸物</span>
                  <span v-else-if="(item.categoryId || item.category_id) === 6" class="badge px-3 py-1.5 rounded-pill border" style="background-color: #faf5ff !important; color: #6b21a8 !important; border-color: #e9d5ff !important; font-weight: 600;">甜點</span>
                  <span v-else-if="(item.categoryId || item.category_id) === 7" class="badge px-3 py-1.5 rounded-pill border" style="background-color: #f0fdfa !important; color: #0f766e !important; border-color: #ccfbf1 !important; font-weight: 600;">飲料</span>
                  <span v-else-if="(item.categoryId || item.category_id) === 8" class="badge px-3 py-1.5 rounded-pill border" style="background-color: #f8fafc !important; color: #475569 !important; border-color: #e2e8f0 !important; font-weight: 600;">酒水</span>
                </td>
                <td class="fw-bold" style="color: #4b5563;">{{ item.itemName }}</td>
                <td class="fw-bold" style="color: #16a34a;">${{ item.price || item.basePrice }}</td>
                <td class="text-muted small" style="max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ item.description || '暫無描述' }}</td>
                <td>
                  <span v-if="item.allergenInfo" class="badge text-amber-800 bg-warning bg-opacity-10 border border-warning-subtle rounded-2" style="font-size: 0.75rem; color: #9a3412; background-color: #ffedd5 !important;">{{ item.allergenInfo }}</span>
                  <span v-else class="text-muted small">無</span>
                </td>
                <td>
                  <span v-if="item.isActive === true || item.isActive === 'true' || item.isActive == 1" class="badge border border-success-subtle" style="color: #16a34a; font-weight: bold; background-color: #f0fdf4 !important;">供應中</span>
                  <span v-else class="badge border border-danger-subtle" style="color: #dc2626; font-weight: bold; background-color: #fef2f2 !important;">已下架</span>
                </td>
                <td class="px-4">
                  <button @click="selectItem(item)" class="btn btn-sm text-white shadow-sm" style="background-color: #8162c9; background-image: linear-gradient(135deg, #9365ec 0%, #8162c9 100%); opacity: 0.9; border: none; font-weight: bold; font-size: 12px; padding: 6px 12px; border-radius: 6px;">
                    編輯此項
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>