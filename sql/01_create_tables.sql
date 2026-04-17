-- ============================================================
-- 餐廳訂位系統 - 建表腳本
-- 執行順序：此檔案由上到下依序執行，已處理好外鍵依賴
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
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '0=籌備 1=營業 2=暫停 3=停業',
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
    day_of_week     TINYINT NOT NULL COMMENT '1=週一 ... 7=週日（ISO 8601）',
    open_time       TIME NOT NULL COMMENT '開店時間',
    close_time      TIME NOT NULL COMMENT '關店時間',
    meal_period     VARCHAR(20) COMMENT 'LUNCH / DINNER / AFTERNOON_TEA / ALL_DAY',
    is_closed       BOOLEAN NOT NULL DEFAULT FALSE COMMENT '該時段是否公休',
    FOREIGN KEY (store_id) REFERENCES store(store_id)
) COMMENT = '門市營業時間（一家店多筆，支援多時段）';

-- ============================================================
-- 4. store_holiday（特殊休息日）
-- ============================================================
CREATE TABLE store_holiday (
    holiday_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id        BIGINT NOT NULL,
    holiday_date    DATE NOT NULL COMMENT '休息日期',
    reason          VARCHAR(100) COMMENT '公休原因',
    FOREIGN KEY (store_id) REFERENCES store(store_id)
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
-- 6. table_info（桌位）
-- ============================================================
CREATE TABLE table_info (
    table_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id        BIGINT NOT NULL,
    table_number    VARCHAR(10) NOT NULL COMMENT '桌號，如 A01、VIP1',
    capacity        TINYINT NOT NULL COMMENT '容納人數',
    table_type      VARCHAR(20) COMMENT 'REGULAR / BOOTH / VIP_ROOM / BAR',
    zone            VARCHAR(20) COMMENT '樓層或區域，如 1F、2F、露臺',
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '0=停用 1=可用 2=維修中',
    is_combinable   BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否可併桌',
    FOREIGN KEY (store_id) REFERENCES store(store_id)
) COMMENT = '桌位資訊';

-- ============================================================
-- 7. role（角色）
-- ============================================================
CREATE TABLE role (
    role_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name       VARCHAR(20) NOT NULL COMMENT 'CUSTOMER / STAFF / MANAGER / ADMIN',
    description     VARCHAR(200) COMMENT '角色說明'
) COMMENT = '角色權限表';

-- ============================================================
-- 8. member（會員）
-- ============================================================
CREATE TABLE member (
    member_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id         BIGINT NOT NULL DEFAULT 1 COMMENT '預設為 CUSTOMER',
    email           VARCHAR(100) NOT NULL UNIQUE COMMENT '登入帳號',
    password_hash   VARCHAR(255) NOT NULL COMMENT '密碼雜湊（BCrypt）',
    name            VARCHAR(50) COMMENT '姓名',
    phone           VARCHAR(20) COMMENT '電話',
    birthday        DATE COMMENT '生日（生日優惠用）',
    point_balance   INT NOT NULL DEFAULT 0 COMMENT '點數餘額（快取，真相在 point_transaction）',
    member_level    VARCHAR(20) NOT NULL DEFAULT 'BRONZE' COMMENT 'BRONZE / SILVER / GOLD / DIAMOND',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE COMMENT '軟刪除',
    FOREIGN KEY (role_id) REFERENCES role(role_id)
) COMMENT = '會員表';

-- ============================================================
-- 9. menu_category（菜單分類）
-- ============================================================
CREATE TABLE menu_category (
    category_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name   VARCHAR(50) NOT NULL COMMENT '分類名稱',
    sort_order      INT DEFAULT 0 COMMENT '前台顯示順序'
) COMMENT = '菜單分類';

-- ============================================================
-- 10. menu_item（菜單品項）
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
-- 11. store_menu（各店菜單覆蓋）
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
) COMMENT = '各門市菜單設定（多對多中介表）';

-- ============================================================
-- 12. reservation（訂位）
-- ============================================================
CREATE TABLE reservation (
    reservation_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id       BIGINT NOT NULL,
    store_id        BIGINT NOT NULL,
    reservation_date DATE NOT NULL COMMENT '訂位日期',
    reservation_time TIME NOT NULL COMMENT '訂位時間',
    party_size      INT NOT NULL COMMENT '用餐人數',
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                    COMMENT 'PENDING / CONFIRMED / CHECKED_IN / COMPLETED / CANCELLED / NO_SHOW',
    deposit_amount  DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '訂金金額',
    payment_status  VARCHAR(20) DEFAULT 'UNPAID' COMMENT 'UNPAID / PAID / REFUNDED',
    special_request TEXT COMMENT '特殊需求備註',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member(member_id),
    FOREIGN KEY (store_id) REFERENCES store(store_id)
) COMMENT = '訂位紀錄';

-- ============================================================
-- 13. reservation_table（訂位-桌位 多對多中介表）
-- ============================================================
CREATE TABLE reservation_table (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id  BIGINT NOT NULL,
    table_id        BIGINT NOT NULL,
    FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id),
    FOREIGN KEY (table_id) REFERENCES table_info(table_id),
    UNIQUE KEY uk_reservation_table (reservation_id, table_id)
) COMMENT = '訂位桌位指派（支援併桌）';

-- ============================================================
-- 14. orders（訂單）
-- 注意：表名用 orders 不用 order，因為 ORDER 是 SQL 保留字
-- ============================================================
CREATE TABLE orders (
    order_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id       BIGINT NOT NULL,
    store_id        BIGINT NOT NULL,
    reservation_id  BIGINT COMMENT '關聯訂位（可為 null，外帶不需訂位）',
    order_type      VARCHAR(20) NOT NULL DEFAULT 'DINE_IN' COMMENT 'DINE_IN / TAKEOUT',
    total_amount    DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '訂單總金額（鎖定當下價格）',
    points_used     INT NOT NULL DEFAULT 0 COMMENT '本次折抵點數',
    points_earned   INT NOT NULL DEFAULT 0 COMMENT '本次累積點數',
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                    COMMENT 'PENDING / PREPARING / SERVED / PAID / CANCELLED',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member(member_id),
    FOREIGN KEY (store_id) REFERENCES store(store_id),
    FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id)
) COMMENT = '訂單主表';

-- ============================================================
-- 15. order_item（訂單明細）
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
-- 16. payment（付款紀錄）
-- ============================================================
CREATE TABLE payment (
    payment_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    amount          DECIMAL(10, 2) NOT NULL COMMENT '付款金額',
    payment_method  VARCHAR(20) COMMENT 'CREDIT_CARD / LINE_PAY / CASH',
    payment_status  VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                    COMMENT 'PENDING / SUCCESS / FAILED / REFUNDED',
    transaction_no  VARCHAR(100) COMMENT '金流服務商交易序號',
    paid_at         DATETIME COMMENT '實際付款成功時間',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
) COMMENT = '付款紀錄';

-- ============================================================
-- 17. point_transaction（點數異動紀錄）
-- ============================================================
CREATE TABLE point_transaction (
    tx_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id       BIGINT NOT NULL,
    store_id        BIGINT COMMENT '發生門市（可為 null 表示系統調整）',
    point_change    INT NOT NULL COMMENT '正數=增加，負數=扣除',
    transaction_type VARCHAR(20) NOT NULL COMMENT 'EARN / USE / EXPIRE / ADJUST',
    reference_id    BIGINT COMMENT '關聯訂單或訂位 ID',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member(member_id),
    FOREIGN KEY (store_id) REFERENCES store(store_id)
) COMMENT = '點數異動紀錄（真相表，member.point_balance 是快取）';
