# LemonGo 里程碑

## M0：架构初始化（已完成）

- [x] Monorepo 目录、README、基础 Docker Compose
- [x] 后端 Maven Wrapper + Spring Boot 3.x 工程基线
- [x] 统一响应、Request ID、Trace Filter、RequestContext、全局异常处理
- [x] MyBatis-Plus、Redis、SpringDoc、Actuator 等依赖与配置就位
- [x] 数据库第一版 DDL / 初始化数据
- [x] Vue3 前端工程基线

## M1：请求链路与监控底座（已完成）

- [x] API 注册表初始化与热加载
- [x] Controller/Service 链路 AOP 采集
- [x] request_log / error_log 落库
- [x] Redis 实时计数与用户活跃度
- [x] 请求日志分页查询、请求详情

## M2：最小电商业务闭环（已完成）

- [x] JWT 登录、用户模块
- [x] 商品模块（真实 MySQL CRUD）
- [x] 购物车模块
- [x] 订单模块
- [x] 模拟异常 API（400/404/500/database/service）

## M3：监控台前端（已完成）

- [x] Dashboard 指标与图表
- [x] 请求日志与链路详情
- [x] 用户活跃度
- [x] 模块负责人监控
- [x] 异常定位错误页

## M4：部署、测试与验收

- [ ] 后端容器化与 Docker Compose 全栈
- [ ] 初始化演示数据
- [ ] 自动化测试（链路、统计、异常定位）
- [ ] 六类核心场景端到端验收
