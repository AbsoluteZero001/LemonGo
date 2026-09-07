CREATE DATABASE IF NOT EXISTS lemongo
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE lemongo;

SET NAMES utf8mb4;

INSERT INTO request_log
    (request_id, user_id, username, request_time, client_ip, http_method, uri,
     controller_name, controller_method, service_name, mapper_name,
     module_id, module_name, developer_id, developer_name,
     http_status, success, error_type, error_message,
     start_time, end_time, duration_ms)
VALUES
    ('DEMO-01', 1, 'zhangsan', DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 9 HOUR, '127.0.0.1', 'GET', '/api/products',
     'ProductController', 'list', 'ProductService.list', 'ProductMapper',
     2, '商品模块', 10002, '李四', 200, 1, NULL, NULL,
     DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 9 HOUR, DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 9 HOUR + INTERVAL 18 SECOND, 18),
    ('DEMO-02', 1, 'zhangsan', DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 11 HOUR, '127.0.0.1', 'GET', '/api/products/1',
     'ProductController', 'detail', 'ProductService.detail', 'ProductMapper',
     2, '商品模块', 10002, '李四', 200, 1, NULL, NULL,
     DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 11 HOUR, DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 11 HOUR + INTERVAL 32 SECOND, 32),
    ('DEMO-03', 2, 'lisi', DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 14 HOUR, '127.0.0.1', 'GET', '/api/cart/items',
     'CartController', 'list', 'CartService.listItems', 'CartItemMapper',
     3, '购物车模块', 10003, '张伟', 200, 1, NULL, NULL,
     DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 14 HOUR, DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 14 HOUR + INTERVAL 12 SECOND, 12),
    ('DEMO-04', 1, 'zhangsan', DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 10 HOUR, '127.0.0.1', 'GET', '/api/orders',
     'OrderController', 'list', 'OrderService.listOrders', 'OrderMasterMapper',
     4, '订单模块', 10002, '李四', 200, 1, NULL, NULL,
     DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 10 HOUR, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 10 HOUR + INTERVAL 45 SECOND, 45),
    ('DEMO-05', 2, 'lisi', DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 16 HOUR, '127.0.0.1', 'GET', '/api/monitor/dashboard',
     'MonitorController', 'dashboard', 'MonitorService.dashboard', NULL,
     5, '系统监控模块', 10003, '张伟', 200, 1, NULL, NULL,
     DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 16 HOUR, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 16 HOUR + INTERVAL 22 SECOND, 22),
    ('DEMO-06', 1, 'zhangsan', DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 9 HOUR, '127.0.0.1', 'GET', '/api/products/2',
     'ProductController', 'detail', 'ProductService.detail', 'ProductMapper',
     2, '商品模块', 10002, '李四', 200, 1, NULL, NULL,
     DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 9 HOUR, DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 9 HOUR + INTERVAL 21 SECOND, 21),
    ('DEMO-07', 2, 'lisi', DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 13 HOUR, '127.0.0.1', 'POST', '/api/cart/items',
     'CartController', 'add', 'CartService.add', 'CartItemMapper',
     3, '购物车模块', 10003, '张伟', 200, 1, NULL, NULL,
     DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 13 HOUR, DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 13 HOUR + INTERVAL 36 SECOND, 36),
    ('DEMO-08', 1, 'zhangsan', DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 8 HOUR, '127.0.0.1', 'POST', '/api/orders',
     'OrderController', 'create', 'OrderService.createOrder', 'OrderMasterMapper',
     4, '订单模块', 10002, '李四', 200, 1, NULL, NULL,
     DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 8 HOUR, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 8 HOUR + INTERVAL 96 SECOND, 96),
    ('DEMO-09', 1, 'zhangsan', DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 9 HOUR, '127.0.0.1', 'GET', '/api/products/3',
     'ProductController', 'detail', 'ProductService.detail', 'ProductMapper',
     2, '商品模块', 10002, '李四', 200, 1, NULL, NULL,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 9 HOUR, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 9 HOUR + INTERVAL 19 SECOND, 19),
    ('DEMO-10', 2, 'lisi', NOW() - INTERVAL 1 HOUR, '127.0.0.1', 'GET', '/api/test/error/500',
     'TestErrorController', 'serverError', 'TestErrorService.throwServerError', NULL,
     5, '系统监控模块', 10003, '张伟', 500, 0, 'SYSTEM_ERROR', '模拟 Service 内部异常',
     NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 1 HOUR + INTERVAL 28 SECOND, 28),
    ('DEMO-11', 1, 'zhangsan', DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 15 HOUR, '127.0.0.1', 'GET', '/api/test/error/database',
     'TestErrorController', 'databaseError', 'TestErrorService.throwDatabaseError', 'TestErrorMapper',
     5, '系统监控模块', 10003, '张伟', 500, 0, 'DATABASE_ERROR', '模拟数据库异常',
     DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 15 HOUR, DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 15 HOUR + INTERVAL 41 SECOND, 41),
    ('DEMO-12', 1, 'zhangsan', NOW() - INTERVAL 3 HOUR, '127.0.0.1', 'GET', '/api/products',
     'ProductController', 'list', 'ProductService.list', 'ProductMapper',
     2, '商品模块', 10002, '李四', 200, 1, NULL, NULL,
     NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 3 HOUR + INTERVAL 16 SECOND, 16),
    ('DEMO-13', 2, 'lisi', NOW() - INTERVAL 2 HOUR, '127.0.0.1', 'GET', '/api/cart/items',
     'CartController', 'list', 'CartService.listItems', 'CartItemMapper',
     3, '购物车模块', 10003, '张伟', 200, 1, NULL, NULL,
     NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR + INTERVAL 14 SECOND, 14),
    ('DEMO-14', 1, 'zhangsan', NOW() - INTERVAL 30 MINUTE, '127.0.0.1', 'GET', '/api/products/1',
     'ProductController', 'detail', 'ProductService.detail', 'ProductMapper',
     2, '商品模块', 10002, '李四', 200, 1, NULL, NULL,
     NOW() - INTERVAL 30 MINUTE, NOW() - INTERVAL 30 MINUTE + INTERVAL 24 SECOND, 24);

