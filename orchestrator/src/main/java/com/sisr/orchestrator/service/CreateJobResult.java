package com.sisr.orchestrator.service;

import java.time.Instant;

import com.sisr.orchestrator.domain.JobStatus;

public record CreateJobResult(String jobId, JobStatus status, Instant createdAt) {
}
