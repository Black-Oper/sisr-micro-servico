import io

from PIL import Image

from worker.model import FakeModel


def _png(width: int, height: int) -> bytes:
    buf = io.BytesIO()
    Image.new("RGB", (width, height), "white").save(buf, format="PNG")
    return buf.getvalue()


def test_fake_model_aumenta_dimensoes():
    entrada = _png(2, 2)

    saida = FakeModel().upscale(entrada, scale=4)

    img = Image.open(io.BytesIO(saida))
    assert img.size == (8, 8)
