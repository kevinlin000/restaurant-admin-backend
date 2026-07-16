# 敘日餐廳管理系統

餐廳訂位、點餐、付款與品牌營運後台整合專題。

> Main 分支目前作為 Kevin Lin 個人作品集展示版本使用。Vercel 線上版不連接雲端資料庫，採前端 demo fallback data 呈現可瀏覽內容；完整後台、登入、資料庫與付款流程請用本機環境展示。

## 展示入口

建議展示路徑：

- `/home`：首頁、品牌內容、精選門市、最新消息
- `/menu`：菜單瀏覽、分類、門市菜單 fallback
- `/order`：點餐流程、購物車、門市與取餐時間
- `/store`：分店資訊與營業狀態
- `/news`：最新消息
- `/faq`：FAQ 與客服查詢

線上版適合給面試官快速瀏覽 UI 與使用流程。後端 API、資料庫權限、後台管理與付款回寫要用本機版本看。

## 專案重點

敘日不是單一功能頁，而是把餐廳真實營運流程串起來：

- 顧客端：訂位、菜單、點餐、付款、門市資訊、消息、FAQ。
- 後台端：會員、訂位、訂單、菜單、門市、首頁、消息、FAQ 管理。
- 營運邏輯：門市營業狀態、店長權限、訂金/付款狀態、FAQ 搜尋與未命中分析。
- Demo 策略：無免費後端資料庫時，前端保留展示資料，避免線上作品變成空頁。

## Kevin Lin 負責範圍

我主要負責品牌營運後台與前台對應展示：

| 模組 | 內容 |
| --- | --- |
| 首頁管理 | Hero 輪播、品牌文案、精選門市、精選消息、後台設定 |
| 門市管理 | 門市資料、營業時間、公休日、桌位、特色標籤、店長權限 |
| 最新消息 | 品牌公告、門市公告、發布/下架、軟刪除、zero datetime cleanup |
| FAQ 客服 | FAQ 知識庫、客服浮窗、搜尋命中、未命中分析 |
| Demo fallback | Vercel 無資料庫時仍顯示菜單、門市、消息與 FAQ |

### 我想讓面試官看到的深度

- **權限邊界放後端**：店長只能管理自己門市，不靠前端隱藏按鈕防呆。
- **營業狀態不是單一欄位**：門市是否營業會結合營業時間、公休日與目前時間。
- **內容管理動態化**：首頁、消息、門市、FAQ 都由後台資料驅動，不寫死在頁面。
- **FAQ 搜尋可控**：不是 AI 幻覺回答，而是基於後台知識庫、關鍵字與命中權重。
- **資料保留與清理**：消息採軟刪除；遇到 MySQL zero date 問題時用 migration 清髒資料。
- **展示可靠性**：AWS RDS 已停用，線上版用前端 fallback data，避免面試官看到空畫面。

## 技術棧

| 層級 | 技術 |
| --- | --- |
| 前端 | Vue 3, Vite, Pinia, Vue Router, Axios |
| 後端 | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA |
| 資料庫 | MySQL 8 |
| 文件 | Swagger / OpenAPI |
| 部署 | Vercel frontend demo |
| 測試/驗證 | Maven test, Vite build, npm audit, Playwright smoke test |

## 專案結構

```text
.
├── backend/                    # Spring Boot API
│   └── src/main/java/com/restaurant/
├── restaurant-admin-frontend/   # Vue 前台與後台
│   └── src/
│       ├── api/                # API 與 demo fallback data
│       ├── components/         # 共用元件
│       ├── layouts/            # 前台/後台 layout
│       ├── router/             # 路由與登入導流
│       └── views/              # customer/admin 頁面
├── sql/                        # 建表、demo data、migration
├── docs/                       # 補充文件
└── README.md
```

## 本機啟動

### 1. 準備 MySQL

```bash
mysql -u root -p
CREATE DATABASE restaurant_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit
```

依序匯入 SQL：

```bash
for f in sql/*.sql; do
  echo "== $f"
  mysql --force -u root -p restaurant_db < "$f"
done
```

### 2. 設定後端

```bash
cp backend/src/main/resources/application-dev.properties.example \
  backend/src/main/resources/application-dev.properties
```

修改：

```properties
spring.datasource.username=root
spring.datasource.password=你的 MySQL 密碼
spring.mail.username=你的 Gmail
spring.mail.password=你的 Gmail App Password
```

啟動後端：

```bash
cd backend
mvn spring-boot:run
```

後端預設：

- API：http://localhost:8080
- Swagger：http://localhost:8080/swagger-ui.html

### 3. 啟動前端

```bash
cd restaurant-admin-frontend
npm install
npm run dev
```

前端預設：http://localhost:5173

若後端不是 `8080`：

```bash
VITE_API_TARGET=http://localhost:8081 npm run dev
```

## Demo 帳號

測試資料預設密碼皆為：

```text
password123
```

| 角色 | 帳號 |
| --- | --- |
| 系統管理員 | `admin@xuri.com` |
| 店長 | `manager@xuri.com` |
| 門市展示店長 | `storemanager@store.local` |
| 員工 | `staff@xuri.com` |
| 會員 | `user1@example.com` |
| 會員 | `user2@example.com` |

## 常用指令

前端：

```bash
cd restaurant-admin-frontend
npm run build
npm audit --omit=dev
npx vercel --prod --yes
```

後端：

```bash
cd backend
mvn test
mvn spring-boot:run
```

Git：

```bash
git checkout develop
git pull --ff-only
git checkout main
git merge --no-ff develop
git push origin main
```

## 線上版限制

- Vercel 版目前只部署前端。
- AWS RDS 已停用，線上版不連接遠端 MySQL。
- 後台登入、資料寫入、付款回寫等完整流程需本機後端與 MySQL。
- 前端 demo fallback data 用於面試展示，不等同正式資料來源。

## 已知正式化項目

若要從作品集 demo 推向正式環境，優先處理：

- CORS 改成固定正式網域。
- 訂位、訂單、付款 API 補完整權限或 access token 邊界。
- JWT 儲存從 localStorage 改成更安全策略，例如 httpOnly cookie。
- 圖片壓縮成 WebP/AVIF，補 lazy loading 與 code splitting。
- SQL migration 整理成可重複重建的新環境流程。

## 分支狀態

- `main`：Kevin 個人展示穩定版，Vercel production 來源。
- `develop`：整合開發版，目前與 main 保持同步。

成果發表後，main 可視為作品集版本；後續修改先進 develop，驗證後再 merge 回 main。
