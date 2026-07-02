-- ============================================================
-- 最新消息展示資料擴充
-- 執行時機：已建立 news_article 後執行，可重複執行。
-- 目的：讓前台最新消息與後台管理使用真實 DB 資料，不依賴前端 fallback。
-- ============================================================

USE restaurant_db;

INSERT INTO news_article (
    category,
    status,
    title,
    summary,
    content,
    published_at,
    start_date,
    end_date,
    store_id,
    cover_image_url,
    is_featured,
    sort_order,
    created_at
)
SELECT
    'OPENING',
    'PUBLISHED',
    '台中勤美店試營運公告｜午間席次優先開放',
    '新門市試營運期間採分段開放訂位，午餐、下午茶與晚餐席次將依現場準備狀況逐步增加。',
    '台中勤美店試營運期間會保留部分現場席，線上訂位以午餐與晚餐基本時段為主。若同行有兒童座椅、輪椅動線或包廂需求，請於備註先告知門市。',
    '2026-06-21',
    '2026-07-05',
    '2026-07-31',
    NULL,
    '/news-images/sushi.jpg',
    TRUE,
    5,
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM news_article WHERE title = '台中勤美店試營運公告｜午間席次優先開放'
);

INSERT INTO news_article (
    category,
    status,
    title,
    summary,
    content,
    published_at,
    start_date,
    end_date,
    store_id,
    cover_image_url,
    is_featured,
    sort_order,
    created_at
)
SELECT
    'EVENT',
    'PUBLISHED',
    '雙人餐酒夜｜週四晚餐限定席',
    '精選雙人套餐搭配指定飲品，適合慶生、約會與下班後的小型聚餐。部分門市提供吧檯席。',
    '活動採限量席次開放，菜色會依當日漁獲與季節蔬菜微調。未飲酒顧客可更換為無酒精茶飲搭配。',
    '2026-06-14',
    '2026-06-27',
    '2026-08-28',
    NULL,
    '/news-images/asahi-beer.jpg',
    FALSE,
    35,
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM news_article WHERE title = '雙人餐酒夜｜週四晚餐限定席'
);

INSERT INTO news_article (
    category,
    status,
    title,
    summary,
    content,
    published_at,
    start_date,
    end_date,
    store_id,
    cover_image_url,
    is_featured,
    sort_order,
    created_at
)
SELECT
    'NOTICE',
    'PUBLISHED',
    '外帶自取包裝調整｜鍋物與生食餐點分裝升級',
    '為維持餐點狀態，外帶自取餐盒將依品項調整為冷熱分裝，部分套餐備餐時間同步延長。',
    '生食、熟食與鍋物品項會分開包裝，建議依預約取餐時間抵達。若需餐具或分食包裝，請在點餐備註提前告知。',
    '2026-06-10',
    NULL,
    NULL,
    NULL,
    '/news-images/seafood-salad.jpg',
    FALSE,
    40,
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM news_article WHERE title = '外帶自取包裝調整｜鍋物與生食餐點分裝升級'
);

INSERT INTO news_article (
    category,
    status,
    title,
    summary,
    content,
    published_at,
    start_date,
    end_date,
    store_id,
    cover_image_url,
    is_featured,
    sort_order,
    created_at
)
SELECT
    'NOTICE',
    'PUBLISHED',
    '暑期熱門餐期提醒｜晚餐尖峰建議提前完成訂位',
    '暑期聚餐需求增加，週五晚餐、週末午餐與連假前夕為熱門時段，建議先完成線上訂位。',
    '系統會依門市席位與餐期容量即時回覆可訂時段。若指定時段已滿，可改選相近時段或其他鄰近門市。',
    '2026-07-02',
    '2026-07-02',
    '2026-08-31',
    NULL,
    '/news-images/tempura.jpg',
    TRUE,
    2,
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM news_article WHERE title = '暑期熱門餐期提醒｜晚餐尖峰建議提前完成訂位'
);

