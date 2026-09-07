# LemonGo 乐檬购

LemonGo 是一个**以请求链路追踪与模块责任监控为核心**的模拟电商业务系统。它不追求做成大型商城，而是让每一次用户请求都能回答四个问题：

1. 谁在什么时间访问了什么 API？
2. 请求经过 Controller、Service、Mapper 到了 MySQL/Redis，花了多久？
3. 请求是否成功？失败发生在哪个接口、哪个模块？
4. 该业务模块由哪位开发者负责？

## 工程形态

```text
LemonGo
├── backend/                 Spring Boot 3.x 单体后端
├── frontend/                Vue 3 + Vite + TypeScript 前端
├── database/init/           数据库初始化 SQL
├── deploy/                  本地与部署辅助配置
├── docs/                    架构与里程碑文档
└── docker-compose.yml       MySQL + Redis 本地基础环境
```

## 快速启动

1. 启动基础设施：

   ```bash
   docker compose up -d mysql redis
   ```

2. 启动后端：

   ```bash
   cd backend
   # Windows PowerShell: $env:JAVA_HOME = "D:\Java\JDK-21"
   ./mvnw spring-boot:run
   ```

   Windows 使用 `mvnw.cmd spring-boot:run`。

3. 启动前端：

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

4. 本地入口：

   - 后端健康检查：http://localhost:8080/api/health
   - Swagger UI：http://localhost:8080/swagger-ui.html
   - 前端开发服务：http://localhost:5173

## 当前进度

已完成可运行的 **M1-M3 演示闭环**：

- 用户端：登录、商品列表/详情、购物车、创建订单、模拟支付
- 链路底座：TraceFilter + AOP 采集 Controller/Service/Mapper，Request ID 贯穿响应与日志
- 请求日志：每次请求写入 MySQL，Dashboard 展示近 7 日访问/异常趋势
- 活跃度：Redis 记录在线与计数，MySQL 持久化日维度访问量、活跃时间、活跃度
- 异常定位：400/404/500/数据库/Service 错误自动返回 Request ID、模块、Controller/Service/Mapper 与负责人
- 监控台：请求日志详情链路、用户活跃、模块负责人、开发者责任视图

演示账号：

- 张三：`zhangsan / zhangsan-123456`
- 李四：`lisi / lisi-123456`

架构说明见 [docs/01-architecture.md](docs/01-architecture.md)。
下一阶段见 [docs/02-roadmap.md](docs/02-roadmap.md)。
