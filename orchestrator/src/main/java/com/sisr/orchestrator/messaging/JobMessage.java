package com.sisr.orchestrator.messaging;

public record JobMessage(
        String jobId,
        String inputBucket,
        String inputKey,
        int scale,
        String createdAt) {
}
