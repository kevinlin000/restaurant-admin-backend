-- 讓顧客端可用訪客身分訂位，並保存訂位聯絡資料。
-- 可重複執行。

ALTER TABLE reservation
MODIFY COLUMN user_id BIGINT NULL COMMENT '會員訂位才填；訪客訂位可為 null';

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE reservation ADD COLUMN customer_name VARCHAR(50) NOT NULL DEFAULT '''' COMMENT ''訂位顧客姓名'' AFTER slot_id',
        'SELECT ''reservation.customer_name already exists'''
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'reservation'
      AND column_name = 'customer_name'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE reservation ADD COLUMN customer_phone VARCHAR(20) NOT NULL DEFAULT '''' COMMENT ''訂位顧客電話'' AFTER customer_name',
        'SELECT ''reservation.customer_phone already exists'''
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'reservation'
      AND column_name = 'customer_phone'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE reservation ADD COLUMN customer_email VARCHAR(100) NULL COMMENT ''訂位顧客 Email'' AFTER customer_phone',
        'SELECT ''reservation.customer_email already exists'''
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'reservation'
      AND column_name = 'customer_email'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