INSERT INTO news_article (
    category,
    status,
    title,
    summary,
    content,
    published_at,
    start_date,
    end_date,
    store_id,
    cover_image_url,
    is_featured,
    sort_order,
    created_at
)
SELECT
    'MEMBER',
    'PUBLISHED',
    '生日禮使用說明｜會員本人用餐享指定甘味招待',
    '生日當月會員本人內用，可依會員等級領取指定甘味或飲品招待，需於結帳前出示會員資料。',
    '生日禮不可折換現金，部分活動套餐與包場專案不併用。實際品項依各門市當日供應為準。',
    '2026-07-01',
    '2026-07-01',
    '2026-12-31',
    NULL,
    '/news-images/caramel-pudding.jpg',
    FALSE,
    45,
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM news_article WHERE title = '生日禮使用說明｜會員本人用餐享指定甘味招待'
);

INSERT INTO news_article (
    category,
    status,
    title,
    summary,
    content,
    published_at,
    start_date,
    end_date,
    store_id,
    cover_image_url,
    is_featured,
    sort_order,
    created_at
)
SELECT
    'EVENT',
    'PUBLISHED',
    '午後甘味席｜抹茶甜點與焙茶飲品組合上市',
    '平日下午限定甘味組合，提供抹茶、焙茶與季節水果甜點，適合輕食聚會與午後小憩。',
    '午後甘味席依門市供應狀態開放，部分品項每日限量。若需外帶，請以線上點餐頁實際供應為準。',
    '2026-06-28',
    '2026-07-03',
    '2026-08-30',
    NULL,
    '/news-images/japanese-tea.jpg',
    FALSE,
    50,
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM news_article WHERE title = '午後甘味席｜抹茶甜點與焙茶飲品組合上市'
);

INSERT INTO news_article (
    category,
    status,
    title,
    summary,
    content,
    published_at,
    start_date,
    end_date,
    store_id,
    cover_image_url,
    is_featured,
    sort_order,
    created_at
)
SELECT
    'NOTICE',
    'PUBLISHED',
    '訂位訂金規則更新｜特殊餐期需於期限內完成付款',
    '包廂、大人數與指定節慶餐期可能需要訂金保留席次，逾期未付款系統將釋出座位。',
    '訂金規則會依餐期與門市設定顯示於訂位流程。取消與退款期限請以訂位完成頁及通知信內容為準。',
    '2026-06-26',
    NULL,
    NULL,
    NULL,
    '/news-images/reservation.jpg',
    FALSE,
    55,
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM news_article WHERE title = '訂位訂金規則更新｜特殊餐期需於期限內完成付款'
);

INSERT INTO news_article (
    category,
    status,
    title,
    summary,
    content,
    published_at,
    start_date,
    end_date,
    store_id,
    cover_image_url,
    is_featured,
    sort_order,
    created_at
)
SELECT
    'MEMBER',
    'PUBLISHED',
    '會員點數制度調整｜折抵點數與升等點數分開累積',
    '會員點數將區分可折抵餘額與等級累積點數，消費折抵不影響會員等級判斷。',
    '訂單完成後會同步累積折抵點數與等級點數；取消訂單時，系統會依付款與點數使用狀態退回可折抵餘額。',
    '2026-06-25',
    '2026-07-01',
    NULL,
    NULL,
    '/news-images/aburi-salmon-sushi.jpg',
    FALSE,
    60,
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM news_article WHERE title = '會員點數制度調整｜折抵點數與升等點數分開累積'
);

-- ============================================================
-- 既有種子資料補圖：三篇早於本檔案存在的消息缺 cover_image_url，
-- 導致前端全靠分類預設圖，同分類文章畫面看起來重複。
-- 只在尚未設定時補值，可重複執行。
-- ============================================================

UPDATE news_article SET cover_image_url = '/news-images/sashimi.jpg'
WHERE title = '夏旬和食祭｜海味、炙燒與清酒佐餐同步登場' AND cover_image_url IS NULL;

UPDATE news_article SET cover_image_url = '/news-images/tofu.jpg'
WHERE title = '重要提醒｜請透過官方網站、APP 或門市電話完成訂位' AND cover_image_url IS NULL;

UPDATE news_article SET cover_image_url = '/news-images/matcha-dessert.jpg'
WHERE title = '敘日會員週｜平日午餐點數雙倍累積' AND cover_image_url IS NULL;
