# 敘日餐廳管理系統

餐廳訂位、點餐、付款與營運後台整合系統。專案目標不是單純完成 CRUD，而是把顧客前台流程與餐廳內部營運資料串成同一套系統：門市狀態、訂位容量、菜單供應、訂單付款、消息發布與 FAQ 客服知識庫都由後台統一維護。

線上展示版只部署前端，遠端 MySQL/RDS 已停用；完整後台管理、登入權限、資料寫入與付款回寫流程請使用本機 MySQL + Spring Boot 環境展示。

## System Overview

```text
Customer Web (Vue)
  ├─ Home / Store / News / FAQ
  ├─ Reservation
  ├─ Menu / Order
  └─ Member

Admin Web (Vue)
  ├─ Homepage / Store / News / FAQ
  ├─ Reservation / Table / Time Slot
  ├─ Menu / Price
  ├─ Order / Dashboard
  └─ Member

Spring Boot API
  ├─ Security / JWT / Role check
  ├─ Service layer business rules
  ├─ JPA repositories
  └─ MySQL schema + migrations
```

## Core Capabilities

| Area | Capability |
| --- | --- |
| Member | 註冊、登入、JWT 身分驗證、會員資料、點數紀錄 |
| Reservation | 可訂時段、桌位容量、訂金欄位、訂位後台、桌位分配 |
| Order | 內用/外帶點餐、購物車、訂單狀態、付款狀態、後台管理 |
| Menu | 菜單分類、餐點上下架、分店菜單與價格設定 |
| Store | 門市資訊、營業時間、公休日、桌位、圖片、特色標籤、營業狀態 |
| Homepage | 首頁 Hero、品牌文案、精選門市、精選消息 |
| News | 品牌公告、門市公告、發布/下架、軟刪除 |
| FAQ | FAQ 知識庫、客服浮窗、搜尋命中、未命中分析 |

## Kevin Lin Ownership

我主要負責品牌營運後台相關模組，以及線上展示版在無後端資料庫時的前端 fallback 設計。

| Module | Responsibility | Engineering Focus |
| --- | --- | --- |
| Homepage | 首頁設定、Hero 輪播、精選門市、精選消息 | 單例設定資料、前台 fallback、防止首頁空白 |
| Store | 門市 CRUD、營業時間、公休日、桌位、圖片、特色標籤 | 店長權限邊界、營業狀態計算、跨模組狀態供應 |
| News | 最新消息前後台、發布/下架、軟刪除 | 保留歷史資料、zero datetime migration |
| FAQ | FAQ 前台、客服浮窗、後台管理、搜尋紀錄 | 可控搜尋、未命中分析、營運補題循環 |
| Demo Data | 前端 demo fallback data | 無雲端資料庫仍可展示核心前台流程 |

## Engineering Decisions

### Backend owns authorization

後台按鈕隱藏只能改善使用體驗，不能作為權限邊界。門市管理相關操作在後端 service layer 依登入者角色與所屬門市檢查：

- `ADMIN` 可管理全部門市。
- `MANAGER` 只能管理自己所屬門市。
- 非授權門市操作會被後端拒絕，不依賴前端路由或 UI 狀態。

### Store availability is derived state

前台顯示「營業中」不是只讀單一狀態欄位。系統會結合：

- 門市啟用狀態
- 每週營業時段
- 公休日/臨時休業
- 目前系統時間

這讓訂位、點餐、門市資訊可以共用同一套營運狀態判斷。

### FAQ search is controlled, not generative

FAQ 客服不是生成式 AI。餐飲規則涉及訂金、退款、付款與門市政策，回答必須可控、可追溯。搜尋邏輯以後台知識庫為資料源，依標題、關鍵字、內文做權重排序；後台同時保留查詢紀錄與未命中問題，讓營運人員補齊內容。

### Demo fallback keeps frontend useful without cloud database

遠端 RDS 已停用後，線上版若完全依賴 API 會變成空頁。前端在主要展示頁保留 demo fallback data：

- API 回空陣列或連線失敗時使用 demo stores/menu/news/FAQ。
- 線上版可展示首頁、菜單、點餐、門市、消息、FAQ。
- 完整資料寫入與權限流程仍以本機後端為準。

這是作品集展示取捨，不是正式資料架構。

## Tech Stack

| Layer | Stack |
| --- | --- |
| Frontend | Vue 3, Vite, Vue Router, Pinia, Axios |
| Backend | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Database | MySQL 8 |
| API Docs | springdoc-openapi / Swagger UI |
| Payment Integration | ECPay sandbox, LINE Pay sandbox |
| Deployment | Vercel frontend demo |
| Verification | Maven test, Vite build, npm audit, Playwright smoke test |

## Repository Layout

