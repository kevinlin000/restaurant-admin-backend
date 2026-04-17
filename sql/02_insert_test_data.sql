-- ============================================================
-- 測試資料
-- 包含：1 品牌、3 門市、桌位、營業時間、4 角色、菜單
-- ============================================================

USE restaurant_db;

-- ============================================================
-- 品牌
-- ============================================================
INSERT INTO brand (brand_name, brand_logo_url) VALUES
('旭集和食集錦', '/images/brand/asahi-logo.png');

-- ============================================================
-- 門市（3 家）
-- ============================================================
INSERT INTO store (brand_id, store_code, store_name, city, district, address, phone, latitude, longitude, mrt_info, parking_info, description, status) VALUES
(1, 'TPE001', '旭集信義 A11 店', '台北市', '信義區', '台北市信義區松壽路11號4樓', '02-2345-6789', 25.0360390, 121.5674080, '捷運市政府站 3 號出口步行 5 分鐘', '統一時代百貨附設停車場，消費滿額可折抵', '信義區旗艦店，270度環景落地窗，享受都市天際線', 1),
(1, 'TPE002', '旭集南港 CITYLINK 店', '台北市', '南港區', '台北市南港區忠孝東路七段369號3樓', '02-2789-1234', 25.0527340, 121.6065700, '捷運南港站直結 CITYLINK', 'CITYLINK 地下停車場，消費折抵 2 小時', '南港新據點，開放式廚房可觀賞料理過程', 1),
(1, 'KHH001', '旭集高雄夢時代店', '高雄市', '前鎮區', '高雄市前鎮區中華五路789號5樓', '07-536-7890', 22.5955130, 120.3079830, '輕軌夢時代站步行 3 分鐘', '夢時代購物中心免費停車 3 小時', '南台灣首店，海港景觀座位區', 1);

-- ============================================================
-- 營業時間（每家店 x 每天 x 午晚餐時段）
-- ============================================================
-- TPE001 信義店
INSERT INTO store_hour (store_id, day_of_week, open_time, close_time, meal_period, is_closed) VALUES
-- 週一到週五
(1, 1, '11:30:00', '14:30:00', 'LUNCH', false),
(1, 1, '17:30:00', '22:00:00', 'DINNER', false),
(1, 2, '11:30:00', '14:30:00', 'LUNCH', false),
(1, 2, '17:30:00', '22:00:00', 'DINNER', false),
(1, 3, '11:30:00', '14:30:00', 'LUNCH', false),
(1, 3, '17:30:00', '22:00:00', 'DINNER', false),
(1, 4, '11:30:00', '14:30:00', 'LUNCH', false),
(1, 4, '17:30:00', '22:00:00', 'DINNER', false),
(1, 5, '11:30:00', '14:30:00', 'LUNCH', false),
(1, 5, '17:30:00', '22:30:00', 'DINNER', false),
-- 週六日（提早開、延後收）
(1, 6, '11:00:00', '15:00:00', 'LUNCH', false),
(1, 6, '17:00:00', '22:30:00', 'DINNER', false),
(1, 7, '11:00:00', '15:00:00', 'LUNCH', false),
(1, 7, '17:00:00', '22:00:00', 'DINNER', false);

-- TPE002 南港店
INSERT INTO store_hour (store_id, day_of_week, open_time, close_time, meal_period, is_closed) VALUES
(2, 1, '11:30:00', '14:30:00', 'LUNCH', false),
(2, 1, '17:30:00', '21:30:00', 'DINNER', false),
(2, 2, '11:30:00', '14:30:00', 'LUNCH', false),
(2, 2, '17:30:00', '21:30:00', 'DINNER', false),
(2, 3, '11:30:00', '14:30:00', 'LUNCH', false),
(2, 3, '17:30:00', '21:30:00', 'DINNER', false),
(2, 4, '11:30:00', '14:30:00', 'LUNCH', false),
(2, 4, '17:30:00', '21:30:00', 'DINNER', false),
(2, 5, '11:30:00', '14:30:00', 'LUNCH', false),
(2, 5, '17:30:00', '22:00:00', 'DINNER', false),
(2, 6, '11:00:00', '15:00:00', 'LUNCH', false),
(2, 6, '17:00:00', '22:00:00', 'DINNER', false),
(2, 7, '11:00:00', '15:00:00', 'LUNCH', false),
(2, 7, '17:00:00', '21:30:00', 'DINNER', false);

