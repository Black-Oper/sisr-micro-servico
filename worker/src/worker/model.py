
import io
from typing import Protocol

from PIL import Image


class Model(Protocol):
    def upscale(self, image_bytes: bytes, scale: int) -> bytes:
        ...


class FakeModel:


    def upscale(self, image_bytes: bytes, scale: int) -> bytes:
        img = Image.open(io.BytesIO(image_bytes))
        nova = img.resize((img.width * scale, img.height * scale))
        saida = io.BytesIO()
        nova.save(saida, format="PNG")
        return saida.getvalue()
