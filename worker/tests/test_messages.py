from worker.messages import JobMessage


def test_parse_job_message_do_json():
    raw = (
        '{"jobId":"job-1","inputBucket":"sisr-inputs",'
        '"inputKey":"job-1/input.png","scale":4,'
        '"createdAt":"2026-06-30T12:00:00Z"}'
    )

    msg = JobMessage.from_json(raw)

    assert msg.job_id == "job-1"
    assert msg.input_bucket == "sisr-inputs"
    assert msg.input_key == "job-1/input.png"
    assert msg.scale == 4
    assert msg.created_at == "2026-06-30T12:00:00Z"
