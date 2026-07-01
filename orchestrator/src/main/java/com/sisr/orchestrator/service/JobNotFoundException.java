package com.sisr.orchestrator.service;

public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(String jobId) {
        super("job não encontrado: " + jobId);
    }
}
