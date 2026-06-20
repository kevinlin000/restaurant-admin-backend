# 敘日 - 餐廳管理系統

> JAVA Project— 餐廳訂位與點餐管理系統

## 專案概述
「敘日」—— 不只是旭日的延伸，更是重逢的起點。
「敘」是敘舊，是放下手機後的深度對談；「日」是時光，是歲月淬鍊出的滋味。
我們參考「敘日」對食材與職人精神的堅持，但在系統設計上，我們更想強調「餐桌上的連結」。
專案目的是開發一套完整的餐廳管理系統，涵蓋會員、訂位、訂餐、菜單、門市五大核心模組。

## 技術棧

| 層級 | 技術 |
|------|------|
| 後端 | Spring Boot 3.x + Spring Security + Spring Data JPA |
| 前端 | Vue 3 + Vite + Pinia + Axios |
| 資料庫 | MySQL 8.0 |
| API 規範 | RESTful API + Swagger/OpenAPI |
| 版本控制 | Git + GitHub |

## 五大核心模組與分工

| 模組 | 負責人 | 分支名稱 | 說明 |
|------|--------|----------|------|
| 會員管理 | (填入姓名) | `feature/member` | 註冊登入、權限控管、點數系統 |
| 訂位功能 | (填入姓名) | `feature/reservation` | 時段預約、人數控管、庫存邏輯 |
| 訂餐功能 | (填入姓名) | `feature/order` | 線上點餐、訂單管理 |
| 價目/菜單 | (填入姓名) | `feature/menu` | 分類展示、價格設定 |
| 找門市 | (填入姓名) | `feature/store` | 據點資訊、分店配置、桌位管理 |

## 專案結構

```
restaurant-project/
├── backend/                          # Spring Boot 後端
│   ├── src/main/java/com/restaurant/
│   │   ├── config/                   # 設定檔（CORS、Security、Swagger）
│   │   ├── controller/               # API Controller
│   │   ├── dto/                      # 資料傳輸物件
│   │   │   ├── request/              # 前端傳入的請求格式
│   │   │   └── response/             # 後端回傳的回應格式
│   │   ├── entity/                   # JPA Entity（對應資料表）
│   │   ├── repository/               # Spring Data JPA Repository
│   │   ├── service/                  # Service 介面
│   │   │   └── impl/                 # Service 實作
│   │   ├── exception/                # 自訂例外處理
│   │   ├── security/                 # Spring Security 相關
│   │   └── util/                     # 工具類
│   └── src/main/resources/
│       ├── application.properties    # 主設定檔（不要 commit 敏感資訊）
│       ├── application-dev.properties   # 開發環境設定
│       └── application-prod.properties  # 正式環境設定
│
├── frontend/                         # Vue 3 前端
│   ├── src/
│   │   ├── api/                      # Axios API 封裝
│   │   ├── assets/                   # 靜態資源（圖片、CSS）
│   │   ├── components/               # 共用元件
│   │   ├── router/                   # Vue Router 路由
│   │   ├── store/                    # Pinia 狀態管理
│   │   └── views/                    # 頁面（依模組分資料夾）
│   │       ├── member/
│   │       ├── reservation/
│   │       ├── order/
│   │       ├── menu/
│   │       └── store/
│   └── package.json
│
├── sql/                              # SQL 腳本
│   ├── 01_create_tables.sql          # 建表語法（按順序）
│   ├── 02_insert_test_data.sql       # 測試資料
│   └── 03_indexes.sql                # 索引優化
│
├── docs/                             # 文件
│   ├── api/                          # API 文件
│   ├── database/                     # ER Model、欄位說明
│   └── meeting-notes/                # 會議記錄
│
├── .gitignore
├── .env.example                      # 環境變數範本
└── README.md
```

## 快速開始

### 1. Clone 專案

```bash
git clone https://github.com/你的帳號/restaurant-project.git
cd restaurant-project
```

### 2. 資料庫設定

```bash
# 連線到 MySQL，建立資料庫
mysql -u root -p
CREATE DATABASE restaurant_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 執行建表腳本
mysql -u root -p restaurant_db < sql/01_create_tables.sql
mysql -u root -p restaurant_db < sql/02_insert_test_data.sql
```

### 3. 後端啟動

```bash
cd backend
# 複製設定檔範本
cp src/main/resources/application-dev.properties.example src/main/resources/application-dev.properties
# 修改資料庫連線資訊與 Gmail 驗證信設定
# spring.mail.password 請填 Gmail App Password，不是 Gmail 登入密碼
# 啟動
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. 前端啟動

```bash
cd frontend
npm install
npm run dev
```

## Git 工作流程

### 分支策略

```
main                    ← 穩定版本，不直接 commit
  └── develop           ← 開發主線，所有功能合併到這裡
       ├── feature/member       ← 會員模組
       ├── feature/reservation  ← 訂位模組
       ├── feature/order        ← 訂餐模組
       ├── feature/menu         ← 菜單模組
       └── feature/store        ← 找門市模組
```

### 每日工作流程

```bash
# 1. 開始工作前，先拉最新的 develop
git checkout develop
git pull origin develop

# 2. 切換到自己的分支，合併 develop 的最新內容
git checkout feature/store
git merge develop

# 3. 寫 code、commit
git add .
git commit -m "feat(store): 新增門市列表 API"

# 4. 推上去
git push origin feature/store

# 5. 功能完成後，在 GitHub 上發 Pull Request 合併到 develop
```

### Commit 訊息規範

```
feat(模組):    新功能        例: feat(store): 新增門市 CRUD API
fix(模組):     修 Bug        例: fix(member): 修正登入驗證邏輯
docs(模組):    文件更新      例: docs(database): 更新 ER Model
style(模組):   格式調整      例: style(store): 統一程式碼縮排
refactor(模組): 重構         例: refactor(order): 抽取共用方法
test(模組):    測試          例: test(store): 新增門市 Service 測試
```

## API 規範

### 統一回應格式

```json
{
  "success": true,
  "message": "操作成功",
  "data": { ... }
}
```

### 錯誤回應格式

```json
{
  "success": false,
  "message": "找不到該門市",
  "errorCode": "STORE_NOT_FOUND"
}
```

### API 路徑規範

| 模組 | 前綴 |
|------|------|
| 會員 | `/api/members` |
| 訂位 | `/api/reservations` |
| 訂餐 | `/api/orders` |
| 菜單 | `/api/menu-items`, `/api/menu-categories` |
| 門市 | `/api/stores`, `/api/tables` |

## 團隊公約

1. **不要直接 push 到 main 或 develop**，一律用 Pull Request。
2. **每次 commit 前先 pull**，避免衝突。
3. **不要 commit 敏感資訊**（密碼、API Key），用 `.env` 或 `application-dev.properties`（已在 .gitignore）。
4. **每個 PR 至少要有 1 個人 review**。
5. **每天結束前至少 push 一次**，避免程式碼只在自己電腦。
