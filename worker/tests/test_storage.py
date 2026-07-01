import pytest
from minio import Minio
from testcontainers.minio import MinioContainer

from worker.storage import ArtifactStorage


@pytest.fixture(scope="module")
def storage():
    container = MinioContainer()
    container.start()
    try:
        cfg = container.get_config()
        client = Minio(
            cfg["endpoint"],
            access_key=cfg["access_key"],
            secret_key=cfg["secret_key"],
            secure=False,
        )
        yield ArtifactStorage(client)
    finally:
        container.stop()


def test_sobe_e_baixa_um_objeto(storage):
    storage.ensure_bucket("sisr-inputs")
    conteudo = b"imagem-fake"

    storage.upload("sisr-inputs", "job-1/input.png", conteudo, "image/png")
    baixado = storage.download("sisr-inputs", "job-1/input.png")

    assert baixado == conteudo
