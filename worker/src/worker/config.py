
import os
from dataclasses import dataclass
from typing import Mapping, Optional


@dataclass(frozen=True)
class Config:
    rabbitmq_host: str
    rabbitmq_port: int
    rabbitmq_user: str
    rabbitmq_pass: str
    queue: str
    redis_host: str
    redis_port: int
    minio_endpoint: str
    minio_access_key: str
    minio_secret_key: str
    minio_secure: bool
    bucket_input: str
    bucket_output: str
    model_scale: int
    model_path: str

    @staticmethod
    def from_env(env: Optional[Mapping[str, str]] = None) -> "Config":
        env = env if env is not None else os.environ
        return Config(
            rabbitmq_host=env.get("RABBITMQ_HOST", "localhost"),
            rabbitmq_port=int(env.get("RABBITMQ_PORT", "5672")),
            rabbitmq_user=env.get("RABBITMQ_USER", "guest"),
            rabbitmq_pass=env.get("RABBITMQ_PASS", "guest"),
            queue=env.get("RABBITMQ_QUEUE", "sisr.jobs.queue"),
            redis_host=env.get("REDIS_HOST", "localhost"),
            redis_port=int(env.get("REDIS_PORT", "6379")),
            minio_endpoint=env.get("MINIO_ENDPOINT", "localhost:9000"),
            minio_access_key=env.get("MINIO_ACCESS_KEY", "minioadmin"),
            minio_secret_key=env.get("MINIO_SECRET_KEY", "minioadmin"),
            minio_secure=env.get("MINIO_SECURE", "false").lower() == "true",
            bucket_input=env.get("MINIO_BUCKET_INPUT", "sisr-inputs"),
            bucket_output=env.get("MINIO_BUCKET_OUTPUT", "sisr-outputs"),
            model_scale=int(env.get("MODEL_SCALE", "2")),
            model_path=env.get("MODEL_PATH", "/app/models/RTDVSR_best_model.pth"),
        )
