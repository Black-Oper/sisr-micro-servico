package com.sisr.orchestrator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;

@Configuration
public class MinioConfig {

    @Bean
    MinioClient minioClient(
            @Value("${minio.endpoint}") String endpoint,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.secure:false}") boolean secure) {
        String url = (secure ? "https" : "http") + "://" + endpoint;
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }
}
