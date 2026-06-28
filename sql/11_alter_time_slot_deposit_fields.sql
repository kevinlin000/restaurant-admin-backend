-- time_slot 增加訂位訂金設定欄位
-- requires_deposit：true=顧客訂此時段後需先支付訂金
-- deposit_amount：該時段訂金金額；未啟用訂金時維持 0

ALTER TABLE time_slot
  ADD COLUMN requires_deposit BOOLEAN NOT NULL DEFAULT FALSE COMMENT '此時段是否需要支付訂金' AFTER is_rule_generated,
  ADD COLUMN deposit_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '此時段訂金金額' AFTER requires_deposit;
