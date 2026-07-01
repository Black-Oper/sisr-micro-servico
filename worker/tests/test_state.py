import pytest
import redis
from testcontainers.redis import RedisContainer

from worker.state import JobState


@pytest.fixture(scope="module")
def redis_client():
    container = RedisContainer("redis:7-alpine")
    container.start()
    try:
        client = redis.Redis(
            host=container.get_container_host_ip(),
            port=int(container.get_exposed_port(6379)),
            decode_responses=True,
        )
        yield client
    finally:
        container.stop()


def test_mark_processing_atualiza_status(redis_client):
    redis_client.hset("job:job-1", mapping={"status": "PENDING", "scale": "4"})

    JobState(redis_client).mark_processing("job-1")

    h = redis_client.hgetall("job:job-1")
    assert h["status"] == "PROCESSING"
    assert h["startedAt"]  # preenchido


def test_mark_done_grava_output_e_finished(redis_client):
    redis_client.hset("job:job-2", mapping={"status": "PROCESSING"})

    JobState(redis_client).mark_done("job-2", "job-2/output.png")

    h = redis_client.hgetall("job:job-2")
    assert h["status"] == "DONE"
    assert h["outputKey"] == "job-2/output.png"
    assert h["finishedAt"]


def test_mark_failed_grava_erro(redis_client):
    redis_client.hset("job:job-3", mapping={"status": "PROCESSING"})

    JobState(redis_client).mark_failed("job-3", "falha na inferencia")

    h = redis_client.hgetall("job:job-3")
    assert h["status"] == "FAILED"
    assert h["error"] == "falha na inferencia"
    assert h["finishedAt"]
