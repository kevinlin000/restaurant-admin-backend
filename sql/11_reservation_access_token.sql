-- 新增訂位成功頁公開查詢 token
-- 信件連結讀取單筆訂位的驗證碼（不是會員登入 JWT）

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'reservation'
      AND column_name = 'access_token'
);

SET @sql := IF(
    @col_exists = 0,
    'ALTER TABLE reservation ADD COLUMN access_token VARCHAR(64) NULL COMMENT ''訂位成功頁公開查詢 token'' AFTER customer_email',
    'SELECT ''reservation.access_token already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE reservation
SET access_token = REPLACE(UUID(), '-', '')
WHERE reservation_id IS NOT NULL
  AND access_token IS NULL;

SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'reservation'
      AND index_name = 'uk_reservation_access_token'
);

SET @sql := IF(
    @idx_exists = 0,
    'ALTER TABLE reservation ADD UNIQUE KEY uk_reservation_access_token (access_token)',
    'SELECT ''uk_reservation_access_token already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
