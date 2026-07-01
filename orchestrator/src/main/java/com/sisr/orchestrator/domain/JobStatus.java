package com.sisr.orchestrator.domain;

/**
 * Estados possíveis de um job e as transições válidas.
 *
 *   PENDING ──► PROCESSING ──► DONE
 *                          └─► FAILED
 *
 * DONE e FAILED são terminais.
 */
public enum JobStatus {

    PENDING,
    PROCESSING,
    DONE,
    FAILED;

    public boolean canTransitionTo(JobStatus target) {
        return switch (this) {
            case PENDING -> target == PROCESSING;
            case PROCESSING -> target == DONE || target == FAILED;
            case DONE, FAILED -> false; // terminais: não saem mais
        };
    }

    public boolean isTerminal() {
        return this == DONE || this == FAILED;
    }
}
