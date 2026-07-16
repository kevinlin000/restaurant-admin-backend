-- ============================================================
-- FAQ text field compatibility migration
-- 執行時機：已跑過 sql/13_faq_item.sql，且資料庫已存在 faq_item 時
--
-- 背景：
--   sql/13_faq_item.sql 使用 CREATE TABLE IF NOT EXISTS。
--   如果本機或共用 DB 先前已建立舊版 faq_item，重新執行 13 不會修改欄位型別。
--   這支 migration 用來把舊表同步到目前後端 Entity / DTO 的欄位長度。
-- ============================================================

ALTER TABLE faq_item
  MODIFY question VARCHAR(255) NOT NULL,
  MODIFY answer TEXT NOT NULL,
  MODIFY keywords TEXT NULL;
