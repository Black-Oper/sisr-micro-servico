package com.sisr.orchestrator.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.minio.MinioClient;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Teste de INTEGRAÇÃO do MinioArtifactStorage contra um MinIO REAL (Testcontainers).
 * Constrói o storage direto com um MinioClient apontando para o container.
 */
@Testcontainers
class MinioArtifactStorageIntegrationTest {

    @Container
    static MinIOContainer minio = new MinIOContainer("minio/minio");

    ArtifactStorage storage;

    @BeforeEach
    void setup() {
        MinioClient client = MinioClient.builder()
                .endpoint(minio.getS3URL())
                .credentials(minio.getUserName(), minio.getPassword())
                .build();
        storage = new MinioArtifactStorage(client);
    }

    @Test
    void subaEBaixaUmObjeto() {
        storage.ensureBucket("sisr-inputs");
        byte[] conteudo = "imagem-fake".getBytes(StandardCharsets.UTF_8);

        storage.upload("sisr-inputs", "job-1/input.png", conteudo, "image/png");
        byte[] baixado = storage.download("sisr-inputs", "job-1/input.png");

        assertThat(baixado).isEqualTo(conteudo);
    }

    @Test
    void ensureBucketEhIdempotente() {
        storage.ensureBucket("sisr-inputs");
        assertThatCode(() -> storage.ensureBucket("sisr-inputs"))
                .doesNotThrowAnyException();
    }
}
