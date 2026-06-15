-- ============================================================
-- 測試資料（對齊新 schema）
-- 包含：1 品牌、7 門市、營業時間、特殊休息日、門市圖片、桌位、4 角色、
--       帳號（user）+ 會員（members）+ 員工（staff）、菜單、
--       時段（time_slot）+ 時段桌型庫存（reservation_capacity）示範
-- ============================================================

USE restaurant_db;

-- ============================================================
-- 品牌
-- ============================================================
INSERT INTO brand (brand_name, brand_logo_url) VALUES
('敘日和食集錦', '/images/brand/xuri-logo.png');

-- ============================================================
-- 門市（7 家）—— status 用字串列舉
-- ============================================================
INSERT INTO store (brand_id, store_code, store_name, city, district, address, phone, latitude, longitude, mrt_info, parking_info, description, main_image_url, status) VALUES
(1, 'TPE001', '敘日信義 A11 店', '台北市', '信義區', '台北市信義區松壽路11號4樓', '02-2345-6789', 25.0360390, 121.5674080, '捷運市政府站 3 號出口步行 5 分鐘', '統一時代百貨附設停車場，消費滿額可折抵', '信義旗艦店以大片落地窗、半開放式板前與城市夜景為主軸，適合紀念日、商務聚餐與週末精緻聚會。', 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=1200&q=80', 'OPEN'),
(1, 'TPE002', '敘日南港 CITYLINK 店', '台北市', '南港區', '台北市南港區忠孝東路七段369號3樓', '02-2789-1234', 25.0527340, 121.6065700, '捷運南港站直結 CITYLINK', 'CITYLINK 地下停車場，消費折抵 2 小時', '南港店鄰近車站與展覽商圈，規劃快速入席動線與開放式廚房，適合展後聚餐與家庭用餐。', 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?auto=format&fit=crop&w=1200&q=80', 'OPEN'),
(1, 'HSC001', '敘日新竹巨城店', '新竹市', '東區', '新竹市東區中央路229號7樓', '03-515-2688', 24.8090290, 120.9757930, '新竹火車站轉乘接駁車約 8 分鐘', '巨城購物中心地下停車場，消費可折抵', '新竹店以明亮木質、親子友善座席與彈性併桌為特色，服務園區商務客與家庭聚會。', 'https://images.unsplash.com/photo-1552566626-52f8b828add9?auto=format&fit=crop&w=1200&q=80', 'OPEN'),
(1, 'TXG001', '敘日台中勤美店', '台中市', '西區', '台中市西區公益路68號3樓', '04-2326-8899', 24.1514320, 120.6639120, '公車科博館站步行 4 分鐘', '勤美誠品綠園道停車場，周邊亦有路外停車場', '台中店以綠園道景觀、沉穩包廂與晚餐酒水搭配為亮點，適合朋友聚會與小型慶祝。', 'https://images.unsplash.com/photo-1559339352-11d035aa65de?auto=format&fit=crop&w=1200&q=80', 'OPEN'),
(1, 'TNN001', '敘日台南南紡店', '台南市', '東區', '台南市東區中華東路一段366號5樓', '06-300-6688', 22.9912360, 120.2331240, '台南火車站轉乘公車約 12 分鐘', '南紡購物中心停車場，消費可折抵', '台南店融入府城溫潤色調與半開放包廂，午餐時段節奏輕快，晚餐則主打慢食體驗。', 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=1200&q=80', 'OPEN'),
(1, 'KHH001', '敘日高雄夢時代店', '高雄市', '前鎮區', '高雄市前鎮區中華五路789號5樓', '07-536-7890', 22.5955130, 120.3079830, '輕軌夢時代站步行 3 分鐘', '夢時代購物中心免費停車 3 小時', '南台灣首店，規劃海港景觀座位與大型聚餐區，適合家庭聚會、團體用餐與節慶慶祝。', 'https://images.unsplash.com/photo-1551218808-94e220e084d2?auto=format&fit=crop&w=1200&q=80', 'OPEN'),
(1, 'HUA001', '敘日花蓮遠百店', '花蓮縣', '花蓮市', '花蓮縣花蓮市和平路581號6樓', '03-836-1188', 23.9789360, 121.6008330, '花蓮轉運站步行約 10 分鐘', '遠東百貨停車場，消費可折抵', '花蓮店以石材、木紋與留白空間呈現慢食氛圍，適合旅途中安排一段安靜完整的用餐時間。', 'https://images.unsplash.com/photo-1550966871-3ed3cdb5ed0c?auto=format&fit=crop&w=1200&q=80', 'OPEN');

-- ============================================================
-- 營業時間（每家店 x 每天 x 午晚餐時段）
-- ============================================================
-- TPE001 信義店
INSERT INTO store_hour (store_id, day_of_week, open_time, close_time, meal_period, is_closed) VALUES
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
(6, 1, '00:00:00', '00:00:00', 'ALL_DAY', true),  -- 週一公休
(6, 2, '11:30:00', '14:30:00', 'LUNCH', false),
(6, 2, '17:30:00', '21:30:00', 'DINNER', false),
(6, 3, '11:30:00', '14:30:00', 'LUNCH', false),
(6, 3, '17:30:00', '21:30:00', 'DINNER', false),
(6, 4, '11:30:00', '14:30:00', 'LUNCH', false),
(6, 4, '17:30:00', '21:30:00', 'DINNER', false),
(6, 5, '11:30:00', '14:30:00', 'LUNCH', false),
(6, 5, '17:30:00', '22:00:00', 'DINNER', false),
(6, 6, '11:00:00', '15:00:00', 'LUNCH', false),
(6, 6, '17:00:00', '22:00:00', 'DINNER', false),
(6, 7, '11:00:00', '15:00:00', 'LUNCH', false),
(6, 7, '17:00:00', '21:30:00', 'DINNER', false);

-- HSC001 / TXG001 / TNN001 / HUA001 共用示範營業時間
INSERT INTO store_hour (store_id, day_of_week, open_time, close_time, meal_period, is_closed)
SELECT store_scope.store_id, schedule.day_of_week, schedule.open_time, schedule.close_time, schedule.meal_period, false
FROM (
    SELECT 3 AS store_id UNION ALL
    SELECT 4 AS store_id UNION ALL
    SELECT 5 AS store_id UNION ALL
    SELECT 7 AS store_id
) store_scope
CROSS JOIN (
    SELECT 1 AS day_of_week, '11:30:00' AS open_time, '14:30:00' AS close_time, 'LUNCH' AS meal_period UNION ALL
    SELECT 1, '17:30:00', '21:30:00', 'DINNER' UNION ALL
    SELECT 2, '11:30:00', '14:30:00', 'LUNCH' UNION ALL
    SELECT 2, '17:30:00', '21:30:00', 'DINNER' UNION ALL
    SELECT 3, '11:30:00', '14:30:00', 'LUNCH' UNION ALL
    SELECT 3, '17:30:00', '21:30:00', 'DINNER' UNION ALL
    SELECT 4, '11:30:00', '14:30:00', 'LUNCH' UNION ALL
    SELECT 4, '17:30:00', '21:30:00', 'DINNER' UNION ALL
    SELECT 5, '11:30:00', '14:30:00', 'LUNCH' UNION ALL
    SELECT 5, '17:30:00', '22:00:00', 'DINNER' UNION ALL
    SELECT 6, '11:00:00', '15:00:00', 'LUNCH' UNION ALL
    SELECT 6, '17:00:00', '22:00:00', 'DINNER' UNION ALL
    SELECT 7, '11:00:00', '15:00:00', 'LUNCH' UNION ALL
    SELECT 7, '17:00:00', '21:30:00', 'DINNER'
) schedule;

-- ============================================================
-- 特殊休息日
-- ============================================================
INSERT INTO store_holiday (store_id, holiday_date, reason) VALUES
(1, '2026-02-17', '農曆除夕公休'),
(1, '2026-02-18', '農曆初一公休'),
(1, '2026-06-24', '員工教育訓練'),
(2, '2026-02-17', '農曆除夕公休'),
(2, '2026-02-18', '農曆初一公休'),
(2, '2026-07-02', '設備保養'),
(3, '2026-07-08', '商場例行保養'),
(4, '2026-06-30', '包場活動暫停對外營業'),
(5, '2026-07-01', '內場設備保養'),
(6, '2026-02-17', '農曆除夕公休'),
(6, '2026-02-18', '農曆初一公休'),
(6, '2026-02-19', '農曆初二公休'),
(6, '2026-07-06', '夢時代館內年度檢修'),
(7, '2026-06-29', '地方活動交通管制');

-- ============================================================
-- 門市圖片（前台輪播 / 後台圖片管理示範）
-- ============================================================
INSERT INTO store_image (store_id, image_url, caption, sort_order) VALUES
(1, 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=1200&q=80', '信義店城市景觀座位', 0),
(1, 'https://images.unsplash.com/photo-1552566626-52f8b828add9?auto=format&fit=crop&w=1200&q=80', '半開放式用餐空間', 1),
(1, 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=1200&q=80', '夜間聚餐氛圍', 2),
(2, 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?auto=format&fit=crop&w=1200&q=80', '南港店開放式廚房', 0),
(2, 'https://images.unsplash.com/photo-1559339352-11d035aa65de?auto=format&fit=crop&w=1200&q=80', '快速入席用餐區', 1),
(2, 'https://images.unsplash.com/photo-1551218808-94e220e084d2?auto=format&fit=crop&w=1200&q=80', '聚餐桌席', 2),
(3, 'https://images.unsplash.com/photo-1552566626-52f8b828add9?auto=format&fit=crop&w=1200&q=80', '新竹店親子友善座席', 0),
(3, 'https://images.unsplash.com/photo-1550966871-3ed3cdb5ed0c?auto=format&fit=crop&w=1200&q=80', '明亮木質用餐區', 1),
(3, 'https://images.unsplash.com/photo-1514933651103-005eec06c04b?auto=format&fit=crop&w=1200&q=80', '彈性併桌區', 2),
(4, 'https://images.unsplash.com/photo-1559339352-11d035aa65de?auto=format&fit=crop&w=1200&q=80', '台中店綠園道座位', 0),
(4, 'https://images.unsplash.com/photo-1521017432531-fbd92d768814?auto=format&fit=crop&w=1200&q=80', '沉穩包廂區', 1),
(4, 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=1200&q=80', '晚餐酒水吧台', 2),
(5, 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=1200&q=80', '台南店溫潤主餐區', 0),
(5, 'https://images.unsplash.com/photo-1551218808-94e220e084d2?auto=format&fit=crop&w=1200&q=80', '半開放包廂', 1),
(5, 'https://images.unsplash.com/photo-1498654896293-37aacf113fd9?auto=format&fit=crop&w=1200&q=80', '慢食晚餐氛圍', 2),
(6, 'https://images.unsplash.com/photo-1551218808-94e220e084d2?auto=format&fit=crop&w=1200&q=80', '高雄店海港景觀區', 0),
(6, 'https://images.unsplash.com/photo-1521017432531-fbd92d768814?auto=format&fit=crop&w=1200&q=80', '大型聚餐區', 1),
(6, 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?auto=format&fit=crop&w=1200&q=80', '家庭用餐區', 2),
(7, 'https://images.unsplash.com/photo-1550966871-3ed3cdb5ed0c?auto=format&fit=crop&w=1200&q=80', '花蓮店慢食空間', 0),
(7, 'https://images.unsplash.com/photo-1514933651103-005eec06c04b?auto=format&fit=crop&w=1200&q=80', '石材與木紋設計', 1),
(7, 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=1200&q=80', '旅途中安靜用餐席', 2);

-- ============================================================
-- 桌位 —— capacity 改 table_size，status 用字串
-- ============================================================
-- TPE001 信義店（18 桌）
INSERT INTO table_info (store_id, table_number, table_size, table_type, zone, status, is_combinable) VALUES
(1, 'A01', 2, 'REGULAR', '1F', 'AVAILABLE', false),
(1, 'A02', 2, 'REGULAR', '1F', 'AVAILABLE', false),
(1, 'A03', 4, 'REGULAR', '1F', 'AVAILABLE', true),
(1, 'A04', 4, 'REGULAR', '1F', 'AVAILABLE', true),
(1, 'A05', 4, 'REGULAR', '1F', 'AVAILABLE', true),
(1, 'A06', 6, 'REGULAR', '1F', 'AVAILABLE', true),
(1, 'B01', 2, 'BOOTH', '1F', 'AVAILABLE', false),
(1, 'B02', 4, 'BOOTH', '1F', 'AVAILABLE', false),
(1, 'B03', 4, 'BOOTH', '1F', 'AVAILABLE', false),
(1, 'C01', 2, 'BAR', '1F', 'AVAILABLE', false),
(1, 'C02', 2, 'BAR', '1F', 'AVAILABLE', false),
(1, 'D01', 4, 'REGULAR', '2F', 'AVAILABLE', true),
(1, 'D02', 4, 'REGULAR', '2F', 'AVAILABLE', true),
(1, 'D03', 6, 'REGULAR', '2F', 'AVAILABLE', true),
(1, 'D04', 6, 'REGULAR', '2F', 'AVAILABLE', true),
(1, 'V01', 8, 'VIP_ROOM', '2F', 'AVAILABLE', false),
(1, 'V02', 10, 'VIP_ROOM', '2F', 'AVAILABLE', false),
(1, 'V03', 12, 'VIP_ROOM', '2F', 'AVAILABLE', false);

-- TPE002 南港店（13 桌）
INSERT INTO table_info (store_id, table_number, table_size, table_type, zone, status, is_combinable) VALUES
(2, 'A01', 2, 'REGULAR', '1F', 'AVAILABLE', false),
(2, 'A02', 2, 'REGULAR', '1F', 'AVAILABLE', false),
(2, 'A03', 4, 'REGULAR', '1F', 'AVAILABLE', true),
(2, 'A04', 4, 'REGULAR', '1F', 'AVAILABLE', true),
(2, 'A05', 4, 'REGULAR', '1F', 'AVAILABLE', true),
(2, 'A06', 6, 'REGULAR', '1F', 'AVAILABLE', true),
(2, 'A07', 6, 'REGULAR', '1F', 'AVAILABLE', true),
(2, 'B01', 2, 'BOOTH', '1F', 'AVAILABLE', false),
(2, 'B02', 4, 'BOOTH', '1F', 'AVAILABLE', false),
(2, 'C01', 2, 'BAR', '1F', 'AVAILABLE', false),
(2, 'C02', 2, 'BAR', '1F', 'AVAILABLE', false),
(2, 'V01', 8, 'VIP_ROOM', '1F', 'AVAILABLE', false),
(2, 'V02', 10, 'VIP_ROOM', '1F', 'AVAILABLE', false);

-- HSC001 新竹店（10 桌）
INSERT INTO table_info (store_id, table_number, table_size, table_type, zone, status, is_combinable) VALUES
(3, 'A01', 2, 'REGULAR', '1F', 'AVAILABLE', false),
(3, 'A02', 2, 'REGULAR', '1F', 'AVAILABLE', false),
(3, 'A03', 4, 'REGULAR', '1F', 'AVAILABLE', true),
(3, 'A04', 4, 'REGULAR', '1F', 'AVAILABLE', true),
(3, 'A05', 6, 'REGULAR', '1F', 'AVAILABLE', true),
(3, 'B01', 4, 'BOOTH', '1F', 'AVAILABLE', false),
(3, 'B02', 4, 'BOOTH', '1F', 'AVAILABLE', false),
(3, 'K01', 6, 'REGULAR', '親子區', 'AVAILABLE', true),
(3, 'K02', 6, 'REGULAR', '親子區', 'AVAILABLE', true),
(3, 'V01', 10, 'VIP_ROOM', '包廂', 'AVAILABLE', false);

-- TXG001 台中店（11 桌）
INSERT INTO table_info (store_id, table_number, table_size, table_type, zone, status, is_combinable) VALUES
(4, 'A01', 2, 'REGULAR', '窗景區', 'AVAILABLE', false),
(4, 'A02', 2, 'REGULAR', '窗景區', 'AVAILABLE', false),
(4, 'A03', 4, 'REGULAR', '窗景區', 'AVAILABLE', true),
(4, 'A04', 4, 'REGULAR', '窗景區', 'AVAILABLE', true),
(4, 'B01', 4, 'BOOTH', '包廂區', 'AVAILABLE', false),
(4, 'B02', 6, 'BOOTH', '包廂區', 'AVAILABLE', false),
(4, 'C01', 2, 'BAR', '吧台', 'AVAILABLE', false),
(4, 'C02', 2, 'BAR', '吧台', 'AVAILABLE', false),
(4, 'G01', 8, 'REGULAR', '團體區', 'AVAILABLE', true),
(4, 'V01', 10, 'VIP_ROOM', '包廂區', 'AVAILABLE', false),
(4, 'V02', 12, 'VIP_ROOM', '包廂區', 'AVAILABLE', false);

-- TNN001 台南店（10 桌）
INSERT INTO table_info (store_id, table_number, table_size, table_type, zone, status, is_combinable) VALUES
(5, 'A01', 2, 'REGULAR', '主用餐區', 'AVAILABLE', false),
(5, 'A02', 2, 'REGULAR', '主用餐區', 'AVAILABLE', false),
(5, 'A03', 4, 'REGULAR', '主用餐區', 'AVAILABLE', true),
(5, 'A04', 4, 'REGULAR', '主用餐區', 'AVAILABLE', true),
(5, 'A05', 6, 'REGULAR', '主用餐區', 'AVAILABLE', true),
(5, 'B01', 4, 'BOOTH', '半包廂', 'AVAILABLE', false),
(5, 'B02', 4, 'BOOTH', '半包廂', 'AVAILABLE', false),
(5, 'C01', 2, 'BAR', '吧台', 'AVAILABLE', false),
(5, 'V01', 8, 'VIP_ROOM', '包廂', 'AVAILABLE', false),
(5, 'V02', 12, 'VIP_ROOM', '包廂', 'AVAILABLE', false);

-- KHH001 高雄店（12 桌）
INSERT INTO table_info (store_id, table_number, table_size, table_type, zone, status, is_combinable) VALUES
(6, 'A01', 2, 'REGULAR', '1F', 'AVAILABLE', false),
(6, 'A02', 4, 'REGULAR', '1F', 'AVAILABLE', true),
(6, 'A03', 4, 'REGULAR', '1F', 'AVAILABLE', true),
(6, 'A04', 4, 'REGULAR', '1F', 'AVAILABLE', true),
(6, 'A05', 6, 'REGULAR', '1F', 'AVAILABLE', true),
(6, 'A06', 6, 'REGULAR', '1F', 'AVAILABLE', true),
(6, 'B01', 2, 'BOOTH', '港景區', 'AVAILABLE', false),
(6, 'B02', 4, 'BOOTH', '港景區', 'AVAILABLE', false),
(6, 'C01', 2, 'BAR', '吧台', 'AVAILABLE', false),
(6, 'V01', 8, 'VIP_ROOM', '包廂', 'AVAILABLE', false),
(6, 'V02', 10, 'VIP_ROOM', '包廂', 'AVAILABLE', false),
(6, 'V03', 16, 'VIP_ROOM', '包廂', 'AVAILABLE', false);

-- HUA001 花蓮店（8 桌）
INSERT INTO table_info (store_id, table_number, table_size, table_type, zone, status, is_combinable) VALUES
(7, 'A01', 2, 'REGULAR', '主用餐區', 'AVAILABLE', false),
(7, 'A02', 2, 'REGULAR', '主用餐區', 'AVAILABLE', false),
(7, 'A03', 4, 'REGULAR', '主用餐區', 'AVAILABLE', true),
(7, 'A04', 4, 'REGULAR', '主用餐區', 'AVAILABLE', true),
(7, 'B01', 4, 'BOOTH', '安靜區', 'AVAILABLE', false),
(7, 'B02', 6, 'BOOTH', '安靜區', 'AVAILABLE', false),
(7, 'V01', 8, 'VIP_ROOM', '包廂', 'AVAILABLE', false),
(7, 'V02', 10, 'VIP_ROOM', '包廂', 'AVAILABLE', false);

-- ============================================================
-- 角色（4 種）
-- ============================================================
INSERT INTO role (role_name, description) VALUES
('CUSTOMER', '一般顧客，可訂位、點餐、查看點數'),
('STAFF', '門市員工，可查看自己門市的訂位與訂單'),
('MANAGER', '門市店長，可管理自己門市的所有資料'),
('ADMIN', '系統管理員，可管理全系統');

-- ============================================================
-- 登入帳號 user（會員與員工共用）
-- 密碼皆為 password123（BCrypt 雜湊）
-- user_id 依序：1=admin 2=manager 3=staff 4=陳小明 5=林小美
-- ============================================================
INSERT INTO user (role_id, email, password_hash, name, phone, birthday) VALUES
(4, 'admin@xuri.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系統管理員', '0900-000-000', '1990-01-01'),
(3, 'manager@xuri.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '王店長',     '0911-111-111', '1985-06-15'),
(2, 'staff@xuri.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李店員',     '0955-555-555', '1997-09-10'),
(1, 'user1@example.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '陳小明',     '0922-222-222', '1995-03-20'),
(1, 'user2@example.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '林小美',     '0933-333-333', '1998-11-08');

-- ============================================================
-- 會員資料 members（顧客；對應 user 4、5 → 陳小明 / 林小美）
-- ============================================================
INSERT INTO members (user_id, point_balance, member_level) VALUES
(4, 120, 'BRONZE'),
(5, 880, 'SILVER');

-- ============================================================
-- 員工資料 staff（manager + staff；皆屬信義店 store_id=1）
-- ============================================================
INSERT INTO staff (user_id, store_id, staff_no, hire_date, status) VALUES
(2, 1, 'M001', '2023-01-01', 'ACTIVE'),
(3, 1, 'S001', '2024-07-15', 'ACTIVE');

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
-- 時段 time_slot（示範：信義店 2026-06-20 午/晚兩個時段）
-- 註：以下假設這是 time_slot 首批資料，slot_id 依序為 1、2
-- ============================================================
INSERT INTO time_slot (store_id, reservation_date, start_time, end_time, is_open) VALUES
(1, '2026-06-20', '11:30:00', '14:30:00', true),  -- slot_id = 1 午餐
(1, '2026-06-20', '17:30:00', '22:00:00', true);  -- slot_id = 2 晚餐

-- ============================================================
-- 時段桌型庫存 reservation_capacity
-- total_count = 信義店各 table_size 的桌數（2人:5 / 4人:7 / 6人:3 / 8人:1 / 10人:1 / 12人:1）
-- reserved_count 初始皆為 0
-- ============================================================
INSERT INTO reservation_capacity (slot_id, table_size, total_count, reserved_count) VALUES
-- 午餐 slot 1
(1, 2, 5, 0),
(1, 4, 7, 0),
(1, 6, 3, 0),
(1, 8, 1, 0),
(1, 10, 1, 0),
(1, 12, 1, 0),
-- 晚餐 slot 2
(2, 2, 5, 0),
(2, 4, 7, 0),
(2, 6, 3, 0),
(2, 8, 1, 0),
(2, 10, 1, 0),
(2, 12, 1, 0);
