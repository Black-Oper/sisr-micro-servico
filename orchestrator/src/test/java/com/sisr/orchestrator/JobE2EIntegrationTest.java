package com.sisr.orchestrator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.sisr.orchestrator.messaging.JobMessage;
import com.sisr.orchestrator.storage.ArtifactStorage;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Teste E2E (capstone): exercita o fluxo completo de criação de job contra
 * Redis + MinIO + RabbitMQ REAIS (Testcontainers), através do contexto Spring
 * inteiro. POST /jobs e confere que o estado caiu no Redis, a imagem no MinIO
 * e a mensagem na fila.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class JobE2EIntegrationTest {

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Container
    static MinIOContainer minio = new MinIOContainer("minio/minio");

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
        registry.add("minio.endpoint", () -> minio.getHost() + ":" + minio.getMappedPort(9000));
        registry.add("minio.access-key", minio::getUserName);
        registry.add("minio.secret-key", minio::getPassword);
        registry.add("security.api-key", () -> "e2e-key");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ArtifactStorage storage;

    @Test
    void fluxoCompletoDeCriacaoDeJob() throws Exception {
        var file = new MockMultipartFile("file", "in.png", "image/png", "fake-image".getBytes());

        MvcResult result = mockMvc.perform(
                        multipart("/api/v1/jobs").file(file).param("scale", "4")
                                .header("X-API-Key", "e2e-key"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.jobId").isNotEmpty())
                .andReturn();

        String jobId = JsonPath.read(result.getResponse().getContentAsString(), "$.jobId");

        // Redis: hash job:{id} com status PENDING
        Object status = redisTemplate.opsForHash().get("job:" + jobId, "status");
        assertThat(status).isEqualTo("PENDING");

        // MinIO: imagem de entrada armazenada (bytes idênticos)
        byte[] armazenado = storage.download("sisr-inputs", jobId + "/input.png");
        assertThat(armazenado).isEqualTo("fake-image".getBytes());

        // RabbitMQ: 1 mensagem publicada com o jobId
        rabbitTemplate.setReceiveTimeout(5000);
        JobMessage msg = (JobMessage) rabbitTemplate.receiveAndConvert(
                com.sisr.orchestrator.config.RabbitConfig.QUEUE);
        assertThat(msg).isNotNull();
        assertThat(msg.jobId()).isEqualTo(jobId);
        assertThat(msg.inputKey()).isEqualTo(jobId + "/input.png");
    }
}
