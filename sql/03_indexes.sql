-- ============================================================
-- 索引優化
-- 針對常用查詢加索引，提升查詢效能
-- 註：UNIQUE KEY 已自帶索引（store_hour / store_holiday / time_slot
--     / reservation_capacity 等的複合唯一鍵），這裡只補非唯一的常用查詢欄位
-- ============================================================

USE restaurant_db;

-- 門市查詢（按縣市篩選是最常用的操作）
CREATE INDEX idx_store_city ON store(city);
CREATE INDEX idx_store_status ON store(status);
CREATE INDEX idx_store_brand ON store(brand_id);

-- 桌位查詢（產生庫存、現場指派用）
CREATE INDEX idx_table_store_status ON table_info(store_id, status);

-- 員工查詢（後台依分店列員工）
CREATE INDEX idx_staff_store ON staff(store_id);

-- 時段查詢（訂位時找「某店某天有哪些可訂時段」——最熱路徑）
CREATE INDEX idx_time_slot_store_date ON time_slot(store_id, reservation_date);

-- 訂位查詢
CREATE INDEX idx_reservation_user ON reservation(user_id);
CREATE INDEX idx_reservation_store ON reservation(store_id);
CREATE INDEX idx_reservation_slot ON reservation(slot_id);

-- 訂單查詢
CREATE INDEX idx_orders_store ON orders(store_id);
CREATE INDEX idx_orders_user ON orders(user_id);

-- 點數查詢（查某人的點數歷史）
CREATE INDEX idx_point_tx_user ON point_transaction(user_id);

-- 付款查詢
CREATE INDEX idx_payment_order ON payment(order_id);
CREATE INDEX idx_payment_reservation ON payment(reservation_id);
