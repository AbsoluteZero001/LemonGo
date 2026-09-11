# LemonGo 乐檬购

> 一个以 **请求链路可观测性与模块责任问责** 为核心主题的模拟电商系统。

LemonGo 刻意保持小体量：用 **用户、商品、购物车、订单 + 模拟支付** 拼出一条真实可操作的业务闭环，把主要精力放在让**每一个请求都可解释、可追踪、可问责**。它不追求"商城功能多"，而追求"一次请求能被完整解释"——这也是整个项目最想表达的东西：

1. 谁、在什么时间、从哪个 IP 访问了哪个 API？
2. 这个请求依次经过了 Controller → Service → Mapper，最后落到 MySQL / Redis，各层耗时多少？
3. 请求成功了吗？失败发生在哪个接口、哪个模块、哪一层？
4. 这个接口或模块由哪一位开发者负责？出问题找谁？

业务在这里不是目的，而是**制造真实观测数据的引擎**：商城闭环让请求日志、错误日志、活跃度、日维度统计有真实流量可记，而这些可观测性数据反过来又能解释业务为什么慢、为什么错、谁该负责。

> 默认密码、种子数据和 JWT 密钥均为演示配置，适合教学、原型演示与可观测性实验，不应直接用于生产环境。

---

## 目录

- [核心价值](#核心价值)
- [业务闭环](#业务闭环)
- [技术栈](#技术栈)
- [请求链路](#请求链路)
- [目录结构](#目录结构)
- [快速开始](#快速开始)
- [演示账号](#演示账号)
- [前端路由](#前端路由)
- [环境变量](#环境变量)
- [API 概览](#api-概览)
- [实时链路（WebSocket）](#实时链路websocket)
- [日志体系详解](#日志体系详解)
- [常用命令](#常用命令)
- [当前进度](#当前进度)
- [常见问题](#常见问题)
- [License](#license)

---

## 核心价值

LemonGo 的价值不在"又一个电商 Demo"，而在一个可复用的**可观测性骨架**。它回答了后端系统最难回答的三个问题：

### 1. 可解释 —— 每个请求都有完整上下文

- 每个请求由 `TraceFilter` 透传或生成一个 `X-Request-Id`，贯穿**响应头、统一响应体、MDC 日志、数据库日志**四者。
- 浏览器拿到 Request ID，就能反查这条请求在 Controller/Service/Mapper 每一层走了多久、最终 HTTP 状态是什么。
- 前端拿到这个 ID 不需要翻日志文件，直接调监控接口就能看到整条链路。

### 2. 可追踪 —— 分层耗时与链路步骤

- `ChainTraceAspect` 通过 AOP 无侵入地采集每次请求**首次经过**的 Controller、Service、Mapper 方法及各自耗时。
- 请求结束时统一落库，形成一条可查询的"链路步骤"：`前端入口 → 接口 → Controller → Service → Mapper → HTTP 返回`。
- 监控台"实时链路"页面通过 WebSocket 即时广播 `REQUEST_COMPLETED`，用户刚点完一个按钮，监控台就能看到这次请求的完整链路。

### 3. 可问责 —— 接口 → 模块 → 开发者责任链

- 用 `sys_api` → `sys_module` → `sys_developer` 三张表维护"接口归谁管"的完整责任链。
- 异常发生时，全局异常处理器按当前请求的 `method + 规范化路径` 匹配到接口，再沿外键一路定位到模块和负责人。
- 前端错误页与监控台展示的是同一条责任链：**"商品详情模块发生异常"，责任人：李四（DEV10002）**。

> 一句话总结核心主题：**业务制造流量，链路解释流量，责任追责到人。**

---

## 业务闭环

LemonGo 用四个真实业务域 + 模拟支付，构成一条**最小但完整、可真实操作、能持续产生观测数据**的闭环：

```text
登录 / 注册
    │
    v
浏览商品列表 ──▶ 查看商品详情
    │
    v
加入购物车 ──▶ 修改数量 / 勾选 / 移除
    │
    v
提交订单（锁定库存）
    │
    v
模拟支付 ──▶ 订单完成
    │
    v
个人中心：查看订单、编辑资料
```

闭环设计原则：

1. **业务够真**：商品是真实 MySQL CRUD，下单会校验并扣减库存（乐观锁 `version`），订单有状态流转（`CREATED → PAID → FINISHED / CANCELLED`），不是写死的假数据。
2. **边界清晰**：用户、商品、购物车、订单四大域各自成包，Controller/Service/Mapper 分层只做一件事，为后续拆分留空间。
3. **反哺可观测**：每一次浏览、加购、下单、支付，都会真实触发请求落库、Redis 计数、活跃度与统计更新——业务越活跃，可观测数据越丰富，两个目标互相增强。

| 业务域 | 关键能力 | 对应的可观测产出 |
| --- | --- | --- |
| 用户 | 注册、登录、退出、页面心跳、资料编辑 | `login_log`、`user_activity`、在线状态 |
| 商品 | 列表、分类、详情、管理端 CRUD | `request_log`、`api_statistics` |
| 购物车 | 加购、改数量、勾选、移除 | `request_log`、`api_statistics` |
| 订单 | 下单、列表、详情、模拟支付 | `request_log`、`module_statistics` |
| 模拟异常 | 400/404/500/database/service 五类演示接口 | `error_log`、责任定位闭环 |

此外，系统内置五类**模拟异常接口**，用来专门验证"异常落库 → 责任定位 → 前端错误页"这条闭环是否真的打通。

---

## 技术栈

| 层级 | 技术 | 版本 |
| --- | --- | --- |
| 后端 | Java | 21 |
| 后端 | Spring Boot | 3.5.x |
| 后端 | MyBatis-Plus | 3.5.17 |
| 后端 | SpringDoc (OpenAPI) | 2.8.17 |
| 后端 | JJWT | 0.12.7 |
| 前端 | Vue | 3.5.x |
| 前端 | Vite | 8.x |
| 前端 | TypeScript | 6.x |
| 前端 | Element Plus | 2.11.x |
| 前端 | ECharts | 6.x |
| 前端 | Pinia / Vue Router | 3.x / 4.5.x |
| 存储 | MySQL | 8.4 |
| 缓存 / 实时计数 | Redis | 7.4 |
| 应用日志 | Logback（文件滚动） | Spring Boot 内建 |

---

## 请求链路

```text
Vue 3 / Axios
  |
  v
TraceFilter
  - 透传或生成 X-Request-Id
  - 初始化 RequestContext，写入 MDC
  - 响应头带回 Request ID
  |
  v
AuthInterceptor（解析 Bearer Token，校验角色）
  |
  v
Controller -> Service -> Mapper -> MySQL / Redis
  |              |            |
  +--- ChainTraceAspect 记录首次经过的分层方法与耗时 ---+
  |
  v
GlobalExceptionHandler + ErrorTraceResolver（写入 error_log）
  |
  v
统一 Result 响应（code / message / requestId / timestamp）
  |
  v
TraceFilter finally -> TraceCompletionService
  - 写入 request_log
  - 更新 user_activity（日维度活跃度）
  - upsert api_statistics / module_statistics（日维度统计）
  - 更新 Redis 在线窗口与计数器
  - WebSocket 广播 REQUEST_COMPLETED
```

核心可观测代码集中在 `backend/src/main/java/com/lemongo/observability/`，详细的包设计、Redis Key 与异常模型见 [docs/01-architecture.md](docs/01-architecture.md)。

---

## 目录结构

```text
LemonGo
├── backend/                     Spring Boot 3.x 单体后端
│   └── src/main/java/com/lemongo
│       ├── common/             统一响应、状态码、请求上下文、Request ID 生成
│       ├── config/             Spring / MyBatis-Plus / OpenAPI / Web / JWT 配置
│       ├── controller/         业务、管理、监控、模拟异常等 API
│       ├── service/            业务逻辑
│       ├── mapper/             MyBatis-Plus Mapper
│       ├── entity/ dto/ vo/    实体、入参、出参
│       ├── exception/          BusinessException 与全局异常处理
│       ├── aspect/             分层链路采集切面（ChainTraceAspect）
│       ├── filter/             TraceFilter、鉴权过滤器
│       ├── observability/      请求完成落库、错误定位、Redis 实时统计与日维度统计
│       └── responsibility/     API 责任注册表（ApiRegistry）
├── frontend/                    Vue 3 + Vite + TypeScript 前端
│   └── src/
│       ├── views/user/         商品、购物车、订单、个人中心
│       ├── views/admin/        商品、用户、订单、开发者、模块、接口管理
│       ├── views/monitor/      Dashboard、实时链路、接口注册表、异常日志
│       ├── views/              request-log/、users/、modules/、developers/ 监控页面
│       ├── api/                接口请求封装与 WebSocket 实时订阅
│       ├── stores/             登录状态与应用状态
│       ├── router/             三端路由与角色守卫
│       └── layouts/            通用布局
├── database/init/               初始化 SQL：schema、基础数据、演示数据
├── docs/                        架构说明与里程碑文档
├── docker-compose.yml           MySQL + Redis 本地基础设施
└── .env.example                 Docker Compose 环境变量示例
```

---

## 快速开始

### 前置要求

- JDK 21
- Node.js 20+（Vite 8 要求）
- Docker 与 Docker Compose（用于启动 MySQL 和 Redis）

### 1. 准备 `.env`

```bash
# macOS / Linux
cp .env.example .env
```

```powershell
# Windows PowerShell
Copy-Item .env.example .env
```

`.env` 仅被 `docker compose` 读取，Spring Boot 不会自动加载它。示例值如下：

```dotenv
MYSQL_ROOT_PASSWORD=root123456
MYSQL_PASSWORD=lemongo123
MYSQL_PORT=3306
REDIS_PORT=6379
```

### 2. 启动基础设施

```bash
docker compose up -d
```

该命令启动 MySQL 与 Redis。首次创建 MySQL 数据卷时，会自动执行 `database/init/` 下的 `01-schema.sql`、`02-data.sql`、`03-demo-data.sql`、`04-session-metrics.sql` 完成建库、建表、基础数据与演示数据初始化。

如需清空并重新初始化本地数据：

```bash
docker compose down -v
docker compose up -d
```

> `down -v` 会删除 MySQL 和 Redis 的命名数据卷，请仅在确认可以清空本地数据时执行。

### 3. 启动后端

后端默认数据库配置是 `root / 123456`，与 Compose 内建的 `lemongo / lemongo123` 不同。使用容器内建用户最省事：

```bash
# macOS / Linux
cd backend
DB_USERNAME=lemongo DB_PASSWORD=lemongo123 ./mvnw spring-boot:run
```

```powershell
# Windows PowerShell
cd backend
$env:DB_USERNAME = "lemongo"
$env:DB_PASSWORD = "lemongo123"
.\mvnw.cmd spring-boot:run
```

如果本机 MySQL 正好使用 `root / 123456`，也可以直接运行 `./mvnw spring-boot:run` 或 `.\mvnw.cmd spring-boot:run`。

首次运行会下载 Maven 依赖，耗时较长；后端默认端口为 `8080`。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器运行在 `http://localhost:5173`，`/api` 与 `/ws` 已分别代理到 `http://localhost:8080` 和 `ws://localhost:8080`。

### 5. 本地入口

| 入口 | 地址 |
| --- | --- |
| 后端健康检查 | http://localhost:8080/api/health |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| 用户端 | http://localhost:5173 |
| 管理端 | http://localhost:5173/admin |
| 监控台 | http://localhost:5173/monitor |

---

## 演示账号

| 用户名 | 密码 | 昵称 | 角色 / 入口 |
| --- | --- | --- | --- |
| `zhangsan` | `zhangsan-123456` | 张三 | `USER` 用户端 |
| `lisi` | `lisi-123456` | 李四用户 | `USER` 用户端 |
| `admin` | `admin-123456` | 管理员 | `ADMIN` 管理端 |
| `monitor` | `monitor-123456` | 监控员 | `MONITOR` 监控台 |

责任映射中的示例开发者：

| 姓名 | 工号 | 部门 | 负责模块 |
| --- | --- | --- | --- |
| 王强 | DEV10001 | 电商后端组 | 用户模块 |
| 李四 | DEV10002 | 电商后端组 | 商品、订单模块 |
| 张伟 | DEV10003 | 平台服务组 | 购物车、系统监控、系统管理模块 |

注册接口会直接创建 `USER` 账号并返回登录态；新账号产生的业务请求仍会写入请求日志与统计。

---

## 前端路由

前端是单页应用，路由守卫按角色分流：登录后 `USER` 进入用户端、`ADMIN` 进入管理端、`MONITOR` 进入监控台；访问越权路由会被重定向回本角色首页。

| 入口 | 路由 | 所需角色 |
| --- | --- | --- |
| 登录 | `/login` | 公开 |
| 商品列表 / 商品详情 | `/products` · `/products/:id` | `USER` |
| 购物车 | `/cart` | `USER` |
| 我的订单 | `/orders` | `USER` |
| 个人中心 | `/profile` | `USER` |
| 商品 / 用户 / 订单管理 | `/admin/products` · `/admin/users` · `/admin/orders` | `ADMIN` |
| 开发者 / 模块 / 接口管理 | `/admin/developers` · `/admin/modules` · `/admin/apis` | `ADMIN` |
| 监控总览 | `/monitor` | `MONITOR` |
| 实时链路 | `/monitor/live` | `MONITOR` |
| 请求日志 / 请求详情 | `/monitor/requests` · `/monitor/requests/:requestId` | `MONITOR` |
| 接口注册表 / 异常日志 | `/monitor/apis` · `/monitor/errors` | `MONITOR` |
| 用户活跃 / 模块 / 开发者监控 | `/monitor/users` · `/monitor/modules` · `/monitor/developers` | `MONITOR` |

---

## 环境变量

### Docker Compose（`.env`）

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `MYSQL_ROOT_PASSWORD` | `root123456` | MySQL root 密码 |
| `MYSQL_PASSWORD` | `lemongo123` | 内建应用用户 `lemongo` 的密码 |
| `MYSQL_PORT` | `3306` | 宿主机映射端口 |
| `REDIS_PORT` | `6379` | 宿主机映射端口 |

### 后端（环境变量）

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | 后端端口 |
| `SPRING_PROFILES_ACTIVE` | `dev` | Spring Profile |
| `DB_URL` | `jdbc:mysql://localhost:3306/lemongo?...` | JDBC 连接串 |
| `DB_USERNAME` | `root` | 数据库用户名 |
| `DB_PASSWORD` | `123456` | 数据库密码 |
| `REDIS_HOST` | `localhost` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | 空 | Redis 密码 |
| `LOG_PATH` | `logs` | Logback 文件日志目录（相对后端运行目录） |
| `LEMONGO_JWT_SECRET` | 演示默认值 | JWT 签名密钥，脱离本地演示环境前必须修改 |

`application-prod.yml` 使用 `${DB_URL}`、`${DB_USERNAME}`、`${DB_PASSWORD}` 且不提供兜底默认值，因此以 `SPRING_PROFILES_ACTIVE=prod` 运行时必须显式提供数据库环境变量，并自行指定 `LEMONGO_JWT_SECRET`。

---

## API 概览

| 模块 | 方法与路径 | 说明 |
| --- | --- | --- |
| 健康检查 | `GET /api/health` | 服务健康检查 |
| 认证 | `POST /api/auth/login` · `/register` · `/logout` · `/heartbeat` | 登录 / 注册 / 退出 / 页面心跳 |
| 用户 | `GET /api/users/me` · `PUT /api/users/me` | 当前用户、编辑资料 |
| 商品 | `GET /api/products` · `GET /api/products/categories` · `GET /api/products/{id}` | 商品列表、分类、详情 |
| 购物车 | `GET/POST /api/cart/items` · `PUT/DELETE /api/cart/items/{id}` | 列表、加入、改数量、移除 |
| 订单 | `POST /api/orders` · `GET /api/orders` · `GET /api/orders/{id}` · `POST /api/orders/{id}/pay` | 下单、列表、详情、模拟支付 |
| 管理端 | `/api/admin/products`、`/api/admin/users`、`/api/admin/orders`、`/api/admin/developers`、`/api/admin/modules`、`/api/admin/apis` 全套 CRUD | 商品、用户、订单、责任关系管理（ADMIN） |
| 监控台 | `/api/monitor/dashboard`、`/request-logs`、`/request-logs/{requestId}`、`/apis`、`/errors`、`/users/activity`、`/users/usage`、`/modules`、`/developers` | 指标、请求详情、接口注册表、异常、活跃度与责任监控（MONITOR） |
| 模拟异常 | `GET /api/test/error/{400,404,500,database,service}` | 五类异常演示 |

完整接口定义以 Swagger UI（`/swagger-ui.html`）为准。

### 统一响应格式

除 WebSocket 外，所有接口都返回统一结构；`requestId` 与响应头 `X-Request-Id` 一致，可直接拿去监控台检索这条请求。

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "requestId": "REQ-20260911103000001",
  "timestamp": "2026-09-11T10:30:00"
}
```

出错时结构不变，HTTP 状态码与 `code` 一致（`400` / `401` / `403` / `404` / `500`），`data` 为 `null`；异常类型、堆栈与责任链定位会写入 `error_log`，可在监控台"异常日志"或请求详情中查看。

---

## 实时链路（WebSocket）

监控台"实时链路"页面通过 WebSocket 订阅请求完成事件：

| 项 | 值 |
| --- | --- |
| 地址 | `ws://localhost:8080/ws/monitor?token=<JWT>` |
| 鉴权 | 握手时通过 `token` 查询参数传入 JWT，角色必须为 `MONITOR`，否则握手返回 401 / 403 |
| 开发环境 | 前端连接 `ws://localhost:5173/ws/monitor`，由 Vite 将 `/ws` 代理转发到后端 |
| 事件类型 | `REQUEST_COMPLETED`，每次请求落库后广播一次（`/api/auth/heartbeat` 心跳除外） |

事件负载示例：

```json
{
  "type": "REQUEST_COMPLETED",
  "data": {
    "requestLog": {
      "requestId": "REQ-20260911103000001",
      "username": "zhangsan",
      "httpMethod": "GET",
      "uri": "/api/products",
      "httpStatus": 200,
      "success": 1,
      "durationMs": 23
    },
    "layers": [
      {
        "layerType": "CONTROLLER",
        "layerName": "ProductController",
        "layerMethod": "list",
        "description": "HTTP 适配层"
      }
    ]
  },
  "timestamp": "2026-09-11T10:30:00"
}
```

前端订阅逻辑在 `frontend/src/api/realtime.ts`：断线后按指数退避自动重连（最长 5s）；没有任何订阅者时主动断开，避免空连接常驻。

---

## 日志体系详解

LemonGo 的"日志"不是单一概念，而是按**用途和生命周期**拆成三类载体，各有明确分工：

```text
┌─────────────────────────────────────────────────────────────┐
│                        日志体系                              │
│                                                             │
│  1. MySQL 结构化日志（历史、可检索、永久）                      │
│     request_log / error_log / login_log                      │
│     user_activity / api_statistics / module_statistics       │
│                                                             │
│  2. Redis 实时计数（快照、自动过期 2 天）                       │
│     online / request_count / error_count ...                 │
│                                                             │
│  3. Logback 文件日志（排障、滚动、15 天 / 2GB）                 │
│     logs/lemon-go.log                                        │
└─────────────────────────────────────────────────────────────┘
```

> 一句话分工：**MySQL 管历史，Redis 管实时，Logback 管排障。**

### 1. 日志对应的数据库表

日志结构化数据全部落在 **MySQL 单库 `lemongo`**（utf8mb4，时区 `+08:00`）。涉及日志/统计的表共 **7 张**，其中 6 张在代码中真实读写，1 张为预留：

| # | 表名 | 类型 | 用途 | 主键 / 写入时机 | 状态 |
| --- | --- | --- | --- | --- | --- |
| 1 | `request_log` | 逐条明细 | 请求访问日志 | 主键 `request_id`；请求结束时写入 | ✅ 活跃 |
| 2 | `error_log` | 逐条明细 | 错误日志（含堆栈） | 自增 `id`；异常发生时写入 | ✅ 活跃 |
| 3 | `login_log` | 逐条明细 | 登录 / 会话日志 | 自增 `id`；登录/退出/心跳时写入 | ✅ 活跃 |
| 4 | `user_activity` | 日维度聚合 | 用户活跃度 | `(user_id, stat_date)` 唯一；请求后 upsert | ✅ 活跃 |
| 5 | `api_statistics` | 日维度聚合 | 接口统计 | `(stat_date, api_id)` 唯一；请求后 upsert | ✅ 活跃 |
| 6 | `module_statistics` | 日维度聚合 | 模块统计 | `(stat_date, module_id)` 唯一；请求后 upsert | ✅ 活跃 |
| 7 | `operation_log` | 逐条明细 | 操作审计日志 | 自增 `id` | ⚠️ 预留，未启用 |

> `operation_log` 表已在 `01-schema.sql` 中定义（含操作类型、操作描述、结果码等字段），但当前代码**没有任何实体、Mapper 或写入逻辑引用它**，属于为后续"操作审计"预留的表，现阶段不会产生数据。

#### 各表字段说明

**① `request_log` —— 请求访问日志（核心表）**

每次请求（除 `/api/auth/heartbeat` 心跳外）结束时由 `TraceCompletionService` 写入一条。主键即 `request_id`，天然支持按 Request ID 精确检索。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `request_id` | VARCHAR(60) | 请求唯一 ID（主键），贯穿全链路 |
| `user_id` / `username` | BIGINT / VARCHAR(50) | 发起人（未登录为 NULL） |
| `request_time` | DATETIME(3) | 请求开始时间 |
| `client_ip` | VARCHAR(64) | 客户端 IP |
| `http_method` / `uri` | VARCHAR | 方法、URI（含查询串摘要 `param_summary`） |
| `controller_name` / `controller_method` | VARCHAR(100) | 首次经过的 Controller 及其方法 |
| `service_name` / `mapper_name` | VARCHAR(100) | 首次经过的 Service、Mapper |
| `module_id` / `module_name` | BIGINT / VARCHAR | 归属模块 |
| `developer_id` / `developer_name` | BIGINT / VARCHAR | 归属开发者 |
| `http_status` / `success` | INT / TINYINT | HTTP 状态码、是否成功（< 400 视为成功） |
| `error_type` / `error_message` | VARCHAR | 异常类型、错误消息（截断至 2000 字符） |
| `start_time` / `end_time` / `duration_ms` | DATETIME(3) / INT | 起止时间与总耗时 |
| `created_at` | DATETIME(3) | 落库时间 |

索引：`idx_request_time`、`idx_request_user(user_id, request_time)`、`idx_request_status(http_status, request_time)`、`idx_request_module(module_id, request_time)`、`idx_request_uri(uri)`。

**② `error_log` —— 错误日志**

异常由全局处理器定位责任链后写入。`stack_trace` 为 `MEDIUMTEXT`（应用层截断至 10 万字符），是定位线上问题的关键证据。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `request_id` | VARCHAR(60) | 关联请求 |
| `api_id` / `module_id` / `developer_id` | BIGINT | 责任链定位结果 |
| `error_code` / `error_type` | INT / VARCHAR | HTTP 状态码、错误类型（`PARAMETER_ERROR`/`AUTH_ERROR`/`NOT_FOUND`/`DATABASE_ERROR`/`SYSTEM_ERROR`） |
| `error_message` | VARCHAR(2000) | 错误消息 |
| `exception_class` | VARCHAR(255) | 异常类全名 |
| `stack_trace` | MEDIUMTEXT | 完整堆栈（截断至 100k 字符） |
| `occurred_at` | DATETIME(3) | 发生时间 |

**③ `login_log` —— 登录 / 会话日志**

记录每次登录尝试与页面会话生命周期，用于统计"今日访问 / 累计访问 / 活跃时长"。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `user_id` / `username` | BIGINT / VARCHAR | 登录用户 |
| `login_time` | DATETIME(3) | 登录时间 |
| `login_ip` / `user_agent` | VARCHAR | 登录 IP、User-Agent |
| `login_status` | TINYINT | 1 成功 / 0 失败 |
| `fail_reason` | VARCHAR(255) | 失败原因 |
| `last_active_time` / `logout_time` | DATETIME(3) | 最后活跃 / 退出时间 |
| `active_seconds` | INT | 会话活跃时长（秒） |
| `session_status` | TINYINT | 1 活跃 / 0 关闭 |

**④ `user_activity` —— 用户活跃度（日维度聚合）**

每用户每天一行，`(user_id, stat_date)` 唯一，请求后累加更新。

| 字段 | 说明 |
| --- | --- |
| `stat_date` | 统计日期（日维度） |
| `first_login_time` / `last_login_time` / `last_active_time` | 当日首/末登录、最后活跃 |
| `request_count_today` / `request_count_total` | 今日 / 累计请求数 |
| `active_seconds_today` / `active_seconds_total` | 今日 / 累计活跃时长 |
| `activity_score` | 活跃度评分 |
| `online_status` | 是否在线 |

**⑤ `api_statistics` / `module_statistics` —— 日维度统计**

分别以 `(stat_date, api_id)`、`(stat_date, module_id)` 为唯一键，记录每日请求数、成功数、错误数、总耗时、最大耗时，供监控台绘制趋势图。

### 2. 存储时长（保留多久）

| 载体 | 保留时长 | 说明 |
| --- | --- | --- |
| **MySQL 日志表** | **永久保留** | 无任何自动清理 / 归档机制。虽已 `@EnableScheduling`（`LemonGoApplication.java:11`），但代码中**没有任何 `@Scheduled` 定时任务**做日志过期或删除，MySQL 日志只增不减，除非人工清理 |
| **Redis 实时计数** | **2 天过期** | 所有实时 key 统一调用 `redis.expire(key, Duration.ofDays(2))`（`RedisObservationService.java:42-46、112`） |
| **Logback 文件** | **15 天** | `logback-spring.xml` 中 `maxHistory=15`，超过 15 天的滚动文件自动删除 |

### 3. 存储容量（上限多大）

| 载体 | 容量上限 | 说明 |
| --- | --- | --- |
| **MySQL** | **无上限** | 日志表未做分区、归档或大小约束，容量只受宿主机磁盘与 InnoDB 表空间限制，随流量无限增长。`stack_trace` 单条截断至 100k 字符，`error_message` 截断至 2000 字符，`param_summary` 上限 2000 字符 |
| **Redis** | **无显式上限** | 未配置 `maxmemory` 淘汰策略，仅靠 2 天 TTL 自动过期；value 都是轻量计数器，内存占用很小 |
| **Logback 文件** | **总容量 2GB** | 单文件 `maxFileSize=50MB`，总容量 `totalSizeCap=2GB`，滚动文件 gzip 压缩（`logback-spring.xml:17-19`） |

### 4. 日志生命周期流转

```text
一个请求的生命周期：
   开始 ──▶ TraceFilter 生成 request_id ──▶ AOP 采集分层耗时
                                            │
                                            ├──▶ 异常？──▶ 写 error_log
                                            │
   结束 ──▶ TraceCompletionService ──▶ 写 request_log
                                      ├──▶ upsert user_activity（日维度）
                                      ├──▶ upsert api_statistics / module_statistics（日维度）
                                      ├──▶ 写 Redis 实时计数（2 天过期）
                                      └──▶ WebSocket 广播 REQUEST_COMPLETED

一个登录会话的生命周期：
   登录 ──▶ 写 login_log（login_status=1, session_status=1）
   心跳 ──▶ 更新 last_active_time / active_seconds
   退出 ──▶ 更新 logout_time / session_status=0
```

### 5. 风险提示与建议

当前 MySQL 日志为**永久保留、无容量上限**，长期运行后 `request_log`、`error_log` 会持续膨胀，是生产化前必须处理的一个点。可选方向：

- 为 `request_log` / `error_log` 增加**分区表**或**定时归档任务**（按月归档、按天删除 N 天前数据）；
- 对 `stack_trace` 大字段做**冷热分离**（热数据保留近期，老数据转冷存储）；
- 启用 `operation_log`，把"谁在管理端改了什么"也纳入审计闭环。

---

## 常用命令

```bash
# 后端测试（当前含 Request ID 单元测试）
cd backend
./mvnw test

# Windows PowerShell
cd backend
.\mvnw.cmd test

# 前端类型检查与构建
cd frontend
npm run build
```

---

## 当前进度

已完成的演示闭环：

- **M0 架构初始化**：Monorepo、Spring Boot 工程基线、统一响应 / Request ID / Trace Filter / 全局异常、数据库 DDL、Vue 3 工程基线。
- **M1 请求链路与监控底座**：API 责任注册表、AOP 分层采集、请求与异常落库、Redis 实时计数、用户活跃度与请求日志查询。
- **M2 最小电商业务闭环**：JWT 登录注册、用户 / 商品 / 购物车 / 订单模块、五类模拟异常接口。
- **M3 监控台与管理端前端**：Dashboard、请求日志与链路详情、接口注册表、异常日志、用户活跃度、模块与开发者责任监控，以及管理端责任关系维护。
- **M4 部署、测试与验收（进行中）**：Docker Compose 全栈、初始化演示数据、自动化测试、端到端验收。

详细里程碑见 [docs/02-roadmap.md](docs/02-roadmap.md)。

---

## 常见问题

**端口被占用怎么办？**

MySQL / Redis 的宿主机端口改 `.env` 里的 `MYSQL_PORT`、`REDIS_PORT`；后端端口用 `SERVER_PORT`；前端端口在 `frontend/vite.config.ts` 的 `server.port`。

**改了 `database/init/` 里的 SQL，为什么没有生效？**

初始化脚本只在 MySQL 数据卷首次创建时执行。需要重新初始化时执行 `docker compose down -v` 后再 `docker compose up -d`，注意这会清空本地 MySQL 与 Redis 数据。

**后端启动报 `Access denied for user`？**

Compose 内建的应用账号是 `lemongo / lemongo123`，而后端默认配置是 `root / 123456`。二选一：要么按"启动后端"一节的命令传入 `DB_USERNAME` / `DB_PASSWORD`，要么让本机 MySQL 使用默认的 root 账号。

**监控台"实时链路"一直连不上？**

WebSocket 握手要求 `MONITOR` 角色，请用 `monitor` 账号登录；同时确认后端已启动、浏览器地址与 Vite 代理配置一致。握手失败时后端会返回 401（缺少或无效 Token）或 403（角色不符）。

**用 `SPRING_PROFILES_ACTIVE=prod` 启动失败？**

`application-prod.yml` 不给数据库配置兜底值，必须显式提供 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`，并自行指定 `LEMONGO_JWT_SECRET`。

---

## License

[MIT](LICENSE) © 2026 AbsoluteZero
