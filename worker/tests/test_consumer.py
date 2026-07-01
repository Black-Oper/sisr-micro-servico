from unittest.mock import Mock

from worker.consumer import make_message_handler

_BODY = (
    b'{"jobId":"job-1","inputBucket":"sisr-inputs",'
    b'"inputKey":"job-1/input.png","scale":4,'
    b'"createdAt":"2026-06-30T12:00:00Z"}'
)


def _method(delivery_tag=42):
    m = Mock()
    m.delivery_tag = delivery_tag
    return m


def test_callback_sucesso_da_ack():
    processor, channel = Mock(), Mock()
    handler = make_message_handler(processor)

    handler(channel, _method(), Mock(), _BODY)

    assert processor.process.call_count == 1
    msg = processor.process.call_args.args[0]
    assert msg.job_id == "job-1"
    assert msg.scale == 4
    channel.basic_ack.assert_called_once_with(delivery_tag=42)
    channel.basic_nack.assert_not_called()


def test_callback_falha_da_nack():
    processor, channel = Mock(), Mock()
    processor.process.side_effect = RuntimeError("boom")
    handler = make_message_handler(processor)

    handler(channel, _method(), Mock(), _BODY)

    channel.basic_nack.assert_called_once_with(delivery_tag=42, requeue=False)
    channel.basic_ack.assert_not_called()
