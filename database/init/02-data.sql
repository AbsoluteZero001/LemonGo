CREATE DATABASE IF NOT EXISTS lemongo
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

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
    (5, '系统监控模块', 'MONITOR', '请求日志与统计监控', 10003),
    (6, '系统管理模块', 'SYSTEM', '责任关系与系统配置', 10003);

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
    (27, '/api/monitor/developers', 'GET', 5, 'MonitorController', 'developers', 'MonitorService.developers', NULL, '开发者监控', 10003),
    (28, '/api/monitor/apis', 'GET', 5, 'MonitorController', 'apis', 'MonitorService.apis', 'SysApiMapper', '接口注册表', 10003),
    (29, '/api/monitor/errors', 'GET', 5, 'MonitorController', 'errors', 'MonitorService.errors', 'ErrorLogMapper', '异常日志', 10003),
    (30, '/api/users/me/activity', 'GET', 1, 'UserController', 'meActivity', 'UserService.meActivity', 'UserActivityMapper', '我的活跃度', 10001),
    (31, '/api/products/categories', 'GET', 2, 'ProductController', 'categories', 'ProductService.categories', 'ProductMapper', '商品分类', 10002),
    (32, '/api/admin/products', 'GET', 2, 'AdminProductController', 'list', 'ProductService.adminList', 'ProductMapper', '管理端商品列表', 10002),
    (33, '/api/admin/products', 'POST', 2, 'AdminProductController', 'create', 'ProductService.create', 'ProductMapper', '管理端新增商品', 10002),
    (34, '/api/admin/products/{id}', 'PUT', 2, 'AdminProductController', 'update', 'ProductService.update', 'ProductMapper', '管理端更新商品', 10002),
    (35, '/api/admin/products/{id}', 'DELETE', 2, 'AdminProductController', 'delete', 'ProductService.delete', 'ProductMapper', '管理端删除商品', 10002),
    (36, '/api/admin/products/{id}/restore', 'PUT', 2, 'AdminProductController', 'restore', 'ProductService.restore', 'ProductMapper', '管理端恢复商品', 10002),
    (37, '/api/admin/users', 'GET', 1, 'AdminUserController', 'list', 'AdminUserService.list', 'SysUserMapper', '管理端用户列表', 10001),
    (38, '/api/admin/users', 'POST', 1, 'AdminUserController', 'create', 'AdminUserService.create', 'SysUserMapper', '管理端新增用户', 10001),
    (39, '/api/admin/users/{id}', 'PUT', 1, 'AdminUserController', 'update', 'AdminUserService.update', 'SysUserMapper', '管理端更新用户', 10001),
    (40, '/api/admin/orders', 'GET', 4, 'AdminOrderController', 'list', 'AdminOrderService.list', 'OrderMasterMapper', '管理端订单列表', 10002),
    (41, '/api/admin/orders/{id}/status', 'PUT', 4, 'AdminOrderController', 'updateStatus', 'AdminOrderService.updateStatus', 'OrderMasterMapper', '管理端订单状态', 10002),
    (42, '/api/admin/developers', 'GET', 6, 'AdminDeveloperController', 'list', 'DeveloperAdminService.list', 'SysDeveloperMapper', '开发者列表', 10003),
    (43, '/api/admin/developers', 'POST', 6, 'AdminDeveloperController', 'create', 'DeveloperAdminService.create', 'SysDeveloperMapper', '新增开发者', 10003),
    (44, '/api/admin/developers/{id}', 'PUT', 6, 'AdminDeveloperController', 'update', 'DeveloperAdminService.update', 'SysDeveloperMapper', '更新开发者', 10003),
    (45, '/api/admin/developers/{id}', 'DELETE', 6, 'AdminDeveloperController', 'delete', 'DeveloperAdminService.delete', 'SysDeveloperMapper', '删除开发者', 10003),
    (46, '/api/admin/modules', 'GET', 6, 'AdminModuleController', 'list', 'ModuleAdminService.list', 'SysModuleMapper', '模块列表', 10003),
    (47, '/api/admin/modules', 'POST', 6, 'AdminModuleController', 'create', 'ModuleAdminService.create', 'SysModuleMapper', '新增模块', 10003),
    (48, '/api/admin/modules/{id}', 'PUT', 6, 'AdminModuleController', 'update', 'ModuleAdminService.update', 'SysModuleMapper', '更新模块', 10003),
    (49, '/api/admin/modules/{id}', 'DELETE', 6, 'AdminModuleController', 'delete', 'ModuleAdminService.delete', 'SysModuleMapper', '删除模块', 10003),
    (50, '/api/admin/apis', 'GET', 6, 'AdminApiController', 'list', 'ApiAdminService.list', 'SysApiMapper', '接口列表', 10003),
    (51, '/api/admin/apis', 'POST', 6, 'AdminApiController', 'create', 'ApiAdminService.create', 'SysApiMapper', '新增接口', 10003),
    (52, '/api/admin/apis/{id}', 'PUT', 6, 'AdminApiController', 'update', 'ApiAdminService.update', 'SysApiMapper', '更新接口', 10003),
    (53, '/api/admin/apis/{id}', 'DELETE', 6, 'AdminApiController', 'delete', 'ApiAdminService.delete', 'SysApiMapper', '删除接口', 10003),
    (54, '/api/admin/apis/refresh', 'POST', 6, 'AdminApiController', 'refresh', 'ApiAdminService.refresh', 'SysApiMapper', '刷新注册表', 10003);

-- Seed users use a salted hash design that will be finalized together with JWT login.
-- role: USER = 用户端, ADMIN = 管理端, MONITOR = 监控台.
INSERT INTO sys_user
    (id, username, password_hash, nickname, email, phone, role)
VALUES
    (1, 'zhangsan', '{seed}zhangsan-123456', '张三', 'zhangsan@lemongo.local', '13800000001', 'USER'),
    (2, 'lisi', '{seed}lisi-123456', '李四用户', 'lisi@lemongo.local', '13800000002', 'USER'),
    (3, 'admin', '{seed}admin-123456', '管理员', 'admin@lemongo.local', '13800000003', 'ADMIN'),
    (4, 'monitor', '{seed}monitor-123456', '监控员', 'monitor@lemongo.local', '13800000004', 'MONITOR');

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
