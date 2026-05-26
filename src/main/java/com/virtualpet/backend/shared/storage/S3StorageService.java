package com.virtualpet.backend.shared.storage;

import java.io.InputStream;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Slf4j
@Service
@Profile("prod")
public class S3StorageService implements StorageService {

    private final S3Client s3;
    private final String bucket;
    private final String region;

    public S3StorageService(S3Client s3,
                            @Value("${aws.s3.bucket}") String bucket,
                            @Value("${aws.region}") String region) {
        this.s3 = s3;
        this.bucket = bucket;
        this.region = region;
    }

    @Override
    public String upload(String key, InputStream content, String contentType) {
        s3.putObject(
            PutObjectRequest.builder()
                .bucket(bucket).key(key).contentType(contentType).build(),
            RequestBody.fromInputStream(content, -1)
        );
        return getUrl(key).orElseThrow();
    }

    @Override
    public Optional<String> getUrl(String key) {
        return Optional.of(
            "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key
        );
    }

    @Override
    public void delete(String key) {
        s3.deleteObject(
            DeleteObjectRequest.builder().bucket(bucket).key(key).build()
        );
    }
}