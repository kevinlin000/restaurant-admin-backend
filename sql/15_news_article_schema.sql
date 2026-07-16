CREATE TABLE IF NOT EXISTS news_article (
    news_id BIGINT NOT NULL AUTO_INCREMENT,
    category VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    title VARCHAR(160) NOT NULL,
    summary VARCHAR(320) NOT NULL,
    content TEXT NULL,
    cover_image_url VARCHAR(500) NULL,
    published_at DATE NOT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    store_id BIGINT NULL,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (news_id),
    INDEX idx_news_public (is_deleted, status, category, is_featured, sort_order, published_at),
    INDEX idx_news_store (store_id, is_deleted),
    CONSTRAINT fk_news_article_store
        FOREIGN KEY (store_id)
        REFERENCES store (store_id)
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
    is_featured,
    sort_order
)
SELECT
    'EVENT',
    'PUBLISHED',
    '夏旬和食祭｜海味、炙燒與清酒佐餐同步登場',
    '以鮭魚、干貝、季節野菜與吟釀酒香搭出夏季限定菜色，內用套餐可加購指定佐餐飲品。',
    '本季菜單以清爽海味與桌邊炙燒為主軸，保留職人料理的節奏，也讓聚餐更有儀式感。',
    '2026-06-24',
    '2026-06-24',
    '2026-08-31',
    NULL,
    TRUE,
    1
WHERE NOT EXISTS (
    SELECT 1 FROM news_article WHERE title = '夏旬和食祭｜海味、炙燒與清酒佐餐同步登場'
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
    is_featured,
    sort_order
)
SELECT
    'NOTICE',
    'PUBLISHED',
    '重要提醒｜請透過官方網站、APP 或門市電話完成訂位',
    '敘日未授權第三方代訂平台收取訂金或轉售席次，所有訂位資訊以官方網站、會員 APP 與門市公告為準。',
    '敘日不會要求顧客透過私人帳號匯款，也不會以非官方 LINE 帳號確認付款。',
    '2026-06-20',
    NULL,
    NULL,
    NULL,
    FALSE,
    20
WHERE NOT EXISTS (
    SELECT 1 FROM news_article WHERE title = '重要提醒｜請透過官方網站、APP 或門市電話完成訂位'
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
    is_featured,
    sort_order
)
SELECT
    'MEMBER',
    'PUBLISHED',
    '敘日會員週｜平日午餐點數雙倍累積',
    '會員平日 11:30 至 14:00 內用，單筆滿額享點數雙倍累積，可與生日禮擇優使用。',
    '活動適用會員本人消費，點數將於結帳後自動入帳，企業包場與外帶訂單不適用。',
    '2026-06-18',
    '2026-07-01',
    '2026-07-14',
    NULL,
    FALSE,
    30
WHERE NOT EXISTS (
    SELECT 1 FROM news_article WHERE title = '敘日會員週｜平日午餐點數雙倍累積'
);
