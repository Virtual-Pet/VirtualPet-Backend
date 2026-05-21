package com.virtualpet.backend.shared.storage;

import java.io.InputStream;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

/**
 * No-operation implementation of {@link StorageService}.
 *
 * <p>Active by default when no other {@link StorageService} bean is present (i.e. when AWS S3 is
 * not configured). Logs a warning on every call so the gap is visible during development.
 *
 * <p>To enable real S3 storage:
 *
 * <ol>
 *   <li>Add the AWS SDK dependency to pom.xml: {@code software.amazon.awssdk:s3}
 *   <li>Create a {@code S3StorageService} that implements {@link StorageService}
 *   <li>Configure {@code AWS_BUCKET_NAME}, {@code AWS_REGION} environment variables
 * </ol>
 */
@Slf4j
@Service
@ConditionalOnMissingBean(value = StorageService.class, ignored = NoOpStorageService.class)
public class NoOpStorageService implements StorageService {

  @Override
  public String upload(String key, InputStream content, String contentType) {
    log.warn(
        "[StorageService] No-op: upload() called for key='{}'. Configure S3 to enable real storage.",
        key);
    return "https://placeholder.virtualpet.local/" + key;
  }

  @Override
  public Optional<String> getUrl(String key) {
    log.warn(
        "[StorageService] No-op: getUrl() called for key='{}'. Configure S3 to enable real storage.",
        key);
    return Optional.empty();
  }

  @Override
  public void delete(String key) {
    log.warn(
        "[StorageService] No-op: delete() called for key='{}'. Configure S3 to enable real storage.",
        key);
  }
}
