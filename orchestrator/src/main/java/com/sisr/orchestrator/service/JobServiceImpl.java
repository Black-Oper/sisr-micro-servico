package com.sisr.orchestrator.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sisr.orchestrator.domain.Job;
import com.sisr.orchestrator.domain.JobStatus;
import com.sisr.orchestrator.domain.ScaleFactor;
import com.sisr.orchestrator.messaging.JobMessage;
import com.sisr.orchestrator.messaging.JobPublisher;
import com.sisr.orchestrator.repository.JobRepository;
import com.sisr.orchestrator.storage.ArtifactStorage;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository repository;
    private final ArtifactStorage storage;
    private final JobPublisher publisher;
    private final String inputBucket;
    private final String outputBucket;

    public JobServiceImpl(JobRepository repository, ArtifactStorage storage,
                          JobPublisher publisher,
                          @Value("${minio.bucket-input}") String inputBucket,
                          @Value("${minio.bucket-output}") String outputBucket) {
        this.repository = repository;
        this.storage = storage;
        this.publisher = publisher;
        this.inputBucket = inputBucket;
        this.outputBucket = outputBucket;
    }

    @Override
    public CreateJobResult createJob(MultipartFile file, ScaleFactor scale) {
        String jobId = UUID.randomUUID().toString();
        String inputKey = jobId + "/input." + extensaoDe(file.getOriginalFilename());
        Instant createdAt = Instant.now();

        // 1) sobe a imagem de entrada no MinIO
        storage.upload(inputBucket, inputKey, bytesDe(file), file.getContentType());

        // 2) cria o job (PENDING) no Redis
        repository.save(Job.pending(jobId, scale.value(), inputKey, createdAt));

        // 3) publica a mensagem na fila
        publisher.publish(new JobMessage(
                jobId, inputBucket, inputKey, scale.value(), createdAt.toString()));

        return new CreateJobResult(jobId, JobStatus.PENDING, createdAt);
    }

    @Override
    public Optional<JobDetails> findJob(String jobId) {
        return repository.findById(jobId).map(JobServiceImpl::toDetails);
    }

    @Override
    public JobResult getResult(String jobId) {
        Job job = repository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
        if (job.status() != JobStatus.DONE) {
            throw new JobNotReadyException(jobId);
        }
        byte[] content = storage.download(outputBucket, job.outputKey());
        return new JobResult(content, "image/png");
    }

    private static byte[] bytesDe(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("erro ao ler o arquivo de entrada", e);
        }
    }

    private static String extensaoDe(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        }
        return "png";
    }

    private static JobDetails toDetails(Job job) {
        return new JobDetails(
                job.id(), job.status(), job.scale(),
                job.createdAt(), job.startedAt(), job.finishedAt(), job.error());
    }
}
