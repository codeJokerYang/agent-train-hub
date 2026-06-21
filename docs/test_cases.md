# AgentTrainHub 测试用例

本文档用于 AgentTrainHub MVP 的功能验收、接口联调和后续自动化测试设计。当前用例覆盖登录认证、数据集管理、模型模板、训练任务、日志指标、模型产物、权限控制和异常场景。

## 1. 测试账号

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| admin | 123456 | ADMIN |
| teacher | 123456 | TEACHER |
| student | 123456 | STUDENT |

## 2. 环境前置条件

1. MySQL 已启动，并已执行 `deploy/mysql/init.sql`。
2. Redis 已启动，若当前阶段未强依赖 Redis，可先跳过。
3. 后端服务已启动，默认地址为 `http://localhost:8080`。
4. 前端服务已启动，默认地址为 `http://localhost:5173`。
5. 准备测试文件：
   - `demo-dataset.zip`
   - `demo-image.jpg`
   - `demo-table.csv`

## 3. 登录认证测试

| 编号 | 用例名称 | 前置条件 | 操作步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| AUTH-001 | 正确账号密码登录 | 数据库存在 student 账号 | 访问登录页，输入 `student / 123456`，点击登录 | 登录成功，跳转 Dashboard，localStorage/Pinia 中保存 token 和用户信息 |
| AUTH-002 | 错误密码登录失败 | 数据库存在 student 账号 | 输入 `student / wrong_password`，点击登录 | 登录失败，页面提示账号或密码错误，不跳转 |
| AUTH-003 | 未登录访问业务页面 | 清空 token | 直接访问 `/datasets` | 自动跳转 `/login` |
| AUTH-004 | 登录后访问登录页 | 已登录 | 访问 `/login` | 自动跳转 `/dashboard` |
| AUTH-005 | 获取当前用户信息 | 已登录 | 调用 `GET /api/auth/me` | 返回当前用户 id、username、realName、role |
| AUTH-006 | 无 token 调用受保护接口 | 未登录 | 调用 `GET /api/datasets` | 返回 401，响应体为统一 Result JSON |
| AUTH-007 | 退出登录 | 已登录 | 点击 Layout 顶栏退出按钮 | 清空 token 和用户信息，跳转登录页 |

## 4. 数据集管理测试

| 编号 | 用例名称 | 前置条件 | 操作步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| DATASET-001 | 上传 zip 数据集 | student 已登录 | 在数据集页选择 `demo-dataset.zip` 上传 | 上传成功，列表出现新数据集，类型为 ZIP，大小和上传时间正确 |
| DATASET-002 | 上传图片数据集 | student 已登录 | 上传 `demo-image.jpg` | 上传成功，类型识别为 IMAGE |
| DATASET-003 | 上传 csv 数据集 | student 已登录 | 上传 `demo-table.csv` | 上传成功，类型识别为 TABULAR |
| DATASET-004 | 数据集列表分页 | student 已上传多个数据集 | 调用列表接口，切换分页 page/size | 返回对应分页数据，总数正确 |
| DATASET-005 | 关键词搜索 | student 已上传名为 pcb-demo 的数据集 | 在搜索框输入 `pcb` | 列表只展示匹配数据 |
| DATASET-006 | 查看数据集详情 | student 已上传数据集 | 点击详情或调用 `GET /api/datasets/{id}` | 返回名称、类型、大小、hash、ownerId、storagePath、status |
| DATASET-007 | 下载自己的数据集 | student 已上传数据集 | 点击下载 | 浏览器下载原始文件，文件名和内容正确 |
| DATASET-008 | 删除自己的数据集 | student 已上传数据集 | 点击删除并确认 | 数据库记录删除，本地文件删除，列表不再显示 |
| DATASET-009 | 取消删除 | student 已上传数据集 | 点击删除，在确认框取消 | 数据集仍存在 |
| DATASET-010 | 上传超过 500MB 文件 | student 已登录 | 选择超过 500MB 的文件 | 前端或后端拒绝上传，提示文件过大 |
| DATASET-011 | 删除不存在的数据集 | student 已登录 | 调用 `DELETE /api/datasets/999999` | 返回资源不存在提示，不出现 500 |
| DATASET-012 | 下载不存在的数据集 | student 已登录 | 调用 `GET /api/datasets/999999/download` | 返回资源不存在提示，不出现 500 |

## 5. 数据权限测试

