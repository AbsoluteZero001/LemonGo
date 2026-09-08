# LemonGo 总体架构

## 1. 定位与边界

LemonGo 是轻量电商业务外壳 + 请求链路可观测系统：

- 业务只需要用户、商品、购物车、订单四个真实闭环，以及少量支付模拟。
- 核心资产不是“商城功能”，而是 API → 模块 → 开发者责任映射、请求日志、活跃度、Redis 实时统计和异常定位。
- 第一阶段保持 Spring Boot 单体；分层时按业务模块清晰切分，为后续拆分保留空间。

## 2. 一次请求的完整链路

```text
Vue3/Axios
    |
    v
OncePerRequestFilter
    生成/透传 Request ID，初始化 RequestContext，写入 MDC
    |
    v
Controller  ->  Service  ->  Mapper  ->  MySQL / Redis
    |            |            |
    +---- AOP 采集方法名、模块、耗时、异常 ----+
    |
    v
GlobalExceptionHandler（异常时按 API -> 模块 -> 开发者定位）
    |
    v
Response（携带 Request ID）
    |
    v
请求日志落库 + Redis 实时计数 + 用户活跃度更新
    |
    v
MySQL 日维度活跃度与 API/模块统计同步更新
```

## 3. 架构原则

1. **先闭环，后堆量。** 每个阶段优先保证“真实请求可以被完整追踪和解释”，功能数量服从核心链路。
2. **单体优先，边界清晰。** 代码按业务包组织，后期如需微服务化，可从 `controller/service/mapper` 边界整模块迁移。
3. **分层只做一件事。** Controller 负责 HTTP 适配，Service 负责业务规则，Mapper 负责数据访问，monitor/statistics 负责可观测性，不侵入业务代码。
4. **日志不手工重复。** Controller/Service 执行信息由 AOP 采集，业务方法不写重复日志。
5. **API 责任关系以数据库为准。** 使用 `sys_api -> sys_module -> sys_developer` 三级映射，前端错误页展示同一条责任链。
6. **Redis 管实时，MySQL 管历史。** Redis 承载在线状态、计数、活跃度快照；MySQL 承载请求日志和可查询历史。

## 4. 后端包结构

```text
com.lemongo
├── common       统一响应、常量、上下文、工具
├── config       Spring/MyBatis-Plus/OpenAPI/Web 配置
├── controller   用户端业务 API
├── service
├── service.impl
├── mapper
├── entity
├── dto
├── vo
├── exception    业务异常与全局异常处理
├── aspect       Controller/Service 链路采集切面
├── filter       OncePerRequestFilter、鉴权过滤器
├── interceptor  预留 MVC 拦截器
├── monitor      请求生命周期、责任映射查询
├── logging      请求日志、错误日志落库服务
├── statistics   Redis 实时统计与 MySQL 快照
└── util
```

## 5. 责任映射模型

数据库以三张基础表维护 API 到负责人的映射：

```text
sys_api
  api_path + http_method
  controller_name / controller_method
  service_name
  module_id
  developer_id
        |
        v
sys_module
  module_name / module_code
  developer_id
        |
        v
sys_developer
  name / employee_no / email / department
```

API 注册表支持带路径参数的 URI 模板，例如 `GET /api/products/{id}`。异常发生后，后端使用当前请求的 method + 规范化路径匹配 `sys_api`，再沿外键定位模块与开发者，返回结构化错误响应。

## 6. 数据职责

| 数据 | 载体 | 用途 |
| --- | --- | --- |
| 业务数据 | MySQL | 用户、商品、购物车、订单 |
| 责任注册表 | MySQL | developer/module/api 映射 |
| 请求历史 | MySQL | 可搜索的 request_log/error_log/login_log |
| 活跃度历史 | MySQL | 日维度 user_activity |
| 统计历史 | MySQL | api_statistics/module_statistics |
| 在线与实时计数 | Redis | user:online、request_count、error_count 等 |
| 结构化应用日志 | Logback | 排障、可观测性审计 |

## 7. Redis Key 设计

```text
user:online
user:last_active:{userId}
user:request_count:{userId}
user:activity:{userId}
api:request_count:{apiId}
api:error_count:{apiId}
module:request_count:{moduleId}
module:error_count:{moduleId}
```

当日计数附带自然日过期；请求日志与 MySQL 日维度统计在每次请求结束时同步落库。

## 8. 异常响应设计

后端不会把源码、堆栈、绝对路径或密钥直接回传。前端接收结构化错误：

```json
{
  "code": 500,
  "message": "商品详情模块发生异常",
  "requestId": "REQ-20260907183000001",
  "path": "/api/products/10001",
  "method": "GET",
  "module": "商品模块",
  "controller": "ProductController",
  "service": "ProductService",
  "developer": {
    "id": 10002,
    "name": "李四",
    "employeeNo": "DEV10002"
  },
  "timestamp": "2026-09-07 18:30:00"
}
```

## 9. 前端形态

前端分两个入口：

- 用户端：登录、商品列表/详情、购物车、订单、个人中心，用于制造真实请求。
- 监控台：Dashboard、请求日志、请求详情链路、用户活跃度、模块与开发者责任监控。

页面不追求营销化设计，以数据表、指标卡、趋势图和链路步骤图为主。

## 10. 运行形态

```text
浏览器
  |
  +-- /           用户端页面（Vite Dev Server / Nginx 静态产物）
  +-- /monitor    监控页面
  |
  v
Spring Boot :8080
  |
  +-- MySQL :3306
  +-- Redis :6379
```

