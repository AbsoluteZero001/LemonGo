# LemonGo 乐檬购

LemonGo 是一个**以请求链路追踪与模块责任监控为核心**的模拟电商业务系统。它不追求做成大型商城，而是让每一次用户请求都能回答四个问题：

1. 谁在什么时间访问了什么 API？
2. 请求经过 Controller、Service、Mapper 到了 MySQL/Redis，花了多久？
3. 请求是否成功？失败发生在哪个接口、哪个模块？
4. 该业务模块由哪位开发者负责？

业务层只需要用户、商品、购物车、订单四个真实闭环 + 少量支付模拟；真正的核心资产是 **API → 模块 → 开发者** 的责任映射、请求日志、用户活跃度、Redis 实时统计与异常定位。

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

## 功能特性

- **链路底座**：`TraceFilter` 生成/透传 `Request ID`，`ChainTraceAspect` 通过 AOP 采集 Controller / Service / Mapper 的方法名、所属模块与耗时，Request ID 贯穿响应体、日志与错误页。
- **请求日志**：每次请求异步落库 `request_log`，支持按时间 / 用户 / 状态 / 模块 / URI 检索与详情链路查看。
- **异常定位**：`GlobalExceptionHandler` + `ErrorTraceResolver` 依据当前请求的 `method + 规范化路径` 匹配 `sys_api`，沿外键定位模块与开发者，返回结构化错误（含 Request ID、模块、Controller/Service/Mapper、负责人）。
- **责任映射**：`sys_api → sys_module → sys_developer` 三级注册表，API 注册支持 `GET /api/products/{id}` 这类带路径参数的 URI 模板。
- **活跃度与统计**：Redis 记录在线状态与实时计数，定时任务将增量快照持久化到 MySQL（日维度访问量、活跃时长、活跃度、API/模块统计）。
- **监控台**：Dashboard 指标与趋势、请求日志与链路详情、接口注册表、异常日志、用户活跃度、模块责任监控、开发者责任视图。
- **三端与角色鉴权**：前端分为用户端 / 管理端 / 监控台三个入口，后端基于 JWT `role` 声明做接口级鉴权（`USER` / `ADMIN` / `MONITOR`），三端菜单互相隔离，`/api/admin/**`、`/api/monitor/**` 前缀强制对应角色。
- **模拟异常**：内置 400 / 404 / 500 / 数据库 / Service 五类异常接口，用于验证异常定位闭环。

---

## 工程结构

```text
LemonGo
├── backend/                     Spring Boot 3.x 单体后端
│   └── src/main/java/com/lemongo
│       ├── common/             统一响应、常量、请求上下文、工具
│       ├── config/             Spring/MyBatis-Plus/OpenAPI/Web/JWT 配置
│       ├── controller/         用户端业务 API + 健康检查 + 模拟异常
│       ├── service/            业务逻辑
│       ├── mapper/             MyBatis-Plus Mapper
│       ├── entity/ dto/ vo/    实体、入参、出参
│       ├── exception/          业务异常与全局异常处理
│       ├── aspect/             Controller/Service 链路采集切面
│       ├── filter/             TraceFilter 等请求过滤器
│       ├── observability/      请求生命周期、责任映射解析、Redis 统计与落库
│       └── responsibility/     API 注册表（责任映射）
├── frontend/                    Vue 3 + Vite + TypeScript 前端
│   └── src/
│       ├── views/user/         用户端：商品、购物车、订单、个人中心
│       ├── views/admin/        管理端：商品/用户/订单/开发者/模块/接口管理
│       ├── views/monitor/      监控台：Dashboard、请求日志、接口注册表、异常、活跃度、模块/开发者
│       ├── api/ components/ layouts/ router/ stores/
├── database/init/               数据库初始化 SQL（schema / 基础数据 / 演示数据）
├── docs/                        架构与里程碑文档
├── docker-compose.yml           MySQL + Redis 本地基础环境
└── .env.example                 环境变量示例
```

---

## 快速启动

### 前置要求

- JDK 21
- Node.js 20+（Vite 8 要求）
- Docker（用于启动 MySQL 与 Redis）

### 1. 准备环境变量

复制示例文件并按需修改（可选，容器与后端均可读取）：

```bash
cp .env.example .env
```

`.env` 默认值如下，与 `docker-compose.yml` 保持一致：

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

该命令会启动 MySQL（含健康检查）与 Redis，并自动执行 `database/init/` 下的初始化脚本（建库建表 + 基础/演示数据）。

### 3. 启动后端

后端默认以 `root / 123456` 连接数据库；若沿用上面 `.env` 的容器密码，需通过环境变量指定。推荐使用容器内建的用户 `lemongo`：