| 编号 | 用例名称 | 前置条件 | 操作步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| PERM-001 | STUDENT 只能看自己的数据集 | student 和另一个用户各有数据集 | student 登录后访问数据集列表 | 只看到自己的数据集 |
| PERM-002 | STUDENT 不能下载他人数据集 | 他人存在数据集 id | student 调用他人数据集下载接口 | 返回 403 或无权限业务错误 |
| PERM-003 | STUDENT 不能删除他人数据集 | 他人存在数据集 id | student 调用他人数据集删除接口 | 返回 403 或无权限业务错误，数据不被删除 |
| PERM-004 | ADMIN 可查看全部数据集 | 数据库有多个用户的数据集 | admin 登录访问数据集列表 | 可以看到全部数据集 |
| PERM-005 | TEACHER 可查看全部数据集 | 数据库有多个用户的数据集 | teacher 登录访问数据集列表 | 按当前需求，teacher 可以看到全部数据集 |

## 6. 模型模板测试

| 编号 | 用例名称 | 前置条件 | 操作步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| TEMPLATE-001 | 查询模型模板列表 | 已登录，init.sql 已导入 | 调用 `GET /api/model-templates` | 返回 enabled=true 的模板 |
| TEMPLATE-002 | 查询模板详情 | 已登录，存在模板 id | 调用 `GET /api/model-templates/{id}` | 返回模板 code、name、algorithmType、默认参数、schema |
| TEMPLATE-003 | 未登录不能查模板 | 未登录 | 调用 `GET /api/model-templates` | 返回 401 |
| TEMPLATE-004 | 内置模板完整 | init.sql 已导入 | 查询模板列表 | 包含 IMAGE_CLASSIFY_DEMO、YOLO_DEMO、TEXT_CLASSIFY_DEMO |

## 7. 训练任务测试

| 编号 | 用例名称 | 前置条件 | 操作步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| JOB-001 | 创建训练任务 | student 已登录，存在数据集和模板 | 填写 taskName、datasetId、templateId、epochs、batchSize、learningRate 后提交 | 创建成功，状态为 PENDING |
| JOB-002 | 创建任务参数缺失 | student 已登录 | 不选择 datasetId 或 templateId 提交 | 返回参数错误，页面提示必填 |
| JOB-003 | 创建任务参数越界 | student 已登录 | epochs 输入 0 或 learningRate 输入负数 | 返回参数错误，不创建任务 |
| JOB-004 | 查看任务列表 | student 已创建任务 | 访问任务列表页 | 展示任务名称、状态、进度、创建时间 |
| JOB-005 | 查看任务详情 | student 已创建任务 | 进入任务详情页 | 展示基本信息、参数、状态、进度 |
| JOB-006 | 启动 PENDING 任务 | 任务状态为 PENDING | 点击启动 | 状态变为 RUNNING，startedAt 有值 |
| JOB-007 | 重复启动 RUNNING 任务 | 任务状态为 RUNNING | 再次点击启动 | 返回状态冲突错误，不重复启动 |
| JOB-008 | 停止 RUNNING 任务 | 任务状态为 RUNNING | 点击停止 | 状态变为 CANCELLED，finishedAt 有值，写入 WARN 日志 |
| JOB-009 | 停止非 RUNNING 任务 | 任务状态为 PENDING 或 SUCCESS | 点击停止 | 返回状态冲突错误 |
| JOB-010 | 重跑失败任务 | 任务状态为 FAILED | 点击重跑 | 新一轮任务执行开始，状态变为 RUNNING |
| JOB-011 | 重跑成功任务 | 任务状态为 SUCCESS | 点击重跑 | 状态变为 RUNNING，进度重新计算 |
| JOB-012 | 模拟训练完成 | 任务已启动，epochs 较小 | 等待训练结束 | 状态变为 SUCCESS，progress=100，finishedAt 有值 |

## 8. 日志与指标测试

| 编号 | 用例名称 | 前置条件 | 操作步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| METRIC-001 | 训练生成日志 | 任务正在 RUNNING | 每 2 秒刷新日志列表 | 日志数量增加，包含 epoch 信息 |
| METRIC-002 | 训练生成 loss 指标 | 任务正在 RUNNING | 调用 `GET /api/training-jobs/{id}/metrics` | 返回 loss 指标，epoch 递增 |
| METRIC-003 | 训练生成 accuracy 指标 | 任务正在 RUNNING | 调用指标接口 | 返回 accuracy 指标，数值整体上升 |
| METRIC-004 | 前端曲线展示 | 任务已有指标 | 打开任务详情页 | ECharts 正常展示 loss/accuracy 曲线 |
| METRIC-005 | 日志分页 | 任务已有多条日志 | 调用日志分页接口 | 返回对应页数据，总数正确 |

## 9. 模型产物测试

