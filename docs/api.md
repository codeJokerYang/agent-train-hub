# API 约定

统一响应结构：

```json
{ "code": 0, "message": "ok", "data": {} }
```

- `code == 0` 表示成功；非 0 为业务/系统错误码（见后端 `common/ErrorCode`）。
- 时间字段统一返回 ISO-8601 字符串。
- 鉴权：除登录外，请求头携带 `Authorization: Bearer <token>`。

> 完整接口设计见 [`../../agent-training-platform-docs/02_technical_design.md`](../../agent-training-platform-docs/02_technical_design.md) 第 7 节。
> 下表为规划接口，标注其落地阶段。

## 已实现

### 通用
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/ping` | 健康检查 / 探活（白名单） |

### Auth
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录，返回 JWT（白名单） |
| GET | `/api/auth/me` | 当前用户 |

### Dataset
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/datasets` | 上传数据集（multipart，计算 SHA-256） |
| GET | `/api/datasets` | 分页列表（按 ownerId 数据权限） |
| GET | `/api/datasets/{id}` | 详情 |
| GET | `/api/datasets/{id}/download` | 下载原始文件 |
| POST | `/api/datasets/{id}/analyze` | 重新分析（内置分析器） |
| DELETE | `/api/datasets/{id}` | 删除 |

## 规划接口（后续阶段）

### Model Template
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/model-templates` | 可用模板列表 |
| POST | `/api/model-templates` | 新增（ADMIN） |
| PUT | `/api/model-templates/{id}` | 更新（ADMIN） |

### Training Job
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/training-jobs` | 创建任务 |
| GET | `/api/training-jobs` | 分页列表 |
| GET | `/api/training-jobs/{id}` | 详情 |
| POST | `/api/training-jobs/{id}/start` | 启动 |
| POST | `/api/training-jobs/{id}/cancel` | 停止 |
| POST | `/api/training-jobs/{id}/rerun` | 重跑 |
| GET | `/api/training-jobs/{id}/metrics` | 指标曲线 |
| GET | `/api/training-jobs/{id}/logs` | 日志分页 |
| GET | `/api/training-jobs/{id}/events` | SSE 实时事件 |

### Artifact
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/training-jobs/{id}/artifacts` | 产物列表 |
| GET | `/api/artifacts/{id}/download` | 下载产物 |

### Agent
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/agent/training-params` | 自然语言生成训练参数 |
| POST | `/api/agent/jobs/{id}/diagnose` | 诊断训练任务 |
| GET | `/api/agent/sessions/{id}/messages` | Agent 历史消息 |
