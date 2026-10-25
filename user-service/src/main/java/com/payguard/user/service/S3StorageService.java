package com.payguard.user.service;

import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
public class S3StorageService {

    private final MinioClient client;
    private final String bucket;
    private final int expiration;

    public S3StorageService(
            MinioClient client,
            @Value("${minio.bucket}") String bucket,
            @Value("${jwt.expiration}") int expiration // Link to photo expires within jwt token
    ) {
        this.client = client;
        this.bucket = bucket;
        this.expiration = expiration / 1000; // mills to secs
    }

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getContentType() == null) {
            return null;
        }

        String key = "avatars/" + UUID.randomUUID();
        String contentType = file.getContentType();

        try (InputStream stream = file.getInputStream()) {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(stream, file.getSize(), -1L)
                            .contentType(contentType)
                            .build()
            );

            return key;
        } catch (Exception e) {
            log.error("Failed to upload photo to MinIO - {}", e.getMessage());
            return null;
        }
    }

    public String getPhotoUrl(String key) {
        if (key == null) return null;
        try {
            return client.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Http.Method.GET)
                            .bucket(bucket)
                            .object(key)
                            .expiry(expiration)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed generate photo URL from MinIO - {}", e.getMessage());
            return null;
        }
    }
}
