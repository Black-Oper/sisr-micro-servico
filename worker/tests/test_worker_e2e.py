"""E2E do worker: publica uma mensagem na fila e verifica que o job foi
processado de ponta a ponta contra RabbitMQ + Redis + MinIO REAIS."""
import io
import json

import pika
import redis
from minio import Minio
from PIL import Image
from testcontainers.minio import MinioContainer
from testcontainers.rabbitmq import RabbitMqContainer
from testcontainers.redis import RedisContainer

from worker.consumer import make_message_handler
from worker.model import FakeModel
from worker.processor import JobProcessor
from worker.state import JobState
from worker.storage import ArtifactStorage

QUEUE = "sisr.jobs.queue"


def _png(width, height):
    buf = io.BytesIO()
    Image.new("RGB", (width, height), "white").save(buf, format="PNG")
    return buf.getvalue()


def test_worker_processa_job_ponta_a_ponta():
    with RabbitMqContainer("rabbitmq:3-management") as rabbit, \
            RedisContainer("redis:7-alpine") as redis_c, \
            MinioContainer() as minio_c:

        # --- clientes reais ---
        connection = pika.BlockingConnection(rabbit.get_connection_params())
        channel = connection.channel()
        channel.queue_declare(QUEUE, durable=True)

        redis_client = redis.Redis(
            host=redis_c.get_container_host_ip(),
            port=int(redis_c.get_exposed_port(6379)),
            decode_responses=True,
        )
        cfg = minio_c.get_config()
        minio_client = Minio(
            cfg["endpoint"], access_key=cfg["access_key"],
            secret_key=cfg["secret_key"], secure=False,
        )

        # --- componentes do worker (modelo FAKE) ---
        storage = ArtifactStorage(minio_client)
        state = JobState(redis_client)
        processor = JobProcessor(state, storage, FakeModel(), output_bucket="sisr-outputs")

        # --- prepara o job (como o orchestrator faria) ---
        storage.ensure_bucket("sisr-inputs")
        storage.ensure_bucket("sisr-outputs")
        storage.upload("sisr-inputs", "job-9/input.png", _png(2, 2), "image/png")
        redis_client.hset("job:job-9", mapping={
            "status": "PENDING", "scale": "4", "inputKey": "job-9/input.png"})

        body = json.dumps({
            "jobId": "job-9", "inputBucket": "sisr-inputs",
            "inputKey": "job-9/input.png", "scale": 4,
            "createdAt": "2026-06-30T12:00:00Z",
        }).encode()
        channel.basic_publish(exchange="", routing_key=QUEUE, body=body)

        # --- consome UMA mensagem e processa ---
        method, properties, received = channel.basic_get(QUEUE, auto_ack=False)
        assert method is not None
        make_message_handler(processor)(channel, method, properties, received)

        # --- verificacoes ---
        h = redis_client.hgetall("job:job-9")
        assert h["status"] == "DONE"
        assert h["outputKey"] == "job-9/output.png"

        out = storage.download("sisr-outputs", "job-9/output.png")
        assert Image.open(io.BytesIO(out)).size == (8, 8)  # 2 * scale(4)

        connection.close()
