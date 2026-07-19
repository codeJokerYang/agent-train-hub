# 启动与排障手册

## 一键起依赖

```bash
cd deploy
docker compose up -d        # 启动 MySQL + Redis
docker compose ps           # 查看状态
docker compose logs -f mysql
docker compose down         # 停止（加 -v 删除数据卷，会清空数据库）
```

## 后端

```bash
cd backend
./mvnw clean compile        # 编译（Windows: mvnw.cmd）
./mvnw test                 # 运行单元测试
./mvnw spring-boot:run      # 启动，端口 8080
```

- 首次运行 `mvnw` 会自动下载 Maven 3.9.9，需联网。
- 验证：`curl http://localhost:8080/api/ping` 返回 `{"code":0,...}`。

## 前端

```bash
cd frontend
npm install
npm run dev                 # 开发，端口 5173，/api 代理到 8080
npm run build               # 生产构建（vue-tsc 类型检查 + vite 打包）
npm run type-check          # 仅类型检查
```

## Worker（mock 脚本，仅标准库）

```bash
cd worker
python dataset_profile.py --input <file.zip> --output profile.json
python train_simulator.py --job-id 1 --config config.json --event-file events.jsonl --interval 1
```

## 常见问题

- **后端启动报无法连接 MySQL/Redis**：先 `docker compose up -d` 起依赖，或用环境变量
  `MYSQL_URL` / `REDIS_HOST` 指向已有实例。
- **端口被占用**：后端 `SERVER_PORT`、前端 `vite.config.ts` 的 `server.port` 可改。
- **JWT secret 太短**：HS256 要求密钥至少 32 字节，用 `JWT_SECRET` 覆盖默认值。
- **生产前端跨域被拒绝**：用 `CORS_ALLOWED_ORIGINS` 配置完整的 HTTPS origin；多个值以逗号分隔，
  例如 `https://train.example.edu,https://admin.example.edu`。不要填写 `*` 或带路径的 URL。
- **没有 Docker**：自行安装 MySQL 8 / Redis 7，手动执行 `deploy/mysql/init.sql`。

## 分阶段实现

1. **第一阶段（已完成）**：项目初始化与骨架，后端可编译、前端可构建。
2. **第二阶段**：登录认证、用户/数据集/模板/任务 CRUD、训练模拟器、SSE、指标曲线、Agent（mock）。
3. **第三阶段**：Docker 化后端/前端、审计日志、MinIO、真实 Python 训练脚本、更多模板。
4. **第四阶段**：Spring AI / Spring AI Alibaba、MCP 工具注册、任务编排可视化、团队协作权限。
