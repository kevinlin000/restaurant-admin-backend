-- =========================================================================
-- 專案名稱：餐飲點餐系統
-- 功能說明：擴充 36 筆菜單測試資料 (對齊現有 category_id 1~8)和特色標籤精簡化
-- 建立日期：2026-06-25
-- =========================================================================


INSERT INTO menu_item (category_id, item_name, description, base_price, image_url, allergen_info, is_active, created_at, updated_at, feature_tags) VALUES
-- ==========================================
-- 1. 前菜 (category_id = 1)
-- ==========================================
(1, '胡麻菠菜', '鮮嫩菠菜搭配特製濃郁日式焙煎胡麻醬，清爽開胃。', 90.00, 'https://i.ibb.co/PZ5sXhft/Spinach.png', '含堅果、大豆', 1, NOW(), NOW(), '["主廚推薦", "人氣爆棚"]'),
(1, '和風溫泉蛋', '半熟滑嫩溫泉蛋，搭配鰹魚昆布高湯醬汁與蔥花。', 60.00, 'https://i.ibb.co/39pg4Vdt/egg.jpg', '含蛋、大豆', 1, NOW(), NOW(), '["經典必點"]'),
(1, '日式香烤茄子', '現烤多汁長茄，淋上生薑泥與特製甘純醬油。', 80.00, 'https://i.ibb.co/3YCpJsXQ/image.png', '含大豆', 1, NOW(), NOW(), '["嚴選食材"]'),
(1, '和風醃漬小黃瓜', '微酸帶甜的清脆小黃瓜，熟客必點的開胃極品。', 50.00, 'https://i.ibb.co/KpbrdBnL/Chilled-Cucumber-Salad.jpg', '無', 1, NOW(), NOW(), '["季節限定"]'),
(1, '築地風厚蛋燒', '每日現做的高湯玉子燒，口感綿密鬆軟，帶有微甜滋味。', 100.00, 'https://i.ibb.co/n8sF0Wnx/Thick-Tamagoyaki.jpg', '含蛋、大豆', 1, NOW(), NOW(), '["手作工法", "經典必點"]'),
(1, '日式冷拌番茄', '精選熟成番茄切片，淋上特製和風油醋醬與洋蔥碎。', 75.00, 'https://i.ibb.co/4gTJzZHs/Chilled-Tomato-Salad.jpg ', '無', 1, NOW(), NOW(), '["季節限定", "嚴選食材"]'),

-- ==========================================
-- 2. 生魚片 (category_id = 2)
-- ==========================================
(2, '極上鮭魚肚生魚片', '嚴選肥美鮭魚腹肉，油脂豐厚，入口即化。', 380.00, 'https://i.ibb.co/7tHGFVBN/Sashimi-Salmon-Belly.jpg', '含生食', 1, NOW(), NOW(), '["主廚推薦", "嚴選食材"]'),
(2, '築地風鮪魚生魚片', '色澤紅潤的深海鮪魚，肉質紮實且帶有回甘層次。', 360.00, 'https://i.ibb.co/zWgXfY6L/Tuna-Sashimi.png', '含生食', 1, NOW(), NOW(), '["經典必點"]'),
(2, '鮮美真鯛生魚片', '白身魚的最高代表，味道清雅淡赤，口感極具彈性。', 340.00, 'https://i.ibb.co/spvmMbF0/Bream-Sashimi.png', '含生食', 1, NOW(), NOW(), '["季節限定"]'),
(2, '生食級干貝綜合盛合', '北海道干貝搭配甜蝦與鮭魚，極致奢華的海洋饗宴。', 480.00, 'https://i.ibb.co/NdnfTD0z/Raw-grade-scallops.png', '含生食、甲殼類', 1, NOW(), NOW(), '["極致奢華", "鮮味極致"]'),

