
from datetime import datetime, timezone


def _now_iso() -> str:
    return datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


class JobState:
    def __init__(self, client):
        self._redis = client

    def _key(self, job_id: str) -> str:
        return f"job:{job_id}"

    def mark_processing(self, job_id: str) -> None:
        self._redis.hset(self._key(job_id), mapping={
            "status": "PROCESSING",
            "startedAt": _now_iso(),
        })

    def mark_done(self, job_id: str, output_key: str) -> None:
        self._redis.hset(self._key(job_id), mapping={
            "status": "DONE",
            "outputKey": output_key,
            "finishedAt": _now_iso(),
        })

    def mark_failed(self, job_id: str, error: str) -> None:
        self._redis.hset(self._key(job_id), mapping={
            "status": "FAILED",
            "error": error,
            "finishedAt": _now_iso(),
        })
