
import json
from dataclasses import dataclass
from typing import Union


@dataclass(frozen=True)
class JobMessage:
    job_id: str
    input_bucket: str
    input_key: str
    scale: int
    created_at: str

    @staticmethod
    def from_json(raw: Union[bytes, str]) -> "JobMessage":
        data = json.loads(raw)
        return JobMessage(
            job_id=data["jobId"],
            input_bucket=data["inputBucket"],
            input_key=data["inputKey"],
            scale=int(data["scale"]),
            created_at=data["createdAt"],
        )
