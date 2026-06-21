-- =====================================================================
-- AgentTrainHub 初始化脚本（第一阶段：核心表结构草稿 + 默认数据）
-- 表设计对应 02_technical_design.md 第 5 节。
-- 通过 docker-compose 启动 MySQL 时，本文件会被自动执行；
-- 也可手动导入：mysql -uroot -p < init.sql
-- 默认账号：admin / teacher / student，密码均为 123456（BCrypt 存储）。
-- =====================================================================

CREATE DATABASE IF NOT EXISTS agent_train_hub
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

-- 应用账号（Docker 镜像会通过 MYSQL_USER 自动创建；此处保证「手动导入到本地 MySQL」时也存在）。
CREATE USER IF NOT EXISTS 'ath'@'%' IDENTIFIED BY 'ath123456';
CREATE USER IF NOT EXISTS 'ath'@'localhost' IDENTIFIED BY 'ath123456';
GRANT ALL PRIVILEGES ON agent_train_hub.* TO 'ath'@'%';
GRANT ALL PRIVILEGES ON agent_train_hub.* TO 'ath'@'localhost';
FLUSH PRIVILEGES;

USE agent_train_hub;

-- ---------------------------------------------------------------------
-- 5.1 users
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
  id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  username      VARCHAR(64)  NOT NULL COMMENT '登录名',
  password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt 密码',
  real_name     VARCHAR(64)  DEFAULT NULL COMMENT '真实姓名',
  role          VARCHAR(32)  NOT NULL COMMENT 'ADMIN / TEACHER / STUDENT',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1 启用，0 禁用',
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ---------------------------------------------------------------------
-- 5.2 datasets
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS datasets (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '数据集ID',
  owner_id     BIGINT       NOT NULL COMMENT '上传人',
  name         VARCHAR(128) NOT NULL COMMENT '数据集名称',
  type         VARCHAR(32)  DEFAULT NULL COMMENT 'IMAGE / TABULAR / TEXT / ZIP / OTHER',
  storage_path VARCHAR(512) DEFAULT NULL COMMENT '文件路径',
  file_size    BIGINT       DEFAULT 0 COMMENT '文件大小(字节)',
  file_hash    VARCHAR(128) DEFAULT NULL COMMENT 'SHA-256',
  profile_json JSON         DEFAULT NULL COMMENT 'Python 分析结果',
  status       VARCHAR(32)  NOT NULL DEFAULT 'READY' COMMENT 'READY / ANALYZING / ANALYZE_FAILED',
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_datasets_owner (owner_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '数据集表';

-- ---------------------------------------------------------------------
-- 5.3 model_templates
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS model_templates (
  id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  code                VARCHAR(64)  NOT NULL COMMENT 'YOLO_DEMO / IMAGE_CLASSIFY_DEMO ...',
  name                VARCHAR(128) NOT NULL COMMENT '模板名称',
  algorithm_type      VARCHAR(64)  NOT NULL COMMENT 'CLASSIFICATION / OBJECT_DETECTION / TEXT_CLASSIFICATION',
  default_params_json JSON         DEFAULT NULL COMMENT '默认参数',
  param_schema_json   JSON         DEFAULT NULL COMMENT '前端表单 schema',
  enabled             TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用',
  created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_templates_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '模型模板表';

-- ---------------------------------------------------------------------
-- 5.4 training_jobs
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS training_jobs (
  id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  owner_id      BIGINT       NOT NULL COMMENT '创建人',
  dataset_id    BIGINT       DEFAULT NULL COMMENT '数据集ID',
  template_id   BIGINT       DEFAULT NULL COMMENT '模板ID',
  task_name     VARCHAR(128) NOT NULL COMMENT '任务名称',
  status        VARCHAR(32)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / RUNNING / SUCCESS / FAILED / CANCELLED',
  params_json   JSON         DEFAULT NULL COMMENT '训练参数',
  progress      INT          NOT NULL DEFAULT 0 COMMENT '0-100',
  current_epoch INT          NOT NULL DEFAULT 0 COMMENT '当前 epoch',
  total_epoch   INT          NOT NULL DEFAULT 0 COMMENT '总 epoch',
  error_message TEXT         DEFAULT NULL COMMENT '失败原因',
  started_at    DATETIME     DEFAULT NULL COMMENT '开始时间',
  finished_at   DATETIME     DEFAULT NULL COMMENT '结束时间',
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_jobs_owner (owner_id),
  KEY idx_jobs_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '训练任务表';

-- ---------------------------------------------------------------------
-- 5.5 training_metrics
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS training_metrics (
  id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '指标ID',
  job_id       BIGINT        NOT NULL COMMENT '任务ID',
  epoch        INT           DEFAULT NULL COMMENT '轮次',
  step         INT           DEFAULT NULL COMMENT '步数',
  metric_name  VARCHAR(64)   NOT NULL COMMENT 'loss / accuracy / precision / recall',
  metric_value DECIMAL(18,6) DEFAULT NULL COMMENT '指标值',
  created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_metrics_job (job_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '训练指标表';

-- ---------------------------------------------------------------------
-- 5.6 training_logs
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS training_logs (
  id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  job_id     BIGINT      NOT NULL COMMENT '任务ID',
  level      VARCHAR(16) NOT NULL DEFAULT 'INFO' COMMENT 'INFO / WARN / ERROR',
  message    TEXT        DEFAULT NULL COMMENT '日志内容',
  created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_logs_job (job_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '训练日志表';

-- ---------------------------------------------------------------------
-- 5.7 model_artifacts
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS model_artifacts (
  id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '产物ID',
  job_id        BIGINT       NOT NULL COMMENT '任务ID',
  artifact_type VARCHAR(32)  NOT NULL DEFAULT 'MODEL' COMMENT 'MODEL / REPORT / LOG',
  file_name     VARCHAR(255) NOT NULL COMMENT '文件名',
  storage_path  VARCHAR(512) DEFAULT NULL COMMENT '文件路径',
  file_size     BIGINT       DEFAULT 0 COMMENT '文件大小(字节)',
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_artifacts_job (job_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '模型产物表';

-- ---------------------------------------------------------------------
-- 5.8 agent_sessions / agent_messages
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS agent_sessions (
  id             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  user_id        BIGINT      NOT NULL COMMENT '用户ID',
  scene          VARCHAR(32) NOT NULL COMMENT 'PARAM_GEN / DIAGNOSE',
  related_job_id BIGINT      DEFAULT NULL COMMENT '关联任务ID',
  created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_sessions_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Agent 会话表';

CREATE TABLE IF NOT EXISTS agent_messages (
  id              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  session_id      BIGINT      NOT NULL COMMENT '会话ID',
  role            VARCHAR(16) NOT NULL COMMENT 'user / assistant / system',
  content         TEXT        DEFAULT NULL COMMENT '文本内容',
  structured_json JSON        DEFAULT NULL COMMENT '结构化输出',
  created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_messages_session (session_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Agent 消息表';

-- =====================================================================
-- 默认账号（密码均为 123456，BCrypt cost=10）
-- =====================================================================
INSERT IGNORE INTO users (username, password_hash, real_name, role, status) VALUES
  ('admin',   '$2a$10$BNZqw7OebTQctClMWyzlIuJcY/DyTf4T8RKr/pqKpRiM/6yiuaNs6', '系统管理员', 'ADMIN',   1),
  ('teacher', '$2a$10$Gfvc5huJwPCqWkb1gPuyMOc7rCXyc4/StbeUlGmnvA5Dm42nJT12S', '示例教师',   'TEACHER', 1),
  ('student', '$2a$10$G8aNYZac5uSXHcVUPI4eSONX1PXEVuyvg89/9P7bHUpCZG5fmgm1u', '示例学生',   'STUDENT', 1);

-- =====================================================================
-- 内置模型模板
-- =====================================================================
INSERT IGNORE INTO model_templates (code, name, algorithm_type, default_params_json, param_schema_json, enabled) VALUES
  ('IMAGE_CLASSIFY_DEMO', '图像分类示例模板', 'CLASSIFICATION',
   '{"epochs": 20, "batchSize": 32, "learningRate": 0.001, "validationRatio": 0.2}',
   '{"fields": [{"key": "epochs", "label": "Epochs", "type": "int", "min": 1, "max": 1000}, {"key": "batchSize", "label": "Batch Size", "type": "int", "min": 1, "max": 1024}, {"key": "learningRate", "label": "Learning Rate", "type": "float", "min": 0}]}',
   1),
  ('YOLO_DEMO', 'YOLO 目标检测示例模板', 'OBJECT_DETECTION',
   '{"epochs": 50, "batchSize": 16, "learningRate": 0.0005, "validationRatio": 0.2}',
   '{"fields": [{"key": "epochs", "label": "Epochs", "type": "int", "min": 1, "max": 1000}, {"key": "batchSize", "label": "Batch Size", "type": "int", "min": 1, "max": 1024}, {"key": "learningRate", "label": "Learning Rate", "type": "float", "min": 0}]}',
   1),
  ('TEXT_CLASSIFY_DEMO', '文本分类示例模板', 'TEXT_CLASSIFICATION',
   '{"epochs": 10, "batchSize": 32, "learningRate": 0.0002, "validationRatio": 0.2}',
   '{"fields": [{"key": "epochs", "label": "Epochs", "type": "int", "min": 1, "max": 1000}, {"key": "batchSize", "label": "Batch Size", "type": "int", "min": 1, "max": 1024}, {"key": "learningRate", "label": "Learning Rate", "type": "float", "min": 0}]}',
   1);