```bash
cd backend
# macOS / Linux
DB_USERNAME=lemongo DB_PASSWORD=lemongo123 ./mvnw spring-boot:run

# Windows PowerShell
$env:DB_USERNAME="lemongo"; $env:DB_PASSWORD="lemongo123"; .\mvnw.cmd spring-boot:run
```

> 首次运行会下载依赖，耗时较长。后端默认端口 `8080`。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端开发服务运行于 `http://localhost:5173`，`/api` 请求已代理到 `http://localhost:8080`。

### 5. 本地入口

| 入口 | 地址 |
| --- | --- |
| 后端健康检查 | http://localhost:8080/api/health |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| 前端用户端 | http://localhost:5173 |
| 管理端 | http://localhost:5173/admin |
| 监控台 | http://localhost:5173/monitor |

---

## 环境变量

### 后端（`backend/src/main/resources/application.yml`）

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | 后端端口 |
| `SPRING_PROFILES_ACTIVE` | `dev` | 激活的 Spring Profile |
| `DB_URL` | `jdbc:mysql://localhost:3306/lemongo?...` | JDBC 连接串 |
| `DB_USERNAME` | `root` | 数据库用户名 |
| `DB_PASSWORD` | `123456` | 数据库密码 |
| `REDIS_HOST` | `localhost` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | 空 | Redis 密码（默认无） |

> 注意：`application.yml` 默认库口令 `root / 123456` 与 `docker-compose.yml` 的默认 root 密码 `root123456` 不一致，请通过环境变量对齐，或直接使用容器内建用户 `lemongo / lemongo123`。

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

---

## API 概览

| 模块 | 方法 & 路径 | 说明 |
| --- | --- | --- |
| 健康检查 | `GET /api/health` | 健康检查 |
| 认证 | `POST /api/auth/login` | 登录 |
| 用户 | `GET /api/users/me` · `GET /api/users/me/activity` | 当前用户信息 / 我的活跃度 |
| 商品 | `GET /api/products` · `GET /api/products/categories` · `GET/POST /api/products` · `GET/PUT/DELETE /api/products/{id}` | 商品列表 / 分类 / 新增 / 详情 / 更新 / 删除 |
| 购物车 | `GET/POST /api/cart/items` · `PUT/DELETE /api/cart/items/{id}` | 购物车列表 / 加入 / 修改数量 / 移除 |
| 订单 | `GET/POST /api/orders` · `GET /api/orders/{id}` · `POST /api/orders/{id}/pay` | 下单 / 订单列表 / 详情 / 模拟支付 |
| 管理端 | `GET/POST /api/admin/products` · `PUT/DELETE /api/admin/products/{id}`，以及 `/api/admin/{users,orders,developers,modules,apis}` 全套 | 商品、用户、订单、责任关系全量管理（ADMIN） |
| 监控 | `GET /api/monitor/dashboard` · `/request-logs` · `/request-logs/{requestId}` · `/apis` · `/errors` · `/users/activity` · `/modules` · `/developers` | 指标 / 请求日志 / 详情 / 接口注册表 / 异常 / 活跃度 / 模块 / 开发者（MONITOR） |
| 模拟异常 | `GET /api/test/error/{400,404,500,database,service}` | 五类异常定位演示 |

完整接口定义以 Swagger UI（`/swagger-ui.html`）为准。

---

## 一次请求的完整链路

```text
Vue3 / Axios
    |
    v
TraceFilter（生成/透传 Request ID，初始化 RequestContext，写入 MDC）
    |
    v
Controller -> Service -> Mapper -> MySQL / Redis
    |           |          |
    +-- ChainTraceAspect 采集方法名、模块、耗时、异常 --+
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
定时任务将 Redis 统计快照持久化到 MySQL
```

更详细的设计（包结构、责任映射模型、Redis Key、异常响应）见 [docs/01-architecture.md](docs/01-architecture.md)。

---

## 当前进度

已完成可运行的 **M0–M3 演示闭环**：

- **M0 架构初始化**：Monorepo 目录、Maven Wrapper + Spring Boot 工程基线、统一响应 / Request ID / Trace Filter / 全局异常处理、数据库 DDL、Vue3 工程基线。
- **M1 请求链路与监控底座**：API 注册表热加载、AOP 采集、请求/错误日志落库、Redis 实时计数与用户活跃度、请求日志查询。
- **M2 最小电商业务闭环**：JWT 登录、商品 / 购物车 / 订单模块、五类模拟异常接口。
- **M3 监控台前端**：Dashboard、请求日志与链路详情、用户活跃度、模块与开发者责任监控、异常定位错误页。

**M4 部署、测试与验收（进行中）**：后端容器化与 Docker Compose 全栈、自动化测试（链路 / 统计 / 异常定位）、端到端验收。

完整里程碑见 [docs/02-roadmap.md](docs/02-roadmap.md)。

---

## License

[MIT](LICENSE) © 2026 AbsoluteZero