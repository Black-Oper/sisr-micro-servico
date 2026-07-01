
import pika
import redis
from minio import Minio

from worker.consumer import make_message_handler
from worker.processor import JobProcessor
from worker.state import JobState
from worker.storage import ArtifactStorage

_DLX = "sisr.dlx"
_DLQ = "sisr.jobs.dlq"
_DEAD_ROUTING_KEY = "job.dead"


def _connect(config):
    credentials = pika.PlainCredentials(config.rabbitmq_user, config.rabbitmq_pass)
    params = pika.ConnectionParameters(
        host=config.rabbitmq_host, port=config.rabbitmq_port, credentials=credentials
    )
    connection = pika.BlockingConnection(params)
    channel = connection.channel()

    channel.exchange_declare(_DLX, exchange_type="direct", durable=True)
    channel.queue_declare(_DLQ, durable=True)
    channel.queue_bind(_DLQ, _DLX, routing_key=_DEAD_ROUTING_KEY)
    channel.queue_declare(config.queue, durable=True, arguments={
        "x-dead-letter-exchange": _DLX,
        "x-dead-letter-routing-key": _DEAD_ROUTING_KEY,
    })
    channel.basic_qos(prefetch_count=1)
    return connection, channel


def run_consumer(config, model) -> None:
    redis_client = redis.Redis(
        host=config.redis_host, port=config.redis_port, decode_responses=True
    )
    minio_client = Minio(
        config.minio_endpoint,
        access_key=config.minio_access_key,
        secret_key=config.minio_secret_key,
        secure=config.minio_secure,
    )
    storage = ArtifactStorage(minio_client)
    storage.ensure_bucket(config.bucket_input)
    storage.ensure_bucket(config.bucket_output)

    processor = JobProcessor(JobState(redis_client), storage, model, config.bucket_output)
    handler = make_message_handler(processor)

    connection, channel = _connect(config)
    channel.basic_consume(queue=config.queue, on_message_callback=handler)
    print("[worker] aguardando jobs em '%s'..." % config.queue, flush=True)
    try:
        channel.start_consuming()
    finally:
        connection.close()
