CREATE DATABASE IF NOT EXISTS lemongo
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE lemongo;

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------------
-- 业务闭环种子数据（张三的一次完整购买链路）
--
-- 链路：登录 -> 商品列表 -> 商品详情 -> 购物车列表 -> 创建订单 -> 我的订单 -> 模拟支付
-- 各表（order / request_log / user_activity / api_statistics / module_statistics）
-- 之间的计数与金额彼此自洽，全部对应到真实业务动作，不包含任何伪造的异常数据。
--
-- 张三购物车在 02-data.sql 中已预置：商品 1（有机柠檬）x2 + 商品 3（运动毛巾）x1
-- 本次下单将这两项全部结算，故下单后清空其购物车。
-- ---------------------------------------------------------------------------

-- 1. 订单主表：商品 1 x2 (39.90) + 商品 3 x1 (19.90) = 99.70，已完成支付
INSERT INTO order_master
    (id, order_no, user_id, total_amount, order_status, pay_status,
     payment_method, paid_time, remark, created_at, updated_at)
VALUES
    (1, 'LG20260908103045123', 1, 99.70, 'PAID', 'PAID',
     'WALLET', NOW() - INTERVAL 38 MINUTE, NULL,
     NOW() - INTERVAL 45 MINUTE, NOW() - INTERVAL 38 MINUTE);

-- 2. 订单明细（商品快照）
INSERT INTO order_item
    (order_id, product_id, product_name, product_image_url, unit_price, quantity, subtotal)
VALUES
    (1, 1, 'LemonGo 有机柠檬 5kg', '/assets/products/lemon-box.svg', 39.90, 2, 79.80),
    (1, 3, 'LemonGo 冰感运动毛巾', '/assets/products/towel.svg', 19.90, 1, 19.90);

-- 3. 下单结算后清空张三的购物车（与真实 OrderService 行为一致）
DELETE FROM cart_item WHERE user_id = 1;

-- 3.1 页面会话：张三从登录到最近一次退出共停留 20 分钟
INSERT INTO login_log
    (user_id, username, login_time, login_ip, user_agent, login_status,
     last_active_time, logout_time, active_seconds, session_status, created_at)
VALUES
    (1, 'zhangsan', NOW() - INTERVAL 58 MINUTE, '127.0.0.1', 'seed-session',
     1, NOW() - INTERVAL 38 MINUTE, NOW() - INTERVAL 38 MINUTE, 1200, 0,
     NOW() - INTERVAL 58 MINUTE);

-- 4. 请求日志：链路每一步各一条，时间/模块/负责人/控制器与 sys_api 注册表一致
INSERT INTO request_log
    (request_id, user_id, username, request_time, client_ip, http_method, uri,
     controller_name, controller_method, service_name, mapper_name,
     module_id, module_name, developer_id, developer_name,
     http_status, success, error_type, error_message,
     start_time, end_time, duration_ms, created_at)