-- ==========================================
-- 3. 壽司 (category_id = 3)
-- ==========================================
(3, '炙燒焦糖鮭魚握壽司', '炙燒後的鮭魚油脂與香甜脆焦糖完美咬合，女孩最愛。', 160.00, 'https://i.ibb.co/KkrhsWZ/Grilled-Caramelized-Salmon-Nigiri.jpg', '含生食、大豆', 1, NOW(), NOW(), '["人氣爆棚", "鮮味極致"]'),
(3, '盛夏加州卷壽司', '內裹酪梨與蟹肉棒，外層灑上飽滿飛魚卵，口感豐富。', 240.00, 'https://i.ibb.co/rGNfVjpN/California-Roll-Sushi.png', '含甲殼類、蛋', 1, NOW(), NOW(), '["季節限定", "手作工法"]'),
(3, '極上星鰻握壽司', '整條豐腴星鰻刷上香甜特調醬汁，香氣撲鼻。', 220.00, 'https://i.ibb.co/mFP36Gwy/Star-Eel-Nigiri-Sushi.png', '含大豆、小麥', 1, NOW(), NOW(), '["職人精神"]'),
(3, '海老天婦羅花壽司', '現炸金黃炸蝦包入壽司中，酥脆與醋飯的完美結合。', 260.00, 'https://i.ibb.co/2YhSxnNg/Hana-Sushi.jpg', '含甲殼類、小麥', 1, NOW(), NOW(), '["人氣爆棚", "鮮味極致"]'),
(3, '辛味噌鮪魚軍艦', '細切鮪魚拌入特製和風辣味噌，微辣開胃欲罷不能。', 150.00, 'https://i.ibb.co/rKRJRQFP/Tuna-Gunkan.png', '含生食、大豆', 1, NOW(), NOW(), '["主廚推薦", "人氣爆棚"]'),

-- ==========================================
-- 4. 熟食 / 定食類 (category_id = 4)
-- ==========================================
(4, '生薑燒肉定食', '經典必吃！特選豬五花與老薑甜醬汁爆炒，超級下飯。', 290.00, 'https://i.ibb.co/SXmQF02Z/Ginger-Braised-Pork-Set-Meal.jpg', '含大豆、小麥', 1, NOW(), NOW(), '["主廚推薦", "人氣爆棚"]'),
(4, '鹽烤鯖魚定食', '嚴選挪威鯖魚鹽烤至外皮金黃酥脆，飽含豐富魚油。', 280.00, 'https://i.ibb.co/7d8c3HRL/Salt-Grilled-Mackerel-Set-Meal.jpg', '無', 1, NOW(), NOW(), '["嚴選食材", "鮮味極致"]'),
(4, '牛肉壽喜燒定食', '醇厚壽喜燒醬汁熬煮板腱牛、蒟蒻絲與烤豆腐，暖胃首選。', 360.00, 'https://i.ibb.co/5WHBtHxt/Beef-Sukiyaki-Set-Meal.jpg', '含大豆、小麥', 1, NOW(), NOW(), '["職人精神", "嚴選食材"]'),
(4, '日式碳烤雞肉定食', '去骨雞腿肉碳烤至微焦香，搭配特製鹽蔥醬，香氣逼人。', 310.00, 'https://i.ibb.co/Gf4FjBKf/Grilled-Chicken-Set-Meal.jpg', '含大豆', 1, NOW(), NOW(), '["主廚推薦", "手作工法"]'),
(4, '豪華名古屋鰻魚三吃定食', '頂級蒲燒鰻魚，可單吃、加配料、注高湯做成茶泡飯。', 490.00, 'https://i.ibb.co/pvqV8D5f/Three-Course-Eel-Set-Meal.jpg', '含大豆、小麥', 1, NOW(), NOW(), '["極致奢華", "經典必點"]'),

-- ==========================================
-- 5. 炸物 (category_id = 5)
-- ==========================================
(5, '南蠻炸雞塊', '金黃酥脆雞塊裹上南蠻酸甜醬，再淋上滿滿手作塔塔醬。', 180.00, 'https://images.unsplash.com/photo-1569058242253-92a9c755a0ec?q=80&w=600', '含大豆、蛋、乳製品', 1, NOW(), NOW(), '["人氣爆棚", "鮮味極致"]'),
(5, '炸蝦天婦羅盛合', '大蝦三隻與季節時蔬（地瓜、香菇），外衣輕薄酥脆。', 240.00, 'https://i.ibb.co/pB9V9L70/Assorted-Fried-Shrimp-Tempura-Platter.jpg', '含甲殼類、小麥', 1, NOW(), NOW(), '["主廚推薦", "人氣爆棚"]'),
(5, '酥炸厚切里肌豬排', '嚴選台灣國產豬里肌，卡滋酥脆，飽滿多汁。', 190.00, 'https://i.ibb.co/fz4BKQZ4/Fried-Pork-Loin-Chop.png', '含小麥、蛋', 1, NOW(), NOW(), '["經典必點", "職人精神"]'),
(5, '北海道廣島炸牡蠣', '肥美多汁的廣島生蠔裹粉酥炸，咬下滿滿海洋鮮甜。', 210.00, 'https://i.ibb.co/bjsst802/Fried-Oysters.png', '含軟體動物、小麥', 1, NOW(), NOW(), '["嚴選食材", "鮮味極致"]'),
(5, '炸起司可樂餅', '日本男爵馬鈴薯泥包裹香濃莫札瑞拉起司，牽絲牽不停。', 120.00, 'https://i.ibb.co/hJr84m0j/Fried-Cheese-Croquettes.jpg', '含乳製品、小麥', 1, NOW(), NOW(), '["主廚推薦", "人氣爆棚"]'),

