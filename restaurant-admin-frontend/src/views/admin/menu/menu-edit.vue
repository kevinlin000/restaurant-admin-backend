<script setup>
import { useRoute } from 'vue-router' // 1. 導入 Vue Router 的捕手手套
import axios from '@/api/axios';
import { ref, onMounted, computed, nextTick, watch } from 'vue'

//上下架選單預設
const actualIsActive = ref(null)

// 2. 啟動手套
const route = useRoute()
const menuItemId = ref(null)

// 🚀
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const isAdmin = userInfo.roleName === 'ADMIN'

// 🏪 多租戶防禦點火線：獲取當前店長專屬的 storeId 
const currentStoreId = ref(
  isAdmin ? 'ALL' : (Number(localStorage.getItem('storeId')) || 1)
)

// 3. 🎯 完美對齊 Java MenuItem.java 的屬性規格！
const formData = ref({
  id: null,
  categoryId: 1,
  itemName: '',
  price: '',
  description: '',
  imageUrl: '',
  isActive: true, // 🚀 史詩級同步：只用 isActive 布林值！
  allergenInfo: '',
  featureTags: '' // 👈 前端下拉選單預設用空字串綁定單選值
})

// 下方列表數據與搜尋字串
const menuItems = ref([])

const categoryList = ref([])

const fetchCategories = async () => {
  try {
    const response = await axios.get('/api/menu-categories')
    categoryList.value = response.data.data || response.data
  } catch (error) {
    console.error('分類載入失敗', error)
  }
}

const searchQuery = ref('')

// 🔍 即時打字動態過濾演算法
const filteredMenuItems = computed(() => {
  if (!searchQuery.value.trim()) {
    return menuItems.value
  }
  return menuItems.value.filter(item => {
    return item.itemName && item.itemName.toLowerCase().includes(searchQuery.value.toLowerCase().trim())
  })
})

// 撈取所有菜單 —— ⚡ 史詩級升級：改為只顯示自己店裡的餐點列表，避開隔壁店家！
const fetchAllMenuItems = async () => {
  try {
    const response = await axios.get(`/api/menu-items/store/${currentStoreId.value}`)
    const formattedData = (response.data.data || response.data).map(item => ({
      ...item,
      price: isAdmin ? item.basePrice : item.finalPrice
    }))
    menuItems.value = formattedData
  } catch (error) {
    console.error('撈取分店菜單列表失敗：', error)
  }
}


// 點擊下方列表快速切換編輯對象（並絲滑滾動至頂部與自動聚焦）
const selectItem = (item) => {
  formData.value = { 
    ...item,
    price: item.finalPrice ,
    isActive: item.isSelectable,
    // ⚡ 核心回填：從下方列表選取時，若有標籤陣列，將第一個值抽出來做為單選值回填
    featureTags: (item.featureTags && item.featureTags.length > 0) ? item.featureTags[0] : ''
  }
  menuItemId.value = item.id
  actualIsActive.value = item.isSelectable
  
  // 🎯 絲滑滾動大絕招
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
  
  // 🎯 自動聚焦餐點名稱輸入框
  setTimeout(() => {
    const nameInput = document.getElementById('itemNameInput')
    if (nameInput) nameInput.focus()
  }, 300)
}

// 4. 網頁開機自動點火
onMounted(async () => {
  menuItemId.value = route.params.id
  fetchAllMenuItems()    //任務a:搬分店大清單
  fetchCategories()
  if (menuItemId.value) {  //任務b:去搬這「單一品項」的舊資料
    try {
      // 🎯 移除硬編碼，回歸相對路徑
      const res = await axios.get(`/api/menu-items/${menuItemId.value}`)
      const rawData = res.data.data || res.data
      
      // ⚡ 核心動態回填：將後端回傳的 List 標籤解構為單選下拉選單所需值
      let currentTag = '';
      if (rawData.featureTags && rawData.featureTags.length > 0) {
        currentTag = rawData.featureTags[0];
      }

      formData.value = {
        ...rawData,
        // 如果是點進來的，由於是從總表撈單一品項，若分店有客製價則優先沿用，沒有就用總部的
        price: rawData.price || rawData.basePrice,
        featureTags: currentTag // 將解析後的標籤回填入 formData
      }
    } catch (error) {
      console.error('撈取單一菜單資料失敗：', error)
    }
  }
})

// ✅ 共用函式，放在 handleUpdateMenu 上方
  const submitMenuUpdate = async (overrideData = {}) => {
  const cleanMenuId = parseInt(menuItemId.value, 10)
  const processedTags = formData.value.featureTags || null

  // ✅ 判斷是全台統一還是特定分店
  const storeIdValue = currentStoreId.value === 'ALL' ? 'ALL' : parseInt(currentStoreId.value, 10)

  await axios.put(`/api/menu-items/${cleanMenuId}/store/${storeIdValue}`, {
    categoryId: Number(formData.value.categoryId),
    itemName: formData.value.itemName,
    price: Number(formData.value.price),
    description: formData.value.description,
    imageUrl: formData.value.imageUrl,
    allergenInfo: formData.value.allergenInfo,
    featureTags: processedTags,
    ...overrideData
  })
}

