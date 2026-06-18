-- ============================================================
-- 敘日餐廳訂位系統 - 建表腳本
-- 執行順序：此檔案由上到下依序執行，已處理好外鍵依賴
--
-- 設計重點：
--   1. 人員拆 user / members / staff / role（會員與員工共用同一登入帳號）
--   2. 訂位走 time_slot（預先產生時段）+ reservation_capacity（按桌型人數計數庫存）
--   3. 庫存真相 = reservation_capacity.reserved_count（訂位時扣）；
--      reservation_table = 到店才指派的實體桌（可後填，與計數器分屬不同階段）
--   4. status 一律用 VARCHAR 字串列舉（自帶語意，避免數字魔法碼）
-- ============================================================

-- 建立資料庫
CREATE DATABASE IF NOT EXISTS restaurant_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE restaurant_db;

-- ============================================================
-- 1. brand（品牌）
-- ============================================================
CREATE TABLE brand (
    brand_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_name      VARCHAR(50) NOT NULL COMMENT '品牌名稱',
    brand_logo_url  VARCHAR(500) COMMENT '品牌 Logo 圖片 URL',
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE COMMENT '軟刪除',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT = '品牌表';

-- ============================================================
-- 2. store（門市）
-- ============================================================
CREATE TABLE store (
    store_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_id        BIGINT NOT NULL COMMENT '所屬品牌',
    store_code      VARCHAR(20) NOT NULL UNIQUE COMMENT '分店代號，如 TPE001',
    store_name      VARCHAR(100) NOT NULL COMMENT '門市名稱',
    city            VARCHAR(20) NOT NULL COMMENT '縣市（篩選用）',
    district        VARCHAR(20) NOT NULL COMMENT '區域（篩選用）',
    address         VARCHAR(200) NOT NULL COMMENT '完整地址',
    phone           VARCHAR(20) COMMENT '電話',
    latitude        DECIMAL(10, 7) COMMENT '緯度（找最近門市用）',
    longitude       DECIMAL(10, 7) COMMENT '經度',
    mrt_info        VARCHAR(100) COMMENT '捷運資訊',
    parking_info    VARCHAR(200) COMMENT '停車資訊',
    description     TEXT COMMENT '門市特色介紹',
    main_image_url  VARCHAR(500) COMMENT '主圖 URL（列表頁用，省 JOIN）',
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN'
                    COMMENT 'PREPARING=籌備 / OPEN=營業 / PAUSED=暫停 / CLOSED=停業',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE COMMENT '軟刪除',
    FOREIGN KEY (brand_id) REFERENCES brand(brand_id)
) COMMENT = '門市主表';

-- ============================================================
-- 3. store_hour（營業時間）
-- ============================================================
CREATE TABLE store_hour (
    hour_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id        BIGINT NOT NULL,
    day_of_week     INT NOT NULL COMMENT '1=週一 ... 7=週日（ISO 8601）',
    open_time       TIME NOT NULL COMMENT '開店時間',
    close_time      TIME NOT NULL COMMENT '關店時間',
    meal_period     VARCHAR(20) COMMENT 'LUNCH / DINNER / AFTERNOON_TEA / ALL_DAY',
    is_closed       BOOLEAN NOT NULL DEFAULT FALSE COMMENT '該時段是否公休',
    FOREIGN KEY (store_id) REFERENCES store(store_id),
    UNIQUE KEY uk_store_hour (store_id, day_of_week, meal_period)
) COMMENT = '門市營業時間（一家店多筆，支援多時段）';

-- ============================================================
-- 4. store_holiday（特殊休息日）
-- ============================================================
CREATE TABLE store_holiday (
    holiday_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id        BIGINT NOT NULL,
    holiday_date    DATE NOT NULL COMMENT '休息日期',
    reason          VARCHAR(100) COMMENT '公休原因',
    FOREIGN KEY (store_id) REFERENCES store(store_id),
    UNIQUE KEY uk_store_holiday (store_id, holiday_date)
) COMMENT = '特殊休息日（春節、內訓等）';

-- ============================================================
-- 5. store_image（門市照片）
-- ============================================================
CREATE TABLE store_image (
    image_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id        BIGINT NOT NULL,
    image_url       VARCHAR(500) NOT NULL COMMENT '圖片 URL',
    caption         VARCHAR(100) COMMENT '圖說',
    sort_order      INT DEFAULT 0 COMMENT '排序（小的在前）',
    FOREIGN KEY (store_id) REFERENCES store(store_id)
) COMMENT = '門市照片（一家店多張）';

-- ============================================================
-- 6. store_feature（門市特色標籤）
-- ============================================================
CREATE TABLE store_feature (
    feature_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id        BIGINT NOT NULL,
    feature_key     VARCHAR(40) NOT NULL COMMENT '標籤代碼，如 BUSINESS / FAMILY / PARKING',
    feature_label   VARCHAR(30) NOT NULL COMMENT '前台顯示名稱',
    sort_order      INT DEFAULT 0 COMMENT '排序（小的在前）',
    FOREIGN KEY (store_id) REFERENCES store(store_id),
    UNIQUE KEY uk_store_feature (store_id, feature_key)
) COMMENT = '門市特色標籤（用於情境篩選與推薦排序）';

-- ============================================================
-- 7. table_info（桌位）
-- 註：capacity 已改名為 table_size（幾人座，避免與庫存的 count 欄位混淆）
-- ============================================================
CREATE TABLE table_info (
    table_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id        BIGINT NOT NULL,
    table_number    VARCHAR(10) NOT NULL COMMENT '桌號，如 A01、VIP1',
    table_size      INT NOT NULL COMMENT '幾人座',
    table_type      VARCHAR(20) COMMENT 'REGULAR / BOOTH / VIP_ROOM / BAR',
    zone            VARCHAR(20) COMMENT '樓層或區域，如 1F、2F、露臺',
    status          VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
                    COMMENT 'AVAILABLE=可用 / DISABLED=停用 / MAINTENANCE=維修中',
    is_combinable   BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否可併桌',
    FOREIGN KEY (store_id) REFERENCES store(store_id)
) COMMENT = '桌位資訊（實體桌定義；用來產生 reservation_capacity 的列）';

-- ============================================================
-- 8. role（角色權限）
-- ============================================================
CREATE TABLE role (
    role_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name       VARCHAR(20) NOT NULL COMMENT 'CUSTOMER / STAFF / MANAGER / ADMIN',
    description     VARCHAR(200) COMMENT '角色說明'
) COMMENT = '角色權限表';

-- ============================================================
-- 9. user（登入帳號；會員與員工共用）
-- ============================================================
CREATE TABLE user (
    user_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id         BIGINT NOT NULL DEFAULT 1 COMMENT '預設 CUSTOMER',
    email           VARCHAR(100) NOT NULL UNIQUE COMMENT '登入帳號',
    password_hash   VARCHAR(255) NOT NULL COMMENT '密碼雜湊（BCrypt）',
    name            VARCHAR(50) COMMENT '姓名',
    phone           VARCHAR(20) COMMENT '電話',
    birthday        DATE COMMENT '生日（生日優惠用）',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE COMMENT '軟刪除',
    FOREIGN KEY (role_id) REFERENCES role(role_id)
) COMMENT = '登入帳號主表';

-- ============================================================
-- 9. members（會員資料；與 user 1:1）
-- ============================================================
CREATE TABLE members (
    member_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE COMMENT '連結 user 帳號（1:1）',
    point_balance   INT NOT NULL DEFAULT 0 COMMENT '點數餘額（快取，真相在 point_transaction）',
    member_level    VARCHAR(20) NOT NULL DEFAULT 'BRONZE' COMMENT 'BRONZE / SILVER / GOLD / DIAMOND',
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) COMMENT = '會員資料';

-- ============================================================
-- 10. staff（員工資料；與 user 1:1）
-- ============================================================
CREATE TABLE staff (
    staff_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE COMMENT '連結 user 帳號（1:1）',
    store_id        BIGINT NOT NULL COMMENT '所屬分店',
    staff_no        VARCHAR(20) NOT NULL UNIQUE COMMENT '員工工號',
    hire_date       DATE COMMENT '到職日',
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                    COMMENT 'ACTIVE=在職 / RESIGNED=離職',
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (store_id) REFERENCES store(store_id)
) COMMENT = '員工資料';

-- ============================================================
-- 11. menu_category（菜單分類）
-- ============================================================
CREATE TABLE menu_category (
    category_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name   VARCHAR(50) NOT NULL COMMENT '分類名稱',
    sort_order      INT DEFAULT 0 COMMENT '前台顯示順序'
) COMMENT = '菜單分類';

-- ============================================================
-- 12. menu_item（菜單品項）
-- ============================================================
CREATE TABLE menu_item (
    menu_item_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id     BIGINT NOT NULL,
    item_name       VARCHAR(100) NOT NULL COMMENT '品項名稱',
    description     TEXT COMMENT '品項說明',
    base_price      DECIMAL(10, 2) NOT NULL COMMENT '全國建議售價',
    image_url       VARCHAR(500) COMMENT '品項圖片 URL',
    allergen_info   VARCHAR(200) COMMENT '過敏原資訊（法規要求）',
    is_active       BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'false=暫時下架',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES menu_category(category_id)
) COMMENT = '菜單品項';

-- ============================================================
-- 13. store_menu（各店菜單覆蓋；多對多中介表）
-- ============================================================
CREATE TABLE store_menu (
    store_menu_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id        BIGINT NOT NULL,
    menu_item_id    BIGINT NOT NULL,
    price           DECIMAL(10, 2) COMMENT '該門市售價（null 則用 base_price）',
    is_available    BOOLEAN NOT NULL DEFAULT TRUE COMMENT '該門市是否供應',
    FOREIGN KEY (store_id) REFERENCES store(store_id),
    FOREIGN KEY (menu_item_id) REFERENCES menu_item(menu_item_id),
    UNIQUE KEY uk_store_menu (store_id, menu_item_id)
) COMMENT = '各門市菜單設定';

-- ============================================================
-- 14. time_slot（可訂時段；後台預先產生）
-- ============================================================
CREATE TABLE time_slot (
    slot_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id        BIGINT NOT NULL,
    reservation_date DATE NOT NULL COMMENT '可訂日期',
    start_time      TIME NOT NULL COMMENT '時段開始',
    end_time        TIME NOT NULL COMMENT '時段結束',
    is_open         BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否開放訂位',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (store_id) REFERENCES store(store_id),
    UNIQUE KEY uk_time_slot (store_id, reservation_date, start_time)
) COMMENT = '可訂時段';

-- ============================================================
-- 15. reservation_capacity（時段桌型庫存；訂位真相）
-- 註：原 table_capacity 改名；每 slot 每種 table_size 一列
--     total_count   = 該店該人數座位的桌數（由 table_info 統計產生）
--     reserved_count= 已被訂走的數量（訂位時 +1，取消時 -1）
-- ============================================================
CREATE TABLE reservation_capacity (
    capacity_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    slot_id         BIGINT NOT NULL,
    table_size      INT NOT NULL COMMENT '幾人座（對應 table_info.table_size）',
    total_count     INT NOT NULL COMMENT '該人數桌型總數',
    reserved_count  INT NOT NULL DEFAULT 0 COMMENT '已訂數量',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (slot_id) REFERENCES time_slot(slot_id),
    UNIQUE KEY uk_reservation_capacity (slot_id, table_size)
) COMMENT = '時段桌型庫存（訂位扣此計數器）';

-- ============================================================
-- 16. reservation（訂位）
-- ============================================================
CREATE TABLE reservation (
    reservation_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    store_id        BIGINT NOT NULL COMMENT '冗餘存放方便查詢（slot 已含 store）',
    slot_id         BIGINT NOT NULL,
    party_size      INT NOT NULL COMMENT '用餐人數',
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                    COMMENT 'PENDING / CONFIRMED / CHECKED_IN / COMPLETED / CANCELLED / NO_SHOW',
    deposit_amount  DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '訂金金額',
    payment_status  VARCHAR(20) DEFAULT 'UNPAID' COMMENT 'UNPAID / PAID / REFUNDED',
    special_request TEXT COMMENT '特殊需求備註',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (store_id) REFERENCES store(store_id),
    FOREIGN KEY (slot_id) REFERENCES time_slot(slot_id)
) COMMENT = '訂位紀錄';

-- ============================================================
-- 17. reservation_table（訂位-實體桌指派；到店才填，支援併桌）
-- ============================================================
CREATE TABLE reservation_table (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id  BIGINT NOT NULL,
    table_id        BIGINT NOT NULL,
    FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id),
    FOREIGN KEY (table_id) REFERENCES table_info(table_id),
    UNIQUE KEY uk_reservation_table (reservation_id, table_id)
) COMMENT = '訂位實體桌指派（到店安排，與庫存計數分屬不同階段）';

