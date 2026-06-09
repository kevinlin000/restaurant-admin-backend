<script setup>
import { ref } from 'vue'

// 🚀 100% 對齊 8 大核心欄位（彌生軒、大戶屋風格豪華定食菜單）
const menuItems = ref([
  { id: 1, categoryId: 1, itemName: "🍱 蜜汁醬烤雞肉定食", price: 340, description: "鮮嫩雞腿肉以特製照燒醬慢火烘烤，搭配現碾金芽米飯、味噌湯與季節小菜。", imageUrl: "https://images.unsplash.com/photo-1546069901-ba9599a7e63c", status: "AVAILABLE", allergenInfo: "本產品含有大豆、麩質小麥" },
  { id: 2, categoryId: 2, itemName: "🍤 熟成炸豬排滑蛋丼", price: 290, description: "嚴選在地熟成豬里肌，現炸金黃酥脆，淋上特製高湯與滑嫩嚴選鮮蛋液。", imageUrl: "https://images.unsplash.com/photo-1583623025817-d180a2221d0a", status: "AVAILABLE", allergenInfo: "本產品含有雞蛋、麩質" }
])

const newItem = ref({ itemName: '', price: '', description: '', imageUrl: '', categoryId: 1, status: 'AVAILABLE', allergenInfo: '' })

const handleAddMenu = () => {
  if (!newItem.value.itemName || !newItem.value.price) {
    alert('請填寫完整餐點名稱與價格！')
    return
  }
  menuItems.value.push({
    id: menuItems.value.length + 1,
    categoryId: Number(newItem.value.categoryId),
    itemName: newItem.value.itemName,
    price: Number(newItem.value.price),
    description: newItem.value.description || "日式職人手作美味。",
    imageUrl: newItem.value.imageUrl || "https://images.unsplash.com/photo-1546069901-ba9599a7e63c",
    status: newItem.value.status,
    allergenInfo: newItem.value.allergenInfo || "無特殊過敏原提示。"
  })
  newItem.value = { itemName: '', price: '', description: '', imageUrl: '', categoryId: 1, status: 'AVAILABLE', allergenInfo: '' }
  alert('🎉 【後台模擬】成功新增一筆定食專賣店餐點！')
}
</script>

<template>
  <div class="container-fluid px-4 py-3">
    <h3 class="mb-5 text-secondary fw-bold">➕ 菜單管理 <span class="text-muted fs-5 fw-normal">/ 新增全新項目</span></h3>

    <div class="card mb-5 border-0 shadow-sm" style="background-color: #fbfbfd;">
      <div class="card-body p-4">
        <h5 class="card-title fw-bold mb-4" style="color: #4a3728;">✨ 填寫日式定食餐點資訊</h5>
        <div class="row g-3">
          <div class="col-md-4">
            <label class="form-label small fw-bold text-dark">餐點名稱</label>
            <input v-model="newItem.itemName" type="text" class="form-control border-light-subtle" placeholder="例如：炭烤鯖魚定食">
          </div>
          <div class="col-md-2">
            <label class="form-label small fw-bold text-dark">價格 (NTD)</label>
            <input v-model="newItem.price" type="number" class="form-control border-light-subtle" placeholder="價格">
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-bold text-dark">餐點分類</label>
            <select v-model="newItem.categoryId" class="form-select border-light-subtle">
              <option :value="1">精緻定食</option>
              <option :value="2">和風丼飯</option>
              <option :value="3">烏龍麵/麵類</option>
              <option :value="4">職人單品/壽司</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-bold text-dark">上架狀態</label>
            <select v-model="newItem.status" class="form-select border-light-subtle">
              <option value="AVAILABLE">上架供應</option>
              <option value="UNAVAILABLE">暫不供應</option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-bold text-dark">餐點詳細描述</label>
            <input v-model="newItem.description" type="text" class="form-control border-light-subtle" placeholder="餐點描述...">
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-bold text-dark">過敏原資訊</label>
            <input v-model="newItem.allergenInfo" type="text" class="form-control border-light-subtle" placeholder="例如：含有甲殼類...">
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-bold text-dark">圖片網址</label>
            <input v-model="newItem.imageUrl" type="text" class="form-control border-light-subtle" placeholder="圖片 URL...">
          </div>
          <div class="col-12 text-end mt-4">
            <button @click="handleAddMenu" class="btn text-white fw-bold shadow-sm px-4" style="background-color: #4a3728;">💾 儲存並上架項目</button>
          </div>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div class="px-4 py-3 border-bottom bg-light">
          <h5 class="mb-0 fw-bold text-dark fs-6">🍱 目前定食餐點列表</h5>
        </div>
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light text-muted small uppercase">
              <tr>
                <th class="px-4">ID</th>
                <th>分類</th>
                <th>餐點名稱</th>
                <th>價格</th>
                <th>餐點描述</th>
                <th>過敏原</th>
                <th class="px-4">狀態</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in menuItems" :key="item.id">
                <td class="px-4 text-secondary">#{{ item.id }}</td>
                <td>
                  <span v-if="item.categoryId === 1" class="badge bg-secondary">精緻定食</span>
                  <span v-else-if="item.categoryId === 2" class="badge bg-secondary">和風丼飯</span>
                  <span v-else-if="item.categoryId === 3" class="badge bg-secondary">麵類</span>
                  <span v-else-if="item.categoryId === 4" class="badge bg-secondary">單品/壽司</span>
                </td>
                <td><span class="text-dark fw-semibold">{{ item.itemName }}</span></td>
                <td class="text-secondary">${{ item.price }}</td>
                <td class="text-muted small" style="max-width: 250px;">{{ item.description }}</td>
                <td class="text-warning small">{{ item.allergenInfo || '無' }}</td>
                <td class="px-4">
                  <span v-if="item.status === 'AVAILABLE'" class="badge" style="color: #71dd37; background-color: #e8fadf;">供應中</span>
                  <span class="badge" style="color: #ff3e1d; background-color: #ffe5e1;" v-else>已下架</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>