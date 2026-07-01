
import io

import torch
from PIL import Image
from torchvision.transforms.functional import to_pil_image, to_tensor

from worker.rtdvsr import RTDVSR


class TorchModel:
    def __init__(self, weights_path: str, device: str = "cpu"):
        self._device = torch.device(device)
        ck = torch.load(weights_path, map_location=self._device, weights_only=False)

        params = ck.get("model_params", {}) if isinstance(ck, dict) else {}
        cfg = ck.get("config", {}) if isinstance(ck, dict) else {}
        self._scale = int(cfg.get("scale_factor", 2))

        net = RTDVSR(
            scale_factor=self._scale,
            channels=3,
            hidden_dim=int(params.get("hidden_dim", 64)),
            num_res_blocks=int(params.get("num_res_blocks", 4)),
        )
        state = ck["model"] if isinstance(ck, dict) and "model" in ck else ck
        net.load_state_dict(state)
        net.eval().to(self._device)
        self._net = net

    def upscale(self, image_bytes: bytes, scale: int) -> bytes:
        # o modelo tem fator fixo (self._scale); o `scale` do request e ignorado.
        img = Image.open(io.BytesIO(image_bytes)).convert("RGB")
        x = to_tensor(img).unsqueeze(0).to(self._device)  # (1, 3, H, W) em [0, 1]

        with torch.no_grad():
            sr, _ = self._net(x, None)

        sr = sr.squeeze(0).clamp(0, 1).cpu()
        out = to_pil_image(sr)
        buf = io.BytesIO()
        out.save(buf, format="PNG")
        return buf.getvalue()
