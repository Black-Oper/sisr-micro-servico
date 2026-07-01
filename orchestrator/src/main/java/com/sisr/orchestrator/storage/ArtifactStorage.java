package com.sisr.orchestrator.storage;

public interface ArtifactStorage {

    void ensureBucket(String bucket);

    void upload(String bucket, String key, byte[] content, String contentType);

    byte[] download(String bucket, String key);
}
