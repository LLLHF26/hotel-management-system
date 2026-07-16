"""日志配置 —— Console + File + Kafka 三通道输出。"""

from __future__ import annotations

import atexit
import json
import logging
import socket
from datetime import datetime, timezone

from config import LOG_FILE, LOG_LEVEL


def _env(key: str, default: str = "") -> str:
    import os
    return os.getenv(key, default)


class KafkaLogHandler(logging.Handler):
    """将日志以 JSON 格式异步发送到 Kafka。"""

    def __init__(self, bootstrap_servers: list[str], topic: str):
        super().__init__()
        self.topic = topic
        try:
            from kafka import KafkaProducer
            self.producer: KafkaProducer | None = KafkaProducer(
                bootstrap_servers=bootstrap_servers,
                value_serializer=lambda v: json.dumps(v, default=str).encode("utf-8"),
                compression_type="gzip",
                acks=1,
                max_block_ms=5000,
                client_id="ai-service-log-producer",
            )
        except Exception:
            self.producer = None
        atexit.register(self.close)

    def emit(self, record: logging.LogRecord) -> None:
        if self.producer is None:
            return
        try:
            log_entry = {
                "timestamp": datetime.now(timezone.utc).isoformat(),
                "level": record.levelname,
                "logger": record.name,
                "message": self.format(record),
                "app": "ai-service",
                "host": socket.gethostname(),
                "thread": record.threadName,
            }
            if record.exc_info and record.exc_info[1]:
                log_entry["stackTrace"] = self.format(record)
            self.producer.send(self.topic, log_entry)
        except Exception:
            self.handleError(record)

    def close(self) -> None:
        if self.producer is not None:
            try:
                self.producer.flush(timeout=10)
                self.producer.close(timeout=5)
            except Exception:
                pass
        super().close()


def setup_logging() -> None:
    """初始化日志：控制台 + 文件 + Kafka（可选）。"""
    root = logging.getLogger()
    root.setLevel(getattr(logging, LOG_LEVEL.upper(), logging.INFO))

    # 控制台（stderr）
    console = logging.StreamHandler()
    console.setFormatter(logging.Formatter("%(asctime)s [%(levelname)s] %(name)s: %(message)s"))
    root.addHandler(console)

    # 文件（修复 LOG_FILE 之前未启用的 bug）
    try:
        file_handler = logging.FileHandler(LOG_FILE, encoding="utf-8")
        file_handler.setFormatter(logging.Formatter("%(asctime)s [%(levelname)s] %(name)s: %(message)s"))
        root.addHandler(file_handler)
    except Exception:
        pass

    # Kafka（连接失败不影响服务启动）
    try:
        kafka_servers = _env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9091,localhost:9092,localhost:9093")
        kafka_topic = _env("KAFKA_LOG_TOPIC", "logs-topic")
        kafka = KafkaLogHandler([s.strip() for s in kafka_servers.split(",")], kafka_topic)
        kafka.setFormatter(logging.Formatter("%(message)s"))
        root.addHandler(kafka)
    except Exception as e:
        logging.warning("Kafka 日志处理器初始化失败: %s", e)
