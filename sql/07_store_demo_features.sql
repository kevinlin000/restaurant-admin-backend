-- ============================================================
-- 找門市 Demo 特色標籤
-- 用途：既有資料庫補上 store_feature 表與 demo 標籤
-- 執行時機：已跑過 01_create_tables.sql + 02_insert_test_data.sql 之後
-- ============================================================

USE restaurant_db;

CREATE TABLE IF NOT EXISTS store_feature (
    feature_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id        BIGINT NOT NULL,
    feature_key     VARCHAR(40) NOT NULL COMMENT '標籤代碼，如 BUSINESS / FAMILY / PARKING',
    feature_label   VARCHAR(30) NOT NULL COMMENT '前台顯示名稱',
    sort_order      INT DEFAULT 0 COMMENT '排序（小的在前）',
    FOREIGN KEY (store_id) REFERENCES store(store_id),
    UNIQUE KEY uk_store_feature (store_id, feature_key)
) COMMENT = '門市特色標籤（用於情境篩選與推薦排序）';

DELETE FROM store_feature
WHERE store_id IN (
    SELECT store_id
    FROM store
    WHERE store_code IN ('TPE001', 'TPE002', 'HSC001', 'TXG001', 'TNN001', 'KHH001', 'HUA001')
);

INSERT INTO store_feature (store_id, feature_key, feature_label, sort_order)
SELECT store_id, 'BUSINESS', '商務聚餐', 1 FROM store WHERE store_code = 'TPE001'
UNION ALL SELECT store_id, 'DATE', '約會推薦', 2 FROM store WHERE store_code = 'TPE001'
UNION ALL SELECT store_id, 'PRIVATE_ROOM', '包廂', 3 FROM store WHERE store_code = 'TPE001'
UNION ALL SELECT store_id, 'CITY_VIEW', '城市景觀', 4 FROM store WHERE store_code = 'TPE001'
UNION ALL SELECT store_id, 'STATION', '車站直達', 1 FROM store WHERE store_code = 'TPE002'
UNION ALL SELECT store_id, 'BUSINESS', '商務聚餐', 2 FROM store WHERE store_code = 'TPE002'
UNION ALL SELECT store_id, 'GROUP', '團體聚餐', 3 FROM store WHERE store_code = 'TPE002'
UNION ALL SELECT store_id, 'FAST_SEATING', '快速入席', 4 FROM store WHERE store_code = 'TPE002'
UNION ALL SELECT store_id, 'FAMILY', '親子友善', 1 FROM store WHERE store_code = 'HSC001'
UNION ALL SELECT store_id, 'PARKING', '停車方便', 2 FROM store WHERE store_code = 'HSC001'
UNION ALL SELECT store_id, 'PRIVATE_ROOM', '包廂', 3 FROM store WHERE store_code = 'HSC001'
UNION ALL SELECT store_id, 'GROUP', '團體聚餐', 4 FROM store WHERE store_code = 'HSC001'
UNION ALL SELECT store_id, 'DATE', '約會推薦', 1 FROM store WHERE store_code = 'TXG001'
UNION ALL SELECT store_id, 'PRIVATE_ROOM', '包廂', 2 FROM store WHERE store_code = 'TXG001'
UNION ALL SELECT store_id, 'BAR', '吧台席', 3 FROM store WHERE store_code = 'TXG001'
UNION ALL SELECT store_id, 'PARKING', '停車方便', 4 FROM store WHERE store_code = 'TXG001'
UNION ALL SELECT store_id, 'DATE', '約會推薦', 1 FROM store WHERE store_code = 'TNN001'
UNION ALL SELECT store_id, 'PRIVATE_ROOM', '包廂', 2 FROM store WHERE store_code = 'TNN001'
UNION ALL SELECT store_id, 'SHOPPING', '商場用餐', 3 FROM store WHERE store_code = 'TNN001'
UNION ALL SELECT store_id, 'FAMILY', '親子友善', 4 FROM store WHERE store_code = 'TNN001'
UNION ALL SELECT store_id, 'GROUP', '團體聚餐', 1 FROM store WHERE store_code = 'KHH001'
UNION ALL SELECT store_id, 'FAMILY', '親子友善', 2 FROM store WHERE store_code = 'KHH001'
UNION ALL SELECT store_id, 'PARKING', '停車方便', 3 FROM store WHERE store_code = 'KHH001'
UNION ALL SELECT store_id, 'PRIVATE_ROOM', '包廂', 4 FROM store WHERE store_code = 'KHH001'
UNION ALL SELECT store_id, 'TRAVEL', '旅途中用餐', 1 FROM store WHERE store_code = 'HUA001'
UNION ALL SELECT store_id, 'QUIET', '安靜用餐', 2 FROM store WHERE store_code = 'HUA001'
UNION ALL SELECT store_id, 'DATE', '約會推薦', 3 FROM store WHERE store_code = 'HUA001'
UNION ALL SELECT store_id, 'PRIVATE_ROOM', '包廂', 4 FROM store WHERE store_code = 'HUA001';
