-- ============================================================
-- 修復舊版最新消息 seed 造成的 zero datetime
-- 原因：JDBC 無法把 0000-00-00 00:00:00 轉成 Java LocalDateTime，
-- 會導致 GET /api/news 回 500。
-- 執行時機：已建立 news_article 後，可重複執行。
-- ============================================================

USE restaurant_db;

UPDATE news_article
SET created_at = CURRENT_TIMESTAMP
WHERE CAST(created_at AS CHAR) LIKE '0000-00-00%';

UPDATE news_article
SET updated_at = NULL
WHERE CAST(updated_at AS CHAR) LIKE '0000-00-00%';
