USE lemongo;

SET NAMES utf8mb4;

INSERT INTO sys_developer (id, name, employee_no, email, department) VALUES
    (10001, '王强', 'DEV10001', 'wangqiang@lemongo.local', '电商后端组'),
    (10002, '李四', 'DEV10002', 'lisi@lemongo.local', '电商后端组'),
    (10003, '张伟', 'DEV10003', 'zhangwei@lemongo.local', '平台服务组');

INSERT INTO sys_module (id, module_name, module_code, description, developer_id) VALUES
    (1, '用户模块', 'USER', '登录、个人信息与活跃度', 10001),
    (2, '商品模块', 'PRODUCT', '商品查询与维护', 10002),
    (3, '购物车模块', 'CART', '购物车操作', 10003),
    (4, '订单模块', 'ORDER', '下单与订单查询', 10002),
    (5, '系统监控模块', 'MONITOR', '请求日志与统计监控', 10003);

INSERT INTO sys_api
    (id, api_path, http_method, module_id, controller_name, controller_method,
     service_name, mapper_name, description, developer_id)
VALUES
    (1, '/api/health', 'GET', 5, 'HealthController', 'health', NULL, NULL, '健康检查', 10003),
    (2, '/api/auth/login', 'POST', 1, 'AuthController', 'login', 'AuthService.login', 'UserMapper', '登录', 10001),
    (3, '/api/users/me', 'GET', 1, 'UserController', 'me', 'UserService.me', 'UserMapper', '当前用户信息', 10001),
    (4, '/api/products', 'GET', 2, 'ProductController', 'list', 'ProductService.listProducts', 'ProductMapper', '商品列表', 10002),
    (5, '/api/products/{id}', 'GET', 2, 'ProductController', 'detail', 'ProductService.getDetail', 'ProductMapper', '商品详情', 10002),
    (6, '/api/products', 'POST', 2, 'ProductController', 'create', 'ProductService.createProduct', 'ProductMapper', '新增商品', 10002),
    (7, '/api/products/{id}', 'PUT', 2, 'ProductController', 'update', 'ProductService.updateProduct', 'ProductMapper', '更新商品', 10002),
    (8, '/api/products/{id}', 'DELETE', 2, 'ProductController', 'delete', 'ProductService.deleteProduct', 'ProductMapper', '删除商品', 10002),
    (9, '/api/cart/items', 'GET', 3, 'CartController', 'list', 'CartService.listItems', 'CartItemMapper', '购物车列表', 10003),
    (10, '/api/cart/items', 'POST', 3, 'CartController', 'add', 'CartService.addItem', 'CartItemMapper', '加入购物车', 10003),
    (11, '/api/cart/items/{id}', 'PUT', 3, 'CartController', 'updateQuantity', 'CartService.updateQuantity', 'CartItemMapper', '修改数量', 10003),
    (12, '/api/cart/items/{id}', 'DELETE', 3, 'CartController', 'remove', 'CartService.removeItem', 'CartItemMapper', '移除购物车项', 10003),
    (13, '/api/orders', 'POST', 4, 'OrderController', 'create', 'OrderService.createOrder', 'OrderMapper', '创建订单', 10002),
    (14, '/api/orders', 'GET', 4, 'OrderController', 'list', 'OrderService.listOrders', 'OrderMapper', '我的订单', 10002),
    (15, '/api/orders/{id}', 'GET', 4, 'OrderController', 'detail', 'OrderService.getOrder', 'OrderMapper', '订单详情', 10002),
    (16, '/api/monitor/dashboard', 'GET', 5, 'MonitorController', 'dashboard', 'MonitorService.dashboard', NULL, '监控首页指标', 10003),
    (17, '/api/monitor/request-logs', 'GET', 5, 'MonitorController', 'requestLogs', 'MonitorService.requestLogs', 'RequestLogMapper', '请求日志', 10003),
    (18, '/api/monitor/request-logs/{requestId}', 'GET', 5, 'MonitorController', 'requestDetail', 'MonitorService.requestDetail', 'RequestLogMapper', '请求详情', 10003),
    (19, '/api/monitor/users/activity', 'GET', 5, 'MonitorController', 'userActivity', 'MonitorService.userActivity', 'UserActivityMapper', '用户活跃度', 10003),
    (20, '/api/monitor/modules', 'GET', 5, 'MonitorController', 'modules', 'MonitorService.modules', 'ModuleStatisticsMapper', '模块责任监控', 10003),
    (21, '/api/test/error/400', 'GET', 5, 'TestErrorController', 'badRequest', NULL, NULL, '模拟 400', 10003),
    (22, '/api/test/error/404', 'GET', 5, 'TestErrorController', 'notFound', NULL, NULL, '模拟 404', 10003),
    (23, '/api/test/error/500', 'GET', 5, 'TestErrorController', 'serverError', 'TestErrorService.throwServerError', NULL, '模拟 500', 10003),
    (24, '/api/test/error/database', 'GET', 5, 'TestErrorController', 'databaseError', 'TestErrorService.throwDatabaseError', 'TestErrorMapper', '模拟数据库异常', 10003),
    (25, '/api/test/error/service', 'GET', 5, 'TestErrorController', 'serviceError', 'TestErrorService.throwServiceError', NULL, '模拟 Service 异常', 10003),
    (26, '/api/orders/{id}/pay', 'POST', 4, 'OrderController', 'pay', 'OrderService.pay', 'OrderMasterMapper', '模拟支付订单', 10002),
    (27, '/api/monitor/developers', 'GET', 5, 'MonitorController', 'developers', 'MonitorService.developers', NULL, '开发者监控', 10003);

-- Seed users use a salted hash design that will be finalized together with JWT login.
INSERT INTO sys_user
    (id, username, password_hash, nickname, email, phone)
VALUES
    (1, 'zhangsan', '{seed}zhangsan-123456', '张三', 'zhangsan@lemongo.local', '13800000001'),
    (2, 'lisi', '{seed}lisi-123456', '李四用户', 'lisi@lemongo.local', '13800000002');

INSERT INTO product
    (id, product_name, category, price, stock, sales, image_url, detail_text, status)
VALUES
    (1, 'LemonGo 有机柠檬 5kg', '生鲜水果', 39.90, 500, 3280, '/assets/products/lemon-box.svg', '模拟电商商品：新鲜有机柠檬。', 1),
    (2, 'LemonGo 随行榨汁杯', '厨房电器', 129.00, 300, 1205, '/assets/products/juicer-cup.svg', '模拟电商商品：便携榨汁杯。', 1),
    (3, 'LemonGo 冰感运动毛巾', '运动户外', 19.90, 1000, 8921, '/assets/products/towel.svg', '模拟电商商品：轻薄快干毛巾。', 1),
    (4, 'LemonGo 轻量双肩包', '箱包配件', 259.00, 200, 635, '/assets/products/backpack.svg', '模拟电商商品：通勤双肩包。', 1),
    (5, 'LemonGo 智能保温杯', '家居生活', 169.00, 400, 2170, '/assets/products/thermos.svg', '模拟电商商品：智能显温保温杯。', 1),
    (6, 'LemonGo 午睡眼罩套装', '家居生活', 49.90, 600, 4142, '/assets/products/eye-mask.svg', '模拟电商商品：遮光眼罩与耳塞。', 1);

INSERT INTO cart_item (user_id, product_id, quantity, checked)
VALUES
    (1, 1, 2, 1),
    (1, 3, 1, 1);