-- ============================================================
-- 18. orders（訂單）
-- 注意：表名用 orders 不用 order，因為 ORDER 是 SQL 保留字
-- ============================================================
CREATE TABLE orders (
    order_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    store_id        BIGINT NOT NULL,
    table_id        BIGINT COMMENT '用餐桌位（外帶可為 null）',
    reservation_id  BIGINT COMMENT '關聯訂位（可為 null，外帶不需訂位）',
    order_type      VARCHAR(20) NOT NULL DEFAULT 'DINE_IN' COMMENT 'DINE_IN / TAKEOUT',
    total_amount    DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '訂單總金額（鎖定當下價格）',
    final_amount    DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '折抵後實付金額',
    points_used     INT NOT NULL DEFAULT 0 COMMENT '本次折抵點數',
    points_earned   INT NOT NULL DEFAULT 0 COMMENT '本次累積點數',
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                    COMMENT 'PENDING / PREPARING / SERVED / PAID / CANCELLED',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (store_id) REFERENCES store(store_id),
    FOREIGN KEY (table_id) REFERENCES table_info(table_id),
    FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id)
) COMMENT = '訂單主表';

-- ============================================================
-- 19. order_item（訂單明細）
-- ============================================================
CREATE TABLE order_item (
    order_item_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    menu_item_id    BIGINT NOT NULL,
    quantity        INT NOT NULL COMMENT '數量',
    unit_price      DECIMAL(10, 2) NOT NULL COMMENT '下單當時單價（交易快照）',
    subtotal        DECIMAL(10, 2) NOT NULL COMMENT '小計 = quantity x unit_price',
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (menu_item_id) REFERENCES menu_item(menu_item_id)
) COMMENT = '訂單明細';

