package com.sisr.orchestrator.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sisr.orchestrator.domain.Job;
import com.sisr.orchestrator.domain.JobStatus;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Teste de INTEGRAÇÃO do RedisJobRepository contra um Redis REAL (Testcontainers).
 *
 * @DataRedisTest sobe só a camada de Redis (sem web/fila). @Testcontainers sobe
 * um container redis:7-alpine efêmero, e @DynamicPropertySource aponta o app
 * para o host/porta dele. Sem mock — valida o contrato real com o Redis.
 */
@DataRedisTest
@Testcontainers
@Import(RedisJobRepository.class)
class RedisJobRepositoryIntegrationTest {

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    JobRepository repository;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Test
    void salvaERecuperaUmJob() {
        var agora = Instant.parse("2026-06-30T12:00:00Z");
        var job = Job.pending("job-1", 4, "job-1/input.png", agora);

        repository.save(job);
        Optional<Job> encontrado = repository.findById("job-1");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().status()).isEqualTo(JobStatus.PENDING);
        assertThat(encontrado.get().scale()).isEqualTo(4);
        assertThat(encontrado.get().inputKey()).isEqualTo("job-1/input.png");
        assertThat(encontrado.get().createdAt()).isEqualTo(agora);
    }

    @Test
    void retornaVazioQuandoNaoExiste() {
        assertThat(repository.findById("nao-existe")).isEmpty();
    }

    @Test
    void salvaComTtl() {
        repository.save(Job.pending("job-ttl", 2, "job-ttl/input.png",
                Instant.parse("2026-06-30T12:00:00Z")));

        Long ttl = redisTemplate.getExpire("job:job-ttl");
        assertThat(ttl).isPositive();
    }
}