// ✅ 簡化後的 handleUpdateMenu
const handleUpdateMenu = async () => {
  try {
    await submitMenuUpdate({ isActive: formData.value.isActive })
    const storeLabel = currentStoreId.value === 'ALL' ? '全台' : `第 ${currentStoreId.value} 號`
    alert(`🎉 ${storeLabel} 分店餐點數據客製修改成功！`)
    fetchAllMenuItems()
  } catch (error) {
    console.error('分店更新餐點失敗：', error)
    alert('❌ 更新失敗，請檢查後端控制台！')
  }
}

// 🚀 6. 雙向開關邏輯：分店專屬狀態取反切換（上架/下架）
// ✅ 安全順序
const handleToggleStatus = async () => {
  if (!formData.value.id) {
    alert('請先在下方列表選擇一個餐點才能進行操作唷！')
    return
  }

  const isCurrentlyAvailable = actualIsActive.value === true
  const newStatus = !isCurrentlyAvailable
  const actionText = isCurrentlyAvailable ? '下架移出分店菜單' : '分店重新上架還原'

  const confirmAction = confirm(`確定要將【${formData.value.itemName}】進行${actionText}嗎？`)
  if (!confirmAction) {
    // ✅ 使用者取消時，把下拉選單還原回真實狀態
    formData.value.isActive = actualIsActive.value
    return
  }

  try {
    await submitMenuUpdate({ isActive: newStatus })
    formData.value.isActive = newStatus
    actualIsActive.value = newStatus
    alert(`🎉 【${formData.value.itemName}】${actionText}成功！`)
    fetchAllMenuItems()
  } catch (error) {
    console.error('狀態切換失敗：', error)
    alert('❌ 操作失敗，請檢查後端控制台！')
  }
}


const handlePermanentDelete = async () => {
  if (!formData.value.id) {
    alert('請先在下方列表選擇一個餐點才能操作！')
    return
  }

  const confirmDelete = confirm(`⚠️ 確定要永久刪除【${formData.value.itemName}】嗎？此操作無法復原！`)
  if (!confirmDelete) return

  try {
    await axios.delete(`/api/menu-items/${formData.value.id}/permanent`)
    alert('✅ 已永久刪除！')
    formData.value = { id: null, categoryId: 1, itemName: '', price: '', description: '', imageUrl: '', isActive: true, allergenInfo: '', featureTags: '' }
    menuItemId.value = null
    fetchAllMenuItems()
  } catch (error) {
    console.error('永久刪除失敗：', error)
    alert('❌ 刪除失敗，請檢查後端控制台！')
  }
}
</script>

