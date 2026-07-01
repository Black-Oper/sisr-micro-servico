
from worker.messages import JobMessage


class JobProcessor:
    def __init__(self, state, storage, model, output_bucket: str):
        self._state = state
        self._storage = storage
        self._model = model
        self._output_bucket = output_bucket

    def process(self, message: JobMessage) -> None:
        self._state.mark_processing(message.job_id)
        try:
            input_bytes = self._storage.download(message.input_bucket, message.input_key)
            output_bytes = self._model.upscale(input_bytes, message.scale)
            output_key = f"{message.job_id}/output.png"
            self._storage.upload(
                self._output_bucket, output_key, output_bytes, "image/png")
            self._state.mark_done(message.job_id, output_key)
        except Exception as e:
            # marca FAILED e propaga -> o consumer da nack (mensagem vai p/ DLQ)
            self._state.mark_failed(message.job_id, str(e))
            raise
