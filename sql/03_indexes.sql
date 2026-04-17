-- ============================================================
-- 索引優化
-- 針對常用查詢加索引，提升查詢效能
-- ============================================================

USE restaurant_db;

-- 門市查詢（按縣市篩選是最常用的操作）
CREATE INDEX idx_store_city ON store(city);
CREATE INDEX idx_store_status ON store(status);
CREATE INDEX idx_store_brand ON store(brand_id);

-- 營業時間查詢（訂位時判斷「這天這時段有開嗎」）
CREATE INDEX idx_store_hour_store_day ON store_hour(store_id, day_of_week);

-- 特殊休息日查詢（訂位時判斷「這天有沒有公休」）
CREATE INDEX idx_store_holiday_store_date ON store_holiday(store_id, holiday_date);

-- 桌位查詢（訂位時找可用桌位）
CREATE INDEX idx_table_store_status ON table_info(store_id, status);

-- 訂位查詢（看某天某店的訂位）
CREATE INDEX idx_reservation_store_date ON reservation(store_id, reservation_date);
CREATE INDEX idx_reservation_member ON reservation(member_id);

-- 訂單查詢
CREATE INDEX idx_orders_store ON orders(store_id);
CREATE INDEX idx_orders_member ON orders(member_id);

-- 點數查詢（查某會員的點數歷史）
CREATE INDEX idx_point_tx_member ON point_transaction(member_id);

-- 付款查詢
CREATE INDEX idx_payment_order ON payment(order_id);
