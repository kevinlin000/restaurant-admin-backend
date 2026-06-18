-- ============================================================
-- Store module demo manager account
-- 執行時機：已跑過 01_create_tables.sql + 02_insert_test_data.sql 之後
-- 用途：建立門市模組展示用 MANAGER 帳號，避免共用其他組員 admin 帳號
-- 登入：storemanager@store.local / password123
-- ============================================================

UPDATE staff s
JOIN user u ON u.user_id = s.user_id
SET s.status = 'RESIGNED'
WHERE u.email = 'store.manager@xuri.com';

UPDATE user
SET is_deleted = TRUE
WHERE email = 'store.manager@xuri.com';

INSERT INTO user (role_id, email, password_hash, name, phone, birthday, is_deleted)
SELECT
    role_id,
    'storemanager@store.local',
    '$2a$10$rJDF6fkfGR8sCuqi/N6mOuyTqvK0UYIm3Q61GWsbNl/t9.e6HK87.',
    'storemanager',
    '0966-666-666',
    '1992-06-15',
    FALSE
FROM role
WHERE role_name = 'MANAGER'
ON DUPLICATE KEY UPDATE
    role_id = VALUES(role_id),
    password_hash = VALUES(password_hash),
    name = VALUES(name),
    phone = VALUES(phone),
    birthday = VALUES(birthday),
    is_deleted = FALSE;

INSERT INTO staff (user_id, store_id, staff_no, hire_date, status)
SELECT
    user_id,
    1,
    'STOREMANAGER',
    '2026-06-15',
    'ACTIVE'
FROM user
WHERE email = 'storemanager@store.local'
ON DUPLICATE KEY UPDATE
    store_id = VALUES(store_id),
    hire_date = VALUES(hire_date),
    status = VALUES(status);
