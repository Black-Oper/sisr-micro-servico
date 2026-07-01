package com.sisr.orchestrator.service;

public class JobNotReadyException extends RuntimeException {

    public JobNotReadyException(String jobId) {
        super("job ainda não concluído: " + jobId);
    }
}
