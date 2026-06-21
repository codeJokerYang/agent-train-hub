# AgentTrainHub 项目测试报告

测试日期：2026-06-21  
测试人员：Codex  
项目路径：`F:\4C\PPTex\agent-train-hub`

## 1. 测试结论

本轮测试结论为：**构建与基础运行通过，数据库端到端联调未执行**。

已通过：

- 后端 Maven 编译通过。
- 后端 JUnit 单元测试通过。
- 前端 TypeScript 类型检查与 Vite 生产构建通过。
- 后端 Spring Boot 应用可启动，`/api/ping` 冒烟接口返回正常。
- 安全白名单与受保护接口基础行为正常：`/api/ping` 放行，未登录访问 `/api/datasets` 返回 401 JSON。
- Python 数据集画像脚本通过冒烟测试。
- Python 训练模拟器通过无 BOM JSON 配置下的冒烟测试。

未执行：

- 登录、数据集上传、任务创建、训练任务启动、产物下载等真实接口端到端联调。原因是当前机器 Docker 未安装，Redis 未启动，本地 MySQL 虽然 3306 端口开放，但项目默认账号 `ath/ath123456` 未初始化，无法连接项目数据库。

本轮发现并已修复 1 个阻断启动的问题：

- `AsyncConfig` 中线程池 Bean 名称 `trainingExecutor` 与业务组件 `TrainingExecutor` 默认 Bean 名冲突，导致 Spring Boot 启动失败。已改为 `trainingTaskExecutor`，并同步更新 `@Qualifier`。

## 2. 测试环境

| 项目 | 版本 / 状态 |
| --- | --- |
| 操作系统 | Windows 11 amd64 |
| Java | `17.0.15`，命令行 `java -version` |
| Maven Wrapper | Apache Maven `3.9.9` |
| Spring Boot | `3.3.5` |
| Node.js | `v24.14.0` |
| npm | `11.9.0` |
| Vue / Vite | Vue 3 + Vite 6 |
| Python | `3.12.13` |
| MySQL | 本地 3306 端口开放，默认应用账号不可用 |
| Redis | 6379 端口未开放 |
| Docker | 未安装或命令不可用 |

## 3. 测试范围

本轮覆盖当前项目已实现代码：

- 后端公共响应、异常结构、健康检查。
- 登录认证与 JWT 相关基础配置。
- 数据集、模型模板、训练任务、训练日志、训练指标、模型产物模块的编译完整性。
- 前端登录页、仪表盘、数据集页、任务创建页、任务列表页、任务详情页、用户页的类型检查与打包。
- Worker 脚本：
  - `worker/dataset_profile.py`
  - `worker/train_simulator.py`

不在本轮范围：

- 真实浏览器交互测试。
- MySQL 真实数据写入测试。
- Redis cancel flag 或缓存测试。
- Agent 模块测试，当前 Agent 模块仍是后续阶段内容。
- 压力测试、并发测试、安全扫描。

## 4. 执行命令与结果

### 4.1 后端单元测试

命令：

```powershell
cd F:\4C\PPTex\agent-train-hub\backend
.\mvnw.cmd test
```

结果：

