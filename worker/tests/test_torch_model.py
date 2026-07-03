import io
from pathlib import Path

import pytest
from PIL import Image

# Sem PyTorch instalado (ex.: no CI) este módulo inteiro é pulado.
pytest.importorskip("torch")

from worker.torch_model import TorchModel  # noqa: E402

WEIGHTS = Path(__file__).resolve().parent.parent / "models" / "RTDVSR_best_model.pth"

pytestmark = pytest.mark.skipif(
    not WEIGHTS.exists(), reason="pesos do modelo ausentes (worker/models/)")


@pytest.fixture(scope="module")
def model():
    return TorchModel(str(WEIGHTS))


def test_torch_model_faz_upscale_2x(model):
    buf = io.BytesIO()
    Image.new("RGB", (16, 16), "blue").save(buf, format="PNG")

    out = model.upscale(buf.getvalue(), scale=2)

    img = Image.open(io.BytesIO(out))
    assert img.size == (32, 32)  # 16 * scale(2)
    assert img.mode == "RGB"
