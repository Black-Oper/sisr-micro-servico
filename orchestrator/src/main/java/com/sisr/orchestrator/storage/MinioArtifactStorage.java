package com.sisr.orchestrator.storage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.stereotype.Component;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

@Component
public class MinioArtifactStorage implements ArtifactStorage {

    private final MinioClient client;

    public MinioArtifactStorage(MinioClient client) {
        this.client = client;
    }

    @Override
    public void ensureBucket(String bucket) {
        try {
            boolean existe = client.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build());
            if (!existe) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new StorageException("erro ao garantir o bucket " + bucket, e);
        }
    }

    @Override
    public void upload(String bucket, String key, byte[] content, String contentType) {
        try (InputStream in = new ByteArrayInputStream(content)) {
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(in, content.length, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new StorageException("erro ao subir " + bucket + "/" + key, e);
        }
    }

    @Override
    public byte[] download(String bucket, String key) {
        try (GetObjectResponse in = client.getObject(
                GetObjectArgs.builder().bucket(bucket).object(key).build())) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new StorageException("erro ao baixar " + bucket + "/" + key, e);
        }
    }
}
