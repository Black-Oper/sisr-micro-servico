package com.sisr.orchestrator.messaging;

public interface JobPublisher {

    void publish(JobMessage message);
}