```text
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

说明：

- 当前自动化测试类为 `CommonResultTest`。
- 测试覆盖较浅，主要验证统一响应结构；登录、数据集、任务状态机等核心服务还缺少单元测试。

### 4.2 后端编译

命令：

```powershell
cd F:\4C\PPTex\agent-train-hub\backend
.\mvnw.cmd compile
```

结果：

```text
Compiling 77 source files
BUILD SUCCESS
```

说明：

- 后端 77 个 Java 源文件编译通过。
- MyBatis-Plus、Spring Security、JWT、Redis、MySQL Driver 等依赖解析正常。

### 4.3 前端构建

命令：

```powershell
cd F:\4C\PPTex\agent-train-hub\frontend
npm.cmd run build
```

结果：

```text
vue-tsc -b && vite build
2246 modules transformed
built in 12.47s
```

构建结论：

- TypeScript 类型检查通过。
- Vite 生产构建通过。

构建警告：

- `@vueuse/core` 中部分 `/* #__PURE__ */` 注释位置 Rollup 无法解释，会被移除。
- 部分 chunk 超过 500 kB，主要来自 Element Plus / ECharts 等依赖，建议后续做按需加载或手动分包。

该警告不影响当前功能运行。

### 4.4 Docker / 中间件检查

命令：

```powershell
docker --version
Test-NetConnection -ComputerName localhost -Port 3306
Test-NetConnection -ComputerName localhost -Port 6379
mysql -h 127.0.0.1 -P 3306 -u ath -path123456 -e "SELECT DATABASE();"
redis-cli --version
```

结果：

| 检查项 | 结果 |
| --- | --- |
| Docker | 命令不可用 |
| MySQL 3306 | TCP 连接成功 |
| MySQL 应用账号 | `Access denied for user 'ath'@'localhost'` |
| Redis 6379 | TCP 连接失败 |
| redis-cli | 命令不可用 |

结论：

- 本机尚未具备完整端到端联调环境。
- 需要安装 Docker 并执行 `cd deploy && docker compose up -d`，或手动用 root 导入 `deploy/mysql/init.sql` 创建数据库与 `ath` 账号。

### 4.5 后端启动与健康检查

首次启动结果：

```text
APPLICATION FAILED TO START
The bean 'trainingExecutor' ... could not be registered.
A bean with that name has already been defined ... TrainingExecutor.class
```

原因：

- `AsyncConfig` 中 `@Bean("trainingExecutor")` 与 `@Component TrainingExecutor` 默认 Bean 名相同。

修复：

- 将线程池 Bean 改名为 `trainingTaskExecutor`。
- 将 `TrainingExecutor` 构造函数中的 `@Qualifier("trainingExecutor")` 改为 `@Qualifier("trainingTaskExecutor")`。

修复后启动冒烟：

```powershell
cd F:\4C\PPTex\agent-train-hub\backend
.\mvnw.cmd spring-boot:run
Invoke-RestMethod http://localhost:8080/api/ping
```

结果：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "app": "agent-train-hub",
    "status": "UP",
    "phase": "phase-1-skeleton"
  }
}
```

结论：

- 修复后 Spring Boot 可以正常启动。
- 健康检查接口可用。

### 4.6 安全层冒烟测试

测试目标：

- `/api/ping` 应放行。
- 未登录访问受保护接口 `/api/datasets` 应返回 401。

结果：

```json
{
  "Started": true,
  "Ping": {
    "code": 0,
    "message": "ok",
    "data": {
      "app": "agent-train-hub",
      "status": "UP"
    }
  },
  "DatasetsNoTokenStatus": 401,
  "DatasetsNoTokenBody": "{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}"
}
```

结论：

- 白名单接口正常。
- 受保护接口未登录访问被拦截。
- 401 响应体保持统一 JSON 格式。

注意：

- 启动日志中出现 Spring Security 默认开发密码提示，说明当前没有自定义 `UserDetailsService` Bean。由于项目通过自定义 JWT Filter 和 AuthService 处理认证，该提示不一定阻断业务；但建议后续显式关闭 HTTP Basic，或补齐认证管理配置，避免运行日志和生产配置混淆。

### 4.7 Worker：数据集画像脚本

测试方法：

- 在临时目录创建 zip 数据集：
  - `train/scratch/img001.jpg`
  - `train/missing_hole/img002.jpg`
- 执行：

```powershell
python worker/dataset_profile.py --input demo-dataset.zip --output profile.json
```

结果：

```json
{
  "fileCount": 2,
  "totalSize": 312,
  "detectedType": "IMAGE",
  "classCount": 2,
  "classes": [
    "missing_hole",
    "scratch"
  ],
  "warnings": []
}
```

结论：

- zip 文件读取成功。
- 图片类型识别成功。
- 类别推断成功。
- 输出 JSON 可用于后端保存 `profile_json`。

### 4.8 Worker：训练模拟器

测试方法：

- 准备配置：

```json
{
  "params": {
    "epochs": 3,
    "batchSize": 8,
    "learningRate": 0.001
  }
}
```

