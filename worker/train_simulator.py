#!/usr/bin/env python3
"""训练模拟器（第一阶段 mock 版）。

用法::

    python train_simulator.py --job-id 1 --config data/jobs/1/config.json \
        --event-file data/jobs/1/events.jsonl --interval 1.0

读取 config（epochs / batchSize / learningRate），逐 epoch 生成事件并写入 jsonl，
同时打印到 stdout；后端读取后落库 MySQL/Redis 并经 SSE 推送给前端。
事件类型对应技术文档第 8 节：job_status / log / metric / artifact / done。
loss 从 ~1.0 指数衰减并带随机波动，accuracy 随之缓慢上升。
"""
import argparse
import json
import math
import os
import random
import sys
import time

DEFAULT_CONFIG = {"epochs": 20, "batchSize": 16, "learningRate": 0.001}


def load_config(path):
    cfg = dict(DEFAULT_CONFIG)
    if path and os.path.exists(path):
        try:
            with open(path, "r", encoding="utf-8") as fp:
                loaded = json.load(fp)
            params = loaded.get("params", loaded) if isinstance(loaded, dict) else {}
            for key in DEFAULT_CONFIG:
                if isinstance(params, dict) and params.get(key) is not None:
                    cfg[key] = params[key]
        except (json.JSONDecodeError, OSError):
            pass
    try:
        cfg["epochs"] = max(1, int(cfg["epochs"]))
    except (TypeError, ValueError):
        cfg["epochs"] = DEFAULT_CONFIG["epochs"]
    return cfg


def emit(writer, event):
    line = json.dumps(event, ensure_ascii=False)
    if writer is not None:
        writer.write(line + "\n")
        writer.flush()
    print(line, flush=True)


def main():
    parser = argparse.ArgumentParser(description="AgentTrainHub training simulator (mock)")
    parser.add_argument("--job-id", required=True)
    parser.add_argument("--config")
    parser.add_argument("--event-file")
    parser.add_argument("--interval", type=float, default=1.0, help="每个 epoch 间隔秒数")
    parser.add_argument("--seed", type=int, default=42)
    args = parser.parse_args()

    random.seed(args.seed)
    cfg = load_config(args.config)
    total = cfg["epochs"]
    job_id = args.job_id

    writer = None
    if args.event_file:
        os.makedirs(os.path.dirname(os.path.abspath(args.event_file)), exist_ok=True)
        writer = open(args.event_file, "w", encoding="utf-8")

    try:
        emit(writer, {"type": "job_status", "jobId": job_id,
                      "payload": {"status": "RUNNING", "progress": 0}})
        emit(writer, {"type": "log", "jobId": job_id,
                      "payload": {"level": "INFO",
                                  "message": "training started: epochs=%s batchSize=%s lr=%s"
                                             % (total, cfg["batchSize"], cfg["learningRate"])}})

        for epoch in range(1, total + 1):
            loss = max(0.02, math.exp(-0.15 * epoch) + random.uniform(-0.03, 0.03))
            accuracy = max(0.0, min(0.99, 1.0 - loss + random.uniform(-0.02, 0.02)))
            progress = int(epoch / total * 100)

            emit(writer, {"type": "metric", "jobId": job_id, "epoch": epoch,
                          "payload": {"epoch": epoch, "loss": round(loss, 4),
                                      "accuracy": round(accuracy, 4)}})
            emit(writer, {"type": "log", "jobId": job_id,
                          "payload": {"level": "INFO",
                                      "message": "epoch %d/%d - loss=%.4f acc=%.4f"
                                                 % (epoch, total, loss, accuracy)}})
            emit(writer, {"type": "job_status", "jobId": job_id,
                          "payload": {"status": "RUNNING", "progress": progress}})

            if args.interval > 0:
                time.sleep(args.interval)

        emit(writer, {"type": "artifact", "jobId": job_id,
                      "payload": {"fileName": "model_job%s.txt" % job_id, "artifactType": "MODEL"}})
        emit(writer, {"type": "done", "jobId": job_id, "payload": {"status": "SUCCESS"}})
    finally:
        if writer is not None:
            writer.close()
    return 0


if __name__ == "__main__":
    sys.exit(main())
