package com.sisr.orchestrator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.sisr.orchestrator.storage.ArtifactStorage;


@Component
public class StorageInitializer implements ApplicationRunner {

    private final ArtifactStorage storage;
    private final String inputBucket;
    private final String outputBucket;

    public StorageInitializer(
            ArtifactStorage storage,
            @Value("${minio.bucket-input}") String inputBucket,
            @Value("${minio.bucket-output}") String outputBucket) {
        this.storage = storage;
        this.inputBucket = inputBucket;
        this.outputBucket = outputBucket;
    }

    @Override
    public void run(ApplicationArguments args) {
        storage.ensureBucket(inputBucket);
        storage.ensureBucket(outputBucket);
    }
}