VALUES
    ('SEED-001', 1, 'zhangsan', NOW() - INTERVAL 58 MINUTE, '127.0.0.1', 'POST', '/api/auth/login',
     'AuthController', 'login', 'AuthService.login', 'UserMapper',
     1, '用户模块', 10001, '王强', 200, 1, NULL, NULL,
     NOW() - INTERVAL 58 MINUTE, NOW() - INTERVAL 58 MINUTE + INTERVAL 45000 MICROSECOND, 45,
     NOW() - INTERVAL 58 MINUTE),
    ('SEED-002', 1, 'zhangsan', NOW() - INTERVAL 55 MINUTE, '127.0.0.1', 'GET', '/api/products',
     'ProductController', 'list', 'ProductService.listProducts', 'ProductMapper',
     2, '商品模块', 10002, '李四', 200, 1, NULL, NULL,
     NOW() - INTERVAL 55 MINUTE, NOW() - INTERVAL 55 MINUTE + INTERVAL 18000 MICROSECOND, 18,
     NOW() - INTERVAL 55 MINUTE),
    ('SEED-003', 1, 'zhangsan', NOW() - INTERVAL 52 MINUTE, '127.0.0.1', 'GET', '/api/products/1',
     'ProductController', 'detail', 'ProductService.getDetail', 'ProductMapper',
     2, '商品模块', 10002, '李四', 200, 1, NULL, NULL,
     NOW() - INTERVAL 52 MINUTE, NOW() - INTERVAL 52 MINUTE + INTERVAL 22000 MICROSECOND, 22,
     NOW() - INTERVAL 52 MINUTE),
    ('SEED-004', 1, 'zhangsan', NOW() - INTERVAL 48 MINUTE, '127.0.0.1', 'GET', '/api/cart/items',
     'CartController', 'list', 'CartService.listItems', 'CartItemMapper',
     3, '购物车模块', 10003, '张伟', 200, 1, NULL, NULL,
     NOW() - INTERVAL 48 MINUTE, NOW() - INTERVAL 48 MINUTE + INTERVAL 12000 MICROSECOND, 12,
     NOW() - INTERVAL 48 MINUTE),
    ('SEED-005', 1, 'zhangsan', NOW() - INTERVAL 45 MINUTE, '127.0.0.1', 'POST', '/api/orders',
     'OrderController', 'create', 'OrderService.createOrder', 'OrderMapper',
     4, '订单模块', 10002, '李四', 200, 1, NULL, NULL,
     NOW() - INTERVAL 45 MINUTE, NOW() - INTERVAL 45 MINUTE + INTERVAL 96000 MICROSECOND, 96,
     NOW() - INTERVAL 45 MINUTE),
    ('SEED-006', 1, 'zhangsan', NOW() - INTERVAL 40 MINUTE, '127.0.0.1', 'GET', '/api/orders',
     'OrderController', 'list', 'OrderService.listOrders', 'OrderMapper',
     4, '订单模块', 10002, '李四', 200, 1, NULL, NULL,
     NOW() - INTERVAL 40 MINUTE, NOW() - INTERVAL 40 MINUTE + INTERVAL 30000 MICROSECOND, 30,
     NOW() - INTERVAL 40 MINUTE),
    ('SEED-007', 1, 'zhangsan', NOW() - INTERVAL 38 MINUTE, '127.0.0.1', 'POST', '/api/orders/1/pay',
     'OrderController', 'pay', 'OrderService.pay', 'OrderMasterMapper',
     4, '订单模块', 10002, '李四', 200, 1, NULL, NULL,
     NOW() - INTERVAL 38 MINUTE, NOW() - INTERVAL 38 MINUTE + INTERVAL 55000 MICROSECOND, 55,
     NOW() - INTERVAL 38 MINUTE);

-- 5. 用户活跃度：张三当日 7 次请求，与上述 request_log 数量一致
INSERT INTO user_activity
    (user_id, stat_date, first_login_time, last_login_time, last_active_time,
     request_count_today, request_count_total, active_seconds_today,
     active_seconds_total, activity_score, online_status)
VALUES
    (1, CURDATE(), NOW() - INTERVAL 58 MINUTE, NOW() - INTERVAL 58 MINUTE, NOW() - INTERVAL 38 MINUTE,
     7, 7, 7, 7, 9, 1);

-- 6. API 统计：按上面 7 次请求逐接口聚合
INSERT INTO api_statistics
    (stat_date, api_id, api_path, http_method, request_count, success_count,
     error_count, total_duration_ms, max_duration_ms)
VALUES
    (CURDATE(), 2,  '/api/auth/login',       'POST', 1, 1, 0, 45,  45),
    (CURDATE(), 4,  '/api/products',         'GET',  1, 1, 0, 18,  18),
    (CURDATE(), 5,  '/api/products/{id}',    'GET',  1, 1, 0, 22,  22),
    (CURDATE(), 9,  '/api/cart/items',       'GET',  1, 1, 0, 12,  12),
    (CURDATE(), 13, '/api/orders',           'POST', 1, 1, 0, 96,  96),
    (CURDATE(), 14, '/api/orders',           'GET',  1, 1, 0, 30,  30),
    (CURDATE(), 26, '/api/orders/{id}/pay',  'POST', 1, 1, 0, 55,  55);

-- 7. 模块统计：按上面 7 次请求逐模块聚合
INSERT INTO module_statistics
    (stat_date, module_id, module_name, request_count, success_count,
     error_count, total_duration_ms, max_duration_ms)
VALUES
    (CURDATE(), 1, '用户模块',   1, 1, 0, 45,  45),
    (CURDATE(), 2, '商品模块',   2, 2, 0, 40,  22),
    (CURDATE(), 3, '购物车模块', 1, 1, 0, 12,  12),
    (CURDATE(), 4, '订单模块',   3, 3, 0, 181, 96);
