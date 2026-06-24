-- time_slot 增加 / 修正星期與規則來源欄位
-- day_of_week：1=週一...7=週日，使用 INT 對應 Java Integer
-- is_rule_generated：true=星期規則批次產生；false=自訂日期或單日修改

ALTER TABLE time_slot
  ADD COLUMN day_of_week INT NULL COMMENT '1=週一...7=週日' AFTER reservation_date,
  ADD COLUMN is_rule_generated BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否由星期規則批次產生' AFTER is_open;

UPDATE time_slot
SET day_of_week = WEEKDAY(reservation_date) + 1
WHERE slot_id > 0
  AND day_of_week IS NULL;
