# AgentTrainHub · Agent+ 算法训练与实验管理平台

面向校内导师与学生的 AI/算法训练平台：支持数据集上传、训练任务参数配置、任务进度可视化、
模型产物下载，并引入 Agent 助手把自然语言需求转换为训练参数、辅助诊断训练日志。

> 完整需求与技术设计见仓库根目录的 [`agent-training-platform-docs/`](../agent-training-platform-docs)
> （01 需求说明书 / 02 技术实现文档 / 03 Claude Code 执行清单）。本目录是据此落地的工程实现。

当前进度：**第一阶段（骨架）已完成；第二阶段进行中 —— 登录认证 + 数据集模块已落地**。
后端可编译、前端可构建；登录→上传数据集→列表/下载/删除链路已打通。
其余业务（训练任务状态机、SSE、Agent）将按阶段填充。

---

## 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.3、Spring Security、MyBatis-Plus 3.5、MySQL 8、Redis 7、JWT(jjwt)、Maven |
| 前端 | Vue 3、Vite 6、TypeScript、Vue Router、Pinia、Axios、Element Plus、ECharts |
| Worker | Python 3（数据集分析 / 训练模拟，第一阶段为标准库 mock） |
| 部署 | Docker Compose（MySQL + Redis），本地开发可直接用 Maven / npm |

---

## 目录结构

```text
agent-train-hub/
  backend/          Spring Boot 后端（Maven，含 mvnw 包装器）
    src/main/java/com/agenttrainhub/
      common/       统一响应 Result、分页、错误码、业务异常、全局异常处理、健康检查
      config/       MyBatis-Plus、Redis、CORS 配置
      security/     Spring Security 配置、JWT、用户上下文（占位，第二阶段接入登录）
      auth/ user/ dataset/ template/ job/ artifact/ agent/ storage/ sse/ audit/
                    各业务模块包（当前为 package-info 占位）
    src/main/resources/application.yml
  frontend/         Vue 3 + Vite + TS 前端
    src/api/        Axios 封装与各接口模块
    src/router/     路由与登录守卫
    src/stores/     Pinia 状态（user）
    src/views/      Login / Dashboard / DatasetList / JobList / JobCreate / JobDetail / UserList / Layout
    src/components/  公共组件（StatusTag）
    src/utils/      格式化工具
  worker/           dataset_profile.py、train_simulator.py、requirements.txt
  deploy/           docker-compose.yml、mysql/init.sql
  docs/             api.md、runbook.md
```

---

## 本地启动

### 0. 前置环境

- JDK 17、Maven 3.6.3+（或直接用 `backend/mvnw`，无需全局 Maven）
- Node.js 18+（建议 20/22）与 npm
- Docker（用于一键起 MySQL/Redis；没有 Docker 也可使用本机已装的 MySQL/Redis）

### 1. 启动依赖（MySQL + Redis）

```bash
cd deploy
docker compose up -d
```

- MySQL：`localhost:3306`，库 `agent_train_hub`，应用账号 `ath / ath123456`，root 密码 `root123456`
- Redis：`localhost:6379`
- 首次启动会自动执行 `deploy/mysql/init.sql`：建表 + 写入 3 个默认账号与 3 个模型模板。

### 2. 启动后端

```bash
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
# 或：mvn spring-boot:run
```

后端默认端口 `8080`。健康检查：`GET http://localhost:8080/api/ping`。

> 关键配置可用环境变量覆盖：`MYSQL_URL` / `MYSQL_USER` / `MYSQL_PASSWORD` /
> `REDIS_HOST` / `REDIS_PORT` / `JWT_SECRET` / `CORS_ALLOWED_ORIGINS` / `AGENT_MODE`。
> `CORS_ALLOWED_ORIGINS` 使用逗号分隔的完整 origin；默认只允许 `http://localhost:5173`。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认端口 `5173`，开发期已配置把 `/api` 代理到后端 `8080`，无需关心跨域。

---