<template>
  <div class="container-fluid px-4 py-3">
    <h3 class="mb-5 text-secondary fw-bold" style="color: #374151 !important;">
      <span style="color: #374151;">📝 菜單管理</span>
      <span class="text-muted fs-5 fw-normal"> / 編輯與修改項目</span>
    </h3>

    <!-- 🎯 ADMIN 專用：說明文字 -->
    <div v-if="isAdmin" class="alert mb-3" style="background-color: #fff7ed; border: 1px solid #fed7aa; color: #9a3412; font-size: 13px; border-radius: 10px; padding: 10px 14px;">
      💡 以總部身份修改菜單，將同步更新所有分店的基準價格，各店依區域規則自動計算最終售價。
    </div>

    <div class="card mb-5 border-0 shadow-sm" style="background-color: #fff7ed;">
      <div class="card-body p-4">
        <h5 class="mb-4 fw-bold" style="color: #374151;">
          <i class="fa-solid fa-pen-to-square me-2" style="color: #ea580c;"></i>修改日式定食餐點資訊
        </h5>

        <div class="row g-3">
          <div class="col-md-3">
            <label class="form-label fw-bold small" style="color: #4b5563;">餐點名稱</label>
            <input id="itemNameInput" v-model="formData.itemName" type="text" class="form-control form-control-solid bg-white" placeholder="點擊下方列表進行編輯..." style="color: #374151; border-color: #fed7aa;">
          </div>

          <div class="col-md-2">
            <label class="form-label fw-bold small" style="color: #4b5563;">價格 (NTD)</label>
            <input v-model="formData.price" type="number" class="form-control form-control-solid bg-white" placeholder="價格" style="color: #374151; border-color: #fed7aa;">
          </div>

          <div class="col-md-3">
            <label class="form-label fw-bold small" style="color: #4b5563;">修改分類</label>
            <select v-model="formData.categoryId" class="form-select form-control-solid bg-white" style="color: #374151; border-color: #fed7aa; font-weight: 500;">
              <option 
                v-for="cat in categoryList" 
                :key="cat.id" 
                :value="cat.id"
                style="color: #374151;">
                {{ cat.categoryName }}
              </option>
            </select>
          </div>

          <div class="col-md-2">
            <label class="form-label fw-bold small" style="color: #4b5563;">✨ 特色行銷標籤</label>
            <select v-model="formData.featureTags" class="form-select form-control-solid bg-white" style="color: #374151; border-color: #fed7aa; font-weight: 500; border-radius: 6px;">
              <option value="">-- 不設定標籤（留白） --</option>
              <option value="主廚推薦">👑 主廚推薦</option>
              <option value="手作工法">👨‍🍳 手作工法</option>
              <option value="人氣爆棚">🔥 人氣爆棚</option>
              <option value="鮮味極致">🐟 鮮味極致</option>
              <option value="經典必點">✨ 經典必點</option>
              <option value="職人精神">🎯 職人精神</option>
              <option value="嚴選食材">🌿 嚴選食材</option>
              <option value="季節限定">🌸 季節限定</option>
              <option value="極致奢華">💎 極致奢華 (限量)</option>
              <option value="限量供應">⏳ 限量供應 (限量)</option>
            </select>
          </div>

          <div class="col-md-2">
            <label class="form-label fw-bold small" style="color: #4b5563;">上架狀態</label>
            <select v-model="formData.isActive" class="form-select form-control-solid bg-white" style="color: #374151; border-color: #fed7aa; font-weight: 500;">
              <option :value="true" style="color: #374151;">🟢 供應中</option>
              <option :value="false" style="color: #374151;">🔴 已下架</option>
            </select>
          </div>

          <div class="col-md-6">
            <label class="form-label fw-bold small" style="color: #4b5563;">餐點詳細描述</label>
            <input v-model="formData.description" type="text" class="form-control form-control-solid bg-white" placeholder="餐點描述..." style="color: #374151; border-color: #fed7aa;">
          </div>

          <div class="col-md-3">
            <label class="form-label fw-bold small" style="color: #4b5563;">過敏原資訊</label>
            <div class="input-group">
              <span class="input-group-text bg-white" style="border-color: #fed7aa; color: #9a3412;">⚠️</span>
              <input
                v-model="formData.allergenInfo"
                type="text"
                class="form-control"
                placeholder="例如：含堅果、大豆"
                style="color: #374151; border-color: #fed7aa; background-color: #ffffff; font-weight: 500;"
              >
            </div>
          </div>

          <div class="col-md-3">
            <label class="form-label fw-bold small" style="color: #4b5563;">圖片網址</label>
            <input v-model="formData.imageUrl" type="text" class="form-control form-control-solid bg-white" placeholder="圖片 URL..." style="color: #374151; border-color: #fed7aa;">
          </div>
        </div>

        <div class="text-end mt-4">
          <button @click="handleToggleStatus" class="btn btn-light-danger px-3 py-2 me-2 fw-bold small" style="border-radius: 8px; border: 1px solid #fecaca; color: #dc2626;">
            快速上/下架切換
          </button>
          <!-- 🎯 ADMIN 專用：永久刪除按鈕 -->
          <button v-if="isAdmin" @click="handlePermanentDelete" class="btn px-3 py-2 me-2 fw-bold small text-white" style="border-radius: 8px; background-color: #7f1d1d; border: none;">
            🗑️ 永久刪除
          </button>
          <button @click="handleUpdateMenu" class="btn px-4 py-2 text-white fw-bold shadow-sm" style="background-color: #ea580c; background-image: linear-gradient(135deg, #f97316 0%, #ea580c 100%); border: none; border-radius: 8px;">
            <i class="fa-solid fa-square-check me-2"></i>確認並儲存修改
          </button>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div class="p-3 border-bottom d-flex align-items-center justify-content-between bg-light">
          <h6 class="mb-0 fw-bold" style="color: #374151;"><i class="fa-solid fa-list-check me-2" style="color: #ea580c;"></i>目前可編輯餐點列表</h6>

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
                <th style="color: #374151;">行銷標籤</th>
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
                
                <td>
                  <span v-if="item.featureTags && item.featureTags.length > 0 && item.featureTags[0]" class="badge border" style="background-color: #fff7ed; color: #c2410c; border-color: #fed7aa; font-weight: bold;">
                    {{ item.featureTags[0] }}
                  </span>
                  <span v-else class="text-muted small fw-normal">無</span>
                </td>

                <td class="text-muted small" style="max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ item.description || '暫無描述' }}</td>
                <td>
                  <span v-if="item.allergenInfo" class="badge text-amber-800 bg-warning bg-opacity-10 border border-warning-subtle rounded-2" style="font-size: 0.75rem; color: #9a3412; background-color: #ffedd5 !important;">{{ item.allergenInfo }}</span>
                  <span v-else class="text-muted small">無</span>
                </td>
                <td>
                  <span v-if="item.isSelectable === true" class="badge border border-success-subtle" style="color: #16a34a; font-weight: bold; background-color: #f0fdf4 !important;">供應中</span>
                  <span v-else class="badge border border-danger-subtle" style="color: #dc2626; font-weight: bold; background-color: #fef2f2 !important;">已下架</span>
                </td>
                <td class="px-4">
                  <button @click="selectItem(item)" class="btn btn-sm text-white shadow-sm" style="background-color: #ea580c; background-image: linear-gradient(135deg, #f97316 0%, #ea580c 100%); opacity: 0.85; border: none; font-weight: bold; font-size: 12px; padding: 6px 12px; border-radius: 6px;">
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