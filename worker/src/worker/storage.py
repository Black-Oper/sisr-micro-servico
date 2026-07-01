
import io


class ArtifactStorage:
    def __init__(self, client):
        self._client = client

    def ensure_bucket(self, bucket: str) -> None:
        if not self._client.bucket_exists(bucket):
            self._client.make_bucket(bucket)

    def download(self, bucket: str, key: str) -> bytes:
        resp = self._client.get_object(bucket, key)
        try:
            return resp.read()
        finally:
            resp.close()
            resp.release_conn()

    def upload(self, bucket: str, key: str, data: bytes, content_type: str) -> None:
        self._client.put_object(
            bucket, key, io.BytesIO(data),
            length=len(data), content_type=content_type,
        )
