package com.sisr.orchestrator.repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.sisr.orchestrator.domain.Job;
import com.sisr.orchestrator.domain.JobStatus;

@Repository
public class RedisJobRepository implements JobRepository {

    private static final String KEY_PREFIX = "job:";

    private final StringRedisTemplate redis;

    public RedisJobRepository(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void save(Job job) {
        Map<String, String> hash = new HashMap<>();
        hash.put("status", job.status().name());
        hash.put("scale", String.valueOf(job.scale()));
        hash.put("inputKey", job.inputKey());
        hash.put("createdAt", job.createdAt().toString());
        putIfPresent(hash, "outputKey", job.outputKey());
        putIfPresent(hash, "startedAt", job.startedAt());
        putIfPresent(hash, "finishedAt", job.finishedAt());
        putIfPresent(hash, "error", job.error());

        redis.opsForHash().putAll(KEY_PREFIX + job.id(), hash);
    }

    @Override
    public Optional<Job> findById(String jobId) {
        Map<Object, Object> hash = redis.opsForHash().entries(KEY_PREFIX + jobId);
        if (hash.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Job(
                jobId,
                JobStatus.valueOf(str(hash, "status")),
                Integer.parseInt(str(hash, "scale")),
                str(hash, "inputKey"),
                str(hash, "outputKey"),
                Instant.parse(str(hash, "createdAt")),
                instantOrNull(hash, "startedAt"),
                instantOrNull(hash, "finishedAt"),
                str(hash, "error")));
    }

    private static void putIfPresent(Map<String, String> hash, String field, Object value) {
        if (value != null) {
            hash.put(field, value.toString());
        }
    }

    private static String str(Map<Object, Object> hash, String field) {
        Object v = hash.get(field);
        return v == null ? null : v.toString();
    }

    private static Instant instantOrNull(Map<Object, Object> hash, String field) {
        String v = str(hash, field);
        return v == null ? null : Instant.parse(v);
    }
}