| 编号 | 用例名称 | 前置条件 | 操作步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| ARTIFACT-001 | 训练成功生成产物 | 任务训练到 SUCCESS | 查看产物列表 | 出现 MODEL 类型 `model-demo.txt` 和 REPORT 类型 `training-report.json` |
| ARTIFACT-002 | 下载模型文件 | 任务有 MODEL 产物 | 点击下载 `model-demo.txt` | 成功下载，内容包含 jobId 或任务摘要 |
| ARTIFACT-003 | 下载训练报告 | 任务有 REPORT 产物 | 点击下载 `training-report.json` | 成功下载，JSON 格式正确 |
| ARTIFACT-004 | 无权限下载产物 | student 尝试下载他人任务产物 | 调用下载接口 | 返回 403 或无权限业务错误 |
| ARTIFACT-005 | 下载不存在的产物 | 已登录 | 调用 `GET /api/artifacts/999999/download` | 返回资源不存在提示，不出现 500 |

## 10. 前端交互测试

| 编号 | 用例名称 | 前置条件 | 操作步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| UI-001 | Layout 显示用户信息 | 已登录 | 进入任意业务页面 | 顶栏展示用户名和角色 |
| UI-002 | 菜单按角色展示 | 用不同角色登录 | 查看左侧菜单 | 不同角色看到符合权限的菜单 |
| UI-003 | 上传 loading 状态 | 上传较大文件 | 观察上传按钮和列表 | 上传过程中按钮 loading 或禁用，完成后刷新列表 |
| UI-004 | 删除二次确认 | 点击删除按钮 | 弹出确认框 | 确认后删除，取消后不删除 |
| UI-005 | 任务轮询刷新 | 任务 RUNNING | 打开任务详情页等待 | 进度、日志、曲线每 2 秒更新 |
| UI-006 | 接口错误提示 | 让接口返回 403/500 | 观察页面 | 展示错误提示，不白屏 |

## 11. Agent 模块测试预留

| 编号 | 用例名称 | 前置条件 | 操作步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| AGENT-001 | 自然语言生成参数 | Agent mock 模式开启 | 输入“用图片分类模型跑 30 轮，batch size 32，学习率 0.001” | 返回 epochs=30、batchSize=32、learningRate=0.001，needConfirm=true |
| AGENT-002 | 模糊参数生成 | Agent mock 模式开启 | 输入“帮我训练一个目标检测模型” | 返回合理默认参数，并给出 warnings |
| AGENT-003 | 日志诊断 ERROR | 任务日志包含 ERROR | 调用诊断接口 | 返回 riskLevel=HIGH，并给出原因 |
| AGENT-004 | loss 上升诊断 | 指标中 loss 连续上升 | 调用诊断接口 | 返回 riskLevel=MEDIUM，建议降低 learningRate |
| AGENT-005 | 模型服务未配置降级 | 未配置大模型 API Key | 调用 Agent 接口 | 使用 mock/fallback，不出现 500 |

## 12. 建议自动化测试清单

### 后端单元测试

- `AuthServiceTest`
  - 正确密码登录成功。
  - 错误密码登录失败。
  - 禁用用户不能登录。
- `DatasetServiceTest`
  - 上传文件保存元数据和 SHA-256。
  - STUDENT 访问他人数据集被拒绝。
  - ADMIN 访问任意数据集成功。
- `TrainingJobServiceTest`
  - PENDING 可以 start。
  - RUNNING 不能重复 start。
  - 非 RUNNING 不能 cancel。
  - SUCCESS 可以 rerun。
- `TrainingExecutorTest`
  - 模拟训练会写日志、指标和进度。
  - cancel flag 生效后任务变 CANCELLED。
- `ArtifactServiceTest`
  - SUCCESS 后产物记录可查询。
  - 无权限下载产物被拒绝。

### 后端接口测试

建议使用 Spring Boot Test + MockMvc：

- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/datasets`
- `GET /api/datasets`
- `GET /api/datasets/{id}/download`
- `POST /api/training-jobs`
- `POST /api/training-jobs/{id}/start`
- `GET /api/training-jobs/{id}/metrics`
- `GET /api/training-jobs/{id}/artifacts`

### 前端测试

建议使用 Vitest + Vue Test Utils：

- Login 表单校验。
- Axios token 注入。
- 路由守卫跳转。
- DatasetList 上传、删除、空状态。
- JobCreate 表单校验。
- JobDetail 轮询刷新和 ECharts 容器渲染。

### E2E 测试

建议使用 Playwright：

1. 登录 student。
2. 上传数据集。
3. 创建训练任务。
4. 启动训练任务。
5. 等待进度变化。
6. 查看日志和曲线。
7. 等待任务成功。
8. 下载模型产物。

## 13. 冒烟测试最小路径

这条路径用于每次改完代码后快速确认系统没有断：

1. 打开前端登录页。
2. 使用 `student / 123456` 登录。
3. 上传 `demo-dataset.zip`。
4. 在数据集列表确认上传记录存在。
5. 创建训练任务，epochs 设置为 3。
6. 启动任务。
7. 等待任务变为 SUCCESS。
8. 确认日志、指标曲线、产物列表都有数据。
9. 下载 `model-demo.txt`。
10. 退出登录。

