-- 變更 menu_item 資料表結構，動態追加特色標籤欄位
ALTER TABLE menu_item 
ADD COLUMN feature_tags VARCHAR(255) NULL COMMENT '分店特色標籤，格式如：👑 店長推薦,🌶️ 微辣';


-- 1. 前菜類
UPDATE menu_item SET feature_tags = '🥢 手工研磨' WHERE menu_item_id = 1; -- 胡麻豆腐
-- ID 2 海鮮沙拉：留空，不貼標籤

-- 2. 生魚片/壽司類
UPDATE menu_item SET feature_tags = '👑 店長推薦' WHERE menu_item_id = 3; -- 綜合生魚片
UPDATE menu_item SET feature_tags = '🔥 人氣熱銷' WHERE menu_item_id = 4; -- 鮭魚刺身
UPDATE menu_item SET feature_tags = '🍣 主廚推薦' WHERE menu_item_id = 5; -- 握壽司盛合
UPDATE menu_item SET feature_tags = '🔥 入口即化' WHERE menu_item_id = 6; -- 炙燒鮭魚壽司

-- 3. 主食/炸物類
UPDATE menu_item SET feature_tags = '🥩 頂級和牛' WHERE menu_item_id = 7; -- 和牛壽喜燒
-- ID 8 天婦羅拼盤：留空，不貼標籤

-- 4. 甜點類
UPDATE menu_item SET feature_tags = '🍵 濃郁系' WHERE menu_item_id = 9;  -- 抹茶提拉米蘇
-- ID 10 焦糖布丁：留空，不貼標籤

-- 5. 飲品/酒類
-- ID 11, 12：留空，不貼標籤
UPDATE menu_item SET feature_tags = '🧊 夏季限定' WHERE menu_item_id = 13; -- 朝日生啤
UPDATE menu_item SET feature_tags = '🍶 頂級清酒' WHERE menu_item_id = 14; -- 獺祭純米大吟釀