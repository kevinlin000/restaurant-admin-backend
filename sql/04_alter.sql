-- 訂單金額
ALTER TABLE orders
ADD COLUMN final_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00
AFTER total_amount;