-- ============================================================
-- 20. payment（付款紀錄）
-- 註：order_id / reservation_id 皆可為 null —— 付餐費填 order_id，付訂金填 reservation_id
-- ============================================================
CREATE TABLE payment (
    payment_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT COMMENT '關聯訂單（付餐費時填）',
    reservation_id  BIGINT COMMENT '關聯訂位（付訂金時填）',
    amount          DECIMAL(10, 2) NOT NULL COMMENT '付款金額',
    payment_method  VARCHAR(20) COMMENT 'CREDIT_CARD / LINE_PAY / CASH',
    payment_status  VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                    COMMENT 'PENDING / SUCCESS / FAILED / REFUNDED',
    transaction_no  VARCHAR(100) COMMENT '金流服務商交易序號',
    paid_at         DATETIME COMMENT '實際付款成功時間',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id)
) COMMENT = '付款紀錄';

-- ============================================================
-- 21. point_transaction（點數異動紀錄；真相表）
-- ============================================================
CREATE TABLE point_transaction (
    tx_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    store_id        BIGINT COMMENT '發生門市（可為 null 表示系統調整）',
    point_change    INT NOT NULL COMMENT '正數=增加，負數=扣除',
    transaction_type VARCHAR(20) NOT NULL COMMENT 'EARN / USE / EXPIRE / ADJUST',
    reference_id    BIGINT COMMENT '關聯訂單或訂位 ID（搭配 transaction_type 判斷）',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (store_id) REFERENCES store(store_id)
) COMMENT = '點數異動紀錄（真相表，members.point_balance 是快取）';
