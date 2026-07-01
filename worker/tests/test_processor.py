from unittest.mock import Mock

import pytest

from worker.messages import JobMessage
from worker.processor import JobProcessor


def _msg():
    return JobMessage("job-1", "sisr-inputs", "job-1/input.png", 4, "2026-06-30T12:00:00Z")


def test_process_sucesso():
    state, storage, model = Mock(), Mock(), Mock()
    storage.download.return_value = b"input"
    model.upscale.return_value = b"output"
    proc = JobProcessor(state, storage, model, output_bucket="sisr-outputs")

    proc.process(_msg())

    state.mark_processing.assert_called_once_with("job-1")
    storage.download.assert_called_once_with("sisr-inputs", "job-1/input.png")
    model.upscale.assert_called_once_with(b"input", 4)
    storage.upload.assert_called_once_with(
        "sisr-outputs", "job-1/output.png", b"output", "image/png")
    state.mark_done.assert_called_once_with("job-1", "job-1/output.png")
    state.mark_failed.assert_not_called()


def test_process_falha_marca_failed_e_propaga():
    state, storage, model = Mock(), Mock(), Mock()
    storage.download.side_effect = RuntimeError("boom")
    proc = JobProcessor(state, storage, model, output_bucket="sisr-outputs")

    with pytest.raises(RuntimeError):
        proc.process(_msg())

    state.mark_failed.assert_called_once()
    assert "boom" in state.mark_failed.call_args.args[1]
    state.mark_done.assert_not_called()
