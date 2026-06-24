# Member 模組更新說明（僅第一～第五順位）

這一版是從原始 `restaurant-admin-backend(14).zip` 重新修改，**沒有沿用上一版會動到 order / reservation 的改動**。

## 本次完成範圍

### 第一順位：後台入口與登入流程

- 新增後台 `/admin/member` 路由。
- 登入後依角色導向：
  - `CUSTOMER` → `/profile`
  - `STAFF` / `MANAGER` / `ADMIN` → `/admin/home`
- 修正 `src/api/http.js`：
  - 改為讀取 `localStorage.accessToken`
  - 修正原本帶入不存在的 `accessToken` 變數問題
  - 401 時清除登入狀態並導回登入頁
- 後台側欄新增「員工管理」入口。
- 將後台 layout 中原本指向不存在 `/admin/profile` 的分店入口改成既有 `/admin/store`。

### 第二順位：後台管理首頁排版優化

- 重做 `src/views/admin/Home.vue`。
- 新增 member 模組可獨立完成的統計卡：
  - 會員總數
  - 今日新增會員
  - 在職員工
  - 鑽石會員人數
- 新增會員等級分布：
  - 銅卡 / 銀卡 / 金卡 / 鑽石
- 新增跨模組對接狀態提示：
  - 消費紀錄等待訂單組 API
  - 訂位紀錄等待訂位組 API

### 第三順位：員工管理頁

- 完成 `src/views/admin/member/member.vue`。
- 功能包含：
  - 員工清單
  - 搜尋姓名 / Email / 電話 / 員編 / 角色
  - 新增員工
  - 設為離職
  - 顯示角色、門市、入職日、狀態
- 前端使用既有 member API：
  - `GET /api/members/staff`
  - `POST /api/members/staff`
  - `PUT /api/members/staff/{staffId}/resign`

### 第四順位：會員中心 tab 架構

- 重做 `src/views/customer/member/profile.vue`。
- 新增 tab：
  - 基本資料
  - 點數紀錄
  - 消費紀錄
  - 訂位紀錄
- 已完成：
  - 基本資料查詢 / 修改姓名與電話
  - 密碼修改
  - 點數餘額
  - 會員等級
  - 點數紀錄表格
  - 集點規則彈窗
- Email 與生日仍維持不可修改。

### 第五順位：消費紀錄與訂位紀錄對接準備

本次沒有修改 order / reservation 模組檔案。會員中心只先保留消費紀錄與訂位紀錄 tab，並顯示對接提示。

建議訂單組提供：

```http
GET /api/orders/me
```

後端請從 JWT 取得目前登入會員，不要讓前端傳 userId。

建議回傳欄位：

```text
orderId
storeName
orderType
totalAmount
finalAmount
pointsUsed
pointsEarned
status
createdAt
```

建議訂位組提供：

```http
GET /api/reservations/me
```

後端請從 JWT 取得目前登入會員，不要讓前端傳 userId。

建議回傳欄位：

```text
reservationId
storeName
reservationDate
timeSlot
partySize
status
tableName
specialRequest
createdAt
```

## 後端新增 / 修改檔案

新增：

```text
backend/src/main/java/com/restaurant/member/controller/MemberAdminController.java
backend/src/main/java/com/restaurant/member/dto/MemberAdminSummaryResponse.java
```

修改：

```text
backend/src/main/java/com/restaurant/member/repository/UserRepository.java
backend/src/main/java/com/restaurant/member/repository/MemberProfileRepository.java
backend/src/main/java/com/restaurant/member/repository/StaffRepository.java
backend/src/main/java/com/restaurant/member/service/UserService.java
backend/src/main/java/com/restaurant/member/service/impl/UserServiceImpl.java
```

## 前端新增 / 修改檔案

修改：

```text
restaurant-admin-frontend/src/api/http.js
restaurant-admin-frontend/src/api/member.js
restaurant-admin-frontend/src/layouts/AdminLayout.vue
restaurant-admin-frontend/src/router/index.js
restaurant-admin-frontend/src/views/admin/Home.vue
restaurant-admin-frontend/src/views/admin/member/member.vue
restaurant-admin-frontend/src/views/customer/member/login.vue
restaurant-admin-frontend/src/views/customer/member/profile.vue
```

## 明確沒有修改的範圍

這版沒有修改：

```text
backend/src/main/java/com/restaurant/order/**
backend/src/main/java/com/restaurant/reservation/**
restaurant-admin-frontend/src/views/admin/order/**
restaurant-admin-frontend/src/views/customer/order/**
restaurant-admin-frontend/src/views/admin/reservation/**
restaurant-admin-frontend/src/views/customer/reservation/**
```

## 驗證結果

已執行：

```bash
cd restaurant-admin-frontend
npm install
npm run build
```

結果：前端 build 成功。

build 過程仍有原本專案的字型 / 圖片相對路徑 warning，例如 boxicons 與部分圖片未在 build time resolve，這些是原專案既有資源路徑警告，不是本次 member 修改造成的編譯失敗。

後端部分此執行環境沒有 `mvn` 指令，因此未執行 `mvn test` 或 `mvn package`。下載後建議用 IntelliJ 或本機 Maven 跑一次後端編譯確認。
