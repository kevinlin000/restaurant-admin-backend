-- =====================================================
-- Orders 狀態修正06/22
-- =====================================================

UPDATE orders
SET status = 'PENDING'
WHERE status IN ('UNPAID', 'PAID');


-- =====================================================
-- Orders created_at 補值
-- =====================================================

UPDATE orders
SET created_at = updated_at
WHERE created_at IS NULL
  AND updated_at IS NOT NULL;

UPDATE orders
SET created_at = NOW()
WHERE created_at IS NULL;


-- =====================================================
-- Payment payment_status 註解修正
-- =====================================================

ALTER TABLE payment
MODIFY COLUMN payment_status VARCHAR(20)
NOT NULL DEFAULT 'UNPAID'
COMMENT 'UNPAID / PAID / REFUNDED';


-- =====================================================
-- Orders status 註解修正
-- =====================================================

ALTER TABLE orders
MODIFY COLUMN status VARCHAR(20)
NOT NULL DEFAULT 'PENDING'
COMMENT 'PENDING / CONFIRMED / PREPARING / READY / COMPLETED / CANCELLED';