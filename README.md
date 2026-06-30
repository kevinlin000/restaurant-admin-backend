# 敘日 - 餐廳管理系統

> JAVA Project— 餐廳訂位與點餐管理系統

## 專案概述
「敘日」—— 不只是旭日的延伸，更是重逢的起點。
「敘」是敘舊，是放下手機後的深度對談；「日」是時光，是歲月淬鍊出的滋味。
我們參考「敘日」對食材與職人精神的堅持，但在系統設計上，我們更想強調「餐桌上的連結」。
專案目的是開發一套完整的餐廳管理系統，涵蓋會員、訂位、訂餐、菜單、門市、品牌首頁、最新消息、FAQ 客服知識庫與後台營運管理。系統不只處理單次交易，也支援餐廳總部日常營運：門市狀態控管、品牌內容發布、顧客問題沉澱與後台權限分工。

## 技術棧

| 層級 | 技術 |
|------|------|
| 後端 | Spring Boot 3.x + Spring Security + Spring Data JPA |
| 前端 | Vue 3 + Vite + Pinia + Axios |
| 資料庫 | MySQL 8.0 |
| API 規範 | RESTful API + Swagger/OpenAPI |
| 版本控制 | Git + GitHub |

## 核心模組與分工

| 模組 | 負責人 | 分支名稱 | 說明 |
|------|--------|----------|------|
| 會員管理 | (填入姓名) | `feature/member` | 註冊登入、權限控管、點數系統 |
| 訂位功能 | (填入姓名) | `feature/reservation` | 時段預約、人數控管、庫存邏輯 |
| 訂餐與付款 | (填入姓名) | `feature/order` | 線上點餐、購物車、訂單管理、付款流程、智慧推薦 |
| 菜單與價格 | (填入姓名) | `feature/menu`, `feat-menu-final` | 菜單分類、餐點詳情、分店定價、上下架管理 |
| 門市營運管理 | kevinlin | `feature/store` | 據點資訊、分店配置、桌位管理、營業狀態、店長門市權限 |
| 品牌首頁管理 | kevinlin | `feature/homepage-management` | 首頁 Hero、品牌文案、精選門市、精選消息 |
| 最新消息管理 | kevinlin | `feature/news` | 品牌公告、活動消息、門市公告、發布與下架流程 |
| FAQ 客服知識庫 | kevinlin | `feature/faq-support`, `feature/faq-analytics` | 常見問題、客服浮窗、搜尋紀錄、未命中問題分析 |

## kevinlin 負責模組亮點

kevinlin 負責的是「品牌營運後台」相關模組，目標是讓敘日不只是可以訂位與點餐，也能像真實餐飲品牌一樣維護門市、公告、首頁內容與客服知識庫。這些模組共同支援總部與店長的日常營運流程。

### 品牌首頁管理

- 前台首頁呈現品牌主視覺、品牌故事、精選分店、最新消息與 FAQ 入口。
- 後台「首頁管理」可維護 Hero 輪播、首頁文案、精選門市與精選消息。
- 首頁內容由資料庫驅動，避免每次活動或門市主推調整都需要改前端程式碼。
- 登入導流支援原頁回跳，後台管理者未登入時會先導向登入頁，登入成功後回到原本要管理的頁面。

### 門市營運管理

- 前台分店資訊支援多門市展示，包含城市區域、地址、交通、停車、營業狀態與特色標籤。
- 後台分店管理支援門市資料維護、營業時間、公休日、桌位、圖片與特色標籤。
- 權限區分 ADMIN 與 MANAGER：ADMIN 可管理全部門市，MANAGER 僅能管理自己所屬門市。
- 提供單店與全域營業狀態 API，供菜單與點餐模組判斷門市是否開放服務。
- 店長門市權限由後端檢查，避免 MANAGER 透過改 URL 操作其他門市。

### 最新消息管理

- 前台最新消息支援品牌公告、活動消息與門市異動資訊。
- 後台可新增、編輯、發布、下架與排序消息。
- 支援全品牌公告與門市公告，讓總部與分店營運資訊可以集中維護。
- 補充 `sql/16_news_zero_datetime_cleanup.sql`，修復舊 seed 資料的 zero datetime，避免最新消息 API 在 JDBC 讀取時間欄位時發生 500。

### FAQ 與客服查詢紀錄

- 前台 FAQ 提供訂位、訂金、點餐、外帶、門市、會員與服務規則查詢。
- 全站右下角提供 FAQ 客服浮窗，讓顧客可直接搜尋常見問題。
- 後台 FAQ 管理可維護問題、答案、分類、關鍵字、狀態與排序。
- 後台客服查詢紀錄會記錄熱門查詢與未命中問題，協助營運端補強 FAQ 內容。
- FAQ 不是靜態說明頁，而是可被客服浮窗與後台分析共用的知識庫。

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
├── restaurant-admin-frontend/         # Vue 3 前端
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
│   │       ├── store/
│   │       ├── news/
│   │       ├── faq/
│   │       └── admin/
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
cd restaurant-admin-frontend
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
| 門市 | `/api/stores`, `/api/admin/stores` |
| 首頁 | `/api/homepage`, `/api/admin/homepage` |
| 最新消息 | `/api/news`, `/api/admin/news` |
| FAQ | `/api/faqs`, `/api/admin/faqs` |

## 團隊公約

1. **不要直接 push 到 main 或 develop**，一律用 Pull Request。
2. **每次 commit 前先 pull**，避免衝突。
3. **不要 commit 敏感資訊**（密碼、API Key），用 `.env` 或 `application-dev.properties`（已在 .gitignore）。
4. **每個 PR 至少要有 1 個人 review**。
5. **每天結束前至少 push 一次**，避免程式碼只在自己電腦。