-- KHH001 高雄店
INSERT INTO store_hour (store_id, day_of_week, open_time, close_time, meal_period, is_closed) VALUES
(3, 1, '00:00:00', '00:00:00', 'ALL_DAY', true),  -- 週一公休
(3, 2, '11:30:00', '14:30:00', 'LUNCH', false),
(3, 2, '17:30:00', '21:30:00', 'DINNER', false),
(3, 3, '11:30:00', '14:30:00', 'LUNCH', false),
(3, 3, '17:30:00', '21:30:00', 'DINNER', false),
(3, 4, '11:30:00', '14:30:00', 'LUNCH', false),
(3, 4, '17:30:00', '21:30:00', 'DINNER', false),
(3, 5, '11:30:00', '14:30:00', 'LUNCH', false),
(3, 5, '17:30:00', '22:00:00', 'DINNER', false),
(3, 6, '11:00:00', '15:00:00', 'LUNCH', false),
(3, 6, '17:00:00', '22:00:00', 'DINNER', false),
(3, 7, '11:00:00', '15:00:00', 'LUNCH', false),
(3, 7, '17:00:00', '21:30:00', 'DINNER', false);

-- ============================================================
-- 特殊休息日
-- ============================================================
INSERT INTO store_holiday (store_id, holiday_date, reason) VALUES
(1, '2026-02-17', '農曆除夕公休'),
(1, '2026-02-18', '農曆初一公休'),
(2, '2026-02-17', '農曆除夕公休'),
(2, '2026-02-18', '農曆初一公休'),
(3, '2026-02-17', '農曆除夕公休'),
(3, '2026-02-18', '農曆初一公休'),
(3, '2026-02-19', '農曆初二公休');

-- ============================================================
-- 桌位
-- ============================================================
-- TPE001 信義店（20 桌）
INSERT INTO table_info (store_id, table_number, capacity, table_type, zone, status, is_combinable) VALUES
(1, 'A01', 2, 'REGULAR', '1F', 1, false),
(1, 'A02', 2, 'REGULAR', '1F', 1, false),
(1, 'A03', 4, 'REGULAR', '1F', 1, true),
(1, 'A04', 4, 'REGULAR', '1F', 1, true),
(1, 'A05', 4, 'REGULAR', '1F', 1, true),
(1, 'A06', 6, 'REGULAR', '1F', 1, true),
(1, 'B01', 2, 'BOOTH', '1F', 1, false),
(1, 'B02', 4, 'BOOTH', '1F', 1, false),
(1, 'B03', 4, 'BOOTH', '1F', 1, false),
(1, 'C01', 2, 'BAR', '1F', 1, false),
(1, 'C02', 2, 'BAR', '1F', 1, false),
(1, 'D01', 4, 'REGULAR', '2F', 1, true),
(1, 'D02', 4, 'REGULAR', '2F', 1, true),
(1, 'D03', 6, 'REGULAR', '2F', 1, true),
(1, 'D04', 6, 'REGULAR', '2F', 1, true),
(1, 'V01', 8, 'VIP_ROOM', '2F', 1, false),
(1, 'V02', 10, 'VIP_ROOM', '2F', 1, false),
(1, 'V03', 12, 'VIP_ROOM', '2F', 1, false);

-- TPE002 南港店（15 桌）
INSERT INTO table_info (store_id, table_number, capacity, table_type, zone, status, is_combinable) VALUES
(2, 'A01', 2, 'REGULAR', '1F', 1, false),
(2, 'A02', 2, 'REGULAR', '1F', 1, false),
(2, 'A03', 4, 'REGULAR', '1F', 1, true),
(2, 'A04', 4, 'REGULAR', '1F', 1, true),
(2, 'A05', 4, 'REGULAR', '1F', 1, true),
(2, 'A06', 6, 'REGULAR', '1F', 1, true),
(2, 'A07', 6, 'REGULAR', '1F', 1, true),
(2, 'B01', 2, 'BOOTH', '1F', 1, false),
(2, 'B02', 4, 'BOOTH', '1F', 1, false),
(2, 'C01', 2, 'BAR', '1F', 1, false),
(2, 'C02', 2, 'BAR', '1F', 1, false),
(2, 'V01', 8, 'VIP_ROOM', '1F', 1, false),
(2, 'V02', 10, 'VIP_ROOM', '1F', 1, false);