- 执行：

```powershell
python worker/train_simulator.py --job-id 1003 --config config.json --event-file events.jsonl --interval 0 --seed 7
```

结果摘要：

```text
events.jsonl 共 13 行
包含事件：job_status、log、metric、artifact、done
最后状态：SUCCESS
进度最终为 100
```

末尾事件：

```json
{"type": "metric", "jobId": "1003", "epoch": 3, "payload": {"epoch": 3, "loss": 0.6398, "accuracy": 0.3548}}
{"type": "log", "jobId": "1003", "payload": {"level": "INFO", "message": "epoch 3/3 - loss=0.6398 acc=0.3548"}}
{"type": "job_status", "jobId": "1003", "payload": {"status": "RUNNING", "progress": 100}}
{"type": "artifact", "jobId": "1003", "payload": {"fileName": "model_job1003.txt", "artifactType": "MODEL"}}
{"type": "done", "jobId": "1003", "payload": {"status": "SUCCESS"}}
```

结论：

- 训练模拟器在标准无 BOM JSON 配置下可正常读取 epoch、batchSize、learningRate。
- 能按 epoch 生成日志、指标、进度、产物和完成事件。

发现的问题：

- 如果配置文件是 PowerShell `Set-Content -Encoding UTF8` 生成的带 BOM JSON，`train_simulator.py` 使用 `encoding="utf-8"` 会解析失败，并静默回退到默认 20 epoch。
- 建议将读取编码改为 `utf-8-sig`，并在配置解析失败时输出 warning 事件或 stderr 日志，避免训练参数被悄悄忽略。

## 5. 测试通过项汇总

| 编号 | 测试项 | 结果 |
| --- | --- | --- |
| T-001 | 后端 `mvnw test` | 通过 |
| T-002 | 后端 `mvnw compile` | 通过 |
| T-003 | 前端 `npm run build` | 通过 |
| T-004 | Spring Boot 启动 | 通过，已修复 Bean 冲突后通过 |
| T-005 | `/api/ping` 健康检查 | 通过 |
| T-006 | 未登录访问 `/api/datasets` | 通过，返回 401 JSON |
| T-007 | 数据集画像脚本 | 通过 |
| T-008 | 训练模拟器无 BOM 配置 | 通过 |
| T-009 | Docker 环境检查 | 未通过，Docker 未安装 |
| T-010 | MySQL 应用账号连接 | 未通过，`ath` 账号未初始化或密码不匹配 |
| T-011 | Redis 环境检查 | 未通过，6379 未开放 |
| T-012 | 真实登录接口联调 | 未执行，数据库不可用 |
| T-013 | 数据集上传/下载联调 | 未执行，数据库不可用 |
| T-014 | 训练任务端到端联调 | 未执行，数据库不可用 |

## 6. 缺陷与风险

### 6.1 已修复：Spring Bean 名称冲突

严重级别：高  
状态：已修复  
影响：应用无法启动。

原因：

- 线程池 Bean 名为 `trainingExecutor`。
- 业务组件类 `TrainingExecutor` 默认 Bean 名也是 `trainingExecutor`。
- Spring Boot 禁止覆盖 Bean，启动失败。

修复文件：

- `backend/src/main/java/com/agenttrainhub/config/AsyncConfig.java`
- `backend/src/main/java/com/agenttrainhub/job/TrainingExecutor.java`

修复方式：

- 线程池 Bean 改为 `trainingTaskExecutor`。
- `@Qualifier` 同步改为 `trainingTaskExecutor`。

### 6.2 待修复：训练模拟器对 BOM JSON 不兼容

严重级别：中  
状态：未修复  
影响：某些 Windows 工具写出的 UTF-8 BOM JSON 会导致配置解析失败，训练脚本静默使用默认参数。

建议：

- 将 `open(path, "r", encoding="utf-8")` 改为 `encoding="utf-8-sig"`。
- 捕获 `JSONDecodeError` 时输出 warning，不要完全静默。

### 6.3 待优化：前端 chunk 体积较大

严重级别：低  
状态：未修复  
影响：首屏加载包体积偏大，但不影响当前功能。

