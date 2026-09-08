CREATE DATABASE IF NOT EXISTS lemongo
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE lemongo;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS sys_developer (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'developer id',
    name VARCHAR(50) NOT NULL COMMENT 'developer name',
    employee_no VARCHAR(50) NOT NULL COMMENT 'employee number',
    email VARCHAR(100) DEFAULT NULL,
    department VARCHAR(100) DEFAULT NULL COMMENT 'department',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_employee_no (employee_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='developers';

CREATE TABLE IF NOT EXISTS sys_module (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    module_name VARCHAR(100) NOT NULL COMMENT 'module display name',
    module_code VARCHAR(50) NOT NULL COMMENT 'module code',
    description VARCHAR(255) DEFAULT NULL,
    developer_id BIGINT UNSIGNED NOT NULL COMMENT 'responsible developer',
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_module_code (module_code),
    KEY idx_module_developer (developer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='business modules';

CREATE TABLE IF NOT EXISTS sys_api (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    api_path VARCHAR(255) NOT NULL COMMENT 'normalized path, e.g. /api/products/{id}',
    http_method VARCHAR(10) NOT NULL,
    module_id BIGINT UNSIGNED NOT NULL,
    controller_name VARCHAR(100) NOT NULL,
    controller_method VARCHAR(100) NOT NULL,
    service_name VARCHAR(100) DEFAULT NULL,
    mapper_name VARCHAR(100) DEFAULT NULL,
    description VARCHAR(255) DEFAULT NULL,
    developer_id BIGINT UNSIGNED NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_api (api_path, http_method),
    KEY idx_api_module (module_id),
    KEY idx_api_developer (developer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='API ownership registry';

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(128) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    email VARCHAR(100) DEFAULT NULL,
    phone VARCHAR(30) DEFAULT NULL,
    avatar_url VARCHAR(255) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'role: USER/ADMIN/MONITOR',
    online_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 offline, 1 online',
    first_login_time DATETIME(3) DEFAULT NULL,
    last_login_time DATETIME(3) DEFAULT NULL,
    last_visit_time DATETIME(3) DEFAULT NULL,
    last_active_time DATETIME(3) DEFAULT NULL,
    activity_score INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='store users';

CREATE TABLE IF NOT EXISTS product (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    product_name VARCHAR(120) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    sales INT NOT NULL DEFAULT 0,
    image_url VARCHAR(500) DEFAULT NULL,
    detail_text VARCHAR(2000) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    version INT NOT NULL DEFAULT 0,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_product_category (category),
    KEY idx_product_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='products';

CREATE TABLE IF NOT EXISTS cart_item (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    product_id BIGINT UNSIGNED NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    checked TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_cart_user_product (user_id, product_id),
    KEY idx_cart_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='shopping cart items';

CREATE TABLE IF NOT EXISTS order_master (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_no VARCHAR(40) NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    order_status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    pay_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    payment_method VARCHAR(20) DEFAULT NULL,
    paid_time DATETIME(3) DEFAULT NULL,
    cancelled_time DATETIME(3) DEFAULT NULL,
    finished_time DATETIME(3) DEFAULT NULL,
    remark VARCHAR(255) DEFAULT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_order_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='order headers';

CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id BIGINT UNSIGNED NOT NULL,
    product_id BIGINT UNSIGNED NOT NULL,
    product_name VARCHAR(120) NOT NULL COMMENT 'snapshot name',
    product_image_url VARCHAR(500) DEFAULT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_order_item_order (order_id),
    KEY idx_order_item_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='order lines';

CREATE TABLE IF NOT EXISTS request_log (
    request_id VARCHAR(60) NOT NULL,
    user_id BIGINT UNSIGNED DEFAULT NULL,
    username VARCHAR(50) DEFAULT NULL,
    request_time DATETIME(3) NOT NULL,
    client_ip VARCHAR(64) DEFAULT NULL,
    http_method VARCHAR(10) NOT NULL,
    uri VARCHAR(300) NOT NULL,
    param_summary VARCHAR(2000) DEFAULT NULL,
    controller_name VARCHAR(100) DEFAULT NULL,
    controller_method VARCHAR(100) DEFAULT NULL,
    service_name VARCHAR(100) DEFAULT NULL,
    mapper_name VARCHAR(100) DEFAULT NULL,
    module_id BIGINT UNSIGNED DEFAULT NULL,
    module_name VARCHAR(100) DEFAULT NULL,
    developer_id BIGINT UNSIGNED DEFAULT NULL,
    developer_name VARCHAR(50) DEFAULT NULL,
    http_status INT NOT NULL,
    success TINYINT NOT NULL DEFAULT 1,
    error_type VARCHAR(50) DEFAULT NULL,
    error_message VARCHAR(2000) DEFAULT NULL,
    start_time DATETIME(3) NOT NULL,
    end_time DATETIME(3) NOT NULL,
    duration_ms INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (request_id),
    KEY idx_request_time (request_time),
    KEY idx_request_user (user_id, request_time),
    KEY idx_request_status (http_status, request_time),
    KEY idx_request_module (module_id, request_time),
    KEY idx_request_uri (uri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='request access logs';

CREATE TABLE IF NOT EXISTS error_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    request_id VARCHAR(60) NOT NULL,
    api_id BIGINT UNSIGNED DEFAULT NULL,
    module_id BIGINT UNSIGNED DEFAULT NULL,
    developer_id BIGINT UNSIGNED DEFAULT NULL,
    error_code INT NOT NULL,
    error_type VARCHAR(50) DEFAULT NULL,
    error_message VARCHAR(2000) DEFAULT NULL,
    exception_class VARCHAR(255) DEFAULT NULL,
    stack_trace MEDIUMTEXT,
    occurred_at DATETIME(3) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_error_request (request_id),
    KEY idx_error_module (module_id, occurred_at),
    KEY idx_error_code (error_code, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='error events';

CREATE TABLE IF NOT EXISTS login_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED DEFAULT NULL,
    username VARCHAR(50) DEFAULT NULL,
    login_time DATETIME(3) NOT NULL,
    login_ip VARCHAR(64) DEFAULT NULL,
    user_agent VARCHAR(500) DEFAULT NULL,
    login_status TINYINT NOT NULL COMMENT '1 success, 0 fail',
    fail_reason VARCHAR(255) DEFAULT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_login_user_time (user_id, login_time),
    KEY idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='login logs';

CREATE TABLE IF NOT EXISTS user_activity (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    stat_date DATE NOT NULL,
    first_login_time DATETIME(3) DEFAULT NULL,
    last_login_time DATETIME(3) DEFAULT NULL,
    last_active_time DATETIME(3) DEFAULT NULL,
    request_count_today INT NOT NULL DEFAULT 0,
    request_count_total INT NOT NULL DEFAULT 0,
    active_seconds_today INT NOT NULL DEFAULT 0,
    active_seconds_total INT NOT NULL DEFAULT 0,
    activity_score INT NOT NULL DEFAULT 0,
    online_status TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_activity_date (user_id, stat_date),
    KEY idx_activity_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='daily user activity';

CREATE TABLE IF NOT EXISTS api_statistics (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    stat_date DATE NOT NULL,
    api_id BIGINT UNSIGNED NOT NULL,
    api_path VARCHAR(255) DEFAULT NULL,
    http_method VARCHAR(10) DEFAULT NULL,
    request_count INT NOT NULL DEFAULT 0,
    success_count INT NOT NULL DEFAULT 0,
    error_count INT NOT NULL DEFAULT 0,
    total_duration_ms BIGINT NOT NULL DEFAULT 0,
    max_duration_ms INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_api_stat (stat_date, api_id),
    KEY idx_api_stat_request (request_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='daily API statistics';

CREATE TABLE IF NOT EXISTS module_statistics (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    stat_date DATE NOT NULL,
    module_id BIGINT UNSIGNED NOT NULL,
    module_name VARCHAR(100) DEFAULT NULL,
    request_count INT NOT NULL DEFAULT 0,
    success_count INT NOT NULL DEFAULT 0,
    error_count INT NOT NULL DEFAULT 0,
    total_duration_ms BIGINT NOT NULL DEFAULT 0,
    max_duration_ms INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_module_stat (stat_date, module_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='daily module statistics';

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    request_id VARCHAR(60) DEFAULT NULL,
    user_id BIGINT UNSIGNED DEFAULT NULL,
    api_id BIGINT UNSIGNED DEFAULT NULL,
    module_id BIGINT UNSIGNED DEFAULT NULL,
    operation_type VARCHAR(50) DEFAULT NULL,
    operation_desc VARCHAR(500) DEFAULT NULL,
    result_code INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_operation_request (request_id),
    KEY idx_operation_module_time (module_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='operation audit logs';

