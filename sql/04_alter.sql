-- 訂單金額
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE orders ADD COLUMN final_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER total_amount',
        'SELECT ''orders.final_amount already exists'''
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'orders'
      AND column_name = 'final_amount'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE orders
SET final_amount = COALESCE(final_amount, total_amount, 0.00);

ALTER TABLE orders
MODIFY COLUMN final_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00;

-- 發票類型/載具號碼
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE orders ADD COLUMN invoice_type VARCHAR(30) DEFAULT ''NONE''',
        'SELECT ''orders.invoice_type already exists'''
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'orders'
      AND column_name = 'invoice_type'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE orders
SET invoice_type = COALESCE(invoice_type, 'NONE');

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE orders ADD COLUMN carrier_number VARCHAR(20)',
        'SELECT ''orders.carrier_number already exists'''
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'orders'
      AND column_name = 'carrier_number'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 修改 store hour / table 欄位型別
ALTER TABLE store_hour
MODIFY COLUMN day_of_week INT NOT NULL;

ALTER TABLE table_info
MODIFY COLUMN table_size INT NOT NULL;
