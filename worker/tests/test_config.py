from worker.config import Config


def test_config_le_do_ambiente():
    env = {
        "RABBITMQ_HOST": "rabbit",
        "RABBITMQ_PORT": "5673",
        "REDIS_HOST": "redis-x",
        "MINIO_ENDPOINT": "minio:9000",
        "MINIO_SECURE": "true",
        "MODEL_SCALE": "2",
    }

    cfg = Config.from_env(env)

    assert cfg.rabbitmq_host == "rabbit"
    assert cfg.rabbitmq_port == 5673
    assert cfg.redis_host == "redis-x"
    assert cfg.minio_endpoint == "minio:9000"
    assert cfg.minio_secure is True
    assert cfg.model_scale == 2


def test_config_usa_defaults():
    cfg = Config.from_env({})

    assert cfg.rabbitmq_host == "localhost"
    assert cfg.rabbitmq_port == 5672
    assert cfg.queue == "sisr.jobs.queue"
    assert cfg.minio_secure is False
    assert cfg.bucket_input == "sisr-inputs"
    assert cfg.bucket_output == "sisr-outputs"
    assert cfg.model_path == "/app/models/RTDVSR_best_model.pth"
