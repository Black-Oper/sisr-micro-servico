package com.sisr.orchestrator.repository;

import java.util.Optional;

import com.sisr.orchestrator.domain.Job;

public interface JobRepository {

    void save(Job job);

    Optional<Job> findById(String jobId);
}
