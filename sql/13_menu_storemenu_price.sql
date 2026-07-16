-- ==================================
-- 補上前 12 道菜的基準價格 (base_price)
-- ==================================
SET SQL_SAFE_UPDATES = 0;

UPDATE menu_item SET base_price = 80 WHERE item_name = '胡麻豆腐';
UPDATE menu_item SET base_price = 320 WHERE item_name = '海鮮沙拉';
UPDATE menu_item SET base_price = 450 WHERE item_name = '綜合生魚片';
UPDATE menu_item SET base_price = 380 WHERE item_name = '鮭魚刺身';
UPDATE menu_item SET base_price = 550 WHERE item_name = '握壽司盛合';
UPDATE menu_item SET base_price = 420 WHERE item_name = '炙燒鮭魚壽司';
UPDATE menu_item SET base_price = 880 WHERE item_name = '和牛壽喜燒';
UPDATE menu_item SET base_price = 350 WHERE item_name = '天婦羅拼盤';
UPDATE menu_item SET base_price = 150 WHERE item_name = '抹茶提拉米蘇';
UPDATE menu_item SET base_price = 120 WHERE item_name = '焦糖布丁';
UPDATE menu_item SET base_price = 90 WHERE item_name = '可爾必思';
UPDATE menu_item SET base_price = 80 WHERE item_name = '烏龍茶';

SET SQL_SAFE_UPDATES = 1;

-- ================================
-- 初始化 SQL
-- 說明：此腳本會自動補齊52個菜單在7家分店，共364 筆門市菜單資料，不會影響現有資料。
-- ================================
SET SQL_SAFE_UPDATES = 0;

INSERT IGNORE INTO store_menu (store_id, menu_item_id, price, is_available)
SELECT s.store_id, m.menu_item_id, m.base_price, 1
FROM store s, menu_item m;

SET SQL_SAFE_UPDATES = 1;

-- ================================
-- 將店家區分為三類，給予不同價格--
-- ================================

SET SQL_SAFE_UPDATES = 0;

UPDATE store_menu
JOIN menu_item mi ON store_menu.menu_item_id = mi.menu_item_id
SET store_menu.price = 
    CASE 
        -- Group A (信義A11, 南港CITYLINK) - 價格 x 1.1
        WHEN store_menu.store_id IN (1, 2) THEN mi.base_price * 1.1
        
        -- Group B (新竹巨城, 台中勤美, 高雄夢時代) - 價格 x 1.0 (原價)
        WHEN store_menu.store_id IN (3, 4, 6) THEN mi.base_price
        
        -- Group C (台南南紡, 花蓮遠百) - 價格 x 0.9
        WHEN store_menu.store_id IN (5, 7) THEN mi.base_price * 0.9
        
        -- 預設情況 (避免沒對到的變成 NULL)
        ELSE mi.base_price
    END;

SET SQL_SAFE_UPDATES = 1;

-- ================================
-- 執行結果檢查 (執行完可以檢查輸出結果/不一定要執行)
-- ================================
SELECT '目前資料總筆數 (應為 364)：' AS Message, COUNT(*) AS TotalCount FROM store_menu;

SELECT '檢查有無價格為 NULL 的資料 (應為 0)：' AS Message, COUNT(*) AS NullPriceCount FROM store_menu WHERE price IS NULL;