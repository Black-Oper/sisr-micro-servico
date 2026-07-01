
from pathlib import Path

from worker.config import Config
from worker.model import FakeModel
from worker.runner import run_consumer


def _build_model(config):
    if Path(config.model_path).exists():
        from worker.torch_model import TorchModel  # import tardio (so quando ha pesos)
        print("[worker] carregando modelo real de %s" % config.model_path, flush=True)
        return TorchModel(config.model_path)
    print("[worker] pesos nao encontrados em %s; usando FakeModel." % config.model_path,
          flush=True)
    return FakeModel()


def main() -> None:
    config = Config.from_env()
    run_consumer(config, _build_model(config))


if __name__ == "__main__":
    main()