建议：

- Element Plus 按需导入。
- ECharts 按需导入。
- 使用 `manualChunks` 拆分 vendor。

### 6.4 待关注：Spring Security 默认密码提示

严重级别：低到中  
状态：未修复  
影响：运行日志出现默认开发密码提示，容易造成配置误解。

建议：

- 显式禁用 HTTP Basic：

```java
.httpBasic(AbstractHttpConfigurer::disable)
.formLogin(AbstractHttpConfigurer::disable)
```

- 或补充自定义 `UserDetailsService` / `AuthenticationProvider`。

### 6.5 测试覆盖不足

严重级别：中  
状态：未修复  
影响：当前 `mvn test` 只有 3 个公共响应测试，无法覆盖登录、权限、数据集、训练任务状态机等核心业务。

建议：

- 增加 `AuthServiceTest`。
- 增加 `DatasetServiceTest`。
- 增加 `TrainingJobServiceTest`。
- 增加 `ArtifactServiceTest`。
- 使用 MockMvc 覆盖主要接口。

## 7. 未执行端到端测试的原因

端到端测试需要：

- 可用 MySQL 数据库。
- 已导入 `deploy/mysql/init.sql`。
- 可用 Redis，或确保当前代码路径不强依赖 Redis。
- 后端服务运行。
- 前端服务运行。

当前机器状态：

- Docker 不可用，无法通过 `docker compose up -d` 拉起 MySQL/Redis。
- Redis 6379 端口未开放。
- MySQL 3306 端口开放，但 `ath/ath123456` 无法登录。

因此无法真实执行：

- `admin / teacher / student` 登录。
- 数据集上传与下载。
- 模型模板读取。
- 训练任务创建、启动、停止、重跑。
- 日志、指标、产物写库与查询。

## 8. 建议下一轮测试计划

### 8.1 准备数据库环境

方式一：安装 Docker 后运行：

```powershell
cd F:\4C\PPTex\agent-train-hub\deploy
docker compose up -d
```

方式二：使用本地 MySQL root 账号导入：

```powershell
mysql -uroot -p < F:\4C\PPTex\agent-train-hub\deploy\mysql\init.sql
```

导入后验证：

```powershell
mysql -h 127.0.0.1 -P 3306 -u ath -path123456 -e "USE agent_train_hub; SHOW TABLES;"
```

### 8.2 执行真实接口冒烟

建议顺序：

1. `POST /api/auth/login` 使用 `student / 123456` 登录。
2. `GET /api/auth/me` 验证 token。
3. `POST /api/datasets` 上传 zip。
4. `GET /api/datasets` 查询列表。
5. `GET /api/model-templates` 查询模板。
6. `POST /api/training-jobs` 创建任务。
7. `POST /api/training-jobs/{id}/start` 启动任务。
8. 轮询 `GET /api/training-jobs/{id}`，等待进度变化。
9. 查询 `logs / metrics / artifacts`。
10. 下载模型产物。

### 8.3 补齐自动化测试

建议优先级：

1. 后端服务层单元测试。
2. 后端 MockMvc 接口测试。
3. 前端表单和路由守卫测试。
4. Playwright E2E 测试。

## 9. 总体评价

当前项目已经具备较好的工程骨架和训练平台 MVP 雏形：

- 后端模块划分清晰，Controller、Service、Mapper、Entity、DTO 分层基本完整。
- 前端可以完成生产构建，页面路由和主要业务页面已接入。
- Worker 脚本能支持数据集分析与训练事件模拟。
- 修复 Bean 冲突后，后端应用能成功启动，基础安全拦截行为符合预期。

当前最大的短板不是编译，而是运行环境和测试覆盖：

- 缺少可用 MySQL/Redis 环境，导致端到端链路尚未验证。
- 自动化测试数量偏少，核心业务风险还没有被单元测试锁住。

建议下一步先把数据库环境跑起来，完成一次真实的“登录 → 上传数据集 → 创建训练任务 → 启动训练 → 查看日志指标 → 下载产物”的端到端冒烟测试，然后再补核心服务层测试。

