
from worker.messages import JobMessage


def make_message_handler(processor):
    """Cria o callback no formato do pika: (channel, method, properties, body)."""

    def on_message(channel, method, properties, body):
        try:
            message = JobMessage.from_json(body)
            processor.process(message)
            channel.basic_ack(delivery_tag=method.delivery_tag)
        except Exception:
            # falha -> nack sem requeue: a mensagem vai para a DLQ
            channel.basic_nack(delivery_tag=method.delivery_tag, requeue=False)

    return on_message