```text
.
├── backend/
│   ├── src/main/java/com/restaurant/
│   │   ├── config/              # Security, CORS, Swagger
│   │   ├── common/              # Shared response/error types
│   │   ├── member/              # Auth, member, staff, point
│   │   ├── reservation/         # Reservation, time slot, capacity, deposit
│   │   ├── order/               # Order, payment, admin dashboard
│   │   ├── menu/                # Category, menu item, store menu
│   │   ├── store/               # Store, hours, holidays, tables, features
│   │   ├── homepage/            # Homepage setting
│   │   ├── news/                # News article
│   │   └── faq/                 # FAQ and search logs
│   └── src/main/resources/
├── restaurant-admin-frontend/
│   ├── src/api/                 # Axios clients + demo fallback data
│   ├── src/components/          # Shared UI, support chat widget
│   ├── src/layouts/             # Customer/Admin layouts
│   ├── src/router/              # Routes and auth redirect
│   └── src/views/               # Customer/Admin pages
├── sql/                         # Schema, seed data, migrations
├── docs/                        # Supplementary docs
└── README.md
```

## Main Routes

### Customer

| Route | Purpose |
| --- | --- |
| `/home` | 品牌首頁 |
| `/reservation` | 顧客訂位 |
| `/menu` | 菜單瀏覽 |
| `/order` | 點餐流程 |
| `/store` | 分店資訊 |
| `/news` | 最新消息 |
| `/faq` | FAQ 查詢 |
| `/login`, `/register`, `/profile` | 會員流程 |

### Admin

| Route | Purpose |
| --- | --- |
| `/admin/home` | 後台首頁 |
| `/admin/reservation*` | 訂位、桌位、時段設定 |
| `/admin/menu-*` | 菜單建立、編輯、設定 |
| `/admin/order-*` | 訂單管理與營收 dashboard |
| `/admin/member` | 會員管理 |
| `/admin/store` | 門市營運管理 |
| `/admin/homepage` | 首頁內容管理 |
| `/admin/news` | 最新消息管理 |
| `/admin/faqs`, `/admin/faq-analytics` | FAQ 與搜尋分析 |

## Local Setup

### Requirements

- Java 17
- Maven
- Node.js `^20.19.0 || >=22.12.0`
- MySQL 8

### 1. Create database

```bash
mysql -u root -p
CREATE DATABASE restaurant_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit
```

### 2. Import schema and demo data

```bash
for f in sql/*.sql; do
  echo "== $f"
  mysql --force -u root -p restaurant_db < "$f"
done
```

### 3. Configure backend

```bash
cp backend/src/main/resources/application-dev.properties.example \
  backend/src/main/resources/application-dev.properties
```

Update local credentials:

```properties
spring.datasource.username=root
spring.datasource.password=your_mysql_password
spring.mail.username=your_gmail
spring.mail.password=your_gmail_app_password
```

### 4. Run backend

```bash
cd backend
mvn spring-boot:run
```

Default endpoints:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### 5. Run frontend

```bash
cd restaurant-admin-frontend
npm install
npm run dev
```

Default frontend:

- `http://localhost:5173`

If backend port changes:

```bash
VITE_API_TARGET=http://localhost:8081 npm run dev
```

## Demo Accounts

Seed password:

```text
password123
```

| Role | Account |
| --- | --- |
| Admin | `admin@xuri.com` |
| Manager | `manager@xuri.com` |
| Store manager demo | `storemanager@store.local` |
| Staff | `staff@xuri.com` |
| Member | `user1@example.com` |
| Member | `user2@example.com` |

## Verification

Frontend:

```bash
cd restaurant-admin-frontend
npm run build
npm audit --omit=dev
```

Backend:

```bash
cd backend
mvn test
```

Smoke test checklist:

- Customer `/menu` renders menu categories and items.
- Customer `/order` can choose store/time and show menu items.
- FAQ search for `訂金可以退嗎` returns a deposit-related answer.
- Admin login can reach store/news/FAQ/homepage pages.
- Manager role cannot modify another store.

## Deployment Notes

- Current hosted demo is frontend-only.
- Remote RDS is intentionally disabled to avoid ongoing cost.
- Production deployment would need managed MySQL, backend hosting, fixed CORS origin, secure secret management, and payment callback domains.
- No public demo URL is stored in this README by design.

Deploy frontend when needed:

```bash
cd restaurant-admin-frontend
npx vercel --prod --yes
```

## Known Production Gaps

| Area | Current Demo State | Production Direction |
| --- | --- | --- |
| CORS | Local/demo allowlist | Restrict to production frontend origin |
| Auth storage | Frontend token storage | Move toward httpOnly cookie or stronger XSS controls |
| Public transactional APIs | Some demo paths remain permissive | Add member auth or scoped access token |
| SQL migrations | Ordered SQL scripts | Consolidate into repeatable migration workflow |
| Assets | Large images remain | Compress WebP/AVIF, add stricter lazy loading |
| Observability | Local logs | Add structured logs and deployment health checks |

## Branch Policy

- `main`: stable portfolio/demo branch.
- `develop`: integration branch, kept in sync with main after verified changes.
- Feature work should land in `develop`, pass verification, then merge into `main`.
