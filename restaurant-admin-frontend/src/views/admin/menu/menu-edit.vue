<script setup>
import { ref } from 'vue'

const menuItems = ref([
  { id: 1, categoryId: 1, itemName: "🍱 蜜汁醬烤雞肉定食", price: 340, description: "鮮嫩雞腿肉以特製照燒醬慢火烘烤，搭配現碾金芽米飯、味噌湯與季節小菜。", imageUrl: "https://images.unsplash.com/photo-1546069901-ba9599a7e63c", status: "AVAILABLE", allergenInfo: "本產品含有大豆、麩質小麥" },
  { id: 2, categoryId: 2, itemName: "🍤 熟成炸豬排滑蛋丼", price: 290, description: "嚴選在地熟成豬里肌，現炸金黃酥脆，淋上特製高湯與滑嫩嚴選鮮蛋液。", imageUrl: "https://images.unsplash.com/photo-1583623025817-d180a2221d0a", status: "AVAILABLE", allergenInfo: "本產品含有雞蛋、麩質" },
  { id: 3, categoryId: 1, itemName: "🐟 炭烤鯖魚定食", price: 310, description: "嚴選肥美挪威鯖魚，慢火炭烤至外皮金黃酥脆，肉質鮮嫩多汁飽含油脂。", imageUrl: "https://images.unsplash.com/photo-1534604973900-c43ab4c2e0ab", status: "UNAVAILABLE", allergenInfo: "本產品含有魚類" }
])

const editingItem = ref({ id: null, itemName: '', price: '', description: '', imageUrl: '', categoryId: 1, status: 'AVAILABLE', allergenInfo: '' })

const selectItem = (item) => {
  editingItem.value = { ...item }
}

const handleUpdateMenu = () => {
  if (!editingItem.value.id) {
    alert('請先在下方列表中點擊「編輯此項」選擇品項！')
    return
  }
  const index = menuItems.value.findIndex(i => i.id === editingItem.value.id)
  if (index !== -1) {
    menuItems.value[index] = { ...editingItem.value }
    alert('🎉 【後台模擬】定食餐點修改成功！')
    editingItem.value = { id: null, itemName: '', price: '', description: '', imageUrl: '', categoryId: 1, status: 'AVAILABLE', allergenInfo: '' }
  }
}
</script>

<template>
  <div class="container-fluid px-4 py-3">
    <h3 class="mb-5 text-secondary fw-bold">✍️ 菜單管理 <span class="text-muted fs-5 fw-normal">/ 修改現有品項</span></h3>

    <div class="card mb-5 border-0 shadow-sm" style="background-color: #fffbf5;">
      <div class="card-body p-4">
        <h5 class="card-title fw-bold mb-4" style="color: #ffab00;">
          📝 正在編輯品項：{{ editingItem.id ? `#${editingItem.id}` : '未選擇' }}
        </h5>
        <div class="row g-3">
          <div class="col-md-4">
            <label class="form-label small fw-bold text-dark">餐點名稱</label>
            <input v-model="editingItem.itemName" type="text" class="form-control border-light-subtle" placeholder="請點下方列表選擇要修改的餐點">
          </div>
          <div class="col-md-2">
            <label class="form-label small fw-bold text-dark">修改價格</label>
            <input v-model="editingItem.price" type="number" class="form-control border-light-subtle" placeholder="修改價格">
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-bold text-dark">修改分類</label>
            <select v-model="editingItem.categoryId" class="form-select border-light-subtle">
              <option :value="1">精緻定食</option>
              <option :value="2">和風丼飯</option>
              <option :value="3">烏龍麵/麵類</option>
              <option :value="4">職人單品/壽司</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-bold text-dark">修改上架狀態</label>
            <select v-model="editingItem.status" class="form-select border-light-subtle">
              <option value="AVAILABLE">供應中</option>
              <option value="UNAVAILABLE">已下架 (軟刪除)</option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-bold text-dark">描述</label>
            <input v-model="editingItem.description" type="text" class="form-control border-light-subtle">
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-bold text-dark">過敏原</label>
            <input v-model="editingItem.allergenInfo" type="text" class="form-control border-light-subtle">
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-bold text-dark">圖片網址</label>
            <input v-model="editingItem.imageUrl" type="text" class="form-control border-light-subtle">
          </div>
          <div class="col-12 text-end mt-4">
            <button @click="handleUpdateMenu" class="btn text-white fw-bold shadow-sm px-4" style="background-color: #ffab00; border-color: #ffab00;">💾 儲存修改</button>
          </div>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div class="px-4 py-3 border-bottom bg-light">
          <h5 class="mb-0 fw-bold text-dark fs-6">🍽️ 選擇要修改的日式定食項目</h5>
        </div>
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light text-muted small uppercase">
              <tr>
                <th class="px-4">ID</th>
                <th>分類</th>
                <th>餐點名稱</th>
                <th>價格</th>
                <th>狀態</th>
                <th class="px-4">操作</th>
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
                <td>
                  <span v-if="item.status === 'AVAILABLE'" class="badge" style="color: #71dd37; background-color: #e8fadf;">供應中</span>
                  <span class="badge" style="color: #ff3e1d; background-color: #ffe5e1;" v-else>已下架</span>
                </td>
                <td class="px-4">
                  <button @click="selectItem(item)" class="btn btn-sm btn-outline-warning">✍️ 編輯此項</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>