INSERT INTO error_log
    (request_id, api_id, module_id, developer_id, error_code, error_type,
     error_message, exception_class, occurred_at)
VALUES
    ('DEMO-10', 23, 5, 10003, 500, 'SYSTEM_ERROR', '模拟 Service 内部异常', 'java.lang.IllegalStateException', NOW() - INTERVAL 1 HOUR),
    ('DEMO-11', 24, 5, 10003, 500, 'DATABASE_ERROR', '模拟数据库异常', 'org.springframework.dao.DataAccessResourceFailureException', DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 15 HOUR);

INSERT INTO user_activity
    (user_id, stat_date, first_login_time, last_login_time, last_active_time,
     request_count_today, request_count_total, active_seconds_today,
     active_seconds_total, activity_score, online_status)
VALUES
    (1, CURDATE(), DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 9 HOUR, CURDATE(), CURDATE(),
     2, 9, 140, 760, 78, 1),
    (2, CURDATE(), DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 10 HOUR, CURDATE(), CURDATE(),
     2, 5, 120, 560, 64, 1);

INSERT INTO api_statistics
    (stat_date, api_id, api_path, http_method, request_count, success_count,
     error_count, total_duration_ms, max_duration_ms)
SELECT CURDATE(), 4, '/api/products', 'GET', 1, 1, 0, 16000, 16000
UNION ALL SELECT CURDATE(), 5, '/api/products/{id}', 'GET', 1, 1, 0, 24000, 24000
UNION ALL SELECT CURDATE(), 9, '/api/cart/items', 'GET', 1, 1, 0, 14000, 14000
UNION ALL SELECT CURDATE(), 23, '/api/test/error/500', 'GET', 1, 0, 1, 28000, 28000;

INSERT INTO module_statistics
    (stat_date, module_id, module_name, request_count, success_count,
     error_count, total_duration_ms, max_duration_ms)
VALUES
    (CURDATE(), 2, '商品模块', 2, 2, 0, 40000, 24000),
    (CURDATE(), 3, '购物车模块', 1, 1, 0, 12000, 12000),
    (CURDATE(), 5, '系统监控模块', 1, 0, 1, 28000, 28000);