-- ==========================================
-- 6. 甜點 (category_id = 6)
-- ==========================================
(6, '宇治金時黃金蕨餅', '軟糯Q彈的日式蕨餅，灑滿極上靜岡抹茶粉與香甜紅豆泥。', 110.00, 'https://i.ibb.co/vxyC32nM/Uji-Kintoki-Golden-Warabi-Mochi.png', '無', 1, NOW(), NOW(), '["季節限定", "手作工法"]'),
(6, '黑糖香草冰淇淋大福', 'Q彈大福皮包裹香草冰淇淋，淋上沖繩極品黑糖蜜。', 90.00, 'https://i.ibb.co/BKccd34Z/Brown-Sugar-Vanilla-Ice-Cream-Daifuku.jpg', '含乳製品', 1, NOW(), NOW(), '["季節限定", "手作工法"]'),
(6, '十勝紅豆白玉湯', '溫熱香甜的紅豆湯，搭配現煮Q彈手作白玉小湯圓。', 100.00, 'https://i.ibb.co/HD6vsMdw/Red-Bean-and-Shiratama-Soup.png', '無', 1, NOW(), NOW(), '["經典必點", "季節限定"]'),
(6, '炙燒焦糖布丁', '綿密細緻的手工布丁，表面覆蓋薄脆琥珀色炙燒焦糖。', 120.00, 'https://i.ibb.co/FNLHspW/Pudding.png', '含乳製品、蛋', 1, NOW(), NOW(), '["主廚推薦", "季節限定"]'),

-- ==========================================
-- 7. 飲料 (category_id = 7)
-- ==========================================
(7, '靜岡御用冰抹茶', '無糖微苦的高級靜岡抹茶，茶香回甘，解膩首選。', 120.00, 'https://i.ibb.co/Y4Fv5qZ3/Matcha-Ice-Tea.jpg', '無', 1, NOW(), NOW(), '["職人精神", "嚴選食材"]'),
(7, '巨峰葡萄氣泡飲', '日本巨峰葡萄果汁搭配清爽氣泡水，微甜沁涼。', 90.00, 'https://i.ibb.co/DDYfc8Ct/Grape-Sparkling-Drink.png', '無', 1, NOW(), NOW(), '["季節限定", "手作工法"]'),
(7, '紀州梅子可爾必思', '酸甜可爾必思融入極品紀州梅果醬，層次感豐富。', 95.00, 'https://i.ibb.co/r2bsPVMp/Kishu-Plum-Calpis.png', '含乳製品', 1, NOW(), NOW(), '["主廚推薦", "人氣爆棚"]'),
(7, '日式焙茶鮮奶', '現泡慢火焙茶融合在地鮮乳，口感醇厚乳香濃郁。', 110.00, 'https://i.ibb.co/v4b7gNxZ/Hojicha-with-Fresh-Milk.png', '含乳製品', 1, NOW(), NOW(), '["職人精神", "嚴選食材"]'),

