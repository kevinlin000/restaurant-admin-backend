-- ============================================================
-- 找門市 Demo 圖片調性更新
-- 用途：改用專案內統一調性的門市圖片，避免依賴外部假圖
-- 執行時機：已跑過 01_create_tables.sql + 02_insert_test_data.sql 之後
-- ============================================================

USE restaurant_db;

UPDATE store
SET main_image_url = '/store-images/xuri-dining-room.jpg'
WHERE store_code IN ('TPE001', 'TXG001', 'KHH001');

UPDATE store
SET main_image_url = '/store-images/xuri-chef-counter.jpg'
WHERE store_code = 'TPE002';

UPDATE store
SET main_image_url = '/store-images/xuri-window-seats.jpg'
WHERE store_code IN ('HSC001', 'HUA001');

UPDATE store
SET main_image_url = '/store-images/xuri-private-room.jpg'
WHERE store_code = 'TNN001';

DELETE FROM store_image
WHERE store_id IN (
    SELECT store_id
    FROM store
    WHERE store_code IN ('TPE001', 'TPE002', 'HSC001', 'TXG001', 'TNN001', 'KHH001', 'HUA001')
);

INSERT INTO store_image (store_id, image_url, caption, sort_order)
SELECT store_id, '/store-images/xuri-dining-room.jpg', '主用餐區與晚餐氛圍', 0
FROM store WHERE store_code = 'TPE001'
UNION ALL
SELECT store_id, '/store-images/xuri-chef-counter.jpg', '板前座位與開放式廚房', 1
FROM store WHERE store_code = 'TPE001'
UNION ALL
SELECT store_id, '/store-images/xuri-private-room.jpg', '商務與慶祝包廂', 2
FROM store WHERE store_code = 'TPE001'
UNION ALL
SELECT store_id, '/store-images/xuri-chef-counter.jpg', '開放式廚房與快速出餐動線', 0
FROM store WHERE store_code = 'TPE002'
UNION ALL
SELECT store_id, '/store-images/xuri-dining-room.jpg', '車站商圈主用餐區', 1
FROM store WHERE store_code = 'TPE002'
UNION ALL
SELECT store_id, '/store-images/xuri-window-seats.jpg', '明亮窗景座位', 2
FROM store WHERE store_code = 'TPE002'
UNION ALL
SELECT store_id, '/store-images/xuri-window-seats.jpg', '親子友善窗景座位', 0
FROM store WHERE store_code = 'HSC001'
UNION ALL
SELECT store_id, '/store-images/xuri-private-room.jpg', '家庭聚會包廂', 1
FROM store WHERE store_code = 'HSC001'
UNION ALL
SELECT store_id, '/store-images/xuri-dining-room.jpg', '彈性併桌用餐區', 2
FROM store WHERE store_code = 'HSC001'
UNION ALL
SELECT store_id, '/store-images/xuri-dining-room.jpg', '綠園道晚餐氛圍', 0
FROM store WHERE store_code = 'TXG001'
UNION ALL
SELECT store_id, '/store-images/xuri-private-room.jpg', '沉穩包廂區', 1
FROM store WHERE store_code = 'TXG001'
UNION ALL
SELECT store_id, '/store-images/xuri-chef-counter.jpg', '酒水與吧台座位', 2
FROM store WHERE store_code = 'TXG001'
UNION ALL
SELECT store_id, '/store-images/xuri-private-room.jpg', '半開放包廂', 0
FROM store WHERE store_code = 'TNN001'
UNION ALL
SELECT store_id, '/store-images/xuri-dining-room.jpg', '溫潤主用餐區', 1
FROM store WHERE store_code = 'TNN001'
UNION ALL
SELECT store_id, '/store-images/xuri-window-seats.jpg', '午餐窗景座位', 2
FROM store WHERE store_code = 'TNN001'
UNION ALL
SELECT store_id, '/store-images/xuri-dining-room.jpg', '大型聚餐主用餐區', 0
FROM store WHERE store_code = 'KHH001'
UNION ALL
SELECT store_id, '/store-images/xuri-chef-counter.jpg', '晚餐吧台與料理區', 1
FROM store WHERE store_code = 'KHH001'
UNION ALL
SELECT store_id, '/store-images/xuri-private-room.jpg', '節慶聚餐包廂', 2
FROM store WHERE store_code = 'KHH001'
UNION ALL
SELECT store_id, '/store-images/xuri-window-seats.jpg', '旅途中的日光座位', 0
FROM store WHERE store_code = 'HUA001'
UNION ALL
SELECT store_id, '/store-images/xuri-private-room.jpg', '安靜包廂座位', 1
FROM store WHERE store_code = 'HUA001'
UNION ALL
SELECT store_id, '/store-images/xuri-dining-room.jpg', '慢食主用餐區', 2
FROM store WHERE store_code = 'HUA001';
