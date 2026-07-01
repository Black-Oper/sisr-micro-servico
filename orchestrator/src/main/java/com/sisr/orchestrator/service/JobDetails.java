package com.sisr.orchestrator.service;

import java.time.Instant;

import com.sisr.orchestrator.domain.JobStatus;

public record JobDetails(
        String jobId,
        JobStatus status,
        int scale,
        Instant createdAt,
        Instant startedAt,
        Instant finishedAt,
        String error) {
}