-- ==========================================
-- 8. 酒水 (category_id = 8)
-- ==========================================
(8, '獺祭純米大吟釀 45ml (杯裝)', '頂級日本清酒代表，帶有精緻的水果香氣與乾淨餘韻。', 280.00, 'https://i.ibb.co/7d44g6XY/Dassai-Junmai-Daiginjo.jpg', '無', 1, NOW(), NOW(), '["極致奢華", "職人精神"]'),
(8, '三得利頂級生啤酒 (The Premium Malts)', '泡沫如奶油般細緻滑順，麥香濃郁，居酒屋靈魂。', 150.00, 'https://i.ibb.co/LdwvTgHY/Suntory-Premium-Draft-Beer.png', '無', 1, NOW(), NOW(), '["職人精神", "嚴選食材"]'),
(8, '盛夏鮮柚沙瓦', '現榨新鮮柚子果肉、燒酎與氣泡水調和，清新爽口。', 140.00, 'https://i.ibb.co/4nHWXjH8/Fresh-Pomelo-Sour.png', '無', 1, NOW(), NOW(), '["季節限定", "鮮味極致"]'),
(8, '角瓶威士忌蘇打 (Highball)', '經典角瓶威士忌與強氣泡水，搭配新鮮檸檬角，解膩神物。', 130.00, 'https://i.ibb.co/NdV3mrDj/Suntory-Corner-Bottle.png', '無', 1, NOW(), NOW(), '["經典必點", "人氣爆棚"]'),
(8, '大關甘口梅酒', '日本大關酒造釀製，風味酸甜濃郁，冰鎮後風味絕佳。', 140.00, 'https://i.ibb.co/8LsyC7NB/Ogami-Sweet-Plum-Liqueur.jpg', '無', 1, NOW(), NOW(), '["限量供應"]');

-- =====================================================================
-- 特色標籤精簡化 (依品項名稱對位)
-- =====================================================================

-- 1. 暫時關閉 MySQL 安全更新限制
SET SQL_SAFE_UPDATES = 0;

-- 2. 大部分標籤維持 3-4 道菜的豐富度
UPDATE menu_item SET feature_tags = '["主廚推薦", "手作工法"]' WHERE item_name IN ('胡麻菠菜', '筑地風厚蛋燒', '生薑燒肉定食', '日式碳烤雞肉定食');
UPDATE menu_item SET feature_tags = '["人氣爆棚", "鮮味極致"]' WHERE item_name IN ('極上鮭魚肚生魚片', '炙燒焦糖鮭魚握壽司', '南蠻炸雞塊', '海老天婦羅花壽司');
UPDATE menu_item SET feature_tags = '["經典必點", "職人精神"]' WHERE item_name IN ('和風溫温泉蛋', '筑地風鮪魚生魚片', '酥炸厚切里肌豬排', '豪華名古屋鰻魚三吃定食');
UPDATE menu_item SET feature_tags = '["嚴選食材", "鮮味極致"]' WHERE item_name IN ('鮮美真鯛生魚片', '北海道廣島炸牡蠣', '鹽烤鯖魚定食', '日式香烤茄子');
UPDATE menu_item SET feature_tags = '["季節限定", "手作工法"]' WHERE item_name IN ('宇治金時黃金蕨餅', '黑糖香草冰淇淋大福', '巨峰葡萄氣泡飲', '盛夏加州卷壽司');
UPDATE menu_item SET feature_tags = '["職人精神", "嚴選食材"]' WHERE item_name IN ('靜岡御用冰抹茶', '日式焙茶鮮奶', '三得利頂級生啤酒 (The Premium Malts)', '牛肉壽喜燒定食');
UPDATE menu_item SET feature_tags = '["經典必點", "季節限定"]' WHERE item_name IN ('和风醃漬小黃瓜', '日式冷拌番茄', '十勝紅豆白玉湯', '炙燒焦糖布丁');
UPDATE menu_item SET feature_tags = '["主廚推薦", "人氣爆棚"]' WHERE item_name IN ('辛味噌鮪魚軍艦', '炸蝦天婦羅盛合', '炸起司可樂餅', '紀州梅子可爾必思');

-- 3. 保留 1-2 道菜的「限量/奢華」稀有標籤
UPDATE menu_item SET feature_tags = '["極致奢華", "限量供應"]' WHERE item_name = '生食級干貝綜合盛合';
UPDATE menu_item SET feature_tags = '["極致奢華", "職人精神"]' WHERE item_name = '獺祭純米大吟釀 45 (杯裝)';
UPDATE menu_item SET feature_tags = '["限量供應"]' WHERE item_name = '大關甘口梅酒';

-- =========================================================================