-- KHH001 高雄店（12 桌）
INSERT INTO table_info (store_id, table_number, capacity, table_type, zone, status, is_combinable) VALUES
(3, 'A01', 2, 'REGULAR', '1F', 1, false),
(3, 'A02', 4, 'REGULAR', '1F', 1, true),
(3, 'A03', 4, 'REGULAR', '1F', 1, true),
(3, 'A04', 4, 'REGULAR', '1F', 1, true),
(3, 'A05', 6, 'REGULAR', '1F', 1, true),
(3, 'A06', 6, 'REGULAR', '1F', 1, true),
(3, 'B01', 2, 'BOOTH', '1F', 1, false),
(3, 'B02', 4, 'BOOTH', '1F', 1, false),
(3, 'C01', 2, 'BAR', '1F', 1, false),
(3, 'V01', 8, 'VIP_ROOM', '1F', 1, false),
(3, 'V02', 10, 'VIP_ROOM', '1F', 1, false),
(3, 'V03', 16, 'VIP_ROOM', '1F', 1, false);

-- ============================================================
-- 角色（4 種）
-- ============================================================
INSERT INTO role (role_name, description) VALUES
('CUSTOMER', '一般顧客，可訂位、點餐、查看點數'),
('STAFF', '門市員工，可查看自己門市的訂位與訂單'),
('MANAGER', '門市店長，可管理自己門市的所有資料'),
('ADMIN', '系統管理員，可管理全系統');

-- ============================================================
-- 測試會員
-- ============================================================
-- 密碼皆為 password123（BCrypt 雜湊）
INSERT INTO member (role_id, email, password_hash, name, phone, birthday, member_level) VALUES
(4, 'admin@restaurant.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系統管理員', '0900-000-000', '1990-01-01', 'DIAMOND'),
(3, 'manager@restaurant.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '王店長', '0911-111-111', '1985-06-15', 'GOLD'),
(1, 'user1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '陳小明', '0922-222-222', '1995-03-20', 'BRONZE'),
(1, 'user2@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '林小美', '0933-333-333', '1998-11-08', 'SILVER');

-- ============================================================
-- 菜單分類
-- ============================================================
INSERT INTO menu_category (category_name, sort_order) VALUES
('前菜', 1),
('生魚片', 2),
('壽司', 3),
('熟食', 4),
('炸物', 5),
('甜點', 6),
('飲料', 7),
('酒水', 8);

-- ============================================================
-- 菜單品項
-- ============================================================
INSERT INTO menu_item (category_id, item_name, description, base_price, allergen_info, is_active) VALUES
-- 前菜
(1, '胡麻豆腐', '手工研磨胡麻醬，搭配嫩滑豆腐', 0.00, '含堅果、大豆', true),
(1, '海鮮沙拉', '新鮮時蔬搭配鮮蝦、花枝', 0.00, '含甲殼類', true),
-- 生魚片
(2, '綜合生魚片', '每日嚴選新鮮漁獲，主廚搭配', 0.00, '含生食', true),
(2, '鮭魚刺身', '挪威鮭魚薄切', 0.00, '含生食', true),
-- 壽司
(3, '握壽司盛合', '主廚推薦 8 貫握壽司', 0.00, '含生食', true),
(3, '炙燒鮭魚壽司', '炙燒表面焦香，入口即化', 0.00, '含生食', true),
-- 熟食
(4, '和牛壽喜燒', '澳洲和牛搭配特製壽喜燒醬汁', 0.00, '含大豆', true),
(4, '天婦羅拼盤', '嚴選蝦、蔬菜酥炸', 0.00, '含麩質、甲殼類', true),
-- 甜點
(6, '抹茶提拉米蘇', '宇治抹茶搭配 Mascarpone', 0.00, '含乳製品、蛋', true),
(6, '焦糖布丁', '法式經典焦糖布丁', 0.00, '含乳製品、蛋', true),
-- 飲料
(7, '可爾必思', '日本原裝進口', 0.00, '含乳製品', true),
(7, '烏龍茶', '台灣高山烏龍', 0.00, null, true),
-- 酒水（單點加購）
(8, '朝日生啤', '日本直送 350ml', 150.00, null, true),
(8, '獺祭純米大吟釀', '山口縣產，一合', 380.00, null, true);

-- ============================================================
-- 各店菜單覆蓋（吃到飽品項 base_price=0，酒水單點）
-- 三家店都供應全部品項
-- ============================================================
INSERT INTO store_menu (store_id, menu_item_id, price, is_available)
SELECT s.store_id, m.menu_item_id, NULL, TRUE
FROM store s CROSS JOIN menu_item m;

-- ============================================================
-- 方案價格（用 store_menu 的 price 覆蓋來表示各方案）
-- 實際方案建議用獨立的 meal_plan 表，這裡先用簡化方式
-- ============================================================
