package com.virtualpet.common.storage;

import java.io.InputStream;
import java.util.Optional;

/**
 * Port for object storage operations.
 *
 * <p>Default implementation is {@link NoOpStorageService}. Replace with an S3-backed implementation
 * (e.g. {@code S3StorageService}) when AWS credentials are available.
 */
public interface StorageService {

  /**
   * Uploads an object and returns its public URL.
   *
   * @param key unique storage key (e.g. "catalog/images/abc123.jpg")
   * @param content raw bytes
   * @param contentType MIME type
   * @return public URL of the uploaded object
   */
  String upload(String key, InputStream content, String contentType);

  /**
   * Returns the public URL for a given key, if it exists.
   *
   * @param key storage key
   * @return Optional URL
   */
  Optional<String> getUrl(String key);

  /**
   * Deletes an object by key.
   *
   * @param key storage key
   */
  void delete(String key);
}
