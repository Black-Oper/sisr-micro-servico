package com.sisr.orchestrator.service;

import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.sisr.orchestrator.domain.ScaleFactor;

public interface JobService {

    CreateJobResult createJob(MultipartFile file, ScaleFactor scale);

    Optional<JobDetails> findJob(String jobId);

    /**
     * Baixa o resultado de um job concluído.
     *
     * @throws JobNotFoundException se o job não existir
     * @throws JobNotReadyException se o job existir mas não estiver DONE
     */
    JobResult getResult(String jobId);
}
