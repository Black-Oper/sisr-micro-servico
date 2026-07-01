package com.sisr.orchestrator.domain;

import java.time.Instant;

public record Job(
        String id,
        JobStatus status,
        int scale,
        String inputKey,
        String outputKey,
        Instant createdAt,
        Instant startedAt,
        Instant finishedAt,
        String error) {

    public static Job pending(String id, int scale, String inputKey, Instant createdAt) {
        return new Job(id, JobStatus.PENDING, scale, inputKey, null, createdAt, null, null, null);
    }
}