## 默认账号

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| admin | 123456 | ADMIN |
| teacher | 123456 | TEACHER |
| student | 123456 | STUDENT |

密码以 BCrypt 存储于 `users` 表（见 `deploy/mysql/init.sql`）。

> 第二阶段已实现真实登录：前端调用 `POST /api/auth/login` 获取 JWT，之后所有请求由 Axios
> 自动携带 `Authorization: Bearer <token>`，后端按角色/ownerId 鉴权。

---

## Agent 模式

由 `application.yml` 的 `agenttrainhub.agent.mode` 控制（环境变量 `AGENT_MODE`）：

- `mock`（默认）：无需大模型 API Key，用规则 / 正则返回可演示的结构化结果。
- `spring-ai`：配置模型 API Key（`AGENT_API_KEY`）后调用 Spring AI。

第一阶段仅占位开关与配置，Agent 接口将在第二阶段实现。

---

## 功能截图

待补充（第二阶段完成可演示链路后补充登录、仪表盘、任务进度等截图）。

---

## 第一阶段已完成内容

- [x] 全套目录骨架：backend / frontend / worker / deploy / docs。
- [x] 后端：Spring Boot 启动类、统一响应 `Result<T>` / `PageResult<T>`、错误码 `ErrorCode`、
      业务异常 `BizException`、全局异常处理 `GlobalExceptionHandler`、健康检查 `GET /api/ping`。
- [x] 后端配置：MyBatis-Plus（分页插件）、Redis（JSON 序列化）、CORS、Spring Security（无状态 + 占位放行）、
      JWT（`JwtTokenProvider` / `JwtAuthenticationFilter` / `JwtProperties`）、`application.yml`。
- [x] 后端 `mvn compile` 通过（26 个源文件），含一个不依赖数据库的单元测试。
- [x] 前端：Vue3 + Vite + TS 工程，路由 / Pinia / Axios 封装 / Element Plus / ECharts；
      7 个页面 + Layout + StatusTag 组件骨架；`npm run build` 通过（含 `vue-tsc` 类型检查）。
- [x] Worker：`dataset_profile.py`、`train_simulator.py` 可运行的 mock 版（仅标准库）+ `requirements.txt`。
- [x] 部署：`docker-compose.yml`（MySQL + Redis）、`mysql/init.sql`（9 张核心表草稿 + 默认账号/模板）。

## 第二阶段进展（进行中）

已完成 **登录认证 + 数据集模块**：

- [x] 登录认证：`POST /api/auth/login`（BCrypt 校验、签发 JWT）、`GET /api/auth/me`；
      `SecurityConfig` 收紧为「白名单放行、其余需认证」，401/403 统一返回 JSON。
- [x] 存储抽象：`StorageService` / `LocalStorageService`（落本地、流式计算 SHA-256）。
- [x] 数据集模块：上传 / 分页列表 / 详情 / 下载 / 删除 / 分析（内置 Java 分析器），
      Service 层按 `ownerId` 做数据权限（ADMIN 全部、其余仅自己）。
- [x] 前端联调：真实登录、Axios 自动带 token、数据集列表/上传/下载/删除/分页、按角色显示菜单。
- [x] `mvn compile`/`test` 与 `npm run build` 均通过。

> 说明：本机未装 Docker、MySQL root 口令未知，故未做线上端到端联调；提供 Docker 或 DB 凭据后
> 可直接 `docker compose up -d` + 启动后端联调（`init.sql` 已自带建库建账号）。

## 下一阶段计划

模型模板模块 → 训练任务状态机 + 模拟执行器（线程池）+ 指标/日志落库 →
SSE 实时进度 → 模型产物下载 → Agent 参数生成与日志诊断（mock 优先）。
详见 [`docs/runbook.md`](docs/runbook.md)。

---

## 相关文档

- 接口约定：[`docs/api.md`](docs/api.md)
- 启动 / 排障手册：[`docs/runbook.md`](docs/runbook.md)
- 原始需求与设计：[`../agent-training-platform-docs/`](../agent-training-platform-docs)
