# LemonGo 乐檬购

LemonGo 是一个以**请求链路追踪与模块责任监控为核心**的模拟电商系统。项目刻意保持小体量，用用户、商品、购物车、订单和模拟支付组成一条真实业务闭环，把主要精力放在让每个请求都可解释、可追踪、可问责：

1. 谁在什么时间访问了什么 API？
2. 请求经过了 Controller、Service、Mapper，并落到 MySQL / Redis，耗时如何？
3. 请求成功了吗？失败发生在哪个接口、哪个模块、哪一层？
4. 该模块或接口由哪位开发者负责？

> 默认密码、种子数据和 JWT 密钥均为演示配置，适合教学、原型演示与可观测性实验，不应直接用于生产环境。

---

## 功能特性

- **Request ID 链路**：`TraceFilter` 透传或生成 `X-Request-Id`，贯穿请求响应头、统一响应体、MDC 日志和数据库日志；浏览器可直接用同一个 ID 追踪异常。
- **分层耗时采集**：`ChainTraceAspect` 通过 AOP 记录每次请求首次经过的 Controller、Service、Mapper 方法及耗时，作为请求详情的链路步骤。
- **责任注册表**：以 `sys_api`、`sys_module`、`sys_developer` 三张表维护“接口 -> 模块 -> 开发者”的归属关系，支持 `/api/products/{id}` 这类带路径参数的 URI 模板。
- **注册表热加载**：接口责任关系在应用启动时加载，管理端完成 API CRUD 后自动刷新；也保留 `POST /api/admin/apis/refresh` 手动刷新入口。
- **请求日志**：每次请求结束时统一写入 `request_log`，记录用户、IP、URI、方法、状态码、耗时、Controller/Service/Mapper、模块与负责人，支持按 Request ID / 用户 / 状态 / URI 检索。
- **异常定位**：异常由全局处理器统一处理，按当前请求的 method + 规范化路径定位 API、模块与负责人并写入 `error_log`；响应只暴露通用信息与 Request ID，堆栈详情仅在监控台查询。
- **实时统计**：Redis 记录 5 分钟在线窗口、用户请求计数、API / 模块请求与错误计数，Redis 故障不会拖垮正常业务请求。
- **历史统计**：请求结束后同步 upsert MySQL 日维度 `user_activity`、`api_statistics`、`module_statistics`，供监控台查询趋势和累计值。
- **最小电商闭环**：登录注册、商品浏览与维护、购物车、下单与模拟支付、个人资料编辑，构成可真实操作、可产生观测数据的业务系统。
- **三端角色入口**：前端划分为用户端、管理端、监控台；后端在 `AuthInterceptor` 中解析 JWT `role` 声明并按接口路径前缀校验角色，`/api/admin/**` 强制 `ADMIN`，`/api/monitor/**` 强制 `MONITOR`。
- **内置模拟异常**：提供 400、404、500、数据库异常、Service 异常五类演示接口，用来验证异常落库与责任定位闭环。

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
| 缓存 | Redis | 7.4 |

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
  - 更新 user_activity
  - upsert api_statistics / module_statistics
  - 更新 Redis 在线窗口与计数器
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
│       ├── aspect/             分层链路采集切面
│       ├── filter/             TraceFilter
│       ├── observability/      请求完成落库、错误定位、Redis 实时统计与日维度统计
│       └── responsibility/     API 责任注册表
├── frontend/                    Vue 3 + Vite + TypeScript 前端
│   └── src/
│       ├── views/user/         商品、购物车、订单、个人中心
│       ├── views/admin/        商品、用户、订单、开发者、模块、接口管理
│       ├── views/monitor/      Dashboard、请求日志、接口注册表、异常、活跃度
│       ├── api/                接口请求封装
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

该命令启动 MySQL 与 Redis。首次创建 MySQL 数据卷时，会自动执行 `database/init/` 下的 `01-schema.sql`、`02-data.sql`、`03-demo-data.sql` 完成建库、建表、基础数据与演示数据初始化。

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

前端开发服务器运行在 `http://localhost:5173`，`/api` 请求已代理到 `http://localhost:8080`。

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
| `LEMONGO_JWT_SECRET` | 演示默认值 | JWT 签名密钥，脱离本地演示环境前必须修改 |

`application-prod.yml` 使用 `${DB_URL}`、`${DB_USERNAME}`、`${DB_PASSWORD}` 且不提供兜底默认值，因此以 `SPRING_PROFILES_ACTIVE=prod` 运行时必须显式提供数据库环境变量，并自行指定 `LEMONGO_JWT_SECRET`。

---

## API 概览

| 模块 | 方法与路径 | 说明 |
| --- | --- | --- |
| 健康检查 | `GET /api/health` | 服务健康检查 |
| 认证 | `POST /api/auth/login` · `POST /api/auth/register` | 登录 / 注册 |
| 用户 | `GET /api/users/me` · `PUT /api/users/me` · `GET /api/users/me/activity` | 当前用户、编辑资料、我的活跃度 |
| 商品 | `GET /api/products` · `GET /api/products/categories` · `GET /api/products/{id}` | 商品列表、分类、详情 |
| 购物车 | `GET/POST /api/cart/items` · `PUT/DELETE /api/cart/items/{id}` | 列表、加入、改数量、移除 |
| 订单 | `POST /api/orders` · `GET /api/orders` · `GET /api/orders/{id}` · `POST /api/orders/{id}/pay` | 下单、列表、详情、模拟支付 |
| 管理端 | `/api/admin/products`、`/api/admin/users`、`/api/admin/orders`、`/api/admin/developers`、`/api/admin/modules`、`/api/admin/apis` 全套 CRUD | 商品、用户、订单、责任关系管理（ADMIN） |
| 监控台 | `/api/monitor/dashboard`、`/request-logs`、`/request-logs/{requestId}`、`/apis`、`/errors`、`/users/activity`、`/users/usage`、`/modules`、`/developers` | 指标、请求详情、接口注册表、异常、活跃度与责任监控（MONITOR） |
| 模拟异常 | `GET /api/test/error/{400,404,500,database,service}` | 五类异常演示 |

完整接口定义以 Swagger UI（`/swagger-ui.html`）为准。

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

## License

[MIT](LICENSE) © 2026 AbsoluteZero
