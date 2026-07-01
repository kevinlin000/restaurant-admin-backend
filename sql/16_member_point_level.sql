-- =====================================================
-- 16_member_point_level.sql
-- 會員點數拆分成
-- point_balance：折抵點數，可於訂單結帳折抵，使用後會減少
-- point_level  ：會員點數，只用來判斷會員等級，不因折抵減少
-- 規則：
-- 1. 每消費 100 元各獲得 1 點
-- 2. 訂單完成累積點數：point_balance + N、point_level
-- 3. 使用點數折抵：point_balance - N、point_level 不變
-- 4. 會員等級只看 point_level累積
-- =====================================================
-- 1. 新增 / 轉換 members.point_level 欄位。


USE restaurant_db;

SET @point_level_action = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'members'
              AND column_name = 'point_level'
        ) THEN 'EXISTS'

        WHEN EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'members'
              AND column_name = 'level_points'
        ) THEN 'RENAME'

        ELSE 'ADD'
    END
);

SET @sql = CASE @point_level_action
    WHEN 'EXISTS' THEN 'SELECT ''members.point_level already exists'' AS message'
    WHEN 'RENAME' THEN 'ALTER TABLE members RENAME COLUMN level_points TO point_level'
    ELSE 'ALTER TABLE members ADD COLUMN point_level INT NOT NULL DEFAULT 0 COMMENT ''會員升級點數（累積升級用，不因折抵減少）'' AFTER point_balance'
END;

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 統一欄位註解。
--    這裡把原本 point_balance 的語意明確改成「折抵點數」。
ALTER TABLE members
    MODIFY COLUMN point_balance INT NOT NULL DEFAULT 0 COMMENT '折抵點數（可用於訂單折抵，使用後會減少）',
    MODIFY COLUMN point_level INT NOT NULL DEFAULT 0 COMMENT '會員升級點數（累積升級用，不因折抵減少）';

ALTER TABLE members
    COMMENT = '會員資料（point_balance=折抵點數，point_level=升級累積點數）';

-- 3. 既有資料轉換。
--    只有在這次剛新增 / 剛改名 point_level 時，才把舊有 point_balance 同步到 point_level。
--    若 point_level 原本就存在，代表資料可能已經被系統使用，不覆蓋既有升級點數。
SET @sync_sql = CASE
    WHEN @point_level_action IN ('ADD', 'RENAME') THEN
        'UPDATE members SET point_level = COALESCE(NULLIF(point_level, 0), point_balance, 0) WHERE member_id IS NOT NULL'
    ELSE
        'SELECT ''skip point_level data sync because column already exists'' AS message'
END;

PREPARE stmt FROM @sync_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 會員等級改由 point_level 判斷。
--    0~29：BRONZE
--    30~59：SILVER
--    60~99：GOLD
--    100 以上：DIAMOND
UPDATE members
SET member_level = CASE
    WHEN point_level >= 100 THEN 'DIAMOND'
    WHEN point_level >= 60 THEN 'GOLD'
    WHEN point_level >= 30 THEN 'SILVER'
    ELSE 'BRONZE'
END
WHERE member_id IS NOT NULL;


