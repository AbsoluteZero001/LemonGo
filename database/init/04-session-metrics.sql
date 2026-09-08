CREATE DATABASE IF NOT EXISTS lemongo
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE lemongo;

SET NAMES utf8mb4;

-- Adds login-session tracking columns for databases initialized before this change.
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'login_log'
       AND column_name = 'last_active_time') = 0,
    'ALTER TABLE login_log ADD COLUMN last_active_time DATETIME(3) NULL AFTER created_at',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'login_log'
       AND column_name = 'logout_time') = 0,
    'ALTER TABLE login_log ADD COLUMN logout_time DATETIME(3) NULL AFTER last_active_time',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'login_log'
       AND column_name = 'active_seconds') = 0,
    'ALTER TABLE login_log ADD COLUMN active_seconds INT NOT NULL DEFAULT 0 AFTER logout_time',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'login_log'
       AND column_name = 'session_status') = 0,
    'ALTER TABLE login_log ADD COLUMN session_status TINYINT NOT NULL DEFAULT 0 AFTER active_seconds',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'login_log'
       AND index_name = 'idx_login_session') = 0,
    'ALTER TABLE login_log ADD KEY idx_login_session (user_id, session_status)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE login_log
SET last_active_time = COALESCE(last_active_time, login_time)
WHERE login_status = 1;

-- Existing installs have no session boundaries yet, so only the newest successful
-- login is opened as the current page session and older ones stay closed.
UPDATE login_log ll
JOIN (
    SELECT user_id, MAX(id) AS max_id
    FROM login_log
    WHERE login_status = 1 AND user_id IS NOT NULL
    GROUP BY user_id
) latest ON latest.max_id = ll.id
SET ll.last_active_time = COALESCE(ll.last_active_time, ll.login_time),
    ll.logout_time = NULL,
    ll.session_status = 1
WHERE ll.logout_time IS NULL
  AND ll.session_status = 0;
