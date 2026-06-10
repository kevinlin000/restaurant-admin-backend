-- 訂單金額
ALTER TABLE orders
ADD COLUMN final_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00
AFTER total_amount;

-- 發票類型/載具號碼
ALTER TABLE orders
ADD COLUMN invoice_type VARCHAR(30) DEFAULT 'NONE',
ADD COLUMN carrier_number VARCHAR(20);